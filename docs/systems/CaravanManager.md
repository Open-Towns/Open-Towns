
# CaravanManager

`CaravanManager` is responsible for loading caravan definitions from XML and storing them in memory for the game to use.

It manages the caravan definition list, not active caravan instances already present in the world.

---

## Location

```text
src/xaos/caravans/CaravanManager.java
```

---

## Main responsibilities

`CaravanManager` handles:

* loading the main `caravans.xml` file
* loading mod caravan XML files
* applying mod overrides
* deleting caravan definitions through XML
* creating `CaravanManagerItem` definitions
* loading possible caravan stock entries
* clearing cached caravan data

---

## Caravan loading flow

The loading flow is:

```text
CaravanManager.getItem(...)
    ->
if caravanList is null
    ->
loadItems()
    ->
load main caravans.xml
    ->
load mod caravans.xml files
    ->
store results in caravanList
```

The caravan list is lazy-loaded. It is only loaded when something first calls:

```java
CaravanManager.getItem(...)
```

---

## Main caravan file

The base caravan XML is loaded from:

```java
Towns.getPropertiesString("DATA_FOLDER") + "caravans.xml"
```

This is loaded first.

---

## Mod caravan files

After the main file is loaded, active mods are checked.

For each loaded mod, the manager looks for:

```text
<user-folder>/mods/<mod-name>/<DATA_FOLDER>/caravans.xml
```

If that file exists, it is loaded after the main file.

This means mod XML files can:

* add new caravan definitions
* override existing caravan definitions
* delete existing caravan definitions

---

## Caravan definition storage

Loaded caravans are stored in:

```java
private static HashMap<String, CaravanManagerItem> caravanList;
```

The XML node name is used as the caravan ID.

Example:

```xml
<militarybasic>
    ...
</militarybasic>
```

This creates or updates a caravan with ID:

```text
militarybasic
```

---

## Loading XML

Caravan XML is loaded by:

```java
loadCaravansFromXml(String caravanListPathName, boolean isMainFile)
```

This method:

1. loads the XML file
2. loops through its child nodes
3. skips non-element nodes
4. handles `<DELETE>` nodes
5. creates or updates caravan definitions
6. stores them in `caravanList`

---

## Creating new caravan definitions

New caravan definitions are created by:

```java
createCaravanFromXml(String id, Node node)
```

This reads:

* `zone`
* `pricePCT`
* `coins`
* `buys`
* `item`
* `comePCT`

Example:

```xml
<militarybasic>
    <zone>zmarket</zone>
    <pricePCT>1d100+300</pricePCT>
    <coins>1d2000+2000</coins>
    <buys>weapon.iron,armor.iron</buys>
    <comePCT>100</comePCT>
</militarybasic>
```

---

## Updating existing caravan definitions

When loading a mod file, if a caravan ID already exists, the manager updates the existing caravan definition.

This happens through:

```java
applyOptionalCaravanValues(CaravanManagerItem caravanData, Node node)
```

Only values present in the mod XML are applied.

This allows mods to partially override existing caravans without needing to redefine every field.

Example:

```xml
<militarybasic>
    <pricePCT>200</pricePCT>
</militarybasic>
```

This would update only the `pricePCT` value for `militarybasic`.

---

## Important behaviour: item list overrides

If a mod caravan entry contains one or more `<item>` nodes, the current implementation replaces the caravan item list with the new list.

It does not merge individual item entries.

Example:

```xml
<militarybasic>
    <item>
        <type>weapon.special</type>
        <PCT>100</PCT>
        <quantity>1</quantity>
    </item>
</militarybasic>
```

This replaces the item list for `militarybasic` rather than adding one item to the existing list.

This behaviour should be documented for modders.

---

## Deleting caravan definitions

Caravan definitions can be removed using a `DELETE` node.

Example:

```xml
<DELETE id="militarybasic" />
```

This is handled by:

```java
deleteCaravanFromXml(Node node)
```

If the ID exists in `caravanList`, it is removed.

---

## Caravan item entries

Caravan stock rules are loaded by:

```java
loadCaravanItems(NodeList nodeList)
```

Each `<item>` entry creates a `CaravanItemData`.

Example:

```xml
<item>
    <type>weapon.iron</type>
    <PCT>85</PCT>
    <quantity>1d5</quantity>
</item>
```

This means:

```text
85% chance to include iron weapons.
If selected, roll 1d5 items.
Each item is randomly chosen from the weapon.iron type.
```

---

## Required item data

Each caravan item entry must include either:

```xml
<id>specific.item.id</id>
```

or:

```xml
<type>item.type</type>
```

If both are missing, loading throws an exception.

---

## Related classes

### `CaravanManagerItem`

Represents a loaded caravan definition.

It stores values such as:

* caravan ID
* zone
* price percentage
* coins
* buys list
* item list
* arrival chance

### `CaravanItemData`

Represents a possible item entry in a caravan definition.

It stores:

* item ID
* item type
* spawn chance percentage
* quantity formula

### `CaravanData`

Represents an actual caravan instance in the game world.

This is separate from `CaravanManagerItem`.

---

## Difference between definition and instance

`CaravanManagerItem` is a template.

`CaravanData` is a real caravan in the world.

Example:

```text
CaravanManagerItem
    "militarybasic" template from XML

CaravanData
    one actual militarybasic caravan currently arriving at town
```

The manager loads definitions. Other systems create real caravan instances from those definitions.

---

## Clearing cached data

The manager can be reset with:

```java
CaravanManager.clear()
```

This sets:

```java
caravanList = null;
```

It also clears `PricesManager`.

This forces caravan and price data to reload later.

---

## Refactor notes

Recent cleanup improved the caravan loading flow by:

* renaming `loadXMLEffects` to `loadCaravansFromXml`
* loading the main XML before mod XML files
* extracting caravan creation into `createCaravanFromXml`
* extracting mod override logic into `applyOptionalCaravanValues`
* extracting delete handling into `deleteCaravanFromXml`
* improving readability with early `continue` checks
* preserving XML-driven mod behaviour

---

## Summary

`CaravanManager` is the caravan definition loader.

It does not directly control caravan movement, trading UI, or active world behaviour.

Its main job is to read caravan templates from XML and make them available by ID.

The loading order is important:

```text
main caravans.xml
    ->
mod caravans.xml files
    ->
final caravanList
```

This preserves mod support while allowing base caravans to be extended, replaced, or removed.

