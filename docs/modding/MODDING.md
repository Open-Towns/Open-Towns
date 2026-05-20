# Towns Mod Loading Map

This is a working map of the existing mod system, based on the current source. It is not a full authoring guide yet, but it gives us the shape of the system and the best next places to build tooling.

## User Folder

At startup, `Game` asks `Utils.createUserFolder` to create the user data tree. By default this lives under:

```text
<user home>/.towns/
```

The game creates these local folders there:

```text
<user home>/.towns/save/
<user home>/.towns/mods/
<user home>/.towns/bury/
<user home>/.towns/screenshots/
```

`towns.ini` can override the base user folder with `USER_FOLDER`. The game still creates a `.towns` folder inside that configured path.

## Activating Mods

Mods are folders under:

```text
<user home>/.towns/mods/<mod name>/
```

The main menu scans that folder and exposes a Mods screen. Toggling a mod updates the `MODS` property in the user `towns.ini`. Internally, `Game.setModsLoaded` parses `MODS` as a comma-separated list, then sorts the active mod names alphabetically.

That means mod load order is currently alphabetical by folder name, not manual drag-and-drop priority.

## Overlay Pattern

Most content managers follow the same pattern:

1. Load the base file from the game tree, usually `src/data/<file>.xml` while running from Gradle.
2. Read `Game.getModsLoaded()`.
3. For each active mod, check for the matching file under `<user home>/.towns/mods/<mod name>/`.
4. If the file exists, load it after the base file.

Many XML managers also detect when a mod uses an ID that already exists. In those cases, the mod usually updates the existing entry instead of creating a second copy. That is the main extension point for balance patches and content overrides.

## Files Mods Can Overlay

The source currently checks active mods for these data files:

```text
data/actions.xml
data/buildings.xml
data/campaigns.xml
data/caravans.xml
data/effects.xml
data/events.xml
data/gods.xml
data/heroes.xml
data/items.xml
data/livingentities.xml
data/matspanel.xml
data/prefixsuffix.xml
data/prices.xml
data/priorities.xml
data/skills.xml
data/terrain.xml
data/types.xml
data/zones.xml
```

The INI/resource layer also checks mods for:

```text
graphics.ini
audio.ini
data/languages/messages.properties
data/languages/messages_<language>_<country>.properties
```

Graphics, audio, and fonts can also be resolved from active mods when code asks for those resources.

## Mission-Specific Lookup

`Utils.getPathToFile` can return multiple candidate paths for a requested file. For campaign/mission content, it checks:

```text
data/<file>
data/campaigns/<campaign id>/<file>
data/campaigns/<campaign id>/<mission id>/<file>
```

Then it appends matching active-mod paths in the same general shape:

```text
<user home>/.towns/mods/<mod name>/data/<file>
<user home>/.towns/mods/<mod name>/data/campaigns/<campaign id>/<file>
<user home>/.towns/mods/<mod name>/data/campaigns/<campaign id>/<mission id>/<file>
```

That is the most promising hook for future map/campaign tooling.

## Tooling Ideas

- A mod folder scaffold command that creates the expected directory tree and starter XML files.
- A validator that checks IDs referenced across XML files before the game tries to load them.
- A map/campaign packer that writes files into the existing `data/campaigns/<campaign>/<mission>/` shape.
- A load-order inspector that shows active mods after the current alphabetical sort.
- A diff tool that compares a mod XML entry against the base entry it overrides.
