
# Tasks System

The `xaos.tasks` package handles player-created tasks, citizen work orders, task placement, task cancellation, and immediate God Mode execution.

The original task logic lived almost entirely inside `Task.java`. It has now been split into smaller helper classes so each file has a clearer responsibility.

`Task.java` remains the central task data object, while the helper classes handle specific task behaviours.

---

## Main responsibilities

The task system is responsible for:

- Storing task data
- Tracking task creation state
- Converting player selections into hot points
- Creating build, mine, stockpile, zone, and item placement orders
- Cancelling existing orders
- Creating custom actions
- Handling God Mode immediate execution
- Saving and loading task data

---

## Key concepts

### Task

A `Task` represents a single order in the world.

Examples:

- Mine this block
- Build this building
- Create and place this item
- Create a stockpile
- Create or expand a zone
- Cancel existing orders
- Perform a custom action

The `Task` object stores the task type, selected points, parameters, hot points, maximum citizens, completion state, and save/load data.

---

### Task type

Task types are integer constants defined in `TaskTypes.java`.

Examples:

```java
TASK_MINE
TASK_BUILD
TASK_CREATE_AND_PLACE
TASK_STOCKPILE
TASK_CREATE_ZONE
TASK_CUSTOM_ACTION
````

`Task.java` still exposes compatibility constants like:

```java
Task.TASK_MINE
```

These map back to `TaskTypes`, so older code does not need to be updated immediately.

---

### Task state

Task state controls how the player is currently creating the task.

Examples:

```java
STATE_CREATING_INIZONE
STATE_CREATING_ENDZONE
STATE_CREATING_SINGLEPOINT
STATE_CREATED
```

Area-based tasks usually start with `STATE_CREATING_INIZONE`, then move to `STATE_CREATING_ENDZONE`.

Single-point tasks use `STATE_CREATING_SINGLEPOINT`.

---

### Points

Tasks can use two selected points:

```java
pointIni
pointEnd
```

`pointIni` is the first selected point.

`pointEnd` is the second selected point for area-based tasks.

Single-point tasks usually only use `pointIni`.

---

### Hot points

A hot point is the actual work target used by citizens.

For example, a mining task may target a block, but the citizen may need to stand beside it, above it, or below it. The hot point stores both the target and the possible access points.

Hot points are stored on the task:

```java
ArrayList<HotPoint> hotPoints
```

If a task has no hot points after creation, it is usually considered finished.

---

### Parameters

Some tasks need extra information.

`parameter` and `parameter2` are used for things like:

* Item headers
* Building headers
* Zone IDs
* Custom action IDs
* Queue IDs

For normal create-and-place tasks, `parameter` usually stores the item header.

For queue-and-place tasks, `parameter` stores the queue/action ID and `parameter2` stores the item that will be placed.

---

## File overview

---

## `Task.java`

`Task.java` is the central data object for tasks.

It owns the fields that need to be saved and loaded:

```java
id
task
state
pointIni
pointEnd
hotPoints
maxCitizens
parameter
parameter2
face
finished
```

It also owns the public task API used elsewhere in the game.

Responsibilities:

* Store task data
* Save and load task data through `Externalizable`
* Track creation state
* Store selected points
* Store parameters
* Store hot points
* Store task completion state
* Delegate task behaviour to helper classes
* Keep compatibility constants for older code

`Task.java` should stay focused on data and orchestration.

Large task-specific behaviour should be moved into helper classes.

Do not move save/load fields out of `Task.java` unless save-game compatibility is being intentionally changed.

---

## `TaskTypes.java`

`TaskTypes.java` contains task type and task state constants.

Examples:

```java
TASK_MINE
TASK_BUILD
TASK_CREATE_AND_PLACE
TASK_STOCKPILE
TASK_CREATE_ZONE
TASK_CUSTOM_ACTION
STATE_CREATED
```

This class should contain constants only.

It should not contain task logic.

Recommended structure:

```java
public final class TaskTypes {

    private TaskTypes() {
    }

