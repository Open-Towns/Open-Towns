# Open Towns XML Menu and Styled UI System Documentation

## Overview

The menu system moves menu layout out of hardcoded Java and into XML files while still using the existing `SmartMenu` runtime model.

Previously, menus were built directly in Java with `SmartMenu` objects. This worked, but it made menu changes difficult because every layout change required Java edits and a rebuild.

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
    ↓
ContextMenu
    ↓
SmartMenuRenderer
```

XML defines structure. Java still owns behaviour, rendering, generated runtime lists, state lookup, and command execution.

This system currently supports:

```text
main menu pages
in-game context menus
centred modal menus
styled background panels
integrated menu titles
normal clickable items
buttons
toggles
sliders
keyboard binding rows
cycle/select-style options
headings
plain text
spacers
generated lists
scrollable menus
mouse-wheel scrolling
conditions
dynamic text
menu transparency
```

---

## Core Runtime Classes

### `SmartMenu`

`SmartMenu` is the runtime representation of either a menu page or an individual menu item.

A `SmartMenu` can represent:

```text
menu page / submenu
clickable item
button
toggle
slider
keyboard binding row
cycle option row
heading
plain text
spacer
```

Typical responsibilities:

```text
stores display name
stores command
stores command parameters
stores child menu items
stores colour
stores transparency
stores maintain-open behaviour
handles item activation
handles special control clicks such as sliders/cycle rows
delegates rendering to SmartMenuRenderer
delegates layout to SmartMenuLayout
```

---

### `ContextMenu`

`ContextMenu` wraps a `SmartMenu` and gives it screen position, size, scrolling, and bounds checking.

Typical responsibilities:

```text
stores x/y position
stores width/height
owns scroll state
clamps size to screen
routes mouse clicks
routes mouse-wheel scrolling
renders normal or scrollable menus
optionally recentres modal menus
```

`ContextMenu` uses its `x` and `y` as the top-left anchor.

That is correct for normal in-game context menus because they open near the mouse.

For modal menus, such as the main menu or in-game escape/options menu, the menu should be centred after its final size is known.

Important rule:

```text
Resize first, then centre.
```

---

### `MenuXmlLoader`

`MenuXmlLoader` reads XML files and turns them into menu definition objects.

It should not create game behaviour directly. Its job is to parse XML safely and produce data.

---

### `MenuDefinition` / `MenuItemDefinition`

These classes represent parsed XML before it is converted into `SmartMenu`.

They should store:

```text
menu ID
title key
transparent flag
item ID
item type
target menu
command key
parameters
dynamic flag
maintain-open flag
condition
generated provider
text colour
keyboard key reference
```

---

### `MenuManager`

`MenuManager` loads all menu XML files and builds the final `SmartMenu` tree.

Typical flow:

```java
MenuManager menuManager = new MenuManager();
menuManager.loadMenus(new File("data/menus/main"));

MenuDefinition menuRoot = menuManager.getMenu("main.root");
SmartMenu mainMenu = menuManager.buildSmartMenu(menuRoot, null);
```

`MenuManager` also handles generated providers such as languages, saves, mods, and campaigns.

---

## Folder Structure

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

    game/
      root.xml
        ...
```

The folders are mainly for organisation. They do not automatically create the menu structure.

For example:

```text
data/menus/main/options/graphics.xml
```

does not automatically mean the menu ID is:

```text
main.options.graphics
```

The XML file defines the real ID:

```xml
<menu id="main.options.graphics" titleKey="menu.main.options.graphics">
```

The folder structure should match the ID because it makes the files easier to find, but Java uses the XML `id`, not the file path.

---

## Naming Convention

Use dot-based IDs that match the folder structure.

