package xaos.actions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.main.Game;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.utils.Messages;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;

public class QueueItem implements Externalizable {

    private static final long serialVersionUID = -5975052832269092344L;

    private final static String TYPE_STR_PICK = "pick"; //$NON-NLS-1$
    private final static String TYPE_STR_PICK_FRIENDLY = "pickFriendly"; //$NON-NLS-1$
    private final static String TYPE_STR_MOVE = "move"; //$NON-NLS-1$
    private final static String TYPE_STR_WAIT = "wait"; //$NON-NLS-1$
    private final static String TYPE_STR_DESTROY_ITEM = "destroyItem"; //$NON-NLS-1$
    private final static String TYPE_STR_DESTROY_CELL_ITEM = "destroyCellItem"; //$NON-NLS-1$
    private final static String TYPE_STR_DESTROY_FRIENDLY = "destroyFriendly"; //$NON-NLS-1$
    private final static String TYPE_STR_LOCK = "lock"; //$NON-NLS-1$
    private final static String TYPE_STR_UNLOCK = "unlock"; //$NON-NLS-1$
    private final static String TYPE_STR_CREATE_ITEM = "createItem"; //$NON-NLS-1$
    private final static String TYPE_STR_CREATE_ITEM_BY_TYPE = "createItemByType"; //$NON-NLS-1$
    private final static String TYPE_STR_CREATE_FRIENDLY = "createFriendly"; //$NON-NLS-1$
    private final static String TYPE_STR_REPLACE_CELL_ITEM = "replaceCellItem"; //$NON-NLS-1$
    private final static String TYPE_STR_CHANGE_TERRAIN = "changeTerrain"; //$NON-NLS-1$
    private final static String TYPE_STR_MOVE_TERRAIN = "moveTerrain"; //$NON-NLS-1$
    private final static String TYPE_STR_REVIVE_HEROES = "reviveHeroes"; //$NON-NLS-1$
    private final static String TYPE_STR_DELETE_COINS = "deleteCoins"; //$NON-NLS-1$
    private final static String TYPE_STR_DELETE_COINS_PCT = "deleteCoinsPCT"; //$NON-NLS-1$
    private final static String TYPE_STR_ADD_GOD_STATUS = "addGodStatus"; //$NON-NLS-1$

    public final static int TYPE_PICK = 1;
    public final static int TYPE_MOVE = 2;
    public final static int TYPE_WAIT = 3;
    public final static int TYPE_DESTROY_ITEM = 4;
    public final static int TYPE_UNLOCK = 5;
    public final static int TYPE_CREATE_ITEM = 6;
    public final static int TYPE_PLACE_ITEM = 7; // Código interno, lo usan los aldeanos
    public final static int TYPE_PICK_FRIENDLY = 8;
    public final static int TYPE_DESTROY_FRIENDLY = 9;
    public final static int TYPE_CREATE_FRIENDLY = 10;
    public final static int TYPE_DESTROY_CELL_ITEM = 11;
    public final static int TYPE_REPLACE_CELL_ITEM = 12;
    public final static int TYPE_CHANGE_TERRAIN = 13;
    public final static int TYPE_MOVE_TERRAIN = 14;
    public final static int TYPE_CREATE_ITEM_BY_TYPE = 15;
    public final static int TYPE_LOCK = 16;
    public final static int TYPE_REVIVE_HEROES = 17;
    public final static int TYPE_DELETE_COINS = 18;
    public final static int TYPE_DELETE_COINS_PCT = 19;
    public final static int TYPE_ADD_GOD_STATUS = 20;

    private int type;
    private String value;
    private boolean useSource;
    private String fx;
    private int fxTurns;
    private int godStatus;

