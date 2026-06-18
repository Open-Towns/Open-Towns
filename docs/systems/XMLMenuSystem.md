# XML Menu System Documentation

## Overview

The new menu system moves menu layout out of hardcoded Java and into XML files.

Previously, menus were built directly in Java using `SmartMenu` objects. This worked, but it made menus difficult to maintain because every menu change required editing Java code and recompiling the game.

The new system keeps `SmartMenu` as the runtime menu object, but changes where the menu structure comes from.

The new flow is:

```text
XML files
    ↓
MenuXmlLoader
    ↓
MenuDefinition / MenuItemDefinition
    ↓
MenuManager
    ↓
SmartMenu objects
```

This means XML defines the menu layout, while Java still handles behaviour, rendering, generated lists, and command execution.

---

# Folder Structure

Menu XML files are organised to reflect the menu hierarchy.

Recommended structure:

```text
data/
  menus/
    main/
      root.xml

      options/
        root.xml
        graphics.xml
        audio.xml
        game.xml
        controls.xml
        performance.xml
        language.xml


     
```

## Folder meaning

The folders are mainly for organisation. They do not automatically create the menu structure on their own.

For example:

```text
data/menus/main/options/graphics.xml
```

does not automatically mean the menu ID is `main.options.graphics`.

The XML file itself defines the real menu ID:

```xml
<menu id="main.options.graphics" titleKey="menu.main.options.graphics">
```

The folder structure should match the menu ID because it makes the files easy to find, but Java uses the XML `id`, not the file path.

---

# Naming Convention

Use dot-based IDs that match the folder structure.

Examples:

```text
main.root
main.options.root
main.options.graphics
main.options.audio
main.options.game
main.options.controls
main.new_game.root
main.tutorials.root
main.load_game.root
main.mod_manager.root
game.root
game.build.root
```

This makes the menu tree easy to understand.

## Good ID examples

```xml
<menu id="main.options.root" titleKey="menu.main.options.root">
```

```xml
<menu id="main.options.graphics" titleKey="menu.main.options.graphics">
```

```xml
<menu id="main.load_game.root" titleKey="menu.main.load_game.root">
```

## Avoid vague IDs

Avoid:

```xml
<menu id="options">
```

```xml
<menu id="menu1">
```

```xml
<menu id="graphics_menu">
```

These are harder to organise once the menu system grows.

---

# Root Menus

Each folder can have a `root.xml`.

A `root.xml` file should usually represent the entry point for that section.

For example:

```text
data/menus/main/options/root.xml
```

would define:

```xml
<menu id="main.options.root" titleKey="menu.main.options.root">
```

And:

```text
data/menus/main/load_game/root.xml
```

would define:

```xml
<menu id="main.load_game.root" titleKey="menu.main.load_game.root">
```

Using `root.xml` avoids lots of repeated names like:

```text
options_menu.xml
load_game_menu.xml
new_game_menu.xml
```

It also makes it clear which file is the main menu for that folder.

---

# Basic Menu XML

A menu file has one root `<menu>` element.

Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<menu id="main.options.root" titleKey="menu.main.options.root" transparent="true">
    <item
        id="graphics"
        type="submenu"
        titleKey="menu.main.options.graphics"
        targetMenu="main.options.graphics" />

    <item
        id="audio"
        type="submenu"
        titleKey="menu.main.options.audio"
        targetMenu="main.options.audio" />

    <item
        id="game"
        type="submenu"
        titleKey="menu.main.options.game"
        targetMenu="main.options.game" />

    <item
        id="back"
        type="back" />
</menu>
```

## Menu attributes

### `id` 

The unique ID of the menu.

```xml
id="main.options.root"
```

This is how other menus link to this menu.

### `titleKey` 

The message key used for the menu title.

```xml
titleKey="menu.main.options.root"
```

This key is looked up through the existing `Messages.getString(...)` system.

### `transparent` - Deafult: True

Controls whether the menu background is drawn.

```xml
transparent="true"
```

or:

```xml
transparent="false"
```

This should usually be set on the `<menu>` itself, not on individual items.

---

# Menu Item Types

Each menu contains `<item>` elements.

The most common item types are:

```text
submenu
text
toggle
slider
keyboard
generated
back
spacer
```

---

# `submenu`

A submenu item opens another menu.

Example:

```xml
<item
    id="graphics"
    type="submenu"
    titleKey="menu.main.options.graphics"
    targetMenu="main.options.graphics" />
