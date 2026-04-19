package xaos.caravans;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.tiles.entities.items.Item;

public class CaravanItemDataInstance implements Externalizable {

    private static final long serialVersionUID = 7778543887802146317L;

    private Item item;
    private int price;
    private int quantity;

    public CaravanItemDataInstance() {
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        item = (Item) in.readObject();
        price = in.readInt();
        quantity = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(item);
        out.writeInt(price);
        out.writeInt(quantity);
    }
}