    public QueueItem() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setType(String sType) throws Exception {
        if (sType == null || sType.length() == 0) {
            throw new Exception(Messages.getString("QueueItem.5")); //$NON-NLS-1$
        }

        if (sType.equalsIgnoreCase(TYPE_STR_PICK)) {
            setType(TYPE_PICK);
        } else if (sType.equalsIgnoreCase(TYPE_STR_MOVE)) {
            setType(TYPE_MOVE);
        } else if (sType.equalsIgnoreCase(TYPE_STR_WAIT)) {
            setType(TYPE_WAIT);
        } else if (sType.equalsIgnoreCase(TYPE_STR_DESTROY_ITEM)) {
            setType(TYPE_DESTROY_ITEM);
        } else if (sType.equalsIgnoreCase(TYPE_STR_DESTROY_FRIENDLY)) {
            setType(TYPE_DESTROY_FRIENDLY);
        } else if (sType.equalsIgnoreCase(TYPE_STR_UNLOCK)) {
            setType(TYPE_UNLOCK);
        } else if (sType.equalsIgnoreCase(TYPE_STR_LOCK)) {
            setType(TYPE_LOCK);
        } else if (sType.equalsIgnoreCase(TYPE_STR_CREATE_ITEM)) {
            setType(TYPE_CREATE_ITEM);
        } else if (sType.equalsIgnoreCase(TYPE_STR_CREATE_ITEM_BY_TYPE)) {
            setType(TYPE_CREATE_ITEM_BY_TYPE);
        } else if (sType.equalsIgnoreCase(TYPE_STR_PICK_FRIENDLY)) {
            setType(TYPE_PICK_FRIENDLY);
        } else if (sType.equalsIgnoreCase(TYPE_STR_CREATE_FRIENDLY)) {
            setType(TYPE_CREATE_FRIENDLY);
        } else if (sType.equalsIgnoreCase(TYPE_STR_DESTROY_CELL_ITEM)) {
            setType(TYPE_DESTROY_CELL_ITEM);
        } else if (sType.equalsIgnoreCase(TYPE_STR_REPLACE_CELL_ITEM)) {
            setType(TYPE_REPLACE_CELL_ITEM);
        } else if (sType.equalsIgnoreCase(TYPE_STR_CHANGE_TERRAIN)) {
            setType(TYPE_CHANGE_TERRAIN);
        } else if (sType.equalsIgnoreCase(TYPE_STR_MOVE_TERRAIN)) {
            setType(TYPE_MOVE_TERRAIN);
        } else if (sType.equalsIgnoreCase(TYPE_STR_REVIVE_HEROES)) {
            setType(TYPE_REVIVE_HEROES);
        } else if (sType.equalsIgnoreCase(TYPE_STR_DELETE_COINS)) {
            setType(TYPE_DELETE_COINS);
        } else if (sType.equalsIgnoreCase(TYPE_STR_DELETE_COINS_PCT)) {
            setType(TYPE_DELETE_COINS_PCT);
        } else if (sType.equalsIgnoreCase(TYPE_STR_ADD_GOD_STATUS)) {
            setType(TYPE_ADD_GOD_STATUS);
        } else {
            throw new Exception(Messages.getString("QueueItem.6") + sType + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public String getValue() {
        return value;
    }

    public void setValueNoException(String value) {
        this.value = value;
    }

    public void setValue(String value) throws Exception {
        // Comprobamos que el value sea correcto
        if (value != null) {
            if (getType() == TYPE_CREATE_ITEM) {
                if (ItemManager.getItem(value) == null) {
                    throw new Exception(Messages.getString("QueueItem.0") + value + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (getType() == TYPE_MOVE || getType() == TYPE_PICK || getType() == TYPE_PICK_FRIENDLY || getType() == TYPE_LOCK) {
                ArrayList<String> alTokens = Utils.getArray(value);

                if (alTokens != null) {
                    for (int i = 0; i < alTokens.size(); i++) {
                        if (getType() == TYPE_PICK || getType() == TYPE_MOVE || getType() == TYPE_LOCK) {
                            if (ItemManager.getItem(alTokens.get(i)) == null) {
                                throw new Exception(Messages.getString("QueueItem.0") + alTokens.get(i) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } else {
                            if (LivingEntityManager.getItem(alTokens.get(i)) == null) {
                                throw new Exception(Messages.getString("QueueItem.0") + alTokens.get(i) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                }
            } else if (getType() == TYPE_CREATE_FRIENDLY) {
                if (LivingEntityManager.getItem(value) == null) {
                    throw new Exception(Messages.getString("QueueItem.0") + value + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (getType() == TYPE_CREATE_ITEM_BY_TYPE) {
                if (!ItemManager.existItemsByType(value)) {
                    throw new Exception(Messages.getString("QueueItem.0") + value + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        this.value = value;
    }

    /**
     * @param useSource the useSource to set
     */
    public void setUseSource(boolean useSource) {
        this.useSource = useSource;
    }

    /**
     * @return the useSource
     */
    public boolean isUseSource() {
        return useSource;
    }

    public void setFxNoException(String fx) {
        this.fx = fx;
    }

    public void setFx(String fx) throws Exception {
        if (fx != null && !UtilsAL.exists(fx)) {
            throw new Exception(Messages.getString("QueueItem.1") + " [" + fx + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        this.fx = fx;
    }

    public String getFx() {
        return fx;
    }

    public void setFxTurns(int fxTurns) {
        this.fxTurns = fxTurns;
    }

    public void setFxTurns(String sFxTurns) {
        try {
            this.fxTurns = Integer.parseInt(sFxTurns);
        } catch (Exception e) {
            this.fxTurns = 0;
        }

        if (this.fxTurns < 0) {
            this.fxTurns = 0;
        }
    }

    public int getFxTurns() {
        return fxTurns;
    }

    public void setGodStatus(int godStatus) {
        this.godStatus = godStatus;
    }

    public void setGodStatus(String sGodStatus) {
        try {
            this.godStatus = Integer.parseInt(sGodStatus);
        } catch (Exception e) {
            this.godStatus = 0;
        }

        if (this.godStatus < 0) {
            this.godStatus = 0;
        }
    }

    public int getGodStatus() {
        return godStatus;
    }

    /**
     * Devuelve una copia de si mismo
     *
     * @return
     */
    public QueueItem copy() {
        QueueItem qi = new QueueItem();
        qi.setType(getType());
        qi.setValueNoException(getValue());
        qi.setUseSource(isUseSource());
        qi.setFxNoException(getFx());
        qi.setFxTurns(getFxTurns());
        qi.setGodStatus(getGodStatus());
        return qi;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type = in.readInt();
        value = (String) in.readObject();
        useSource = in.readBoolean();
        fx = (String) in.readObject();
        fxTurns = in.readInt();

        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V14) {
            godStatus = in.readInt();
        } else {
            godStatus = 0;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(type);
        out.writeObject(value);
        out.writeBoolean(useSource);
        out.writeObject(fx);
        out.writeInt(fxTurns);
        out.writeInt(godStatus);
    }
}
