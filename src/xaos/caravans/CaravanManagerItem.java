package xaos.caravans;

import java.util.ArrayList;

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
    private String pricePCT;
    private String coins;
    private ArrayList<String> buys;
    private ArrayList<CaravanItemData> itemList;
    private int comePCT;

    public CaravanData getInstance(int livingID, int x, int y, int z) {
        CaravanData caravanData = new CaravanData();

        // Items
        ArrayList<CaravanItemDataInstance> alItems = new ArrayList<CaravanItemDataInstance>();
        CaravanItemData cid;
        int iQtty;
        for (int i = 0; i < itemList.size(); i++) {
            cid = itemList.get(i);
            if (Utils.getRandomBetween(1, 100) <= cid.getPCT()) {
                // Hit, miramos la cantidad
                iQtty = Utils.launchDice(cid.getQuantity());
                for (int q = 0; q < iQtty; q++) {
                    String itemID;
                    if (cid.getId() != null) {
                        itemID = cid.getId();
                    } else {
                        itemID = ItemManager.getRandomItemByType(cid.getType()).getIniHeader();
                    }

                    // Que el item exista (debería)
                    if (itemID == null) {
                        continue;
                    }

                    // Miramos que el precio sea mayor que 0
                    ItemManagerItem imi = ItemManager.getItem(itemID);
                    if (imi == null || imi.getValue() <= 0) {
                        continue;
                    }

                    Item item = Item.createItem(imi);
                    int iPrice = PricesManager.getPrice(item);

					// Miramos que no exista el item, en ese caso sumamos X a la cantidad
                    // Los items militares van sueltos
                    boolean bFound = false;
                    if (!(item instanceof MilitaryItem)) {
                        // Iten NO militar
                        for (int current = 0; current < alItems.size(); current++) {
                            if (iPrice == alItems.get(current).getPrice() && alItems.get(current).getItem().getIniHeader().equals(itemID)) {
                                // Bingo!
                                alItems.get(current).setQuantity(alItems.get(current).getQuantity() + 1);
                                bFound = true;
                                break;
                            }
                        }
                    }

                    if (!bFound) {
                        CaravanItemDataInstance cidi = new CaravanItemDataInstance();
                        cidi.setItem(item);
                        cidi.setPrice(iPrice);
                        cidi.setQuantity(1);
                        alItems.add(cidi);
                    }
                }
            }
        }

        caravanData.setAlItems(alItems);
        caravanData.setStatus(CaravanData.STATUS_COMING);
        caravanData.setLivingId(livingID);
        caravanData.setPricePCT(Utils.launchDice(getPricePCT()));
        caravanData.setCoins(Utils.launchDice(getCoins()));
        caravanData.setStartingPoint(Point3DShort.getPoolInstance(x, y, z));
        caravanData.setTurnsToLeave(World.TIME_MODIFIER_DAY * 3);

        // To buy
        caravanData.setMenuCaravanToBuy(new SmartMenu());

        // To sell
        caravanData.setMenuTownToSell(new SmartMenu());

        return caravanData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setZone(String zone) throws Exception {
        if (zone == null || zone.length() == 0) {
            throw new Exception(Messages.getString("CaravanManagerItem.1")); //$NON-NLS-1$
        }

        if (ZoneManager.getItem(zone) == null) {
            throw new Exception(Messages.getString("CaravanManagerItem.2") + zone + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        this.zone = zone;
    }

    public String getZone() {
        return zone;
    }

    public void setPricePCT(String pricePCT) {
        this.pricePCT = pricePCT;
    }

    public String getPricePCT() {
        return pricePCT;
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

    public void setBuysString(String sBuys) {
        setBuys(Utils.getArray(sBuys));
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

    public void setComePCT(int comePCT) {
        this.comePCT = comePCT;
    }

    public void setComePCT(String sComePCT) throws Exception {
        if (sComePCT == null || sComePCT.trim().length() == 0) {
            setComePCT(100);
        } else {
            boolean bError = false;
            try {
                int iPCT = Integer.parseInt(sComePCT);
                if (iPCT <= 0) {
                    bError = true;
                } else {
                    if (iPCT > 100) {
                        iPCT = 100;
                    }
                    setComePCT(iPCT);
                }
            } catch (Exception e) {
                bError = true;
            }
            if (bError) {
                throw new Exception(Messages.getString("CaravanManagerItem.0") + sComePCT + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    public int getComePCT() {
        return comePCT;
    }
}
