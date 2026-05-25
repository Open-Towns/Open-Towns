package xaos.tasks;

public class TaskTypes {
    
    // Task types
    public final static int TASK_NO_TASK = 0; // No task
    public final static int TASK_DIG = 3; // Dig task, mine downward
    public final static int TASK_MINE = 4; // Mine task
    public final static int TASK_CANCEL_ORDER = 5; // Cancel order, mine/dig/chop
    public final static int TASK_MINE_LADDER = 7; // Mine task and place ladder

    // Citizens
    public final static int TASK_WEAR = 10; // Equip citizen task
    public final static int TASK_WEAR_OFF = 11; // Unequip citizen task
    public final static int TASK_CONVERT_TO_CIVILIAN = 12; // Convert a soldier into a civilian task
    public final static int TASK_CONVERT_TO_SOLDIER = 13; // Convert a civilian into a soldier task
    public final static int TASK_FIGHT = 14; // Fight task, only assigned to soldiers
    public final static int TASK_HEAL = 15; // Heal task
    public final static int TASK_AUTOEQUIP = 16; // Auto-equip task
    public final static int TASK_SOLDIER_SET_STATE = 17; // Change a soldier's state, guard, boss around, patrol
    public final static int TASK_SOLDIER_ADD_PATROL_POINT = 18; // Add a patrol point to a soldier task
    public final static int TASK_SOLDIER_REMOVE_PATROL_POINT = 19; // Remove a patrol point from a soldier task

    // Groups
    public final static int TASK_SOLDIER_ADD_PATROL_POINT_GROUP = 20; // Add a patrol point to a group task
    public final static int TASK_SOLDIER_REMOVE_PATROL_POINT_GROUP = 21; // Remove a patrol point from a group task

    // Buildings
    public final static int TASK_BUILD = 25; // Build task, buildings
    public final static int TASK_DESTROY_BUILDING = 26;
    public final static int TASK_TURN_OFF_NON_STOP = 27;
    public final static int TASK_TURN_ON_NON_STOP = 28;

    // Terrain
    public final static int TASK_TERRAIN_RAISE = 30;
    public final static int TASK_TERRAIN_LOWER = 31;
    public final static int TASK_TERRAIN_CHANGE = 32;
    public final static int TASK_TERRAIN_ADD_FLUID = 33;
    public final static int TASK_TERRAIN_REMOVE_FLUID = 34;

    // Items
    public final static int TASK_CREATE_AND_PLACE = 40; // Build items and place them somewhere task
    public final static int TASK_REMOVE_BUILDING_TASK = 41; // Remove the item currently being built from a building
                                                            // task
    public final static int TASK_CREATE_IN_A_BUILDING = 42; // Build items in a given building task
    public final static int TASK_CREATE = 43; // Build items without specifying a building or place task
    public final static int TASK_DESTROY_ENTITY = 44;
    public final static int TASK_CREATE_AND_PLACE_ROW = 45; // Build items and place them somewhere task. Creates a row
                                                            // of them
    public final static int TASK_LOCK = 46;
    public final static int TASK_UNLOCK_OPEN = 47;
    public final static int TASK_UNLOCK_CLOSE = 48;

    // Stockpiles
    public final static int TASK_STOCKPILE = 50; // Create stockpile task
    public final static int TASK_DELETE_STOCKPILE = 51;

    // Zones
    public final static int TASK_CREATE_ZONE = 56; // Create zones task, hospital, dining room, carpenters, etc.
    public final static int TASK_DELETE_ZONE = 57; // Delete zone task
    public final static int TASK_EXPAND_ZONE = 58; // Expand zone task
    public final static int TASK_CHANGE_OWNER = 59; // Change the zone owner task
    public final static int TASK_CHANGE_OWNER_GROUP = 60; // Change the zone owner group task

    // Haul / Move / put in containers
    public final static int TASK_HAUL = 65; // Haul task. These are special, created on the fly and not stored in the
                                            // task list. They disappear when the citizen drops them
    public final static int TASK_MOVE_AND_LOCK = 66; // Like haul, but persistent. It is stored in the task list. Used
                                                     // only by create tasks when an item already exists in the world
                                                     // and nothing needs to be built
    public final static int TASK_DROP = 67; // Like haul, except the citizen does not need to pick anything up. Also
                                            // created on the fly
    public final static int TASK_PUT_IN_CONTAINER = 68; // Like haul, except the citizen does not need to pick anything
                                                        // up. Also created on the fly
    public final static int TASK_REMOVE_FROM_CONTAINER = 69; // Like haul, except the citizen does not need to pick
                                                             // anything up. Also created on the fly

    // Sleep / eat
    public final static int TASK_SLEEP = 70;
    public final static int TASK_EAT = 71;

    // Custom action
    public final static int TASK_CUSTOM_ACTION = 80;
    public final static int TASK_QUEUE = 81;
    public final static int TASK_QUEUE_AND_PLACE = 82;
    public final static int TASK_QUEUE_AND_PLACE_ROW = 83;
    public final static int TASK_QUEUE_AND_PLACE_AREA = 84;

    // Containers
    // public final static int TASK_CONTAINER_ENABLE_ALL = 90;
    // public final static int TASK_CONTAINER_DISABLE_ALL = 91;
    // public final static int TASK_CONTAINER_ENABLE_ITEM = 92;
    // public final static int TASK_CONTAINER_DISABLE_ITEM = 93;

    // Caravan
    public final static int TASK_MOVE_TO_CARAVAN = 100;

    // Food
    public final static int TASK_FOOD_NEEDED = 110;

    // Task states
    public final static int STATE_CREATING_INIZONE = 1; // Marks the start of an area
    public final static int STATE_CREATING_ENDZONE = 2; // Marks the end of an area
    public final static int STATE_CREATING_SINGLEPOINT = 3; // Marks a point on the map
    public final static int STATE_CREATED = 10; // Indicates that the task has already been created
}
