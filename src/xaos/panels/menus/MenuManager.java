package xaos.panels.menus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.panels.MainMenuPanel;
import xaos.utils.LanguageData;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Utils;
import xaos.utils.UtilsKeyboard;

public final class MenuManager {

    private final Map<String, MenuDefinition> menusById = new HashMap<>();

    public void loadMenus(File rootFolder) {
        if (rootFolder == null || !rootFolder.exists()) {
            throw new IllegalArgumentException("Menu folder does not exist: " + rootFolder);
        }

        if (!rootFolder.isDirectory()) {
            throw new IllegalArgumentException("Menu path is not a folder: " + rootFolder);
        }

        menusById.clear();
        loadMenusRecursive(rootFolder);

    }

    private void loadMenusRecursive(File folder) {
        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                loadMenusRecursive(file);
                continue;
            }

            if (!file.getName().toLowerCase().endsWith(".xml")) {
                continue;
            }

            MenuDefinition menu = MenuXmlLoader.load(file);

            if (menusById.containsKey(menu.getId())) {
                throw new IllegalArgumentException(
                        "Duplicate menu id found: " + menu.getId()
                                + " in file: " + file.getPath());
            }

            menusById.put(menu.getId(), menu);
        }
    }

    public MenuDefinition getMenu(String id) {
        return menusById.get(id);
    }

    public boolean hasMenu(String id) {
        return menusById.containsKey(id);
    }

    public Map<String, MenuDefinition> getMenusById() {
        return menusById;
    }

    public void clear() {
        menusById.clear();
    }

    public SmartMenu buildSmartMenu(String menuId, SmartMenu parent) {
        MenuDefinition definition = menusById.get(menuId);

        if (definition == null) {
            throw new IllegalArgumentException("Menu not found: " + menuId);
        }

        return buildSmartMenu(definition, parent, new HashSet<String>());
    }

    public SmartMenu buildSmartMenu(MenuDefinition definition, SmartMenu parent) {
        return buildSmartMenu(definition, parent, new HashSet<String>());
    }

    private SmartMenu buildSmartMenu(MenuDefinition definition, SmartMenu parent, Set<String> buildStack) {
        if (definition == null) {
            throw new IllegalArgumentException("Cannot build null menu definition.");
        }

        if (buildStack.contains(definition.getId())) {
            throw new IllegalArgumentException("Circular menu reference detected: " + definition.getId());
        }

        buildStack.add(definition.getId());

        SmartMenu menu = new SmartMenu(
                SmartMenu.TYPE_MENU,
                Messages.getString(definition.getTitleKey()),
                parent,
                null,
                null,
                null,
                null, null);

        for (MenuItemDefinition item : definition.getItems()) {

            buildSmartMenuItems(item, menu, buildStack);

        }

        buildStack.remove(definition.getId());

        return menu;
    }

    private SmartMenu buildSmartMenuItem(MenuItemDefinition item, SmartMenu parent, Set<String> buildStack) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot build null menu item.");
        }

        String label = item.getTitleKey() == null
                ? ""
                : Messages.getString(item.getTitleKey());

        if ("submenu".equals(item.getType())) {
            return buildSubmenuItem(item, parent, buildStack);
        }
        if ("keyboard".equals(item.getType())) {
            int keyboardKey = resolveKeyboardKey(item.getKey());
            if (keyboardKey == -1) {
                throw new IllegalArgumentException("Keyboard item is missing or has invalid key: " + item.getId());
            } else {
                return MainMenuPanel.createKeyboardMenu(keyboardKey, MainMenuPanel.textColor,
                        MainMenuPanel.borderColor);
            }

        }

        if ("back".equals(item.getType())) {
            return new SmartMenu(
                    SmartMenu.TYPE_ITEM,
                    Messages.getString("MainMenuPanel.7"),
                    parent,
                    CommandPanel.COMMAND_BACK,
                    null,
                    null,
                    null, null);
        }
        if ("spacer".equals(item.getType())) {
            return new SmartMenu(
                    SmartMenu.TYPE_TEXT,
                    null,
                    parent,
                    null,
                    null,
                    null,
                    null, null);
        }

        int smartMenuType = toSmartMenuType(item.getType());

        return new SmartMenu(
                smartMenuType,
                label,
                parent,
                resolveCommandKey(item.getCommand()),
                null,
                null,
                null, null);

    }

    private SmartMenu buildSubmenuItem(MenuItemDefinition item, SmartMenu parent, Set<String> buildStack) {
        String targetMenuId = item.getTargetMenu();

        if (targetMenuId == null || targetMenuId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Submenu item is missing targetMenu. Item id: " + item.getId());
        }

        MenuDefinition targetMenu = menusById.get(targetMenuId);

        if (targetMenu == null) {
            throw new IllegalArgumentException(
                    "Target menu not found: " + targetMenuId
                            + " for submenu item: " + item.getId());
        }

        return buildSmartMenu(targetMenu, parent, buildStack);
    }

    private int toSmartMenuType(String type) {
        if ("text".equals(type)) {
            return SmartMenu.TYPE_TEXT;
        }
        // needto create toggle and slider types in SmartMenu
        if ("toggle".equals(type)) {
            return SmartMenu.TYPE_ITEM;
        }
        if ("slider".equals(type)) {
            return SmartMenu.TYPE_ITEM;
        }

        throw new IllegalArgumentException("Unknown menu item type: " + type);
    }

    private static final Class<?> DEFAULT_COMMAND_CLASS = CommandPanel.class;

    private String resolveCommandKey(String commandKey) {
        return resolveCommandKey(DEFAULT_COMMAND_CLASS, commandKey);
    }

    private String resolveCommandKey(Class<?> commandClass, String commandKey) {
        if (commandKey == null || commandKey.trim().isEmpty()) {
            return null;
        }

        try {
            java.lang.reflect.Field field = commandClass.getDeclaredField(commandKey);
            field.setAccessible(true);

            Object value = field.get(null);

            if (!(value instanceof String)) {
                throw new IllegalArgumentException("Command field is not a String: " + commandKey);
            }

            return (String) value;
        } catch (NoSuchFieldException exception) {
            throw new IllegalArgumentException("Unknown command key: " + commandKey, exception);
        } catch (IllegalAccessException exception) {
            throw new IllegalArgumentException("Cannot access command key: " + commandKey, exception);
        }
    }

    private static final Class<?> DEFAULT_KEYBOARD_CLASS = UtilsKeyboard.class;

    private Integer resolveKeyboardKey(String KeyboardKey) {
        return resolveKeyboardKey(DEFAULT_KEYBOARD_CLASS, KeyboardKey);
    }

    private Integer resolveKeyboardKey(Class<?> KeyboardClass, String KeyboardKey) {
        if (KeyboardKey == null || KeyboardKey.trim().isEmpty()) {
            return -1;
        }

        try {
            java.lang.reflect.Field field = KeyboardClass.getDeclaredField(KeyboardKey);
            field.setAccessible(true);

            Object value = field.get(null);

            if (!(value instanceof Integer)) {
                throw new IllegalArgumentException("Keyboard field is not an Integer: " + KeyboardKey);
            }

            return (Integer) value;
        } catch (NoSuchFieldException exception) {
            throw new IllegalArgumentException("Unknown keyboard key: " + KeyboardKey, exception);
        } catch (IllegalAccessException exception) {
            throw new IllegalArgumentException("Cannot access keyboard key: " + KeyboardKey, exception);
        }
    }

    private void buildSmartMenuItems(MenuItemDefinition item, SmartMenu parent, Set<String> buildStack) {
        System.out.println("Building menu item: " + item.getId() + " of type: " + item.getType());
        if ("generated".equals(item.getType())) {

            buildGeneratedItems(item, parent);
            return;
        }

        SmartMenu smartMenuItem = buildSmartMenuItem(item, parent, buildStack);
        smartMenuItem.setDynamic(item.isDynamic());
        smartMenuItem.setMaintainOpen(item.isMaintainOpen());
        parent.addItem(smartMenuItem);
    }

    private void buildGeneratedItems(MenuItemDefinition item, SmartMenu parent) {
        String provider = item.getProvider();

        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("Generated menu item is missing provider: " + item.getId());
        }

        if ("main.options.languages.root".equals(provider)) {
            buildLanguageItems(parent);
            return;
        }

        throw new IllegalArgumentException("Unknown generated menu provider: " + provider);
    }

    private void buildLanguageItems(SmartMenu parent) {
        ArrayList<LanguageData> languages = Utils.getLanguages();

        if (languages == null || languages.size() <= 1) {
            return;
        }

        for (LanguageData languageData : languages) {
            SmartMenu languageItem = createLanguageItem(languageData, parent);
            parent.addItem(languageItem);
        }
    }

    private SmartMenu createLanguageItem(LanguageData languageData, SmartMenu parent) {
        Point3D modIndexPoint = getLanguageModIndexPoint(languageData);

        return new SmartMenu(
                SmartMenu.TYPE_ITEM,
                languageData.name,
                parent,
                CommandPanel.COMMAND_CHANGE_LANGUAGE,
                languageData.language,
                languageData.country,
                modIndexPoint,
                null);
    }

    private Point3D getLanguageModIndexPoint(LanguageData languageData) {
        if (languageData == null || languageData.mod == null || Game.getModsLoaded() == null) {
            return null;
        }

        int modIndex = getLoadedModIndex(languageData.mod);

        if (modIndex == -1) {
            return null;
        }

        return new Point3D(modIndex, 0, 0);
    }

    private int getLoadedModIndex(Object languageMod) {
        for (int index = 0; index < Game.getModsLoaded().size(); index++) {
            if (Game.getModsLoaded().get(index).equals(languageMod)) {
                return index;
            }
        }

        return -1;
    }
}