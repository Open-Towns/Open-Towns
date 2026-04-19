package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.main.Game;
import xaos.main.World;

public class CitizenGroups implements Externalizable {

    private static final long serialVersionUID = 2133422179578566722L;

    public final static int MAX_GROUPS = SoldierGroups.MAX_GROUPS;

    private ArrayList<Integer> citizensWithoutGroup = new ArrayList<Integer>();
    private ArrayList<CitizenGroupData> groups = new ArrayList<CitizenGroupData>(MAX_GROUPS);

    public CitizenGroups() {
        clear();
    }

    public void setGroups(ArrayList<CitizenGroupData> groups) {
        this.groups = groups;
    }

    public ArrayList<CitizenGroupData> getGroups() {
        return groups;
    }

    public CitizenGroupData getGroup(int iIndex) {
        if (iIndex >= 0 && iIndex < groups.size()) {
            return groups.get(iIndex);
        }

        return null;
    }

    public void setCitizensWithoutGroup(ArrayList<Integer> citizensWithoutGroup) {
        this.citizensWithoutGroup = citizensWithoutGroup;
    }

    public ArrayList<Integer> getCitizensWithoutGroup() {
        return citizensWithoutGroup;
    }

    public void addCitizenToGroup(int iCitizenID, int iDestinationGroup) {
        if (iDestinationGroup == -1) {
            getCitizensWithoutGroup().add(Integer.valueOf(iCitizenID));
        } else {
            getGroup(iDestinationGroup).getLivingIDs().add(Integer.valueOf(iCitizenID));
        }
    }

    public boolean removeCitizenFromGroup(int citizenID, int iCurrentGroup) {
        if (iCurrentGroup == -1) {
            return getCitizensWithoutGroup().remove(Integer.valueOf(citizenID));
        } else {
            return getGroup(iCurrentGroup).getLivingIDs().remove(Integer.valueOf(citizenID));
        }
    }

    /**
     * Esto comprueba que no haya algún ciudadano inexistente en los grupos
     */
    public void purgeNonExistentCitizens() {
        // Sin grupo
        ArrayList<Integer> aiNoGroup = getCitizensWithoutGroup();
        if (aiNoGroup != null) {
            for (int i = (aiNoGroup.size() - 1); i >= 0; i--) {
                if (!World.getCitizenIDs().contains(aiNoGroup.get(i))) {
                    // BAM
                    aiNoGroup.remove(i);
                }
            }
        }

        // En grupo
        for (int g = 0; g < MAX_GROUPS; g++) {
            CitizenGroupData cgd = getGroup(g);
            if (cgd != null) {
                ArrayList<Integer> aiGroup = cgd.getLivingIDs();
                if (aiGroup != null) {
                    for (int i = (aiGroup.size() - 1); i >= 0; i--) {
                        if (!World.getCitizenIDs().contains(aiGroup.get(i))) {
                            // BAM
                            aiNoGroup.remove(i);
                        }
                    }
                }
            }
        }
    }

    public void clear() {
        citizensWithoutGroup = new ArrayList<Integer>();
        groups = new ArrayList<CitizenGroupData>(MAX_GROUPS);
        for (int i = 0; i < MAX_GROUPS; i++) {
            CitizenGroupData cgd = new CitizenGroupData(i);
            groups.add(cgd);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V11) {
            citizensWithoutGroup = (ArrayList<Integer>) in.readObject();
            groups = (ArrayList<CitizenGroupData>) in.readObject();

            if (groups.size() < MAX_GROUPS) {
                int iSize = groups.size();
                for (int i = 0; i < (MAX_GROUPS - iSize); i++) {
                    CitizenGroupData cgd = new CitizenGroupData(groups.size());
                    groups.add(cgd);
                }
            }
        } else {
            citizensWithoutGroup = new ArrayList<Integer>();
            groups = new ArrayList<CitizenGroupData>(MAX_GROUPS);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(citizensWithoutGroup);
        out.writeObject(groups);
    }
}
