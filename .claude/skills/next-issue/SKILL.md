---
name: next-issue
description: Use when the user asks what to work on next, wants the next issue picked for them, or says something like "get me the next story" — for the IIoT platform project tracked in JacekGos/iiot-core-platform.
---

# Picking the next issue to work on

This finds "what's next" in the centrally-tracked backlog (`JacekGos/iiot-core-platform` — see the `issue-creation` skill for the full tracking conventions: milestones = epics, `<epic>.<n>` titles, `repo:*` labels). It's the step before `issue-creation`'s "starting work on an existing issue" flow, not a replacement for it.

## Steps

1. **Find the earliest epic that still has open work.** Milestones are in creation order, but epics should be worked roughly in numeric order, so parse the epic number out of each milestone title rather than trusting GitHub's internal milestone `number` field:
   ```
   gh api repos/JacekGos/iiot-core-platform/milestones --jq '.[] | select(.open_issues > 0) | "\(.title)\t\(.open_issues)"' \
     | sed -E 's/^EPIC-([0-9]+)/\1/' | sort -n | head -1
   ```
   That gives the lowest-numbered epic with at least one open issue. If none have open issues, say so — the tracked backlog is fully closed, worth telling the user rather than guessing.

2. **List that epic's open issues, ordered by their `<epic>.<n>` number** (not GitHub's issue number, which reflects creation order and can drift from intended sequence if issues were added out of order later):
   ```
   gh issue list --repo JacekGos/iiot-core-platform --milestone "EPIC-<n>" --state open --json number,title \
     --jq 'sort_by(.title | capture("\\.(?<n>[0-9]+)").n | tonumber)'
   ```
   Take the first (lowest `<n>`) entry as the candidate.

3. **Check it isn't already in flight** before presenting it as "next" — don't suggest something someone's already halfway through:
   - Check for an existing branch matching the convention (`<epic>-<n>-*`) in whichever repo(s) the issue's `repo:*` labels point to.
   - Check for an open PR referencing it: `gh pr list --repo <that-repo> --state open --search "in:body #<n>"` (or the full `owner/iiot-core-platform#<n>` form, since the PR repo differs from the tracking repo).
   - If either exists, skip this candidate and move to the next-lowest `<n>` in the same epic (or the next epic, if that one's exhausted) rather than silently ignoring the conflict.

4. **Pull the full issue** for the one that's actually clear: `gh issue view <n> --repo JacekGos/iiot-core-platform`. Present its title, repo label(s), and acceptance criteria to the user, plus the branch name it maps to (`<epic>-<n>-<slug>`, per the `issue-creation` convention).

5. Ask whether the user wants to start on it now (hand off to `issue-creation`'s implementation flow) or just wanted a look at what's next.

## Notes

- If `gh` isn't available in the current environment, fall back to the same logic against the plain GitHub REST API via `curl` (`api.github.com/repos/JacekGos/iiot-core-platform/milestones`, `.../issues?milestone=<n>&state=open`) — the milestone-then-issue-ordering logic is identical either way.
- Don't jump straight to whichever epic looks most "interesting" — respect epic order unless the user explicitly asks to work out of sequence.
- This skill only decides *which* issue is next. Once one is chosen, defer to `issue-creation` for actually implementing it, checking acceptance criteria, and closing it out.
