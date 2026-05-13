package xaos.caravans;

import java.util.ArrayList;
import java.util.HashMap;

import xaos.data.CaravanData;
import xaos.main.World;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;

import xaos.utils.Messages;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.zones.ZoneManager;

public class CaravanManagerItem {

    private String id;
    private String zone;
    private String pricePercentFormula;
    private String coins;
    private ArrayList<String> buys;
    private ArrayList<CaravanItemData> itemList;
    private int spawnChancePercentage;

    public CaravanData createCaravanInstance(int livingID, int x, int y, int z) {

        CaravanData caravanData = new CaravanData();

        // Items
        ArrayList<CaravanItemDataInstance> caravanItemList = new ArrayList<CaravanItemDataInstance>();
        HashMap<String, CaravanItemDataInstance> itemCounts = new HashMap<String, CaravanItemDataInstance>();

        int itemTypeQuantity;

        for (CaravanItemData caravanItemData : itemList) {

            if (Utils.getRandomBetween(1, 100) <= caravanItemData.getSpawnChancePercent()) {

                itemTypeQuantity = Utils.launchDice(caravanItemData.getQuantity());

                for (int q = 0; q < itemTypeQuantity; q++) {
                    String itemId = caravanItemData.getItemId() != null ? caravanItemData.getItemId()
                            : ItemManager.getRandomItemByType(caravanItemData.getItemType()).getIniHeader();

                    if (itemId == null) {
                        continue;
                    }

                    ItemManagerItem itemManagerItem = ItemManager.getItem(itemId);
                    if (itemManagerItem == null || itemManagerItem.getValue() <= 0) {
                        continue;
                    }

                    Item item = Item.createItem(itemManagerItem);
                    int itemPrice = PricesManager.getPrice(item);

                    if (item instanceof MilitaryItem) {
                        CaravanItemDataInstance caravanItemDataInstance = createCaravanItemInstance(item, itemPrice);
                        caravanItemList.add(caravanItemDataInstance);
                    } else {
                        // For non-military items, we want to combine them if they are the same item and
                        // price - itemKey allows items to be combined in the same caravan if they have
                        // the same item and price but allows different items or same item with
                        // different price to be separate entries in the caravan
                        String itemKey = item.getIniHeader() + ":" + itemPrice;
                        CaravanItemDataInstance existing = itemCounts.get(itemKey);

                        if (existing != null) {
                            existing.setQuantity(existing.getQuantity() + 1);
                        } else {
                            CaravanItemDataInstance newInstance = new CaravanItemDataInstance();
                            newInstance.setItem(item);
                            newInstance.setQuantity(1);
                            newInstance.setPrice(itemPrice);

                            itemCounts.put(itemKey, newInstance);
                        }
                    }

                }
                caravanItemList.addAll(itemCounts.values());
            }
        }

        caravanData.setAlItems(caravanItemList);
        caravanData.setStatus(CaravanData.STATUS_COMING);
        caravanData.setLivingId(livingID);
        caravanData.setPricePCT(Utils.launchDice(getPricePercentFormula()));
        caravanData.setCoins(Utils.launchDice(getCoins()));
        caravanData.setStartingPoint(Point3DShort.getPoolInstance(x, y, z));
        caravanData.setTurnsToLeave(World.TIME_MODIFIER_DAY * 3);

        // To buy
        caravanData.setMenuCaravanToBuy(new SmartMenu());

        // To sell
        caravanData.setMenuTownToSell(new SmartMenu());

        return caravanData;

    }

    private CaravanItemDataInstance createCaravanItemInstance(Item item, int itemPrice) {
        CaravanItemDataInstance newInstance = new CaravanItemDataInstance();
        newInstance.setItem(item);
        newInstance.setPrice(itemPrice);
        newInstance.setQuantity(1);
        return newInstance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setZone(String zone) throws Exception {
        if (zone == null || zone.length() == 0) {
            // message: "Empty caravan zone"
            throw new Exception(Messages.getString("CaravanManagerItem.1"));
        }

        if (ZoneManager.getItem(zone) == null) {
            // message: "Unknown caravan zone [" + zone + "]"
            throw new Exception(Messages.getString("CaravanManagerItem.2") + zone + "]");
        }

        this.zone = zone;
    }

    public String getZone() {
        return zone;
    }

    public void setPricePercentFormula(String pricePercentFormula) {
        this.pricePercentFormula = pricePercentFormula;
    }

    public String getPricePercentFormula() {
        return pricePercentFormula;
    }

    public void setCoins(String coins) {
        this.coins = coins;
    }

    public String getCoins() {
        return coins;
    }

    public void setBuys(ArrayList<String> buys) {
        this.buys = buys;
    }

    public void setBuysString(String buys) {
        setBuys(Utils.getArray(buys));
    }

    public ArrayList<String> getBuys() {
        return buys;
    }

    public ArrayList<CaravanItemData> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<CaravanItemData> itemList) {
        this.itemList = itemList;
    }

    public void setSpawnChancePercentage(int spawnChancePercentage) {
        this.spawnChancePercentage = spawnChancePercentage;
    }

    public void setComePCT(String spawnChancePercent) throws Exception {
        if (spawnChancePercent == null || spawnChancePercent.trim().length() == 0) {
            setSpawnChancePercentage(100);
        } else {
            boolean isPercentChanceInvalid = false;
            try {
                int spawnPercentage = Integer.parseInt(spawnChancePercent);
                if (spawnPercentage <= 0) {
                    isPercentChanceInvalid = true;
                } else {
                    if (spawnPercentage > 100) {
                        spawnPercentage = 100;
                    }
                    setSpawnChancePercentage(spawnPercentage);
                }
            } catch (NumberFormatException e) {
                isPercentChanceInvalid = true;
            }
            if (isPercentChanceInvalid) {
                // message: "Bad caravan come PCT [" + spawnChancePercent + "]"
                throw new Exception(Messages.getString("CaravanManagerItem.0") + spawnChancePercent + "]");
            }
        }
    }

    public int getSpawnChancePercentage() {
        return spawnChancePercentage;
    }
}
