package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;

public final class ViewEntityCommandHandler implements CommandHandler {

    public static final int NEXT_CITIZEN = 0;
    public static final int PREVIOUS_CITIZEN = 1;
    public static final int NEXT_SOLDIER = 2;
    public static final int PREVIOUS_SOLDIER = 3;
    public static final int NEXT_HERO = 4;
    public static final int PREVIOUS_HERO = 5;

    private final int actionType;

    public ViewEntityCommandHandler(int actionType) {
        this.actionType = actionType;
    }

    @Override
    public void execute(CommandContext context) {
        if (actionType == NEXT_CITIZEN && Game.getWorld().setNextIndexViewCitizen()) {
            Game.getWorld().setViewOnCitizen();
            return;
        }

        if (actionType == PREVIOUS_CITIZEN && Game.getWorld().setPreviousIndexViewCitizen()) {
            Game.getWorld().setViewOnCitizen();
            return;
        }

        if (actionType == NEXT_SOLDIER && Game.getWorld().setNextIndexViewSoldier()) {
            Game.getWorld().setViewOnSoldier();
            return;
        }

        if (actionType == PREVIOUS_SOLDIER && Game.getWorld().setPreviousIndexViewSoldier()) {
            Game.getWorld().setViewOnSoldier();
            return;
        }

        if (actionType == NEXT_HERO && Game.getWorld().setNextIndexViewHero()) {
            Game.getWorld().setViewOnHero();
            return;
        }

        if (actionType == PREVIOUS_HERO && Game.getWorld().setPreviousIndexViewHero()) {
            Game.getWorld().setViewOnHero();
        }
    }
}