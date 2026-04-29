package xaos.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import xaos.platform.lwjgl3.LWJGLException;
import xaos.platform.lwjgl3.input.Cursor;
import xaos.platform.lwjgl3.input.Mouse;
import xaos.platform.lwjgl3.opengl.Display;
import xaos.platform.lwjgl3.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import xaos.Towns;
import xaos.data.GlobalEventData;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MainPanel;
import xaos.panels.UIPanel;
import xaos.property.PropertyFile;
import xaos.tiles.Cell;
import xaos.tiles.Tile;


public final class UtilsGL {


	public static boolean ATI_begin = false;
	public static boolean ATI_drawed = false;

	// Textures
	private static int maxTextureId = 1;
	private static final Deque<Integer> freeTextureIds = new ArrayDeque<Integer> ();
	private static final HashMap<String, ImageData> cachedImages = new HashMap<String, ImageData> ();
	private static final HashMap<String, boolean[][]> cachedTextureAlphas = new HashMap<String, boolean[][]> ();

	private static int lastWindowWidth;
	private static int lastWindowHeight;


	public static DisplayMode getFullscreenDisplayMode () throws LWJGLException {
		int iWidth = Display.getDesktopDisplayMode ().getWidth ();
		int iHeight = Display.getDesktopDisplayMode ().getHeight ();

		DisplayMode[] modes = Display.getAvailableDisplayModes ();
		int freq = 0;

		DisplayMode targetDisplayMode = null, current;
		for (int i = 0; i < modes.length; i++) {
			current = modes[i];

			if ((current.getWidth () == iWidth) && (current.getHeight () == iHeight)) {
				if ((targetDisplayMode == null) || (current.getFrequency () >= freq)) {
					if ((targetDisplayMode == null) || (current.getBitsPerPixel () > targetDisplayMode.getBitsPerPixel ())) {
						targetDisplayMode = current;
						freq = targetDisplayMode.getFrequency ();
					}
				}

				// If we've found a match for bpp and frequence against the
				// original display mode then it's probably best to go for this one
				// since it's most likely compatible with the monitor
				if ((current.getBitsPerPixel () == Display.getDesktopDisplayMode ().getBitsPerPixel ()) && (current.getFrequency () == Display.getDesktopDisplayMode ().getFrequency ())) {
					return current;
				}
			}
		}

		return null; // no display modes available
	}


	/**
	 * inits the openGL system and sets the initial width and height for the window
	 * 
	 * @param width
	 * @param height
	 * @param bFullScreen
	 */
	public static void initGL (int width, int height, boolean bFullScreen) {
		try {
			lastWindowWidth = width;
			lastWindowHeight = height;
			if (bFullScreen) {
				// Full screen
				Display.setDisplayMode (getFullscreenDisplayMode ());
			} else {
				Display.setDisplayMode (new DisplayMode (width, height));
			}

			Display.setResizable (true);
			Display.setFullscreen (bFullScreen);
			Display.setVSyncEnabled (true);
			Display.setTitle ("Towns");

			ImageData iconImage = loadImage ("icon.png");
			Display.setIcon (new ByteBuffer [] { iconImage.getImagePixels () });
			Display.create ();

			setNativeCursor ();
		}
		catch (Exception e) {
			Log.log (Log.LEVEL_ERROR, e.toString (), "UtilsGL"); //$NON-NLS-1$
			Game.exit ();
		}

		initGLModes ();
	}