Good examples:

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
game.options.root
game.build.root
```

Avoid vague IDs:

```xml
<menu id="options">
```

```xml
<menu id="menu1">
```

```xml
<menu id="graphics_menu">
```

These become hard to maintain as the menu tree grows.

---

## Root Menus

Each folder can have a `root.xml`.

A `root.xml` file should usually represent the entry point for that section.

Example:

```text
data/menus/main/options/root.xml
```

defines:

```xml
<menu id="main.options.root" titleKey="menu.main.options.root">
```

Using `root.xml` avoids repeated names like:

```text
options_menu.xml
load_game_menu.xml
new_game_menu.xml
```

It also makes it obvious which file is the main menu for that folder.

---

## Basic XML Format

Each XML file has one root `<menu>` element.

Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<menu id="main.options.root" titleKey="menu.main.options.root" transparent="false">
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

---

## Menu Attributes

### `id`

The unique menu ID.

```xml
id="main.options.root"
```

Other menus use this ID through `targetMenu`.

---

### `titleKey`

The message key used for the menu title.

```xml
titleKey="menu.main.options.root"
```

This is looked up through the existing `Messages.getString(...)` system.

---

### `transparent`

Controls whether a styled background panel is drawn.

```xml
transparent="true"
```

or:

```xml
transparent="false"
```

Default should be treated as:

```text
true
```

Use `transparent="false"` for menus that should have a styled background panel and integrated title.

This should usually be set on the `<menu>` element, not on individual items.

---

## Menu Item Types

The XML item types map to `SmartMenu` types.

Current runtime types:

```java
public static final int TYPE_NO_TYPE = -1;
public static final int TYPE_TEXT = 0;
public static final int TYPE_MENU = 1;
public static final int TYPE_ITEM = 2;
public static final int TYPE_BUTTON = 5;
public static final int TYPE_TOGGLE = 6;
public static final int TYPE_SLIDER = 7;
public static final int TYPE_HEADING = 8;
public static final int TYPE_KEYBOARD = 9;
public static final int TYPE_CYCLE = 10;
```

Recommended XML type mappings:

```text
submenu  -> TYPE_MENU
item     -> TYPE_ITEM
button   -> TYPE_BUTTON
toggle   -> TYPE_TOGGLE
slider   -> TYPE_SLIDER
keyboard -> TYPE_KEYBOARD
cycle    -> TYPE_CYCLE
option   -> TYPE_CYCLE
heading  -> TYPE_HEADING
text     -> TYPE_TEXT
spacer   -> TYPE_SPACER 
back     -> TYPE_BUTTON with COMMAND_BACK
generated -> Java-generated items
```

---

## `submenu`

A submenu item opens another menu.

Example:

```xml
<item
    id="graphics"
    type="submenu"
    titleKey="menu.main.options.graphics"
    targetMenu="main.options.graphics" />
```

Required attributes:

```text
id
type
titleKey
targetMenu
```

`targetMenu` must match another menu ID.

If the target menu does not exist, the loader should throw an error.

---

## `item`

A normal clickable command row.

Example:

```xml
<item
    id="load_game"
    type="item"
    titleKey="menu.main.load_game"
    command="COMMAND_MM_LOAD_GAME" />
```

Use this for normal actions such as:

```text
load game
new game
open mods folder
refresh mods
exit
delete save
```

---

## `button`

A more prominent clickable row.

Example:

```xml
<item
    id="start_game"
    type="button"
    titleKey="menu.main.start_game"
    command="COMMAND_MM_START_GAME" />
```



---

## `back`

A back item returns to the parent menu.

Example:

```xml
<item
    id="back"
    type="back" />
```

The Java builder turns this into a clickable item using:

```java
CommandPanel.COMMAND_BACK
```

The label currently comes from:

```java
Messages.getString("MainMenuPanel.7")
```



---

## `text`

A non-clickable text row.

Example:

```xml
<item
    id="description"
    type="text"
    titleKey="menu.main.options.graphics.description" />
```



---

## `heading`

A decorative non-clickable heading row.

Example:

```xml
<item
    id="graphics_heading"
    type="heading"
    titleKey="menu.main.options.graphics" />
```

Headings can use a decorative plate and section-divider styling.

---

## `spacer`

A blank line.

Example:

```xml
<item
    id="spacer_top"
    type="spacer" />
```

This is clearer than using an empty text item.

---

## `toggle`

A styled on/off control.

Example:

```xml
<item
    id="fullscreen"
    type="toggle"
    titleKey="menu.main.options.graphics.fullscreen"
    command="COMMAND_MM_SWITCH_FULLSCREEN"
    dynamic="true"
    maintainOpen="true" />
