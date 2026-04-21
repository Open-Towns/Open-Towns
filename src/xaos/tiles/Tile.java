package xaos.tiles;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.lwjgl.opengl.GL11;
import xaos.property.PropertyFile;

import xaos.Towns;
import xaos.main.Game;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.TextureData;
import xaos.utils.Utils;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsIniHeaders;

/**
 * All son tiles: entities, Items, terrains, ....
 *
 */
public class Tile implements Externalizable {

    private static final long serialVersionUID = -8129072708038216803L;

    private int iID;

    public final static int TERRAIN_ICON_WIDTH = 64;
    public final static int TERRAIN_ICON_HEIGHT = 32;

    private static final float TEXTURE_ICON_WIDTH = 0.25f / 4f;
    private static final float TEXTURE_ICON_HEIGHT = 0.125f / 4f;

    private transient int textureID;

    private transient ColorGL colorMiniMap;
    private transient byte tileX;
    private transient byte tileY;
    private transient float tileTexX0; // Coordenada X0 de la textura
    private transient float tileTexY0; // Coordenada Y0 de la textura
    private transient float tileTexX1; // Coordenada X1 de la textura
    private transient float tileTexY1; // Coordenada Y1 de la textura
    private transient byte tileXRot;
    private transient byte tileYRot;
    private transient float tileTexX0Rot; // Coordenada X0 de la textura rotada
    private transient float tileTexY0Rot; // Coordenada Y0 de la textura rotada
    private transient float tileTexX1Rot; // Coordenada X1 de la textura rotada
    private transient float tileTexY1Rot; // Coordenada Y1 de la textura rotada

    private transient short tileWidth;
    private transient short tileWidthOffset;
    private transient short tileHeight;
    private transient short tileHeightOffset;

    // Animation
    private transient short animationTiles; // Número de tiles de los que se compone la animación (0 o 1 es lo mismo)
    private transient short animationFrameDelay; // Número de frames que deben pasar entre un tile y el siguiente
    private transient short currentAnimationTile;
    private transient short currentFrameDelay;
    private transient float currentTileTexX0; // Coordenada X0 de la textura
    private transient float currentTileTexY0; // Coordenada Y0 de la textura
    private transient float currentTileTexX1; // Coordenada X1 de la textura
    private transient float currentTileTexY1; // Coordenada Y1 de la textura

    private String iniHeader; // TODO: XCN passar a INT, es guanya 0.5Mb (depth=11)
    private transient int numericIniHeader;

    public Tile() {
    }

    public Tile(String iniHeader) {
        setIniHeader(iniHeader);
        setNumericIniHeader(UtilsIniHeaders.getIntIniHeader(iniHeader));
        refreshTransients();
    }

    public void refreshTransients() {
        setNumericIniHeader(UtilsIniHeaders.getIntIniHeader(iniHeader));

        // Texture
        setTextureID(getIniHeader(), null);

        // Ponemos el color de minimapa
        setColorMiniMap(Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + getIniHeader() + "]COLOR_MINIMAP")); //$NON-NLS-1$ //$NON-NLS-2$

        // Guardamos las coordenadas del tileset
        setTileSetCoordinates(getIniHeader(), null);

        // Guardamos el tamaño del tile
        setTileSize(getIniHeader(), null);

        // Guardamos las coordenadas de la textura del tileset
        setTileSetTexCoordinates();

