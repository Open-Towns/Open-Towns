package xaos.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import xaos.property.PropertyFile;

import xaos.Towns;
import xaos.main.Game;

public final class UtilFont {

    public static short MAX_WIDTH = 16; // Ancho, se calcula con la info de la fuente, 16 es lo mínimo
    public static short MAX_HEIGHT = 16; // Altura, se calcula con la info de la fuente, 16 es lo mínimo
//	public static short MAX_HEIGHT_NUMBERS = 8; // Altura, se calcula con la info de la fuente, 16 es lo mínimo

    private static CharDef[] chars;

    static {
        parseFont();
    }

    public static CharDef getCharDef(int charID) {
        if (charID < chars.length) {
            return chars[charID];
        }

        return null;
    }

    public static void parseFont() {
        try {
            // now parse the font file
            BufferedReader in = null;

            String sFontFilePath = Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "FONT_FILE"); //$NON-NLS-1$
            File f = new File(sFontFilePath);
            if (f.exists()) {
                in = new BufferedReader(new FileReader(sFontFilePath));
            } else {
                // Mods
                File fUserFolder = new File(Game.getUserFolder());
                if (fUserFolder.exists() && fUserFolder.isDirectory()) {
                    ArrayList<String> alMods = Game.getModsLoaded();
                    if (alMods != null && alMods.size() > 0) {
                        for (int i = 0; i < alMods.size(); i++) {
                            String sModFontFilePath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "FONT_FILE"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            File fIni = new File(sModFontFilePath);
                            if (fIni.exists()) {
                                in = new BufferedReader(new FileReader(sModFontFilePath));
                                break;
                            }
                        }
                    }
                }
            }

            if (in == null) {
                throw new Exception(sFontFilePath);
            }

            List<CharDef> charDefs = new ArrayList<CharDef>(255);
            int maxChar = 0;
            boolean done = false;
            while (!done) {
                String line = in.readLine();
                if (line == null) {
                    done = true;
                } else {
                    if (line.startsWith("chars c")) { //$NON-NLS-1$
                        // ignore
                    } else if (line.startsWith("char")) { //$NON-NLS-1$
                        CharDef def = parseChar(line);
                        if (def != null) {
                            maxChar = Math.max(maxChar, def.id);
                            charDefs.add(def);
                        }
                    }
                    if (line.startsWith("kernings c")) { //$NON-NLS-1$
                        // ignore
                    } else if (line.startsWith("kerning")) { //$NON-NLS-1$
                        // ignore
                    }
                }
            }

            int iMinOffset = 100;
            chars = new CharDef[maxChar + 1];
            for (Iterator<CharDef> iter = charDefs.iterator(); iter.hasNext();) {
                CharDef def = (CharDef) iter.next();
                chars[def.id] = def;
                if (iMinOffset > def.yoffset) {
                    iMinOffset = def.yoffset;
                }
            }

			// Hemos acabado, ahora recorro todos para restar pixels al yoffset (para tener lo mínimo yoffset=0)
            // También seteamos el max_height aquí
            if (iMinOffset > 0) {
                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] != null) {
                        // yoffset
                        chars[i].yoffset -= iMinOffset;

                        // MAX_HEIGHT
                        if ((chars[i].yoffset + chars[i].height) > MAX_HEIGHT) {
                            MAX_HEIGHT = (short) (chars[i].yoffset + chars[i].height);
                        }
                    }

                }
            }

        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("UtilFont.0") + " " + e.toString(), "UtilFont"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Game.exit();
        }
    }

    /**
     * Parse a single character line from the definition
     *
     * @param line The line to be parsed
     * @return The character definition from the line
     * @throws SlickException Indicates a given character is not valid in an
     * angel code font
     */
    private static CharDef parseChar(String line) {
        CharDef def = new CharDef();
        StringTokenizer tokens = new StringTokenizer(line, " ="); //$NON-NLS-1$

        tokens.nextToken(); // char
        tokens.nextToken(); // id
        def.id = Short.parseShort(tokens.nextToken()); // id value
        if (def.id < 0) {
            return null;
        }

        tokens.nextToken(); // x
        def.x = Short.parseShort(tokens.nextToken()); // x value
        tokens.nextToken(); // y
        def.y = Short.parseShort(tokens.nextToken()); // y value
        tokens.nextToken(); // width
        def.width = Short.parseShort(tokens.nextToken()); // width value
        tokens.nextToken(); // height
        def.height = Short.parseShort(tokens.nextToken()); // height value

        def.xTex = def.x / 256f;
        def.yTex = def.y / 256f;
        def.widthTex = def.width / 256f;
        def.heightTex = def.height / 256f;
        tokens.nextToken(); // x offset
        tokens.nextToken(); // x offset
        // def.xoffset = Short.parseShort(tokens.nextToken()); // xoffset value
        tokens.nextToken(); // y offset
        def.yoffset = Short.parseShort(tokens.nextToken()); // yoffset value
        tokens.nextToken(); // xadvance
        def.xadvance = Short.parseShort(tokens.nextToken()); // xadvance

        // Miramos MAX_WIDTH
        if (def.width > MAX_WIDTH) {
            MAX_WIDTH = def.width;
        }
        // Miramos MAX_HEIGHT_NUMBERS
//		if (((def.id >= '0' && def.id <= '9') || (def.id == '/')) && def.height > MAX_HEIGHT_NUMBERS) {
//			MAX_HEIGHT_NUMBERS = def.height;
//		}

        return def;
    }

    public static int getWidth(String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        for (int i = 0, n = text.length(); i < n; i++) {
            int id = text.charAt(i);

            if (id >= chars.length) {
                continue;
            }
            CharDef charDef = chars[id];
            if (charDef == null) {
                continue;
            }

            if ((i + 1) < n) {
                width += charDef.xadvance;
            } else {
                width += charDef.width;
            }
        }

        return width;
    }

    /**
     * Returns the number of chars where the string has to be splitted It takes
     * account words
     *
     * @param sText Text
     * @param iWidth Max width
     * @return the number of chars where the string has to be splitted
     */
    public static int getMaxCharsByWidth(String sText, int iWidth) {
        if (sText.length() < 5 || getWidth(sText) <= iWidth) {
            return sText.length();
        }

        // No cabe, partimos por palabras
        int iIndexPrevious = 0;
        int iIndex = sText.indexOf(' ');
        while (iIndex != -1) {
            if (getWidth(sText.substring(0, iIndex)) <= iWidth) {
                iIndexPrevious = iIndex;
                if ((iIndexPrevious + 1) >= sText.length()) {
                    break;
                }
                iIndex = sText.substring(iIndexPrevious + 1).indexOf(' ');
                if (iIndex == -1) {
                    return iIndexPrevious;
                }
                iIndex += iIndexPrevious + 1;
            } else {
                if (iIndexPrevious > 0) {
                    return iIndexPrevious;
                } else {
                    break;
                }
            }
        }

        // Si llega aquí es que el primer espacio ya no cabe, partimos a saco
        iIndex = sText.indexOf(' ');
        if (iIndex == -1) {
            iIndex = sText.length() - 1;
        }

        for (int i = iIndex; i >= 1; i--) {
            if (getWidth(sText.substring(0, i)) <= iWidth) {
                return i;
            }
        }

        return -1;
    }
}
