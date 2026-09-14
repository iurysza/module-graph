---
name: skill-cleaner
description: Audits installed Agent Skills for duplicates, unused candidates, loaded roots, oversized descriptions, and prompt cost. Use when cleaning or debugging skill collections.
compatibility: Requires Node.js 18 or newer. Usage-history analysis supports Codex and OpenClaw logs.
metadata:
  category: agent-workspace
---

# Skill Cleaner

Use this when trimming skill prompt budget, finding duplicate skills, auditing enabled/disabled skill roots, or deciding which skills/plugins to remove.

## Workflow

1. Resolve this skill's directory from the loaded `SKILL.md`, then run its bundled analyzer:

```bash
node ./scripts/skill-cleaner.mjs --no-logs
```

Useful variants:

```bash
node ./scripts/skill-cleaner.mjs --months 3
node ./scripts/skill-cleaner.mjs --months 6 --max-log-mb 800 --deep-logs
node ./scripts/skill-cleaner.mjs --context-tokens 200000 --budget-percent 2 --no-logs
node ./scripts/skill-cleaner.mjs --root /path/to/another/skills-directory --no-logs
node ./scripts/skill-cleaner.mjs --json --no-logs
```

2. Read the report in this order:
- `Skill Budget`: GPT-5.5 context size, 2% skills budget, Codex-budgeted usage, and pre-budget full-list pressure.
- `Description candidates`: long descriptions where relaxed grammar saves prompt budget.
- `Duplicates`: same skill name or near-identical description/body across Codex, plugin cache, repo siblings, and personal skill roots.
- `Unused candidates`: no recent `$skill` mention, `SKILL.md` read, or explicit skill-use trace in recent Codex/OpenClaw logs.
- `Root summary`: where skills came from and whether config marks them disabled.

3. Before deleting or editing:
- Verify the kept copy exists and is loaded.
- Treat cross-runtime duplicates as informational: separate runtimes may legitimately need separate copies.
- Never delete managed plugin-cache files directly; use the runtime's plugin manager.
- Delete a suggested duplicate only after verifying the kept path exists and the same runtime loads it.
- Keep repo-local OpenClaw maintainer skills when they encode repo policy or live operations.
- Preserve trigger nouns in descriptions: product, tool, action, object.

## Analyzer Notes

- The script mirrors Codex's model-visible line shape: `- name: description (file: path)`.
- It applies Codex-like frontmatter rules: YAML frontmatter only, default name from parent dir, single-line sanitized `name` and `description`.
- It follows Codex `core-skills/src/render.rs`: 2% of raw `context_window`, token cost `ceil(utf8_bytes / 4)`, then full descriptions -> equal description truncation -> omitted minimum lines.
- It reads `~/.codex/models_cache.json` for the selected model's `context_window`; fallback is 200,000 tokens and 2%. Pass `--context-tokens` for an exact budget.
- It scans common global roots for `.agents`, Pi, Codex, Claude Code, OpenCode, and OpenClaw, plus skill roots in the current project and its ancestors.
- Extra folders are included only with repeatable `--root <path>` flags.
- It realpath-dedupes roots, so agent-specific symlinks to a shared skill directory do not create false duplicates.
- For duplicate names, it reports description/body similarity and suggests deletion candidates only when bodies are near copies. Keep priority defaults to direct Codex system skills, then direct Codex skills, then plugin skills, then personal/repo copies.
- It scans `~/.codex/history.jsonl` and recent `~/.codex/sessions/**/*.jsonl` by default. Add `--deep-logs` for archived sessions and common OpenClaw/Clawd log folders.
- Usage evidence is heuristic: `$skill`, `Use $skill`, and paths like `skills/<name>/SKILL.md`.

## Privacy and Output Policy

- Scanning stays local. The report emits paths and usage counts, not session contents.
- Use `--no-logs` unless the user wants usage-history analysis.
- Reports can contain usernames and local paths; review them before sharing.
- Suggest first; edit only when the user asks.
- If asked to apply cleanup, make small grouped commits: descriptions, deletes, config disables.
- Do not delete ignored/untracked skill dirs without naming the destination or confirming they are disposable.
