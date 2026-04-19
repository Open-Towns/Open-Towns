package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class FocusData implements Externalizable {

    private static final long serialVersionUID = -609789119350379884L;

    private int entityID;
    private int entityType;

    public FocusData() {
    }

    public FocusData(int ID, int type) {
        setEntityID(ID);
        setEntityType(type);
    }

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        entityID = in.readInt();
        entityType = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(entityID);
        out.writeInt(entityType);
    }
}
