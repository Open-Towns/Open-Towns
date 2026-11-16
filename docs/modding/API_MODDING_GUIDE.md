# Open Towns Modding Guide

This guide covers creating Java mods for **Open Towns SDK 0.1**, using **API version 1**.

It is intended as the starting point for mod developers. For the complete public Java surface, see:

`docs/modding/API_REFERENCE.md`

The SDK is designed so mods interact with Open Towns through the public `xaos.api.*` packages rather than depending directly on the game's internal classes.

---

## 1. What can Open Towns mods do?

SDK 0.1 supports Java mods that can interact with several parts of the game, including:

- Definitions
- World and cells
- Entities
- Items
- Buildings
- Tasks
- Area selection
- Events
- Inventory and containers
- Stockpiles
- Fluids
- Production
- Actions
- UI messages and game actions
- Mod save data
- Item behaviours
- Building behaviours

Mods can also include the existing XML/content files used by Towns.

A mod can therefore combine traditional Towns content definitions with Java behaviour.

The SDK deliberately exposes stable API objects instead of requiring mods to access Towns' internal implementation classes.

---

# 2. Recommended starting point

The easiest way to create a mod is to copy the **OpenTownsExampleMod** project.

The example mod contains the Gradle setup needed to:

- compile against the Open Towns API
- build the mod JAR
- generate `mod.properties`
- package mod files
- install the mod into the Open Towns mods directory

The three SDK example projects have different purposes:

| Example | Purpose |
| --- | --- |
| `OpenTownsExampleMod` | Minimal Java mod and lifecycle example |
| `OpenTownsLavaBuckets` | Definitions, production, actions, selection and fluids |
| `OpenTownsConveyorBelts` | Behaviours, containers, inventory transfer and item facing |

Start with `OpenTownsExampleMod` unless you specifically need one of the more advanced systems.

---

# 3. Requirements

The current Open Towns development environment uses Java 25.

You should have:

- JDK 25 installed
- Git, if cloning the examples
- a Java editor or IDE such as VS Code or IntelliJ
- Open Towns available locally
- the Open Towns API JAR available to the Gradle project

The example Gradle project is already configured to compile against the API JAR.

You should not add the complete Open Towns game JAR as a normal mod dependency unless you are deliberately developing against internal code.

Normal mods should use:

```text
xaos.api.*
```

and not:

```text
xaos.internal.*
xaos.main.*
xaos.tiles.*
```

Internal packages are not part of the supported mod API.

---

# 4. Creating a new mod

Copy the example mod project and rename it.

The main values for a mod are stored in `gradle.properties`.

For example:

```properties
modId=example-mod
modName=Example Mod
modVersion=1.0.0
modAuthor=Your Name
modMainClass=examplemod.ExampleMod
modDependencies=
```

## Mod ID

The mod ID is particularly important.

For example:

```properties
modId=example-mod
```

Valid IDs may contain:

```text
a-z
0-9
_
-
.
```

Use lowercase IDs.

Good:

```text
example-mod
lava-buckets
conveyor-belts
my_mod
```

Avoid spaces and uppercase letters.

The mod ID becomes the namespace used by your definitions.

---

# 5. Your main mod class

Every Java mod implements:

```java
xaos.api.mods.TownsMod
```

A minimal mod looks like this:

```java
package examplemod;

import xaos.api.mods.ModContext;
import xaos.api.mods.TownsMod;

public final class ExampleMod implements TownsMod {

    @Override
    public void onLoad(ModContext context) {
        context.logger().info(
                "Example Mod loaded!"
        );
    }

    @Override
    public void onWorldLoaded(ModContext context) {
        context.api()
                .ui()
                .showMessage(
                        "Hello from Example Mod!"
                );
    }

    @Override
    public void onWorldUnloaded(ModContext context) {
        context.logger().info(
                "Example Mod world unloaded."
        );
    }

    @Override
    public void onUnload(ModContext context) {
        context.logger().info(
                "Example Mod unloaded."
        );
    }
}
```