```

Visual style:

```text
[ Fullscreen                         [ ON  ● ] ]
[ Show tooltips                      [  ● OFF ] ]
```

The renderer should read the current state from existing game/options logic, not from XML, where possible.

---

## `slider`

A styled percentage/value control.

Example:

```xml
<item
    id="music_volume"
    type="slider"
    titleKey="menu.main.options.audio.music"
    command="COMMAND_MM_SET_MUSIC_VOLUME"
    maintainOpen="true" />
```

Visual style:

```text
[ Music volume                    ━━━━━●──── 70% ]
```

For current volume sliders, the renderer should read from game state:

```java
Game.getVolumeMusic()
Game.getVolumeFX()
```

If the game internally stores volume from `0–10`, render as `0–100`:

```java
displayValue = Game.getVolumeMusic() * 10;
```

and save as:

```java
gameValue = Math.round(clickedPercent / 10f);
```

---

## `keyboard`

A keyboard binding row.

Example:

```xml
<item
    id="keyboard_movement_up"
    type="keyboard"
    key="KEY_MOVE_UP"
    textColor="default" />
```

The Java builder resolves the `key` value against `UtilsKeyboard`.

This:

```xml
key="KEY_MOVE_UP"
```

expects a static field in `UtilsKeyboard` called:

```java
KEY_MOVE_UP
```

If the key cannot be found, the builder should throw an error.

Keyboard strings currently look like:

```text
Action name( Key )
```

The renderer should split the label and key using the final bracket pair.

---

## `cycle`

A styled control for options with multiple possible values.

Example:

```xml
<item
    id="sieges"
    type="cycle"
    titleKey="menu.main.options.game.sieges"
    command="COMMAND_MM_SWITCH_SIEGES"
    maintainOpen="true" />
```

Visual style:

```text
[ Difficulty                      < Normal > ]
[ Autosave                        < 5 days > ]
[ Sieges                          < On     > ]
```

For now, cycle values should not be passed from XML unless the underlying option logic is fully understood.

Instead, XML identifies the command and Java resolves the current display value from existing game state.

Example:

```java
if (command.equals(CommandPanel.COMMAND_MM_SWITCH_SIEGES)) {
    return getOnOffText(Game.isSiegesEnabled());
}
```

Clicking a cycle row should execute the existing command and keep the old Java option system as the source of truth.

---

## `generated`

A generated item is a placeholder where Java creates runtime menu entries.

Example:

```xml
<item
    id="available_languages"
    type="generated"
    provider="main.options.languages.root" />
```

Use generated items when entries come from runtime data:

```text
available languages
campaigns
tutorials
save files
mods
servers
```

XML defines where the generated list appears. Java defines how the list is built.

---

## Generated Providers

Current generated providers:

```text
main.options.languages.root
main.tutorials.root
main.new_game.root
main.load_game.saves
main.mod_manager.root
```

### Languages

```xml
<item
    id="available_languages"
    type="generated"
    provider="main.options.languages.root" />
```

Uses:

```java
Utils.getLanguages()
```

and creates language items using:

```java
CommandPanel.COMMAND_CHANGE_LANGUAGE
```

---

### Tutorials

```xml
<item
    id="tutorials"
    type="generated"
    provider="main.tutorials.root" />
```

Uses:

```java
CampaignManager.getCampaigns()
```

and includes only campaigns where:

```java
campaignData.isTutorial() == true
```

---

### New Game Campaigns

```xml
<item
    id="campaigns"
    type="generated"
    provider="main.new_game.root" />
```

Uses:

```java
CampaignManager.getCampaigns()
```

and includes only campaigns where:

```java
campaignData.isTutorial() == false
```

---

### Save Games

```xml
<item
    id="savegames"
    type="generated"
    provider="main.load_game.saves" />
```

Uses:

```java
Utils.getSaveFiles()
```

and creates load/delete menu entries.

---

### Mods

```xml
<item
    id="mods"
    type="generated"
    provider="main.mod_manager.root" />
```

Uses mod folder data and creates mod toggle entries.

---

## Commands

XML does not call Java methods directly.

Instead, XML stores a command key.

Example:

```xml
<item
    id="pause_on_start"
    type="toggle"
    titleKey="menu.main.options.game.pause_on_start"
    command="COMMAND_MM_SWITCH_PAUSE"
    dynamic="true"
    maintainOpen="true" />
