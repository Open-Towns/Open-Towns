package xaos.commands.professions;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.data.CitizenGroupData;
import xaos.data.CitizenGroups;
import xaos.main.Game;

public final class JobGroupCommandHandler implements CommandHandler {

    public static final int ENABLE_ALL = 0;
    public static final int DISABLE_ALL = 1;
    public static final int ENABLE_ITEM = 2;
    public static final int DISABLE_ITEM = 3;

    private final int actionType;

    public JobGroupCommandHandler(int actionType) {
        this.actionType = actionType;
    }

    @Override
    public void execute(CommandContext context) {
        int groupId = Integer.parseInt(context.getParameter());

        if (groupId < 0 || groupId >= CitizenGroups.MAX_GROUPS) {
            return;
        }

        CitizenGroupData groupData = Game.getWorld()
                .getCitizenGroups()
                .getGroup(groupId);

        if (groupData == null) {
            return;
        }

        if (actionType == ENABLE_ALL) {
            groupData.removeAllDeniedJobs();
        } else if (actionType == DISABLE_ALL) {
            groupData.addAllDeniedJobs();
        } else if (actionType == ENABLE_ITEM) {
            groupData.removeDeniedJob(context.getParameter2());
        } else if (actionType == DISABLE_ITEM) {
            groupData.addDeniedJob(context.getParameter2());
        }

        groupData.setJobsToCitizens();
    }
}