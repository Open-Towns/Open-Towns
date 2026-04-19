package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SkillData implements Externalizable {

    private static final long serialVersionUID = 5658923530822811121L;

    private String skillID;
    private int coolDown;
    private int use;

    public SkillData() {
    }

    public String getSkillID() {
        return skillID;
    }

    public void setSkillID(String skillID) {
        this.skillID = skillID;
    }

    public int getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
    }

    public void setUse(int use) {
        this.use = use;
    }

    public int getUse() {
        return use;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        skillID = (String) in.readObject();
        coolDown = in.readInt();
        use = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(skillID);
        out.writeInt(coolDown);
        out.writeInt(use);
    }
}