	public static void initGLModes () {
		GL11.glDisable (GL11.GL_LIGHTING);
		GL11.glDisable (GL11.GL_DITHER);
		GL11.glDisable (GL11.GL_STENCIL_TEST);
		GL11.glDisable (GL11.GL_LINE_SMOOTH);
		GL11.glDisable (GL11.GL_POINT_SMOOTH);
		GL11.glDisable (GL11.GL_POLYGON_SMOOTH);

		GL11.glViewport (0, 0, Display.getWidth (), Display.getHeight ());
		GL11.glLoadIdentity ();
		GL11.glMatrixMode (GL11.GL_PROJECTION);
		GL11.glLoadIdentity ();
		GL11.glOrtho (0, UtilsGL.getWidth (), UtilsGL.getHeight (), 0, -((World.MAP_WIDTH * World.MAP_HEIGHT * 1) + 68), 1);

		GL11.glMatrixMode (GL11.GL_MODELVIEW);
		GL11.glLoadIdentity ();
		GL11.glTranslatef (0.375f, 0.375f, 0);

		GL11.glEnable (GL11.GL_TEXTURE_2D);
		GL11.glDisable (GL11.GL_DEPTH_TEST);
		// GL11.glEnable (GL11.GL_DEPTH_TEST);
		// GL11.glDepthFunc (GL11.GL_LEQUAL);
		GL11.glEnable (GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc (GL11.GL_GREATER, 0);
		GL11.glEnable (GL11.GL_BLEND);
		GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}


	public static boolean isFullScreen () {
		return Display.isFullscreen ();
	}


	public static void onResize (int width, int height, boolean fullScreen) {
		if (!fullScreen) {
			lastWindowWidth = width;
			lastWindowHeight = height;
		}
	}


	public static void toggleFullScreen () {
		try {
			if (Display.isFullscreen ()) {
				Display.setDisplayMode (new DisplayMode (lastWindowWidth, lastWindowHeight));
			} else {
				lastWindowWidth = Display.getWidth ();
				lastWindowHeight = Display.getHeight ();
				DisplayMode fullscreenDisplayMode = getFullscreenDisplayMode ();
				Display.setDisplayModeAndFullscreen (fullscreenDisplayMode);
			}
		}
		catch (LWJGLException e) {
			Log.log (Log.LEVEL_ERROR, e.toString (), "UtilsGL"); //$NON-NLS-1$
			Game.exit ();
		}
	}


	public static int getLastWindowWidth () {
		return lastWindowWidth;
	}


	public static int getLastWindowHeight () {
		return lastWindowHeight;
	}


	public static int getWidth () {
		return Display.getWidth ();
	}


	public static int getHeight () {
		return Display.getHeight ();
	}


	private static void setNativeCursor () throws Exception {
		final File cursorFile = new File (Towns.getPropertiesString ("GRAPHICS_FOLDER") + Towns.getPropertiesString (PropertyFile.PROPERTY_FILE_GRAPHICS, "CURSOR_FILE"));
		BufferedImage imageCursor = ImageIO.read (cursorFile);

		int[] buffer = new int [imageCursor.getWidth ()];
		IntBuffer intBuffer = ByteBuffer.allocateDirect (imageCursor.getWidth () * imageCursor.getHeight () * 4).order (ByteOrder.nativeOrder ()).asIntBuffer ();

		// swap y-axis
		for (int y = imageCursor.getHeight () - 1; y >= 0; --y) {
			imageCursor.getRGB (0, y, imageCursor.getWidth (), 1, buffer, 0, imageCursor.getWidth ());
			intBuffer.put (buffer);
		}
		intBuffer.rewind ();

		try {
			Mouse.setNativeCursor (new Cursor (imageCursor.getWidth (), imageCursor.getHeight (), 0, 15, 1, intBuffer, null));
		}
		catch (LWJGLException e) {
			Log.log (Log.LEVEL_ERROR, "No native cursor support.", UtilsGL.class.getCanonicalName ());
		}
	}


	// private static void setNativeCursor() throws Exception {
	//        BufferedImage imageCursor = ImageIO.read(new File(Towns.getPropertiesString("GRAPHICS_FOLDER") + Towns.getPropertiesString(Towns.PropertyFile.PROPERTY_FILE_GRAPHICS, "CURSOR_FILE"))); //$NON-NLS-1$ //$NON-NLS-2$
	//
	// ImageData imageData = loadImage(imageCursor);
	// if (imageData == null || imageData.imagePixels == null) {
	// return;
	// }
	//
	// byte[] texturePixels = imageData.imagePixels;
	// byte[] texturePixels2 = new byte[texturePixels.length];
	//
	// // Giramos los pixels pq sale al revés
	// int iPixel1, iPixel2;
	// for (int x = 0; x < 16; x++) {
	// for (int y = 0; y < 16; y++) {
	// iPixel1 = (y * 16 * 4) + (x * 4);
	// iPixel2 = ((15 - y) * 16 * 4) + (x * 4);
	// for (int z = 0; z < 4; z++) {
	// texturePixels2[iPixel1 + z] = texturePixels[iPixel2 + z];
	// }
	// }
	// }
	//
	// /*
	// * Note that the IntBuffer contains packed pixel data, so one int contains all four pixel components.
	// */
	// IntBuffer intbuf = ByteBuffer.allocateDirect(16 * 16 * 4 /* sizeof(int) */).order(ByteOrder.nativeOrder()).asIntBuffer();
	//
	// ByteBuffer pixels = ByteBuffer.wrap(texturePixels2);
	//
	// byte[] inPixel = new byte[4];
	// for (int i = 0; i < (16 * 16); i++) {
	// int outPixel = 0x00000000; // AARRGGBB
	//
	// inPixel[0] = pixels.get(); // RR
	// inPixel[1] = pixels.get(); // GG
	// inPixel[2] = pixels.get(); // BB
	// inPixel[3] = pixels.get(); // AA
	//
	// outPixel |= inPixel[3] << 24; // AA
	// outPixel |= inPixel[0] << 16; // RR
	// outPixel |= inPixel[1] << 8; // GG
	// outPixel |= inPixel[2]; // BB
	//
	// intbuf.put(outPixel);
	// }
	// intbuf.flip();
	//
	// try {
	// Mouse.setNativeCursor(new Cursor(16, 16, 0, 15, 1, intbuf, null));
	// } catch (LWJGLException e) {
	// Log.log(Log.LEVEL_ERROR, "No native cursor support.", UtilsGL.class.getCanonicalName());
	// }
	// }
	public static final void glBegin (final int mode) {
		if (ATI_begin) {
			glEnd ();
		} else {
			ATI_begin = true;
		}

		GL11.glBegin (mode);
	}


	public static final void glEnd () {
		if (ATI_begin) {
			if (!ATI_drawed) {
				Tile tile = World.getTileRedCross ();
				drawTexture (0, 0, tile.getTileWidth (), tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), ColorGL.WHITE, 0);
				GL11.glColor4f (1, 1, 1, 1);
			}

			ATI_begin = false;
			ATI_drawed = false;
			GL11.glEnd ();
		}
	}