The class specified by:

```properties
modMainClass=examplemod.ExampleMod
```

must match the package and class name exactly.

---

# 6. Mod lifecycle

`TownsMod` provides four lifecycle hooks.

## `onLoad()`

```java
public void onLoad(ModContext context)
```

Called when the Java mod itself is loaded.

Use this for things that belong to the lifetime of the mod rather than a particular game world.

Typical uses include:

- logging startup information
- persistent registrations
- one-time mod initialisation

Do not assume a playable world exists here.

---

## `onWorldLoaded()`

```java
public void onWorldLoaded(ModContext context)
```

Called when a playable world becomes available.

Most gameplay registrations belong here.

For example:

```java
@Override
public void onWorldLoaded(ModContext context) {

    context.api()
            .ui()
            .showMessage(
                    "My Mod is active."
            );
}
```

World-dependent systems such as item behaviours should normally be registered here.

---

## `onWorldUnloaded()`

```java
public void onWorldUnloaded(ModContext context)
```

Called when the active world is being unloaded.

Use this to clear your own temporary Java state.

Resources registered through `context.worldResource(...)` are automatically released when the world unloads.

---

## `onUnload()`

```java
public void onUnload(ModContext context)
```

Called when the mod itself is unloaded.

Use this for final cleanup of mod-level state.

---

# 7. ModContext

The `ModContext` passed to lifecycle methods is the main entry point for your mod.

Important methods include:

```java
context.modId();
context.metadata();
context.api();
context.logger();
context.saveData();
context.resource(...);
context.worldResource(...);
```

Do not attempt to obtain a global `GameApi` instance.

Use:

```java
context.api()
```

instead.

---

# 8. GameApi

The main gameplay APIs are accessed through:

```java
context.api()
```

For example:

```java
var world =
        context.api().world();

var inventory =
        context.api().inventory();

var items =
        context.api().items();
```

SDK 0.1 exposes:

```java
context.api().definitions();
context.api().world();
context.api().entities();
context.api().ui();
context.api().tasks();
context.api().selection();
context.api().events();
context.api().inventory();
context.api().stockpiles();
context.api().items();
context.api().itemBehaviours();
context.api().buildings();
context.api().buildingBehaviours();
context.api().fluids();
context.api().production();
context.api().actions();
```

See `API_REFERENCE.md` for the full method signatures.

---

# 9. Namespaced definitions

Open Towns uses namespaced definition IDs.

A definition ID consists of:

```text
namespace:path
```

For example:

```text
towns:stone
lava-buckets:lava-bucket
conveyor-belts:conveyor
```

Create one in Java using:

```java
DefinitionId conveyor =
        DefinitionId.of(
                "conveyor-belts",
                "conveyor"
        );
```

Base-game definitions normally use the:

```text
towns:
```

namespace.

For example:

```java
DefinitionId stone =
        DefinitionId.towns(
                "stone"
        );
```

You can also parse a complete ID:

```java
DefinitionId id =
        DefinitionId.parse(
                "conveyor-belts:conveyor"
        );
```

Using namespaces prevents unrelated mods from accidentally defining the same ID.

---

# 10. Looking up definitions

Definitions are available through:

```java
context.api()
        .definitions()
```

For example:

```java
var item =
        context.api()
                .definitions()
                .items()
                .get(
                        "towns:woodenbucket"
                );
```

Registries are available for:

```text
items
living
buildings
actions
events
terrain
```

Definitions returned by the API are read-only snapshots.

Mods should not directly modify Towns' internal definition objects.

---

# 11. Resources and automatic cleanup

Several APIs return registrations or subscriptions.

These implement:

```java
ModResource
```

Open Towns can manage their lifecycle automatically.

For something that should exist only while a world is loaded:

```java
context.worldResource(
        context.api()
                .ui()
                .registerGameAction(
                        "example-mod:test",
                        "Test Action",
                        () -> {
                            context.api()
                                    .ui()
                                    .showMessage(
                                            "Hello!"
                                    );
                        }
                )
);
```

