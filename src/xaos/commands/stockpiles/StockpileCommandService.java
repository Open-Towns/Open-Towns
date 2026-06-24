package xaos.commands.stockpiles;

import java.util.ArrayList;

import xaos.main.Game;
import xaos.stockpiles.Stockpile;
import xaos.data.Type;
import xaos.main.World;

public final class StockpileCommandService {

    public void copyToAllMatchingStockpiles(String sourceStockpileId) {
        Stockpile sourceStockpile = Stockpile.getStockpile(sourceStockpileId);

        if (sourceStockpile == null) {
            return;
        }

        Type sourceType = sourceStockpile.getType();
        ArrayList<Stockpile> stockpiles = Game.getWorld().getStockpiles();

        for (int i = 0; i < stockpiles.size(); i++) {
            Stockpile destinationStockpile = stockpiles.get(i);

            if (!shouldCopyToStockpile(sourceStockpile, destinationStockpile, sourceType)) {
                continue;
            }

            boolean removedSomething = copyTypeElements(
                    sourceType,
                    destinationStockpile.getType()
            );

            if (removedSomething) {
                markStockpileItemsForHauling(destinationStockpile);
            }
        }
    }

    private boolean shouldCopyToStockpile(
            Stockpile sourceStockpile,
            Stockpile destinationStockpile,
            Type sourceType
    ) {
        return destinationStockpile.getID() != sourceStockpile.getID()
                && destinationStockpile.getType().getID().equals(sourceType.getID())
                && !destinationStockpile.isLockedToCopy();
    }

    private boolean copyTypeElements(Type sourceType, Type destinationType) {
        boolean removedSomething = false;

        for (int i = destinationType.getElements().size() - 1; i >= 0; i--) {
            String destinationElement = destinationType.getElements().get(i);

            if (!sourceType.contains(destinationElement)) {
                destinationType.removeElement(destinationElement);
                removedSomething = true;
            }
        }

        for (int i = sourceType.getElements().size() - 1; i >= 0; i--) {
            String sourceElement = sourceType.getElements().get(i);

            if (!destinationType.contains(sourceElement)) {
                destinationType.addElement(
                        sourceElement,
                        sourceType.getElementNames().get(i)
                );
            }
        }

        return removedSomething;
    }

    private void markStockpileItemsForHauling(Stockpile stockpile) {
        for (int i = 0; i < stockpile.getPoints().size(); i++) {
            Game.getWorld().addItemToBeHauled(
                    World.getCell(stockpile.getPoints().get(i)).getItem()
            );
        }
    }
}