	// public static void drawTextureZ (int x0, int y0, int x1, int y1, float texX0, float texY0, float texX1, float texY1, ColorGL color, int z) {
	// GL11.glColor3f (color.r, color.g, color.b);
	// drawTextureZ (x0, y0, x1, y1, texX0, texY0, texX1, texY1, z);
	// }
	//
	//
	// public static void drawTextureZ (int x0, int y0, int x1, int y1, float texX0, float texY0, float texX1, float texY1, ColorGL color, float transparency, int z) {
	// GL11.glColor4f (color.r, color.g, color.b, transparency);
	// drawTextureZ (x0, y0, x1, y1, texX0, texY0, texX1, texY1, z);
	// }
	public static void drawTextureZ (int x0, int y0, int x1, int y1, float texX0, float texY0, float texX1, float texY1, int z) {
		ATI_drawed = true;
		GL11.glTexCoord2f (texX0, texY0);
		GL11.glVertex3i (x0, y0, z);
		GL11.glTexCoord2f (texX0, texY1);
		GL11.glVertex3i (x0, y1, z);
		GL11.glTexCoord2f (texX1, texY1);
		GL11.glVertex3i (x1, y1, z);
		GL11.glTexCoord2f (texX1, texY0);
		GL11.glVertex3i (x1, y0, z);
	}


	// public static void drawTextureZ (int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, float texX0, float texY0, float texX1, float texY1, int z) {
	// ATI_drawed = true;
	// GL11.glTexCoord2f (texX0, texY0);
	// GL11.glVertex3i (x0, y0, z);
	// GL11.glTexCoord2f (texX0, texY1);
	// GL11.glVertex3i (x1, y1, z);
	// GL11.glTexCoord2f (texX1, texY1);
	// GL11.glVertex3i (x2, y2, z);
	// GL11.glTexCoord2f (texX1, texY0);
	// GL11.glVertex3i (x3, y3, z);
	// }
	public static void drawTexture (int x0, int y0, int x1, int y1, float texX0, float texY0, float texX1, float texY1, ColorGL color) {
		GL11.glColor3f (color.r, color.g, color.b);
		drawTexture (x0, y0, x1, y1, texX0, texY0, texX1, texY1);
	}


	public static void drawTexture (int x0, int y0, int x1, int y1, float texX0, float texY0, float texX1, float texY1, ColorGL color, float transparency) {
		GL11.glColor4f (color.r, color.g, color.b, transparency);
		drawTexture (x0, y0, x1, y1, texX0, texY0, texX1, texY1);
	}


	public static void drawTexture (int x0, int y0, int x1, int y1, float texX0, float texY0, float texX1, float texY1) {
		ATI_drawed = true;
		GL11.glTexCoord2f (texX0, texY0);
		GL11.glVertex2i (x0, y0);
		GL11.glTexCoord2f (texX0, texY1);
		GL11.glVertex2i (x0, y1);
		GL11.glTexCoord2f (texX1, texY1);
		GL11.glVertex2i (x1, y1);
		GL11.glTexCoord2f (texX1, texY0);
		GL11.glVertex2i (x1, y0);
	}


	public static void drawTexture (int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, float texX0, float texY0, float texX1, float texY1) {
		ATI_drawed = true;
		GL11.glTexCoord2f (texX0, texY0);
		GL11.glVertex2i (x0, y0);
		GL11.glTexCoord2f (texX0, texY1);
		GL11.glVertex2i (x1, y1);
		GL11.glTexCoord2f (texX1, texY1);
		GL11.glVertex2i (x2, y2);
		GL11.glTexCoord2f (texX1, texY0);
		GL11.glVertex2i (x3, y3);
	}


	// public static void drawStringZ (String sMessage, int x, int y, int z) {
	// drawStringZ (sMessage, x, y, null, z);
	// }
	//
	//
	// public static void drawStringZ (String sMessage, int x, int y, ColorGL color, int z) {
	// if (sMessage == null) {
	// return;
	// }
	//
	// int[] aiMessage = new int [sMessage.length ()];
	// for (int i = 0; i < sMessage.length (); i++) {
	// aiMessage[i] = (int) sMessage.charAt (i);
	// }
	//
	// int xOffset = x;
	// CharDef cd;
	// for (int j = 0; j < aiMessage.length; j++) {
	// cd = UtilFont.getCharDef (sMessage.charAt (j));
	//
	// if (cd == null) {
	// continue;
	// }
	// if (color == null) {
	// GL11.glColor3f (1, 1, 1);
	// } else {
	// GL11.glColor3f (color.r, color.g, color.b);
	// }
	// UtilsGL.drawTextureZ (xOffset, y + cd.yoffset, xOffset + cd.width, y + cd.yoffset + cd.height, cd.xTex, cd.yTex, cd.xTex + cd.widthTex, cd.yTex + cd.heightTex, z);
	// xOffset += cd.xadvance;
	// }
	// }
	public static void drawString (String sMessage, int x, int y) {
		drawString (sMessage, x, y, null);
	}


	public static void drawStringZ (String sMessage, int x, int y, int z) {
		drawStringZ (sMessage, x, y, null, z);
	}