When the world unloads, Open Towns automatically releases the registration.

This is preferable to manually unregistering it.

For resources intended to live for the whole mod lifetime, use:

```java
context.resource(
        resource
);
```

---

# 12. UI actions

A mod can currently register simple game actions.

Example:

```java
context.worldResource(
        context.api()
                .ui()
                .registerGameAction(
                        "example-mod:hello",
                        "Say Hello",
                        () -> {
                            context.api()
                                    .ui()
                                    .showMessage(
                                            "Hello from the mod!"
                                    );
                        }
                )
);
```

`registerGameAction()` returns a:

```java
UiActionRegistration
```

The registration can be manually removed with:

```java
registration.unregister();
```

but using `worldResource()` is recommended for world-specific registrations.

The current UI registration system is intentionally small. A larger mod UI API is planned after SDK 0.1.

---

# 13. Messages

The simplest message API is:

```java
context.api()
        .ui()
        .showMessage(
                "Something happened."
        );
```

Messages can also specify a `UiMessageType`.

Available types include:

```text
SYSTEM
ANNOUNCEMENT
COMBAT
HERO
```

---

# 14. World access

Access the current world using:

```java
var world =
        context.api().world();
```

Common operations include:

```java
world.width();
world.height();
world.depth();
world.isValidPosition(position);
world.cell(position);
```

A position is represented by:

```java
WorldPosition
```

Example:

```java
WorldPosition position =
        new WorldPosition(
                10,
                20,
                5
        );
```

Positions can be offset:

```java
WorldPosition next =
        position.offset(
                1,
                0,
                0
        );
```

Convenience methods are also available for:

```text
relative cells
horizontal neighbours
rectangular areas
radius searches
```

---

# 15. Items

Individual placed items can be inspected through the item APIs.

A placed item may expose:

```text
ID
definition
position
facing
operative state
locked state
```

Direction is represented by:

```java
ItemFacing.WEST
ItemFacing.NORTH
ItemFacing.EAST
ItemFacing.SOUTH
```

This is useful for directional systems such as conveyor belts.

---

# 16. Item behaviours

Mods can attach Java behaviour to a particular item definition.

For example:

```java
private static final DefinitionId CONVEYOR =
        DefinitionId.of(
                "conveyor-belts",
                "conveyor"
        );
```

Register behaviour:

```java
context.worldResource(
        context.api()
                .itemBehaviours()
                .register(
                        CONVEYOR,
                        30,
                        tick -> {
                            var item =
                                    tick.item();

                            long simulationTick =
                                    tick.simulationTick();

                            // Behaviour here.
                        }
                )
);
```

The integer specifies the behaviour interval in simulation ticks.

Item behaviours are useful for things such as:

```text
conveyors
machines
automatic containers
special interactive objects
periodic effects
```

---

# 17. Buildings

Buildings can be queried using:

```java
context.api()
        .buildings()
```

You can:

```text
look up a building by entity ID
find a building at a position
list buildings
find buildings by definition
```

Buildings expose their origin and occupied cells.

Building behaviours can also be registered similarly to item behaviours.

---

# 18. Inventory and containers

Container access is available through:

```java
context.api()
        .inventory()
```

Examples:

```java
var container =
        context.api()
                .inventory()
                .container(
                        containerId
                );
```

A `ContainerView` exposes:

```text
container ID
contained item IDs
empty state
full state
item count
```

Inventory mutations include:

```text
create
consume
transfer
transferItem
```

---

# 19. Inventory reservations

For automation systems, use reservations when an item must not be claimed twice.

Example:

```java
var reservation =
        context.api()
                .inventory()
                .reserveItem(
                        sourceContainerId,
                        itemId
                );
```

If no reservation can be created:

```java
reservation == null
```

To transfer the reserved item:

```java
var result =
        context.api()
                .inventory()
                .transferReserved(
                        reservation,
                        targetContainerId
                );
```

