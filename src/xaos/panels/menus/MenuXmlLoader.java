package xaos.panels.menus;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class MenuXmlLoader {

    private MenuXmlLoader() {
    }

    public static MenuDefinition load(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Safer defaults - stops some unwanted external XML behaviour.
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            Element menuElement = document.getDocumentElement();

            if (!"menu".equals(menuElement.getTagName())) {
                throw new IllegalArgumentException("Root element must be <menu>: " + file.getPath());
            }

            String menuId = required(menuElement, "id", file);
            String titleKey = required(menuElement, "titleKey", file);

            MenuDefinition menuDefinition = new MenuDefinition(menuId, titleKey);

            NodeList childNodes = menuElement.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element itemElement = (Element) node;

                if (!"item".equals(itemElement.getTagName())) {
                    continue;
                }

                MenuItemDefinition item = parseItem(itemElement, file);
                menuDefinition.addItem(item);
            }

            return menuDefinition;
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load menu XML: " + file.getPath(), exception);
        }
    }

    private static MenuItemDefinition parseItem(Element itemElement, File file) {
        String id = required(itemElement, "id", file);
        String type = required(itemElement, "type", file);
        String titleKey = optional(itemElement, "titleKey");
        String targetMenu = optional(itemElement, "targetMenu");
        String command = optional(itemElement, "command");
        boolean dynamic = optionalBoolean(itemElement, "dynamic", false);
        boolean maintainOpen = optionalBoolean(itemElement, "maintainOpen", true);
        String provider = optional(itemElement, "provider");
        String key = optional(itemElement, "key");
        String condition = optional(itemElement, "condition");
        String textColorName = optional(itemElement, "textColorName");
        boolean transparent = optionalBoolean(itemElement, "transparent", true);

        return new MenuItemDefinition(
                id,
                type,
                titleKey,
                targetMenu,
                command,
                dynamic,
                maintainOpen,
                provider,
                key,
                condition,
                textColorName,
                transparent
            
            );
    }

    private static String required(Element element, String attributeName, File file) {
        String value = element.getAttribute(attributeName);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required attribute '" + attributeName + "' in " + file.getPath());
        }

        return value.trim();
    }

    private static String optional(Element element, String attributeName) {
        String value = element.getAttribute(attributeName);

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private static boolean optionalBoolean(Element element, String attributeName, boolean defaultValue) {
        String value = element.getAttribute(attributeName);

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        value = value.trim().toLowerCase();

        if ("true".equals(value)) {
            return true;
        }

        if ("false".equals(value)) {
            return false;
        }

        throw new IllegalArgumentException(
                "Invalid boolean value for attribute '" + attributeName + "': " + value);
    }
}