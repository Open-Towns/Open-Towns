package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.tiles.Tile;

public class GlobalEventData implements Externalizable {

    private static final long serialVersionUID = 1943236493343125044L;

    private static Tile TILE_EVENT = new Tile("icon_events");

    private boolean shadows;
    private boolean halfShadows;
    private float red;
    private float green;
    private float blue;
    private int waitPCT; // Suma (multiplicación) de todos los PCT activos
    private int walkSpeedPCT; // Suma (multiplicación) de todos los PCT activos
    private static Tile icon; // Toma el valor del icono del primer evento (o "icon_events", "icon_eventsON");

    public GlobalEventData() {
        reset();
    }

    public void reset() {
        setShadows(false);
        setHalfShadows(false);
        setRed(0);
        setGreen(0);
        setBlue(0);
        setWaitPCT(100);
        setWalkSpeedPCT(100);

        setIcon();
    }

    public void setShadows(boolean shadows) {
        this.shadows = shadows;
    }

    public boolean isShadows() {
        return shadows;
    }

    public void setHalfShadows(boolean halfShadows) {
        this.halfShadows = halfShadows;
    }

    public boolean isHalfShadows() {
        return halfShadows;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getRed() {
        return red;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getGreen() {
        return green;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public float getBlue() {
        return blue;
    }

    public void setWaitPCT(int waitPCT) {
        this.waitPCT = waitPCT;
    }

    public int getWaitPCT() {
        return waitPCT;
    }

    public void setWalkSpeedPCT(int walkSpeedPCT) {
        this.walkSpeedPCT = walkSpeedPCT;
    }

    public int getWalkSpeedPCT() {
        return walkSpeedPCT;
    }

    public static Tile getIcon() {
        if (icon == null) {
            setIcon();
        }

        return icon;
    }

    public static void setIcon() {
        if (Game.getWorld() != null && Game.getWorld().getEvents() != null) {
            EventManagerItem emi;
            for (int i = 0; i < Game.getWorld().getEvents().size(); i++) {
                emi = EventManager.getItem(Game.getWorld().getEvents().get(i).getEventID());
                if (emi != null && emi.getIcon() != null) {
                    icon = emi.getIcon();
                    return;
                }
            }
        }

        // Si llega aquí es que aún no está inicializado o no hay eventos, metemos el default
        icon = TILE_EVENT;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        shadows = in.readBoolean();
        halfShadows = in.readBoolean();
        red = in.readFloat();
        green = in.readFloat();
        blue = in.readFloat();
        waitPCT = in.readInt();
        walkSpeedPCT = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(shadows);
        out.writeBoolean(halfShadows);
        out.writeFloat(red);
        out.writeFloat(green);
        out.writeFloat(blue);
        out.writeInt(waitPCT);
        out.writeInt(walkSpeedPCT);
    }
}