```

The builder resolves:

```xml
command="COMMAND_MM_SWITCH_PAUSE"
```

against `CommandPanel` using reflection.

This expects a static field in `CommandPanel` called:

```java
COMMAND_MM_SWITCH_PAUSE
```

The resolved command value is passed into `SmartMenu`.

This avoids creating a second command system while the existing `CommandPanel` logic is still in use.

---

## Dynamic Text

Some menu items display values that change at runtime.

Example message keys:

```properties
menu.main.options.audio.music=Music __MUSIC__
menu.main.options.game.autosave=Autosave: __SAVE_DAYS__
```

Use:

```xml
dynamic="true"
```

Example:

```xml
<item
    id="autosave"
    type="cycle"
    titleKey="menu.main.options.game.autosave"
    command="COMMAND_MM_SWITCH_AUTOSAVE_DAYS"
    dynamic="true"
    maintainOpen="true" />
```

When `dynamic="true"` is set, the menu can use:

```java
Utils.getDynamicString(item.getName())
```

instead of drawing the raw name.


---

## Maintain Open

Some menu items should execute their command but keep the menu open.

Example:

```xml
<item
    id="music_volume"
    type="slider"
    titleKey="menu.main.options.audio.music"
    command="COMMAND_MM_SET_MUSIC_VOLUME"
    maintainOpen="true" />
```

Useful for:

```text
volume
UI scale
tooltip scale
world zoom
toggles
sliders
cycle options
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

## Conditions

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

Current condition support:

```text
savegames.exists
```

Negative conditions use `!`.

Example:

```xml
<item
    id="no_saves"
    type="text"
    titleKey="menu.main.load_game.no_saves"
    condition="!savegames.exists" />
```

---

## Text Colour

Menu items can request a text colour.

Example:

```xml
<item
    id="credits"
    type="text"
    titleKey="menu.main.credits"
    textColor="credits" />
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

Explicit item colour should win. Submenu/default colours should only be fallbacks.

---

## Transparency and Panel Backgrounds

Transparency should usually be set on the `<menu>` element.

Example:

```xml
<menu
    id="main.options.root"
    titleKey="menu.main.options.root"
    transparent="false">
```

Runtime behaviour:

```text
isTrasparency() == true  -> render rows only
isTrasparency() == false -> render styled panel background and integrated title
```

The current method name is misspelled as `isTrasparency()`. Keep it unless doing a larger cleanup.

For XML loading:

```java
menu.setTrasparency(definition.isTransparent());
```

---

## Main Menu Flow

The main menu creates a `ContextMenu`, loads XML menus through `MenuManager`, builds a `SmartMenu`, and renders it through `menu.render()`.

Recommended flow:

```java
public void createMenu() {
    setLoadingText(new String());
    loadingGame = false;

    MenuManager menuManager = new MenuManager();
    menuManager.loadMenus(new File("data/menus/main"));

    MenuDefinition menuRoot = menuManager.getMenu("main.root");
    SmartMenu mainMenu = menuManager.buildSmartMenu(menuRoot, null);

    menu = new ContextMenu();
    menu.setHeight(MainFrame.MIN_HEIGHT - UtilFont.MAX_HEIGHT * 8);
    menu.setSmartMenu(mainMenu);

    repositionMenu();
}
```

For centred main menus, use:

```java
private void repositionMenu() {
    if (menu == null || menu.getSmartMenu() == null) {
        return;
    }

    menu.resize();

    xMenu = renderX + (renderWidth - menu.getWidth()) / 2;
    yMenu = renderY + (renderHeight - menu.getHeight()) / 2;

    if (xMenu < renderX + 20) {
        xMenu = renderX + 20;
    }

    if (yMenu < renderY + 20) {
        yMenu = renderY + 20;
    }

    menu.setX(xMenu);
    menu.setY(yMenu);
}
```

When clicking the main menu, pass relative coordinates using the actual menu position:

```java
menu.mousePressed(x - menu.getX(), y - menu.getY());
repositionMenu();
```

---

## Centred Menus vs Anchored Menus

There are two different menu positioning behaviours.

### Anchored Menus

Used for normal in-game context menus.

```text
x/y = top-left anchor near the mouse
```

These should not be automatically centred.

### Centred Menus

Used for modal menus:

```text
main menu
escape/options menu
confirm dialogs
```

These should be centred after every size change.

Recommended `ContextMenu` support:

```java
private boolean centered;

