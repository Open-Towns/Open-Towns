package xaos.caravans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaravanManagerItemTest {

    @Test
    void missingComePctDefaultsToOneHundred() throws Exception {
        CaravanManagerItem item = new CaravanManagerItem();

        item.setComePCT(null);

        assertEquals(100, item.getSpawnChancePercentage());
    }

    @Test
    void blankComePctDefaultsToOneHundred() throws Exception {
        CaravanManagerItem item = new CaravanManagerItem();

        item.setComePCT("   ");

        assertEquals(100, item.getSpawnChancePercentage());
    }

    @Test
    void comePctAboveOneHundredIsClamped() throws Exception {
        CaravanManagerItem item = new CaravanManagerItem();

        item.setComePCT("150");

        assertEquals(100, item.getSpawnChancePercentage());
    }

    @Test
    void comePctInsideRangeIsStored() throws Exception {
        CaravanManagerItem item = new CaravanManagerItem();

        item.setComePCT("75");

        assertEquals(75, item.getSpawnChancePercentage());
    }

    @Test
    void zeroComePctThrowsException() {
        CaravanManagerItem item = new CaravanManagerItem();

        assertThrows(Exception.class, () -> item.setComePCT("0"));
    }

    @Test
    void invalidComePctThrowsException() {
        CaravanManagerItem item = new CaravanManagerItem();

        assertThrows(Exception.class, () -> item.setComePCT("bad"));
    }

    @Test
    void pricePercentFormulaIsStored() {
        CaravanManagerItem item = new CaravanManagerItem();

        item.setPricePercentFormula("1d100+300");

        assertEquals("1d100+300", item.getPricePercentFormula());
    }

    @Test
    void coinsFormulaIsStored() {
        CaravanManagerItem item = new CaravanManagerItem();

        item.setCoins("1d2000+2000");

        assertEquals("1d2000+2000", item.getCoins());
    }
}