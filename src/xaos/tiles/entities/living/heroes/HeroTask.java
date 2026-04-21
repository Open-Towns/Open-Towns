package xaos.tiles.entities.living.heroes;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class HeroTask implements Externalizable {

    private static final long serialVersionUID = -6266264964470429319L;

    public final static int TASK_NO_TASK = 0;
    public final static int TASK_RAGE = 1; // Héroe entra en furia y empieza a petar aldeanos
    public final static int TASK_LEAVING = 2; // Leaving the town
    public final static int TASK_DESTROY_BLOCKING = 3; // Destroys blocking items he found
    public final static int TASK_IDLE = 4;
    public final static int TASK_EXPLORING = 5;
    public final static int TASK_EATING = 6;
    public final static int TASK_SLEEPING = 7;
    public final static int TASK_EQUIPING = 8;

    private int taskID;

    public HeroTask() {
    }

    public HeroTask(int iID) {
        setTaskID(iID);
    }

    /**
     * @param taskID the taskID to set
     */
    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    /**
     * @return the taskID
     */
    public int getTaskID() {
        return taskID;
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        taskID = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(taskID);
    }
}
