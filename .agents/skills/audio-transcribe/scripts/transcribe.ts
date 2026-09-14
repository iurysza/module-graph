#!/usr/bin/env bun
import { existsSync, mkdirSync, rmSync } from "fs";
import { basename, dirname, extname, join, resolve } from "path";
import { parseArgs } from "util";

const DEFAULT_CHUNK_MINUTES = 30;
const STRUCTURED_MAX_CHUNK_MINUTES = 30;
const SMART_MAX_CHUNK_MINUTES = 60;
const SUPPORTED_FORMATS = [".mp3", ".wav", ".m4a", ".ogg", ".flac", ".webm"];
const MODEL_NAME = process.env.GEMINI_TRANSCRIBE_MODEL || "gemini-3.5-transcribe";

type TranscriptionMode = "verbatim" | "smart";

interface AudioChunk {
  path: string;
  offsetSeconds: number;
  durationSeconds: number;
}

interface TranscribeOptions {
  chunkMinutes: number;
  customVocabulary: string[];
  keep: boolean;
  languageCodes: string[];
  mode: TranscriptionMode;
  outputPath: string;
}

interface WordAnnotation {
  end_offset?: string;
  speaker?: string;
  start_offset?: string;
  text?: string;
  type?: string;
}

interface InteractionContent {
  annotations?: WordAnnotation[];
}

interface InteractionStep {
  content?: InteractionContent[];
}

interface TranscriptionResponse {
  output_text?: string;
  steps?: InteractionStep[];
}

async function run(
  command: string[],
): Promise<{ stdout: string; stderr: string; exitCode: number }> {
  const process = Bun.spawn(command, { stdout: "pipe", stderr: "pipe" });
  const stdout = await new Response(process.stdout).text();
  const stderr = await new Response(process.stderr).text();
  return {
    stdout: stdout.trim(),
    stderr: stderr.trim(),
    exitCode: await process.exited,
  };
}

async function getDuration(filePath: string): Promise<number> {
  const result = await run([
    "ffprobe",
    "-i",
    filePath,
    "-show_entries",
    "format=duration",
    "-v",
    "quiet",
    "-of",
    "csv=p=0",
  ]);
  const duration = Number.parseFloat(result.stdout);
  if (result.exitCode !== 0 || !Number.isFinite(duration)) {
    throw new Error(`ffprobe could not read ${filePath}: ${result.stderr}`);
  }
  return duration;
}

function resolveApiKey(): string {
  const candidates = [
    process.env.GEMINI_API_KEY,
    process.env.GOOGLE_API_KEY,
    process.env.OPENCODE_GOOGLE_API_KEY,
  ];
  const key = candidates.find((value) => value?.trim())?.trim();
  if (!key) {
    throw new Error(
      "No Gemini API key configured. Set GEMINI_API_KEY or GOOGLE_API_KEY.",
    );
  }
  return key;
}

async function normalizeAudio(inputPath: string, outputPath: string): Promise<void> {
  console.log(`Normalizing ${basename(inputPath)} for upload...`);
  const result = await run([
    "ffmpeg",
    "-y",
    "-i",
    inputPath,
    "-vn",
    "-ac",
    "1",
    "-ar",
    "16000",
    "-b:a",
    "48k",
    outputPath,
  ]);
  if (result.exitCode !== 0) {
    throw new Error(`ffmpeg normalization failed: ${result.stderr}`);
  }
}

async function splitAudio(
  normalizedPath: string,
  workDir: string,
  duration: number,
  chunkMinutes: number,
): Promise<AudioChunk[]> {
  const chunkSeconds = chunkMinutes * 60;
  const count = Math.ceil(duration / chunkSeconds);
  if (count <= 1) {
    return [{ path: normalizedPath, offsetSeconds: 0, durationSeconds: duration }];
  }

  console.log(`Splitting into ${count} chunks of at most ${chunkMinutes} minutes...`);
  const chunks: AudioChunk[] = [];
  for (let index = 0; index < count; index += 1) {
    const offsetSeconds = index * chunkSeconds;
    const chunkPath = join(workDir, `chunk-${String(index + 1).padStart(3, "0")}.mp3`);
    const result = await run([
      "ffmpeg",
      "-y",
      "-i",
      normalizedPath,
      "-ss",
      String(offsetSeconds),
      "-t",
      String(chunkSeconds),
      "-c",
      "copy",
      chunkPath,
    ]);
    if (result.exitCode !== 0) {
      throw new Error(`ffmpeg failed to create chunk ${index + 1}: ${result.stderr}`);
    }
    chunks.push({
      path: chunkPath,
      offsetSeconds,
      durationSeconds: Math.min(chunkSeconds, duration - offsetSeconds),
    });
  }
  return chunks;
}

