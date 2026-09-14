import { describe, expect, test } from "bun:test";
import {
  extractWordAnnotations,
  formatChunkTranscript,
  formatStructuredTranscript,
  formatTimestamp,
  retryAfterMilliseconds,
} from "./transcribe.ts";

describe("formatTimestamp", () => {
  test("uses hours after the first hour", () => {
    expect(formatTimestamp(65)).toBe("01:05");
    expect(formatTimestamp(3665)).toBe("01:01:05");
  });
});

describe("formatChunkTranscript", () => {
  test("labels speaker scope when Gemini must split the recording", () => {
    expect(
      formatChunkTranscript(
        "[00:00] Speaker 0: Hello.",
        { path: "chunk.mp3", offsetSeconds: 1800, durationSeconds: 95 },
        1,
        2,
      ),
    ).toBe("## Chunk 2 (30:00 to 31:35)\n\n[00:00] Speaker 0: Hello.");
  });

  test("does not add a chunk heading to a single request", () => {
    expect(
      formatChunkTranscript(
        "[00:00] Speaker 0: Hello.",
        { path: "audio.mp3", offsetSeconds: 0, durationSeconds: 20 },
        0,
        1,
      ),
    ).toBe("[00:00] Speaker 0: Hello.");
  });
});

describe("retryAfterMilliseconds", () => {
  test("reads Gemini's directed rate-limit delay", () => {
    expect(retryAfterMilliseconds(new Error("Retry in 59.218353713s."))).toBe(59219);
    expect(retryAfterMilliseconds(new Error("quota exceeded"))).toBeUndefined();
  });
});

describe("extractWordAnnotations", () => {
  test("ignores non-word annotations", () => {
    expect(
      extractWordAnnotations({
        steps: [
          {
            content: [
              {
                annotations: [
                  { type: "citation", text: "ignored" },
                  { type: "word_info", text: "Hello", start_offset: "0.5s" },
                ],
              },
            ],
          },
        ],
      }),
    ).toEqual([{ type: "word_info", text: "Hello", start_offset: "0.5s" }]);
  });
});

describe("formatStructuredTranscript", () => {
  test("groups contiguous words by speaker and applies the chunk offset", () => {
    expect(
      formatStructuredTranscript(
        [
          { type: "word_info", speaker: "spk:1", start_offset: "0.5s", text: "Hello" },
          { type: "word_info", speaker: "spk:1", start_offset: "0.8s", text: "," },
          { type: "word_info", speaker: "spk:1", start_offset: "0.9s", text: "there" },
          { type: "word_info", speaker: "spk:2", start_offset: "2.0s", text: "Hi" },
          { type: "word_info", speaker: "spk:2", start_offset: "2.2s", text: "!" },
        ],
        60,
      ),
    ).toBe("[01:00] Speaker 1: Hello, there\n\n[01:02] Speaker 2: Hi!");
  });

  test("orders provider annotations by audio time", () => {
    expect(
      formatStructuredTranscript(
        [
          { type: "word_info", speaker: "spk:1", start_offset: "2s", text: "Later" },
          { type: "word_info", speaker: "spk:2", start_offset: "0.5s", text: "First" },
        ],
        0,
      ),
    ).toBe("[00:00] Speaker 2: First\n\n[00:02] Speaker 1: Later");
  });

  test("fails when Gemini omits usable timestamp annotations", () => {
    expect(() => formatStructuredTranscript([{ type: "word_info", text: "Hello" }], 0)).toThrow(
      "Gemini returned no timestamped word annotations.",
    );
  });
});
