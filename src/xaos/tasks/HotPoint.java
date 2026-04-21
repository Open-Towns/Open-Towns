package xaos.tasks;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.utils.Point3DShort;

/**
 * Clase usada por las tareas (Task's) para indicar donde hay que ir y sobre que
 * casilla actuar. Ej: Hay que ir a la posición 10,10,0 y minar la casilla
 * 10,11,0
 */
public final class HotPoint implements Externalizable {

    private static final long serialVersionUID = 7742440798076745420L;

    /**
     * Punto donde ejecutar la acción
     */
    private Point3DShort hotPoint;

    /**
     * Indica posibles puntos donde ir para actuar sobre el hotpoint
     */
    private ArrayList<Point3DShort> places;

    /**
     * Indica si el hotpoint está acabado
     */
    private boolean finished;

    /**
     * Se usa para indicar alguna ID o algo
     */
    private int parameter;

    public HotPoint() {
    }

    public HotPoint(Point3DShort hotPoint, ArrayList<Point3DShort> places) {
        setHotPoint(hotPoint);
        setPlaces(places, false);
    }

    public HotPoint(Point3DShort hotPoint, Point3DShort place) {
        setHotPoint(hotPoint);
        addPlace(place);
    }

    public Point3DShort getHotPoint() {
        return hotPoint;
    }

    public void setHotPoint(Point3DShort hotPoint) {
        this.hotPoint = hotPoint;
    }

    public ArrayList<Point3DShort> getPlaces() {
        return places;
    }

    public void setPlaces(ArrayList<Point3DShort> places, boolean maintainCurrentOrder) {
        if (maintainCurrentOrder && this.places != null && places != null) {
            // Mantenemos el orden, ya que otros aldeanos podrían estar haciendo cosas
            int iSize = this.places.size();
            for (int i = (iSize - 1); i >= 0; i--) {
                if (places.contains(this.places.get(i))) {
                    // Si ya existe lo borramos de la nueva lista
                    places.remove(this.places.get(i));
                } else {
                    // Si no existe lo quitamos de la lista actual
                    this.places.remove(i);
                }
            }

            // Una vez terminado metemos lo que queda en la lista
            for (int i = 0; i < places.size(); i++) {
                this.places.add(places.get(i));
            }
        } else {
            this.places = places;
        }
    }

    public void addPlace(Point3DShort point) {
        if (places == null) {
            places = new ArrayList<Point3DShort>();
        }

        places.add(point);
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setParameter(int parameter) {
        this.parameter = parameter;
    }

    public int getParameter() {
        return parameter;
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        hotPoint = (Point3DShort) in.readObject();
        places = (ArrayList<Point3DShort>) in.readObject();
        finished = in.readBoolean();
        parameter = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(hotPoint);
        out.writeObject(places);
        out.writeBoolean(finished);
        out.writeInt(parameter);
    }
}
