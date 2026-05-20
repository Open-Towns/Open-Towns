package xaos.caravans;

import xaos.tiles.entities.items.ItemManager;
import xaos.utils.Messages;

public class CaravanItemData {

    private String itemId;
    private String itemType;
    private int spawnChancePercent;
    private String itemQuantityFormula;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) throws Exception {
        if (itemId != null) {
            if (ItemManager.getItem(itemId) == null) {
                // message: "Bad caravan item [" + itemId + "]"
                throw new Exception(Messages.getString("CaravanItemData.1") + itemId + "]"); 
            }
        }

        this.itemId = itemId;
    }

    public void setItemType(String itemType) throws Exception {
        if (itemType != null) {
            if (ItemManager.getRandomItemByType(itemType) == null) {
                // message: "Bad caravan item type [" + itemType + "]"
                throw new Exception(Messages.getString("CaravanItemData.2") + itemType + "]"); 
            }
        }
        this.itemType = itemType;
    }

    public String getItemType() {
        return itemType;
    }

    public int getSpawnChancePercent() {
        return spawnChancePercent;
    }

    public void setSpawnChancePercent(int spawnChancePercent) {
        if (spawnChancePercent < 1) {
            this.spawnChancePercent = 1;
        } else if (spawnChancePercent > 100) {
            this.spawnChancePercent = 100;
        } else {
            this.spawnChancePercent = spawnChancePercent;
        }
    }

    public void setSpawnChancePercent(String percentAsString) throws Exception {
        try {
            setSpawnChancePercent(Integer.parseInt(percentAsString));
        } catch (NumberFormatException e) {
            // message: "Bad caravan item PCT [" + percentAsString + "]"
            // PCT = percentage
            throw new Exception(Messages.getString("CaravanItemData.0") + percentAsString + "]"); 
        }
    }

    public String getQuantity() {
        return itemQuantityFormula;
    }

    public void setQuantity(String itemQuantityFormula) {
        //quantity formula can be any string, it will be evaluated later when generating the caravan inventory for example 1d20+20
        this.itemQuantityFormula = itemQuantityFormula;
    }
}
