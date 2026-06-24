package xaos.commands.professions;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.data.CitizenGroupData;
import xaos.tiles.entities.living.*;
import xaos.main.Game;
import xaos.campaign.TutorialTrigger;
import xaos.main.World;

public final class CitizenSetJobGroupCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        int citizenId = Integer.parseInt(context.getParameter());
        int newGroupId = Integer.parseInt(context.getParameter2());

        LivingEntity livingEntity = World.getLivingEntityByID(citizenId);

        if (livingEntity == null) {
            return;
        }

        LivingEntityManagerItem item = LivingEntityManager.getItem(
                livingEntity.getIniHeader()
        );

        if (item == null || item.getType() != LivingEntity.TYPE_CITIZEN) {
            return;
        }

        Citizen citizen = (Citizen) livingEntity;

        int currentGroupId = citizen.getCitizenData().getGroupID();

        Game.getWorld()
                .getCitizenGroups()
                .removeCitizenFromGroup(citizenId, currentGroupId);

        Game.getWorld()
                .getCitizenGroups()
                .addCitizenToGroup(citizenId, newGroupId);

        citizen.getCitizenData().setGroupID(newGroupId);
        citizen.getCitizenData().removeAllDeniedJobs();

        if (newGroupId != -1) {
            CitizenGroupData groupData = Game.getWorld()
                    .getCitizenGroups()
                    .getGroup(newGroupId);

            if (groupData != null) {
                groupData.setJobsToCitizens();
            }
        }

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_CIV2GROUP,
                newGroupId + 1,
                null
        );
    }
}