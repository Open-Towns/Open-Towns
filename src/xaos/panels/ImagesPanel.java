package xaos.panels;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.lwjgl.opengl.GL11;

import xaos.campaign.MissionData;
import xaos.campaign.TutorialFlow;
import xaos.campaign.TutorialTrigger;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.panels.UI.UIPanel;
import xaos.tiles.Tile;
import xaos.utils.ColorGL;
import xaos.utils.TextureData;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;
import static xaos.panels.UI.UIPanel.*;
import static xaos.panels.UI.UIPanelState.*;
import static xaos.panels.UI.UIPanelInputHandler.*;


public class ImagesPanel {

	public static int WIDTH = 800;
	public static int HEIGHT = 600;
	public static int TEXT_X = 32;
	public static int MAX_TEXT_WIDTH = 300;

	public static final int MINI_IMAGE_WIDTH = 128;
	public static final int MINI_IMAGE_HEIGHT = 256;

	private static boolean visible;

	// Points
	private static Point panelPoint = new Point (0, 0);
	private static Point closeButtonPoint = new Point (0, 0);
	private static Point nextImagePoint = new Point (0, 0);
	private static Point previousImagePoint = new Point (0, 0);
	private static Point nextMissionPoint = new Point (0, 0);

	// Tiles
	private static Tile[] tilePanel;
	private static Tile tileNext;
	private static Tile tileNextDisabled;
	private static Tile tilePrevious;
	private static Tile tilePreviousDisabled;
	private static Tile tileNextMission;

	// Images
	private static HashMap<String, TextureData> hashImageTextureData = new HashMap<String, TextureData> ();

	// Flow indexes
	private static int currentFlowIndex = 0;
	private static int maxFlowIndex = 0;
	// private static int maxFlowReaded = -1;

	// Tile
	private static Tile currentTile = null;

	// Texts
	private static ArrayList<String> alTexts = new ArrayList<String> ();


	public ImagesPanel (int renderWidth, int renderHeight, MissionData missionData) {
		createPanel (renderWidth, renderHeight);
		loadImages (missionData);
	}


	private static void createPanel (int renderWidth, int renderHeight) {
		resize (renderWidth, renderHeight);
	}


	public static void render (int mousePanel) {
		// Fondo
		GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tilePanel[0].getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, tilePanel[0].getTextureID ());
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		UIPanel.renderBackground (tilePanel, panelPoint, WIDTH, HEIGHT);

		// Get the current flow
		TutorialFlow tutorialFlow = null;
		if (Game.getCurrentMissionData () != null && getCurrentFlowIndex () < Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
			tutorialFlow = Game.getCurrentMissionData ().getTutorialFlows ().get (getCurrentFlowIndex ());
		}

		if (tutorialFlow != null) {
			// if (getMaxFlowReaded () < getCurrentFlowIndex ()) {
			// setMaxFlowReaded (getCurrentFlowIndex ());
			// }

			UtilsGL.glEnd ();
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);

			int iY = panelPoint.y + UtilFont.MAX_HEIGHT;
			// Title
			if (tutorialFlow.getTitle () != null) {
				UtilsGL.drawStringWithBorder (tutorialFlow.getTitle (), panelPoint.x + (WIDTH / 2) - UtilFont.getWidth (tutorialFlow.getTitle ()) / 2, iY, ColorGL.WHITE, ColorGL.BLACK);
			}

			// Text
			if (tutorialFlow.getTexts ().size () > 0) {
				if (alTexts.size () == 0) {
					parseTutorialTexts (tutorialFlow.getTexts ());
				}

				if (alTexts.size () > 0) {
					iY += UtilFont.MAX_HEIGHT * 2;
					for (int i = 0; i < alTexts.size (); i++) {
						iY += UtilFont.MAX_HEIGHT;
						//UtilsGL.drawStringWithBorder (alTexts.get (i), TEXT_X, iY, ColorGL.WHITE, ColorGL.BLACK);
						UtilsGL.drawString (alTexts.get (i), TEXT_X, iY, ColorGL.BLACK);
					}
				}
			}

