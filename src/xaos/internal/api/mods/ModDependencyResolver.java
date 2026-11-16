package xaos.internal.api.mods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModDependencyResolver {

    private enum State {
        VISITING,
        RESOLVED,
        FAILED
    }

    private ModDependencyResolver() {
    }

    public static Result resolve(
            Map<String, List<String>> dependenciesByMod
    ) {
        Map<String, State> states =
                new HashMap<>();

        List<String> loadOrder =
                new ArrayList<>();

        Map<String, String> failures =
                new LinkedHashMap<>();

        List<String> modIds =
                new ArrayList<>(
                        dependenciesByMod.keySet()
                );

        /*
         * Keep unrelated mods deterministic.
         */
        modIds.sort(
                String::compareTo
        );

        for (String modId : modIds) {

            resolve(
                    modId,
                    dependenciesByMod,
                    states,
                    new ArrayList<>(),
                    loadOrder,
                    failures
            );
        }

        return new Result(
                List.copyOf(loadOrder),
                Map.copyOf(failures)
        );
    }

    private static boolean resolve(
            String modId,
            Map<String, List<String>> dependenciesByMod,
            Map<String, State> states,
            List<String> path,
            List<String> loadOrder,
            Map<String, String> failures
    ) {
        State state =
                states.get(modId);

        if (state == State.RESOLVED) {
            return true;
        }

        if (state == State.FAILED) {
            return false;
        }

        /*
         * Finding a mod already being visited means
         * the dependency graph contains a cycle.
         */
        if (state == State.VISITING) {

            int cycleStart =
                    path.indexOf(modId);

            List<String> cycle =
                    new ArrayList<>();

            if (cycleStart >= 0) {

                cycle.addAll(
                        path.subList(
                                cycleStart,
                                path.size()
                        )
                );

            } else {

                cycle.addAll(path);
            }

            cycle.add(modId);

            String message =
                    "Circular Java mod dependency detected: "
                            + String.join(
                                    " -> ",
                                    cycle
                            );

            /*
             * Every mod taking part in the cycle fails.
             */
            if (cycleStart >= 0) {

                for (int i = cycleStart;
                        i < path.size();
                        i++) {

                    String cycleMod =
                            path.get(i);

                    states.put(
                            cycleMod,
                            State.FAILED
                    );

                    failures.putIfAbsent(
                            cycleMod,
                            message
                    );
                }
            }

            states.put(
                    modId,
                    State.FAILED
            );

            failures.putIfAbsent(
                    modId,
                    message
            );

            return false;
        }

        List<String> dependencies =
                dependenciesByMod.get(modId);

        if (dependencies == null) {

            states.put(
                    modId,
                    State.FAILED
            );

            failures.putIfAbsent(
                    modId,
                    "Java mod ["
                            + modId
                            + "] could not be found."
            );

            return false;
        }

        states.put(
                modId,
                State.VISITING
        );

        path.add(modId);

        for (String dependencyId
                : dependencies) {

            if (!dependenciesByMod.containsKey(
                    dependencyId)) {

                states.put(
                        modId,
                        State.FAILED
                );

                failures.putIfAbsent(
                        modId,
                        "Cannot load Java mod ["
                                + modId
                                + "]: required dependency ["
                                + dependencyId
                                + "] is not enabled or could not be found."
                );

                path.remove(
                        path.size() - 1
                );

                return false;
            }

            if (!resolve(
                    dependencyId,
                    dependenciesByMod,
                    states,
                    path,
                    loadOrder,
                    failures
            )) {

                states.put(
                        modId,
                        State.FAILED
                );

                failures.putIfAbsent(
                        modId,
                        "Cannot load Java mod ["
                                + modId
                                + "]: dependency ["
                                + dependencyId
                                + "] failed to load."
                );

                path.remove(
                        path.size() - 1
                );

                return false;
            }
        }

        path.remove(
                path.size() - 1
        );

        states.put(
                modId,
                State.RESOLVED
        );

        loadOrder.add(modId);

        return true;
    }

    public record Result(
            List<String> loadOrder,
            Map<String, String> failures
    ) {
    }
}