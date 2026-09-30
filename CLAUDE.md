# iiot-core-platform

## Approval

Never make ANY change without first stating exactly what you're about to do and waiting for explicit confirmation. This is not limited to GitHub actions — it covers everything: editing or creating a file, running a build/test/lint command, installing a dependency, running any shell command that changes state, git operations (commit, push, branch, checkout), and GitHub actions (issues, milestones, labels, commits, branches, pushes, PRs). This applies even when a skill's steps say to "create", "implement", "commit", or "run" something — describe the action, then wait for a yes before executing it. Read-only actions (viewing a file, listing issues, searching, running tests to check status) don't need approval — only anything that changes state does.

Kotlin + Spring Boot 3 modular monolith. Contains all application logic — pipeline execution, config management, time-series storage, 
alerting, dashboard delivery, REST API. Structured into isolated modules communicating only through interfaces and Spring Application Events.

## Build & test

- Build: `./gradlew build`
- Full check (tests + ktlint + Detekt + ArchUnit boundary tests): `./gradlew check`
- Module-level tests: `./gradlew :pipeline-engine:test`, `:config:test`, etc.
- Needs Docker Compose stack running for integration tests (PostgreSQL, TimescaleDB, Redpanda)

## Module boundaries — enforced by ArchUnit, do not violate

- Modules: `pipeline-engine`, `config`, `storage`, `notification`, `dashboard`, `shared-kernel`
- Cross-module communication ONLY through: (a) interfaces defined in a module's `api` package, or (b) Spring Application Events — never direct imports of another module's internal classes
- `shared-kernel` has zero dependencies on any other module and zero Spring dependencies — pure Kotlin only (it holds `DataEvent`, `PipelineBlock`, Kafka event models)
- `./gradlew check` runs ArchUnit tests that fail the build if a boundary is violated — if that happens, fix the dependency, don't suppress the test

## DDD is applied selectively (see ADR-013 if unsure why)

- Full DDD (aggregates, value objects, domain events): `pipeline-engine` only
- Light DDD (value objects, simple validation): `config`, `notification`
- No DDD, plain service + repository: `storage`, `dashboard`

## Kafka & auth essentials

- Consumes `raw-events` from iiot-connector; produces `config-changes` to it
- Auth: Spring Security + JWT, no external identity provider. Roles: `ADMIN`, `OPERATOR`, `VIEWER` via `@PreAuthorize`

Full architecture rationale and ADRs live in the "Industrial IOT platform" Claude project, not here — check there before re-deriving a decision that's already been made (e.g. why TimescaleDB over InfluxDB, why no service discovery).

## Code style

- ktlint + Detekt enforced via `./gradlew check`, CI fails on violations

## Working with GitHub issues

This repo IS the central tracker for the whole project — issues/milestones for `iiot-connector` and `iiot-core-web` also live here, not in their own repos.

- Labels: type is `bug` or `enhancement`; repo is `repo:connector`, `repo:core-platform`, or `repo:core-web` (a cross-cutting issue can carry more than one repo label). That's the full label set — don't invent new ones.
- Milestones = epics, named `EPIC-<n>`.
- Issue titles are `<epic>.<n> — <short title>` (e.g. `5.3 — REST API skeleton and JWT security`) — no `STORY-` prefix, so it doubles as a branch-name prefix: `<epic>-<n>-<slug>` (dot becomes dash), e.g. branch `5-3-rest-api-jwt`.
- Before implementing a story, pull its acceptance criteria with `gh issue view <n>` — treat the AC as the definition of done, verify against it before opening a PR.
- Since a PR here and the issue it closes are both in this repo, bare `Closes #<n>` in the PR description is enough. When a PR in `iiot-connector` or `iiot-core-web` closes an issue tracked here, it needs the full form instead: `Closes JacekGos/iiot-core-platform#<n>`.
- A bug noticed during work should become its own issue (`gh issue create --label bug,repo:core-platform`), not silently expand the scope of the current one.
- planning.md is retired as a source for new work — new stories/epics are created directly on GitHub.
