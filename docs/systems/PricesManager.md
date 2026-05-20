# PricesManager

`PricesManager` is responsible for calculating the base value of items, including extra value from military item stats.

It does **not** calculate the final vendor/shop price shown in caravan trade menus. Caravan prices apply an additional markup afterwards using `pricePCT`.

---

# Location

```text
src/xaos/caravans/PricesManager.java

```

---

# What it does

`PricesManager` calculates:

- normal item value
- military item value
- military stat bonus value
- price rules loaded from `prices.xml`

---

# Price rules

Price rules are loaded from:

```text
data/prices.xml
```

Example:

```xml
<attack>18</attack>
<defense>18</defense>
<damage>18</damage>
<LOS>1</LOS>
```

These values are divisors.

They mean:

```text
every X points of that stat adds +1 to item value
```

So if:

```xml
<attack>18</attack>
```

then every 18 attack points adds +1 value.

---

# Normal item pricing

For non-military items:

```java
ItemManager.getItem(itemHeader).getValue()
```

If the value is less than `1`, the result is `0`.

---

# Military item pricing

Military items use:

```text
final base price =
base item value
+ attackModifier / attackRule
+ defenseModifier / defenseRule
+ damageModifier / damageRule
+ losModifier / losRule
```

Java integer division is used, so results are rounded down.

## Example

```text
base value = 300
attack = 192
defense = 175
damage = 292
LOS = 0
```

Rules:

```text
attack = 18
defense = 18
damage = 18
LOS = 1
```

Calculation:

```text
300
+ (192 / 18)
+ (175 / 18)
+ (292 / 18)
+ (0 / 1)

= 300 + 10 + 9 + 16 + 0
= 335
```

So `PricesManager` returns:

```text
335
```

---

# Caravan vendor prices

The caravan vendor price is calculated later.

Caravan menu pricing uses:

```java
(caravanItemPrice * caravanData.getPricePCT()) / 100
```

So if `PricesManager` returns `335`, and the caravan has:

```xml
<pricePCT>1d100+300</pricePCT>
```

then the caravan may roll something like:

```text
316
```

Final vendor price:

```text
335 * 316 / 100 = 1058
```

This means:

```text
PricesManager price != displayed caravan vendor price
```

`PricesManager` calculates the base value.

The caravan system applies the vendor markup.

---

# Refactor notes

The refactor introduced:

- `PriceType` enum
- central price rule lookup
- `calculateMilitaryPrice(...)`
- safer handling of invalid divisors
- JUnit tests for military price calculation

---

# PriceType enum

Price rules are accessed through:

```java
PriceType.ATTACK
PriceType.DEFENSE
PriceType.DAMAGE
PriceType.LOS
```

This avoids repeated getter methods like:

```java
getAttackPrice()
getDefensePrice()
getDamagePrice()
getLOSPrice()
```

---

# Testing

Unit tests currently cover:

- base value less than 1 returns 0
- military stat modifiers increase price correctly
- invalid divisors fall back safely
- integer division behaviour
- whole-point modifier contribution

Run tests with:

```powershell
.\gradlew test
```

---

# Important behaviour

Invalid price rules are protected with a default divisor.

If a divisor is less than `1`, it falls back to:

```text
1000
```

This prevents divide-by-zero errors and stops invalid XML values from causing extreme prices.

---

# Summary

`PricesManager` should be treated as the base item valuation system.

The full displayed caravan price is:

```text
PricesManager.getPrice(item) * caravan pricePCT / 100
```