export function formatTimestamp(totalSeconds: number): string {
  const seconds = Math.floor(totalSeconds % 60);
  const minutes = Math.floor(totalSeconds / 60) % 60;
  const hours = Math.floor(totalSeconds / 3600);
  return hours > 0
    ? `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`
    : `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

function formatDuration(totalSeconds: number): string {
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = Math.floor(totalSeconds % 60);
  if (hours > 0) return `${hours}h ${minutes}m ${seconds}s`;
  if (minutes > 0) return `${minutes}m ${seconds}s`;
  return `${seconds}s`;
}

function parseOffset(offset: string | undefined): number | undefined {
  if (!offset) return undefined;
  const match = /^(\d+(?:\.\d+)?)s$/.exec(offset);
  if (!match) return undefined;
  const seconds = Number.parseFloat(match[1]);
  return Number.isFinite(seconds) && seconds >= 0 ? seconds : undefined;
}

function speakerName(speaker: string | undefined): string {
  if (!speaker) return "Speaker";
  const match = /^spk[_:](\d+)$/i.exec(speaker);
  return match ? `Speaker ${match[1]}` : speaker;
}

function joinWords(words: string[]): string {
  return words
    .join(" ")
    .replace(/\s+([,.;:!?])/g, "$1")
    .replace(/([([{])\s+/g, "$1")
    .replace(/\s+([)\]}])/g, "$1");
}

export function extractWordAnnotations(response: TranscriptionResponse): WordAnnotation[] {
  const words: WordAnnotation[] = [];
  for (const step of response.steps ?? []) {
    for (const content of step.content ?? []) {
      for (const annotation of content.annotations ?? []) {
        if (annotation.type === "word_info") words.push(annotation);
      }
    }
  }
  return words;
}

export function formatStructuredTranscript(
  annotations: WordAnnotation[],
  offsetSeconds: number,
): string {
  const groups: Array<{ speaker: string; startSeconds: number; words: string[] }> = [];
  const timestampedAnnotations = annotations
    .map((annotation) => ({ annotation, startSeconds: parseOffset(annotation.start_offset) }))
    .filter(
      (entry): entry is { annotation: WordAnnotation; startSeconds: number } =>
        Boolean(entry.annotation.text?.trim()) && entry.startSeconds !== undefined,
    )
    .sort((left, right) => left.startSeconds - right.startSeconds);

  for (const { annotation, startSeconds } of timestampedAnnotations) {
    const word = annotation.text!.trim();
    const speaker = speakerName(annotation.speaker);
    const group = groups.at(-1);
    if (!group || group.speaker !== speaker) {
      groups.push({
        speaker,
        startSeconds: offsetSeconds + startSeconds,
        words: [word],
      });
    } else {
      group.words.push(word);
    }
  }

  if (groups.length === 0) {
    throw new Error("Gemini returned no timestamped word annotations.");
  }

  return groups
    .map(
      (group) =>
        `[${formatTimestamp(group.startSeconds)}] ${group.speaker}: ${joinWords(group.words)}`,
    )
    .join("\n\n");
}

export function formatChunkTranscript(
  transcript: string,
  chunk: AudioChunk,
  index: number,
  total: number,
): string {
  if (total === 1) return transcript;
  const start = formatTimestamp(chunk.offsetSeconds);
  const end = formatTimestamp(chunk.offsetSeconds + chunk.durationSeconds);
  return `## Chunk ${index + 1} (${start} to ${end})\n\n${transcript}`;
}

export function safeProviderError(error: unknown): string {
  if (!error || typeof error !== "object") return "ProviderError";
  const value = error as Record<string, unknown>;
  const constructor = (error as { constructor?: { name?: string } }).constructor;
  const name = constructor?.name?.match(/^[A-Za-z][A-Za-z0-9_]{0,63}$/)?.[0] ?? "ProviderError";
  const details = [name];
  if (typeof value.status === "number") details.push(`status=${value.status}`);
  if (typeof value.statusCode === "number") details.push(`status=${value.statusCode}`);
  if (
    typeof value.code === "string" &&
    /^[A-Za-z0-9_.-]{1,64}$/.test(value.code)
  ) {
    details.push(`code=${value.code}`);
  }
  return details.join(" ");
}

export function retryAfterMilliseconds(error: unknown): number | undefined {
  if (!(error instanceof Error)) return undefined;
  const match = /retry in (\d+(?:\.\d+)?)s/i.exec(error.message);
  if (!match) return undefined;
  return Math.ceil(Number.parseFloat(match[1]) * 1000);
}

