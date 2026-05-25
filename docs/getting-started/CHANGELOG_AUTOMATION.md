# Changelog Automation

Open-Towns uses two related changelog surfaces:

- [CHANGELOG.md](../CHANGELOG.md) keeps the long-form project history and high-level unreleased notes.
- GitHub release notes are generated automatically when a version tag is pushed.

The automated release notes are built from merged pull requests, labels, and linked issues.

## Release Notes Workflow

The release workflow is `.github/workflows/release.yml`.

When a tag such as `v0.0.3` or `0.1.0` is pushed, the workflow:

1. Builds the Windows x64 release archive.
2. Builds the Linux x64 release archive.
3. Downloads both build artifacts.
4. Generates release notes from merged pull requests.
5. Creates or updates the GitHub Release for the tag.

The release-note categories are configured in `.github/release.yml`.

## PR Labels Drive Categories

Use labels on pull requests so generated release notes land in the right section.

| Label | Generated section |
| --- | --- |
| `bug` | Bug Fixes |
| `enhancement` | Features |
| `documentation` | Documentation |
| `skip-changelog` | Excluded from generated notes |

Pull requests without one of these labels fall into `Other Changes`.

Labels such as `good first issue`, `help wanted`, `question`, `duplicate`, `invalid`, and `wontfix` are still useful for issue triage, but they are not release-note categories.

## Issues and PR Text

Link issues from PRs with GitHub keywords when the PR closes an issue:

```text
Closes #29
Fixes #36
Resolves #42
```

This lets GitHub connect the issue, PR, and generated release notes.

## Recommended Maintainer Flow

For each PR before merge:

- Add one changelog category label.
- Add `skip-changelog` only for changes that should not appear in release notes.
- Make sure the PR title is readable as a release note line.
- Link related issues with `Closes #`, `Fixes #`, or `Resolves #` when appropriate.
- Keep the PR focused so generated release notes stay clear.

## Updating CHANGELOG.md

`docs/CHANGELOG.md` should be updated manually for high-level project milestones, release summaries, or major compatibility notes.

Do not try to duplicate every merged PR there. The generated GitHub Release notes are the detailed per-PR changelog.

Use `CHANGELOG.md` for:

- A short `Unreleased` summary while work is accumulating on `Dev`.
- Notable behavior changes.
- Compatibility notes.
- Save/data/modding impact.
- Links to generated GitHub Releases when a version is published.