public void setCentered(boolean centered) {
    this.centered = centered;
}

private void centerOnScreen() {
    setX(UtilsGL.getWidth() / 2 - getWidth() / 2);
    setY(UtilsGL.getHeight() / 2 - getHeight() / 2);
}
```

At the end of `resize()`:

```java
if (centered) {
    centerOnScreen();
}
```

Use only for modal menus:

```java
ContextMenu menuExit = createEscapeMenu();
menuExit.setCentered(true);
menuExit.resize();
Game.setContextMenu(menuExit);
```

---

## Rendering Package Structure

Recommended package:

```text
xaos.panels.menus.rendering
```

Recommended classes:

```text
SmartMenuRenderer
SmartMenuLayout
MenuPrimitiveRenderer
MenuPanelRenderer
MenuItemRenderer
MenuButtonRenderer
MenuToggleRenderer
MenuSliderRenderer
MenuKeyboardRenderer
MenuCycleRenderer
MenuTextRenderer
MenuTextSanitiser
```

`SmartMenu` should stay mostly as a data and input object. Rendering details should live in renderer classes.

---

## `SmartMenuRenderer`

`SmartMenuRenderer` dispatches to specialised renderers.

Example:

```java
switch (item.getType()) {
    case SmartMenu.TYPE_TEXT:
    case SmartMenu.TYPE_HEADING:
        MenuTextRenderer.render(item, text, x, y, width, height);
        break;

    case SmartMenu.TYPE_MENU:
    case SmartMenu.TYPE_ITEM:
        MenuItemRenderer.render(item, text, x, y, width, height, hovered);
        break;

    case SmartMenu.TYPE_BUTTON:
        MenuButtonRenderer.render(item, text, x, y, width, height, hovered);
        break;

    case SmartMenu.TYPE_TOGGLE:
        MenuToggleRenderer.render(item, text, x, y, width, height, hovered);
        break;

    case SmartMenu.TYPE_SLIDER:
        MenuSliderRenderer.render(item, text, x, y, width, height, hovered);
        break;

    case SmartMenu.TYPE_KEYBOARD:
        MenuKeyboardRenderer.render(item, text, x, y, width, height, hovered);
        break;

    case SmartMenu.TYPE_CYCLE:
        MenuCycleRenderer.render(item, text, x, y, width, height, hovered);
        break;
}
```

---

## Styled Panel Rendering

If a menu is not transparent, draw a styled panel behind it.

Panel renderer responsibilities:

```text
outer shadow
outer frame
inner frame
dark fill
integrated title plate
optional title redraw over clipped content
```

Important constants:

```java
public static final int PANEL_PADDING_X = 12;
public static final int PANEL_PADDING_Y = 18;
```

If title spacing needs improving later:

```java
public static final int PANEL_PADDING_TOP = 24;
public static final int PANEL_PADDING_BOTTOM = 14;
```

---

## Rendering With Panel Padding

When a menu has a panel, item rows must render inside the padded content area.

Correct pattern:

```java
int contentX = x;
int contentY = y;
int contentWidth = width;
int contentHeight = height;

if (!menu.isTrasparency()) {
    MenuPanelRenderer.renderPanel(menu, x, y, width, height);

    contentX = x + MenuPanelRenderer.PANEL_PADDING_X;
    contentY = y + MenuPanelRenderer.PANEL_PADDING_Y;
    contentWidth = width - MenuPanelRenderer.PANEL_PADDING_X * 2;
    contentHeight = height - MenuPanelRenderer.PANEL_PADDING_Y * 2;
}
```

Rows must render with:

```java
renderMenuItemByType(
    item,
    text,
    contentX,
    itemScreenY,
    contentWidth,
    itemHeight,
    hovered);
```

Do not render rows using the original full `x` and `width`, otherwise rows will touch the panel edges.

---

## Scrollable Menus

Scrollable menus are owned by `ContextMenu`.

Keyboard menus can be clamped more aggressively:

```java
private static final int MAX_KEYBOARD_VISIBLE_ROWS = 10;
```

This keeps keybinding menus manageable.

---

## Correct Scroll Height Logic

For panel menus, the visible content area is smaller than the full outer menu because the panel has padding.

Use separate values:

```text
menuContentHeight = height of all rows
fullPanelHeight   = menuContentHeight + panel padding
visibleContent    = current visible height - panel padding
maxScrollY        = menuContentHeight - visibleContent
```

Recommended pattern:

```java
int menuContentHeight = smartMenu.getContentHeight() + 8;

