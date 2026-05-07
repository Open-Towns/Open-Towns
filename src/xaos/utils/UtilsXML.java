package xaos.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Se usa para cargar buildings y items
 */
public final class UtilsXML {

    public static Node getChild (NodeList list, String sChildName) throws Exception {
        Node node;

        for (int i = 0; i < list.getLength(); i++) {
            node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(sChildName) && node.getChildNodes().item(0) != null) {
            	return node;
            }
        }

        return null;
    }

    public static String getChildValue(NodeList list, String sChildName) throws Exception {
        Node node;
        String sLocale = Locale.getDefault().getLanguage() + Locale.getDefault().getCountry();

        for (int i = 0; i < list.getLength(); i++) {
            node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(sChildName) && node.getChildNodes().item(0) != null) {
                NamedNodeMap map = node.getAttributes();
                if (map != null && map.getNamedItem(sLocale) != null) {
                    return map.getNamedItem(sLocale).getNodeValue();
                } else {
                    return node.getChildNodes().item(0).getNodeValue();
                }
            }
        }

        return null;
    }

    public static ArrayList<String> getChildValues(NodeList list, String sChildName) throws Exception {
        ArrayList<String> alPrerequisites = new ArrayList<String>();
        Node node;
        String sLocale = Locale.getDefault().getLanguage() + Locale.getDefault().getCountry();

        for (int i = 0; i < list.getLength(); i++) {
            node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(sChildName) && node.getChildNodes().item(0) != null) {
                NamedNodeMap map = node.getAttributes();
                if (map != null && map.getNamedItem(sLocale) != null) {
                    alPrerequisites.add(map.getNamedItem(sLocale).getNodeValue());
                } else {
                    alPrerequisites.add(node.getChildNodes().item(0).getNodeValue());
                }
            }
        }

        return alPrerequisites;
    }

    public static Document loadXMLFile(String fileName) throws Exception {
        File f = new File(fileName);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        String xml = Files.readString(f.toPath(), StandardCharsets.UTF_8);
        if (xml.startsWith("<<")) { //$NON-NLS-1$
            xml = "<" + xml.substring(2); //$NON-NLS-1$
            return db.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        }
        return db.parse(f);
    }

    public static Document loadXMLFileFromString(String sXML) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
//		sXML = "<?xml version='1.0' encoding='ISO-8859-1' ?><response><server><name>TownsMods.net</name><downloadURL>http://townsmods.net/api/getbury/__ID__</downloadURL><uploadURL>http://townsmods.net/api/getbury/__ID__</uploadURL></server><buriedFiles><buriedFile><fileName>popo</fileName><fileID>pot</fileID></buriedFile></buriedFiles></response>";
        return db.parse(new InputSource(new ByteArrayInputStream(sXML.getBytes())));
    }
}
