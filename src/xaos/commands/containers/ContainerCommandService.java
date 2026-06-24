package xaos.commands.containers;

import java.util.ArrayList;

import xaos.main.Game;
import xaos.tiles.entities.items.Container;
import xaos.data.Type;

public final class ContainerCommandService {

    public void copyToAllMatchingContainers(int sourceContainerId) {
        Container sourceContainer = Game.getWorld().getContainer(sourceContainerId);

        if (sourceContainer == null) {
            return;
        }

        Type sourceType = sourceContainer.getType();
        ArrayList<Container> containers = Game.getWorld().getContainers();

        for (int i = 0; i < containers.size(); i++) {
            Container destinationContainer = containers.get(i);

            if (!shouldCopyToContainer(sourceContainer, destinationContainer, sourceType)) {
                continue;
            }

            boolean removedSomething = copyTypeElements(
                    sourceType,
                    destinationContainer.getType()
            );

            if (removedSomething) {
                destinationContainer.setWrongItemsInside(true);
            }
        }
    }

    private boolean shouldCopyToContainer(
            Container sourceContainer,
            Container destinationContainer,
            Type sourceType
    ) {
        return destinationContainer.getItemID() != sourceContainer.getItemID()
                && destinationContainer.getType().getID().equals(sourceType.getID())
                && !destinationContainer.isLockedToCopy();
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
}
