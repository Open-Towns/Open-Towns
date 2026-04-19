package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.entities.living.Citizen;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;

public class SoldierData implements Externalizable {

    private static final long serialVersionUID = 1632109198736549922L;

    // STATES
    public static final int STATE_NOT_A_SOLDIER = 0;
    public static final int STATE_GUARD = 1; // Default
    public static final int STATE_PATROL = 2;
    public static final int STATE_BOSS_AROUND = 3;
    public static final int STATE_IN_A_GROUP = 4;

    // PATROL
    public static final int COUNTER_MAX_PATROL_POINTS = 8;
    public static final int WAIT_TURNS_BETWEEN_PATROLS = (World.TIME_MODIFIER_HOUR / 2);

    // BOSS AROUND
    public static final int COUNTER_MAX_BOSS_AROUND = 64;
    public static final int BOOST_TURNS_BOSS_AROUND = 64; // Turnos que el aldeano correrá/currará más
    public static final int BOOST_PCT_BOSS_AROUND_WALK = 150; // A mayor % más correrán
    public static final int BOOST_PCT_BOSS_AROUND_WORK = 65; // A MENOR % menos turnos tardarán en hacer las tareas

    private int state;
    private int counter;
    private int targetID;
    private int group;

    // Patrol
    private ArrayList<Point3DShort> patrolPoints;
    private int patrolPointTarget;
    private int patrolWaitTime;

    // Level/XP
    private int level; // Nivel
    private int xp; // Experiencia

    public SoldierData() {
    }

    public SoldierData(int livingID) {
        setPatrolPoints(new ArrayList<Point3DShort>(COUNTER_MAX_PATROL_POINTS));
        resetData(livingID);
        setState(STATE_NOT_A_SOLDIER);
        setGroup(-1, livingID);
        setLevel(1);
        setXp(0);
    }

    public void resetData(int livingID) {
        setCounter(0);
        setTargetID(-1);
        setPatrolPointTarget(0);
        setPatrolWaitTime(WAIT_TURNS_BETWEEN_PATROLS);

        while (getPatrolPoints().size() > 0) {
            removePatrolPoint(getPatrolPoints().get(0));
        }
    }

    public int getState() {
        return state;
    }

    private void setState(int state) {
        this.state = state;
    }

