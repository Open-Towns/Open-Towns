package xaos.api.items;

import java.util.List;
import java.util.Optional;

import xaos.api.definitions.DefinitionId;

public interface ItemQueryApi {

    List<ItemReference> find(
            DefinitionId definitionId
    );

    default Optional<ItemReference> first(
            DefinitionId definitionId
    ) {
        return find(definitionId)
                .stream()
                .findFirst();
    }

    default int count(
            DefinitionId definitionId
    ) {
        return find(definitionId).size();
    }

    default List<ItemReference> findInWorld(
            DefinitionId definitionId
    ) {
        return find(definitionId)
                .stream()
                .filter(ItemReference::isInWorld)
                .toList();
    }

    default List<ItemReference> findInContainers(
            DefinitionId definitionId
    ) {
        return find(definitionId)
                .stream()
                .filter(ItemReference::isInContainer)
                .toList();
    }
}