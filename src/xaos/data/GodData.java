package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class GodData implements Externalizable {

    private static final long serialVersionUID = 4936677475569870379L;

    private String godID;

    private boolean hidden;

    private String fullName;

    private int status;

    private int hoursLastEvent;

    public GodData(String sID) {
        setGodID(sID);
        setHidden(true);
        setStatus(50);
        setHoursLastEvent(0);
    }

    public void setGodID(String godID) {
        this.godID = godID;
    }

    public String getGodID() {
        return godID;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setStatus(int status) {
        if (status < 1) {
            status = 1;
        } else if (status > 100) {
            status = 100;
        }
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setHoursLastEvent(int hoursLastEvent) {
        this.hoursLastEvent = hoursLastEvent;
    }

    public int getHoursLastEvent() {
        return hoursLastEvent;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        godID = (String) in.readObject();
        hidden = in.readBoolean();
        fullName = (String) in.readObject();
        status = in.readInt();
        hoursLastEvent = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(godID);
        out.writeBoolean(hidden);
        out.writeObject(fullName);
        out.writeInt(status);
        out.writeInt(hoursLastEvent);
    }
}