	public static void drawString (String sMessage, int x, int y, ColorGL color) {
		if (sMessage == null) {
			return;
		}

		int[] aiMessage = new int [sMessage.length ()];
		for (int i = 0; i < sMessage.length (); i++) {
			aiMessage[i] = (int) sMessage.charAt (i);
		}

		int xOffset = x;
		CharDef cd;
		for (int j = 0; j < aiMessage.length; j++) {
			cd = UtilFont.getCharDef (sMessage.charAt (j));

			if (cd == null) {
				continue;
			}
			if (color == null) {
				GL11.glColor3f (1, 1, 1);
			} else {
				GL11.glColor3f (color.r, color.g, color.b);
			}
			UtilsGL.drawTexture (xOffset, y + cd.yoffset, xOffset + cd.width, y + cd.yoffset + cd.height, cd.xTex, cd.yTex, cd.xTex + cd.widthTex, cd.yTex + cd.heightTex);
			xOffset += cd.xadvance;
		}
		GL11.glColor3f (1, 1, 1);
	}


	public static void drawStringZ (String sMessage, int x, int y, ColorGL color, int z) {
		if (sMessage == null) {
			return;
		}

		int[] aiMessage = new int [sMessage.length ()];
		for (int i = 0; i < sMessage.length (); i++) {
			aiMessage[i] = (int) sMessage.charAt (i);
		}

		int xOffset = x;
		CharDef cd;
		for (int j = 0; j < aiMessage.length; j++) {
			cd = UtilFont.getCharDef (sMessage.charAt (j));

			if (cd == null) {
				continue;
			}
			if (color == null) {
				GL11.glColor3f (1, 1, 1);
			} else {
				GL11.glColor3f (color.r, color.g, color.b);
			}
			UtilsGL.drawTextureZ (xOffset, y + cd.yoffset, xOffset + cd.width, y + cd.yoffset + cd.height, cd.xTex, cd.yTex, cd.xTex + cd.widthTex, cd.yTex + cd.heightTex, z);
			xOffset += cd.xadvance;
		}
		GL11.glColor3f (1, 1, 1);
	}


	public static void drawStringWithBorder (String sMessage, int x, int y, ColorGL color, ColorGL colorBorder) {
		// Borde
		UtilsGL.drawString (sMessage, x - 1, y - 1, colorBorder);
		UtilsGL.drawString (sMessage, x - 1, y, colorBorder);
		UtilsGL.drawString (sMessage, x - 1, y + 1, colorBorder);
		UtilsGL.drawString (sMessage, x, y - 1, colorBorder);
		UtilsGL.drawString (sMessage, x, y + 1, colorBorder);
		UtilsGL.drawString (sMessage, x + 1, y - 1, colorBorder);
		UtilsGL.drawString (sMessage, x + 1, y, colorBorder);
		UtilsGL.drawString (sMessage, x + 1, y + 1, colorBorder);

		// Centro
		UtilsGL.drawString (sMessage, x, y, color);
	}


	public static void drawStringWithBorderZ (String sMessage, int x, int y, ColorGL color, ColorGL colorBorder, int z) {
		// Borde
		UtilsGL.drawStringZ (sMessage, x - 1, y - 1, colorBorder, z);
		UtilsGL.drawStringZ (sMessage, x - 1, y, colorBorder, z);
		UtilsGL.drawStringZ (sMessage, x - 1, y + 1, colorBorder, z);
		UtilsGL.drawStringZ (sMessage, x, y - 1, colorBorder, z);
		UtilsGL.drawStringZ (sMessage, x, y + 1, colorBorder, z);
		UtilsGL.drawStringZ (sMessage, x + 1, y - 1, colorBorder, z);
		UtilsGL.drawStringZ (sMessage, x + 1, y, colorBorder, z);
		UtilsGL.drawStringZ (sMessage, x + 1, y + 1, colorBorder, z);

		// Centro
		UtilsGL.drawStringZ (sMessage, x, y, color, z);
	}


