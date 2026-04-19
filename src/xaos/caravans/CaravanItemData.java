package xaos.caravans;

import xaos.tiles.entities.items.ItemManager;
import xaos.utils.Messages;

public class CaravanItemData {

    private String id;
    private String type;
    private int PCT;
    private String quantity;

    public String getId() {
        return id;
    }

    public void setId(String id) throws Exception {
        if (id != null) {
            if (ItemManager.getItem(id) == null) {
                throw new Exception(Messages.getString("CaravanItemData.1") + id + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        this.id = id;
    }

    public void setType(String type) throws Exception {
        if (type != null) {
            if (ItemManager.getRandomItemByType(type) == null) {
                throw new Exception(Messages.getString("CaravanItemData.2") + type + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getPCT() {
        return PCT;
    }

    public void setPCT(int iPCT) {
        PCT = iPCT;
        if (PCT < 1) {
            PCT = 1;
        } else if (PCT > 100) {
            PCT = 100;
        }
    }

    public void setPCT(String sPCT) throws Exception {
        try {
            setPCT(Integer.parseInt(sPCT));
        } catch (Exception e) {
            throw new Exception(Messages.getString("CaravanItemData.0") + sPCT + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