```

## Required attributes

```text
id
type
titleKey
targetMenu
```

## How it works

`targetMenu` must match another menu ID.

For example, this item:

```xml
targetMenu="main.options.graphics"
```

must point to a menu like this:

```xml
<menu id="main.options.graphics" titleKey="menu.main.options.graphics">
```

If the target menu does not exist, the menu loader should throw an error.

---

# `text`

A text item displays non-clickable text.

Example:

```xml
<item
    id="title"
    type="text"
    titleKey="menu.main.options.graphics" />
```

You can also use a text item as a blank spacer, but `type="spacer"` is clearer.

---

# `spacer`

A spacer adds a blank line.

Example:

```xml
<item
    id="spacer_top"
    type="spacer" />
```

This is cleaner than using:

```xml
<item type="text" />
```

Use spacers to visually separate menu sections.

---

# `back`

A back item returns to the parent menu.

Example:

```xml
<item
    id="back"
    type="back" />
```

The Java builder turns this into a `SmartMenu.TYPE_ITEM` using:

```java
CommandPanel.COMMAND_BACK
```

The label currently comes from:

```java
Messages.getString("MainMenuPanel.7")
```

Later this can be changed to use a proper key such as:

```properties
menu.common.back=Back
```

---

# `toggle`

A toggle is currently mapped to `SmartMenu.TYPE_ITEM`.

Example:

```xml
<item
    id="pause_on_start"
    type="toggle"
    titleKey="menu.main.options.game.pause_on_start"
    command="COMMAND_MM_SWITCH_PAUSE"
    dynamic="true"
    maintainOpen="true"
    textColor="default" />
```

## Notes

At the moment, `toggle` does not yet have its own custom visual type in `SmartMenu`.

For now, this:

```xml
type="toggle"
```

maps to:

```java
SmartMenu.TYPE_ITEM
```

Later, once graphical toggles are implemented, `type="toggle"` can map to a new `SmartMenu.TYPE_TOGGLE`.

---

# `slider`

A slider is currently mapped to `SmartMenu.TYPE_ITEM`.

Example:

```xml
<item
    id="ui_scale"
    type="slider"
    titleKey="menu.main.options.graphics.ui_scale"
    command="COMMAND_MM_SWITCH_UI_SCALE"
    dynamic="true"
    maintainOpen="true"
    textColor="default" />
```

## Notes

Like toggles, sliders currently behave like normal clickable menu items.

Later, once graphical sliders are implemented, `type="slider"` can map to a new `SmartMenu.TYPE_SLIDER`.

---

# `keyboard`

A keyboard item is used for keybinding entries.

Example:

```xml
<item
    id="keyboard_movement_up"
    type="keyboard"
    key="KEY_MOVE_UP"
    textColor="default" />
```

The Java builder resolves the key against `UtilsKeyboard` using reflection.

So this:

```xml
key="KEY_MOVE_UP"
```

expects a static field in `UtilsKeyboard` called:

```java
KEY_MOVE_UP
```

If the key cannot be found, the builder throws an error.

---

# `generated`

A generated item is a placeholder where Java creates a list of menu items.

Use this when menu entries come from code, files, mods, saves, campaigns, or installed content.

Example:

```xml
<item
    id="available_languages"
    type="generated"
    provider="main.options.languages.root" />
```

## Why generated items exist

Some menu entries cannot be written directly in XML because they are not known until runtime.

Examples:

```text
available languages
campaign missions
save files
mods folder contents
server list
```

The XML defines where the list appears. Java defines how the list is built.

---

# Generated Providers

The current generated providers are:

```text
main.options.languages.root
main.tutorials.root
main.new_game.root
main.load_game.saves
main.mod_manager.root
```

## Languages

```xml
<item
    id="available_languages"
    type="generated"
    provider="main.options.languages.root" />
```

This calls Java code that uses:

```java
Utils.getLanguages()
```

and creates language items using:

```java
CommandPanel.COMMAND_CHANGE_LANGUAGE
```

## Tutorials

```xml
<item
    id="tutorials"
    type="generated"
    provider="main.tutorials.root" />
```

This loads tutorial campaigns from:

```java
CampaignManager.getCampaigns()
```

and only includes campaigns where:

```java
campaignData.isTutorial() == true
```

## New Game Campaigns

```xml
<item
    id="campaigns"
    type="generated"
    provider="main.new_game.root" />
```

This loads non-tutorial campaigns from:

```java
CampaignManager.getCampaigns()
```

and only includes campaigns where:

```java
campaignData.isTutorial() == false
```

## Save Games

```xml
<item
    id="savegames"
    type="generated"
    provider="main.load_game.saves" />
