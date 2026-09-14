---
name: tool-install
description: Plans and executes tool installations or upgrades with explicit approval, impact checks, rollback, and verification. Use only when the user asks to install or update software.
metadata:
  category: development
---

# Tool Install Planner

## Purpose

Turn an "install this" or "update this" request into a fully researched,
user-approved execution plan. The agent must never mutate the system without
explicit sign-off.

```
    +-----------------+     +------------------+     +------------------+
    |  You paste link |---->|  I research tool |---->|  I inspect your  |
    |  or say "install|     |  + env impact    |     |  current system  |
    |  X"             |     |                  |     |                  |
    +-----------------+     +------------------+     +------------------+
                                                          |
                              +---------------------------+
                              v
                       +------------------+
                       |  I generate plan |
                       |  + briefing      |
                       |  + decisions     |
                       |  + rollback      |
                       +------------------+
                              |
                              v
                       +------------------+
                       |  You approve     |
                       |  (or revise)     |
                       +------------------+
                              |
                              v
                       +------------------+
                       |  I execute       |
                       |  + verify        |
                       +------------------+
```

## Workflow

### 1. Ingest the Link

Fetch the provided URL with `fetch_content` or `web_search` to understand:

- What the tool does (1-2 sentences)
- Who uses it: end users directly, developers, automation/orchestration, or AI agents
- Official installation methods (Homebrew, npm, pip, cargo, manual, etc.)
- System requirements and dependencies
- Default install paths and state mutations
- Any post-install setup (config files, env vars, services, login items)

If the user asked to **update** an already-installed tool, also research:

- What's new in the latest version (changelog, release notes)
- Breaking changes or migration steps
- Whether the current install method supports in-place upgrades
- Config file format changes or deprecations

If the link is sparse, run 2-3 targeted `web_search` queries to fill gaps:

- `"<tool-name> installation guide macOS"`
- `"<tool-name> Homebrew formula"`
- `"<tool-name> dependencies requirements"`

### 2. Environment Impact Analysis

Inspect the current environment to determine blast radius. Run diagnostics as
needed

For each impact category, report **what will change**:

| Category           | Questions to Answer                                         |
| ------------------ | ----------------------------------------------------------- |
| **Binaries**       | New executables? Where? Symlinks?                           |
| **Dotfiles**       | New configs in `~/.config`, `~/.<tool>rc`, shell rc files?  |
| **Shell env**      | `PATH`, `alias`, `export`, `eval $(...)` additions?         |
| **Services**       | Background daemon? LaunchAgent? Auto-start?                 |
| **macOS prefs**    | Accessibility permissions? Full Disk Access? Notifications? |
| **Disk space**     | Approximate download + install size                         |
| **Network**        | Firewall rules? Network extensions?                         |
| **Existing tools** | Conflicts with already-installed tools? Replaces anything?  |
| **Uninstall**      | Clean removal possible? Orphans left behind?                |

### 3. Decision Points

Surface choices the user can make. Examples:

- **Install method**: Homebrew vs. manual vs. npm vs. pip
- **Scope**: Global (`-g`, `/usr/local`) vs. user-local (`~/.local`)
- **Version**: Latest vs. pinned version
- **Config**: Default config or import/customize now
- **Service**: Enable auto-start now or manually later
- **Shell integration**: Add to `.zshrc` now or skip
- **Existing conflicts**: Replace old version? Keep both?

For each decision, present:

- The options
- Your recommendation with rationale
- The default (if user wants to skip the choice)

### 4. Generate the Installation/Update Plan

Write a markdown plan with this structure:

```markdown
# Install Plan: <tool-name>

## Briefing

### What It Is

1-2 sentences on what this tool does and its main purpose.

### How You'll Use It

- **Agent tool** — I (the AI) will call this on your behalf as part of
  automated workflows. You don't interact with it directly.
- **User-exposed tool** — You will run this manually in your terminal or via
  a GUI. It's a tool for your direct use.
- **Both** — You can use it directly, and I may also invoke it in automated
  flows.

Typical daily usage: "Youll run `foo` in terminal to..." or "I'll call `foo`
when you ask me to..."

### ASCII Overview
```

[Your Request] [Terminal] [System]
| | |
v v v
+--------+ +-------------+ +----------------+
| "do X" |--------->| tool-name X |----->| changes files |
+--------+ +-------------+ | starts service |
| writes config |
+----------------+

```

## Current State
Already installed? Version? Install method found?

## Environment Impact
- Binaries: ...
- Config: ...
- Shell: ...
- Services: ...
- Disk: ~X MB
- Risks: ...

## Decisions
1. **Install via** [Homebrew / npm / pip / manual]
   - Recommended: Homebrew
   - Rationale: ...
2. **Shell integration** [Yes / No]
   - Recommended: Yes
   - Adds to `.zshrc`: ...
3. ...

## Steps
1. Step one
2. Step two
3. Verify with: `<command>`
4. Rollback if needed: `<command>`

## Rollback Plan
How to fully undo this installation or revert to the previous version.
```

If `plannotator` is available, gate the plan:

```bash
plannotator annotate /tmp/tool-install-<tool-name>.md --gate
```

Otherwise, present the plan inline and ask for explicit approval.

### 5. Approval Gate

**Do not proceed with installation until the user explicitly approves.**

Approval format to request:

> Reply with **"approve"** or **"go"** to execute, or tell me which decisions
> to change.

If the user wants changes, revise the plan and re-present.

### 6. Execute (Only After Approval)

Run the approved steps one at a time. Verify each step before proceeding:

```bash
# After install or update, verify
<tool-name> --version
which <tool-name>
```

If a step fails, stop immediately. Do not continue. Report the error and ask
whether to rollback, retry, or abort.

## Rules

1. **Never install without explicit user approval.**
2. **Always research first.** Never guess what a tool does or how to install it.
3. **Always inspect the current environment.** Don't assume a clean slate.
4. **Always provide a rollback plan.** The user should know how to undo.
5. **Prefer Homebrew for macOS CLI tools** unless there's a good reason not to.
6. **Prefer user-local installs** when global isn't required.
7. **Flag destructive changes.** Overwriting configs, replacing system tools, or
   enabling background services deserve extra warning.
8. **If the link is a GitHub repo**, check `README.md`, `install.sh`, releases
   page, and `Makefile` for official install instructions.
9. **If the link is a website**, look for "Download", "Install", or "Get Started"
   sections.
10. **If the install involves `curl | bash`**, flag it as high-risk and recommend
    an alternative if one exists.

## Examples

### Install

**User:** install this https://github.com/jesseduffield/lazygit

**Agent actions:**

1. Fetch the GitHub repo README → understand it's a terminal UI for git
2. Check releases → available via Homebrew (`brew install lazygit`)
3. Check environment → `which lazygit` returns nothing, not installed
4. Check shell config → no lazygit aliases or env vars present
5. Decision points: install via Homebrew or what. pros and cons, add alias to `.zshrc`
   (optional, default no)
6. Present plan, wait for approval
7. On approval: `brew install lazygit`, verify with `lazygit --version`

### Update

**User:** update lazygit

**Agent actions:**

1. Check current version → `lazygit --version` → v0.40.0
2. Research latest → v0.44.0, changelog shows new features, no breaking changes
3. Check install method → `brew list lazygit` → installed via Homebrew
4. Decision points: update now (recommended), or pin at current version
5. Present plan showing version delta and any migration steps
6. On approval: `brew upgrade lazygit`, verify with `lazygit --version`