If you no longer need it:

```java
context.api()
        .inventory()
        .releaseReservation(
                reservation
        );
```

Reservations are temporary runtime objects and are not persisted with the save.

A successful reserved transfer consumes the reservation.

---

# 20. Tasks

Tasks can be created through:

```java
context.api().tasks()
```

SDK 0.1 includes operations such as:

```text
dig
destroy
build
mine
mine area
begin queue-and-place
```

For example:

```java
TaskResult result =
        context.api()
                .tasks()
                .mine(
                        position
                );
```

Task results indicate whether the operation was:

```text
QUEUED
EXECUTED
REJECTED
```

For user-driven placement:

```java
context.api()
        .tasks()
        .beginQueueAndPlace(
                BUILD_CONVEYOR
        );
```

This allows the normal Towns placement system to handle positioning and rotation.

---

# 21. Area selection

Mods can ask the player to select an area:

```java
context.api()
        .selection()
        .selectArea(
                selection -> {

                    WorldPosition origin =
                            selection.origin();

                    int width =
                            selection.width();

                    int height =
                            selection.height();
                }
        );
```

The selection API also supports:

```java
isSelecting();
cancel();
```

---

# 22. Fluids

SDK 0.1 deliberately exposes the two fluids currently supported by the API:

```java
FluidApi.WATER
FluidApi.LAVA
```

Before using a fluid definition, you can check:

```java
context.api()
        .fluids()
        .supports(
                fluidId
        );
```

Read a fluid cell using:

```java
FluidView fluid =
        context.api()
                .fluids()
                .at(
                        position
                );
```

Fluid mutation operations include:

```java
fill(...)
remove(...)
```

The Lava Buckets example demonstrates this API.

---

# 23. Production

Production actions can be queued with:

```java
context.api()
        .production()
        .queue(
                actionDefinition,
                quantity
        );
```

Example:

```java
ProductionResult result =
        context.api()
                .production()
                .queue(
                        MAKE_LAVA_BUCKET,
                        1
                );
```

Useful result methods include:

```java
result.complete();
result.acceptedAnything();
result.accepted();
result.message();
```

---

# 24. Actions

World actions can be queued through:

```java
context.api()
        .actions()
        .queueAt(
                actionDefinition,
                position
        );
```

For example:

```java
ActionResult result =
        context.api()
                .actions()
                .queueAt(
                        DUMP_LAVA,
                        position
                );
```

Then check:

```java
if (result.accepted()) {
    // queued
}
```

---

# 25. Events

Mods can subscribe to game events:

```java
var subscription =
        context.api()
                .events()
                .subscribe(
                        DayStartedEvent.class,
                        event -> {
                            context.logger()
                                    .info(
                                            "New day: "
                                            + event.day()
                                    );
                        }
                );
```

Subscriptions are resources.

For a world-scoped event subscription:

```java
context.worldResource(
        subscription
);
```

SDK 0.1 currently includes events such as:

```text
DayStartedEvent
SimulationTickEvent
```

More game events can be added to the SDK over time without mods needing to depend on internal game classes.

---

# 26. Mod save data

Each mod receives its own save-data interface:

```java
context.saveData()
```

Supported value types include:

```text
String
int
long
double
boolean
```

Example:

```java
int uses =
        context.saveData()
                .getInt(
                        "uses",
                        0
                );

uses++;

context.saveData()
        .putInt(
                "uses",
                uses
        );
```

Other useful operations include:

```java
contains(...)
remove(...)
keys()
clear()
```

Use mod save data instead of writing directly into Towns' internal save objects.

---

# 27. Dependencies

A Java mod can declare dependencies on other Java mods.

For example:

```properties
modDependencies=other-mod
```

Multiple dependencies are comma separated.

Conceptually this becomes:

```properties
dependencies=other-mod,another-mod
```

in the generated mod metadata.

Open Towns resolves dependencies before loading the dependent mod.

If a required dependency:

- is missing
- cannot load
- contains a dependency cycle

