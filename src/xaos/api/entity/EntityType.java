package xaos.api.entity;

/**
 * Broad runtime categories of entities exposed through the public API.
 */
public enum EntityType {

    ITEM,

    BUILDING,

    LIVING,

    /**
     * Internal/special entities such as temporary visual indicators.
     */
    SPECIAL,

    /**
     * Fallback for an entity type that the API does not yet recognise.
     */
    UNKNOWN
}