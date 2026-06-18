package xaos.panels.menus;

import java.util.ArrayList;
import java.util.List;

public final class MenuDefinition {

    private final String id;
    private final String titleKey;
    private final List<MenuItemDefinition> items = new ArrayList<>();
    private final boolean transparent = true;

    public MenuDefinition(String id, String titleKey) {
        this.id = id;
        this.titleKey = titleKey;
    }

    public String getId() {
        return id;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public List<MenuItemDefinition> getItems() {
        return items;
    }

    public void addItem(MenuItemDefinition item) {
        items.add(item);
    }

    public boolean isTransparent() {
        return transparent;
    }

}