	public static void drawTooltip (String tooltip, int tooltipX, int tooltipY, int renderWidth, int renderHeight) {
		if (tooltip != null) {
			int tooltipWidth = UtilFont.getWidth (tooltip);
			int tooltipHeight = UtilFont.MAX_HEIGHT;

			if (tooltipX < 0) {
				tooltipX = 0;
			} else if (tooltipY + tooltipHeight + 1 > renderHeight) {
				tooltipY -= ((tooltipY + tooltipHeight + 1) - renderHeight);
			}
			if (tooltipY < 0) {
				tooltipY = 0;
			} else if (tooltipX + tooltipWidth + 1 > renderWidth) {
				tooltipX -= ((tooltipX + tooltipWidth + 1) - renderWidth);
			}

			// Textures
			GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, UIPanel.tileTooltipBackground.getTextureID ());
			glBegin (GL11.GL_QUADS);
			UtilsGL.drawTexture (tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, UIPanel.tileTooltipBackground.getTileSetTexX0 (), UIPanel.tileTooltipBackground.getTileSetTexY0 (), UIPanel.tileTooltipBackground.getTileSetTexX1 (), UIPanel.tileTooltipBackground.getTileSetTexY1 ());
			glEnd ();
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
			glBegin (GL11.GL_QUADS);
			UtilsGL.drawString (tooltip, tooltipX, tooltipY);
			glEnd ();
		}
	}


	/**
	 * deletes the texture with the given id
	 * 
	 * @param texture
	 */
	public static void deleteTexture (TextureData texture) {
		GL11.glDeleteTextures (texture.getTextureID ());
		freeTextureIds.add (texture.getTextureID ());
		if (texture.getFileName () != null) {
			cachedImages.remove (texture.getFileName ());
			cachedImages.put (texture.getFileName (), new ImageData (texture));
			cachedTextureAlphas.remove (texture.getFileName ());
		}
	}


	public static ImageData loadImage (String imageFile) {
		return loadImage (imageFile, false);
	}


	/**
	 * loads a image file and returns its data.
	 * 
	 * @param imageFile
	 * @param reload
	 * @return
	 */
	public static ImageData loadImage (String imageFile, boolean reload) {
		if (cachedImages.containsKey (imageFile)) {
			if (!reload) {
				return cachedImages.get (imageFile);
			} else {
				ImageData image = cachedImages.get (imageFile);
				if (image instanceof TextureData) {
					deleteTexture ((TextureData) image);
				}
			}
		}

		ImageData imageData = null;
		try {
			// Primero miramos que no estén en alguna carpeta de mod
			File fUserFolder = new File (Game.getUserFolder ());
			if (fUserFolder.exists () && fUserFolder.isDirectory ()) {
				ArrayList<String> alMods = Game.getModsLoaded ();
				if (alMods != null && alMods.size () > 0) {
					for (int i = 0; i < alMods.size (); i++) {
						String sModTexture = fUserFolder.getAbsolutePath () + System.getProperty ("file.separator") + Game.MODS_FOLDER1 + System.getProperty ("file.separator") + alMods.get (i) + System.getProperty ("file.separator") + Towns.getPropertiesString ("GRAPHICS_FOLDER") + System.getProperty ("file.separator") + imageFile;
						File fTexture = new File (sModTexture);
						if (fTexture.exists ()) {
							imageData = loadImageData (sModTexture, imageFile);
							break;
						}
					}
				}
			}
			if (imageData == null) {
				imageData = loadImageData (Towns.getPropertiesString ("GRAPHICS_FOLDER") + System.getProperty ("file.separator") + imageFile, imageFile);
			}
		}
		catch (IOException e) {
			// e.printStackTrace();
			Log.log (Log.LEVEL_DEBUG, "Fast decoding of image [" + imageFile + "] failed: " + e.toString (), "UtilsGL");
			// return null;
		}

		// try to load the image over java ImageIO instead
		if (imageData == null) {
			try {
				imageData = loadImageDataImageIO (Towns.getPropertiesString ("GRAPHICS_FOLDER") + System.getProperty ("file.separator") + imageFile, imageFile);
			}
			catch (IOException e) {
				Log.log (Log.LEVEL_ERROR, "ImageIO decoding of image [" + imageFile + "] failed: " + e.toString (), "UtilsGL");
			}
		}

		if (imageData != null) {
			imageData.clearPixels ();
			cachedImages.put (imageFile, imageData);
		} else {
			Log.log (Log.LEVEL_ERROR, "Failed to load image " + imageFile, "UtilsGL");
		}

		return imageData;
	}


	/**
	 * Loads the image data using LWJGL3's stb bindings.
	 * 
	 * @param imageFile
	 * @return
	 */
	private static ImageData loadImageData (String imageFile, String imageName) throws IOException {
		byte[] imageBytes = Files.readAllBytes (new File (imageFile).toPath ());
		ByteBuffer encoded = MemoryUtil.memAlloc (imageBytes.length);
		IntBuffer widthBuffer = BufferUtils.createIntBuffer (1);
		IntBuffer heightBuffer = BufferUtils.createIntBuffer (1);
		IntBuffer channelsBuffer = BufferUtils.createIntBuffer (1);
		ByteBuffer decoded = null;

		try {
			encoded.put (imageBytes);
			encoded.flip ();

			decoded = STBImage.stbi_load_from_memory (encoded, widthBuffer, heightBuffer, channelsBuffer, 4);
			if (decoded == null) {
				throw new IOException (STBImage.stbi_failure_reason ());
			}

			ByteBuffer buffer = ByteBuffer.allocateDirect (decoded.remaining ());
			buffer.put (decoded.asReadOnlyBuffer ());
			buffer.flip ();

			return new ImageData (imageName, widthBuffer.get (0), heightBuffer.get (0), buffer, GL11.GL_RGBA);
		}
		finally {
			if (decoded != null) {
				STBImage.stbi_image_free (decoded);
			}
			MemoryUtil.memFree (encoded);
		}
	}


	private static ImageData loadImageDataImageIO (String imageFile, String imageName) throws IOException {
		BufferedImage image = ImageIO.read (new File (imageFile));
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect (4 * image.getWidth () * image.getHeight ());
		byteBuffer.rewind ();
		IntBuffer intBuffer = byteBuffer.asIntBuffer ();
		intBuffer.rewind ();

		int[] buffer = new int [image.getWidth ()];
		for (int y = 0; y < image.getHeight (); ++y) {
			image.getRGB (0, y, image.getWidth (), 1, buffer, 0, image.getWidth ());
			for (int x = 0; x < image.getWidth (); ++x) {
				int value = buffer[x];

				// convert ARGB to RGBA
				value = (value << 8) | (value >>> 24);
				intBuffer.put (value);
			}
		}
		byteBuffer.flip ();

		return new ImageData (imageName, image.getWidth (), image.getHeight (), byteBuffer, GL11.GL_RGBA);
	}


	/**
	 * Loads the given texture and returns it. If the texture has been loaded before then it is returned from cache or if reloaded is true then it is reloaded from disk.
	 * 
	 * @param imageFile
	 * @param textureMode
	 * @param reload should the texture be reloaded when it was cached before?
	 * @return
	 */
	public static TextureData loadTexture (String imageFile, int textureMode, boolean reload) {
		if (imageFile == null) {
			return null;
		}

		ImageData image = cachedImages.get (imageFile);
		if (image == null || reload) {
			image = loadImage (imageFile, reload);
		}

		if (image != null) {
			if (image instanceof TextureData && !reload) {
				TextureData texture = (TextureData) image;
				return texture;
			} else {
				TextureData texture;
				if (image instanceof TextureData) {
					texture = (TextureData) image;
				} else {
					int textureID = getFreeTextureID ();
					texture = new TextureData (image, textureID);
					texture.clearPixels ();
					cachedImages.put (imageFile, texture);
				}

				loadTexture (image, texture.getTextureID (), textureMode);
				return texture;
			}
		}

		return null; // can't load
	}


	private static int getFreeTextureID () {
		return !freeTextureIds.isEmpty () ? freeTextureIds.poll () : maxTextureId++;
	}


	/**
	 * Loads a texture into video memory
	 * 
	 * @param imageFile
	 * @param textureMode
	 * @return the texture ID, 0 if something fails, -1 in special cases (air)
	 */
	public static TextureData loadTexture (String imageFile, int textureMode) {
		return loadTexture (imageFile, textureMode, false);
	}


	/**
	 * Loads a texture from a image data. Note that these textures are not cached at all.
	 * 
	 * @param image
	 * @param textureMode
	 * @return
	 */
	public static TextureData loadTexture (ImageData image, int textureMode) {
		int textureID = getFreeTextureID ();
		TextureData texture = new TextureData (image, textureID);
		loadTexture (texture, textureID, textureMode);

		return texture;
	}


	/**
	 * reloads the changed texture data into the video memory
	 * 
	 * @param texture
	 */
	public static void reloadTexture (TextureData texture) {
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, texture.getTextureID ());
		GL11.glTexSubImage2D (GL11.GL_TEXTURE_2D, 0, 0, 0, texture.getWidth (), texture.getHeight (), texture.getFormat (), GL11.GL_UNSIGNED_BYTE, texture.getImagePixels ());
	}


	/**
	 * loads a texture into the video memory
	 * 
	 * @param buffer
	 * @param id
	 * @param textureMode
	 * @param width
	 * @param height
	 * @param format
	 * @return
	 */
	private static void loadTexture (ImageData image, int id, int textureMode) {
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, id);
		GL11.glPixelStorei (GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexParameteri (GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri (GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glTexParameteri (GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri (GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexEnvi (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, textureMode);

		GL11.glTexImage2D (GL11.GL_TEXTURE_2D, 0, image.getFormat (), image.getWidth (), image.getHeight (), 0, image.getFormat (), GL11.GL_UNSIGNED_BYTE, image.getImagePixels ());
	}


	/**
	 * Loads a texture into video memory
	 * 
	 * @return texture ID
	 */
	public static int reloadTexture (float[] textureFloats, int ID, float textureMode, int width, int height, int format) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer (textureFloats.length);
		buffer.put (textureFloats);
		buffer = (FloatBuffer) buffer.rewind ();

		GL11.glBindTexture (GL11.GL_TEXTURE_2D, ID);
		GL11.glTexSubImage2D (GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, format, GL11.GL_FLOAT, buffer);

		return ID;
	}


	// /**
	// * Loads a texture into video memory
	// *
	// * @return texture ID
	// */
	// public static int loadTexture(float[] textureFloats, int ID, float textureMode, int width, int height, int format) {
	// FloatBuffer buffer = BufferUtils.createFloatBuffer(textureFloats.length);
	// buffer.put(textureFloats);
	// buffer = (FloatBuffer) buffer.rewind();
	//
	// GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID);
	// GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
	// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
	// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
	// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
	// GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, textureMode);
	//
	// GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format, width, height, 0, format, GL11.GL_FLOAT, buffer);
	//
	// return ID;
	// }

	/**
	 * Return the Image pixels in RGBA format.
	 * 
	 * @param image
	 * @return the ImageData
	 */
	public static ImageData toImage (Image image) {
		if (image == null) {
			return null;
		}
		int width, height;

		int[] pixelsARGB;
		width = image.getWidth (null);
		height = image.getHeight (null);
		pixelsARGB = new int [width * height];
		PixelGrabber pg = new PixelGrabber (image, 0, 0, width, height, pixelsARGB, 0, width);
		try {
			pg.grabPixels ();
		}
		catch (InterruptedException e) {
			return null;
		}

		ByteBuffer buffer = ByteBuffer.allocateDirect (pixelsARGB.length * 4);
		buffer.rewind ();
		int p, r, g, b, a;
		for (int i = 0; i < pixelsARGB.length; i++) {
			p = pixelsARGB[i];
			a = (p >> 24) & 0xFF; // get pixel bytes in ARGB order
			r = (p >> 16) & 0xFF;
			g = (p >> 8) & 0xFF;
			b = p & 0xFF;

			buffer.put ((byte) r);
			buffer.put ((byte) g);
			buffer.put ((byte) b);
			buffer.put ((byte) a);
		}

		return new ImageData (null, width, height, buffer, GL11.GL_RGBA);
	}


	// public static void clearAllCached() {
	// System.out.println (cachedImages.size ());
	// if (cachedImages != null) {
	// Iterator<String> itImages = cachedImages.keySet ().iterator ();
	// ArrayList<String> alImages = new ArrayList<String> ();
	// while (itImages.hasNext ()) {
	// String sAux = itImages.next ();
	// if (sAux != null) {
	// alImages.add (sAux);
	// }
	// }
	//
	// for (int i = 0; i < alImages.size (); i++) {
	// ImageData id = cachedImages.get (alImages.get (i));
	// if (id instanceof TextureData) {
	// deleteTexture ((TextureData) id);
	// }
	// }
	// cachedImages.clear ();
	// }
	// clearCachedAlphas ();
	// maxTextureId = 1;
	// if (freeTextureIds != null) {
	// freeTextureIds.clear ();
	// }
	// }

	public static void clearCachedAlphas () {
		if (cachedTextureAlphas != null) {
			cachedTextureAlphas.clear ();
		}
	}


	/**
	 * Obtenemos la parte alpha del tile pasado
	 * 
	 * @param tile
	 * @return
	 */
	public static boolean[][] generateAlpha (Tile tile) {
		String sFileName = Towns.getPropertiesString (PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + tile.getIniHeader () + "]TEXTURE_FILE"); //$NON-NLS-1$ //$NON-NLS-2$
		return generateAlpha (tile, tile.getTileWidth (), tile.getTileHeight (), sFileName);
	}


	public static boolean[][] generateAlpha (Tile tile, int width, int height) {
		String sFileName = Towns.getPropertiesString (PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + tile.getIniHeader () + "]TEXTURE_FILE"); //$NON-NLS-1$ //$NON-NLS-2$
		return generateAlpha (tile, width, height, sFileName);
	}


	/**
	 * Obtenemos la parte alpha del tile pasado
	 * 
	 * @param tile
	 * @param sFileName
	 * @return
	 */
	public static boolean[][] generateAlpha (Tile tile, String sFileName) {
		return generateAlpha (tile, tile.getTileWidth (), tile.getTileHeight (), sFileName);
	}


	public static boolean[][] generateAlpha (Tile tile, int width, int height, String sFilename) {
		boolean[][] alphaArray = new boolean [tile.getTileWidth ()] [tile.getTileHeight ()];
		boolean[][] storedAlpha = cachedTextureAlphas.get (sFilename);
		if (storedAlpha == null) {
			ImageData image = loadImage (sFilename);
			if (image == null) {
				return null;
			}
			storedAlpha = UtilsGL.createAlpha (image);
			cachedTextureAlphas.put (sFilename, storedAlpha);
		}

		if (storedAlpha.length != width || storedAlpha[0].length != height) {
			boolean[][] alphaStored = cachedTextureAlphas.get (sFilename);
			int iXIndex = Tile.TERRAIN_ICON_WIDTH * tile.getTileSetX ();
			int iYIndex = Tile.TERRAIN_ICON_HEIGHT * tile.getTileSetY ();
			for (int x = 0; x < tile.getTileWidth (); x++) {
				for (int y = 0; y < tile.getTileHeight (); y++) {
					alphaArray[x][y] = alphaStored[iXIndex + x][iYIndex + y];
				}
			}

		} else {
			alphaArray = storedAlpha;
		}

		return generateAlpha (alphaArray, width, height);

	}


	public static boolean[][] generateAlpha (boolean[][] alphaArray, int width, int height) {
		// Si llega aquí es que es un tamaño personalizado
		boolean[][] alphaPersonal = new boolean [width] [height];
		float relationW = (float) alphaArray.length / (float) width;
		float relationH = (float) alphaArray[0].length / (float) height;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				alphaPersonal[x][y] = alphaArray[(int) (x * relationW)][(int) (y * relationH)];
			}
		}

		return alphaPersonal;
	}


	/**
	 * creates a mask from the given image data
	 * 
	 * @param imageData
	 * @return
	 */
	private static boolean[][] createAlpha (ImageData imageData) {
		boolean[][] alphaArray = new boolean [imageData.getWidth ()] [imageData.getHeight ()];
		final ByteBuffer imagePixels = imageData.getImagePixels ();

		imagePixels.rewind ();
		for (int y = 0; y < imageData.getHeight (); ++y) {
			for (int x = 0; x < imageData.getWidth (); ++x) {
				imagePixels.get ();
				imagePixels.get ();
				imagePixels.get ();
				alphaArray[x][y] = imagePixels.get () == 0; // RGB_A
			}
		}

		return alphaArray;
	}


	/**
	 * Cambia la textura si es necesario. Devuelve la textura seteada
	 * 
	 * @param iTexture
	 * @param iCurrentTexture
	 * @return la textura seteada
	 */
	public static int setTexture (Tile tile, int iCurrentTexture) {
		int iTexture = tile.getTextureID ();

		if (iTexture != iCurrentTexture) {
			UtilsGL.glEnd ();
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, iTexture);
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
		}

		return iTexture;
	}


	public static int setTexture (int iCurrentTexture, int iNewTexture) {
		if (iNewTexture != iCurrentTexture) {
			UtilsGL.glEnd ();
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, iNewTexture);
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
		}

		return iNewTexture;
	}


	/**
	 * Cambia la textura Y añade brillo
	 * 
	 * @param iTexture
	 * @return la textura seteada
	 */
	public static int setTextureBrightness (Tile tile, Cell cell, boolean transparency) {
		int iTexture = tile.getTextureID ();

		float fRed;
		if (cell.isLightRedFull ()) {
			fRed = 0.2f;
		} else if (cell.isLightRedHalf ()) {
			fRed = 0.1f;
		} else {
			fRed = 0.05f;
		}
		float fGreen;
		if (cell.isLightGreenFull ()) {
			fGreen = 0.2f;
		} else if (cell.isLightGreenHalf ()) {
			fGreen = 0.1f;
		} else {
			fGreen = 0.05f;
		}
		float fBlue;
		if (cell.isLightBlueFull ()) {
			fBlue = 0.2f;
		} else if (cell.isLightBlueHalf ()) {
			fBlue = 0.1f;
		} else {
			fBlue = 0.05f;
		}

		UtilsGL.glEnd ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_ADD);

		if (cell.isLight ()) {
			if (transparency) {
				GL11.glColor4f (fRed, fGreen, fBlue, MainPanel.TRANSPARENCY);
			} else {
				GL11.glColor3f (fRed, fGreen, fBlue);
			}
		} else {
			GlobalEventData ged = Game.getWorld ().getGlobalEvents ();
			if (transparency) {
				GL11.glColor4f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue (), MainPanel.TRANSPARENCY);
			} else {
				GL11.glColor3f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue ());
			}
		}
		UtilsGL.glBegin (GL11.GL_QUADS);

		return iTexture;
	}


	/**
	 * Cambia la textura Y añade brillo
	 * 
	 * @param iTexture
	 * @return la textura seteada
	 */
	public static int setTextureBrightnessBright (Tile tile, Cell cell, boolean transparency) {
		int iTexture = tile.getTextureID ();

		float fRed;
		if (cell.isLightRedFull ()) {
			fRed = 0.2f;
		} else {
			fRed = 0.1f;
		}
		float fGreen;
		if (cell.isLightGreenFull ()) {
			fGreen = 0.2f;
		} else {
			fGreen = 0.1f;
		}
		float fBlue;
		if (cell.isLightBlueFull ()) {
			fBlue = 0.2f;
		} else {
			fBlue = 0.1f;
		}

		UtilsGL.glEnd ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_ADD);

		if (cell.isLight ()) {
			if (transparency) {
				GL11.glColor4f (fRed, fGreen, fBlue, MainPanel.TRANSPARENCY);
			} else {
				GL11.glColor3f (fRed, fGreen, fBlue);
			}
		} else {
			GlobalEventData ged = Game.getWorld ().getGlobalEvents ();
			if (transparency) {
				GL11.glColor4f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue (), MainPanel.TRANSPARENCY);
			} else {
				GL11.glColor3f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue ());
			}
		}
		UtilsGL.glBegin (GL11.GL_QUADS);

		return iTexture;
	}


	public static void setColorGreen () {
		GL11.glColor3f (0.2f, 1f, 0.2f);
	}


	public static void setColorRed () {
//		int iTexture = tile.getTextureID ();
//
//		UtilsGL.glEnd ();
//		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iTexture);

//		if (Game.OPENGL_13_AVAILABLE) {
//			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
//			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_ADD);
//			GL11.glColor3f (0.4f, 0f, 0f);
//			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL13.GL_SUBTRACT);
//			GL11.glColor3f (0f, 0.5f, 0.5f);
//		} else {
//			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			GL11.glColor3f (1f, 0.2f, 0.2f);
//		}
//		UtilsGL.glBegin (GL11.GL_QUADS);

//		return iTexture;
	}

	public static void unsetColor () {
		GL11.glColor3f (1f, 1f, 1f);
	}


	public static int setTextureBrightnessDarker (Tile tile, Cell cell, boolean transparency) {
		int iTexture = tile.getTextureID ();

		float fRed;
		if (cell.isLightRedFull ()) {
			fRed = 0.15f;
		} else if (cell.isLightRedHalf ()) {
			fRed = 0.05f;
		} else {
			fRed = 0;
		}
		float fGreen;
		if (cell.isLightGreenFull ()) {
			fGreen = 0.15f;
		} else if (cell.isLightGreenHalf ()) {
			fGreen = 0.05f;
		} else {
			fGreen = 0;
		}
		float fBlue;
		if (cell.isLightBlueFull ()) {
			fBlue = 0.15f;
		} else if (cell.isLightBlueHalf ()) {
			fBlue = 0.05f;
		} else {
			fBlue = 0;
		}

		UtilsGL.glEnd ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_ADD);

		if (cell.isLight ()) {
			if (transparency) {
				GL11.glColor4f (fRed, fGreen, fBlue, MainPanel.TRANSPARENCY);
			} else {
				GL11.glColor3f (fRed, fGreen, fBlue);
			}
		} else {
			GlobalEventData ged = Game.getWorld ().getGlobalEvents ();
			if (transparency) {
				GL11.glColor4f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue (), MainPanel.TRANSPARENCY);
			} else {
				GL11.glColor3f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue ());
			}
		}

		UtilsGL.glBegin (GL11.GL_QUADS);

		return iTexture;
	}


	public static void destroy () {
		Display.destroy ();
	}
}
