package xaos.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UtilsServer {

    public static String getServerName(String serverURL) {
        String sXML;

        try {
            sXML = getUrlSource(serverURL);
            if (sXML == null || sXML.trim().length() == 0) {
                Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.0"), "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            Document doc = UtilsXML.loadXMLFileFromString(sXML);
            if (doc == null || doc.getDocumentElement() == null || doc.getDocumentElement().getChildNodes() == null) {
                Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.2"), "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;

            String sServerName = null;
            String sAux;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // Obtenemos el nameID, width i height
                    sAux = node.getNodeName();

                    if (sAux.equalsIgnoreCase("SERVER")) { //$NON-NLS-1$
                        // Server Tag
                        NodeList nodeListServer = node.getChildNodes();
                        sServerName = UtilsXML.getChildValue(nodeListServer, "name"); //$NON-NLS-1$
                    }
                }
            }

            if (sServerName != null && sServerName.trim().length() > 0) {
                return sServerName;
            }
        } catch (Exception e) {
            // log the error
            Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.21") + " [" + e.toString() + "]", "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        return Messages.getString("UtilsServer.1") + " [" + serverURL + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static String getBuriedTown(String serverURL, String buryFolder) {
        String sXML;

        try {
            sXML = getUrlSource(serverURL);
            if (sXML == null || sXML.trim().length() == 0) {
                Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.0"), "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            Document doc = UtilsXML.loadXMLFileFromString(sXML);
            if (doc == null || doc.getDocumentElement() == null || doc.getDocumentElement().getChildNodes() == null) {
                Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.2"), "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;

            String sDownloadURL = null;
            ArrayList<String> alNames = new ArrayList<String>();
            ArrayList<String> alIDs = new ArrayList<String>();
            String sAux;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // Obtenemos el nameID, width i height
                    sAux = node.getNodeName();

                    if (sAux.equalsIgnoreCase("SERVER")) { //$NON-NLS-1$
                        // Server Tag
                        NodeList nodeListServer = node.getChildNodes();
                        sDownloadURL = UtilsXML.getChildValue(nodeListServer, "downloadURL"); //$NON-NLS-1$
                    } else {
                        if (sAux.equalsIgnoreCase("BURIEDFILES")) { //$NON-NLS-1$
                            // Files
                            NodeList nodeListBurieds = node.getChildNodes();
                            Node nodeBurieds;
                            for (int j = 0; j < nodeListBurieds.getLength(); j++) {
                                nodeBurieds = nodeListBurieds.item(j);

                                if (nodeBurieds.getNodeType() == Node.ELEMENT_NODE) {
                                    sAux = nodeBurieds.getNodeName();
                                    if (sAux.equalsIgnoreCase("BURIEDFILE")) { //$NON-NLS-1$
                                        // Tenemos un fichero, cargamos el nombre y el ID
                                        String sName = UtilsXML.getChildValue(nodeBurieds.getChildNodes(), "fileName"); //$NON-NLS-1$
                                        String sID = UtilsXML.getChildValue(nodeBurieds.getChildNodes(), "fileID"); //$NON-NLS-1$
                                        if (sName != null && sID != null && sName.trim().length() > 0 && sID.trim().length() > 0) {
                                            // Miramos que no tenga carácteres especiales
                                            if (!sName.contains("/") && !sName.contains("\\") && !sName.contains(";") && !sName.contains("&") && !sName.contains("#") && !sName.contains("..")) {
                                                // Todo ok, aunque primero miramos que el fichero no exista en local
                                                File fTest = new File(buryFolder + File.separator + sName);
                                                if (!fTest.exists()) {
                                                    alNames.add(sName.trim());
                                                    alIDs.add(sID.trim());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (sDownloadURL != null && alNames.size() > 0) {
                // Pillamos uno a random
                int iIndexRandom = Utils.getRandomBetween(0, (alNames.size() - 1));
                // Reemplazamos los __ID__ de la URL por el fileID
                int iIndexID = sDownloadURL.indexOf("__ID__"); //$NON-NLS-1$
                if (iIndexID == -1) {
                    Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.13"), "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    while (iIndexID != -1) {
                        sDownloadURL = sDownloadURL.substring(0, iIndexID) + alIDs.get(iIndexRandom) + sDownloadURL.substring(iIndexID + "__ID__".length()); //$NON-NLS-1$
                        iIndexID = sDownloadURL.indexOf("__ID__"); //$NON-NLS-1$
                    }

                    if (downloadBuryFile(sDownloadURL, buryFolder + File.separator + alNames.get(iIndexRandom))) {
                        return alNames.get(iIndexRandom);
                    } else {
                        Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.17"), "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$
                    }

                }
            } else {
                if (sDownloadURL == null) {
                    Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.19"), "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } catch (Exception e) {
            // log the error
            Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.21") + " [" + e.toString() + "]", "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        return null;
    }

    private static String getUrlSource(String url) throws Exception {
        StringBuilder a = new StringBuilder();
        if (!url.startsWith("http://")) { //$NON-NLS-1$
            url = "http://" + url; //$NON-NLS-1$
        }

        URL urlObject = new URI(url).toURL();
        URLConnection uc = urlObject.openConnection();

        try {
            uc.setReadTimeout (1500);
        }
        catch (Exception e) {
        }

        InputStream iStream = uc.getInputStream();

        try {
        	uc.setReadTimeout (0);
        }
        catch (Exception e) {
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(iStream, "UTF-8")); //$NON-NLS-1$
        String inputLine;

        while ((inputLine = br.readLine()) != null) {
            a.append(inputLine);
        }
        br.close();

        return a.toString();
    }

    private static boolean downloadBuryFile(String dlURL, String sPathToFile) throws Exception {
        /*
         * Get a connection to the URL and start up a buffered reader.
         */
        URL url = new URI(dlURL).toURL();
        url.openConnection();
        InputStream reader = url.openStream();

        /*
         * Setup a buffered file writer to write out what we read from the website.
         */
        FileOutputStream writer = new FileOutputStream(sPathToFile);
        byte[] buffer = new byte[32 * 1024];
        int totalBytesRead = 0;
        int bytesRead = 0;

        while ((bytesRead = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, bytesRead);
            buffer = new byte[32 * 1024];
            totalBytesRead += bytesRead;
        }

        writer.close();
        reader.close();

        return true;
    }

    public static void main(String[] args) {
        getBuriedTown("http://townsmods.net/api/bury", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
