# Contributing to Open Towns

Thank you for contributing to Open Towns.

The goal of this project is to stabilize, modernize, document, and carefully improve the original Towns source code while preserving the spirit and behavior of the game.

This project is not a rewrite. Contributions should aim to make the existing codebase easier to understand, safer to maintain, and more reliable.

## Project Goals

Open Towns should prioritize:

- Stability
- Documentation
- Clearer code structure
- Reduced technical debt
- Small quality of life improvements
- Better contributor onboarding
- Preserving existing gameplay unless a change is intentional and discussed

Large new systems, major gameplay changes, or visual redesigns should usually be discussed before work begins. For broad gameplay experiments, consider creating a mod first.

## General Contribution Expectations

All contributions should improve the project in at least one of these ways:

- Make the code easier to understand
- Make the code easier to maintain
- Fix a bug
- Improve stability
- Improve documentation
- Add tests
- Improve developer experience
- Add a small quality of life feature

Please avoid changes that only move code around without making it clearer.

## Code Style Expectations

When editing code, aim for clarity over cleverness.

### Naming

Use clear, descriptive names for:

- Classes
- Methods
- Variables
- Constants
- Helper objects
- Temporary values

Avoid vague names such as:

```java
sAux
iAux
temp
data
thing
stuff
doStuff
handleThings
```

Prefer names that explain intent:

```java
tooltipText
selectedTask
accessPoint
stockpileArea
isPlacementValid
shouldCloseMenu
```

Short names are acceptable for very small local scopes, such as loop counters:

```java
for (int i = 0; i < items.size(); i++) {
    ...
}
```

Where the meaning matters, use a meaningful name.

## Clarity Over Compactness

Do not compress logic into dense one-liners if it makes the code harder to understand.

Prefer this:

```java
boolean isValidBuildTask = taskType == TASK_BUILD && targetItem != null;

if (isValidBuildTask) {
    createBuildTask(targetItem);
}
```

Over this:

```java
if (taskType == TASK_BUILD && targetItem != null) createBuildTask(targetItem);
```

The goal is code that future contributors can understand quickly.

## Comments

Comments should explain why something is being done, not simply repeat what the code says.

Good comment:

```java
// The cursor image is flipped vertically because LWJGL expects cursor pixel data
// in a different orientation to standard image loading.
```

Poor comment:

```java
// Set operative to true
operative = true;
```

### Small Tweaks

For small changes, comments are expected where the reason for the change is not immediately obvious.

Examples:

- Fixing an edge case
- Preserving old behavior
- Working around engine behavior
- Avoiding a known crash
- Handling legacy data
- Adding compatibility logic

Small changes do not need full markdown documentation, but the code should still be understandable.

## Refactors

A refactor is any change that restructures code without intentionally changing gameplay behavior.

Examples:

- Extracting helper classes
- Splitting a large class into smaller classes
- Renaming confusing methods
- Replacing repeated logic with shared methods
- Moving calculations into utility classes
- Simplifying conditionals
- Reducing duplicated UI logic

### Refactor Requirements

Refactors should:

- Preserve existing behavior unless stated otherwise
- Be split into sensible commits where possible
- Avoid mixing unrelated changes
- Use clearer names
- Reduce complexity
- Include comments where behavior is non-obvious
- Include documentation explaining the new structure
- Mention any known risks or behavior that may need testing

If a refactor changes behavior, that must be clearly stated in the pull request.

## Documentation Requirements

Documentation is expected when a contribution introduces or significantly changes:

- A system
- A manager class
- A helper class
- A workflow
- A task type
- UI behavior
- Build logic
- Save/load behavior
- Game rules
- Developer setup
- Modding behavior
- Public APIs or reusable utilities

### Small Fixes

Small fixes usually only need:

- Clear code
- Good naming
- A short explanation in the pull request
- A comment if the reason is not obvious

Example:

```markdown
Fixed tooltip positioning for the level-up icon when UI scaling is enabled.
```

### Refactors or Additions

Refactors and additions should include full documentation.

This may be added as:

- A markdown file in the `docs` folder
- A section in an existing documentation file
- Class-level comments
- Method comments for important public or helper methods
- A pull request summary explaining the structure

## Documentation Should Explain

Documentation should cover:

- What the system does
- Where the main files are
- How the main classes relate to each other
- What behavior should be preserved
- What assumptions the code makes
- Any edge cases
- How to test the change manually
- Any known limitations

Example structure:

```markdown
# Task System Overview

## Purpose

Explains what the task system is responsible for.

## Main Files

- Task.java
- TaskManager.java
- TaskArea.java
- TaskAccessPoints.java
- TaskImmediateExecuter.java

## Behavior

Explains how normal queued tasks work.

## Testing Checklist

- Build walls normally
- Place furniture normally
- Place items in god mode
- Cancel tasks
- Save and reload
```

## Pull Request Expectations

Use the reusable pull request template in [docs/pull_request_template.md](pull_request_template.md).

Keep pull requests focused on one purpose. If the work has a related issue, link it. If behavior changes, call that out clearly.

## Behavior Changes

Behavior changes should be clearly separated from refactors.

A behavior change is anything that affects how the game plays, even slightly.

Examples:

- Changing task priority
- Changing where items can be placed
- Changing build validation
- Changing citizen behavior
- Changing UI interaction
- Changing save/load data
- Changing economy, combat, food, or production logic

If your contribution changes behavior, explain:

- What changed
- Why it changed
- Whether the old behavior was a bug
- How the new behavior was tested

Avoid hiding behavior changes inside a refactor.

## Tests

Where possible, add tests for new or changed logic.

Tests are especially useful for:

- Utility methods
- Date/time logic
- Coordinate calculations
- Save/load parsing
- Data conversion
- Task validation
- UI scaling calculations
- Path/access point logic

If a change cannot easily be tested with automated tests, include a manual testing checklist in the pull request.

## Manual Testing Checklist

For gameplay or UI changes, include a checklist like this:

```markdown
## Manual Testing

- [ ] Game launches successfully
- [ ] Existing save loads successfully
- [ ] New game starts successfully
- [ ] Feature works at normal UI scale
- [ ] Feature works at larger UI scale
- [ ] Behavior still works after save/load
- [ ] No obvious errors in logs
```

For task system changes, also consider checking:

- [ ] Tasks can be created normally
- [ ] Tasks can be cancelled normally
- [ ] Citizens can reach the affected task area
- [ ] Save/load preserves the affected task state
- [ ] Existing task behavior is unchanged unless the PR says otherwise

## Commit Guidelines

Commits should be focused and understandable.

Good examples:

```text
Refactor tooltip positioning into TooltipRenderer
Add documentation for task system helpers
Fix stockpile access point validation
Rename unclear task area variables
Add tests for DateUtils
```

Poor examples:

```text
stuff
fixes
changes
update
big refactor
```

## File and Class Documentation

When adding a new class, include a short class-level comment explaining its purpose.

Example:

```java
/**
 * Handles stockpile-related task behavior for Task.
 *
 * This helper exists to keep stockpile creation and validation logic separate
 * from the main Task class. It should not change task behavior directly unless
 * that behavior change is documented in the pull request.
 */
final class TaskStockpileHelper {
    ...
}
```

Important methods should also include comments where useful:

```java
/**
 * Creates a stockpile for the selected task area.
 *
 * This preserves the original behavior of checking each tile in the selected
 * area and only creating stockpile cells where placement is valid.
 */
private void createStockpile(TaskArea area) {
    ...
}
```

Do not over-comment obvious methods.

## Preserving Original Behavior

Because this project is based on an existing game, preserving behavior matters.

When refactoring, check:

- Does the same action still work in-game?
- Does the same UI flow still happen?
- Are save files still compatible?
- Are existing constants still respected?
- Are tutorial triggers still working?
- Are old edge cases still handled?

If you are unsure whether something is intentional, mention it in the pull request instead of removing it.

## Legacy Code

Some code may look strange because it supports old behavior, old data, or engine-specific quirks.

Do not remove legacy-looking code unless you understand what it does.

If you find confusing code, prefer one of these approaches:

- Rename variables for clarity
- Extract a helper method
- Add a comment explaining the behavior
- Open an issue asking whether it is still needed
- Add documentation for future contributors

## Avoid Large Mixed Pull Requests

Large pull requests are harder to review and more likely to introduce bugs.

Try to avoid combining:

- Refactor plus new feature
- Formatting plus behavior change
- Documentation plus unrelated code changes
- UI change plus task system change
- Build system change plus gameplay change

Better:

```text
PR 1: Add documentation for task system
PR 2: Refactor TaskArea handling
PR 3: Add god mode placement improvement
```

Worse:

```text
PR 1: Refactor tasks, add god mode, change UI, update docs, reformat files
```

## Good First Contributions

Good first contributions include:

- Improving unclear names
- Adding comments to confusing code
- Writing documentation for one file or folder
- Adding manual testing notes
- Fixing typos
- Adding small tests for utility methods
- Extracting very small helper methods
- Creating issue checklists for files that need review

Avoid starting with large refactors unless you have discussed the plan first.

## Contribution Standard by Change Type

| Change Type | Expected Standard |
| --- | --- |
| Typo fix | Simple PR description |
| Small bug fix | Clear explanation and testing notes |
| Small clarity tweak | Improved naming and optional comment |
| UI tweak | Screenshot or clear manual test notes where possible |
| Refactor | Documentation, testing checklist, and no hidden behavior changes |
| New helper class | Class-level documentation and explanation in PR |
| New system | Full markdown documentation and testing notes |
| Gameplay change | Clear behavior explanation and discussion where needed |
| Save/load change | Careful testing and compatibility notes |
| Build system change | Setup documentation updated |

## Reviewer Expectations

Reviewers should check:

- Is the purpose of the change clear?
- Is the code easier to understand?
- Are names meaningful?
- Are comments useful?
- Is documentation included where needed?
- Has the change been tested?
- Are behavior changes clearly stated?
- Is the pull request focused?
- Does this support the current project goals?

## Final Principle

A good contribution should make the next contributor's job easier.

Before submitting, ask:

- Is this clearer than before?
- Would someone new to the project understand this faster?
- Have I explained anything non-obvious?
- Have I avoided changing behavior accidentally?
- Have I documented larger changes properly?

If the answer is yes, it is likely a good contribution.