    public static final int TASK_MINE = 4;
}
```

---

## `TaskArea.java`

`TaskArea` is a simple internal data holder for a selected task area.

It stores normalized coordinates:

```java
xStart
yStart
xEnd
yEnd
z
xSwapped
ySwapped
```

The task area is normalized so helper classes can safely loop from start to end even if the player dragged in the opposite direction.

For example, if the player drags from bottom-right to top-left, `TaskArea` swaps the coordinates internally.

This class is package-private and only used by classes in `xaos.tasks`.

---

## `TaskAccessPoints.java`

`TaskAccessPoints` calculates possible access points around a target cell.

This is used when a citizen needs to reach a target from nearby cells.

Examples:

* Mining
* Digging
* Ladders
* Bridges
* Items that can be built on holes
* Tasks that need a reachable neighbouring cell

It checks:

* Adjacent cells on the same level
* The cell below
* Adjacent cells above if the above cell is mined
* Adjacent cells below if the below cell is mined
* The cell directly above as a final option

This class is stateless.

It should not create tasks, hot points, or world entities.

---

## `TaskHotPointBuilder.java`

`TaskHotPointBuilder` converts a completed player selection into actual task hot points.

It is the main routing class for normal task creation.

It decides which helper should handle the task:

```java
TASK_MINE -> mining hot points
TASK_CANCEL_ORDER -> cancel helper
TASK_BUILD -> build helper
TASK_STOCKPILE -> stockpile helper
TASK_CREATE_ZONE -> zone helper
TASK_EXPAND_ZONE -> zone helper
TASK_CREATE_AND_PLACE -> create/place helper
TASK_QUEUE_AND_PLACE -> create/place helper
```

It should stay as a coordinator.

Large task-specific branches should be placed in dedicated helper files instead of growing this class too much.

---

## `TaskImmediateExecutor.java`

`TaskImmediateExecutor` handles immediate execution, mainly for God Mode.

Normal tasks are usually queued and completed by citizens.

God Mode bypasses that and applies supported task effects directly to the world.

Examples:

* Instantly mine a cell
* Instantly complete a building
* Instantly place an item
* Immediately execute task manager actions for supported task types

Important behaviour:

Create-and-place tasks are executed before `prepareForQueue()` in God Mode.

This prevents the normal queue logic from creating citizen tasks before the item is instantly placed.

This fixed issues where citizens would later try to move an item that God Mode had already placed.

---

## `TaskCancelHelper.java`

`TaskCancelHelper` handles cancel-order behaviour.

It cancels work that overlaps the selected cancel area.

It can cancel:

* Existing mine/dig hot points
* Temporary task manager items
* Queued custom actions
* Waiting custom actions
* Active citizen custom actions

It also clears the order flag from affected cells.

This class should only remove or finish existing work.

It should not create new tasks.

---

## `TaskCustomActionBuilder.java`

`TaskCustomActionBuilder` creates custom actions from selected cells.

Custom actions can target:

* Items
* Living entities
* Terrain

The task parameter may contain one action ID or multiple comma-separated action IDs.

Example:

```java
"cut_tree"
"cut_tree,gather_fruit"
```

This helper checks the selected cell and creates the matching custom action if the selected target supports it.

It does not create normal `Task` instances.

Instead, it creates `Action` objects and adds them to the task manager.

---

## `TaskBuildHelper.java`

`TaskBuildHelper` handles normal building task creation.

It is responsible for:

* Loading the building definition
* Creating the building entity
* Checking whether the building can be placed
* Finding the building entrance
* Creating the construction hot point
* Setting max citizens
* Setting prerequisites
* Setting automatic building behaviour
* Adding the building to the world
* Marking occupied building cells
* Removing stockpile/zone data from building cells

This helper creates the build task setup.

It does not instantly complete the building. Immediate completion is handled by `TaskImmediateExecutor`.

---

## `TaskStockpileHelper.java`

`TaskStockpileHelper` handles stockpile creation.

It scans the selected area and creates a stockpile from every valid cell.

It is responsible for:

* Checking which cells can become stockpile cells
* Adding valid cells to the stockpile
* Applying disabled-items mode if enabled
* Adding the stockpile to the world
* Updating tutorial flow

This helper does not create haul tasks directly.

The task manager handles stockpile hauling after the stockpile exists.

---

## `TaskZoneHelper.java`

`TaskZoneHelper` handles zone creation and zone expansion.

It supports normal zones and special zone types.

Supported special zones include:

* Personal zones
* Hero rooms
* Barracks

It is responsible for:

* Loading the zone definition
* Creating the correct `Zone` subclass
* Checking whether cells are available
* Adding points to the zone
* Assigning owners or groups when needed
* Adding the zone to the world
* Updating tutorial flow

For personal zones, it assigns the zone to the first citizen or soldier without a zone.

For hero rooms, it assigns the zone to a hero that requires a free room.

For barracks, it assigns the zone to the first soldier group without a zone.

---

## `TaskCreateAndPlaceHelper.java`

`TaskCreateAndPlaceHelper` handles the normal create-and-place workflow.

This is the non-God-Mode flow.

It is responsible for:

* Checking whether the destination can accept the item
* Reusing an existing unlocked item if possible
* Creating a persistent move-and-lock task for reused items
* Creating queue-and-place custom actions
* Finding the best production building
* Adding a new item to the nearest suitable building queue
* Showing missing-building messages when needed

This helper does not handle God Mode immediate placement.

God Mode immediate placement is handled by `TaskImmediateExecutor`.

---

## Normal task creation flow

The normal task creation flow is:

```text
Player selects task
Task stores task type
Player selects point or area
Task.finishCreation()
Task.prepareForQueue()
TaskHotPointBuilder builds hot points or delegates to helper
Game.taskCreated(task)
TaskManager assigns work to citizens
```

In code:

```java
finishCreation()
    prepareForQueue()
        setZoneHotPoints()
            TaskHotPointBuilder
                helper classes
    Game.taskCreated(this)