the dependent mod will not be loaded.

Dependencies are resolved explicitly.

Do not rely on alphabetical load order to make one mod modify another.

---

# 28. `mod.properties`

A built Java mod contains a `mod.properties` file.

It identifies the Java component to Open Towns.

A generated file conceptually contains:

```properties
id=example-mod
name=Example Mod
version=1.0.0
author=Your Name
apiVersion=1
dependencies=
jar=<mod jar filename>
main=examplemod.ExampleMod
```

`id`, `jar`, `main`, and `apiVersion` are required by the Java mod loader.

The example Gradle project generates this file for you.

You normally edit `gradle.properties` rather than maintaining `mod.properties` manually.

---

# 29. API versions

SDK 0.1 uses:

```text
API version 1
```

The game checks the mod's requested API version before loading it.

A mod requiring a newer API than the running version will be rejected rather than being loaded against an incompatible interface.

This version is separate from your own mod version.

For example:

```text
Open Towns SDK: 0.1
API version: 1
Your mod version: 1.3.2
```

---

# 30. Building a mod

From the mod project directory on Windows:

```powershell
.\gradlew.bat build
```

The Gradle wrapper should compile your Java code and package the mod.

If compilation fails because `xaos.api.*` cannot be found, check that the project can locate the Open Towns API JAR.

The example project handles this configuration for you.

---

# 31. Installing a development build

The example project includes an installation task.

Run:

```powershell
.\gradlew.bat installMod
```

This builds the mod and installs it into the Open Towns mods directory.

The default user mod location is:

```text
C:\Users\<username>\.towns\mods
```

Your installed mod should have its own folder, for example:

```text
.towns
└── mods
    └── example-mod
        ├── mod.properties
        ├── example-mod.jar
        └── any XML or graphics files used by the mod
```

Enable the mod through the normal Open Towns mod system.

---

# 32. Content and Java can coexist

Java support does not replace the existing Towns content-mod system.

Existing XML/content-only mods can continue to work without a Java component.

A Java-enabled mod can contain both:

```text
traditional Towns XML/content
+
Java behaviour
```

For example, the Conveyor Belts mod defines the conveyor as Towns content and uses Java to give the placed item its transfer behaviour.

This is the recommended model where possible:

```text
data/content describes what something is
Java describes unusual behaviour
```

---

# 33. Graphics

Mods can continue using Towns' existing graphics configuration system.

For example, an item graphics entry can reference an atlas:

```ini
[conveyor]TILE_X = 0
[conveyor]TILE_Y = 0
[conveyor]TILE_HEIGHT = 64
[conveyor]TILE_X_ROT = 1
[conveyor]TILE_Y_ROT = 0
[conveyor]TEXTURE_FILE = items4.png
```

Rotatable items should also be configured appropriately in their item definition.

The current SDK does not provide a general Java rendering API.

The larger UI and asset system is planned separately from SDK 0.1.

---

# 34. Logging

Use the mod logger:

```java
context.logger()
        .info(
                "Something happened"
        );
```

Errors can be logged with:

```java
context.logger()
        .error(
                "Something failed"
        );
```

or:

```java
context.logger()
        .error(
                "Something failed",
                exception
        );
```

Prefer this over directly using Towns' internal logging classes.

---

# 35. Common development pattern

A typical mod will look approximately like this:

