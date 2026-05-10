package xaos.caravans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricesManagerTest {
    // check that the price calculation method returns the expected price based on
    // the base value and modifiers
    @Test
    void returnsZeroWhenBaseValueIsLessThanOne() {
        int result = PricesManager.calculateMilitaryPrice(
                0,
                20,
                10,
                30,
                5,
                10,
                10,
                10,
                10);

        assertEquals(0, result);
    }

    @Test
    void calculatesMilitaryPriceUsingModifiersAndDivisors() {
        int result = PricesManager.calculateMilitaryPrice(
                100,
                20,
                10,
                30,
                5,
                10,
                10,
                10,
                10);

        assertEquals(106, result);
    }

    @Test
    void usesDefaultRuleWhenDivisorsAreInvalid() {
        int result = PricesManager.calculateMilitaryPrice(
                100,
                20,
                10,
                30,
                5,
                0,
                0,
                0,
                0);

        assertEquals(100, result);
    }

    @Test
    void usesIntegerDivisionForModifierContributions() {
        int result = PricesManager.calculateMilitaryPrice(
                100,
                9,
                9,
                9,
                9,
                10,
                10,
                10,
                10);

        assertEquals(100, result);
    }

    @Test
    void addsOnlyWholePointsFromEachModifier() {
        int result = PricesManager.calculateMilitaryPrice(
                100,
                10,
                20,
                30,
                40,
                10,
                10,
                10,
                10);

        assertEquals(110, result);
    }
}