int fullPanelHeight = menuContentHeight;

if (!smartMenu.isTrasparency()) {
    fullPanelHeight += MenuPanelRenderer.PANEL_PADDING_Y * 2;
}

scrollable = fullPanelHeight > maxHeight;

if (scrollable) {
    setHeight(maxHeight);
} else {
    setHeight(fullPanelHeight);
    scrollY = 0;
}

int visibleContentHeight = getHeight();

if (!smartMenu.isTrasparency()) {
    visibleContentHeight -= MenuPanelRenderer.PANEL_PADDING_Y * 2;
}

int maxScrollY = menuContentHeight - visibleContentHeight;

if (maxScrollY < 0) {
    maxScrollY = 0;
}
```

This prevents the final row, especially Back, from being cut off at the bottom.

---

## Clipping Scrollable Content

Scrollable rows should not draw over the panel title or top lip.

Use `GL_SCISSOR_TEST`.

OpenGL scissor uses bottom-left coordinates. The UI uses top-left coordinates.

```java
private static void beginClip(int x, int y, int width, int height) {
    if (width <= 0 || height <= 0) {
        return;
    }

    int scissorY = UtilsGL.getHeight() - y - height;

    GL11.glEnable(GL11.GL_SCISSOR_TEST);
    GL11.glScissor(x, scissorY, width, height);
}

private static void endClip() {
    GL11.glDisable(GL11.GL_SCISSOR_TEST);
}
```

Always use `try/finally`:

```java
beginClip(contentX, contentY, contentWidth, contentHeight);

try {
    // render rows
} finally {
    endClip();
}
```

If the title plate should always appear above rows, redraw it after clipped rendering:

```java
if (!menu.isTrasparency()) {
    MenuPanelRenderer.renderPanelTitleOnly(menu, panelX, panelY, panelWidth);
}
```

---

## Mouse Wheel Scrolling

Main menu wheel events should be routed through `MainMenuPanel`:

```java
if (getPanelMainMenu().isActive()) {
    getPanelMainMenu().mouseWheelMoved(mouseWheelMoved, mouseX, mouseY);
    continue;
}
```

Then pass them to the main menu `ContextMenu`:

```java
public void mouseWheelMoved(int amount, int mouseX, int mouseY) {
    if (menu == null || menu.getSmartMenu() == null) {
        return;
    }

    if (mouseX < menu.getX()
            || mouseX >= menu.getX() + menu.getWidth()
            || mouseY < menu.getY()
            || mouseY >= menu.getY() + menu.getHeight()) {
        return;
    }

    menu.mouseWheelMoved(amount);
}
```

---

## Mouse Click Handling

`MainMenuPanel` should pass relative coordinates:

```java
menu.mousePressed(x - menu.getX(), y - menu.getY());
```

`ContextMenu.mousePressed()` should adjust for panel padding and scroll:

```java
public void mousePressed(int x, int y) {
    if (getSmartMenu() == null) {
        return;
    }

    if (x < 0 || x >= getWidth()) {
        return;
    }

    if (y < 0 || y >= getHeight()) {
        return;
    }

    int menuX = x;
    int menuY = y;

    if (!getSmartMenu().isTrasparency()) {
        menuX -= MenuPanelRenderer.PANEL_PADDING_X;
        menuY -= MenuPanelRenderer.PANEL_PADDING_Y;
    }

    if (menuX < 0 || menuY < 0) {
        return;
    }

    int finalMenuY = scrollable ? menuY + scrollY : menuY;

    setSmartMenu(getSmartMenu().mousePressed(
        menuX,
        finalMenuY,
        getContentClickWidth()));

    resize();
}
```

---

## Slider Behaviour

Use shared constants for rendering and input math.

```java
public static final int SLIDER_WIDTH = 150;
public static final int SLIDER_HEIGHT = 24;
public static final int SLIDER_RIGHT_PADDING = 14;
public static final int SLIDER_GAP = 14;
public static final int VALUE_TEXT_WIDTH = 34;
public static final int VALUE_TEXT_GAP = 8;
```

Click-to-set logic should use the same constants as the renderer.

For current volume sliders:

```text
render value -> Game.getVolumeMusic() / Game.getVolumeFX()
click value  -> Game.setVolumeMusic(...) / Game.setVolumeFX(...)
save         -> Utils.saveOptions()
```

If internal volume uses `0–10`, convert to/from percent.

---

## Toggle Behaviour

Toggles render as styled rows with a switch on the right.

The row must reserve width for the switch so text does not overlap.

Recommended width addition:

```java
if (item.getType() == SmartMenu.TYPE_TOGGLE) {
    itemWidth += 130;
}
```

---

## Keyboard Behaviour

Keyboard rows render the action label on the left and a keycap on the right.

Example:

```text
[ Move camera up                         [ W ] ]
[ Pause                               [ SPACE ] ]
```

Split using the final bracket pair:

```java
int openBracket = text.lastIndexOf("(");
int closeBracket = text.lastIndexOf(")");
```

Keyboard rows should have a stronger horizontal inset so they do not touch the panel edge.

---

## Cycle Option Behaviour

Cycle rows show a current value but should not duplicate hidden game option logic in XML.

XML:

```xml
<item
    id="sieges"
    type="cycle"
    titleKey="menu.main.options.game.sieges"
    command="COMMAND_MM_SWITCH_SIEGES"
    maintainOpen="true" />
