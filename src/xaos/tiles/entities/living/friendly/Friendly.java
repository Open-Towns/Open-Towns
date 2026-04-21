package xaos.tiles.entities.living.friendly;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.tiles.entities.living.LivingEntity;

public class Friendly extends LivingEntity implements Externalizable {

    private static final long serialVersionUID = -7742274154801728614L;

    public Friendly() {
        super();
    }

    public Friendly(String sIniHeader) {
        super(sIniHeader);
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }
}
