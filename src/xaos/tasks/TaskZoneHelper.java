package xaos.tasks;

import java.util.ArrayList;

import xaos.campaign.TutorialTrigger;
import xaos.data.HeroPrerequisite;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.Cell;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.tiles.entities.living.heroes.HeroManager;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3DShort;
import xaos.utils.UtilsIniHeaders;
import xaos.zones.Zone;
import xaos.zones.ZoneBarracks;
import xaos.zones.ZoneHeroRoom;
import xaos.zones.ZoneManager;
import xaos.zones.ZoneManagerItem;
import xaos.zones.ZonePersonal;

final class TaskZoneHelper {

    private final Task owner;

    TaskZoneHelper(Task owner) {
        this.owner = owner;
    }

    void createZone(TaskArea area) {
        ZoneManagerItem zoneDefinition = ZoneManager.getItem(owner.getParameter());

        if (zoneDefinition == null) {
            return;
        }

        Zone zone = createZoneInstance(zoneDefinition);

        boolean canCreateZone = Zone.areCellsAvailableForZone(
                zoneDefinition,
                area.xStart,
                area.yStart,
                area.xEnd,
                area.yEnd,
                area.z,
                null);

        if (!canCreateZone) {
            return;
        }

        addAvailablePointsToZone(zone, area);

        if (zone.getPoints() == null || zone.getPoints().size() == 0) {
            return;
        }

        assignZoneOwnerIfNeeded(zone, zoneDefinition);

        Game.getWorld().addZone(zone, false);

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_ZONE,
                UtilsIniHeaders.getIntIniHeader(zone.getIniHeader()),
                null);
    }

    void expandZone(TaskArea area) {
        Zone zone = Zone.getZone(Integer.parseInt(owner.getParameter()));

        if (zone == null) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("Task.37"), owner.getClass().toString()); //$NON-NLS-1$
            return;
        }

        ZoneManagerItem zoneDefinition = ZoneManager.getItem(zone.getIniHeader());

        boolean canExpandZone = Zone.areCellsAvailableForZone(
                zoneDefinition,
                area.xStart,
                area.yStart,
                area.xEnd,
                area.yEnd,
                area.z,
                zone);

        if (!canExpandZone) {
            return;
        }

        addExpansionPointsToZone(zone, area);

        if (zone.getPoints() != null && zone.getPoints().size() > 0) {
            Game.getWorld().addZone(zone, true);
        }
    }

    private Zone createZoneInstance(ZoneManagerItem zoneDefinition) {
        if (zoneDefinition.getType() == ZoneManagerItem.TYPE_PERSONAL) {
            return new ZonePersonal(owner.getParameter());
        }

        if (zoneDefinition.getType() == ZoneManagerItem.TYPE_HERO_ROOM) {
            return new ZoneHeroRoom(owner.getParameter());
        }

        if (zoneDefinition.getType() == ZoneManagerItem.TYPE_BARRACKS) {
            return new ZoneBarracks(owner.getParameter());
        }

        return new Zone(owner.getParameter());
    }

    private void addAvailablePointsToZone(Zone zone, TaskArea area) {
        for (short x = area.xStart; x <= area.xEnd; x++) {
            for (short y = area.yStart; y <= area.yEnd; y++) {
                if (!World.getCell(x, y, area.z).hasZone()) {
                    zone.getPoints().add(Point3DShort.getPoolInstance(x, y, area.z));
                }
            }
        }
    }

    private void assignZoneOwnerIfNeeded(Zone zone, ZoneManagerItem zoneDefinition) {
        if (zoneDefinition.getType() == ZoneManagerItem.TYPE_PERSONAL) {
            assignPersonalZoneOwner((ZonePersonal) zone);
        } else if (zoneDefinition.getType() == ZoneManagerItem.TYPE_HERO_ROOM) {
            assignHeroRoomOwner((ZoneHeroRoom) zone);
        } else if (zoneDefinition.getType() == ZoneManagerItem.TYPE_BARRACKS) {
            assignBarracksGroup((ZoneBarracks) zone);
        }
    }

    private void assignPersonalZoneOwner(ZonePersonal zone) {
        if (assignPersonalZoneToCitizenList(zone, World.getCitizenIDs())) {
            return;
        }

        assignPersonalZoneToCitizenList(zone, World.getSoldierIDs());
    }

    private boolean assignPersonalZoneToCitizenList(ZonePersonal zone, ArrayList<Integer> citizenIds) {
        for (int i = 0; i < citizenIds.size(); i++) {
            Citizen citizen = (Citizen) World.getLivingEntityByID(citizenIds.get(i));

            if (citizen == null || citizen.getCitizenData().hasZone()) {
                continue;
            }

            citizen.getCitizenData().setZoneID(zone.getID());
            zone.setOwnerID(citizen.getID());
            return true;
        }

        return false;
    }

    private void assignHeroRoomOwner(ZoneHeroRoom zone) {
        for (int i = 0; i < World.getHeroIDs().size(); i++) {
            Hero hero = (Hero) World.getLivingEntityByID(World.getHeroIDs().get(i));

            if (hero == null || hero.getCitizenData().hasZone()) {
                continue;
            }

            HeroPrerequisite freeRoomPrerequisite = HeroPrerequisite.getHeroPrerequisite(
                    HeroManager.getStayPrerequisites(
                            LivingEntityManager.getItem(hero.getIniHeader()).getHeroStayPrerequisite()),
                    HeroPrerequisite.ID_FREE_ROOM);

            if (freeRoomPrerequisite != null && freeRoomPrerequisite.isValueBoolean()) {
                hero.getCitizenData().setZoneID(zone.getID());
                zone.setOwnerID(hero.getID());
                return;
            }
        }
    }

    private void assignBarracksGroup(ZoneBarracks zone) {
        for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
            SoldierGroupData soldierGroupData = Game.getWorld().getSoldierGroups().getGroup(i);

            if (soldierGroupData.hasZone()) {
                continue;
            }

            soldierGroupData.setZoneID(zone.getID());
            zone.setGroupID(i);
            return;
        }
    }

    private void addExpansionPointsToZone(Zone zone, TaskArea area) {
        for (short x = area.xStart; x <= area.xEnd; x++) {
            for (short y = area.yStart; y <= area.yEnd; y++) {
                Point3DShort zonePoint = Point3DShort.getPoolInstance(x, y, area.z);
                Cell zoneCell = World.getCell(zonePoint);

                if (zoneCell.getAstarZoneID() != -1 && !zone.getPoints().contains(zonePoint)) {
                    zone.getPoints().add(zonePoint);
                }
            }
        }
    }
}