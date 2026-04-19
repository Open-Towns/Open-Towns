package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.actions.ActionPriorityManager;
import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.utils.Messages;
import xaos.utils.UtilsIniHeaders;

public class CitizenGroupData implements Externalizable {

    private static final long serialVersionUID = 2274922628971868017L;

    private int id;
    private String name;
    private ArrayList<Integer> livingIDs;
    private ArrayList<Integer> jobsDenied;

    public CitizenGroupData() {
    }

    public CitizenGroupData(int id) {
        setId(id);
        setName(null);
        livingIDs = new ArrayList<Integer>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().length() == 0) {
            this.name = Messages.getString("CitizenGroupData.0") + (id + 1); //$NON-NLS-1$
        } else {
            this.name = name;
        }
    }

    public ArrayList<Integer> getLivingIDs() {
        return livingIDs;
    }

    public void setLivingIDs(ArrayList<Integer> livingIDs) {
        this.livingIDs = livingIDs;
    }

    public void setJobsDenied(ArrayList<Integer> jobsDenied) {
        this.jobsDenied = jobsDenied;
    }

    public ArrayList<Integer> getJobsDenied() {
        return jobsDenied;
    }

    public boolean containsDeniedJob(int iJob) {
        if (jobsDenied == null) {
            return false;
        }

        Integer iObj = Integer.valueOf(iJob);
        return jobsDenied.contains(iObj);
    }

    public void removeAllDeniedJobs() {
        jobsDenied = null;
    }

    public void addAllDeniedJobs() {
        ArrayList<String> alPriorities = ActionPriorityManager.getPrioritiesList();
        if (alPriorities != null) {
            for (int i = 0; i < alPriorities.size(); i++) {
                addDeniedJob(alPriorities.get(i));
            }
        }
    }

    public void addDeniedJob(String sJob) {
        addDeniedJob(UtilsIniHeaders.getIntIniHeader(sJob));
    }

    public void addDeniedJob(int iJob) {
        if (jobsDenied == null) {
            jobsDenied = new ArrayList<Integer>(1);
            jobsDenied.add(Integer.valueOf(iJob));
        } else {
            Integer iObj = Integer.valueOf(iJob);
            if (!jobsDenied.contains(iObj)) {
                jobsDenied.add(iObj);
            }
        }
    }

    public void removeDeniedJob(String sJob) {
        removeDeniedJob(UtilsIniHeaders.getIntIniHeader(sJob));
    }

    public void removeDeniedJob(int iJob) {
        if (jobsDenied == null) {
            return;
        }

        Integer iObj = Integer.valueOf(iJob);
        jobsDenied.remove(iObj);

        if (jobsDenied.size() == 0) {
            jobsDenied = null;
        }
    }

    /**
     * Setea los jobs a todos los citizens del grupo
     */
    public void setJobsToCitizens() {
        if (getLivingIDs() != null) {
            LivingEntity le;
            for (int i = 0; i < getLivingIDs().size(); i++) {
                le = World.getLivingEntityByID(getLivingIDs().get(i).intValue());
                if (le != null) {
                    LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
                    if (lemi != null && lemi.getType() == LivingEntity.TYPE_CITIZEN) {
                        Citizen cit = (Citizen) le;
                        cit.getCitizenData().removeAllDeniedJobs();
                        cit.getCitizenData().addDeniedJobs(jobsDenied);
                    }
                }
            }
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V11) {
            id = in.readInt();
            name = (String) in.readObject();
            livingIDs = (ArrayList<Integer>) in.readObject();

            ArrayList<String> alStrings = (ArrayList<String>) in.readObject();
            if (alStrings == null || alStrings.size() == 0) {
                jobsDenied = null;
            } else {
                jobsDenied = new ArrayList<Integer>(alStrings.size());
                for (int i = 0; i < alStrings.size(); i++) {
                    jobsDenied.add(Integer.valueOf(UtilsIniHeaders.getIntIniHeader(alStrings.get(i))));
                }
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeObject(name);
        out.writeObject(livingIDs);
        // Pasamos a Strings
        out.writeObject(UtilsIniHeaders.getArrayStrings(jobsDenied));
    }
}
