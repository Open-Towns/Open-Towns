package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class TrapEffectData implements Externalizable {

    private static final long serialVersionUID = -1672326282666801539L;

    private int damage;

    public TrapEffectData() {
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        damage = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(damage);
    }
}