```

---

## God Mode task flow

God Mode can bypass normal citizen work.

For create-and-place tasks, God Mode executes before queue preparation:

```text
Task.finishCreation()
God Mode create/place check
TaskImmediateExecutor places item directly
Current task is deleted
Normal queue creation is skipped
```

This is important because create-and-place tasks can otherwise create normal citizen work before the item is placed instantly.

For other immediate tasks, the flow is:

```text
Task.finishCreation()
prepareForQueue()
tryExecuteImmediatelyInGodMode()
delete current task if successful
```

Some task types still need hot points to exist before they can execute immediately, so they run after `prepareForQueue()`.

---

## Why `Task.java` still has compatibility constants

Task constants were moved into `TaskTypes.java`.

However, many existing files may still reference constants through `Task`, for example:

```java
Task.TASK_MINE
Task.TASK_BUILD
Task.TASK_CREATE_AND_PLACE
```

To avoid breaking the whole codebase at once, `Task.java` keeps compatibility aliases:

```java
public static final int TASK_MINE = TaskTypes.TASK_MINE;
```

New code can use `TaskTypes.TASK_MINE`, but old code can continue using `Task.TASK_MINE`.

Over time, usages can be migrated gradually.

---

## Save-game compatibility

`Task` implements `Externalizable`.

This means save/load order matters.

The following fields are saved and loaded in order:

```java
id
task
state
pointIni
pointEnd
hotPoints
maxCitizens
parameter
parameter2
finished
face
```

Do not reorder these fields in `readExternal()` or `writeExternal()` unless you also handle save version compatibility.

Do not move these saved fields into helper classes unless save-game compatibility is being intentionally changed.

---

## Package-private helper access

Some methods in `Task.java` are package-private so helper classes in `xaos.tasks` can use them without making them public.

Examples:

```java
void addHotPoint(HotPoint hotPoint)
Cell getMineTargetCell(Cell[][][] cells, short x, short y)
boolean canMineTargetCell(Cell cell)
```

These methods are intentionally not public API.

They exist so helper classes can work with `Task` while keeping access limited to the package.

---

## Adding a new task type

To add a new task type:

1. Add the constant to `TaskTypes.java`.
2. Add a compatibility alias to `Task.java` if older code may need `Task.TASK_*`.
3. Decide its creation state in `Task.requiresAreaSelection()` or `Task.requiresSinglePointSelection()`.
4. Add display text mapping in `TASK_MESSAGE_KEYS`.
5. Add happiness/work behaviour if needed.
6. Add routing in `TaskHotPointBuilder`.
7. Add immediate execution support in `TaskImmediateExecutor` if needed.
8. Add save/load data only if the task needs new persistent fields.

---

## When to create a new helper

Create a new helper when a block of task logic:

* Has one clear responsibility
* Is more than a small utility method
* Touches a specific feature area
* Makes `Task.java` harder to read
* Can be tested or reasoned about separately

Examples:

```text
TaskBuildHelper
TaskZoneHelper
TaskStockpileHelper
TaskCreateAndPlaceHelper
```

Avoid creating helpers for one-line methods unless it improves structure.

---

## Current design goal

The goal of this refactor is not to completely redesign the task system.

The goal is to make the existing behaviour easier to understand and safer to change.

The current design keeps:

* `Task.java` as the saved data object
* Helper classes as behaviour modules
* Existing task constants available through `Task`
* Existing save-game structure intact
* Existing gameplay behaviour mostly unchanged

This makes the task system easier to work on without introducing unnecessary risk.


