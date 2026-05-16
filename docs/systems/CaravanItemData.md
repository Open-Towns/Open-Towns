# CaravanItemData

`CaravanItemData` represents one possible item entry in a caravan's stock generation rules.

It does **not** represent an actual item in a caravan inventory. Instead, it describes what a caravan _may_ spawn when the caravan is created.

## Location

```text
src/xaos/caravans/CaravanItemData.java
```

## Purpose

This class stores the XML-loaded rules for a caravan item entry:

- item ID
- item type
- spawn chance percentage
- quantity formula

These values are later used when generating actual caravan inventory.

## Fields

### `itemId`

A specific item ID to spawn.

Example:

```xml
<id>weapon.iron.axe</id>
```

If `itemId` is set, the caravan will try to spawn that exact item.

The setter validates the item exists using:

```java
ItemManager.getItem(itemId)
```

If the item does not exist, an exception is thrown.

### `itemType`

A category/type of item to choose randomly from.

Example:

```xml
<type>weapon.iron</type>
```

If `itemType` is set, the caravan will choose a random matching item using:

```java
ItemManager.getRandomItemByType(itemType)
```

If no item exists for that type, an exception is thrown.

### `spawnChancePercent`

The percentage chance that this caravan item entry is used.

Example:

```xml
<PCT>85</PCT>
```

This means the item entry has an 85% chance to be included when the caravan is generated.

Values are clamped:

```text
less than 1 -> 1
greater than 100 -> 100
```

This prevents invalid percentages from breaking generation.

### `itemQuantity`

The quantity formula used when the item entry is selected.

Example:

```xml
<quantity>1d5</quantity>
```

This is usually a dice-style string and is later evaluated using the game's dice rolling utilities.

## Example XML

```xml
<item>
    <type>weapon.iron</type>
    <PCT>85</PCT>
    <quantity>1d5</quantity>
</item>
```

Meaning:

```text
85% chance to add iron weapons to the caravan.
If selected, add 1d5 items.
Each item is randomly chosen from the weapon.iron type.
```

## Validation

`CaravanItemData` performs validation during loading.

### Specific item validation

```java
ItemManager.getItem(itemId)
```

If this returns `null`, the item ID is invalid.

### Item type validation

```java
ItemManager.getRandomItemByType(itemType)
```

If this returns `null`, the item type is invalid.

### Percentage validation

Percent values are parsed from XML as strings and converted with:

```java
Integer.parseInt(percentAsString)
```

If parsing fails, a `NumberFormatException` is caught and converted into a clearer loading exception.

## Relationship to caravan generation

`CaravanItemData` is used when creating a real caravan instance.

The caravan generation process roughly does this:

```text
for each CaravanItemData entry:
    roll 1-100
    if roll <= spawnChancePercent:
        roll quantity
        choose item by itemId or itemType
        create item instance
        calculate price
        add item to caravan stock
```

## Notes

`PCT` means percentage.

In caravan XML, it controls the chance that an item entry appears in the caravan's generated stock.

This class is part of caravan stock generation, not final vendor price calculation.

Final prices are handled later through:

- `PricesManager`
- caravan item base price
- caravan `pricePCT` markup