        // ANIMATION
        // Guardamos el número de tiles para la animación
        setAnimationTiles(Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + getIniHeader() + "]ANIMATION_TILES")); //$NON-NLS-1$ //$NON-NLS-2$
        setAnimationFrameDelay(Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + getIniHeader() + "]ANIMATION_FRAME_DELAY", Game.FPS_INGAME)); //$NON-NLS-1$ //$NON-NLS-2$
        setCurrentAnimationTile(0);
        setCurrentFrameDelay(Utils.getRandomBetween(0, getAnimationFrameDelay() - 1));
    }

    public void setID(int ID) {
        iID = ID;
    }

    public int getID() {
        return iID;
    }

    public void setTextureID(String sIniHeader, String sIniHeader2) {
        TextureData texture = UtilsGL.loadTexture(Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + sIniHeader + "]TEXTURE_FILE"), GL11.GL_REPLACE);
        if (texture == null) {
            texture = UtilsGL.loadTexture(Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + sIniHeader2 + "]TEXTURE_FILE"), GL11.GL_REPLACE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (texture == null) {
                Log.log(Log.LEVEL_ERROR, Messages.getString("Tile.6") + sIniHeader + Messages.getString("Tile.7") + sIniHeader2 + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                Game.exit();
            }
        }

        setTextureID(texture.getTextureID());
    }

    public void setTextureID(int textureID) {
        this.textureID = textureID;
    }

    public int getTextureID() {
        return textureID;
    }

    public void setColorMiniMap(String sColor) {
        if (sColor != null) {
            colorMiniMap = Utils.getColorFromString(sColor);
        } else {
            colorMiniMap = null;
        }
    }

    public ColorGL getColorMiniMap() {
        return colorMiniMap;
    }

    public int getTileSetX() {
        return this.tileX;
    }

    public int getTileSetY() {
        return this.tileY;
    }

    public float getBaseTileSetTexX0() {
        return this.tileTexX0;
    }

    public float getBaseTileSetTexX1() {
        return this.tileTexX1;
    }

    public float getBaseTileSetTexY0() {
        return this.tileTexY0;
    }

    public float getBaseTileSetTexY1() {
        return this.tileTexY1;
    }

    public int getTileSetXRot() {
        return this.tileXRot;
    }

    public int getTileSetYRot() {
        return this.tileYRot;
    }

    public float getBaseTileSetTexX0Rot() {
        return this.tileTexX0Rot;
    }

    public float getBaseTileSetTexX1Rot() {
        return this.tileTexX1Rot;
    }

    public float getBaseTileSetTexY0Rot() {
        return this.tileTexY0Rot;
    }

    public float getBaseTileSetTexY1Rot() {
        return this.tileTexY1Rot;
    }

    public float getTileSetTexX0() {
        return this.currentTileTexX0;
    }

    public float getTileSetTexY0() {
        return this.currentTileTexY0;
    }

    public void setTileSetTexX0(float texX0) {
        this.currentTileTexX0 = texX0;
    }

    public void setTileSetTexY0(float texY0) {
        this.currentTileTexY0 = texY0;
    }

    public float getTileSetTexX1() {
        return this.currentTileTexX1;
    }

    public float getTileSetTexY1() {
        return this.currentTileTexY1;
    }

    public void setTileSetTexX1(float texX1) {
        this.currentTileTexX1 = texX1;
    }

    public void setTileSetTexY1(float texY1) {
        this.currentTileTexY1 = texY1;
    }

    public void setTileWidth(int width) {
        this.tileWidth = (short) width;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public int getTileWidthOffset() {
        return this.tileWidthOffset;
    }

    public void setTileHeight(int height) {
        this.tileHeight = (short) height;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    public int getTileHeightOffset() {
        return this.tileHeightOffset;
    }

    public void setTileSetCoordinates(String iniHeader, String iniHeader2) {
        this.tileX = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_X", -1); //$NON-NLS-1$ //$NON-NLS-2$
        this.tileY = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_Y", -1); //$NON-NLS-1$ //$NON-NLS-2$
        this.tileXRot = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_X_ROT", -1); //$NON-NLS-1$ //$NON-NLS-2$
        this.tileYRot = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_Y_ROT", -1); //$NON-NLS-1$ //$NON-NLS-2$

        if (this.tileX == -1 || this.tileY == -1) {
            this.tileX = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_X"); //$NON-NLS-1$ //$NON-NLS-2$
            this.tileY = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_Y"); //$NON-NLS-1$ //$NON-NLS-2$
            this.tileXRot = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_X_ROT"); //$NON-NLS-1$ //$NON-NLS-2$
            this.tileYRot = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_Y_ROT"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public boolean setTileSetCoordinatesReturn(String iniHeader, String iniHeader2) {
        this.tileX = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_X", -1); //$NON-NLS-1$ //$NON-NLS-2$
        this.tileY = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_Y", -1); //$NON-NLS-1$ //$NON-NLS-2$
        this.tileXRot = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_X_ROT", -1); //$NON-NLS-1$ //$NON-NLS-2$
        this.tileYRot = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_Y_ROT", -1); //$NON-NLS-1$ //$NON-NLS-2$

        if (this.tileX == -1 || this.tileY == -1) {
            this.tileX = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_X", -1); //$NON-NLS-1$ //$NON-NLS-2$
            this.tileY = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_Y", -1); //$NON-NLS-1$ //$NON-NLS-2$
            this.tileXRot = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_X_ROT", -1); //$NON-NLS-1$ //$NON-NLS-2$
            this.tileYRot = (byte) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_Y_ROT", -1); //$NON-NLS-1$ //$NON-NLS-2$
            if (this.tileX == -1 || this.tileY == -1) {
                return false;
            }
        }

        return true;
    }

    public void setTileSize(String iniHeader, String iniHeader2) {
        // Width
        this.tileWidth = (short) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_WIDTH", -1); //$NON-NLS-1$ //$NON-NLS-2$
        if (this.tileWidth == -1) {
            this.tileWidth = (short) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_WIDTH"); //$NON-NLS-1$ //$NON-NLS-2$
            if (this.tileWidth == 0) {
                this.tileWidth = TERRAIN_ICON_WIDTH;
            }
        }
        this.tileWidthOffset = (short) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_WIDTH_OFFSET", -1); //$NON-NLS-1$ //$NON-NLS-2$
        if (this.tileWidthOffset == -1) {
            this.tileWidthOffset = (short) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_WIDTH_OFFSET"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Height
        this.tileHeight = (short) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_HEIGHT", -1); //$NON-NLS-1$ //$NON-NLS-2$
        if (this.tileHeight == -1) {
            this.tileHeight = (short) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_HEIGHT"); //$NON-NLS-1$ //$NON-NLS-2$
            if (this.tileHeight == 0) {
                this.tileHeight = TERRAIN_ICON_HEIGHT;
            }
        }
        this.tileHeightOffset = (short) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader + "]TILE_HEIGHT_OFFSET", -1); //$NON-NLS-1$ //$NON-NLS-2$
        if (this.tileHeightOffset == -1) {
            this.tileHeightOffset = (short) Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + iniHeader2 + "]TILE_HEIGHT_OFFSET"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void setTileSetTexCoordinates() {
        this.tileTexX0 = this.tileX * TEXTURE_ICON_WIDTH;
        this.tileTexY0 = this.tileY * TEXTURE_ICON_HEIGHT;
        this.tileTexX1 = this.tileTexX0 + (TEXTURE_ICON_WIDTH * ((float) this.tileWidth / (float) TERRAIN_ICON_WIDTH));
        this.tileTexY1 = this.tileTexY0 + (TEXTURE_ICON_HEIGHT * ((float) this.tileHeight / (float) TERRAIN_ICON_HEIGHT));
        if (this.tileXRot != -1 && this.tileYRot != -1) {
            this.tileTexX0Rot = this.tileXRot * TEXTURE_ICON_WIDTH;
            this.tileTexY0Rot = this.tileYRot * TEXTURE_ICON_HEIGHT;
            this.tileTexX1Rot = this.tileTexX0Rot + (TEXTURE_ICON_WIDTH * ((float) this.tileWidth / (float) TERRAIN_ICON_WIDTH));
            this.tileTexY1Rot = this.tileTexY0Rot + (TEXTURE_ICON_HEIGHT * ((float) this.tileHeight / (float) TERRAIN_ICON_HEIGHT));
        } else {
            this.tileTexX0Rot = this.tileTexX0;
            this.tileTexY0Rot = this.tileTexY0;
            this.tileTexX1Rot = this.tileTexX1;
            this.tileTexY1Rot = this.tileTexY1;
        }
        this.currentTileTexX0 = this.tileTexX0;
        this.currentTileTexX1 = this.tileTexX1;
        this.currentTileTexY0 = this.tileTexY0;
        this.currentTileTexY1 = this.tileTexY1;
    }

    public void setAnimationTiles(int animationTiles) {
        this.animationTiles = (short) animationTiles;
    }

    public int getAnimationTiles() {
        return animationTiles;
    }

    public void setAnimationFrameDelay(int animationFrameDelay) {
        this.animationFrameDelay = (short) animationFrameDelay;
    }

    public int getAnimationFrameDelay() {
        return animationFrameDelay;
    }

    public void setCurrentAnimationTile(int currentAnimationTile) {
        if (currentAnimationTile >= animationTiles) {
            this.currentAnimationTile = 0;
        } else {
            this.currentAnimationTile = (short) currentAnimationTile;
        }

        if (this.currentAnimationTile == 0) {
            this.currentTileTexX0 = this.tileTexX0;
            this.currentTileTexX1 = this.tileTexX1;
        } else {
            this.currentTileTexX0 = this.tileTexX0 + TEXTURE_ICON_WIDTH * this.currentAnimationTile;
            this.currentTileTexX1 = this.tileTexX1 + TEXTURE_ICON_WIDTH * this.currentAnimationTile;
        }
    }

    public int getCurrentAnimationTile() {
        return currentAnimationTile;
    }

    public void resetAnimationItem(boolean bRotated) {
        currentAnimationTile = 0;
        if (bRotated) {
            this.currentTileTexX0 = this.tileTexX0Rot;
            this.currentTileTexX1 = this.tileTexX1Rot;
            this.currentTileTexY0 = this.tileTexY0Rot;
            this.currentTileTexY1 = this.tileTexY1Rot;
        } else {
            this.currentTileTexX0 = this.tileTexX0;
            this.currentTileTexX1 = this.tileTexX1;
            this.currentTileTexY0 = this.tileTexY0;
            this.currentTileTexY1 = this.tileTexY1;
        }
    }

    public void resetAnimationLiving(boolean bRotated) {
        currentAnimationTile = 0;
        this.currentTileTexX0 = this.tileTexX0;
        this.currentTileTexX1 = this.tileTexX1;
    }

    public void updateAnimation(boolean bRotated) {
        if (getAnimationTiles() > 1 && !Game.isPaused()) {
            setCurrentFrameDelay(getCurrentFrameDelay() + 1);
            if (getCurrentFrameDelay() == 0) {
                currentAnimationTile++;
                if (currentAnimationTile >= animationTiles) {
                    currentAnimationTile = 0;
                }

                if (this.currentAnimationTile == 0) {
                    if (bRotated) {
                        this.currentTileTexX0 = this.tileTexX0Rot;
                        this.currentTileTexX1 = this.tileTexX1Rot;
                    } else {
                        this.currentTileTexX0 = this.tileTexX0;
                        this.currentTileTexX1 = this.tileTexX1;
                    }
                } else {
                    if (bRotated) {
                        this.currentTileTexX0 = this.currentTileTexX1;
                        this.currentTileTexX1 += (this.tileTexX1Rot - this.tileTexX0Rot);
                    } else {
                        this.currentTileTexX0 = this.currentTileTexX1;
                        this.currentTileTexX1 += (this.tileTexX1 - this.tileTexX0);
                    }
                }
            }
        }
    }

    public void setCurrentFrameDelay(int currentFrameDelay) {
        if (currentFrameDelay >= getAnimationFrameDelay()) {
            this.currentFrameDelay = 0;
        } else {
            this.currentFrameDelay = (short) currentFrameDelay;
        }
    }

    public int getCurrentFrameDelay() {
        return currentFrameDelay;
    }

    public void setIniHeader(String iniHeader) {
        this.iniHeader = iniHeader;
    }

    public String getIniHeader() {
        return iniHeader;
    }

    public void setNumericIniHeader(int numericIniHeader) {
        this.numericIniHeader = numericIniHeader;
    }

    public int getNumericIniHeader() {
        return numericIniHeader;
    }

    public void changeGraphic(String sNewIniHeader) {
        setTextureID(sNewIniHeader, getIniHeader());
        setTileSetCoordinates(sNewIniHeader, getIniHeader());
        setTileSize(sNewIniHeader, getIniHeader());
        setTileSetTexCoordinates();
        setAnimationTiles(Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + sNewIniHeader + "]ANIMATION_TILES")); //$NON-NLS-1$ //$NON-NLS-2$
        setAnimationFrameDelay(Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + sNewIniHeader + "]ANIMATION_FRAME_DELAY", Game.FPS_INGAME)); //$NON-NLS-1$ //$NON-NLS-2$
        setCurrentAnimationTile(0);
        setCurrentFrameDelay(Utils.getRandomBetween(0, getAnimationFrameDelay() - 1));
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        iID = in.readInt();
        iniHeader = (String) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(iID);
        out.writeObject(iniHeader);
    }
}
