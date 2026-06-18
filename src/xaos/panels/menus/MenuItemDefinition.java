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
    private final String condition;
    private final String textColorName;
    private final boolean transparent;

    public MenuItemDefinition(
            String id,
            String type,
            String titleKey,
            String targetMenu,
            String command,
            boolean dynamic,
            boolean maintainOpen,
            String provider,
            String key,
            String condition,
            String textColorName,
            boolean transparent

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
        this.condition = condition;
        this.textColorName = textColorName;
        this.transparent = transparent;
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

    public String getCondition() {
        return condition;
    }

    public String getTextColorName() {
        return textColorName;
    }
    public boolean isTransparent(){
        return transparent;
    }
}