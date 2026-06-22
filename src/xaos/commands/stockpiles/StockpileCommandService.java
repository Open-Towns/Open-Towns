package xaos.commands.stockpiles;

import xaos.data.Type;
import xaos.main.Game;
import xaos.main.World;
import xaos.stockpiles.Stockpile;

public final class StockpileCommandService {

    public void copySettingsToMatchingStockpiles(String sourceStockpileId) {
        Stockpile source = Stockpile.getStockpile(sourceStockpileId);

        if (source == null) {
            return;
        }

        Type sourceType = source.getType();

        for (Stockpile destination : Game.getWorld().getStockpiles()) {
            if (shouldCopyToStockpile(source, destination, sourceType)) {
                boolean removedItems = copyTypeElements(sourceType, destination.getType());

                if (removedItems) {
                    markItemsForHauling(destination);
                }
            }
        }
    }

    private boolean shouldCopyToStockpile(
            Stockpile source,
            Stockpile destination,
            Type sourceType
    ) {
        return destination.getID() != source.getID()
                && destination.getType().getID().equals(sourceType.getID())
                && !destination.isLockedToCopy();
    }

    private boolean copyTypeElements(Type sourceType, Type destinationType) {
        boolean removedSomething = false;

        for (int i = destinationType.getElements().size() - 1; i >= 0; i--) {
            String element = destinationType.getElements().get(i);

            if (!sourceType.contains(element)) {
                destinationType.removeElement(element);
                removedSomething = true;
            }
        }

        for (int i = sourceType.getElements().size() - 1; i >= 0; i--) {
            String element = sourceType.getElements().get(i);

            if (!destinationType.contains(element)) {
                destinationType.addElement(
                        element,
                        sourceType.getElementNames().get(i)
                );
            }
        }

        return removedSomething;
    }

    private void markItemsForHauling(Stockpile stockpile) {
        for (int i = 0; i < stockpile.getPoints().size(); i++) {
            Game.getWorld().addItemToBeHauled(
                    World.getCell(stockpile.getPoints().get(i)).getItem()
            );
        }
    }
}