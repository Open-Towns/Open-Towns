package xaos.panels.menus;

public final class MenuItemDefinition {

    private final String id;
    private final String type;
    private final String titleKey;
    private final String targetMenu;
    private final String command;
    private final boolean dynamic;
    private final boolean maintainOpen;
    private final String provider;
    private final String key;

    public MenuItemDefinition(
            String id,
            String type,
            String titleKey,
            String targetMenu,
            String command,
            boolean dynamic,
            boolean maintainOpen,
            String provider,
            String key
    ) {
        this.id = id;
        this.type = type;
        this.titleKey = titleKey;
        this.targetMenu = targetMenu;
        this.command = command;
        this.dynamic = dynamic;
        this.maintainOpen = maintainOpen;
        this.provider = provider;
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getProvider() {
        return provider;
    }

    public String getType() {
        return type;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public String getTargetMenu() {
        return targetMenu;
    }

    public String getCommand() {
        return command;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isMaintainOpen() {
        return maintainOpen;
    }
}