```

Java renderer:

```java
if (command.equals(CommandPanel.COMMAND_MM_SWITCH_SIEGES)) {
    return getOnOffText(Game.isSiegesEnabled());
}
```

Click behaviour:

```java
CommandPanel.executeCommand(
    item.getCommand(),
    item.getParameter(),
    item.getParameter2(),
    item.getDirectCoordinates(),
    null,
    0);
```

This keeps existing Java as the source of truth.

---

## Text Sanitising

`MenuTextSanitiser` must preserve translations.

Do not remove non-ASCII characters.

Avoid:

```java
text = text.replaceAll("[^\\x00-\\x7F]", "");
```

Avoid:

```java
text = text.replaceAll("[^a-zA-Z0-9 ]", "");
```

These break Spanish and other translations.

Safe behaviour:

```text
preserve Unicode letters
remove null/control characters
remove known leaked formatting markers
trim whitespace
```

Example:

```java
public static String sanitise(String text) {
    if (text == null) {
        return "";
    }

    String result = text;

    result = result.replace("$NON-NLS-1$", "");
    result = result.replace("$NON-NLS-2$", "");

    result = removeControlCharacters(result);

    return result.trim();
}
```

---

## Recommended Widths

`SmartMenuLayout.getRecommendedWidth()` should account for control space.

```java
if (item.getType() == SmartMenu.TYPE_ITEM || item.getType() == SmartMenu.TYPE_MENU) {
    itemWidth += 60;
}

if (item.getType() == SmartMenu.TYPE_TEXT) {
    itemWidth += 50;
}

if (item.getType() == SmartMenu.TYPE_HEADING) {
    itemWidth += 90;
}

if (item.getType() == SmartMenu.TYPE_TOGGLE) {
    itemWidth += 130;
}

if (item.getType() == SmartMenu.TYPE_SLIDER) {
    itemWidth += 210;
}

if (item.getType() == SmartMenu.TYPE_KEYBOARD) {
    itemWidth += 210;
}

if (item.getType() == SmartMenu.TYPE_CYCLE) {
    itemWidth += 210;
}
```

For non-transparent panel menus, `ContextMenu.resize()` should add panel padding:

```java
int recommendedWidth = smartMenu.getRecommendedWidth();

if (!smartMenu.isTrasparency()) {
    recommendedWidth += MenuPanelRenderer.PANEL_PADDING_X * 2;
}

if (scrollable) {
    recommendedWidth += 12;
}

setWidth(recommendedWidth);
```

---

## Recommended Heights

```java
case SmartMenu.TYPE_TEXT:
    return UtilFont.MAX_HEIGHT + 18;

case SmartMenu.TYPE_HEADING:
    return UtilFont.MAX_HEIGHT + 24;

case SmartMenu.TYPE_KEYBOARD:
    return UtilFont.MAX_HEIGHT + 24;

