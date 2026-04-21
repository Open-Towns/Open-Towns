package xaos.tiles.entities.living.projectiles;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.main.Game;
import xaos.tiles.Cell;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.LivingEntity;
import xaos.utils.Point3DShort;

public class Projectile extends Tile implements Externalizable {

    private static final long serialVersionUID = -5065819692431962137L;

    public final static int DIRECTION_EAST = 1;
    public final static int DIRECTION_NORTH = 2;
    public final static int DIRECTION_SOUTH = 4;
    public final static int DIRECTION_WEST = 8;

    private static byte locations[][][];

    private Point3DShort coordinates = Point3DShort.getPoolInstance(-1, -1, -1);
    private int damage;
    private int maxDistance;
    private LivingEntity attacker;
    private String attackerWeapon; // Para obtener los verbos después
    private LivingEntity victim;
    private ArrayList<Point3DShort> path;
    private int direction; // Binaria
    private boolean delete; // Se guarda para que se dibuje un turno más

    public Projectile() {
        super();
    }

    public Projectile(String sIniHeader) {
        super(sIniHeader);
    }

    public static byte[][][] getLocations() {
        return locations;
    }

    public static void setLocations(byte[][][] locations) {
        Projectile.locations = locations;
    }

    public void setCoordinates(Point3DShort point) {
        if (locations != null) {
            if (getX() != -1) { // Con esto sabemos si es un nuevo proyectil que aún no tiene coordenadas
                locations[getX()][getY()][getZ()]--;
            }
            locations[point.x][point.y][point.z]++;
        }

        this.coordinates = point;

        if (point.x != -1) { // No se si es realmente necesario
            ItemManagerItem imi = ItemManager.getItem(getIniHeader());
            if (imi != null && imi.getLightRadius() > 0) {
                Cell.generateLightsItemRemovedCellMined(point.x, point.y, point.z, imi.getLightRadius() + 1); // +1 para que borre lo de al lado (donde estaba antes)
            }
        }
    }

    public Point3DShort getCoordinates() {
        return coordinates;
    }

    public int getX() {
        return coordinates.x;
    }

    public int getY() {
        return coordinates.y;
    }

    public int getZ() {
        return coordinates.z;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public void setAttacker(LivingEntity attacker) {
        this.attacker = attacker;
    }

    public void setAttackerWeapon(String attackerWeapon) {
        this.attackerWeapon = attackerWeapon;
    }

    public String getAttackerWeapon() {
        return attackerWeapon;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public void setVictim(LivingEntity victim) {
        this.victim = victim;
    }

    public void setPath(ArrayList<Point3DShort> path) {
        this.path = path;

		// Miramos la dirección en la que hay que dibujar el ammo
        // Obtenemos el 1er y último puntos del recorrido para ello
        if (path != null && path.size() > 1) {
            Point3DShort p3dIni = path.get(0);
            Point3DShort p3dFin = path.get(path.size() - 1);

            // Miramos qué parte es más corta (servirá para girarlo bien en modo isométrico) (difícil de entender)
            boolean bParteCortaHorizontal = Math.abs(p3dIni.x - p3dFin.x) < Math.abs(p3dIni.y - p3dFin.y);

            if (bParteCortaHorizontal) {
                if (p3dIni.y < p3dFin.y) {
                    setDirection(DIRECTION_SOUTH | DIRECTION_EAST);
                } else {
                    setDirection(DIRECTION_NORTH | DIRECTION_WEST);
                }
            } else {
                if (p3dIni.x < p3dFin.x) {
                    setDirection(DIRECTION_NORTH | DIRECTION_EAST);
                } else {
                    setDirection(DIRECTION_SOUTH | DIRECTION_WEST);
                }
            }
        }
    }

    public ArrayList<Point3DShort> getPath() {
        return path;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isDelete() {
        return delete;
    }

    private boolean checkHit() {
        if (victim.getCoordinates().equals(getCoordinates())) {
            // Hit
            LivingEntity.doHit(getAttacker(), getVictim(), false, ItemManager.getItem(attackerWeapon), damage, maxDistance, true);
            return true;
        }

        return false;
    }

    public boolean nextTurn() {
        if (path.size() > 0) {
            Point3DShort nextPoint = path.remove(0);
            if (checkHit()) {
                return true;
            }
            setCoordinates(nextPoint);
            if (checkHit()) {
                return true;
            }

            return false;
        }

        LivingEntity.doHit(getAttacker(), getVictim(), false, ItemManager.getItem(attackerWeapon), damage, maxDistance, false);
        return true;
    }

    public void delete() {
        if (getX() != -1) {
            locations[getX()][getY()][getZ()]--;

            ItemManagerItem imi = ItemManager.getItem(getIniHeader());
            if (imi != null && imi.getLightRadius() > 0) {
                Cell.generateLightsItemRemovedCellMined(getX(), getY(), getZ(), imi.getLightRadius());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        coordinates = (Point3DShort) in.readObject();
        damage = in.readInt();
        maxDistance = in.readInt();
        attacker = (LivingEntity) in.readObject();
        if (attacker != null) {
            attacker.refreshTransients();
        }
        attackerWeapon = (String) in.readObject();
        victim = (LivingEntity) in.readObject();
        if (victim != null) {
            victim.refreshTransients();
        }
        path = (ArrayList<Point3DShort>) in.readObject();
        direction = in.readInt();
        if (Game.SAVEGAME_LOADING_VERSION <= Game.SAVEGAME_V10b) {
            in.readObject();
        }
        delete = in.readBoolean();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(coordinates);
        out.writeInt(damage);
        out.writeInt(maxDistance);
        out.writeObject(attacker);
        out.writeObject(attackerWeapon);
        out.writeObject(victim);
        out.writeObject(path);
        out.writeInt(direction);
        out.writeBoolean(delete);
    }
}
