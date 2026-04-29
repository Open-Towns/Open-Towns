package xaos.panels;

import java.awt.Point;
import java.util.ArrayList;

import xaos.platform.lwjgl3.input.Keyboard;

import xaos.caravans.CaravanManager;
import xaos.caravans.CaravanManagerItem;
import xaos.caravans.PricesManager;
import xaos.data.CaravanData;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public class TradePanel {

    public static int MAX_VERTICAL_BUTTONS;

    public static Tile tileTradeButton = new Tile("trade_button"); //$NON-NLS-1$
    public static final boolean tileTradeButtonAlpha[][] = UtilsGL.generateAlpha(tileTradeButton);
    public static Tile tileTradeConfirm = new Tile("trade_confirm"); //$NON-NLS-1$
    public static Tile tileTradeConfirmDisabled = new Tile("trade_confirm_disabled"); //$NON-NLS-1$
    public static final boolean tileTradeConfirmAlpha[][] = UtilsGL.generateAlpha(tileTradeConfirm);
    public static Tile tileTradeBuy = new Tile("trade_buy"); //$NON-NLS-1$
    public static Tile tileTradeSell = new Tile("trade_sell"); //$NON-NLS-1$
    public static Tile tileTradeCaravanCoins = new Tile("trade_ccoins"); //$NON-NLS-1$
    public static Tile tileTradeTownCoins = new Tile("trade_tcoins"); //$NON-NLS-1$
    public static Tile tileTradeCost = new Tile("trade_cost"); //$NON-NLS-1$

    // Caravan column
    private SmartMenu menuCaravan;
    private ArrayList<Point> alButtonPointsCaravan;
    private Point scrollUpCaravanPoint;
    private Point scrollDownCaravanPoint;
    private int indexButtonsCaravan;

    // Caravan to-buy column
    private ArrayList<Point> alButtonPointsCaravanToBuy;
    private Point scrollUpCaravanToBuyPoint;
    private Point scrollDownCaravanToBuyPoint;
    private int indexButtonsToBuyCaravan;

    // Town column
    private SmartMenu menuTown;
    private ArrayList<Point> alButtonPointsTown;
    private Point scrollUpTownPoint;
    private Point scrollDownTownPoint;
    private int indexButtonsTown;

    // Town to-sell column
    private ArrayList<Point> alButtonPointsTownToSell;
    private Point scrollUpTownToSellPoint;
    private Point scrollDownTownToSellPoint;
    private int indexButtonsToSellTown;
    private int priceToSell;
    private String priceToSellString;

    // Icons
    private Point caravanCoinsIconPoint;
    private Point townCoinsIconPoint;
    private Point buyIconPoint;
    private Point sellIconPoint;
    private Point costPoint;

    // Confirm
    private Point confirmPoint;

    // Cost
    private int cost;
    private boolean transactionReady;

    public static void loadStatics() {
    }

    public TradePanel(CaravanData caravanData, Point coordinates, int width, int height) {
        int iCaravanID = caravanData.getLivingId();
        LivingEntity le = World.getLivingEntityByID(iCaravanID);
        if (le == null) {
            return;
        }
        LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
        if (lemi == null || lemi.getCaravan() == null) {
            return;
        }
        CaravanManagerItem cmi = CaravanManager.getItem(lemi.getCaravan());
        if (cmi == null) {
            return;
        }

        // Creamos el menú de la caravan
        createCaravanMenu(caravanData);

        // Creamos la lista de objetos en el pueblo que la caravana acepta para comprar
        createTownMenu(caravanData, cmi, le.getCoordinates());

        resize(caravanData);

        UIPanel.resizeIcons(getMenuCaravan(), UIPanel.TRADE_PANEL_BUTTON_WIDTH, UIPanel.TRADE_PANEL_BUTTON_HEIGHT);
        UIPanel.resizeIcons(getMenuTown(), UIPanel.TRADE_PANEL_BUTTON_WIDTH, UIPanel.TRADE_PANEL_BUTTON_HEIGHT);
    }

    private void createCaravanMenu(CaravanData caravanData) {
        setMenuCaravan(new SmartMenu());
        String sIniHeader;
        Item item;
        ItemManagerItem imi;
        for (int i = 0; i < caravanData.getAlItems().size(); i++) {
            item = caravanData.getAlItems().get(i).getItem();
            sIniHeader = item.getIniHeader();
            imi = ItemManager.getItem(sIniHeader);
            // El directpoint indica: x=precio, y=cantidad restante, z=cantidad marcada para comprar

            int iNumItemsToBuy = getNumItemsToBuy(caravanData, item);
            SmartMenu sm = new SmartMenu(SmartMenu.TYPE_ITEM, (item instanceof MilitaryItem) ? ((MilitaryItem) item).getExtendedTilename() : imi.getName(), null, null, sIniHeader, "x" + (caravanData.getAlItems().get(i).getQuantity() - iNumItemsToBuy), new Point3D((caravanData.getAlItems().get(i).getPrice() * caravanData.getPricePCT()) / 100, caravanData.getAlItems().get(i).getQuantity() - iNumItemsToBuy, (item instanceof MilitaryItem) ? item.getID() : -1)); //$NON-NLS-1$
            sm.setIcon(sIniHeader);
            sm.setIconType(SmartMenu.ICON_TYPE_ITEM);
            getMenuCaravan().addItem(sm);
        }
    }

    public void createTownMenu() {
        CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
        if (caravanData == null || caravanData.getStatus() != CaravanData.STATUS_IN_PLACE) {
            return;
        }

        int iCaravanID = caravanData.getLivingId();
        LivingEntity le = World.getLivingEntityByID(iCaravanID);
        if (le == null) {
            return;
        }
        LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
        if (lemi == null || lemi.getCaravan() == null) {
            return;
        }
        CaravanManagerItem cmi = CaravanManager.getItem(lemi.getCaravan());
        if (cmi == null) {
            return;
        }

        createTownMenu(caravanData, cmi, le.getCoordinates());
    }

    private void createTownMenu(CaravanData caravanData, CaravanManagerItem cmi, Point3DShort p3dCaravanCoordinates) {
        setMenuTown(new SmartMenu());

        if (p3dCaravanCoordinates == null) {
            return;
        }
        int iASZID = World.getCell(p3dCaravanCoordinates).getAstarZoneID();

        ItemManagerItem imi;
        ArrayList<String> alBuys = cmi.getBuys();
        if (alBuys != null && alBuys.size() > 0) {
            // Items del mundo
            Integer[] aItems = World.getItems().keySet().toArray(new Integer[0]);
            Item oItem;
            ArrayList<String> alIniHeaders = new ArrayList<>();
            ArrayList<Integer> alQtty = new ArrayList<>();
            for (int i = (aItems.length - 1); i >= 0; i--) {
                oItem = World.getItems().get(aItems[i]);
                if (oItem != null && !oItem.isLocked()) {
                    if (World.getCell(oItem.getCoordinates()).getAstarZoneID() == iASZID) {
                        imi = ItemManager.getItem(oItem.getIniHeader());
                        if (imi.getType() != null && alBuys.contains(imi.getType())) {
                            if (oItem instanceof MilitaryItem) {
                                int iNumItemsToSell = getNumItemsToSell(caravanData, oItem);
                                int iNumItemsTownMenu = 1 - iNumItemsToSell;
                                // Si el número de items en el menú de town da negativo, restamos items del menoToSell
                                if (iNumItemsTownMenu < 0) {
                                    iNumItemsToSell = (iNumItemsToSell + iNumItemsTownMenu);
                                    if (setItemsToSellQtty(caravanData, oItem, iNumItemsToSell)) {
                                        // Item borrado, ponemos el máximo al menú de town y resizeamos
                                        iNumItemsTownMenu = 1;
                                    } else {
                                        iNumItemsTownMenu = 0;
                                    }
                                }

                                SmartMenu sm = new SmartMenu(SmartMenu.TYPE_ITEM, ((MilitaryItem) oItem).getExtendedTilename(), null, null, imi.getIniHeader(), "x" + iNumItemsTownMenu, new Point3D(PricesManager.getPrice(oItem), iNumItemsTownMenu, oItem.getID()));
                                sm.setIcon(imi.getIniHeader());
                                sm.setIconType(SmartMenu.ICON_TYPE_ITEM);
                                getMenuTown().addItem(sm);
                            } else {
                                int iIndex = alIniHeaders.indexOf(oItem.getIniHeader());
                                if (iIndex == -1) {
                                    alIniHeaders.add(oItem.getIniHeader());
                                    alQtty.add(1);
                                } else {
                                    alQtty.set(iIndex, alQtty.get(iIndex) + 1);
                                }
                            }
                        }
                    }
                }
            }

            // Items em containers
            ArrayList<Container> alContainers = Game.getWorld().getContainers();
            ArrayList<Item> alContainerItems;
            Item item;
            nextContainer:
            for (int i = 0; i < alContainers.size(); i++) {
                item = Item.getItemByID(alContainers.get(i).getItemID());
                if (item == null || World.getCell(item.getCoordinates()).getAstarZoneID() != iASZID) {
                    continue nextContainer;
                }

                // Container en la zona
                alContainerItems = alContainers.get(i).getItemsInside();

                for (int j = 0; j < alContainerItems.size(); j++) {
                    oItem = alContainerItems.get(j);
                    imi = ItemManager.getItem(oItem.getIniHeader());
                    if (imi.getType() != null && alBuys.contains(imi.getType())) {
                        if (oItem instanceof MilitaryItem) {
                            int iNumItemsToSell = getNumItemsToSell(caravanData, oItem);
                            int iNumItemsTownMenu = 1 - iNumItemsToSell;
                            // Si el número de items en el menú de town da negativo, restamos items del menoToSell
                            if (iNumItemsTownMenu < 0) {
                                iNumItemsToSell = (iNumItemsToSell + iNumItemsTownMenu);
                                if (setItemsToSellQtty(caravanData, oItem, iNumItemsToSell)) {
                                    // Item borrado, ponemos el máximo al menú de town y resizeamos
                                    iNumItemsTownMenu = 1;
                                } else {
                                    iNumItemsTownMenu = 0;
                                }
                            }

                            SmartMenu sm = new SmartMenu(SmartMenu.TYPE_ITEM, ((MilitaryItem) oItem).getExtendedTilename(), null, null, imi.getIniHeader(), "x" + iNumItemsTownMenu, new Point3D(PricesManager.getPrice(oItem), iNumItemsTownMenu, oItem.getID()));
                            sm.setIcon(imi.getIniHeader());
                            sm.setIconType(SmartMenu.ICON_TYPE_ITEM);
                            getMenuTown().addItem(sm);
                        } else {
                            int iIndex = alIniHeaders.indexOf(oItem.getIniHeader());
                            if (iIndex == -1) {
                                alIniHeaders.add(oItem.getIniHeader());
                                alQtty.add(1);
                            } else {
                                alQtty.set(iIndex, alQtty.get(iIndex) + 1);
                            }
                        }
                    }
                }
            }

            for (int m = 0; m < alIniHeaders.size(); m++) {
                imi = ItemManager.getItem(alIniHeaders.get(m));
                int iNumItemsToSell = getNumItemsToSell(caravanData, imi.getIniHeader());
                int iNumItemsTownMenu = alQtty.get(m) - iNumItemsToSell;
                // Si el número de items en el menú de town da negativo, restamos items del menoToSell
                if (iNumItemsTownMenu < 0) {
                    iNumItemsToSell = (iNumItemsToSell + iNumItemsTownMenu);
                    if (setItemsToSellQtty(caravanData, imi.getIniHeader(), iNumItemsToSell)) {
                        // Item borrado, ponemos el máximo al menú de town y resizeamos
                        iNumItemsTownMenu = alQtty.get(m);
                    } else {
                        iNumItemsTownMenu = 0;
                    }
                }

                if (alQtty.get(m) > 0) {
                    SmartMenu sm = new SmartMenu(SmartMenu.TYPE_ITEM, imi.getName(), null, null, imi.getIniHeader(), "x" + iNumItemsTownMenu, new Point3D(imi.getValue(), iNumItemsTownMenu, -1));
                    sm.setIcon(imi.getIniHeader());
                    sm.setIconType(SmartMenu.ICON_TYPE_ITEM);
                    getMenuTown().addItem(sm);
                }
            }
        }

        // Repasamos toda la lista de to-sell para ver si todos los items que hay ahí existen en la lista de town
        checkToSellList(caravanData);

        resize(caravanData);
    }

    /**
     * Mira toda la lista de to-sell para ver si hay items en el menu de town
     * por cada uno
     *
     * @param caravanData
     */
    private void checkToSellList(CaravanData caravanData) {
        if (caravanData == null || caravanData.getMenuTownToSell() == null || getMenuTown() == null) {
            return;
        }

        SmartMenu smToSell, smTown;
        boolean bExists;
        for (int i = caravanData.getMenuTownToSell().getItems().size() - 1; i >= 0; i--) {
            smToSell = caravanData.getMenuTownToSell().getItems().get(i);

            bExists = false;
            for (int j = 0; j < getMenuTown().getItems().size(); j++) {
                smTown = getMenuTown().getItems().get(j);
                if (smTown.getDirectCoordinates().z != -1) {
                    // Item militar
                    if (smTown.getDirectCoordinates().z == smToSell.getDirectCoordinates().z) {
                        bExists = true;
                        break;
                    }
                } else {
                    // Item genérico
                    if (smTown.getDirectCoordinates().x == smToSell.getDirectCoordinates().x && smTown.getParameter().equals(smToSell.getParameter())) {
                        bExists = true;
                        break;
                    }
                }
            }

            if (!bExists) {
                caravanData.getMenuTownToSell().getItems().remove(i);
            }
        }
    }

    private int getNumItemsToBuy(CaravanData caravanData, Item item) {
        if (caravanData == null || caravanData.getMenuCaravanToBuy() == null) {
            return 0;
        }

        SmartMenu smItem;
        boolean bMilitary = item instanceof MilitaryItem;
        for (int i = 0; i < caravanData.getMenuCaravanToBuy().getItems().size(); i++) {
            smItem = caravanData.getMenuCaravanToBuy().getItems().get(i);
            if (bMilitary) {
                if (smItem.getDirectCoordinates().z == item.getID()) {
                    return smItem.getDirectCoordinates().y;
                }
            } else {
                if (smItem.getDirectCoordinates().x == ((PricesManager.getPrice(item) * caravanData.getPricePCT()) / 100) && smItem.getParameter().equals(item.getIniHeader())) {
                    return smItem.getDirectCoordinates().y;
                }
            }
        }

        return 0;
    }

    private int getNumItemsToSell(CaravanData caravanData, Item item) {
        if (caravanData == null || caravanData.getMenuTownToSell() == null) {
            return 0;
        }

        SmartMenu smItem;
        boolean bMilitary = item instanceof MilitaryItem;
        for (int i = 0; i < caravanData.getMenuTownToSell().getItems().size(); i++) {
            smItem = caravanData.getMenuTownToSell().getItems().get(i);
            if (bMilitary) {
                if (smItem.getDirectCoordinates().z == item.getID()) {
                    return smItem.getDirectCoordinates().y;
                }
            } else {
                if (smItem.getDirectCoordinates().x == PricesManager.getPrice(item) && smItem.getParameter().equals(item.getIniHeader())) {
                    return smItem.getDirectCoordinates().y;
                }
            }
        }

        return 0;
    }

    private int getNumItemsToSell(CaravanData caravanData, String sIniHeader) {
        if (caravanData == null || caravanData.getMenuTownToSell() == null) {
            return 0;
        }

        SmartMenu smItem;
        for (int i = 0; i < caravanData.getMenuTownToSell().getItems().size(); i++) {
            smItem = caravanData.getMenuTownToSell().getItems().get(i);
            if (smItem.getDirectCoordinates().x == PricesManager.getPrice(sIniHeader) && smItem.getParameter().equals(sIniHeader)) {
                return smItem.getDirectCoordinates().y;
            }
        }

        return 0;
    }

    /**
     * Cambia el valor de un item en la lista de to-sell. Si el valor llega a 0
     * (o menos ¿?) el item se borra y se devuelve true
     *
     * @param caravanData
     * @param sIniHeader
     * @param iQtty
     * @return
     */
    private boolean setItemsToSellQtty(CaravanData caravanData, String sIniHeader, int iQtty) {
        if (caravanData == null || caravanData.getMenuTownToSell() == null) {
            return true;
        }

        SmartMenu smItem;
        Point3D p3dInfo;
        for (int i = 0; i < caravanData.getMenuTownToSell().getItems().size(); i++) {
            smItem = caravanData.getMenuTownToSell().getItems().get(i);
            p3dInfo = smItem.getDirectCoordinates();
            if (p3dInfo.x == PricesManager.getPrice(sIniHeader) && smItem.getParameter().equals(sIniHeader)) {
                if (iQtty == 0) {
                    caravanData.getMenuTownToSell().getItems().remove(i);
                    return true;
                } else {
                    p3dInfo.y = iQtty;
                    smItem.setDirectCoordinates(p3dInfo);
                    smItem.setParameter2("x" + iQtty);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Cambia el valor de un item en la lista de to-sell. Si el valor llega a 0
     * (o menos ¿?) el item se borra y se devuelve true
     *
     * @param caravanData
     * @param sIniHeader
     * @param iQtty
     * @return
     */
    private boolean setItemsToSellQtty(CaravanData caravanData, Item oITem, int iQtty) {
        if (caravanData == null || caravanData.getMenuTownToSell() == null) {
            return true;
        }

        SmartMenu smItem;
        Point3D p3dInfo;
        for (int i = 0; i < caravanData.getMenuTownToSell().getItems().size(); i++) {
            smItem = caravanData.getMenuTownToSell().getItems().get(i);
            p3dInfo = smItem.getDirectCoordinates();
            if (p3dInfo.x == PricesManager.getPrice(oITem) && smItem.getParameter().equals(oITem.getIniHeader())) {
                if (iQtty == 0) {
                    caravanData.getMenuTownToSell().getItems().remove(i);
                    return true;
                } else {
                    p3dInfo.y = iQtty;
                    smItem.setDirectCoordinates(p3dInfo);
                    smItem.setParameter2("x" + iQtty);
                    return false;
                }
            }
        }

        return true;
    }

    public SmartMenu getMenuCaravan() {
        return menuCaravan;
    }

    public void setMenuCaravan(SmartMenu menuCaravan) {
        this.menuCaravan = menuCaravan;
    }

    public ArrayList<Point> getAlButtonPointsCaravan() {
        return alButtonPointsCaravan;
    }

    public void setAlButtonPointsCaravan(ArrayList<Point> alButtonPointsCaravan) {
        this.alButtonPointsCaravan = alButtonPointsCaravan;
    }

    public Point getScrollDownCaravanPoint() {
        return scrollDownCaravanPoint;
    }

    public void setScrollDownCaravanPoint(Point scrollDownCaravanPoint) {
        this.scrollDownCaravanPoint = scrollDownCaravanPoint;
    }

    public Point getScrollUpCaravanPoint() {
        return scrollUpCaravanPoint;
    }

    public void setScrollUpCaravanPoint(Point scrollUpCaravanPoint) {
        this.scrollUpCaravanPoint = scrollUpCaravanPoint;
    }

    public int getIndexButtonsCaravan() {
        return indexButtonsCaravan;
    }

    public void setIndexButtonsCaravan(int indexButtonsCaravan) {
        this.indexButtonsCaravan = indexButtonsCaravan;
    }

    public void setAlButtonPointsCaravanToBuy(ArrayList<Point> alButtonPointsCaravanToBuy) {
        this.alButtonPointsCaravanToBuy = alButtonPointsCaravanToBuy;
    }

    public ArrayList<Point> getAlButtonPointsCaravanToBuy() {
        return alButtonPointsCaravanToBuy;
    }

    public void setScrollUpCaravanToBuyPoint(Point scrollUpCaravanToBuyPoint) {
        this.scrollUpCaravanToBuyPoint = scrollUpCaravanToBuyPoint;
    }

    public Point getScrollUpCaravanToBuyPoint() {
        return scrollUpCaravanToBuyPoint;
    }

    public void setScrollDownCaravanToBuyPoint(Point scrollDownCaravanToBuyPoint) {
        this.scrollDownCaravanToBuyPoint = scrollDownCaravanToBuyPoint;
    }

    public Point getScrollDownCaravanToBuyPoint() {
        return scrollDownCaravanToBuyPoint;
    }

    public void setIndexButtonsToBuyCaravan(int indexButtonsToBuyCaravan) {
        this.indexButtonsToBuyCaravan = indexButtonsToBuyCaravan;
    }

    public int getIndexButtonsToBuyCaravan() {
        return indexButtonsToBuyCaravan;
    }

    public SmartMenu getMenuTown() {
        return menuTown;
    }

    public void setMenuTown(SmartMenu menuTown) {
        this.menuTown = menuTown;
    }

    public ArrayList<Point> getAlButtonPointsTown() {
        return alButtonPointsTown;
    }

    public void setAlButtonPointsTown(ArrayList<Point> alButtonPointsTown) {
        this.alButtonPointsTown = alButtonPointsTown;
    }

    public Point getScrollUpTownPoint() {
        return scrollUpTownPoint;
    }

    public void setScrollUpTownPoint(Point scrollUpTownPoint) {
        this.scrollUpTownPoint = scrollUpTownPoint;
    }

    public Point getScrollDownTownPoint() {
        return scrollDownTownPoint;
    }

    public void setScrollDownTownPoint(Point scrollDownTownPoint) {
        this.scrollDownTownPoint = scrollDownTownPoint;
    }

    public int getIndexButtonsTown() {
        return indexButtonsTown;
    }

    public void setIndexButtonsTown(int indexButtonsTown) {
        this.indexButtonsTown = indexButtonsTown;
    }

    public ArrayList<Point> getAlButtonPointsTownToSell() {
        return alButtonPointsTownToSell;
    }

    public void setAlButtonPointsTownToSell(ArrayList<Point> alButtonPointsTownToSell) {
        this.alButtonPointsTownToSell = alButtonPointsTownToSell;
    }

    public Point getScrollUpTownToSellPoint() {
        return scrollUpTownToSellPoint;
    }

    public void setScrollUpTownToSellPoint(Point scrollUpTownToSellPoint) {
        this.scrollUpTownToSellPoint = scrollUpTownToSellPoint;
    }

    public Point getScrollDownTownToSellPoint() {
        return scrollDownTownToSellPoint;
    }

    public void setScrollDownTownToSellPoint(Point scrollDownTownToSellPoint) {
        this.scrollDownTownToSellPoint = scrollDownTownToSellPoint;
    }

    public int getIndexButtonsToSellTown() {
        return indexButtonsToSellTown;
    }

    public void setIndexButtonsToSellTown(int indexButtonsToSellTown) {
        this.indexButtonsToSellTown = indexButtonsToSellTown;
    }

    public int getPriceToSell() {
        return priceToSell;
    }

    public void setPriceToSell(int priceToSell) {
        this.priceToSell = priceToSell;
    }

    public String getPriceToSellString() {
        return priceToSellString;
    }

    public void setPriceToSellString(String priceToSellString) {
        this.priceToSellString = priceToSellString;
    }

    public Point getCaravanCoinsIconPoint() {
        return caravanCoinsIconPoint;
    }

    public void setCaravanCoinsIconPoint(Point caravanCoinsIconPoint) {
        this.caravanCoinsIconPoint = caravanCoinsIconPoint;
    }

    public Point getTownCoinsIconPoint() {
        return townCoinsIconPoint;
    }

    public void setTownCoinsIconPoint(Point townCoinsIconPoint) {
        this.townCoinsIconPoint = townCoinsIconPoint;
    }

    public Point getBuyIconPoint() {
        return buyIconPoint;
    }

    public void setBuyIconPoint(Point buyIconPoint) {
        this.buyIconPoint = buyIconPoint;
    }

    public Point getSellIconPoint() {
        return sellIconPoint;
    }

    public void setSellIconPoint(Point sellIconPoint) {
        this.sellIconPoint = sellIconPoint;
    }

    public void setCostPoint(Point costPoint) {
        this.costPoint = costPoint;
    }

    public Point getCostPoint() {
        return costPoint;
    }

    public void setConfirmPoint(Point confirmPoint) {
        this.confirmPoint = confirmPoint;
    }

    public Point getConfirmPoint() {
        return confirmPoint;
    }

    public void setCost(CaravanData caravanData, int cost) {
        this.cost = cost;

        // Miramos si la transacción está ready
        if (caravanData == null) {
            return;
        }

        int iWorldCoins = Game.getWorld().getCoins();
        int iCostoins = cost;
        if (caravanData.getMenuCaravanToBuy().getItems().size() > 0 || caravanData.getMenuTownToSell().getItems().size() > 0) {
            // Hay items en las listas, miramos si el pueblo puede permitirse el gasto
            if (iCostoins > 0 && iCostoins > iWorldCoins) {
                // Compra, miramos si el pueblo tiene suficiente pasta
                setTransactionReady(false);
            } else if (iCostoins < 0 && Math.abs(iCostoins) > caravanData.getCoins()) {
                // Venta, miramos si la caravana tiene suficiente pasta
                setTransactionReady(false);
            } else {
                // Compra/venta justa o el pueblo/caravana tiene pasta suficiente
                setTransactionReady(true);
            }
        } else {
            setTransactionReady(false);
        }
    }

    public int getCost() {
        return cost;
    }

    public void setTransactionReady(boolean transactionReady) {
        this.transactionReady = transactionReady;
    }

    public boolean isTransactionReady() {
        return transactionReady;
    }

    public void resize(CaravanData caravanData) {
        Point coordinates = UIPanel.tradePanelPoint;
        int COLUMN1X = coordinates.x + 48;
        int COLUMN4X = coordinates.x + UIPanel.TRADE_PANEL_WIDTH - 48 - tileTradeButton.getTileWidth();
        int iAux = (COLUMN4X - COLUMN1X) / 4;
        int COLUMN2X = COLUMN1X + iAux - tileTradeButton.getTileWidth() - 16;
        int COLUMN3X = COLUMN4X - iAux - tileTradeButton.getTileWidth() / 2 - 16;

        // Icons
        setCaravanCoinsIconPoint(new Point(COLUMN1X + tileTradeButton.getTileWidth() / 2 - tileTradeCaravanCoins.getTileWidth() / 2, coordinates.y + 12));
        setTownCoinsIconPoint(new Point(COLUMN4X + tileTradeButton.getTileWidth() / 2 - tileTradeTownCoins.getTileWidth() / 2, coordinates.y + 12));
        setBuyIconPoint(new Point(COLUMN2X + tileTradeButton.getTileWidth() / 2 - tileTradeBuy.getTileWidth() / 2 + (tileTradeButton.getTileWidth() * 2), coordinates.y + 48));
        setSellIconPoint(new Point(COLUMN3X + tileTradeButton.getTileWidth() / 2 - tileTradeSell.getTileWidth() / 2, coordinates.y + 48));
        setCostPoint(new Point(coordinates.x + UIPanel.TRADE_PANEL_WIDTH / 2 - tileTradeCost.getTileWidth() / 2, coordinates.y + 12));

        int iYMin = getCaravanCoinsIconPoint().y + tileTradeCaravanCoins.getTileHeight() + UtilFont.MAX_HEIGHT * 2;
        int iYMax = coordinates.y + UIPanel.TRADE_PANEL_HEIGHT - 48 - UIPanel.tileScrollDown.getTileHeight() - 16 - UtilFont.MAX_HEIGHT * 2;
        MAX_VERTICAL_BUTTONS = (iYMax - iYMin) / (tileTradeButton.getTileHeight() + 16);

		// Caravan buttons
        // Index, miramos si el actual es válido con el nuevo size
        if (getIndexButtonsCaravan() + MAX_VERTICAL_BUTTONS > getMenuCaravan().getItems().size()) {
            setIndexButtonsCaravan(0);
        }

        setScrollUpCaravanPoint(new Point(COLUMN1X + tileTradeButton.getTileWidth() / 2 - UIPanel.tileScrollUp.getTileWidth() / 2, iYMin));
        iYMin += UIPanel.tileScrollUp.getTileHeight() + 16; // Tamaño del primer scroll + espacio en blanco (16)

        int iMaxButtons = MAX_VERTICAL_BUTTONS;
        if (iMaxButtons > caravanData.getAlItems().size()) {
            iMaxButtons = caravanData.getAlItems().size();
        }

        ArrayList<Point> alButtonPoints = new ArrayList<>(iMaxButtons);
        for (int i = 0; i < iMaxButtons; i++) {
            alButtonPoints.add(new Point(COLUMN1X, iYMin));
            iYMin += (tileTradeButton.getTileHeight() + 16);
        }
        setAlButtonPointsCaravan(alButtonPoints);

        setScrollDownCaravanPoint(new Point(COLUMN1X + tileTradeButton.getTileWidth() / 2 - UIPanel.tileScrollDown.getTileWidth() / 2, iYMin));

        // Caravan buttons to-buy
        iYMin = getCaravanCoinsIconPoint().y + tileTradeCaravanCoins.getTileHeight() + UtilFont.MAX_HEIGHT * 2;
        // Index, miramos si el actual es válido con el nuevo size
        if (getIndexButtonsToBuyCaravan() + MAX_VERTICAL_BUTTONS > caravanData.getMenuCaravanToBuy().getItems().size()) {
            setIndexButtonsToBuyCaravan(0);
        }

        setScrollUpCaravanToBuyPoint(new Point(COLUMN2X + tileTradeButton.getTileWidth() / 2 - UIPanel.tileScrollUp.getTileWidth() / 2 + (tileTradeButton.getTileWidth() * 2), iYMin));
        iYMin += UIPanel.tileScrollUp.getTileHeight() + 16; // Tamaño del primer scroll + espacio en blanco (16)

        iMaxButtons = MAX_VERTICAL_BUTTONS;
        if (iMaxButtons > caravanData.getMenuCaravanToBuy().getItems().size()) {
            iMaxButtons = caravanData.getMenuCaravanToBuy().getItems().size();
        }
        ArrayList<Point> alButtonPointsToBuy = new ArrayList<>(iMaxButtons);
        for (int i = 0; i < iMaxButtons; i++) {
            alButtonPointsToBuy.add(new Point(COLUMN2X + (tileTradeButton.getTileWidth() * 2), iYMin));
            iYMin += (tileTradeButton.getTileHeight() + 16);
        }
        setAlButtonPointsCaravanToBuy(alButtonPointsToBuy);

        setScrollDownCaravanToBuyPoint(new Point(COLUMN2X + tileTradeButton.getTileWidth() / 2 - UIPanel.tileScrollDown.getTileWidth() / 2 + (tileTradeButton.getTileWidth() * 2), iYMin));

        // Confirm
        setConfirmPoint(new Point(coordinates.x + UIPanel.TRADE_PANEL_WIDTH / 2 - tileTradeConfirm.getTileWidth() / 2, coordinates.y + UIPanel.TRADE_PANEL_HEIGHT - tileTradeConfirm.getTileHeight() - 8));

        // Price
        recheckPriceToBuySell(caravanData);

		// Trade town buttons
        // Index, miramos si el actual es válido con el nuevo size
        if (getIndexButtonsTown() + MAX_VERTICAL_BUTTONS > getMenuTown().getItems().size()) {
            setIndexButtonsTown(0);
        }

        iYMin = getCaravanCoinsIconPoint().y + tileTradeCaravanCoins.getTileHeight() + UtilFont.MAX_HEIGHT * 2;
        setScrollUpTownPoint(new Point(COLUMN4X + tileTradeButton.getTileWidth() / 2 - UIPanel.tileScrollUp.getTileWidth() / 2, iYMin));
        iYMin += UIPanel.tileScrollUp.getTileHeight() + 16; // Tamaño del primer scroll + espacio en blanco (16)
        iMaxButtons = MAX_VERTICAL_BUTTONS;
        if (iMaxButtons > getMenuTown().getItems().size()) {
            iMaxButtons = getMenuTown().getItems().size();
        }

        ArrayList<Point> alButtonPointsTown = new ArrayList<>(iMaxButtons);
        for (int i = 0; i < iMaxButtons; i++) {
            alButtonPointsTown.add(new Point(COLUMN4X, iYMin));
            iYMin += (tileTradeButton.getTileHeight() + 16);
        }
        setAlButtonPointsTown(alButtonPointsTown);

        setScrollDownTownPoint(new Point(COLUMN4X + tileTradeButton.getTileWidth() / 2 - UIPanel.tileScrollDown.getTileWidth() / 2, iYMin));

        // Town buttons to-sell
        iYMin = getCaravanCoinsIconPoint().y + tileTradeCaravanCoins.getTileHeight() + UtilFont.MAX_HEIGHT * 2;
        // Index, miramos si el actual es válido con el nuevo size
        if (getIndexButtonsToSellTown() + MAX_VERTICAL_BUTTONS > caravanData.getMenuTownToSell().getItems().size()) {
            setIndexButtonsToSellTown(0);
        }

        setScrollUpTownToSellPoint(new Point(COLUMN3X + tileTradeButton.getTileWidth() / 2 - UIPanel.tileScrollUp.getTileWidth() / 2, iYMin));
        iYMin += UIPanel.tileScrollUp.getTileHeight() + 16; // Tamaño del primer scroll + espacio en blanco (16)

        iMaxButtons = MAX_VERTICAL_BUTTONS;
        if (iMaxButtons > caravanData.getMenuTownToSell().getItems().size()) {
            iMaxButtons = caravanData.getMenuTownToSell().getItems().size();
        }
        ArrayList<Point> alButtonPointsToSell = new ArrayList<>(iMaxButtons);
        for (int i = 0; i < iMaxButtons; i++) {
            alButtonPointsToSell.add(new Point(COLUMN3X, iYMin));
            iYMin += (tileTradeButton.getTileHeight() + 16);
        }
        setAlButtonPointsTownToSell(alButtonPointsToSell);

        setScrollDownTownToSellPoint(new Point(COLUMN3X + tileTradeButton.getTileWidth() / 2 - UIPanel.tileScrollDown.getTileWidth() / 2, iYMin));
    }

    public void scrollUpCaravan() {
        if (getIndexButtonsCaravan() > 0) {
            setIndexButtonsCaravan(getIndexButtonsCaravan() - 1);
        }
    }

    public void scrollDownCaravan() {
        if (getIndexButtonsCaravan() + MAX_VERTICAL_BUTTONS < getMenuCaravan().getItems().size()) {
            setIndexButtonsCaravan(getIndexButtonsCaravan() + 1);
        }
    }

    public void scrollUpToBuyCaravan() {
        if (getIndexButtonsToBuyCaravan() > 0) {
            setIndexButtonsToBuyCaravan(getIndexButtonsToBuyCaravan() - 1);
        }
    }

    public void scrollDownToBuyCaravan() {
        CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
        if (caravanData != null && getIndexButtonsToBuyCaravan() + MAX_VERTICAL_BUTTONS < caravanData.getMenuCaravanToBuy().getItems().size()) {
            setIndexButtonsToBuyCaravan(getIndexButtonsToBuyCaravan() + 1);
        }
    }

    public void scrollUpToSellTown() {
        if (getIndexButtonsToSellTown() > 0) {
            setIndexButtonsToSellTown(getIndexButtonsToSellTown() - 1);
        }
    }

    public void scrollDownToSellTown() {
        CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
        if (caravanData != null && getIndexButtonsToSellTown() + MAX_VERTICAL_BUTTONS < caravanData.getMenuTownToSell().getItems().size()) {
            setIndexButtonsToSellTown(getIndexButtonsToSellTown() + 1);
        }
    }

    public void scrollUpTown() {
        if (getIndexButtonsTown() > 0) {
            setIndexButtonsTown(getIndexButtonsTown() - 1);
        }
    }

    public void scrollDownTown() {
        if (getIndexButtonsTown() + MAX_VERTICAL_BUTTONS < getMenuTown().getItems().size()) {
            setIndexButtonsTown(getIndexButtonsTown() + 1);
        }
    }

    public void selectItemToBuy(int iIndex) {
        CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
        if (caravanData == null || getMenuCaravan().getItems().size() <= iIndex) {
            return;
        }

		// Seleccionamos un item para comprar
        // Cambiamos el menu de botones de la caravana
        SmartMenu menuItem = getMenuCaravan().getItems().get(iIndex);
        Point3D p3dFlags = menuItem.getDirectCoordinates();
        if (p3dFlags.y > 0) {
            boolean bControl = p3dFlags.y > 99 && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
            boolean bShift = p3dFlags.y > 9 && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));

            if (bControl) {
                p3dFlags.y = p3dFlags.y - 100;
            } else if (bShift) {
                p3dFlags.y = p3dFlags.y - 10;
            } else {
                p3dFlags.y = p3dFlags.y - 1;
            }

            menuItem.setDirectCoordinates(p3dFlags);
            menuItem.setParameter2("x" + p3dFlags.y); //$NON-NLS-1$

			// Añadimos 1 item al menú de items a comprar
            // Primero miramos si existe
            int iFoundIndex = -1;
            SmartMenu menuAux;
            for (int i = 0; i < caravanData.getMenuCaravanToBuy().getItems().size(); i++) {
                menuAux = caravanData.getMenuCaravanToBuy().getItems().get(i);
                if (menuAux.getDirectCoordinates().z != -1) {
                    // Item militar
                    if (menuAux.getDirectCoordinates().z == menuItem.getDirectCoordinates().z) {
                        iFoundIndex = i;
                        break;
                    }
                } else {
                    // Item genérico
                    if (menuAux.getParameter().equals(menuItem.getParameter()) && menuAux.getDirectCoordinates().x == menuItem.getDirectCoordinates().x) {
                        iFoundIndex = i;
                        break;
                    }
                }
            }
            if (iFoundIndex != -1) {
                // Ya existe
                SmartMenu sm = caravanData.getMenuCaravanToBuy().getItems().get(iFoundIndex);
                if (bControl) {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 100;
                } else if (bShift) {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 10;
                } else {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 1;
                }
                sm.setParameter2("x" + sm.getDirectCoordinates().y); //$NON-NLS-1$

                recheckPriceToBuySell(caravanData);
            } else {
                // Nuevo item
                SmartMenu sm = new SmartMenu(SmartMenu.TYPE_ITEM, menuItem.getName(), null, null, menuItem.getParameter(), (bControl ? "x100" : (bShift ? "x10" : "x1")), new Point3D(p3dFlags.x, (bControl ? 100 : (bShift ? 10 : 1)), p3dFlags.z)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                sm.setIcon(menuItem.getIcon().getIniHeader());
                sm.setIconType(menuItem.getIconType());
                caravanData.getMenuCaravanToBuy().addItem(sm);
                UIPanel.resizeIcons(caravanData.getMenuCaravanToBuy(), UIPanel.TRADE_PANEL_BUTTON_WIDTH, UIPanel.TRADE_PANEL_BUTTON_HEIGHT);

                scrollDownToBuyCaravan();

                resize(caravanData); // Esto ya hace un recheck del coste
            }
        }
    }

    public void selectItemToNonBuy(int iIndex) {
        CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
        if (caravanData == null || caravanData.getMenuCaravanToBuy().getItems().size() <= iIndex) {
            return;
        }

        // Cambiamos el menu de botones de buy de la caravana
        SmartMenu menuBuy = caravanData.getMenuCaravanToBuy().getItems().get(iIndex);
        Point3D p3dFlags = menuBuy.getDirectCoordinates();
        if (p3dFlags.y > 0) {
            boolean bControl = p3dFlags.y > 99 && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
            boolean bShift = p3dFlags.y > 9 && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));

            if (bControl) {
                p3dFlags.y = p3dFlags.y - 100;
            } else if (bShift) {
                p3dFlags.y = p3dFlags.y - 10;
            } else {
                p3dFlags.y = p3dFlags.y - 1;
            }

            if (p3dFlags.y > 0) {
                menuBuy.setDirectCoordinates(p3dFlags);
                menuBuy.setParameter2("x" + p3dFlags.y); //$NON-NLS-1$
            }

			// Sumar 1 a los items de la caravana
			// Añadimos 1 item al menú de items a comprar
            // Primero miramos si existe
            int iFoundIndex = -1;
            SmartMenu menuAux;
            for (int i = 0; i < getMenuCaravan().getItems().size(); i++) {
                menuAux = getMenuCaravan().getItems().get(i);
                if (menuAux.getDirectCoordinates().z != -1) {
                    // Item militar
                    if (menuAux.getDirectCoordinates().z == menuBuy.getDirectCoordinates().z) {
                        iFoundIndex = i;
                        break;
                    }
                } else {
                    // Item genérico
                    if (menuAux.getParameter().equals(menuBuy.getParameter()) && menuAux.getDirectCoordinates().x == menuBuy.getDirectCoordinates().x) {
                        iFoundIndex = i;
                        break;
                    }
                }
            }
            if (iFoundIndex != -1) {
                // Encontrado (lo normal)
                SmartMenu sm = getMenuCaravan().getItems().get(iFoundIndex);
                if (bControl) {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 100;
                } else if (bShift) {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 10;
                } else {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 1;
                }
                sm.setParameter2("x" + sm.getDirectCoordinates().y); //$NON-NLS-1$
            } else {
                // No debería pasar
            }

            if (p3dFlags.y <= 0) {
                // Eliminamos el item de la compra
                caravanData.getMenuCaravanToBuy().getItems().remove(iIndex);
                resize(caravanData);
            } else {
                recheckPriceToBuySell(caravanData);
            }
        }
    }

    public void selectItemToSell(int iIndex) {
        CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
        if (caravanData == null || getMenuTown().getItems().size() <= iIndex) {
            return;
        }

		// Seleccionamos un item para vender
        // Cambiamos el menu de botones del pueblo
        SmartMenu menuItem = getMenuTown().getItems().get(iIndex);
        Point3D p3dFlags = menuItem.getDirectCoordinates();
        if (p3dFlags.y > 0) {
            boolean bControl = p3dFlags.y > 99 && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
            boolean bShift = p3dFlags.y > 9 && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));

            if (bControl) {
                p3dFlags.y = p3dFlags.y - 100;
            } else if (bShift) {
                p3dFlags.y = p3dFlags.y - 10;
            } else {
                p3dFlags.y = p3dFlags.y - 1;
            }

            menuItem.setDirectCoordinates(p3dFlags);
            menuItem.setParameter2("x" + p3dFlags.y); //$NON-NLS-1$

			// Añadimos 1 (o 10) item al menú de items a vender
            // Primero miramos si existe
            int iFoundIndex = -1;
            SmartMenu menuAux;
            boolean bMilitary = menuItem.getDirectCoordinates().z != -1;
            if (bMilitary) {
                for (int i = 0; i < caravanData.getMenuTownToSell().getItems().size(); i++) {
                    menuAux = caravanData.getMenuTownToSell().getItems().get(i);
                    if (menuAux.getDirectCoordinates().z == menuItem.getDirectCoordinates().z) {
                        iFoundIndex = i;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < caravanData.getMenuTownToSell().getItems().size(); i++) {
                    menuAux = caravanData.getMenuTownToSell().getItems().get(i);
                    if (menuAux.getParameter().equals(menuItem.getParameter()) && menuAux.getDirectCoordinates().x == menuItem.getDirectCoordinates().x) {
                        iFoundIndex = i;
                        break;
                    }
                }
            }

            if (iFoundIndex != -1) {
                // Ya existe
                SmartMenu sm = caravanData.getMenuTownToSell().getItems().get(iFoundIndex);
                if (bControl) {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 100;
                } else if (bShift) {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 10;
                } else {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 1;
                }
                sm.setParameter2("x" + sm.getDirectCoordinates().y); //$NON-NLS-1$

                recheckPriceToBuySell(caravanData);
            } else {
                // Nuevo item
                SmartMenu sm = new SmartMenu(SmartMenu.TYPE_ITEM, menuItem.getName(), null, null, menuItem.getParameter(), (bControl ? "x100" : (bShift ? "x10" : "x1")), new Point3D(p3dFlags.x, (bControl ? 100 : (bShift ? 10 : 1)), p3dFlags.z)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                sm.setIcon(menuItem.getIcon().getIniHeader());
                sm.setIconType(menuItem.getIconType());
                caravanData.getMenuTownToSell().addItem(sm);
                UIPanel.resizeIcons(caravanData.getMenuTownToSell(), UIPanel.TRADE_PANEL_BUTTON_WIDTH, UIPanel.TRADE_PANEL_BUTTON_HEIGHT);
                scrollDownToSellTown();

                resize(caravanData);
            }
        }
    }

    public void selectItemToNonSell(int iIndex) {
        CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
        if (caravanData == null || caravanData.getMenuTownToSell().getItems().size() <= iIndex) {
            return;
        }

        // Cambiamos el menu de botones de sell del pueblo
        SmartMenu menuSell = caravanData.getMenuTownToSell().getItems().get(iIndex);
        Point3D p3dFlags = menuSell.getDirectCoordinates();
        if (p3dFlags.y > 0) {
            boolean bControl = p3dFlags.y > 99 && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
            boolean bShift = p3dFlags.y > 9 && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));

            if (bControl) {
                p3dFlags.y = p3dFlags.y - 100;
            } else if (bShift) {
                p3dFlags.y = p3dFlags.y - 10;
            } else {
                p3dFlags.y = p3dFlags.y - 1;
            }

            if (p3dFlags.y > 0) {
                menuSell.setDirectCoordinates(p3dFlags);
                menuSell.setParameter2("x" + p3dFlags.y); //$NON-NLS-1$
            }

			// Sumar 1 a los items del pueblo
			// Añadimos 1 item al menú de items a vender
            // Primero miramos si existe
            int iFoundIndex = -1;
            SmartMenu menuAux;
            boolean bMilitary = menuSell.getDirectCoordinates().z != -1;
            if (bMilitary) {
                for (int i = 0; i < getMenuTown().getItems().size(); i++) {
                    menuAux = getMenuTown().getItems().get(i);
                    if (menuAux.getParameter().equals(menuSell.getParameter()) && menuAux.getDirectCoordinates().z == menuSell.getDirectCoordinates().z) {
                        iFoundIndex = i;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < getMenuTown().getItems().size(); i++) {
                    menuAux = getMenuTown().getItems().get(i);
                    if (menuAux.getParameter().equals(menuSell.getParameter()) && menuAux.getDirectCoordinates().x == menuSell.getDirectCoordinates().x) {
                        iFoundIndex = i;
                        break;
                    }
                }
            }
            if (iFoundIndex != -1) {
                // Encontrado (lo normal)
                SmartMenu sm = getMenuTown().getItems().get(iFoundIndex);
                if (bControl) {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 100;
                } else if (bShift) {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 10;
                } else {
                    sm.getDirectCoordinates().y = sm.getDirectCoordinates().y + 1;
                }
                sm.setParameter2("x" + sm.getDirectCoordinates().y); //$NON-NLS-1$
            } else {
                // No debería pasar
            }

            if (p3dFlags.y <= 0) {
                // Eliminamos el item de la compra
                caravanData.getMenuTownToSell().getItems().remove(iIndex);
                resize(caravanData);
            } else {
                recheckPriceToBuySell(caravanData);
            }
        }
    }

    public void recheckPriceToBuySell(CaravanData caravanData) {
        // Buy
        int iTotal = 0;
        if (caravanData != null && caravanData.getMenuCaravanToBuy() != null & caravanData.getMenuCaravanToBuy().getItems().size() > 0) {
            int iPrice = 0;
            ArrayList<SmartMenu> alItems = caravanData.getMenuCaravanToBuy().getItems();
            SmartMenu smItem;
            for (int i = 0, n = alItems.size(); i < n; i++) {
                smItem = alItems.get(i);
                iPrice += (smItem.getDirectCoordinates().x * smItem.getDirectCoordinates().y);
            }
            iTotal = iPrice;
        }

        // Sell
        if (caravanData != null && caravanData.getMenuTownToSell() != null && caravanData.getMenuTownToSell().getItems().size() > 0) {
            int iPrice = 0;
            ArrayList<SmartMenu> alItems = caravanData.getMenuTownToSell().getItems();
            SmartMenu smItem;
            for (int i = 0, n = alItems.size(); i < n; i++) {
                smItem = alItems.get(i);
                iPrice += (smItem.getDirectCoordinates().x * smItem.getDirectCoordinates().y);
            }
            iTotal -= iPrice;
        }

        setCost(caravanData, iTotal);
    }
}
