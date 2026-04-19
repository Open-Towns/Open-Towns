package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

public class SoldierGroups implements Externalizable {

    private static final long serialVersionUID = 775074143636086575L;

    public final static int MAX_GROUPS = 10;

    private ArrayList<Integer> soldiersWithoutGroup = new ArrayList<Integer>();
    private ArrayList<SoldierGroupData> groups = new ArrayList<SoldierGroupData>(MAX_GROUPS);

    public SoldierGroups() {
        clear();
    }

    public void setGroups(ArrayList<SoldierGroupData> groups) {
        this.groups = groups;
    }

    public ArrayList<SoldierGroupData> getGroups() {
        return groups;
    }

    public SoldierGroupData getGroup(int iIndex) {
        if (iIndex >= 0 && iIndex < groups.size()) {
            return groups.get(iIndex);
        }

        return null;
    }

    public void setSoldiersWithoutGroup(ArrayList<Integer> soldiersWithoutGroup) {
        this.soldiersWithoutGroup = soldiersWithoutGroup;
    }

    public ArrayList<Integer> getSoldiersWithoutGroup() {
        return soldiersWithoutGroup;
    }

    public void addSoldierToGroup(int iSoldierID, int iDestinationGroup) {
        if (iDestinationGroup == -1) {
            getSoldiersWithoutGroup().add(Integer.valueOf(iSoldierID));
        } else {
            getGroup(iDestinationGroup).getLivingIDs().add(Integer.valueOf(iSoldierID));
        }
    }

    public boolean removeSoldierFromGroup(int soldierID, int iCurrentGroup) {
        if (iCurrentGroup == -1) {
            return getSoldiersWithoutGroup().remove(Integer.valueOf(soldierID));
        } else {
            return getGroup(iCurrentGroup).getLivingIDs().remove(Integer.valueOf(soldierID));
        }
    }

    public void clear() {
        soldiersWithoutGroup = new ArrayList<Integer>();
        groups = new ArrayList<SoldierGroupData>(MAX_GROUPS);
        for (int i = 0; i < MAX_GROUPS; i++) {
            SoldierGroupData sgd = new SoldierGroupData(i);
            groups.add(sgd);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        soldiersWithoutGroup = (ArrayList<Integer>) in.readObject();
        groups = (ArrayList<SoldierGroupData>) in.readObject();

        if (groups.size() < MAX_GROUPS) {
            int iSize = groups.size();
            for (int i = 0; i < (MAX_GROUPS - iSize); i++) {
                SoldierGroupData sgd = new SoldierGroupData(groups.size());
                groups.add(sgd);
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(soldiersWithoutGroup);
        out.writeObject(groups);
    }

//	private Object readResolve() throws ObjectStreamException {
//		return new SoldierGroups ();
//	}
}