case SmartMenu.TYPE_MENU:
case SmartMenu.TYPE_ITEM:
case SmartMenu.TYPE_BUTTON:
case SmartMenu.TYPE_TOGGLE:
case SmartMenu.TYPE_SLIDER:
case SmartMenu.TYPE_CYCLE:
    return UtilFont.MAX_HEIGHT + 30;
```

---

## Adding a New Menu

### 1. Create the XML file

```text
data/menus/main/options/debug.xml
```

### 2. Give it a unique menu ID

```xml
<menu id="main.options.debug" titleKey="menu.main.options.debug" transparent="false">
```

### 3. Add menu items

```xml
<item
    id="debug_mode"
    type="toggle"
    titleKey="menu.main.options.debug.debug_mode"
    command="COMMAND_MM_SWITCH_DEBUG"
    dynamic="true"
    maintainOpen="true" />
```

### 4. Link to it from another menu

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

### 5. Add message keys

```properties
menu.main.options.debug=Debug
menu.main.options.debug.debug_mode=Debug mode __DEBUG__
```

### 6. Ensure the command exists

In `CommandPanel`:

```java
COMMAND_MM_SWITCH_DEBUG
```

---

## Adding a Generated Provider

### 1. Add XML placeholder

```xml
<item
    id="servers"
    type="generated"
    provider="main.servers.list" />
```

### 2. Add provider handling in `MenuManager`

```java
if ("main.servers.list".equals(provider)) {
    buildServerItems(parent);
    return;
}
```

### 3. Add the builder method

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

## Common Errors

### Target menu not found

```text
Target menu not found: main.options.graphics
```

An item points to:

```xml
targetMenu="main.options.graphics"
```

but no XML file defines:

```xml
<menu id="main.options.graphics">
```

---

### Unknown command key

```text
Unknown command key: COMMAND_MM_SWITCH_UI_SCALE
```

The XML command does not match a static field in `CommandPanel`.

Check spelling and case.

---

### Unknown generated provider

```text
Unknown generated menu provider: main.options.languages
```

The XML provider does not match a provider handled by `MenuManager`.

---

### Premature end of file

```text
Premature end of file
```

The XML file is empty or incomplete.

Even unfinished XML files need a valid root element:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<menu id="main.options.controls" titleKey="menu.main.options.controls" transparent="true">
</menu>
```

---

## OpenGL State Safety

Menu renderers use immediate-mode OpenGL.

Renderers should clean up after themselves.

`MenuPrimitiveRenderer.drawColoredRect()` should restore texture and colour state:

```java
public static void drawColoredRect(int x, int y, int width, int height, float r, float g, float b) {
    if (width <= 0 || height <= 0) {
        return;
    }

    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glColor4f(r, g, b, 1f);

    GL11.glBegin(GL11.GL_QUADS);
    GL11.glVertex2i(x, y);
    GL11.glVertex2i(x + width, y);
    GL11.glVertex2i(x + width, y + height);
    GL11.glVertex2i(x, y + height);
    GL11.glEnd();

    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glColor4f(1f, 1f, 1f, 1f);
    GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
}
```

When using clipping, always disable scissor after rendering.

---


## Current Status

Completed:

```text
XML-driven main menu loading
folder-based XML organisation
menu ID naming convention
submenu linking
generated providers
conditions
dynamic text flags
maintain-open flags
text colours
menu transparency
styled panel backgrounds
integrated menu titles
styled clickable items
styled buttons
styled toggles
styled sliders
slider click-to-set behaviour
styled keyboard rows
styled text and headings
styled cycle option rows
main menu centring
centred modal menu support
scrollable keyboard menus
scrollbar support
scissor clipping for scroll content
safer text sanitising for translations
```

Still worth improving later:

```text
drag support for sliders
proper central state resolver for controls
cleaner XML option schema once old option logic is understood
better generated provider registry
replace isTrasparency() with isTransparent()
unit tests for layout width/height calculations
remove legacy render-time texture deletion/startup thread behaviour
cache expensive dynamic text
formal XML schema documentation
```

---

## Summary

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
layout
input behaviour
state lookup
actual command execution
```

This keeps menu layout editable and reviewable while preserving the existing `SmartMenu` and `CommandPanel` systems.
