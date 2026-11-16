package xaos.internal.api.definitions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import xaos.api.definitions.Definition;
import xaos.api.definitions.DefinitionId;
import xaos.api.definitions.DefinitionRegistry;

/**
 * Internal adapter from legacy Towns managers
 * to read-only public API snapshots.
 */
final class MappedDefinitionRegistry<
        S,
        T extends Definition
> implements DefinitionRegistry<T> {

    private final DefinitionKind kind;

    private final Supplier<
            ? extends Map<String, S>
    > allSupplier;

    private final BiFunction<
            DefinitionId,
            S,
            T
    > mapper;

    MappedDefinitionRegistry(
            DefinitionKind kind,
            Supplier<? extends Map<String, S>> allSupplier,
            BiFunction<DefinitionId, S, T> mapper
    ) {
        this.kind = kind;
        this.allSupplier = allSupplier;
        this.mapper = mapper;
    }

    @Override
    public T get(String id) {
        DefinitionId requested;

        try {
            requested = DefinitionId.parse(id);
        } catch (IllegalArgumentException exception) {
            return null;
        }

        DefinitionId actual =
                DefinitionOwnershipRegistry.idFor(
                        kind,
                        requested.path()
                );

        /*
         * Check that the requested namespace matches
         * the actual owner of the legacy definition.
         */
        if (!actual.equals(requested)) {
            return null;
        }

        Map<String, S> definitions =
                allSupplier.get();

        S source =
                definitions.get(
                        requested.path()
                );

        if (source == null) {
            return null;
        }

        return mapper.apply(
                actual,
                source
        );
    }

    @Override
    public List<T> all() {
        return allSupplier.get()
                .entrySet()
                .stream()
                .map(entry -> {
                    DefinitionId id =
                            DefinitionOwnershipRegistry.idFor(
                                    kind,
                                    entry.getKey()
                            );

                    return mapper.apply(
                            id,
                            entry.getValue()
                    );
                })
                .toList();
    }

    @Override
    public Set<DefinitionId> ids() {
        LinkedHashSet<DefinitionId> ids =
                new LinkedHashSet<>();

        for (String legacyId
                : allSupplier.get().keySet()) {

            ids.add(
                    DefinitionOwnershipRegistry.idFor(
                            kind,
                            legacyId
                    )
            );
        }

        return Set.copyOf(ids);
    }
}