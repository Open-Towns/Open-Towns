package xaos.data;

import java.util.HashMap;

public class Types {

    private static HashMap<String, Type> list = new HashMap<String, Type>();

    public static Type getType(String typeName) {
        return getType(typeName, true);
    }

    public static Type getType(String typeName, boolean bCopy) {
        // Devolvemos una copia, siempre
        Type type = list.get(typeName);
        if (type == null) {
            return null;
        }

        if (!bCopy) {
            return type;
        }

        Type typeReturn = new Type(type.getID());
        for (int i = 0; i < type.getElements().size(); i++) {
            typeReturn.addElement(type.getElements().get(i), type.getElementNames().get(i));
        }

        return typeReturn;
    }

    public static void addElement(String sType, String sElement, String sElementName) {
        if (list.containsKey(sType)) {
            Type type = list.get(sType);
            if (!type.contains(sElement)) {
                type.addElement(sElement, sElementName);
                list.put(sType, type);
            }
        } else {
            Type type = new Type(sType);
            type.addElement(sElement, sElementName);
            list.put(sType, type);
        }
    }

    public static void clear() {
        list.clear();
    }
}
