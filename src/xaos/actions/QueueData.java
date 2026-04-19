package xaos.actions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Para guardar datos temporales de las colas (lo usan los aldeanos)
 */
public class QueueData implements Externalizable {

    private static final long serialVersionUID = -2130572372975665616L;

    private int itemIDCurrentPlace; // ID del item que debe estar en la currentPlace (se usa para el MOVE)
    private int itemIDPick; // ID del item que va a pillar (se usa para el PICK)
    private int livingIDPick; // ID del living que va a pillar (se usa para el PICK_LIVING)
    private int waitCounter;
    private QueueItem lastQueueItem;

    public QueueData() {
        setItemIDCurrentPlace(-1);
        setItemIDPick(-1);
        setWaitCounter(-1);
        setLastQueueItem(null);
    }

    public int getItemIDCurrentPlace() {
        return itemIDCurrentPlace;
    }

    public void setItemIDCurrentPlace(int itemIDCurrentPlace) {
        this.itemIDCurrentPlace = itemIDCurrentPlace;
    }

    public void setItemIDPick(int itemIDPick) {
        this.itemIDPick = itemIDPick;
    }

    public int getItemIDPick() {
        return itemIDPick;
    }

    public void setLivingIDPick(int livingIDPick) {
        this.livingIDPick = livingIDPick;
    }

    public int getLivingIDPick() {
        return livingIDPick;
    }

    public void setWaitCounter(int waitCounter) {
        this.waitCounter = waitCounter;
    }

    public int getWaitCounter() {
        return waitCounter;
    }

    public void setLastQueueItem(QueueItem lastQueueItem) {
        this.lastQueueItem = lastQueueItem;
    }

    public QueueItem getLastQueueItem() {
        return lastQueueItem;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        itemIDCurrentPlace = in.readInt();
        itemIDPick = in.readInt();
        livingIDPick = in.readInt();
        waitCounter = in.readInt();
        lastQueueItem = (QueueItem) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(itemIDCurrentPlace);
        out.writeInt(itemIDPick);
        out.writeInt(livingIDPick);
        out.writeInt(waitCounter);
        out.writeObject(lastQueueItem);
    }
}
