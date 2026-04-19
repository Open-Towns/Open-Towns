package xaos.actions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.main.Game;
import xaos.tiles.entities.items.Item;
import xaos.utils.Point3DShort;

public class Action implements Externalizable {

    private static final long serialVersionUID = 2857246074554917976L;

    private String id;
    private int turns;
    private Point3DShort terrainPoint; // Punto de terreno para las tareas "TERRAIN"
    private Point3DShort destinationPoint; // Punto para las tareas queue and place
    private int entityID; // Entidad a la que hace referencia (item o living), para las tareas "ITEM" y "LIVING"
    private boolean silent; // Indica si saca mensajes de error en caso de no poderse hacer
    private int face = Item.FACE_WEST; // Indica si hay que rotar el item

    // Queue
    private ArrayList<QueueItem> queue; // Cola de tareas
    private QueueData queueData;

    public Action() {
    }

    public Action(String sID) {
        setId(sID);
        setEntityID(-1);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public void setTerrainPoint(Point3DShort terrainPoint) {
        this.terrainPoint = terrainPoint;
    }

    public Point3DShort getTerrainPoint() {
        return terrainPoint;
    }

    public void setDestinationPoint(Point3DShort destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    public Point3DShort getDestinationPoint() {
        return destinationPoint;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    public int getEntityID() {
        return entityID;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isSilent() {
        return silent;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setQueue(ArrayList<QueueItem> queue) {
        this.queue = queue;
    }

    public ArrayList<QueueItem> getQueue() {
        return queue;
    }

    public void setQueueData(QueueData queueData) {
        this.queueData = queueData;
    }

    public QueueData getQueueData() {
        return queueData;
    }

    public boolean execute() {
        if (getTurns() > 0) {
            setTurns(getTurns() - 1);
        }

        return getTurns() <= 0;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("Action\n");
        buffer.append("ID " + id + " turns " + turns + " terrain " + terrainPoint + " destPoint " + destinationPoint + " entityID " + entityID + "\n");
        return buffer.toString();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (String) in.readObject();
        turns = in.readInt();
        terrainPoint = (Point3DShort) in.readObject();
        destinationPoint = (Point3DShort) in.readObject();
        entityID = in.readInt();
        silent = in.readBoolean();
        queue = (ArrayList<QueueItem>) in.readObject();
        queueData = (QueueData) in.readObject();

        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V14) {
            face = in.readInt();
        } else {
            face = Item.FACE_WEST;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeInt(turns);
        out.writeObject(terrainPoint);
        out.writeObject(destinationPoint);
        out.writeInt(entityID);
        out.writeBoolean(silent);
        out.writeObject(queue);
        out.writeObject(queueData);
        out.writeInt(face);
    }
}
