package xaos.actions;

import java.util.ArrayList;

import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.terrain.TerrainManager;
import xaos.utils.Messages;
import xaos.utils.Utils;

public class ActionManagerItem {

    private String id;
    private String name;
    private String priorityID;
    private int turns;
    private String effect;
    private boolean killsSource;
    private ArrayList<QueueItem> queue;
    private String generatedItem; // For queues
    private boolean inverted;

    public ActionManagerItem(String sID) throws Exception {
        setId(sID);
    }

    public void setId(String sID) throws Exception {
        if (sID == null || sID.trim().length() == 0) {
            throw new Exception(Messages.getString("ActionManagerItem.0")); //$NON-NLS-1$
        }

        this.id = sID;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPriorityID(String priorityID) {
        this.priorityID = priorityID;
    }

    public String getPriorityID() {
        return priorityID;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public void setTurns(String turns) {
        setTurns(Utils.getInteger(turns, 0));
    }

    public int getTurns() {
        return turns;
    }

    public void setEffect(String effect) throws Exception {
        // Comprobamos que el effect exista
        if (effect != null) {
            if (ItemManager.getItem(effect) == null) {
                if (TerrainManager.getItem(effect) == null) {
                    throw new Exception(Messages.getString("ActionManagerItem.1") + effect + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        this.effect = effect;
    }

    public String getEffect() {
        return effect;
    }

    public void setKillsSource(boolean killsSource) {
        this.killsSource = killsSource;
    }

    public void setKillsSource(String killsSource) {
        setKillsSource(Boolean.parseBoolean(killsSource));
    }

    public boolean isKillsSource() {
        return killsSource;
    }

    public void setQueue(ArrayList<QueueItem> queue) throws Exception {
        if (queue == null || queue.size() == 0) {
            throw new Exception(Messages.getString("ActionManagerItem.5")); //$NON-NLS-1$
        }

        this.queue = queue;

        if (getName() == null) {
            // Miramos si tiene generated item, en ese caso ponemos ese nombre en el tooltip
            if (getGeneratedItem() != null && getGeneratedItem().length() > 0) {
                ItemManagerItem imi = ItemManager.getItem(getGeneratedItem());
                if (imi != null) {
                    setName(imi.getName());
                }
            } else {
				// Si no tiene generated item buscamos un <createItem>, empezando por el final
                // Buscamos el item generado "tag <createItem>"
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).getType() == QueueItem.TYPE_CREATE_ITEM) {
                        setGeneratedItem(queue.get(i).getValue());
                        if (getName() == null || getName().trim().length() == 0) {

                            ItemManagerItem imi = ItemManager.getItem(queue.get(i).getValue());
                            if (imi != null) {
                                setName(imi.getName());
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public ArrayList<QueueItem> getQueue() {
        if (queue == null) {
            return null;
        }

        // Devolvemos una copia
        ArrayList<QueueItem> alReturn = new ArrayList<QueueItem>();
        for (int i = 0; i < queue.size(); i++) {
            alReturn.add(queue.get(i).copy());
        }

        return alReturn;
    }

    public ArrayList<QueueItem> getQueueNoCopy() {
        return queue;
    }

    public void setGeneratedItem(String generatedItem) {
        this.generatedItem = generatedItem;
    }

    public String getGeneratedItem() {
        return generatedItem;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public void setInverted(String sInverted) {
        setInverted(Boolean.parseBoolean(sInverted));
    }

    public boolean isInverted() {
        return inverted;
    }
}