async function deleteUploadedFile(client: any, name: string, chunkIndex: number): Promise<void> {
  try {
    await client.files.delete({ name });
  } catch (error) {
    console.warn(
      `Could not delete uploaded audio for chunk ${chunkIndex + 1}: ${safeProviderError(error)}`,
    );
  }
}

async function transcribeChunk(
  client: any,
  chunk: AudioChunk,
  index: number,
  total: number,
  options: TranscribeOptions,
): Promise<string> {
  console.log(`Uploading and transcribing chunk ${index + 1}/${total}...`);
  let uploadedFile: { mimeType?: string; name?: string; uri?: string } | undefined;

  try {
    try {
      uploadedFile = await client.files.upload({
        file: chunk.path,
        config: { mimeType: "audio/mp3" },
      });
      if (!uploadedFile.uri || !uploadedFile.name) {
        throw new Error("Gemini did not return an uploaded file URI and name.");
      }

      const mode =
        options.mode === "smart"
          ? { type: "smart" }
          : {
              type: "verbatim",
              diarization_mode: "speaker",
              timestamp_granularities: ["word"],
            };
      const request = {
        model: MODEL_NAME,
        input: [
          {
            type: "audio",
            uri: uploadedFile.uri,
            mime_type: uploadedFile.mimeType ?? "audio/mp3",
          },
        ],
        generation_config: {
          transcription_config: {
            language_codes: options.languageCodes,
            custom_vocabulary: options.customVocabulary,
            mode,
          },
        },
      };
      let response: TranscriptionResponse;
      try {
        response = (await client.interactions.create(request)) as TranscriptionResponse;
      } catch (error) {
        const retryAfter = retryAfterMilliseconds(error);
        if (retryAfter === undefined) throw error;
        console.warn(
          `Gemini rate limit for chunk ${index + 1}. Retrying after Gemini's ${Math.ceil(retryAfter / 1000)} second delay...`,
        );
        await Bun.sleep(retryAfter);
        response = (await client.interactions.create(request)) as TranscriptionResponse;
      }

      if (options.mode === "smart") {
        const text = response.output_text?.trim();
        if (!text) throw new Error(`Gemini returned no transcript for chunk ${index + 1}.`);
        return text;
      }

      return formatStructuredTranscript(
        extractWordAnnotations(response),
        chunk.offsetSeconds,
      );
    } catch (error) {
      throw new Error(
        `Gemini request failed for chunk ${index + 1}: ${
          error instanceof Error ? error.message : safeProviderError(error)
        }`,
      );
    }
  } finally {
    if (uploadedFile?.name) {
      await deleteUploadedFile(client, uploadedFile.name, index);
    }
  }
}

function formatList(values: string[]): string {
  return values.length === 0 ? "[]" : JSON.stringify(values);
}

async function transcribe(inputPath: string, options: TranscribeOptions): Promise<void> {
  const absoluteInput = resolve(inputPath);
  const outputPath = resolve(options.outputPath);
  if (!existsSync(absoluteInput)) throw new Error(`File not found: ${absoluteInput}`);
  if (absoluteInput === outputPath) throw new Error("Output path must differ from input path.");

  const extension = extname(absoluteInput).toLowerCase();
  if (!SUPPORTED_FORMATS.includes(extension)) {
    throw new Error(`Unsupported format ${extension}. Supported: ${SUPPORTED_FORMATS.join(", ")}`);
  }

  const duration = await getDuration(absoluteInput);
  mkdirSync(dirname(outputPath), { recursive: true });
  const workDir = join(
    dirname(outputPath),
    `.audio-transcribe-work-${process.pid}-${Date.now()}`,
  );
  mkdirSync(workDir, { recursive: true });

  console.log(`Input: ${absoluteInput}`);
  console.log(`Duration: ${formatDuration(duration)}`);
  console.log(`Model: ${MODEL_NAME}`);
  console.log(`Mode: ${options.mode}`);

  try {
    const normalizedPath = join(workDir, "normalized.mp3");
    await normalizeAudio(absoluteInput, normalizedPath);
    const chunks = await splitAudio(
      normalizedPath,
      workDir,
      duration,
      options.chunkMinutes,
    );

    const { GoogleGenAI } = await import("@google/genai");
    const client = new GoogleGenAI({ apiKey: resolveApiKey() });
    const transcripts: string[] = [];
    for (const [index, chunk] of chunks.entries()) {
      const transcript = await transcribeChunk(
        client,
        chunk,
        index,
        chunks.length,
        options,
      );
      transcripts.push(formatChunkTranscript(transcript, chunk, index, chunks.length));
    }

    const output = `---
source: ${JSON.stringify(basename(absoluteInput))}
duration_seconds: ${Math.round(duration)}
chunks: ${chunks.length}
model: ${JSON.stringify(MODEL_NAME)}
mode: ${JSON.stringify(options.mode)}
language_codes: ${formatList(options.languageCodes)}
custom_vocabulary: ${formatList(options.customVocabulary)}
transcribed: ${JSON.stringify(new Date().toISOString())}
---

# Transcript: ${basename(absoluteInput, extension)}

${transcripts.join("\n\n")}
`;
    await Bun.write(outputPath, output);
    const outputFile = Bun.file(outputPath);
    if (!(await outputFile.exists()) || outputFile.size === 0) {
      throw new Error(`Transcript was not written: ${outputPath}`);
    }
    console.log(`Transcript saved: ${outputPath}`);
    if (options.keep) console.log(`Intermediate audio retained: ${workDir}`);
  } finally {
    if (!options.keep) rmSync(workDir, { recursive: true, force: true });
  }
}

