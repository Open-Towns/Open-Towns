package xaos.tiles.entities.living.allies;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import xaos.tiles.entities.living.LivingEntity;

public class Ally extends LivingEntity implements Serializable {

    private static final long serialVersionUID = 6466277440410807905L;

    public Ally() {
        super();
    }

    public Ally(String sIniHeader) {
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
