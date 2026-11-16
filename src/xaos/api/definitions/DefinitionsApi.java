package xaos.api.definitions;

/** Entry point for XML/content definitions exposed through the public API. */
public interface DefinitionsApi {

    DefinitionRegistry<ItemDefinition> items();

    DefinitionRegistry<LivingDefinition> living();

    DefinitionRegistry<BuildingDefinition> buildings();

    DefinitionRegistry<ActionDefinition> actions();

    DefinitionRegistry<EventDefinition> events();

    DefinitionRegistry<TerrainDefinition> terrain();
}
