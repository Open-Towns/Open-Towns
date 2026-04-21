package xaos.tiles.entities.living.heroes;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.main.World;

public class HeroBehaviour implements Externalizable {

    private static final long serialVersionUID = 5060453086539838303L;

    public final static int BEHAVIOUR_ID_NONE = 0;
    public final static int BEHAVIOUR_ID_IDLE = 1;
    public final static int BEHAVIOUR_ID_EXPLORE = 2;

    private int idlePCT;
    private int explorePCT;

    public HeroBehaviour() {
    }

    public int getIdlePCT() {
        return idlePCT;
    }

    public void setIdlePCT(int idlePCT) {
        this.idlePCT = idlePCT;
    }

    public void setIdlePCT(String sIdlePCT) throws Exception {
        setIdlePCT(Integer.parseInt(sIdlePCT));
    }

    public int getExplorePCT() {
        return explorePCT;
    }

    public void setExplorePCT(int explorePCT) {
        this.explorePCT = explorePCT;
    }

    public void setExplorePCT(String sExplorePCT) throws Exception {
        setExplorePCT(Integer.parseInt(sExplorePCT));
    }

    public boolean checkPCTs() {
        return (idlePCT + explorePCT) == 100;
    }

    /**
     * Devuelve el siguiente behaviour ID
     *
     * @param iBehaviorID ID actual
     * @param behaviour Behacviour data del héroe, para buscar el siguiente con
     * PCT > 0
     * @return el siguiente behaviour ID
     */
    public static int next(int iBehaviorID, HeroBehaviour behaviour) {
        if (iBehaviorID == BEHAVIOUR_ID_NONE) {
            // None -> idle
            iBehaviorID = BEHAVIOUR_ID_IDLE;
            if (behaviour.getIdlePCT() > 0) {
                return iBehaviorID;
            }
        }

        if (iBehaviorID == BEHAVIOUR_ID_IDLE) {
            // Idle -> Explore
            iBehaviorID = BEHAVIOUR_ID_EXPLORE;
            if (behaviour.getExplorePCT() > 0) {
                return iBehaviorID;
            }
        }

        if (iBehaviorID == BEHAVIOUR_ID_EXPLORE) {
            // Explore -> Idle
            iBehaviorID = BEHAVIOUR_ID_IDLE;
            if (behaviour.getIdlePCT() > 0) {
                return iBehaviorID;
            }
        }

        return BEHAVIOUR_ID_NONE; // No debería llegar nunca
    }

    /**
     * Returna el número de turnos a pasar en éste behaviour
     *
     * @param iBehaviourID ID del behaviour a mirar
     * @param behaviour Datos de behaviour
     * @return el número de turnos a pasar en éste behaviour
     */
    public static int getTurns(int iBehaviourID, HeroBehaviour behaviour) {
        int iMaxTurns = World.TIME_MODIFIER_DAY;
        int PCT = 0;
        switch (iBehaviourID) {
            case BEHAVIOUR_ID_IDLE:
                PCT = behaviour.getIdlePCT();
                break;
            case BEHAVIOUR_ID_EXPLORE:
                PCT = behaviour.getExplorePCT();
                break;
        }

        return (iMaxTurns * PCT) / 100;
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        idlePCT = in.readInt();
        explorePCT = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(idlePCT);
        out.writeInt(explorePCT);
    }
}
