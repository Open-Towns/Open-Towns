package xaos.actions;

import xaos.tiles.Tile;
import xaos.utils.Messages;

public class ActionPriorityManagerItem {

    private String id;
    private String name;
    private Tile icon;

    public ActionPriorityManagerItem(String sID) throws Exception {
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

    public void setIcon(String sIcon) {
        if (sIcon != null) {
            this.icon = new Tile(sIcon);
        }
    }

    public Tile getIcon() {
        return icon;
    }
}
