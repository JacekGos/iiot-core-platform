---
name: issue-creation
description: Use when creating a new GitHub issue for the iiot-connector, iiot-core-platform, or iiot-core-web repos — a fresh story from scratch, or turning a user-described bug/idea into an issue (planning.md is no longer the source for new work).
---

# IIoT GitHub issue workflow

This project (Industrial IoT platform) tracks ALL work centrally in **`JacekGos/iiot-core-platform`** issues/milestones, even though the code lives across three repos: `iiot-connector`, `iiot-core-platform`, `iiot-core-web`. Kept deliberately lightweight — no story points, no priority labels, no sprint ceremony.

**Labels** (don't invent new ones without the user asking):
- Type: `bug` or `enhancement` — every issue gets exactly one
- Repo: `repo:connector`, `repo:core-platform`, `repo:core-web` — every issue gets at least one; a cross-cutting story gets more than one

**Milestones** = epics, named `EPIC-<n>`, all created in `iiot-core-platform` regardless of which repo(s) the epic's stories touch.

**Numbering convention**: stories are titled `<epic>.<n> — <short title>` (e.g. `4.6 — OPC-UA connector implementation`) — no "STORY-" word, just the numbers, so it doubles cleanly as a branch prefix. `<epic>` matches the milestone number; `<n>` increments within that epic, never reused even for closed/deleted issues.

**Branch naming**: derive from the issue's number pair by replacing the dot with a dash: issue `4.6` → branch `4-6-<short-slug>`, e.g. `4-6-opc-ua-connector`. Use this when creating a branch to work on a story.

Planning.md (in the "Industrial IOT platform" Claude project) held the original backlog and was migrated to issues once — it is no longer the source for new stories or epics. Don't add new entries there; GitHub is now the single source of truth for tracking.

## Creating a new story from scratch (not from planning.md)

When the user wants to add a new story/task — not implementing an existing one:

1. Ask (if not already clear from context) which epic it belongs to. If it's genuinely new work with no fitting epic yet, say so and suggest creating a new epic first (see the `epic-creation` skill) rather than forcing it under an unrelated one.
2. Ask which repo(s) it belongs to — `connector`, `core-platform`, `core-web`, or more than one if cross-cutting. If it's ambiguous from the description, make your best guess and say so, don't block on it.
3. Ask (or infer from the description) whether it's a `bug` or `enhancement`.
4. Compute the next story number for that epic:
   ```
   gh issue list --repo JacekGos/iiot-core-platform --milestone "EPIC-<n>" --state all --json title --jq '.[].title' | grep -oP "^<n>\.\K[0-9]+" | sort -n | tail -1
   ```
   Next number is that value + 1 (or 1 if the epic has no stories yet).
5. Write a clear title (`<epic>.<n> — <short title>`) and a body with acceptance criteria as a markdown checklist — draft these from what the user described, don't just transcribe their raw words if they were brief; ask a clarifying question if the scope is genuinely unclear.
6. Create it: `gh issue create --repo JacekGos/iiot-core-platform --milestone "EPIC-<n>" --label "<bug|enhancement>,<repo labels>" --title "..." --body "..."`.
7. Show the created issue's URL and number back to the user, along with the branch name it maps to (`<epic>-<n>-<slug>`).

## When the user reports a bug or idea in plain language

Don't just describe it back conversationally — turn it into a real issue unless the user is clearly just thinking out loud. Follow the "Creating a new story from scratch" flow above, defaulting the type to `bug` when something is described as broken.

## General principles for this project

- Keep it lightweight — no story points, no sprint ceremony, no priority labels. The milestone + issue list is enough signal for what's next.
- One issue = one unit of work = ideally one PR. Don't batch unrelated changes into a single issue's scope.
- `bug` and `enhancement` are the only type labels; `repo:connector`/`repo:core-platform`/`repo:core-web` are the only repo labels — don't propose new ones without the user asking.
- Never reuse a story or epic number, even if the original issue/milestone was closed or deleted.
- Titles are plain `<epic>.<n> — <title>`, no "STORY-" word — it needs to double as a branch-name prefix.
