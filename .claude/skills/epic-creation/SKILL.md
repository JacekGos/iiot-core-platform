---
name: epic-creation
description: Use when the user wants to create a new epic (milestone) for the IIoT platform project in JacekGos/iiot-core-platform — distinct from creating an ordinary story/issue under an existing epic.
---

# IIoT epic creation

Epics are GitHub Milestones, all created in **`JacekGos/iiot-core-platform`** (the single tracking repo for this project), named `EPIC-<n>` regardless of which of the three repos (`iiot-connector`, `iiot-core-platform`, `iiot-core-web`) the epic's stories will eventually touch.

This is a separate, less frequent action from creating a story under an existing epic (see the `issue-creation` skill for that) — use this one only when the work genuinely doesn't fit any existing epic.

## Steps

1. Confirm this really needs a new epic — check existing milestones first (`gh api repos/JacekGos/iiot-core-platform/milestones --jq '.[].title'`) and ask the user to confirm none of them fit, if it's not obvious.
2. Ask for:
   - A short epic name/theme (e.g. "Predictive Maintenance ML Service") — becomes part of the milestone description, not its title (the title is always just `EPIC-<n>`)
   - A one-to-two sentence description of what the epic covers
   - Optionally, which repo(s) it's expected to mostly involve — useful context for the description, not a stored field (repo labels are still applied per-story, not per-epic)
3. Compute the next epic number:
   ```
   gh api repos/JacekGos/iiot-core-platform/milestones --paginate -X GET -f state=all --jq '.[].title' | grep -oP 'EPIC-\K[0-9]+' | sort -n | tail -1
   ```
   Next number is that value + 1. Never reuse a number, even if a prior milestone was closed or deleted.
4. Create the milestone:
   ```
   gh api repos/JacekGos/iiot-core-platform/milestones -f title="EPIC-<n>" -f description="<short name> — <description>"
   ```
5. Report back the milestone number, title, and URL. Ask whether the user wants to immediately break it into stories (hand off to `issue-creation`'s story-creation flow) or leave it empty for now as a placeholder.

## Principles

- Don't create stories as part of this flow unless the user explicitly asks to go straight into breaking the epic down — epic creation and story creation are separate steps.
- Don't rename or renumber existing epics to "make room" — numbers are permanent once assigned.
- Keep the milestone description short; detailed rationale belongs in the "Industrial IOT platform" Claude project's docs if it's substantial enough to need it, not crammed into the milestone description.
