package xaos.tasks;

import xaos.campaign.TutorialTrigger;
import xaos.main.Game;
import xaos.stockpiles.Stockpile;
import xaos.utils.Point3DShort;
import xaos.utils.UtilsIniHeaders;

final class TaskStockpileHelper {

    private final Task owner;

    TaskStockpileHelper(Task owner) {
        this.owner = owner;
    }

    void createStockpile(TaskArea area) {
        boolean hasStockpilePoint = false;
        Stockpile stockpile = new Stockpile(owner.getParameter());

        for (short x = area.xStart; x <= area.xEnd; x++) {
            for (short y = area.yStart; y <= area.yEnd; y++) {
                if (Stockpile.isCellAvailableForStockpile(x, y, area.z)) {
                    stockpile.addPoint(Point3DShort.getPoolInstance(x, y, area.z));
                    hasStockpilePoint = true;
                }
            }
        }

        if (!hasStockpilePoint) {
            return;
        }

        if (stockpile.getPoints() == null || stockpile.getPoints().size() == 0) {
            return;
        }

        if (Game.isDisabledItemsON()) {
            stockpile.disableAll();
        }

        Game.getWorld().addStockPile(stockpile);

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_PILE,
                UtilsIniHeaders.getIntIniHeader(owner.getParameter()),
                null);
    }
}