```java
public final class MyMod
        implements TownsMod {

    private static final String MOD_ID =
            "my-mod";

    private static final DefinitionId MACHINE =
            DefinitionId.of(
                    MOD_ID,
                    "machine"
            );

    @Override
    public void onLoad(
            ModContext context
    ) {
        context.logger()
                .info(
                        "My Mod loaded."
                );
    }

    @Override
    public void onWorldLoaded(
            ModContext context
    ) {

        context.worldResource(
                context.api()
                        .itemBehaviours()
                        .register(
                                MACHINE,
                                30,
                                tick -> runMachine(
                                        context,
                                        tick
                                )
                        )
        );

        context.worldResource(
                context.api()
                        .ui()
                        .registerGameAction(
                                MOD_ID + ":test",
                                "Test Machine",
                                () -> test(
                                        context
                                )
                        )
        );
    }

    private void runMachine(
            ModContext context,
            ItemBehaviourContext tick
    ) {
        // Machine behaviour.
    }

    private void test(
            ModContext context
    ) {
        context.api()
                .ui()
                .showMessage(
                        "Machine test."
                );
    }

    @Override
    public void onWorldUnloaded(
            ModContext context
    ) {
        // Clear your own temporary state here.
    }

    @Override
    public void onUnload(
            ModContext context
    ) {
        context.logger()
                .info(
                        "My Mod unloaded."
                );
    }
}
```

This structure keeps:

```text
mod lifecycle
game API access
world resources
and mod behaviour
```

clearly separated.

---

# 36. Things mods should not do

SDK mods should avoid reaching directly into internal Open Towns classes.

Avoid code such as:

```java
Game.getWorld()
ItemManager.getItem(...)
Container(...)
```

even if those classes are visible while developing Open Towns itself.

They are implementation details and can change without preserving mod compatibility.

Prefer:

```java
context.api().world()
context.api().definitions()
context.api().inventory()
```

The purpose of the public SDK is to create a compatibility boundary between mods and the original Towns codebase.

---

# 37. Current SDK 0.1 limitations

SDK 0.1 is intentionally the first stable foundation rather than a complete modding platform.

Notable limitations include:

- the UI API is still small
- custom full UI layouts are not yet exposed
- arbitrary rendering hooks are not yet part of the SDK
- fluids currently expose the supported base fluid types rather than arbitrary mod-created fluid systems
- explicit cross-mod definition overrides are not part of 0.1
- richer game-event coverage will be added later
- dependency version ranges are not yet part of the dependency system

Mods should not depend on load order to override another mod.

Future SDK releases can expand these systems while retaining the API-version boundary introduced in 0.1.

---

# 38. Example mods

## OpenTownsExampleMod

Start here.

Demonstrates:

```text
TownsMod
ModContext
logging
lifecycle hooks
simple UI access
Gradle build/install workflow
```

---

## OpenTownsLavaBuckets

Demonstrates a mod adding gameplay that does not exist in the base game.

Includes:

```text
custom definitions
production
UI actions
area selection
world actions
fluid queries
lava placement
namespaced IDs
```

---

## OpenTownsConveyorBelts

Demonstrates a more advanced runtime behaviour mod.

Includes:

```text
custom placed item
rotation/facing
item behaviours
containers
inventory reservations
inventory transfers
world position queries
automatic resource cleanup
```

It is a useful reference for creating machines or automation systems.

---

# 39. API reference

This guide explains how the SDK is intended to be used.

For exact classes, methods and signatures, use:

```text
docs/modding/API_REFERENCE.md
```

The API reference is generated directly from:

```text
opentowns-api.jar
```

and should therefore be treated as the authoritative list of Java symbols exposed by the SDK.

Do not manually add internal classes to your mod just because they are absent from the public API reference.

---

# 40. Suggested learning path

For a new modder:

1. Build and run `OpenTownsExampleMod`.
2. Rename it and change its metadata.
3. Add a simple UI action.
4. Read a world cell.
5. Add one XML-defined item.
6. Give that item an `ItemBehaviour`.
7. Look through Lava Buckets for actions/fluids.
8. Look through Conveyor Belts for inventory automation.
9. Use `API_REFERENCE.md` when you need exact method signatures.

Once those steps make sense, most of the SDK 0.1 systems can be combined without needing to work directly with the original Towns implementation.

---

# SDK 0.1 design rule

The most important rule when writing an Open Towns mod is:

> **If the SDK provides an API for something, use the API rather than accessing the original Towns implementation directly.**

That boundary is what allows Open Towns itself to continue evolving without requiring every mod to be rewritten each time the underlying game code changes.