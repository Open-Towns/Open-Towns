package xaos.commands.professions;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;

import xaos.tiles.entities.living.*;

import xaos.main.World;

public final class ProfessionCommandHandler implements CommandHandler {

    public static final int ENABLE_ALL = 0;
    public static final int DISABLE_ALL = 1;
    public static final int ENABLE_ITEM = 2;
    public static final int DISABLE_ITEM = 3;

    private final int actionType;

    public ProfessionCommandHandler(int actionType) {
        this.actionType = actionType;
    }

    @Override
    public void execute(CommandContext context) {
        Citizen citizen = getCitizen(Integer.parseInt(context.getParameter()));

        if (citizen == null) {
            return;
        }

        if (actionType == ENABLE_ALL) {
            citizen.getCitizenData().removeAllDeniedJobs();
        } else if (actionType == DISABLE_ALL) {
            citizen.getCitizenData().addAllDeniedJobs();
        } else if (actionType == ENABLE_ITEM) {
            citizen.getCitizenData().removeDeniedJob(context.getParameter2());
        } else if (actionType == DISABLE_ITEM) {
            citizen.getCitizenData().addDeniedJob(context.getParameter2());
        }
    }

    private Citizen getCitizen(int livingEntityId) {
        LivingEntity livingEntity = World.getLivingEntityByID(livingEntityId);

        if (livingEntity == null) {
            return null;
        }

        LivingEntityManagerItem item = LivingEntityManager.getItem(
                livingEntity.getIniHeader()
        );

        if (item == null || item.getType() != LivingEntity.TYPE_CITIZEN) {
            return null;
        }

        return (Citizen) livingEntity;
    }
}