package xaos.panels;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.utils.ColorGL;
import xaos.utils.Point3DShort;
import xaos.utils.UtilFont;

public final class MessagesPanelData implements Externalizable {

    private static final long serialVersionUID = 6983348450572822925L;

    private String message;
    private ColorGL color;
    private Point3DShort view;
    private int entityID;
    ;
	private int width;

    public MessagesPanelData() {
    }

    public MessagesPanelData(String message, ColorGL color, Point3DShort view, int iEntityID) {
        setMessage(message);
        setColor(color);
        setView(view);
        setEntityID(iEntityID);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;

        if (message != null) {
            setWidth(UtilFont.getWidth(message));
        } else {
            setWidth(0);
        }
    }

    public ColorGL getColor() {
        return color;
    }

    public void setColor(ColorGL color) {
        this.color = color;
    }

    public Point3DShort getView() {
        return view;
    }

    public void setView(Point3DShort view) {
        if (view != null && view.x != -1) {
            this.view = Point3DShort.getPoolInstance(view);
        } else {
            this.view = view;
        }
    }

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        message = (String) in.readObject();
        color = (ColorGL) in.readObject();
        view = (Point3DShort) in.readObject();
        entityID = in.readInt();
        width = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(message);
        out.writeObject(color);
        out.writeObject(view);
        out.writeInt(entityID);
        out.writeInt(width);
    }
}
