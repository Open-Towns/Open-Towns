package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.living.LivingEntity;

public class CarryingData implements Externalizable {

    private static final long serialVersionUID = 3790135713233087786L;

    private Item carrying; // Objeto que el aldeano lleva encima
    private LivingEntity carryingLiving; // Living que el aldeano lleva encima

    public CarryingData() {
    }

    public Item getCarrying() {
        return carrying;
    }

    public void setCarrying(Item carrying) {
        this.carrying = carrying;
    }

    public LivingEntity getCarryingLiving() {
        return carryingLiving;
    }

    public void setCarryingLiving(LivingEntity carryingLiving) {
        this.carryingLiving = carryingLiving;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        carrying = (Item) in.readObject();
        carryingLiving = (LivingEntity) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(carrying);
        out.writeObject(carryingLiving);
    }
}