```

This loads save files from:

```java
Utils.getSaveFiles()
```

and creates load/delete menu entries.

## Mods

```xml
<item
    id="mods"
    type="generated"
    provider="main.mod_manager.root" />
```

This loads mod folders from:

```java
Utils.getModsFolders()
```

and creates mod toggle entries.

---

# Commands

XML does not call Java methods directly.

Instead, XML passes a command key.

Example:

```xml
<item
    id="pause_on_start"
    type="toggle"
    titleKey="menu.main.options.game.pause_on_start"
    command="COMMAND_MM_SWITCH_PAUSE"
    dynamic="true" />
```

The builder resolves:

```xml
command="COMMAND_MM_SWITCH_PAUSE"
```

against `CommandPanel` using reflection.

So this XML expects a static field in `CommandPanel` called:

```java
COMMAND_MM_SWITCH_PAUSE
```

The resolved value is passed into `SmartMenu`.

## Why this approach is used

This avoids adding another command layer while the old command system is still in place.

It means XML stores the command key, and Java still owns the actual command values.

---

# Dynamic Text

Some menu items display values that change at runtime.

Example:

```properties
menu.main.options.audio.music=Music __MUSIC__
menu.main.options.game.autosave=Autosave: __SAVE_DAYS__
```

For these items, use:

```xml
dynamic="true"
```

Example:

```xml
<item
    id="autosave"
    type="toggle"
    titleKey="menu.main.options.game.autosave"
    command="COMMAND_MM_SWITCH_AUTOSAVE_DAYS"
    dynamic="true"
    maintainOpen="true" />
```

When `dynamic="true"` is set, `SmartMenu.render()` uses:

```java
Utils.getDynamicString(item.getName())
```

instead of drawing the name directly.

---

# Maintain Open

Some menu items should execute their command but keep the menu open.

Example:

```xml
<item
    id="ui_scale"
    type="slider"
    titleKey="menu.main.options.graphics.ui_scale"
    command="COMMAND_MM_SWITCH_UI_SCALE"
    dynamic="true"
    maintainOpen="true" />
```

This is useful for options like:

```text
volume
UI scale
tooltip scale
world zoom
toggles
sliders
```

The XML attribute:

```xml
maintainOpen="true"
```

maps to:

```java
SmartMenu.setMaintainOpen(true)
```

---

# Conditions

Conditions hide or show menu items depending on runtime state.

Example:

```xml
<item
    id="load_game"
    type="submenu"
    titleKey="menu.main.load_game"
    targetMenu="main.load_game.root"
    condition="savegames.exists" />
```

This only shows the Load Game menu if save files exist.

## Current condition support

```text
savegames.exists
```

## Negative conditions

The condition system also supports `!`.

Example:

```xml
<item
    id="no_saves"
    type="text"
    titleKey="menu.main.load_game.no_saves"
    condition="!savegames.exists" />
```

This means:

```text
show this item only when savegames.exists is false
```

---

# Text Colour

Menu items can request a text colour.

Example:

```xml
<item
    id="credits"
    type="text"
    titleKey="menu.main.credits"
    textColor="credits" />
```

Or:

```xml
<item
    id="delete_warning"
    type="text"
    titleKey="menu.main.load_game.delete_warning"
    textColor="red" />
```

Supported colour names:

```text
default
credits
red
white
black
gray
grey
light_gray
light_grey
dark_gray
dark_grey
green
blue
yellow
orange
```

Hex colours can also be supported:

```xml
textColor="#FFAA00"
```

## Important note about SmartMenu rendering

The old `SmartMenu.render()` logic used `COLORGL_SUBMENU` whenever an item had a parent. That meant item colours could be set correctly but still ignored during rendering.

The better rule is:

```text
explicit item colour should win
submenu colour should only be the fallback
```

So the render logic should use `item.getColor()` if it exists.

---

# Transparency

Transparency should usually be set on the `<menu>` element.

Example:

```xml
<menu
    id="main.options.root"
    titleKey="menu.main.options.root"
    transparent="true">
```

or:

```xml
<menu
    id="main.load_game.root"
    titleKey="menu.main.load_game.root"
    transparent="false">
