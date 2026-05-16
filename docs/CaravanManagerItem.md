
# CaravanManagerItem

`CaravanManagerItem` represents a caravan template loaded from XML.

It is not an active caravan in the world. Instead, it defines how a caravan type should behave when the game creates a real caravan instance.

---

## Location

```text
src/xaos/caravans/CaravanManagerItem.java
````

---

## Purpose

`CaravanManagerItem` stores caravan template data such as:

* caravan ID
* destination zone
* vendor price markup formula
* starting coins formula
* item types the caravan will buy
* possible stock entries
* arrival chance percentage

When the game needs to create a real caravan, this template creates a `CaravanData` instance.

---

## Template vs instance

`CaravanManagerItem` is the template.

`CaravanData` is the real active caravan.

Example:

```text
CaravanManagerItem
    militarybasic

CaravanData
    one generated militarybasic caravan currently travelling to town
```

---

## Important fields

### `id`

The caravan template ID.

Example XML:

```xml
<militarybasic>
    ...
</militarybasic>
```

This creates a caravan template with ID:

```text
militarybasic
```

---

### `zone`

The zone this caravan attempts to visit.

Example:

```xml
<zone>zmarket</zone>
```

The zone is validated with:

```java
ZoneManager.getItem(zone)
```

If the zone is missing or invalid, loading throws an exception.

---

### `pricePercentFormula`

The dice formula used to calculate caravan vendor markup.

Example:

```xml
<pricePCT>1d100+300</pricePCT>
```

When the caravan is generated, this is rolled using:

```java
Utils.launchDice(getPricePercentFormula())
```

The result is stored on the active `CaravanData` as `pricePCT`.

Example:

```text
1d100+300
```

rolls between:

```text
301 and 400
```

This means the caravan sells items for 301% to 400% of their base caravan item price.

---

### `coins`

The dice formula used to calculate how many coins the caravan has.

Example:

```xml
<coins>1d2000+2000</coins>
```

This is rolled during caravan instance creation:

```java
caravanData.setCoins(Utils.launchDice(getCoins()))
```

---

### `buys`

A list of item types the caravan is willing to buy from the town.

Example:

```xml
<buys>weapon.iron,armor.iron,decorative.special</buys>
```

This is parsed using:

```java
Utils.getArray(...)
```

---

### `itemList`

A list of `CaravanItemData` entries.

Each entry describes a possible stock item or item type the caravan may bring.

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

### `spawnChancePercentage`

This is the caravan arrival chance loaded from `comePCT`.

Example:

```xml
<comePCT>100</comePCT>
```

The value is clamped to a valid percentage range.

If missing, it defaults to:

```text
100
```

---

## Creating a real caravan

A real caravan is created with:

```java
getInstance(int livingID, int x, int y, int z)
```

This method creates and returns a `CaravanData`.

It handles:

1. rolling possible caravan stock
2. creating item instances
3. calculating base item prices
4. grouping duplicate non-military items
5. setting caravan status
6. rolling vendor markup percentage
7. rolling caravan coins
8. setting the caravan starting point
9. creating trade menus

---

## Stock generation flow

The stock generation flow is roughly:

```text
for each CaravanItemData entry:
    roll 1-100
    if roll <= spawnChancePercent:
        roll quantity
        for each quantity:
            choose item by id or type
            validate item exists
            create item instance
            calculate item price
            add to caravan inventory
```

---

## Item selection

Each `CaravanItemData` can specify either:

```xml
<id>specific.item.id</id>
```

or:

```xml
<type>weapon.iron</type>
```

If an item ID is present, that exact item is used.

If no item ID is present, the game picks a random item from the given type:

```java
ItemManager.getRandomItemByType(caravanItemData.getItemType())
```

---

## Base item price

Each generated item has a base price calculated by:

```java
PricesManager.getPrice(item)
```

For non-military items, this is usually the item base value.

For military items, this includes stat modifiers such as:

* attack
* defense
* damage
* LOS

---

## Military items

Military items are added as separate caravan entries.

This is important because military items can have unique stat modifiers.

Example:

```java
if (item instanceof MilitaryItem) {
    alItems.add(createCaravanItemInstance(item, itemPrice));
}
```

---

## Non-military item grouping

Non-military items can be grouped into a single caravan entry if they have the same item and price.

The grouped key is:

```java
item.getIniHeader() + ":" + itemPrice
```

This means:

```text
same item + same price = combined quantity
same item + different price = separate entry
different item = separate entry
```

This avoids showing many duplicate rows in the caravan trade menu.

---

## Caravan vendor markup

`CaravanManagerItem` does not directly calculate the final displayed vendor price.

It rolls and stores the caravan markup percentage:

```java
caravanData.setPricePCT(Utils.launchDice(getPricePercentFormula()))
```

The final vendor price is later calculated in the caravan trade menu using:

```java
(caravanItemPrice * caravanData.getPricePCT()) / 100
```

Example:

```text
base item price = 335
pricePCT = 316

335 * 316 / 100 = 1058
```

---

## Trade menu setup

When the caravan instance is created, it also creates empty trade menus:

```java
caravanData.setMenuCaravanToBuy(new SmartMenu());
caravanData.setMenuTownToSell(new SmartMenu());
```

These are later populated by the UI/trade system.

---

## Refactor notes

Recent cleanup improved this class by:

* renaming `pricePCT` to `pricePercentFormula`
* renaming ambiguous percentage fields
* extracting caravan item instance creation into a helper method
* using clearer variable names
* grouping non-military duplicate items with a map
* preserving separate entries for military items
* improving error handling around percentage parsing

---

## Related classes

### `CaravanManager`

Loads caravan templates from XML and stores them by ID.

### `CaravanItemData`

Represents one possible stock rule inside a caravan template.

### `CaravanItemDataInstance`

Represents an actual item entry inside a generated caravan inventory.

### `CaravanData`

Represents a real active caravan in the world.

### `PricesManager`

Calculates base item value before caravan markup is applied.

---

## Summary

`CaravanManagerItem` is the bridge between XML caravan definitions and real in-game caravans.

It controls what a caravan can bring, how many items it generates, how much money it has, and what markup percentage it uses when selling goods.

