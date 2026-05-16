# Documentation System

The documentation system keeps technical docs in the repository and publishes contributor-facing navigation to the GitHub Wiki.

Repository docs are the source of truth. Wiki pages are generated from those docs so contributors can browse them more easily.

---

## Location

```text
.github/workflows/Document-index.yml
docs/
```

---

## Main responsibilities

The documentation workflow handles:

* scanning repository Markdown files
* generating the wiki documentation index
* publishing repository docs into generated wiki pages
* generating the wiki sidebar
* skipping wiki publishing safely from forks without `WIKI_TOKEN`

---

## Repository docs

Repository docs live under:

```text
docs/
```

Use the current categories:

```text
docs/getting-started/ = setup, build, run, and contributor onboarding docs
docs/modding/ = modding and Towns++ documentation
docs/systems/ = code systems, managers, architecture, and implementation details
```

The repository should not track a generated docs index file. The workflow creates that index during CI and publishes it to the wiki.

---

## Wiki publishing

The workflow publishes generated pages to:

```text
Open-Towns/Open-Towns.wiki.git
```

The generated index is published as:

```text
Repository-Docs-Index.md
```

The generated sidebar is published as:

```text
_Sidebar.md
```

Generated wiki docs include a marker comment at the top of the file. The workflow uses that marker to clean up old generated pages without deleting hand-written wiki pages.

---

## Wiki page names

Repository docs are mapped to stable wiki page names:

```text
README.md -> Repository-README
docs/getting-started/BUILDING.md -> Getting-Started-BUILDING
docs/modding/MODDING.md -> Modding-MODDING
docs/systems/TooltipRenderer.md -> Systems-TooltipRenderer
docs/CHANGELOG.md -> Docs-CHANGELOG
```

These names keep generated pages grouped in the wiki sidebar even though GitHub Wiki stores pages as flat Markdown files.

---

## Fork behavior

Fork workflows can generate the docs index, but they should not publish to the upstream wiki by default.

When the workflow is running outside:

```text
Open-Towns/Open-Towns
```

it skips wiki publishing unless the fork has a `WIKI_TOKEN` secret.

This keeps downstream test runs safe while still allowing the upstream workflow to publish documentation after merge.

---

## Token requirements

The workflow uses:

```text
secrets.WIKI_TOKEN || github.token
```

If `github.token` cannot push to the wiki repository, add a repository secret named:

```text
WIKI_TOKEN
```

That token needs permission to push to:

```text
Open-Towns/Open-Towns.wiki.git
```

---

## Editing rule

Edit repository docs, not generated wiki pages.

Use this pattern:

```text
README.md = short project overview
docs/ = technical source docs
Wiki = generated navigation and browsable copies
```

If a wiki page is generated, update the source file under `docs/` and let the workflow republish it.