    /**
     * Cambia el estado del soldier/citizen, y lo mueve de las listas si hace
     * falta
     *
     * @param state
     * @param livingID
     */
    public void setState(int state, int group, int livingID) {
        resetData(livingID);

        // Si pasa de soldier a civil o viceversa tenemos que actualizar los vectores
        if (this.state == STATE_NOT_A_SOLDIER || state == STATE_NOT_A_SOLDIER) {
            if (this.state != state) {
                if (this.state == STATE_NOT_A_SOLDIER) {
					// De CIVIL a SOLDADO
                    // Buscamos al civil
                    int iCivIndex = World.getCitizenIDs().indexOf(livingID);
                    if (iCivIndex != -1) { // Debería pasar SIEMPRE
                        World.getSoldierIDs().add(World.getCitizenIDs().remove(iCivIndex));
                    }
                } else {
					// De SOLDADO a CIVIL
                    // Buscamos al soldado
                    int iSoldierIndex = World.getSoldierIDs().indexOf(livingID);
                    if (iSoldierIndex != -1) { // Debería pasar SIEMPRE
                        World.getCitizenIDs().add(World.getSoldierIDs().remove(iSoldierIndex));
                    }
                }

                // XP / Level
                setLevel(1);
                setXp(0);
            }
        }

        this.state = state;

        if (state == STATE_IN_A_GROUP) {
            setGroup(group, livingID);
        } else {
            setGroup(-1, livingID);
        }
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void setTargetID(int targetID) {
        this.targetID = targetID;
    }

    public int getTargetID() {
        return targetID;
    }

    public void setGroup(int group, int livingID) {
        if (getState() == STATE_NOT_A_SOLDIER) {
            // Civil
            Game.getWorld().getSoldierGroups().removeSoldierFromGroup(livingID, this.group);
            this.group = group;
        } else {
            // Soldado
            boolean bDifferentGroups = this.group != group;

            if (bDifferentGroups) {
                // Lo eliminamos de la lista de groups en la que esté (si es que está)
                Game.getWorld().getSoldierGroups().removeSoldierFromGroup(livingID, this.group);

                this.group = group;

                // Lo añadimos a la lista que toque
                if (getState() != STATE_NOT_A_SOLDIER && bDifferentGroups) {
                    Game.getWorld().getSoldierGroups().addSoldierToGroup(livingID, this.group);
                }
            } else {
				// Mismo grupo, no hay que hacer nada
                // Si pasa de aldeano a soldado podría ser que sea el mismo grupo (-1), así que miramos eso y lo añadimos a la lista de soldados sin grupo
                if (!Game.getWorld().getSoldierGroups().getSoldiersWithoutGroup().contains(Integer.valueOf(livingID))) {
                    Game.getWorld().getSoldierGroups().addSoldierToGroup(livingID, this.group);
                }
            }
        }
    }

    public int getGroup() {
        return group;
    }

    public void setPatrolPoints(ArrayList<Point3DShort> patrolPoints) {
        this.patrolPoints = patrolPoints;
    }

    public ArrayList<Point3DShort> getPatrolPoints() {
        return patrolPoints;
    }

    public void setPatrolPointTarget(int patrolPointTarget) {
        this.patrolPointTarget = patrolPointTarget;
    }

    public int getPatrolPointTarget() {
        return patrolPointTarget;
    }

    public void addPatrolPoint(Point3D p3d) {
        if (getPatrolPoints().size() == SoldierData.COUNTER_MAX_PATROL_POINTS) {
            getPatrolPoints().remove(0);
        }

        getPatrolPoints().add(Point3DShort.getPoolInstance(p3d.x, p3d.y, p3d.z));

        World.getCell(p3d).setFlagPatrol(true);
    }

    public void removePatrolPoint(Point3DShort p3d) {
        getPatrolPoints().remove(p3d);
        World.checkFlagPatrolPoint(p3d);
    }

    public void setPatrolWaitTime(int patrolWaitTime) {
        this.patrolWaitTime = patrolWaitTime;
    }

    public int getPatrolWaitTime() {
        return patrolWaitTime;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getXp() {
        return xp;
    }

    /**
     * Devuelve la experiencia que lleva en el nivel (en %)
     *
     * @return
     */
    public int getXpPCT() {
        if (getLevel() > 0) {
            return ((getXp() % 300)) / 3;
        }

        return 0;
    }

    /**
     * Mira si tiene que subir de nivel (o niveles), en ese caso lo hace y
     * devuelve true.
     *
     * @return
     */
    public boolean checkAndUpdateLevelUp(Citizen citizen) {
        if (getLevel() < 50 && getXp() >= (300 * getLevel())) {
            while (getLevel() < 50 && getXp() >= (300 * getLevel())) {
                setLevel(getLevel() + 1);
            }
            return true;
        }

        return false;
    }

    public boolean isSoldier() {
        return getState() != STATE_NOT_A_SOLDIER;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        state = in.readInt();
        counter = in.readInt();
        targetID = in.readInt();
        group = in.readInt();
        patrolPoints = (ArrayList<Point3DShort>) in.readObject();
        patrolPointTarget = in.readInt();
        patrolWaitTime = in.readInt();

        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V14) {
            level = in.readInt();
            xp = in.readInt();
        } else {
            level = 0;
            xp = 0;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(state);
        out.writeInt(counter);
        out.writeInt(targetID);
        out.writeInt(group);
        out.writeObject(patrolPoints);
        out.writeInt(patrolPointTarget);
        out.writeInt(patrolWaitTime);
        out.writeInt(level);
        out.writeInt(xp);
    }
}
