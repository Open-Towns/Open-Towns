package xaos.commands.debug;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.TownsProperties;
import xaos.main.World;
import xaos.panels.MessagesPanel;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.heroes.Hero;


public final class TestCommandHandler implements CommandHandler {

    public static final int NEW_CITIZEN = 0;
    public static final int FULFILL_NEEDS = 1;
    public static final int SPAWN_SIEGE = 2;
    public static final int FORCE_SLEEP = 3;
    public static final int CHECK_CARAVANS = 4;
    public static final int REVEAL_MAP = 5;
    public static final int CHECK_HEROES = 6;

    private final int actionType;

    public TestCommandHandler(int actionType) {
        this.actionType = actionType;
    }

    @Override
    public void execute(CommandContext context) {
        if (!TownsProperties.TEST_COMMANDS) {
            return;
        }

        if (actionType == NEW_CITIZEN) {
            World.addNewLiving(null, LivingEntity.TYPE_CITIZEN, true, 0, 0, 0, true);
            return;
        }

        if (actionType == FULFILL_NEEDS) {
            fulfillNeeds();
            return;
        }

        if (actionType == SPAWN_SIEGE) {
            Game.getWorld().spawnSiege();
            return;
        }

        if (actionType == FORCE_SLEEP) {
            forceSleep();
            return;
        }

        if (actionType == CHECK_CARAVANS) {
            Game.getWorld().checkCaravansCome();
            return;
        }

        if (actionType == REVEAL_MAP) {
            revealMap();
            return;
        }

        if (actionType == CHECK_HEROES) {
            Game.getWorld().checkHeroesCome();
        }
    }

    private void fulfillNeeds() {
        for (int i = 0; i < World.getCitizenIDs().size(); i++) {
            ((Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i)))
                    .getCitizenData()
                    .setHungry(5000);
        }

        for (int i = 0; i < World.getSoldierIDs().size(); i++) {
            ((Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i)))
                    .getCitizenData()
                    .setHungry(5000);
        }

        for (int i = 0; i < World.getHeroIDs().size(); i++) {
            ((Hero) World.getLivingEntityByID(World.getHeroIDs().get(i)))
                    .getCitizenData()
                    .setHungry(5000);
        }

        MessagesPanel.clear();
        MessagesPanel.addMessage(
                MessagesPanel.TYPE_SYSTEM,
                TownsProperties.GAME_NAME + " " + TownsProperties.GAME_VERSION_FULL
        );
    }

    private void forceSleep() {
        for (int i = 0; i < World.getCitizenIDs().size(); i++) {
            ((Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i)))
                    .getCitizenData()
                    .setSleep(0);
        }

        for (int i = 0; i < World.getSoldierIDs().size(); i++) {
            ((Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i)))
                    .getCitizenData()
                    .setSleep(0);
        }

        for (int i = 0; i < World.getHeroIDs().size(); i++) {
            ((Hero) World.getLivingEntityByID(World.getHeroIDs().get(i)))
                    .getCitizenData()
                    .setSleep(0);
        }
    }

    private void revealMap() {
        for (int x = 0; x < World.MAP_WIDTH; x++) {
            for (int y = 0; y < World.MAP_HEIGHT; y++) {
                for (int z = 0; z < World.MAP_DEPTH; z++) {
                    World.getCell(x, y, z).setDiscovered(true);
                }
            }
        }

        Game.getWorld().setNumFloorsDiscovered(World.MAP_DEPTH);
    }
}