```

## Why menu-level transparency matters

`SmartMenu.render()` checks transparency on the menu being rendered:

```java
if (!isTrasparency()) {
    // draw menu background
}
```

That means transparency on normal child items does not usually affect anything visually.

Menu-level transparency should map to:

```java
menu.setTrasparency(definition.isTransparent());
```

---

# Adding a New Menu

To add a new menu:

## 1. Create the XML file

Example:

```text
data/menus/main/options/debug.xml
```

## 2. Give it a unique menu ID

```xml
<menu id="main.options.debug" titleKey="menu.main.options.debug" transparent="true">
```

## 3. Add menu items

```xml
<item
    id="debug_mode"
    type="toggle"
    titleKey="menu.main.options.debug.debug_mode"
    command="COMMAND_MM_SWITCH_DEBUG"
    dynamic="true"
    maintainOpen="true" />
```

## 4. Link to it from another menu

In:

```text
data/menus/main/options/root.xml
```

add:

```xml
<item
    id="debug"
    type="submenu"
    titleKey="menu.main.options.debug"
    targetMenu="main.options.debug" />
```

## 5. Add message keys

In `messages.properties`:

```properties
menu.main.options.debug=Debug
menu.main.options.debug.debug_mode=Debug mode __DEBUG__
```

## 6. Ensure the command exists

In `CommandPanel`, there must be a static field matching the XML command:

```java
COMMAND_MM_SWITCH_DEBUG
```

If the field does not exist, the menu builder will throw an error.

---

# Adding a New Generated Provider

Use generated providers when the menu entries come from Java.

## 1. Add XML placeholder

```xml
<item
    id="servers"
    type="generated"
    provider="main.servers.list" />
```

## 2. Add provider handling in `MenuManager`

```java
if ("main.servers.list".equals(provider)) {
    buildServerItems(parent);
    return;
}
```

## 3. Add the builder method

```java
private void buildServerItems(SmartMenu parent) {
    if (Game.getServerNames() == null || Game.getServerNames().isEmpty()) {
        parent.addItem(new SmartMenu(
                SmartMenu.TYPE_TEXT,
                Messages.getString("menu.main.servers.none"),
                parent,
                null,
                null,
                null));
        return;
    }

    for (int serverIndex = 0; serverIndex < Game.getServerNames().size(); serverIndex++) {
        String serverName = Game.getServerNames().get(serverIndex);

        SmartMenu serverItem = new SmartMenu(
                SmartMenu.TYPE_ITEM,
                serverName,
                parent,
                CommandPanel.COMMAND_SELECT_SERVER,
                serverName,
                null,
                new Point3D(serverIndex, serverIndex, serverIndex),
                null);

        parent.addItem(serverItem);
    }
}
```

---

# Common Errors

## Target menu not found

Example error:

```text
Target menu not found: main.options.graphics
```

This means an item has:

```xml
targetMenu="main.options.graphics"
```

but no XML file defines:

```xml
<menu id="main.options.graphics">
```

## Unknown command key

Example error:

```text
Unknown command key: COMMAND_MM_SWITCH_UI_SCALE
```

This means the XML command does not match a static field in `CommandPanel`.

Check spelling and case.

## Unknown generated provider

Example error:

```text
Unknown generated menu provider: main.options.languages
```

This means the XML provider does not match one of the providers handled by `MenuManager`.

## Premature end of file

Example error:

```text
Premature end of file
```

This usually means the XML file is empty or only has the XML header.

Even unfinished XML files need a valid root element:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<menu id="main.options.controls" titleKey="menu.main.options.controls" transparent="true">
</menu>
```

---

# Current Limitations

## Toggles and sliders are not graphical yet

Currently:

```xml
type="toggle"
```

and:

```xml
type="slider"
```

both map to:

```java
SmartMenu.TYPE_ITEM
```

Later they should be mapped to new SmartMenu types such as:

```java
SmartMenu.TYPE_TOGGLE
SmartMenu.TYPE_SLIDER
```

## Generated items still need Java

XML can decide where generated content appears, but Java still builds the actual items.

Examples:

```text
save files
mods
campaigns
languages
```

## Some rendering behaviour still comes from legacy SmartMenu

The old `SmartMenu` rendering code may still contain assumptions from the hardcoded menu system.

One known issue is colour handling, where old code used `parent == null` to decide whether to use item colour or submenu colour.

The better long-term rule is:

```text
explicit item colour should win
submenu colour should only be a fallback
```

---

# Summary

The XML menu system separates menu layout from Java code.

XML controls:

```text
menu structure
menu labels
submenu links
item type
dynamic flags
maintain-open flags
conditions
generated provider placement
basic colours
transparency
```

Java controls:

```text
SmartMenu object creation
command resolution
runtime-generated content
save file loading
campaign loading
language loading
mod loading
rendering
actual command execution
```

This makes menus easier to edit, easier to review, and easier to mod while still keeping the existing `SmartMenu` and `CommandPanel` systems intact.