function stringArray(value: string | string[] | undefined): string[] {
  if (value === undefined) return [];
  return (Array.isArray(value) ? value : [value]).map((item) => item.trim()).filter(Boolean);
}

async function main(): Promise<void> {
  const { values, positionals } = parseArgs({
    args: Bun.argv.slice(2),
    options: {
      "chunk-minutes": { type: "string", default: String(DEFAULT_CHUNK_MINUTES) },
      language: { type: "string", multiple: true },
      vocabulary: { type: "string", multiple: true },
      smart: { type: "boolean", default: false },
      output: { type: "string" },
      keep: { type: "boolean", default: false },
      help: { type: "boolean", short: "h", default: false },
    },
    allowPositionals: true,
  });

  if (values.help || positionals.length === 0) {
    console.log(`Audio Transcription

Usage:
  bun run transcribe.ts <audio-file> [options]

Options:
  --chunk-minutes <n>  Chunk duration in minutes (default: ${DEFAULT_CHUNK_MINUTES})
  --language <code>    BCP-47 language hint. Repeat for each known language.
  --vocabulary <term>  Domain term, acronym, or name. Repeat for each term.
  --smart              Return a cleaned transcript without speaker labels or timestamps.
  --output <path>      Markdown output path (default: beside input)
  --keep               Keep normalized and chunk audio
  -h, --help           Show this help

Chunk limits:
  Verbatim output uses speaker diarization and word timestamps. Gemini limits it to ${STRUCTURED_MAX_CHUNK_MINUTES} minutes per request.
  --smart omits those features and supports up to ${SMART_MAX_CHUNK_MINUTES} minutes per request.

Credentials:
  GEMINI_API_KEY (preferred), GOOGLE_API_KEY, or OPENCODE_GOOGLE_API_KEY

Model:
  GEMINI_TRANSCRIBE_MODEL (default: ${MODEL_NAME})

Supported formats: ${SUPPORTED_FORMATS.join(", ")}`);
    return;
  }

  const mode: TranscriptionMode = values.smart ? "smart" : "verbatim";
  const maximumChunkMinutes =
    mode === "smart" ? SMART_MAX_CHUNK_MINUTES : STRUCTURED_MAX_CHUNK_MINUTES;
  const chunkMinutes = Number.parseInt(values["chunk-minutes"] as string, 10);
  if (!Number.isInteger(chunkMinutes) || chunkMinutes < 1 || chunkMinutes > maximumChunkMinutes) {
    throw new Error(
      `--chunk-minutes must be an integer from 1 to ${maximumChunkMinutes} for ${mode} mode.`,
    );
  }

  const languageCodes = stringArray(values.language);
  const customVocabulary = stringArray(values.vocabulary);
  if (customVocabulary.length > 1000) {
    throw new Error("--vocabulary accepts at most 1000 terms.");
  }
  if (mode === "verbatim" && customVocabulary.length > 0) {
    throw new Error(
      "--vocabulary cannot be combined with verbatim mode because Gemini rejects custom vocabulary with word timestamps. Use --smart for vocabulary-biased transcription.",
    );
  }

  const inputPath = positionals[0];
  const inputExtension = extname(inputPath);
  const defaultOutput = join(
    dirname(inputPath),
    `${basename(inputPath, inputExtension)}.transcript.md`,
  );

  await transcribe(inputPath, {
    chunkMinutes,
    customVocabulary,
    keep: values.keep as boolean,
    languageCodes,
    mode,
    outputPath: (values.output as string | undefined) ?? defaultOutput,
  });
}

if (import.meta.main) {
  main().catch((error: Error) => {
    console.error(`Error: ${error.message}`);
    process.exit(1);
  });
}