			UtilsGL.glEnd ();

			iCurrentTexture = tilePanel[0].getTextureID ();
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);

			// Image
			if (tutorialFlow.getImage () != null) {
				iCurrentTexture = UtilsGL.setTexture (iCurrentTexture, hashImageTextureData.get (tutorialFlow.getImage ()).getTextureID ());
				// UtilsGL.drawTexture (panelPoint.x, panelPoint.y, panelPoint.x + WIDTH, panelPoint.y + HEIGHT, 0, 0, 1, 1);
				// UtilsGL.drawTexture (panelPoint.x + WIDTH / 2 - 256, panelPoint.y + HEIGHT / 2 - 256, panelPoint.x + WIDTH / 2 + 256, panelPoint.y + HEIGHT / 2 + 256, 0, 0, 1, 1);
				UtilsGL.drawTexture (panelPoint.x - MINI_IMAGE_WIDTH / 2, panelPoint.y - 64, panelPoint.x + MINI_IMAGE_WIDTH / 2, panelPoint.y + (MINI_IMAGE_HEIGHT - 64), 0, 0, 1, 1);
			}

			// Tile
			if (currentTile != null) {
				iCurrentTexture = UtilsGL.setTexture (currentTile, iCurrentTexture);
				UtilsGL.drawTexture (panelPoint.x - currentTile.getTileWidth () / 2, panelPoint.y - 64, panelPoint.x + currentTile.getTileWidth () / 2, panelPoint.y + (currentTile.getTileHeight () - 64), currentTile.getTileSetTexX0 (), currentTile.getTileSetTexY0 (), currentTile.getTileSetTexX1 (), currentTile.getTileSetTexY1 ());
			}

			// Next mission button
			if (tutorialFlow.getNextMission () != null) {
				iCurrentTexture = UtilsGL.setTexture (tileNextMission, iCurrentTexture);
				UIPanel.drawTile (tileNextMission, nextMissionPoint, mousePanel == MOUSE_IMAGES_PANEL_NEXT_MISSION);
			}
		}

		// Close button
		if (mousePanel == MOUSE_IMAGES_PANEL_CLOSE) {
			iCurrentTexture = UtilsGL.setTexture (tileButtonClose, iCurrentTexture);
			drawTile (tileButtonClose, closeButtonPoint);
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileButtonCloseDisabled, iCurrentTexture);
			drawTile (tileButtonCloseDisabled, closeButtonPoint);
		}

		// Previous button
		if (getCurrentFlowIndex () <= 0) {
			iCurrentTexture = UtilsGL.setTexture (tilePreviousDisabled, iCurrentTexture);
			UIPanel.drawTile (tilePreviousDisabled, previousImagePoint);
		} else {
			iCurrentTexture = UtilsGL.setTexture (tilePrevious, iCurrentTexture);
			UIPanel.drawTile (tilePrevious, previousImagePoint, mousePanel == MOUSE_IMAGES_PANEL_PREVIOUS);
		}

		// Next button
		if (getCurrentFlowIndex () < getMaxFlowIndex ()) {
			iCurrentTexture = UtilsGL.setTexture (tileNext, iCurrentTexture);
			drawTile (tileNext, nextImagePoint, mousePanel == MOUSE_IMAGES_PANEL_NEXT);
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileNextDisabled, iCurrentTexture);
			drawTile (tileNextDisabled, nextImagePoint);
		}

		UtilsGL.glEnd ();
	}


	private static void parseTutorialTexts (ArrayList<String> texts) {
		notifyFlowChanged ();

		for (int i = 0; i < texts.size (); i++) {
			parseTutorialText (texts.get (i));

			// Blank line
			if ((i + 1) < texts.size ()) {
				alTexts.add (new String ());
			}
		}
	}


	private static void parseTutorialText (String sText) {
		int iMaxChars = UtilFont.getMaxCharsByWidth (sText, MAX_TEXT_WIDTH - UtilFont.MAX_WIDTH);
		if (sText.length () > iMaxChars) {
			alTexts.add (new String (sText.substring (0, iMaxChars)));
			parseTutorialText (sText.substring (iMaxChars).trim ());
		} else {
			alTexts.add (sText);
		}
	}


	public static boolean previousFlow () {
		setCurrentFlowIndex (getCurrentFlowIndex () - 1);
		if (getCurrentFlowIndex () < 0) {
			setCurrentFlowIndex (0);
		} else {
			return true;
		}

		return false;
	}


	public static boolean nextFlow () {
		setCurrentFlowIndex (getCurrentFlowIndex () + 1);
		if (getCurrentFlowIndex () > getMaxFlowIndex ()) {
			setCurrentFlowIndex (getMaxFlowIndex ());
		} else {
			Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_NONE, 0, null, null, false);
			return true;
		}

		return false;
	}


	public static void triggerNewFlow (boolean bAdvance) {
		setMaxFlowIndex (getMaxFlowIndex () + 1);

		if (bAdvance) {
			setCurrentFlowIndex (getMaxFlowIndex ());
		}

		if (!isVisible ()) {
			setVisible (true);
		}
	}


	public static boolean nextMission () {
		String sCampaign = null;
		String sMission = null;

		// Next mission
		if (Game.getCurrentMissionData () != null) {
			if (getCurrentFlowIndex () >= 0 && getCurrentFlowIndex () <= Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
				String sNextMission = Game.getCurrentMissionData ().getTutorialFlows ().get (getCurrentFlowIndex ()).getNextMission ();

				if (sNextMission != null) {
					StringTokenizer tokenizer = new StringTokenizer (sNextMission, ",");
					if (tokenizer.hasMoreTokens ()) {
						sCampaign = tokenizer.nextToken ();
						if (tokenizer.hasMoreTokens ()) {
							sMission = tokenizer.nextToken ();
						}
					}

				}
			}
		}

		if (sCampaign != null && sMission != null) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_EXIT_TO_MAIN_MENU_NOSAVE, null, null, null, null, 0);
			CommandPanel.executeCommand (CommandPanel.COMMAND_MM_NEWGAME, sCampaign, sMission, null, null, 0);
			return true;
		}

		return false;
	}


	public static void loadImages (MissionData missionData) {
		clearImages ();
		setCurrentFlowIndex (0);

		if (missionData != null) {
			for (int i = 0; i < missionData.getTutorialFlows ().size (); i++) {
				if (missionData.getTutorialFlows ().get (i).getImage () != null && !hashImageTextureData.containsKey (missionData.getTutorialFlows ().get (i).getImage ())) {
					TextureData td = UtilsGL.loadTexture (missionData.getTutorialFlows ().get (i).getImage (), GL11.GL_MODULATE);
					if (td != null) {
						td.clearPixels ();
						hashImageTextureData.put (missionData.getTutorialFlows ().get (i).getImage (), td);
					}
				}
			}
		}

		setMaxFlowIndex (0);
		// setMaxFlowReaded (-1);
	}


	private static void clearImages () {
		if (hashImageTextureData.size () > 0) {
			// Just in case an image is duplicated we will create a non-duplicate list
			ArrayList<TextureData> alNonDuplicatesList = new ArrayList<TextureData> ();

			Iterator<String> iterator = hashImageTextureData.keySet ().iterator ();
			while (iterator.hasNext ()) {
				String sAux = iterator.next ();
				if (!duplicatedTexture (alNonDuplicatesList, hashImageTextureData.get (sAux))) {
					alNonDuplicatesList.add (hashImageTextureData.get (sAux));
				}
			}

			for (int i = 0; i < alNonDuplicatesList.size (); i++) {
				UtilsGL.deleteTexture (alNonDuplicatesList.get (i));
			}

			hashImageTextureData.clear ();
		}
	}


	private static boolean duplicatedTexture (ArrayList<TextureData> alTextures, TextureData texture) {
		for (int i = 0; i < alTextures.size (); i++) {
			if (texture.getTextureID () == alTextures.get (i).getTextureID ()) {
				return true;
			}
		}

		return false;
	}


	public static void setVisible (boolean visible) {
		ImagesPanel.visible = visible;

		if (Game.getCurrentMissionData () == null) { // || Game.getCurrentMissionData ().getTutorialFlowIndex () >= Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
			ImagesPanel.visible = false;
		}
	}


	public static boolean isVisible () {
		return visible;
	}


	public static Point getPanelPoint () {
		return panelPoint;
	}


	public static void setPanelPoint (Point panelPoint) {
		ImagesPanel.panelPoint = panelPoint;
	}


	public static Point getCloseButtonPoint () {
		return closeButtonPoint;
	}


	public static void setCloseButtonPoint (Point closeButtonPoint) {
		ImagesPanel.closeButtonPoint = closeButtonPoint;
	}


	public static Point getNextImagePoint () {
		return nextImagePoint;
	}


	public static Point getNextMissionPoint () {
		return nextMissionPoint;
	}


	public static Point getPreviousImagePoint () {
		return previousImagePoint;
	}


	public static Tile getTileNext () {
		return tileNext;
	}


	public static Tile getTileNextMission () {
		return tileNextMission;
	}


	public static Tile getTilePrevious () {
		return tilePrevious;
	}


	public static void setCurrentFlowIndex (int currentFlowIndex) {
		// If the previous flow have PLACE triggers we should set the cell blinking to OFF
		boolean bDifferentFlows = currentFlowIndex != ImagesPanel.currentFlowIndex;

		if (bDifferentFlows) {
			Game.setBlinkingCells (ImagesPanel.currentFlowIndex, false);
		}

		ImagesPanel.currentFlowIndex = currentFlowIndex;
		notifyFlowChanged ();
		
		// Tile
		if (Game.getCurrentMissionData () != null && currentFlowIndex >= 0 && currentFlowIndex < Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
			TutorialFlow flow = Game.getCurrentMissionData ().getTutorialFlows ().get (currentFlowIndex);
			if (flow.getTile () == null) {
				currentTile = null;
			} else {
				currentTile = new Tile (flow.getTile ());
			}
		}
		

		if (bDifferentFlows) {
			// Blinking cells?
			Game.setBlinkingCells (ImagesPanel.currentFlowIndex, true);

			// Events?
			if (Game.getCurrentMissionData () != null && currentFlowIndex >= 0 && currentFlowIndex < Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
				TutorialFlow flow = Game.getCurrentMissionData ().getTutorialFlows ().get (currentFlowIndex);
				if (flow.getStartEvents () != null) {
					for (int i = 0; i < flow.getStartEvents ().size (); i++) {
						EventManagerItem emi = EventManager.getItem (flow.getStartEvents ().get (i));
						if (emi != null) {
							Game.getWorld ().addEvent (emi);
						}
					}
				}
			}
		}
	}


	public static int getCurrentFlowIndex () {
		return currentFlowIndex;
	}


	public static void setMaxFlowIndex (int maxFlowIndex) {
		ImagesPanel.maxFlowIndex = maxFlowIndex;
		if (Game.getCurrentMissionData () != null) {
			if (ImagesPanel.maxFlowIndex >= Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
				ImagesPanel.maxFlowIndex = (Game.getCurrentMissionData ().getTutorialFlows ().size () - 1);
			}
		} else {
			ImagesPanel.maxFlowIndex = 0;
		}
	}


	public static int getMaxFlowIndex () {
		return maxFlowIndex;
	}


	// public static void setMaxFlowReaded (int maxFlowReaded) {
	// ImagesPanel.maxFlowReaded = maxFlowReaded;
	// }
	//
	//
	// public static int getMaxFlowReaded () {
	// return maxFlowReaded;
	// }

	public static void resize (int renderWidth, int renderHeight) {
		if (tilePanel == null) {
			tilePanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
			tilePanel[0] = new Tile ("images_panel"); //$NON-NLS-1$
			tilePanel[1] = new Tile ("images_panel_N"); //$NON-NLS-1$
			tilePanel[2] = new Tile ("images_panel_S"); //$NON-NLS-1$
			tilePanel[3] = new Tile ("images_panel_E"); //$NON-NLS-1$
			tilePanel[4] = new Tile ("images_panel_W"); //$NON-NLS-1$
			tilePanel[5] = new Tile ("images_panel_NE"); //$NON-NLS-1$
			tilePanel[6] = new Tile ("images_panel_NW"); //$NON-NLS-1$
			tilePanel[7] = new Tile ("images_panel_SE"); //$NON-NLS-1$
			tilePanel[8] = new Tile ("images_panel_SW"); //$NON-NLS-1$

			// Tiles
			tileNext = new Tile ("scrollright");
			tileNextDisabled = new Tile ("scrollright_disabled");
			tilePrevious = new Tile ("scrollleft");
			tilePreviousDisabled = new Tile ("scrollleft_disabled");
			tileNextMission = new Tile ("nextmission");
		}

		// Width / height
//		WIDTH = UtilsGL.getWidth () - 512 + 2 * tilePanel[3].getTileWidth ();
		WIDTH = 384 + 2 * tilePanel[3].getTileWidth ();
//		HEIGHT = UtilsGL.getHeight () - 256 + 2 * tilePanel[1].getTileHeight ();
		HEIGHT = 384 + 2 * tilePanel[1].getTileHeight ();

		// Panel x,y
//		panelPoint.setLocation (renderWidth / 2 - WIDTH / 2, renderHeight / 2 - HEIGHT / 2);
		int iIconSize = 64;
		if (tileIconTutorial != null) {
			iIconSize = tileIconTutorial.getTileWidth ();
		}
		panelPoint.setLocation (PIXELS_TO_BORDER + iIconSize + MINI_IMAGE_WIDTH / 2 + getImagesPanelOffset (), renderHeight / 2 - (UtilsGL.getHeight () - 256 + 2 * tilePanel[1].getTileHeight ()) / 2);

		// Close button
		closeButtonPoint.setLocation (panelPoint.x + WIDTH - tileButtonClose.getTileWidth (), panelPoint.y);

		// Previous button
		previousImagePoint.setLocation (panelPoint.x + WIDTH / 2 - 2 * tilePrevious.getTileWidth (), panelPoint.y + HEIGHT - tilePrevious.getTileHeight () - tilePrevious.getTileHeight () / 4);

		// Next button
		nextImagePoint.setLocation (panelPoint.x + WIDTH / 2 + tileNext.getTileWidth (), panelPoint.y + HEIGHT - tileNext.getTileHeight () - tileNext.getTileHeight () / 4);

		// Next mission button
		nextMissionPoint.setLocation (panelPoint.x + WIDTH - 2 * tileNextMission.getTileWidth (), panelPoint.y + HEIGHT - tileNextMission.getTileHeight () - tileNextMission.getTileHeight () / 4);

		// Text X and WIDTH
		int iTextXStart = panelPoint.x + MINI_IMAGE_WIDTH / 2 + 32;
		int iTextXEnd = panelPoint.x + WIDTH - 32;
		// int iTextYStart = panelPoint.y + 64;
		// int iTextYEnd = panelPoint.x + HEIGHT - 128;
		TEXT_X = iTextXStart;
		MAX_TEXT_WIDTH = iTextXEnd - iTextXStart;

		notifyFlowChanged ();
	}


	public static void notifyFlowChanged () {
		alTexts.clear ();
	}


	public static void clear () {
		notifyFlowChanged ();
		clearImages ();

		currentTile = null;
		currentFlowIndex = 0;
		maxFlowIndex = 0;
	}
}
