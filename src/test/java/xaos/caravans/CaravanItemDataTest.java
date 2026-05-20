package xaos.caravans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaravanItemDataTest {

    @Test
    void spawnChanceBelowOneIsClampedToOne() {
        CaravanItemData data = new CaravanItemData();

        data.setSpawnChancePercent(0);

        assertEquals(1, data.getSpawnChancePercent());
    }

    @Test
    void spawnChanceAboveOneHundredIsClampedToOneHundred() {
        CaravanItemData data = new CaravanItemData();

        data.setSpawnChancePercent(150);

        assertEquals(100, data.getSpawnChancePercent());
    }

    @Test
    void spawnChanceInsideRangeIsStored() {
        CaravanItemData data = new CaravanItemData();

        data.setSpawnChancePercent(85);

        assertEquals(85, data.getSpawnChancePercent());
    }

    @Test
    void spawnChanceStringIsParsed() throws Exception {
        CaravanItemData data = new CaravanItemData();

        data.setSpawnChancePercent("30");

        assertEquals(30, data.getSpawnChancePercent());
    }

    @Test
    void invalidSpawnChanceStringThrowsException() {
        CaravanItemData data = new CaravanItemData();

        assertThrows(Exception.class, () -> data.setSpawnChancePercent("abc"));
    }

    @Test
    void quantityIsStored() {
        CaravanItemData data = new CaravanItemData();

        data.setQuantity("1d5");

        assertEquals("1d5", data.getQuantity());
    }
}