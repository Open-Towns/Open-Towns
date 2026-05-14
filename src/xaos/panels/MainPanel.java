package xaos.panels;

import java.awt.Point;
import java.util.ArrayList;

import xaos.platform.lwjgl3.input.Mouse;
import org.lwjgl.opengl.GL11;
import xaos.TownsProperties;

import xaos.campaign.TutorialFlow;
import xaos.campaign.TutorialTrigger;
import xaos.data.CitizenData;
import xaos.data.EffectData;
import xaos.data.EquippedData;
import xaos.data.GlobalEventData;
import xaos.data.HeroData;
import xaos.data.SoldierData;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.data.Type;
import xaos.effects.EffectManager;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.UI.UIPanel;
import xaos.panels.UI.UIPanelInputHandler;
import xaos.panels.menus.ContextMenu;
import xaos.panels.menus.SmartMenu;
import xaos.stockpiles.Stockpile;
import xaos.tasks.Task;
import xaos.tiles.Cell;
import xaos.tiles.Tile;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.entities.living.enemies.Enemy;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.tiles.entities.living.projectiles.Projectile;
import xaos.tiles.terrain.Terrain;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.tiles.terrain.special.Lava;
import xaos.tiles.terrain.special.Water;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.UtilFont;
import xaos.utils.Utils;
import xaos.utils.UtilsGL;
import xaos.zones.Zone;
import xaos.zones.ZoneBarracks;
import xaos.zones.ZoneHeroRoom;
import xaos.zones.ZoneManager;
import xaos.zones.ZoneManagerItem;
import xaos.zones.ZonePersonal;
import static xaos.panels.UI.UIPanelState.*;
import static xaos.panels.UI.UIPanelInputHandler.*;
import static xaos.panels.UI.UIPanel.*;

public final class MainPanel {

	private static final long serialVersionUID = 6859663021221287027L;

	private static Tile GRID_TILE = new Tile ("grid"); //$NON-NLS-1$
	private static Tile GRID_NOT_ALLOWED_TILE = new Tile ("gridNA"); //$NON-NLS-1$
	private static Tile MOUSE_3D_TILE = new Tile ("mouseCursor3D"); //$NON-NLS-1$
	private static Tile MOUSE_ARROW_WEST = new Tile ("mouseArrowWest"); //$NON-NLS-1$
	private static Tile MOUSE_ARROW_NORTH = new Tile ("mouseArrowNorth"); //$NON-NLS-1$
	private static Tile MOUSE_ARROW_EAST = new Tile ("mouseArrowEast"); //$NON-NLS-1$
	private static Tile MOUSE_ARROW_SOUTH = new Tile ("mouseArrowSouth"); //$NON-NLS-1$

	public static final float TRANSPARENCY = (float) 0.4;

	public static int renderWidth = 1024;
	public static int renderHeight = 600;

	// Máximo de tiles a pintar
	private static int maxTilesWidthHeight;
	private static int maxTilesX;
	private static int maxTilesY;

	// Centro de la pantalla
	public static int xCentro;
	public static int yCentro;

	public static boolean flatMouseON;
	public static boolean tDMouseON;
	public static boolean gridON;
	public static boolean bMiniBlocksON;
	public static boolean bHideUION;
	// public static int depthXMin = 0;
	// public static int depthYMin = 0;

	public static int itemBuildFace = Item.FACE_WEST;

	// Blinks
	private static boolean bCheckBlinkPiles = false;
	private static boolean bCheckBlinkItems = false;
	private static boolean bCheckBlinkCells = false;

	// LOCKED WALLCONNECTOR
	private static Tile lockedConnectorTile = new Tile ("lockedconnector"); //$NON-NLS-1$


	public MainPanel () {
		resize (UtilsGL.getWidth (), UtilsGL.getHeight ());
		flatMouseON = true;
		tDMouseON = true;
		gridON = false;
		bHideUION = false;
	}


	public static void toggleFlatMouse () {
		flatMouseON = !flatMouseON;
		Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_FLATCURSOR, null);
	}


	public static void toggle3DMouse () {
		tDMouseON = !tDMouseON;

		if (Game.getCurrentState () == Game.STATE_CREATING_TASK && Game.getCurrentTask ().getState () == Task.STATE_CREATING_ENDZONE) {
			Game.deleteCurrentTask ();
		}

		Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_3DMOUSE, null);

		// if (tDMouseON) {
		// // Acaba de activarlo
		// if (Game.getCurrentState () == Game.STATE_CREATING_TASK && Game.getCurrentTask ().getState () == Task.STATE_CREATING_ENDZONE) {
		// Point3D p3dIni = Game.getCurrentTask ().getPointIni ();
		// // Game.getCurrentTask ().setPointIni (new Point3D (p3dIni.x, p3dIni.y, getMaxZ3DMouse (p3dIni.x, p3dIni.y, p3dIni.z)));
		// Game.getCurrentTask ().setPointIni (getMax3DMouse (p3dIni.x, p3dIni.y, p3dIni.z));
		// }
		// }
	}


	public static void toggleGrid () {
		gridON = !gridON;

		Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_GRID, null);
	}


	public static void toggleMiniBlocks () {
		bMiniBlocksON = !bMiniBlocksON;
		Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_FLAT, null);
	}


	public static void toggleHideUI () {
		bHideUION = !bHideUION;
	}


	public static void toggleItemBuildFace () {
		if (itemBuildFace == Item.FACE_WEST) {
			itemBuildFace = Item.FACE_NORTH;
		} else if (itemBuildFace == Item.FACE_NORTH) {
			itemBuildFace = Item.FACE_EAST;
		} else if (itemBuildFace == Item.FACE_EAST) {
			itemBuildFace = Item.FACE_SOUTH;
		} else {
			itemBuildFace = Item.FACE_WEST;
		}
	}


	public static void render () {
		int xView = Game.getWorld ().getView ().x;
		int yView = Game.getWorld ().getView ().y;
		int zView = Game.getWorld ().getView ().z;

		int cellXMax = xView + (maxTilesWidthHeight / 2);
		if (bMiniBlocksON) {
			cellXMax++;
		}
		if (cellXMax >= World.MAP_WIDTH) {
			cellXMax = (World.MAP_WIDTH - 1);
		}

		int cellXMin = xView - (maxTilesWidthHeight / 2);
		// depthXMin = (cellXMin < 0) ? 0 : cellXMin;
		cellXMin -= (World.MAP_DEPTH - zView);

		int cellYMax = yView + (maxTilesWidthHeight / 2) + 2;
		cellYMax += (World.MAP_DEPTH - zView);

		int cellYMin = ((-(maxTilesWidthHeight / 2) - 1) + yView) - 2;
		if (cellYMin < 0) {
			cellYMin = 0;
		}
		// depthYMin = cellYMin;

		int iBaseXGeneral = (-xView) * (Tile.TERRAIN_ICON_WIDTH / 2) + ((-yView) * Tile.TERRAIN_ICON_WIDTH / 2) + xCentro;
		int iBaseYGeneral = (-yView) * (Tile.TERRAIN_ICON_HEIGHT / 2) - ((-xView) * Tile.TERRAIN_ICON_HEIGHT / 2) + yCentro;
		Point pointMouse = new Point (Mouse.getX (), renderHeight - Mouse.getY () - 1);
		Point3D pointTileMouse = getTileMouse (pointMouse.x, pointMouse.y, xView, yView, zView);
		boolean bMouseInMainArea = UIPanelInputHandler.isMouseOnAPanel (pointMouse.x, pointMouse.y) == MOUSE_NONE;

		GL11.glEnable (GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc (GL11.GL_LEQUAL);

		if (blinkTurns >= MAX_BLINK_TURNS / 2) {
			bCheckBlinkPiles = TutorialFlow.isBlinkPiles ();
			bCheckBlinkItems = TutorialFlow.isBlinkItems ();
			bCheckBlinkCells = true;
		} else {
			bCheckBlinkPiles = false;
			bCheckBlinkItems = false;
			bCheckBlinkCells = false;
		}

		if (bMouseInMainArea) {
			int iTextureID = renderAllTerrains (zView, cellXMin, cellXMax, cellYMin, cellYMax, iBaseXGeneral, iBaseYGeneral, pointTileMouse, 0, -1);
			iTextureID = renderAllEntities (zView, cellXMin, cellXMax, cellYMin, cellYMax, iBaseXGeneral, iBaseYGeneral, pointTileMouse, 0, iTextureID);
			iTextureID = renderMouse (iBaseXGeneral, iBaseYGeneral, zView, pointTileMouse, iTextureID);
			renderTask (zView, iBaseXGeneral, iBaseYGeneral + Tile.TERRAIN_ICON_HEIGHT, pointTileMouse, iTextureID, cellXMin, cellYMin);
		} else {
			int iTextureID = renderAllTerrains (zView, cellXMin, cellXMax, cellYMin, cellYMax, iBaseXGeneral, iBaseYGeneral, null, 0, -1);
			iTextureID = renderAllEntities (zView, cellXMin, cellXMax, cellYMin, cellYMax, iBaseXGeneral, iBaseYGeneral, null, 0, iTextureID);
			renderTask (zView, iBaseXGeneral, iBaseYGeneral + Tile.TERRAIN_ICON_HEIGHT, null, iTextureID, cellXMin, cellYMin);
		}
		GL11.glDisable (GL11.GL_DEPTH_TEST);

		// Animation de los special tiles
		World.updateSpecialTilesAnimation ();

		Game.getPanelUI ().render ();

		// Información tooltip sólo si no hay un menú contextual abierto
		if (bMouseInMainArea && pointTileMouse != null && (typingPanel == null)) {
			if (Game.getCurrentState () != Game.STATE_SHOWING_CONTEXT_MENU) {
				if (!bHideUION) {
					int iXGeneral = iBaseXGeneral + (pointTileMouse.x + pointTileMouse.y) * (Tile.TERRAIN_ICON_WIDTH / 2);
					int iYGeneral = iBaseYGeneral - (pointTileMouse.x - pointTileMouse.y) * (Tile.TERRAIN_ICON_HEIGHT / 2);

					if (zView != pointTileMouse.z) {
						if (zView > pointTileMouse.z) {
							iYGeneral += ((zView - pointTileMouse.z) * Tile.TERRAIN_ICON_HEIGHT);
						} else {
							iYGeneral += ((pointTileMouse.z - zView) * Tile.TERRAIN_ICON_HEIGHT);
						}
					}
					renderTooltip (iXGeneral, iYGeneral, pointTileMouse);
				}
			}
		}
	}


	private static int renderMouse (int iBaseXGeneral, int iBaseYGeneral, int zView, Point3D pointTileMouse, int currentTextureID) {
		// Mouse & 3D Mouse
		if (pointTileMouse != null && typingPanel == null) {
			if (!(Game.getCurrentState () == Game.STATE_CREATING_TASK && (Game.getCurrentTask ().getState () == Task.STATE_CREATING_ENDZONE || Game.getCurrentTask ().getState () == Task.STATE_CREATING_SINGLEPOINT))) {
				int iYGeneral = iBaseYGeneral - (pointTileMouse.x - pointTileMouse.y) * (Tile.TERRAIN_ICON_HEIGHT / 2);
				int iXGeneral = iBaseXGeneral + (pointTileMouse.x + pointTileMouse.y) * (Tile.TERRAIN_ICON_WIDTH / 2);

				if (zView != pointTileMouse.z) {
					if (zView > pointTileMouse.z) {
						iYGeneral += ((zView - pointTileMouse.z) * Tile.TERRAIN_ICON_HEIGHT);
					} else {
						iYGeneral += ((pointTileMouse.z - zView) * Tile.TERRAIN_ICON_HEIGHT);
					}
				}

				// Mouse
				Cell cell = World.getCell (pointTileMouse);
				Tile tile;
				if (cell.getTerrain ().getTerrainID () == TerrainManagerItem.TERRAIN_AIR_ID && (pointTileMouse.z + 1) < World.MAP_DEPTH) {
					// Miramos la de abajo
					Cell cellUnder = World.getCell (pointTileMouse.x, pointTileMouse.y, pointTileMouse.z + 1);
					if (cellUnder.getTerrain ().getTerrainID () == TerrainManagerItem.TERRAIN_AIR_ID) {
						tile = World.getTileMouseCursorAir ();
					} else {
						boolean bAux = bMiniBlocksON;
						if (pointTileMouse.z != zView) {
							bMiniBlocksON = false;
						}
						tile = World.getTileMouseCursor (cell.isDiscovered () && cell.isMined ());
						if (pointTileMouse.z != zView) {
							bMiniBlocksON = bAux;
						}
					}
				} else {
					boolean bAux = bMiniBlocksON;
					if (pointTileMouse.z != zView) {
						bMiniBlocksON = false;
					}
					tile = World.getTileMouseCursor (cell.isDiscovered () && cell.isMined ());
					if (pointTileMouse.z != zView) {
						bMiniBlocksON = bAux;
					}
				}
				currentTextureID = UtilsGL.setTexture (tile, currentTextureID);
				UtilsGL.drawTextureZ (iXGeneral, iYGeneral, iXGeneral + tile.getTileWidth (), iYGeneral + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), Cell.getDepth (pointTileMouse.x, pointTileMouse.y, pointTileMouse.z));

				// Mouse 2D, arrows pointing down
				if (!tDMouseON && Game.isMouse2DCubesON ()) {
					int iZ = pointTileMouse.z;

					tile = MOUSE_3D_TILE;
					boolean bEnd = false;
					Item item;
					ItemManagerItem imi;
					while (!bEnd) {
						iZ++;
						if (iZ < World.MAP_DEPTH) {
							cell = World.getCell (pointTileMouse.x, pointTileMouse.y, iZ);
							if (!cell.isMined () || cell.getTerrain ().hasFluids ()) {
								bEnd = true;
							} else {
								item = cell.getItem ();
								if (item != null) {
									imi = ItemManager.getItem (item.getIniHeader ());
									if (imi.isBase ()) {
										bEnd = true;
									}
								}

								if (!bEnd) {
									iYGeneral += (tile.getTileHeight () / 2);

									currentTextureID = UtilsGL.setTexture (tile, currentTextureID);
									UtilsGL.drawTextureZ (iXGeneral, iYGeneral, iXGeneral + tile.getTileWidth (), iYGeneral + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), Cell.getDepth (pointTileMouse.x, pointTileMouse.y, iZ));
								}
							}
						} else {
							bEnd = true;
						}
					}
				}

				if (tDMouseON) {
					// Level number in the regular mouse
					int iLevel = World.MAP_NUM_LEVELS_OUTSIDE - pointTileMouse.z;
					String sLevel = Integer.toString (iLevel);
					int sLevelW = UtilFont.getWidth (sLevel) / 2;
					currentTextureID = UtilsGL.setTexture (currentTextureID, Game.TEXTURE_FONT_ID);
					UtilsGL.drawStringZ (sLevel, iXGeneral + tile.getTileWidth () / 2 - sLevelW, iYGeneral + tile.getTileHeight () / 2 + UtilFont.MAX_HEIGHT / 2 - 2, ColorGL.WHITE, Cell.getDepth (pointTileMouse.x, pointTileMouse.y, pointTileMouse.z));
				}
			}
		}

		GL11.glColor3f (1, 1, 1);

		return currentTextureID;
	}


	public static int renderAllTerrains (int zView, int cellXMin, int cellXMax, int cellYMin, int cellYMax, int iBaseXGeneral, int iBaseYGeneral, Point3D pointTileMouse, int zLevelOffset, int currentTextureID) {
		Tile tile;
		int iXGeneral, iYGeneral, iDepth;
		Cell cell;
		forPrincipal: for (int i = cellXMax; i >= cellXMin; i--) {
			for (int j = cellYMin; j <= cellYMax; j++) {
				iYGeneral = iBaseYGeneral - (i - j) * (Tile.TERRAIN_ICON_HEIGHT / 2);
				if (iYGeneral <= -2 * Tile.TERRAIN_ICON_HEIGHT) {
					continue;
				}
				iXGeneral = iBaseXGeneral + (i + j) * (Tile.TERRAIN_ICON_WIDTH / 2);

				if (i >= 0 && j >= 0 && i < World.MAP_WIDTH && j < World.MAP_HEIGHT) {
					cell = World.getCell (i, j, zView);
				} else {
					cell = null;
				}

				if (cell != null && iXGeneral > -Tile.TERRAIN_ICON_WIDTH && iYGeneral < (renderHeight + 4 * Tile.TERRAIN_ICON_HEIGHT)) {
					iDepth = Cell.getDepth (i, j, zView);

					// Zones + stockpiles
					if (cell.hasZone ()) {
						Zone zone = Zone.getZone (cell.getZoneID ());
						if (zone != null) {
							ZoneManagerItem zmi = ZoneManager.getItem (zone.getIniHeader ());
							tile = zmi.getTile ();

							if (zView < (World.MAP_DEPTH - 1)) {
								Cell cellDown = World.getCell (i, j, zView + 1);
								currentTextureID = setColorShadowLightCellTerrain (cellDown, tile, zLevelOffset + 1, currentTextureID, false);
							} else {
								currentTextureID = setColorShadowLightCellTerrain (cell, tile, zLevelOffset + 1, currentTextureID, false);
							}
							UtilsGL.drawTextureZ (iXGeneral, iYGeneral + Tile.TERRAIN_ICON_HEIGHT, iXGeneral + tile.getTileWidth (), iYGeneral + tile.getTileHeight () + Tile.TERRAIN_ICON_HEIGHT, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), iDepth);
						}
					} else if (cell.hasStockPile ()) {
						tile = World.getTileStockpile ();

						boolean blink = false;
						if (bCheckBlinkPiles) {
							Type type = Stockpile.getStockpile (i, j, zView).getType ();
							if (type != null && TutorialFlow.currentBlinkPiles (type.getID ())) {
								blink = true;
							}
						}

						if (zView < (World.MAP_DEPTH - 1)) {
							Cell cellDown = World.getCell (i, j, zView + 1);
							currentTextureID = setColorShadowLightCellTerrain (cellDown, tile, zLevelOffset + 1, currentTextureID, false);
						} else {
							currentTextureID = setColorShadowLightCellTerrain (cell, tile, zLevelOffset + 1, currentTextureID, false);
						}

						if (blink) {
							UtilsGL.setColorRed ();
						}
						UtilsGL.drawTextureZ (iXGeneral, iYGeneral + Tile.TERRAIN_ICON_HEIGHT, iXGeneral + tile.getTileWidth (), iYGeneral + tile.getTileHeight () + Tile.TERRAIN_ICON_HEIGHT, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), iDepth);
						if (blink) {
							UtilsGL.unsetColor ();
						}
					}

					// Terreno
					if (!cell.isDiscovered () || !cell.isMined ()) {
						if (cell.isDiscovered ()) {
							if (bMiniBlocksON && zLevelOffset == 0) {
								tile = TerrainManager.getBlockByID (cell.getTerrain ().getTerrainID ());
							} else {
								tile = TerrainManager.getTileByTileID (cell.getTerrain ().getTerrainTileID ());
							}
						} else {
							if (bMiniBlocksON && zLevelOffset == 0) {
								tile = World.getTileUnknownMini ();
							} else {
								tile = World.getTileUnknown ();
							}
						}

						currentTextureID = setColorShadowLightCellTerrain (cell, tile, zLevelOffset, currentTextureID, false);
						UtilsGL.drawTextureZ (iXGeneral, iYGeneral, iXGeneral + tile.getTileWidth (), iYGeneral + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), iDepth);
					}

					// Blink?
					if (bCheckBlinkCells && cell.isBlink ()) {
						tile = World.getTileMouseCursorBAD (true);
						currentTextureID = UtilsGL.setTexture (tile, currentTextureID);
//						UtilsGL.setColorGreen ();
						GL11.glColor3f (1f, 1f, 1f);
						UtilsGL.drawTextureZ (iXGeneral, iYGeneral, iXGeneral + tile.getTileWidth (), iYGeneral + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), iDepth);
//						UtilsGL.unsetColor ();
					}
				}

				// Under
				if (cell == null || cell.isShouldPaintUnder () || (flatMouseON && zLevelOffset == 0 && pointTileMouse != null && isMouseNearCell (cell, pointTileMouse))) { // El zLevelOffset es para que se vea bien cuando acercas al ratón a un item con _block
					// Hay que pintar lo de abajo
					if (zView < (World.MAP_DEPTH - 1)) {
						currentTextureID = renderAllTerrains (zView + 1, i + 1, i + 1, j - 1, j - 1, iBaseXGeneral, iBaseYGeneral + (Tile.TERRAIN_ICON_WIDTH / 2), null, (zLevelOffset + 1), currentTextureID);
					}
				} else {
					if (bMiniBlocksON && zLevelOffset == 0) {
						if (zView < (World.MAP_DEPTH - 1)) {
							currentTextureID = renderAllTerrains (zView + 1, i + 1, i + 1, j - 1, j - 1, iBaseXGeneral, iBaseYGeneral + (Tile.TERRAIN_ICON_WIDTH / 2), null, (zLevelOffset + 1), currentTextureID);
						}
					}
				}

				if (iXGeneral >= renderWidth) {
					continue forPrincipal;
				}
			}
		}

		return currentTextureID;
	}


	public static float getColorShadowLightCell (Cell cell, int zLevelOffset) {
		float fColor;
		if (zLevelOffset <= 0) {
			// Primer nivel
			if (cell.isLight ()) {
				fColor = 1f;
			} else {
				if (cell.isShadow (true)) {
					fColor = 0.7f;
				} else if (Game.getWorld ().getGlobalEvents ().isHalfShadows ()) {
					fColor = 0.85f;
				} else {
					fColor = 1f;
				}
			}
		} else {
			// Niveles por debajo (más oscuro)
			if (cell.isShadow (true)) {
				if (cell.isLight ()) {
					fColor = 0.80f;
				} else {
					fColor = 0.5f;
				}
			} else if (Game.getWorld ().getGlobalEvents ().isHalfShadows ()) {
				if (cell.isLight ()) {
					fColor = 0.90f;
				} else {
					fColor = 0.65f;
				}
			} else {
				if (cell.isLight ()) {
					fColor = 0.85f;
				} else {
					fColor = 0.75f; // Casilla normal (sin sombra y sin luz)
				}
			}
		}

		return fColor;
	}


	public static int setColorShadowLightCellTerrain (Cell cell, Tile tile, int zLevelOffset, int iCurrentTextureID, boolean bTransparency) {
		if (cell.isLight () && Game.OPENGL_13_AVAILABLE) {
			return setColorShadowLightCellTerrain (cell, 0, tile, bTransparency, iCurrentTextureID, zLevelOffset);
		} else {
			return setColorShadowLightCellTerrain (cell, getColorShadowLightCell (cell, zLevelOffset), tile, bTransparency, iCurrentTextureID, zLevelOffset);
		}
	}


	public static int setColorShadowLightCell (Cell cell, Tile tile, int zLevelOffset, int iCurrentTextureID, boolean bTransparency) {
		if (cell.isLight () && Game.OPENGL_13_AVAILABLE) {
			return setColorShadowLightCell (cell, 0, tile, bTransparency, iCurrentTextureID, zLevelOffset);
		} else {
			return setColorShadowLightCell (cell, getColorShadowLightCell (cell, zLevelOffset), tile, bTransparency, iCurrentTextureID, zLevelOffset);
		}
	}


	public static int setColorShadowLightCellNoLight (Cell cell, Tile tile, int zLevelOffset, int iCurrentTextureID, boolean bTransparency) {
		return setColorShadowLightCellNoLight (cell, getColorShadowLightCell (cell, zLevelOffset), tile, bTransparency, iCurrentTextureID);
	}


	public static int setColorShadowLightCell (Cell cell, float fShadowLightColor, Tile tile, boolean bTransparency, int iCurrentTextureID, int zLevelOffset) {
		if (cell.isLight ()) {
			if (Game.OPENGL_13_AVAILABLE) {
				if (zLevelOffset > 1) {
					UtilsGL.setTextureBrightnessDarker (tile, cell, bTransparency);
				} else {
					UtilsGL.setTextureBrightness (tile, cell, bTransparency);
				}
				return -1;
			} else {
				float fRed;
				if (cell.isLightRedFull ()) {
					fRed = fShadowLightColor + ((zLevelOffset > 1) ? 0.15f : 0.2f);
				} else if (cell.isLightRedHalf ()) {
					fRed = fShadowLightColor + ((zLevelOffset > 1) ? 0.05f : 0.1f);
				} else {
					fRed = fShadowLightColor - ((zLevelOffset > 1) ? 0.15f : 0.1f);
				}
				if (fRed > 1f) {
					fRed = 1f;
				} else if (fRed < 0) {
					fRed = 0;
				}
				float fGreen;
				if (cell.isLightGreenFull ()) {
					fGreen = fShadowLightColor + ((zLevelOffset > 1) ? 0.15f : 0.2f);
				} else if (cell.isLightGreenHalf ()) {
					fGreen = fShadowLightColor + ((zLevelOffset > 1) ? 0.05f : 0.1f);
				} else {
					fGreen = fShadowLightColor - ((zLevelOffset > 1) ? 0.15f : 0.1f);
				}
				if (fGreen > 1f) {
					fGreen = 1f;
				} else if (fGreen < 0) {
					fGreen = 0;
				}
				float fBlue;
				if (cell.isLightBlueFull ()) {
					fBlue = fShadowLightColor + ((zLevelOffset > 1) ? 0.15f : 0.2f);
				} else if (cell.isLightBlueHalf ()) {
					fBlue = fShadowLightColor + ((zLevelOffset > 1) ? 0.05f : 0.1f);
				} else {
					fBlue = fShadowLightColor - ((zLevelOffset > 1) ? 0.15f : 0.1f);
				}
				if (fBlue > 1f) {
					fBlue = 1f;
				} else if (fBlue < 0) {
					fBlue = 0;
				}

				iCurrentTextureID = UtilsGL.setTexture (tile, iCurrentTextureID);
				if (cell.isOpen ()) {
					GlobalEventData ged = Game.getWorld ().getGlobalEvents ();
					if (bTransparency) {
						GL11.glColor4f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue (), TRANSPARENCY);
					} else {
						GL11.glColor3f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue ());
					}
				} else {
					if (bTransparency) {
						GL11.glColor4f (fRed, fGreen, fBlue, TRANSPARENCY);
					} else {
						GL11.glColor3f (fRed, fGreen, fBlue);
					}
				}
				return iCurrentTextureID;
			}
		} else {
			iCurrentTextureID = UtilsGL.setTexture (tile, iCurrentTextureID);

			GlobalEventData ged = Game.getWorld ().getGlobalEvents ();
			if (bTransparency) {
				GL11.glColor4f (fShadowLightColor + ged.getRed (), fShadowLightColor + ged.getGreen (), fShadowLightColor + ged.getBlue (), TRANSPARENCY);
			} else {
				GL11.glColor3f (fShadowLightColor + ged.getRed (), fShadowLightColor + ged.getGreen (), fShadowLightColor + ged.getBlue ());
			}
			return iCurrentTextureID;
		}
	}


	public static int setColorShadowLightCellTerrain (Cell cell, float fShadowLightColor, Tile tile, boolean bTransparency, int iCurrentTextureID, int zLevelOffset) {
		if (cell.isLight ()) {
			if (Game.OPENGL_13_AVAILABLE) {
				if (zLevelOffset == 0) {
					UtilsGL.setTextureBrightnessBright (tile, cell, bTransparency);
				} else if (zLevelOffset > 1) {
					UtilsGL.setTextureBrightnessDarker (tile, cell, bTransparency);
				} else {
					UtilsGL.setTextureBrightness (tile, cell, bTransparency);
				}
				return -1;
			} else {
				float fRed;
				if (cell.isLightRedFull ()) {
					fRed = fShadowLightColor + ((zLevelOffset > 1) ? 0.15f : 0.2f);
				} else if (cell.isLightRedHalf ()) {
					fRed = fShadowLightColor + ((zLevelOffset > 1) ? 0.05f : 0.1f);
				} else {
					fRed = fShadowLightColor - ((zLevelOffset > 1) ? 0.15f : 0.1f);
				}
				if (fRed > 1f) {
					fRed = 1f;
				} else if (fRed < 0) {
					fRed = 0;
				}
				float fGreen;
				if (cell.isLightGreenFull ()) {
					fGreen = fShadowLightColor + ((zLevelOffset > 1) ? 0.15f : 0.2f);
				} else if (cell.isLightGreenHalf ()) {
					fGreen = fShadowLightColor + ((zLevelOffset > 1) ? 0.05f : 0.1f);
				} else {
					fGreen = fShadowLightColor - ((zLevelOffset > 1) ? 0.15f : 0.1f);
				}
				if (fGreen > 1f) {
					fGreen = 1f;
				} else if (fGreen < 0) {
					fGreen = 0;
				}
				float fBlue;
				if (cell.isLightBlueFull ()) {
					fBlue = fShadowLightColor + ((zLevelOffset > 1) ? 0.15f : 0.2f);
				} else if (cell.isLightBlueHalf ()) {
					fBlue = fShadowLightColor + ((zLevelOffset > 1) ? 0.05f : 0.1f);
				} else {
					fBlue = fShadowLightColor - ((zLevelOffset > 1) ? 0.15f : 0.1f);
				}
				if (fBlue > 1f) {
					fBlue = 1f;
				} else if (fBlue < 0) {
					fBlue = 0;
				}

				iCurrentTextureID = UtilsGL.setTexture (tile, iCurrentTextureID);
				GlobalEventData ged = Game.getWorld ().getGlobalEvents ();
				if (bTransparency) {
					GL11.glColor4f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue (), TRANSPARENCY);
				} else {
					GL11.glColor3f (fRed + ged.getRed (), fGreen + ged.getGreen (), fBlue + ged.getBlue ());
				}
				return iCurrentTextureID;
			}
		} else {
			iCurrentTextureID = UtilsGL.setTexture (tile, iCurrentTextureID);
			if (Game.OPENGL_13_AVAILABLE && zLevelOffset == 0) {
				UtilsGL.setTextureBrightnessBright (tile, cell, bTransparency);
				return -1;
			} else {
				if (zLevelOffset <= 1 && !cell.isShadow (true) && !Game.getWorld ().getGlobalEvents ().isHalfShadows ()) {
					fShadowLightColor = 1f;
				}

				GlobalEventData ged = Game.getWorld ().getGlobalEvents ();
				if (bTransparency) {
					GL11.glColor4f (fShadowLightColor + ged.getRed (), fShadowLightColor + ged.getGreen (), fShadowLightColor + ged.getBlue (), TRANSPARENCY);
				} else {
					GL11.glColor3f (fShadowLightColor + ged.getRed (), fShadowLightColor + ged.getGreen (), fShadowLightColor + ged.getBlue ());
				}
			}
			return iCurrentTextureID;
		}
	}


	public static int setColorShadowLightCellNoLight (Cell cell, float fShadowLightColor, Tile tile, boolean bTransparency, int iCurrentTextureID) {
		iCurrentTextureID = UtilsGL.setTexture (tile, iCurrentTextureID);

		GlobalEventData ged = Game.getWorld ().getGlobalEvents ();
		if (bTransparency) {
			GL11.glColor4f (fShadowLightColor + ged.getRed (), fShadowLightColor + ged.getGreen (), fShadowLightColor + ged.getBlue (), TRANSPARENCY);
		} else {
			GL11.glColor3f (fShadowLightColor + ged.getRed (), fShadowLightColor + ged.getGreen (), fShadowLightColor + ged.getBlue ());
		}

		return iCurrentTextureID;
	}


	public static int renderAllEntities (int zView, int cellXMin, int cellXMax, int cellYMin, int cellYMax, int iBaseXGeneral, int iBaseYGeneral, Point3D pointTileMouse, int zLevelOffset, int currentTextureID) {
		int iXGeneral, iYGeneral;
		Cell cell;

		forPrincipal: for (int i = cellXMax; i >= cellXMin; i--) {
			for (int j = cellYMin; j <= cellYMax; j++) {
				iYGeneral = iBaseYGeneral - (i - j) * (Tile.TERRAIN_ICON_HEIGHT / 2);
				if (iYGeneral <= -2 * Tile.TERRAIN_ICON_HEIGHT) {
					continue;
				}
				iXGeneral = iBaseXGeneral + (i + j) * (Tile.TERRAIN_ICON_WIDTH / 2);

				if (i >= 0 && j >= 0 && i < World.MAP_WIDTH && j < World.MAP_HEIGHT) {
					cell = World.getCell (i, j, zView);
				} else {
					cell = null;
				}
				if (cell != null && iXGeneral > -Tile.TERRAIN_ICON_WIDTH && iYGeneral < (renderHeight + 4 * Tile.TERRAIN_ICON_HEIGHT)) {
					float fColorShadowLight = getColorShadowLightCell (cell, zLevelOffset);
					currentTextureID = renderEntities (i, j, zView, cell, iXGeneral, iYGeneral + Tile.TERRAIN_ICON_HEIGHT, currentTextureID, pointTileMouse, fColorShadowLight, zLevelOffset);
				}

				// Under
				// if (cell == null || cell.isShouldPaintUnder ()) {
				// if (zView < (World.MAP_DEPTH - 1)) {
				// currentTextureID = renderAllEntities (zView + 1, i + 1, i + 1, j - 1, j - 1, iBaseXGeneral, iBaseYGeneral + (Tile.TERRAIN_ICON_WIDTH / 2), null, (zLevelOffset + 1), currentTextureID);
				// }
				// }
				if (cell == null || cell.isShouldPaintUnder () || (flatMouseON && zLevelOffset == 0 && pointTileMouse != null && isMouseNearCell (cell, pointTileMouse))) { // El zLevelOffset es para que se vea bien cuando acercas al ratón a un item con _block
					// Hay que pintar lo de abajo
					if (zView < (World.MAP_DEPTH - 1)) {
						currentTextureID = renderAllEntities (zView + 1, i + 1, i + 1, j - 1, j - 1, iBaseXGeneral, iBaseYGeneral + (Tile.TERRAIN_ICON_WIDTH / 2), null, (zLevelOffset + 1), currentTextureID);
					}
				} else {
					if (bMiniBlocksON && zLevelOffset == 0) {
						if (zView < (World.MAP_DEPTH - 1)) {
							currentTextureID = renderAllEntities (zView + 1, i + 1, i + 1, j - 1, j - 1, iBaseXGeneral, iBaseYGeneral + (Tile.TERRAIN_ICON_WIDTH / 2), null, (zLevelOffset + 1), currentTextureID);
						}
					}
				}

				if (iXGeneral >= renderWidth) {
					continue forPrincipal;
				}
			}
		}

		// Grid + orders
		GL11.glColor3f (1, 1, 1);
		forPrincipalGrid: for (int i = cellXMax; i >= cellXMin; i--) {
			for (int j = cellYMin; j <= cellYMax; j++) {
				iYGeneral = iBaseYGeneral - (i - j) * (Tile.TERRAIN_ICON_HEIGHT / 2);
				if (iYGeneral <= -2 * Tile.TERRAIN_ICON_HEIGHT) {
					continue;
				}
				iXGeneral = iBaseXGeneral + (i + j) * (Tile.TERRAIN_ICON_WIDTH / 2);

				if (i >= 0 && j >= 0 && i < World.MAP_WIDTH && j < World.MAP_HEIGHT && iXGeneral > -Tile.TERRAIN_ICON_WIDTH && iYGeneral < (renderHeight + Tile.TERRAIN_ICON_HEIGHT)) {
					cell = World.getCells ()[i][j][zView];

					// Si la celda tiene órdenes pintamos su tile
					if (cell.isFlagOrders ()) {
						Tile tile;
						if (zLevelOffset == 0 && bMiniBlocksON) {
							if (cell.isDiscovered () && cell.isMined ()) {
								tile = World.getTileOrders (true);
							} else {
								tile = World.getTileOrdersMiniBlock ();
							}
						} else {
							tile = World.getTileOrders (cell.isDiscovered () && cell.isMined ());
						}

						currentTextureID = UtilsGL.setTexture (tile, currentTextureID);
						UtilsGL.drawTextureZ (iXGeneral, iYGeneral, iXGeneral + tile.getTileWidth (), iYGeneral + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), Cell.getDepth (i, j, zView));
					}

					// Grid
					if (gridON && zLevelOffset == 0 && cell.isDiscovered () && cell.isMined ()) {
						if (!cell.isDigged ()) {
							currentTextureID = UtilsGL.setTexture (GRID_TILE, currentTextureID);
							UtilsGL.drawTextureZ (iXGeneral, iYGeneral + Tile.TERRAIN_ICON_HEIGHT, iXGeneral + GRID_TILE.getTileWidth (), iYGeneral + GRID_TILE.getTileHeight () + Tile.TERRAIN_ICON_HEIGHT, GRID_TILE.getTileSetTexX0 (), GRID_TILE.getTileSetTexY0 (), GRID_TILE.getTileSetTexX1 (), GRID_TILE.getTileSetTexY1 (), Cell.getDepth (i, j, zView));
						} else {
							// Celda digada, pero miramos si hay algo debajo (simplemente mirando el ASZID bastará)
							if (cell.getAstarZoneID () != -1) {
								currentTextureID = UtilsGL.setTexture (GRID_TILE, currentTextureID);
								UtilsGL.drawTextureZ (iXGeneral, iYGeneral + Tile.TERRAIN_ICON_HEIGHT, iXGeneral + GRID_TILE.getTileWidth (), iYGeneral + GRID_TILE.getTileHeight () + Tile.TERRAIN_ICON_HEIGHT, GRID_TILE.getTileSetTexX0 (), GRID_TILE.getTileSetTexY0 (), GRID_TILE.getTileSetTexX1 (), GRID_TILE.getTileSetTexY1 (), Cell.getDepth (i, j, zView));
							} else {
								currentTextureID = UtilsGL.setTexture (GRID_NOT_ALLOWED_TILE, currentTextureID);
								UtilsGL.drawTextureZ (iXGeneral, iYGeneral, iXGeneral + GRID_NOT_ALLOWED_TILE.getTileWidth (), iYGeneral + GRID_NOT_ALLOWED_TILE.getTileHeight (), GRID_NOT_ALLOWED_TILE.getTileSetTexX0 (), GRID_NOT_ALLOWED_TILE.getTileSetTexY0 (), GRID_NOT_ALLOWED_TILE.getTileSetTexX1 (), GRID_NOT_ALLOWED_TILE.getTileSetTexY1 (), Cell.getDepth (i, j, zView));
							}
						}
					}
				}

				if (iXGeneral >= renderWidth) {
					continue forPrincipalGrid;
				}
			}
		}

		GL11.glColor3f (1, 1, 1);
		return currentTextureID;
	}


	/**
	 * Devuelve la Z Máxima donde se deberá dibujar el 3D mouse
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static int getMaxZ3DMouse (int x, int y, int z) {
		int iZ = z;
		Cell cell = World.getCell (x, y, iZ);
		ItemManagerItem imi;
		Item item = cell.getItem ();
		if (item != null) {
			imi = ItemManager.getItem (item.getIniHeader ());
		} else {
			imi = null;
		}
		while (iZ < World.MAP_DEPTH && cell.isMined () && !cell.getTerrain ().hasFluids () && (imi == null || !imi.isBase ())) {
			iZ++;
			if (iZ < World.MAP_DEPTH) {
				cell = World.getCell (x, y, iZ);
				item = cell.getItem ();
				if (item != null) {
					imi = ItemManager.getItem (item.getIniHeader ());
				} else {
					imi = null;
				}
			}
		}
		iZ--;
		if (iZ < z) {
			return z;
		}

		return iZ;
	}


	public static void renderTask (int zView, int iBaseXGeneral, int iBaseYGeneral, Point3D pointTileMouse, int currentTextureID, int cellXMin, int cellYMin) {
		// Tasks
		if (pointTileMouse != null) {
			if (Game.getCurrentState () == Game.STATE_CREATING_TASK && (Game.getCurrentTask ().getState () == Task.STATE_CREATING_ENDZONE || Game.getCurrentTask ().getState () == Task.STATE_CREATING_SINGLEPOINT)) {
				int iXGeneral, iYGeneral;
				int z3D;
				if (tDMouseON && Game.getCurrentState () == Game.STATE_CREATING_TASK && Game.getCurrentTask ().getState () == Task.STATE_CREATING_ENDZONE) {
					z3D = Game.getCurrentTask ().getPointIni ().z;
				} else {
					z3D = (tDMouseON) ? pointTileMouse.z : zView;
				}
				Cell cell;
				GL11.glColor4f (1, 1, 1, 1);

				// Miramos donde está el ratón para mostrar info por pantalla y el cuadradito blanco
				boolean mouseOK = true;
				boolean bBuilding = false;
				boolean bIteming = false;
				boolean stockpiling = false;
				boolean zoning = false;
				boolean mining = false;
				boolean digging = false;
				Zone expandZone = null;
				boolean queuing = false;
				boolean rowing = false;
				boolean rowingHorizontal = false;
				ZoneManagerItem zmi = null;
				// Marcado de zona, pintar cuadraditos blancos
				// Creando tarea de END_ZONE o de SINGLE_POINT (building)
				int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
				if (Game.getCurrentTask ().getState () == Task.STATE_CREATING_ENDZONE) {
					if (Game.getCurrentTask ().getTask () == Task.TASK_STOCKPILE) {
						stockpiling = true;
					} else if (Game.getCurrentTask ().getTask () == Task.TASK_CREATE_ZONE) {
						zoning = true;
						zmi = ZoneManager.getItem (Game.getCurrentTask ().getParameter ());
					} else if (Game.getCurrentTask ().getTask () == Task.TASK_EXPAND_ZONE) {
						zoning = true;
						expandZone = Zone.getZone (Integer.parseInt (Game.getCurrentTask ().getParameter ()));
						zmi = ZoneManager.getItem (expandZone.getIniHeader ()); // Usamos el expandzone solo por "performance"
					} else if (Game.getCurrentTask ().getTask () == Task.TASK_CREATE_AND_PLACE_ROW || Game.getCurrentTask ().getTask () == Task.TASK_QUEUE_AND_PLACE_ROW) {
						rowing = true;
						bIteming = true;
						if (Game.getCurrentTask ().getTask () == Task.TASK_QUEUE_AND_PLACE_ROW) {
							queuing = true;
						}
					} else if (Game.getCurrentTask ().getTask () == Task.TASK_QUEUE_AND_PLACE_AREA) {
						bIteming = true;
						queuing = true;
					} else if (Game.getCurrentTask ().getTask () == Task.TASK_MINE || Game.getCurrentTask ().getTask () == Task.TASK_MINE_LADDER) {
						mining = true;
					} else if (Game.getCurrentTask ().getTask () == Task.TASK_DIG) {
						digging = true;
					}
					x1 = Game.getCurrentTask ().getPointIni ().x;
					y1 = Game.getCurrentTask ().getPointIni ().y;
					x2 = pointTileMouse.x;
					y2 = pointTileMouse.y;
				} else if (Game.getCurrentTask ().getState () == Task.STATE_CREATING_SINGLEPOINT) {
					// Single-point
					if (Game.getCurrentTask ().getTask () == Task.TASK_BUILD || Game.getCurrentTask ().getTask () == Task.TASK_CREATE_AND_PLACE || Game.getCurrentTask ().getTask () == Task.TASK_QUEUE_AND_PLACE) {
						// Building
						if (Game.getCurrentTask ().getTask () == Task.TASK_BUILD) {
							bBuilding = true;
						} else {
							bIteming = true;
							if (Game.getCurrentTask ().getTask () == Task.TASK_QUEUE_AND_PLACE) {
								queuing = true;
							}
						}
						x1 = pointTileMouse.x;
						y1 = pointTileMouse.y;
						if (Game.getCurrentTask ().getTask () == Task.TASK_BUILD) {
							BuildingManagerItem item = BuildingManager.getItem (Game.getCurrentTask ().getParameter ());
							if (item != null) {
								x2 = x1 + item.getWidth () - 1;
								y2 = y1 + item.getHeight () - 1;
							} else {
								x2 = x1;
								y2 = y1;
							}
						} else {
							x2 = x1;
							y2 = y1;
						}

						if (x2 >= World.MAP_WIDTH) {
							x2 = World.MAP_WIDTH - 1;
							mouseOK = false;
						}
						if (y2 >= World.MAP_HEIGHT) {
							y2 = World.MAP_HEIGHT - 1;
							mouseOK = false;
						}
					} else {
						// Tarea desconocida
						x1 = x2 = pointTileMouse.x;
						y1 = y2 = pointTileMouse.y;

						Log.log (Log.LEVEL_ERROR, Messages.getString ("MainPanel.3") + Game.getCurrentTask ().getTask () + "]", "MainPanel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}

				int iAux;
				if (x1 > x2) {
					iAux = x1;
					x1 = x2;
					x2 = iAux;
				}
				if (y1 > y2) {
					iAux = y1;
					y1 = y2;
					y2 = iAux;
				}

				// Rowing?
				if (rowing) {
					rowingHorizontal = x2 - x1 >= y2 - y1;
				}

				BuildingManagerItem bmi = null;
				ItemManagerItem imi = null;
				boolean bBuildingOK = false;
				if (bBuilding) {
					bmi = BuildingManager.getItem (Game.getCurrentTask ().getParameter ());
					bBuildingOK = true;
				} else if (bIteming) {
					if (queuing) {
						// En caso de colas el item está en el parámetro 2
						imi = ItemManager.getItem (Game.getCurrentTask ().getParameter2 ());
					} else {
						imi = ItemManager.getItem (Game.getCurrentTask ().getParameter ());
					}
					if (imi == null) {
						// No ha encontrado el item??? Quizá es un createItemByType y el valor es un type, así que pillamos el primer item con ese type
						if (queuing) {
							// En caso de colas el item está en el parámetro 2
							imi = ItemManager.getFirstItemByType (Game.getCurrentTask ().getParameter2 ());
						} else {
							imi = ItemManager.getFirstItemByType (Game.getCurrentTask ().getParameter ());
						}
					}
				}
				boolean bZoneOK = false;
				if (zoning) {
					// bZoneOK = Zone.areCellsAvailableForZone (zmi, x1, y1, x2, y2, zView, expandZone);
					bZoneOK = Zone.areCellsAvailableForZone (zmi, x1, y1, x2, y2, z3D, expandZone);
				}

				boolean bDraw;
				for (int i = x1; i <= x2; i++) {
					for (int j = y1; j <= y2; j++) {
						bDraw = true;
						if (mouseOK) {
							if (rowing) {
								if (rowingHorizontal) {
									if (j != Game.getCurrentTask ().getPointIni ().y) {
										continue;
									}
								} else {
									if (i != Game.getCurrentTask ().getPointIni ().x) {
										continue;
									}
								}
							}
							// cell = World.getCells ()[i][j][zView];
							cell = World.getCells ()[i][j][z3D];
							Tile terrainSpecialTile = null;
							boolean bUseMouseBad = false;
							if (bBuilding) {
								if (bBuildingOK) {
									// Miramos si la casilla forma parte del edificio para dibujarla o no
									// Ya que ahora los edificios no tienen porque ser rectangulares
									char groundDataChar = bmi.getGroundData ().charAt ((j - y1) * bmi.getWidth () + (i - x1));
									if (groundDataChar == Building.GROUND_NON_BUILDING) {
										bDraw = false;
									} else {
										if (!Building.isCellAvailableForBuilding (bmi, i, j, z3D)) {
											bUseMouseBad = true;
										}
									}
								} else {
									bUseMouseBad = true;
								}
							} else if (bIteming) {
								if (!Item.isCellAvailableForItem (imi, i, j, z3D, true, true)) {
									bUseMouseBad = true;
								} else {
									// El item puede ir ahí, pero miraremos que no esté construyendo en el aire y no haya camino posible
									if (imi.canBeBuiltOnHoles ()) {
										// Miramos si hay algún ASZID distinto de -1 en un radio de 3x3x3
										boolean bAllUnavailable = true;
										foriteming: for (int itemX = (i - 1); itemX <= (i + 1); itemX++) {
											for (int itemY = (j - 1); itemY <= (j + 1); itemY++) {
												for (int itemZ = (z3D - 1); itemZ <= (z3D + 1); itemZ++) {
													if (Utils.isInsideMap (itemX, itemY, itemZ)) {
														if (World.getCell (itemX, itemY, itemZ).getAstarZoneID () != -1) {
															bAllUnavailable = false;
															break foriteming;
														}
													}
												}
											}
										}
										if (bAllUnavailable) {
											bUseMouseBad = true;
										}
									}
								}
							} else if (stockpiling) {
								if (!Stockpile.isCellAvailableForStockpile (i, j, z3D)) {
									bUseMouseBad = true;
								}
							} else if (zoning) {
								if (!bZoneOK) {
									bUseMouseBad = true;
								}
							} else if (mining) {
								if (cell.isDiscovered () && cell.isMined ()) {
									bUseMouseBad = true;
								}
							} else if (digging) {
								if (!cell.isDiscovered () && !cell.isDiggable ()) {
									bUseMouseBad = true;
								}
							} else {
								// En otro caso
								if (!cell.isDiscovered ()) {
									bUseMouseBad = true;
								}
							}

							if (bDraw) {
								if (cell.isDiscovered ()) {
									if (bUseMouseBad) {
										if (bMiniBlocksON && z3D == zView && !cell.isMined ()) {
											terrainSpecialTile = World.getTileMouseCursorBADMiniBlock ();
										} else {
											terrainSpecialTile = World.getTileMouseCursorBAD (cell.isMined ());
										}
									} else {
										if (bMiniBlocksON && z3D == zView && !cell.isMined ()) {
											terrainSpecialTile = World.getTileMouseCursorMiniBlock ();
										} else {
											terrainSpecialTile = World.getTileMouseCursor (cell.isMined ());
										}
									}
								} else {
									if (bUseMouseBad) {
										terrainSpecialTile = World.getTileMouseCursorBAD (false);
									} else {
										terrainSpecialTile = World.getTileMouseCursor (false);
									}
								}

								iXGeneral = iBaseXGeneral + (i + j) * (Tile.TERRAIN_ICON_WIDTH / 2) + terrainSpecialTile.getTileWidthOffset ();
								iYGeneral = iBaseYGeneral - (i - j) * (Tile.TERRAIN_ICON_HEIGHT / 2) - terrainSpecialTile.getTileHeight () - terrainSpecialTile.getTileHeightOffset () + Tile.TERRAIN_ICON_HEIGHT;

								currentTextureID = UtilsGL.setTexture (terrainSpecialTile, currentTextureID);
								UtilsGL.drawTextureZ (iXGeneral, iYGeneral + (z3D - zView) * Tile.TERRAIN_ICON_HEIGHT, iXGeneral + terrainSpecialTile.getTileWidth (), iYGeneral + terrainSpecialTile.getTileHeight () + (z3D - zView) * Tile.TERRAIN_ICON_HEIGHT, terrainSpecialTile.getTileSetTexX0 (), terrainSpecialTile.getTileSetTexY0 (), terrainSpecialTile.getTileSetTexX1 (), terrainSpecialTile.getTileSetTexY1 (), Cell.getDepth (i, j, z3D));

								// Arrows encima del mouse (en caso de iteming y item que se pueda rotar
								if (bIteming && !bUseMouseBad && imi != null && imi.isCanBeRotated ()) {
									if (itemBuildFace == Item.FACE_WEST) {
										terrainSpecialTile = MOUSE_ARROW_WEST;
									} else if (itemBuildFace == Item.FACE_NORTH) {
										terrainSpecialTile = MOUSE_ARROW_NORTH;
									} else if (itemBuildFace == Item.FACE_EAST) {
										terrainSpecialTile = MOUSE_ARROW_EAST;
									} else {
										terrainSpecialTile = MOUSE_ARROW_SOUTH;
									}

									iXGeneral = iBaseXGeneral + (i + j) * (Tile.TERRAIN_ICON_WIDTH / 2) + terrainSpecialTile.getTileWidthOffset ();
									iYGeneral = iBaseYGeneral - (i - j) * (Tile.TERRAIN_ICON_HEIGHT / 2) - terrainSpecialTile.getTileHeight () - terrainSpecialTile.getTileHeightOffset () + Tile.TERRAIN_ICON_HEIGHT;

									currentTextureID = UtilsGL.setTexture (terrainSpecialTile, currentTextureID);
									UtilsGL.drawTextureZ (iXGeneral, iYGeneral + (z3D - zView) * Tile.TERRAIN_ICON_HEIGHT, iXGeneral + terrainSpecialTile.getTileWidth (), iYGeneral + terrainSpecialTile.getTileHeight () + (z3D - zView) * Tile.TERRAIN_ICON_HEIGHT, terrainSpecialTile.getTileSetTexX0 (), terrainSpecialTile.getTileSetTexY0 (), terrainSpecialTile.getTileSetTexX1 (), terrainSpecialTile.getTileSetTexY1 (), Cell.getDepth (i, j, z3D));
								}
							}
						}
					}
				}
			}
		}

		UtilsGL.glEnd ();
	}


	private static int renderEntities (int i, int j, int zView, Cell cell, int iXGeneral, int iYGeneral, int currentTextureID, Point3D pointTileMouse, float fColorShadowLight, int zLevelOffset) {
		// Entities
		Tile tile;
		int iXSpecific, iYSpecific;

		// Discovered
		if (cell.isDiscovered ()) {
			if (cell.isMined ()) {
				int iDepth = Cell.getDepth (i, j, zView);
				boolean bLight = cell.isLight ();

				// Patrol points
				if (cell.isFlagPatrol ()) {
					tile = World.getTilePatrolMark ();
					// currentTextureID = UtilsGL.setTexture (tile, currentTextureID);
					currentTextureID = setColorShadowLightCell (cell, tile, zLevelOffset, currentTextureID, false);
					UtilsGL.drawTextureZ (iXGeneral, iYGeneral, iXGeneral + tile.getTileWidth (), iYGeneral + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), iDepth);
				}

				// Miramos si hay proyectiles
				if (Projectile.getLocations ()[i][j][zView] > 0) {
					// Hay proyectil, lo buscamos y lo pintamos
					Projectile projectile;
					for (int c = 0; c < Game.getWorld ().getProjectiles ().size (); c++) {
						projectile = Game.getWorld ().getProjectiles ().get (c);
						if (projectile.getCoordinates ().x == (i) && projectile.getCoordinates ().y == (j) && projectile.getCoordinates ().z == zView) {
							iYSpecific = iYGeneral - (projectile.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT);

							// Dirección
							float texX0 = projectile.getTileSetTexX0 ();
							float texX1 = projectile.getTileSetTexX1 ();
							float texY0 = projectile.getTileSetTexY0 ();
							float texY1 = projectile.getTileSetTexY1 ();
							if ((projectile.getDirection () & Projectile.DIRECTION_WEST) > 0) {
								// Oeste
								float fAux = texX0;
								texX0 = texX1;
								texX1 = fAux;
							}
							if ((projectile.getDirection () & Projectile.DIRECTION_SOUTH) > 0) {
								// Sur
								float fAux = texY0;
								texY0 = texY1;
								texY1 = fAux;
							}
							// currentTextureID = UtilsGL.setTexture (projectile, currentTextureID);
							currentTextureID = setColorShadowLightCell (cell, projectile, zLevelOffset, currentTextureID, false);
							UtilsGL.drawTextureZ (iXGeneral, iYSpecific, iXGeneral + projectile.getTileWidth (), iYSpecific + projectile.getTileHeight (), texX0, texY0, texX1, texY1, iDepth);
						}
					}
				}

				// Items
				Item item = cell.getItem ();
				currentTextureID = renderItems (cell, item, iXGeneral, iYGeneral, currentTextureID, pointTileMouse, fColorShadowLight, iDepth, zLevelOffset);

				// Buildings
				if (cell.hasBuilding ()) {
					Building building = Building.getBuilding (cell.getBuildingCoordinates ());
					if (building != null) {
						BuildingManagerItem bmi = BuildingManager.getItem (building.getIniHeader ());
						if ((building.getCoordinates ().x) == cell.getCoordinates ().x && (building.getCoordinates ().y + bmi.getHeight () - 1) == cell.getCoordinates ().y) {
							building.updateAnimation ();
							iXSpecific = iXGeneral - ((bmi.getHeight () - 1) * (Tile.TERRAIN_ICON_WIDTH / 2));
							iYSpecific = iYGeneral - building.getTileHeight () + building.getTileHeightOffset () + ((bmi.getHeight () - 1) * Tile.TERRAIN_ICON_HEIGHT / 2);
							// currentTextureID = UtilsGL.setTexture (building, currentTextureID);
							boolean bTransparency = (flatMouseON && pointTileMouse != null && isMouseNearCell (cell, pointTileMouse, building));
							currentTextureID = setColorShadowLightCell (cell, building, zLevelOffset, currentTextureID, bTransparency);
							UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + building.getTileWidth (), iYSpecific + building.getTileHeight (), building.getTileSetTexX0 (), building.getTileSetTexY0 (), building.getTileSetTexX1 (), building.getTileSetTexY1 (), iDepth);

							// Edificio NO construido (NO operativo) o item no construido (no operativo) (cruz roja)
							if (!building.isOperative ()) {
								// currentTextureID = UtilsGL.setTexture (World.getTileRedCross (), currentTextureID);
								currentTextureID = setColorShadowLightCell (cell, World.getTileRedCross (), zLevelOffset, currentTextureID, bTransparency);
								UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + building.getTileWidth (), iYSpecific + building.getTileHeight (), World.getTileRedCross ().getTileSetTexX0 (), World.getTileRedCross ().getTileSetTexY0 (), World.getTileRedCross ().getTileSetTexX1 (), World.getTileRedCross ().getTileSetTexY1 (), iDepth);
							}
						}
					}
				}

				// Fluids
				if (cell.getTerrain ().hasFluids ()) {
					iYSpecific = iYGeneral - Tile.TERRAIN_ICON_HEIGHT;

					if (cell.getTerrain ().getFluidType () == Terrain.FLUIDS_WATER) {
						if (item != null && ItemManager.getItem (item.getIniHeader ()).isAllowFluids ()) {
							tile = World.getTileWater (1);
						} else {
							tile = World.getTileWater (cell.getTerrain ().getFluidCount ());
						}
					} else { // if (cell.getTerrain ().getFluidType () == Terrain.FLUIDS_LAVA) {
						if (item != null && ItemManager.getItem (item.getIniHeader ()).isAllowFluids ()) {
							tile = World.getTileLava (1);
						} else {
							tile = World.getTileLava (cell.getTerrain ().getFluidCount ());
						}
					}
					// currentTextureID = UtilsGL.setTexture (tile, currentTextureID);
					currentTextureID = setColorShadowLightCell (cell, tile, zLevelOffset, currentTextureID, false);
					UtilsGL.drawTextureZ (iXGeneral, iYSpecific, iXGeneral + tile.getTileWidth (), iYSpecific + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), iDepth);
					// Debug, fuerza de los fluidos
					// if (Game.DEBUG_MODE) {
					// UtilsGL.glEnd ();
					// GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
					// GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
					// UtilsGL.glBegin (GL11.GL_QUADS);
					// currentTextureID = Game.TEXTURE_FONT_ID;
					//
					// UtilsGL.drawStringZ (Integer.toString (cell.getTerrain ().getFluidCount ()), iXGeneral + Tile.TERRAIN_ICON_WIDTH / 2 - UtilFont.MAX_WIDTH / 2, iYSpecific + UtilFont.MAX_HEIGHT / 2, iDepth + 1);
					// }
				}

				// Livings
				currentTextureID = renderLivings (cell, iXGeneral, iYGeneral, currentTextureID, fColorShadowLight, bLight, zLevelOffset);
			}
		}

		return currentTextureID;
	}


	private static boolean isMouseNearCell (Cell cell, Point3D pointTileMouse) {
		return (Math.abs (cell.getCoordinates ().x - pointTileMouse.x) <= 3 && Math.abs (cell.getCoordinates ().y - pointTileMouse.y) <= 3);
	}


	private static boolean isMouseNearCell (Cell cell, Point3D pointTileMouse, Tile tile) {
		return (Math.abs (cell.getCoordinates ().x - pointTileMouse.x) <= 3 && Math.abs (cell.getCoordinates ().y - pointTileMouse.y) <= 3 && tile.getTileHeight () > Tile.TERRAIN_ICON_HEIGHT);
	}


	private static int renderItems (Cell cell, Item item, int iXGeneral, int iYGeneral, int currentTextureID, Point3D pointTileMouse, float fColorShadowLight, int iDepth, int zLevelOffset) {
		if (item != null) {
			Tile tile = (Tile) item;
			boolean bFlatNearMouse = false;

			tile.updateAnimation (item.isFacingNorth () || item.isFacingEast ());

			if (pointTileMouse != null && flatMouseON) {
				if (isMouseNearCell (cell, pointTileMouse, tile)) {
					bFlatNearMouse = true;
				}
			}

			if (bFlatNearMouse || (bMiniBlocksON && zLevelOffset == 0)) {
				Tile tileAux = ItemManager.getMiniItem (tile.getIniHeader ());
				if (tileAux != null) {
					tile = tileAux;
				}
			}
			int iXSpecific, iYSpecific;
			if (tile.getTileWidth () != Tile.TERRAIN_ICON_WIDTH) {
				iXSpecific = iXGeneral - (tile.getTileWidth () / 2) + (Tile.TERRAIN_ICON_WIDTH / 2);
			} else {
				iXSpecific = iXGeneral;
			}
			iYSpecific = iYGeneral - (tile.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT);

			int addDepth = (tile.getTileHeight () > 2 * Tile.TERRAIN_ICON_HEIGHT) ? (tile.getTileHeight () - (2 * Tile.TERRAIN_ICON_HEIGHT)) / Tile.TERRAIN_ICON_HEIGHT : 0;

			currentTextureID = setColorShadowLightCell (cell, tile, zLevelOffset, currentTextureID, false);

			// Blink? tutorial?
			boolean blink = false;
			if (bCheckBlinkItems) {
				blink = TutorialFlow.currentBlinkItem (item.getIniHeader ());

				if (blink) {
					UtilsGL.setColorRed ();
				}
			}

			// Rotate
			if (item.isFacingWest () || item.isFacingEast ()) {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + tile.getTileWidth (), iYSpecific + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), iDepth + addDepth);
			} else {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + tile.getTileWidth (), iYSpecific + tile.getTileHeight (), tile.getTileSetTexX1 (), tile.getTileSetTexY0 (), tile.getTileSetTexX0 (), tile.getTileSetTexY1 (), iDepth + addDepth);
			}

			if (blink) {
				UtilsGL.unsetColor ();
			}

			// Locked doors
			if (item.isDoorStatus (Item.FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED)) {
				int iYSpecificLocked = iYSpecific + (tile.getTileHeight () / 2) - (lockedConnectorTile.getTileHeight () / 2);

				currentTextureID = setColorShadowLightCell (cell, lockedConnectorTile, zLevelOffset, currentTextureID, bFlatNearMouse);
				UtilsGL.drawTextureZ (iXSpecific, iYSpecificLocked, iXSpecific + lockedConnectorTile.getTileWidth (), iYSpecificLocked + lockedConnectorTile.getTileHeight (), lockedConnectorTile.getTileSetTexX0 (), lockedConnectorTile.getTileSetTexY0 (), lockedConnectorTile.getTileSetTexX1 (), lockedConnectorTile.getTileSetTexY1 (), iDepth + addDepth);
			}

			// (NO operativo) o item no construido (no operativo) (cruz roja)
			if (!item.isOperative ()) {
				currentTextureID = setColorShadowLightCell (cell, World.getTileRedCross (), zLevelOffset, currentTextureID, bFlatNearMouse);
				UtilsGL.drawTextureZ (iXGeneral, iYSpecific, iXGeneral + tile.getTileWidth (), iYSpecific + tile.getTileHeight (), World.getTileRedCross ().getTileSetTexX0 (), World.getTileRedCross ().getTileSetTexY0 (), World.getTileRedCross ().getTileSetTexX1 (), World.getTileRedCross ().getTileSetTexY1 (), iDepth + addDepth);
			}
		}

		return currentTextureID;
	}


	private static int renderLivings (Cell cell, int iXGeneral, int iYGeneral, int currentTextureID, float fColorShadowLight, boolean bLight, int zLevelOffset) {
		// Miramos si aquí hay friendlies
		int iXSpecific = -1, iYSpecific = -1;
		int iFacingDirection;
		ArrayList<LivingEntity> alLivings = cell.getLivings ();

		if (alLivings == null) {
			return currentTextureID;
		}

		LivingEntity le;
		Citizen cit;
		LivingEntityManagerItem lemi;
		int iDepth;
		for (int liv = 0; liv < alLivings.size (); liv++) {
			le = alLivings.get (liv);

			if (le.getPath ().size () == 0) {
				iDepth = Cell.getDepth (cell.getCoordinates ().x, cell.getCoordinates ().y, cell.getCoordinates ().z);
			} else {
				Point3DShort p3d = le.getPath ().get (0);
				if (p3d.x > cell.getCoordinates ().x && p3d.y > cell.getCoordinates ().y) {
					iDepth = Math.max (Cell.getDepth (cell.getCoordinates ().x, cell.getCoordinates ().y, cell.getCoordinates ().z), Cell.getDepth (p3d.x - 1, p3d.y, p3d.z));
				} else if (p3d.x < cell.getCoordinates ().x && p3d.y < cell.getCoordinates ().y) {
					iDepth = Math.max (Cell.getDepth (cell.getCoordinates ().x, cell.getCoordinates ().y, cell.getCoordinates ().z), Cell.getDepth (p3d.x, p3d.y + 1, p3d.z));
				} else {
					iDepth = Math.max (Cell.getDepth (cell.getCoordinates ().x, cell.getCoordinates ().y, cell.getCoordinates ().z), Cell.getDepth (p3d.x, p3d.y, p3d.z));
				}
			}

			// Si la celda tiene un item grande, miraremos que el iDepth no tenga que ser mayor
			Item item = (le.getPath ().size () == 0) ? cell.getItem () : World.getCell (le.getPath ().get (0)).getItem ();
			if (item != null) {
				int addDepth = (item.getTileHeight () > 2 * Tile.TERRAIN_ICON_HEIGHT) ? (item.getTileHeight () - (2 * Tile.TERRAIN_ICON_HEIGHT)) / Tile.TERRAIN_ICON_HEIGHT : 0;
				if (addDepth > 0) {
					iDepth = Math.max (iDepth, Cell.getDepth (cell.getCoordinates ().x, cell.getCoordinates ().y, cell.getCoordinates ().z) + addDepth);
					;
				} else {
					// Item road, sumamos 1
					if (ItemManager.getItem (item.getIniHeader ()).getFloorWalkSpeed () != 100) {
						iDepth++;
					}
				}
			}

			// Skills / Effects icon
			// Haremos 8 turnos mostrando, 8 sin mostrar, 8 mostrando ..... alternando entre efectos
			int iNumEffects = le.getLivingEntityData ().getEffects ().size ();
			if (iNumEffects > 0) {
				int iCurrentCounter = le.getSkillAnimationCounter ();
				iCurrentCounter++;
				if (iCurrentCounter >= (iNumEffects * 2 * 8)) { // El x2 es para el mostrar / no mostrar
					le.setSkillAnimationCounter (0);
				} else {
					le.setSkillAnimationCounter (iCurrentCounter);
				}

				// Usamos la misma variable para ver que skill mirar
				int iIndexSkill = iCurrentCounter / 16;
				if (iIndexSkill < iNumEffects && (iCurrentCounter % 16 < 8)) {
					EffectData effectData = le.getLivingEntityData ().getEffects ().get (iIndexSkill);
					Tile tile = EffectManager.getItem (effectData.getEffectID ()).getIcon ();
					if (tile != null) {
						iXSpecific = iXGeneral + (int) le.getPositionOffset ().x;
						iYSpecific = iYGeneral - (tile.getTileHeight () + tile.getTileHeight ()) + tile.getTileHeightOffset () + (int) le.getPositionOffset ().y;

						currentTextureID = setColorShadowLightCellNoLight (cell, tile, zLevelOffset, currentTextureID, false);
						UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + tile.getTileWidth (), iYSpecific + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 (), iDepth);
					}
				}
			}

			lemi = LivingEntityManager.getItem (le.getIniHeader ());
			if (lemi.getType () == LivingEntity.TYPE_CITIZEN) {
				// RENDER CITIZENS
				cit = (Citizen) le;

				// Comprobamos que no tenga un effect de graphicchange
				boolean bGraphiChanged = false;
				for (int e = 0; e < cit.getLivingEntityData ().getEffects ().size (); e++) {
					if (cit.getLivingEntityData ().getEffects ().get (e).isGraphicChange ()) {
						bGraphiChanged = true;
						break;
					}
				}

				if (lemi.isFacingDirections ()) {
					iFacingDirection = le.getFacingDirection ();

					if (iFacingDirection == LivingEntity.FACING_DIRECTION_NORTH || iFacingDirection == LivingEntity.FACING_DIRECTION_NORTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_EAST) {
						if (!bGraphiChanged) {
							currentTextureID = renderCarrying (cell, cit, iXGeneral, iYGeneral, currentTextureID, fColorShadowLight, bLight, iDepth, zLevelOffset);
						}
						currentTextureID = renderCitizen (cell, cit, iXGeneral, iYGeneral, currentTextureID, bGraphiChanged, fColorShadowLight, bLight, iDepth, zLevelOffset);
					} else {
						currentTextureID = renderCitizen (cell, cit, iXGeneral, iYGeneral, currentTextureID, bGraphiChanged, fColorShadowLight, bLight, iDepth, zLevelOffset);
						if (!bGraphiChanged) {
							currentTextureID = renderCarrying (cell, cit, iXGeneral, iYGeneral, currentTextureID, fColorShadowLight, bLight, iDepth, zLevelOffset);
						}
					}
				} else {
					currentTextureID = renderCitizen (cell, cit, iXGeneral, iYGeneral, currentTextureID, bGraphiChanged, fColorShadowLight, bLight, iDepth, zLevelOffset);
					if (!bGraphiChanged) {
						currentTextureID = renderCarrying (cell, cit, iXGeneral, iYGeneral, currentTextureID, fColorShadowLight, bLight, iDepth, zLevelOffset);
					}
				}

				// Miramos si tiene que mostrar el signo de exclamación
				if (cit.getShowExclamationTurns () > 0) {
					Tile tileExclamation = World.getTileCitizenExclamation ();
					iYSpecific = iYGeneral - (tileExclamation.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + tileExclamation.getTileHeightOffset () + (int) cit.getPositionOffset ().y;
					iXSpecific = iXGeneral + (int) cit.getPositionOffset ().x;

					currentTextureID = setColorShadowLightCell (cell, tileExclamation, zLevelOffset, currentTextureID, false);
					UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + tileExclamation.getTileWidth (), iYSpecific + tileExclamation.getTileHeight (), tileExclamation.getTileSetTexX0 (), tileExclamation.getTileSetTexY0 (), tileExclamation.getTileSetTexX1 (), tileExclamation.getTileSetTexY1 (), iDepth);
				}

				// Miramos si duerme o come
				if (cit.getCitizenData ().getBlinkAnimationTurns () > (CitizenData.MAX_BLINK_ANIMATION_TURNS / 2)) {
					Tile tileTask = null;
					if (cit.isSleeping ()) {
						tileTask = World.getTileCitizenSleeping ();
					} else if (cit.getCitizenData ().getHungry () <= 0) {
						tileTask = World.getTileCitizenEating ();
					}

					if (tileTask != null) {
						iYSpecific = iYGeneral - (tileTask.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + tileTask.getTileHeightOffset () + (int) cit.getPositionOffset ().y;
						iXSpecific = iXGeneral + (int) cit.getPositionOffset ().x;

						currentTextureID = setColorShadowLightCellNoLight (cell, tileTask, zLevelOffset, currentTextureID, false);
						UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + tileTask.getTileWidth (), iYSpecific + tileTask.getTileHeight (), tileTask.getTileSetTexX0 (), tileTask.getTileSetTexY0 (), tileTask.getTileSetTexX1 (), tileTask.getTileSetTexY1 (), iDepth);

						// En el caso de comer, miramos si está pasando hambre porque no hay comida
						if (!cit.isSleeping () && cit.getCitizenData ().getHungryEating () < 0) {
							// Dibujamos la cruz roja
							Tile tileRedCross = World.getTileRedCross ();
							// currentTextureID = UtilsGL.setTexture (tileRedCross, currentTextureID);
							currentTextureID = setColorShadowLightCellNoLight (cell, tileRedCross, zLevelOffset, currentTextureID, false);
							UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + tileRedCross.getTileWidth (), iYSpecific + tileRedCross.getTileHeight (), tileRedCross.getTileSetTexX0 (), tileRedCross.getTileSetTexY0 (), tileRedCross.getTileSetTexX1 (), tileRedCross.getTileSetTexY1 (), iDepth);
						}
					}
				}

			} else if (lemi.getType () == LivingEntity.TYPE_HERO) {
				// Comprobamos que no tenga un effect de graphicchange
				Hero hero = (Hero) le;
				boolean bGraphiChanged = false;
				for (int e = 0; e < hero.getLivingEntityData ().getEffects ().size (); e++) {
					if (hero.getLivingEntityData ().getEffects ().get (e).isGraphicChange ()) {
						bGraphiChanged = true;
						break;
					}
				}

				currentTextureID = renderHero (cell, hero, iXGeneral, iYGeneral, currentTextureID, bGraphiChanged, fColorShadowLight, bLight, iDepth, zLevelOffset);
			} else if (lemi.getType () == LivingEntity.TYPE_ENEMY) {
				Enemy enemy = (Enemy) le;
				if (le.getTileWidth () != Tile.TERRAIN_ICON_WIDTH) {
					iXSpecific = iXGeneral - (le.getTileWidth () / 2) + (Tile.TERRAIN_ICON_WIDTH / 2) + (int) le.getPositionOffset ().x;
				} else {
					iXSpecific = iXGeneral + (int) le.getPositionOffset ().x;
				}
				iYSpecific = iYGeneral - (le.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + (int) le.getPositionOffset ().y;

				if (lemi.isFacingDirections ()) {
					iFacingDirection = le.getFacingDirection ();
					// Carrying?
					if (iFacingDirection == LivingEntity.FACING_DIRECTION_NORTH || iFacingDirection == LivingEntity.FACING_DIRECTION_NORTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_EAST) {
						if (enemy.getCarryingData () != null) {
							if (enemy.getCarryingData ().getCarrying () != null) {
								Item itemCarrying = enemy.getCarryingData ().getCarrying ();
								int iXCarrying = iXSpecific + (le.getTileWidth () / 2) - (itemCarrying.getTileWidth () / 2);
								int iYCarrying = iYSpecific + (le.getTileHeight ()) - itemCarrying.getTileHeight ();
								currentTextureID = setColorShadowLightCell (cell, itemCarrying, zLevelOffset, currentTextureID, false);
								UtilsGL.drawTextureZ (iXCarrying, iYCarrying, iXCarrying + itemCarrying.getTileWidth (), iYCarrying + itemCarrying.getTileHeight (), itemCarrying.getTileSetTexX0 (), itemCarrying.getTileSetTexY0 (), itemCarrying.getTileSetTexX1 (), itemCarrying.getTileSetTexY1 (), iDepth);
							}
							if (enemy.getCarryingData ().getCarryingLiving () != null) {
								LivingEntity leCarrying = enemy.getCarryingData ().getCarryingLiving ();
								int iXCarrying = iXSpecific + (le.getTileWidth () / 2) - (leCarrying.getTileWidth () / 2);
								int iYCarrying = iYSpecific + (le.getTileHeight ()) - leCarrying.getTileHeight ();
								currentTextureID = setColorShadowLightCell (cell, leCarrying, zLevelOffset, currentTextureID, false);
								UtilsGL.drawTextureZ (iXCarrying, iYCarrying + le.getTileHeight (), iXCarrying + leCarrying.getTileWidth (), iYCarrying, leCarrying.getTileSetTexX0 (), leCarrying.getTileSetTexY0 (), leCarrying.getTileSetTexX1 (), leCarrying.getTileSetTexY1 (), iDepth);
							}
						}
					}

					// Miramos si hay que hacer flip según la dirección donde mire
					// currentTextureID = UtilsGL.setTexture (le, currentTextureID);
					currentTextureID = setColorShadowLightCell (cell, le, zLevelOffset, currentTextureID, false);
					if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
						// Flip
						UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + le.getTileWidth (), iYSpecific + le.getTileHeight (), le.getTileSetTexX1 (), le.getTileSetTexY0 (), le.getTileSetTexX0 (), le.getTileSetTexY1 (), iDepth);
					} else {
						UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + le.getTileWidth (), iYSpecific + le.getTileHeight (), le.getTileSetTexX0 (), le.getTileSetTexY0 (), le.getTileSetTexX1 (), le.getTileSetTexY1 (), iDepth);
					}

					// Carrying?
					if (iFacingDirection != LivingEntity.FACING_DIRECTION_NORTH && iFacingDirection != LivingEntity.FACING_DIRECTION_NORTH_EAST && iFacingDirection != LivingEntity.FACING_DIRECTION_EAST) {
						if (enemy.getCarryingData () != null) {
							if (enemy.getCarryingData ().getCarrying () != null) {
								Item itemCarrying = enemy.getCarryingData ().getCarrying ();
								int iXCarrying = iXSpecific + (le.getTileWidth () / 2) - (itemCarrying.getTileWidth () / 2);
								int iYCarrying = iYSpecific + (le.getTileHeight ()) - itemCarrying.getTileHeight ();
								currentTextureID = setColorShadowLightCell (cell, itemCarrying, zLevelOffset, currentTextureID, false);
								UtilsGL.drawTextureZ (iXCarrying, iYCarrying, iXCarrying + itemCarrying.getTileWidth (), iYCarrying + itemCarrying.getTileHeight (), itemCarrying.getTileSetTexX0 (), itemCarrying.getTileSetTexY0 (), itemCarrying.getTileSetTexX1 (), itemCarrying.getTileSetTexY1 (), iDepth);
							}
							if (enemy.getCarryingData ().getCarryingLiving () != null) {
								LivingEntity leCarrying = enemy.getCarryingData ().getCarryingLiving ();
								int iXCarrying = iXSpecific + (le.getTileWidth () / 2) - (leCarrying.getTileWidth () / 2);
								int iYCarrying = iYSpecific + (le.getTileHeight ()) - leCarrying.getTileHeight ();
								currentTextureID = setColorShadowLightCell (cell, leCarrying, zLevelOffset, currentTextureID, false);
								UtilsGL.drawTextureZ (iXCarrying, iYCarrying + le.getTileHeight (), iXCarrying + leCarrying.getTileWidth (), iYCarrying, leCarrying.getTileSetTexX0 (), leCarrying.getTileSetTexY0 (), leCarrying.getTileSetTexX1 (), leCarrying.getTileSetTexY1 (), iDepth);
							}
						}
					}
				} else {
					currentTextureID = setColorShadowLightCell (cell, le, zLevelOffset, currentTextureID, false);
					UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + le.getTileWidth (), iYSpecific + le.getTileHeight (), le.getTileSetTexX0 (), le.getTileSetTexY0 (), le.getTileSetTexX1 (), le.getTileSetTexY1 (), iDepth);
					// Carrying
					if (enemy.getCarryingData () != null) {
						if (enemy.getCarryingData ().getCarrying () != null) {
							Item itemCarrying = enemy.getCarryingData ().getCarrying ();
							iXSpecific = iXSpecific + (le.getTileWidth () / 2) - (itemCarrying.getTileWidth () / 2);
							iYSpecific = iYSpecific + (le.getTileHeight ()) - itemCarrying.getTileHeight ();
							currentTextureID = setColorShadowLightCell (cell, itemCarrying, zLevelOffset, currentTextureID, false);
							UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + itemCarrying.getTileWidth (), iYSpecific + itemCarrying.getTileHeight (), itemCarrying.getTileSetTexX0 (), itemCarrying.getTileSetTexY0 (), itemCarrying.getTileSetTexX1 (), itemCarrying.getTileSetTexY1 (), iDepth);
						}
					}
				}

			} else {
				// RENDER LIVINGS (FRIENDLIES?)
				if (le.getTileWidth () != Tile.TERRAIN_ICON_WIDTH) {
					iXSpecific = iXGeneral - (le.getTileWidth () / 2) + (Tile.TERRAIN_ICON_WIDTH / 2) + (int) le.getPositionOffset ().x;
				} else {
					iXSpecific = iXGeneral + (int) le.getPositionOffset ().x;
				}
				iYSpecific = iYGeneral - (le.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + (int) le.getPositionOffset ().y;

				currentTextureID = setColorShadowLightCell (cell, le, zLevelOffset, currentTextureID, false);
				if (lemi.isFacingDirections ()) {
					// Miramos si hay que hacer flip según la dirección donde mire
					iFacingDirection = le.getFacingDirection ();
					if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
						// Flip
						UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + le.getTileWidth (), iYSpecific + le.getTileHeight (), le.getTileSetTexX1 (), le.getTileSetTexY0 (), le.getTileSetTexX0 (), le.getTileSetTexY1 (), iDepth);
					} else {
						UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + le.getTileWidth (), iYSpecific + le.getTileHeight (), le.getTileSetTexX0 (), le.getTileSetTexY0 (), le.getTileSetTexX1 (), le.getTileSetTexY1 (), iDepth);
					}
				} else {
					UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + le.getTileWidth (), iYSpecific + le.getTileHeight (), le.getTileSetTexX0 (), le.getTileSetTexY0 (), le.getTileSetTexX1 (), le.getTileSetTexY1 (), iDepth);
				}
			}

			// Attack "animation" tile
			if (le.getAttackAnimationCounter () > 0) {
				if (le.getTileWidth () != Tile.TERRAIN_ICON_WIDTH) {
//					iXSpecific = iXGeneral - (le.getTileWidth () / 2) + (Tile.TERRAIN_ICON_WIDTH / 2) + (int) le.getPositionOffset ().x;
					iXSpecific = iXGeneral - (le.getTileWidth () / 2) + (Tile.TERRAIN_ICON_WIDTH / 2);
				} else {
//					iXSpecific = iXGeneral + (int) le.getPositionOffset ().x;
					iXSpecific = iXGeneral;
				}
				iXSpecific = iXSpecific + (le.getTileWidth () / 2) - lockedConnectorTile.getTileWidth () / 2;
				iYSpecific = iYGeneral + le.getTileHeight () / 2 - lockedConnectorTile.getTileHeight () / 2;

				currentTextureID = UtilsGL.setTexture (lockedConnectorTile, currentTextureID);
				UtilsGL.drawTextureZ (iXGeneral, iYGeneral, iXGeneral + lockedConnectorTile.getTileWidth (), iYGeneral + lockedConnectorTile.getTileHeight (), lockedConnectorTile.getTileSetTexX0 (), lockedConnectorTile.getTileSetTexY0 (), lockedConnectorTile.getTileSetTexX1 (), lockedConnectorTile.getTileSetTexY1 (), iDepth);
			}

			// Damage animation (for all the livings)
			if (le.getDamageAnimationCounter () > 0) {
				if (le.getDamageAnimationText () != null) {
					// Render it!
					if (le.getTileWidth () != Tile.TERRAIN_ICON_WIDTH) {
//						iXSpecific = iXGeneral - (le.getTileWidth () / 2) + (Tile.TERRAIN_ICON_WIDTH / 2) + (int) le.getPositionOffset ().x;
						iXSpecific = iXGeneral - (le.getTileWidth () / 2) + (Tile.TERRAIN_ICON_WIDTH / 2);
					} else {
//						iXSpecific = iXGeneral + (int) le.getPositionOffset ().x;
						iXSpecific = iXGeneral;
					}
					iXSpecific = iXSpecific + (le.getTileWidth () / 2) - le.getDamageAnimationTextWidth () / 2;
					iYSpecific = iYGeneral - (le.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + (int) le.getPositionOffset ().y - 4 * (LivingEntity.DAMAGE_ANIMATION_FPS - le.getDamageAnimationCounter ());

					currentTextureID = UtilsGL.setTexture (-1, Game.TEXTURE_FONT_ID);
					UtilsGL.drawStringZ (le.getDamageAnimationText (), iXSpecific, iYSpecific, ColorGL.RED, iDepth);

				}
			}

			le.updateAnimation (lemi.isAnimatedWhenIdle ());

			if (!le.isFighting () || le.getPath ().size () != 1 || le.getAttackAnimationCounter () > 0) {
				le.updatePathOffsets ();
			}
		}

		return currentTextureID;
	}


	private static int renderCitizen (Cell cell, Citizen cit, int iXGeneral, int iYGeneral, int currentTextureID, boolean bGraphicChanged, float fColorShadowLight, boolean bLight, int iDepth, int zLevelOffset) {
		int iFacingDirection = cit.getFacingDirection ();
		int iXSpecific = iXGeneral + (int) cit.getPositionOffset ().x;
		int iYSpecific = iYGeneral - (cit.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + (int) cit.getPositionOffset ().y;

		// Render
		currentTextureID = setColorShadowLightCell (cell, cit, zLevelOffset, currentTextureID, false);
		if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
			// Flip
			UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + cit.getTileWidth (), iYSpecific + cit.getTileHeight (), cit.getTileSetTexX1 (), cit.getTileSetTexY0 (), cit.getTileSetTexX0 (), cit.getTileSetTexY1 (), iDepth);
		} else {
			UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + cit.getTileWidth (), iYSpecific + cit.getTileHeight (), cit.getTileSetTexX0 (), cit.getTileSetTexY0 (), cit.getTileSetTexX1 (), cit.getTileSetTexY1 (), iDepth);
		}

		// Miramos si lleva algo equipado para dibujarlo
		if (!bGraphicChanged) {
			EquippedData equippedData = cit.getEquippedData ();
			if (equippedData.isWearing (MilitaryItem.LOCATION_BODY)) {
				MilitaryItem mi = equippedData.getBody ();
				float height = mi.getBaseTileSetTexY1 () - mi.getBaseTileSetTexY0 ();
				float directionOffset = mi.getFacingDirectionYOffset (iFacingDirection);
				currentTextureID = setColorShadowLightCell (cell, mi, zLevelOffset, currentTextureID, false);
				if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
					// Flip
					UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_body_x (), iYSpecific + cit.getOffset_body_y (), iXSpecific + cit.getOffset_body_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_body_y () + mi.getTileHeight (), mi.getTileSetTexX1 (), directionOffset, mi.getTileSetTexX0 (), directionOffset + height, iDepth);
				} else {
					UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_body_x (), iYSpecific + cit.getOffset_body_y (), iXSpecific + cit.getOffset_body_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_body_y () + mi.getTileHeight (), mi.getTileSetTexX0 (), directionOffset, mi.getTileSetTexX1 (), directionOffset + height, iDepth);
				}
				mi.updateAnimation (false);
			}
			if (equippedData.isWearing (MilitaryItem.LOCATION_HEAD)) {
				MilitaryItem mi = equippedData.getHead ();
				float height = mi.getBaseTileSetTexY1 () - mi.getBaseTileSetTexY0 ();
				float directionOffset = mi.getFacingDirectionYOffset (iFacingDirection);
				currentTextureID = setColorShadowLightCell (cell, mi, zLevelOffset, currentTextureID, false);
				if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
					// Flip
					UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_head_x (), iYSpecific + cit.getOffset_head_y (), iXSpecific + cit.getOffset_head_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_head_y () + mi.getTileHeight (), mi.getTileSetTexX1 (), directionOffset, mi.getTileSetTexX0 (), directionOffset + height, iDepth);
				} else {
					UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_head_x (), iYSpecific + cit.getOffset_head_y (), iXSpecific + cit.getOffset_head_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_head_y () + mi.getTileHeight (), mi.getTileSetTexX0 (), directionOffset, mi.getTileSetTexX1 (), directionOffset + height, iDepth);
				}
				mi.updateAnimation (false);
			}
			if (equippedData.isWearing (MilitaryItem.LOCATION_FEET)) {
				MilitaryItem mi = equippedData.getFeet ();
				float height = mi.getBaseTileSetTexY1 () - mi.getBaseTileSetTexY0 ();
				float directionOffset = mi.getFacingDirectionYOffset (iFacingDirection);
				currentTextureID = setColorShadowLightCell (cell, mi, zLevelOffset, currentTextureID, false);
				if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
					// Flip
					UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_feet_x (), iYSpecific + cit.getOffset_feet_y (), iXSpecific + cit.getOffset_feet_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_feet_y () + mi.getTileHeight (), mi.getTileSetTexX1 (), directionOffset, mi.getTileSetTexX0 (), directionOffset + height, iDepth);
				} else {
					UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_feet_x (), iYSpecific + cit.getOffset_feet_y (), iXSpecific + cit.getOffset_feet_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_feet_y () + mi.getTileHeight (), mi.getTileSetTexX0 (), directionOffset, mi.getTileSetTexX1 (), directionOffset + height, iDepth);
				}
				mi.updateAnimation (false);
			}
			if (equippedData.isWearing (MilitaryItem.LOCATION_LEGS)) {
				MilitaryItem mi = equippedData.getLegs ();
				float height = mi.getBaseTileSetTexY1 () - mi.getBaseTileSetTexY0 ();
				float directionOffset = mi.getFacingDirectionYOffset (iFacingDirection);
				currentTextureID = setColorShadowLightCell (cell, mi, zLevelOffset, currentTextureID, false);
				if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
					// Flip
					UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_legs_x (), iYSpecific + cit.getOffset_legs_y (), iXSpecific + cit.getOffset_legs_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_legs_y () + mi.getTileHeight (), mi.getTileSetTexX1 (), directionOffset, mi.getTileSetTexX0 (), directionOffset + height, iDepth);
				} else {
					UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_legs_x (), iYSpecific + cit.getOffset_legs_y (), iXSpecific + cit.getOffset_legs_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_legs_y () + mi.getTileHeight (), mi.getTileSetTexX0 (), directionOffset, mi.getTileSetTexX1 (), directionOffset + height, iDepth);
				}
				mi.updateAnimation (false);
			}
		}

		return currentTextureID;
	}


	private static int renderHero (Cell cell, Hero hero, int iXGeneral, int iYGeneral, int currentTextureID, boolean bGraphicChanged, float fColorShadowLight, boolean bLight, int iDepth, int zLevelOffset) {
		int iFacingDirection = hero.getFacingDirection ();

		// Render
		// Carrying?
		if (!bGraphicChanged) {
			if (iFacingDirection == LivingEntity.FACING_DIRECTION_NORTH || iFacingDirection == LivingEntity.FACING_DIRECTION_NORTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_EAST) {
				currentTextureID = renderCarrying (cell, hero, iXGeneral, iYGeneral, currentTextureID, fColorShadowLight, bLight, iDepth, zLevelOffset);
			}
		}

		// Hero
		int iXSpecific = iXGeneral + (int) hero.getPositionOffset ().x;
		int iYSpecific = iYGeneral - (hero.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + (int) hero.getPositionOffset ().y;
		currentTextureID = setColorShadowLightCell (cell, hero, zLevelOffset, currentTextureID, false);
		if (LivingEntityManager.getItem (hero.getIniHeader ()).isFacingDirections ()) {
			if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
				// Flip
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + hero.getTileWidth (), iYSpecific + hero.getTileHeight (), hero.getTileSetTexX1 (), hero.getTileSetTexY0 (), hero.getTileSetTexX0 (), hero.getTileSetTexY1 (), iDepth);
			} else {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + hero.getTileWidth (), iYSpecific + hero.getTileHeight (), hero.getTileSetTexX0 (), hero.getTileSetTexY0 (), hero.getTileSetTexX1 (), hero.getTileSetTexY1 (), iDepth);
			}
		} else {
			UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + hero.getTileWidth (), iYSpecific + hero.getTileHeight (), hero.getTileSetTexX0 (), hero.getTileSetTexY0 (), hero.getTileSetTexX1 (), hero.getTileSetTexY1 (), iDepth);
		}

		// Carrying?
		if (!bGraphicChanged) {
			if (iFacingDirection != LivingEntity.FACING_DIRECTION_NORTH && iFacingDirection != LivingEntity.FACING_DIRECTION_NORTH_EAST && iFacingDirection != LivingEntity.FACING_DIRECTION_EAST) {
				currentTextureID = renderCarrying (cell, hero, iXGeneral, iYGeneral, currentTextureID, fColorShadowLight, bLight, iDepth, zLevelOffset);
			}
		}

		// Miramos si duerme o come
		if (hero.getCitizenData ().getBlinkAnimationTurns () > (CitizenData.MAX_BLINK_ANIMATION_TURNS / 2)) {
			Tile tileTask = null;
			if (hero.isSleeping ()) {
				tileTask = World.getTileCitizenSleeping ();
			} else if (hero.getCitizenData ().getHungry () <= 0) {
				tileTask = World.getTileCitizenEating ();
			}

			if (tileTask != null) {
				iXSpecific = iXGeneral + (int) hero.getPositionOffset ().x;
				iYSpecific = iYGeneral - (tileTask.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + tileTask.getTileHeightOffset () + (int) hero.getPositionOffset ().y;

				// En el caso de comer, miramos si está pasando hambre porque no hay comida
				if (!hero.isSleeping () && hero.getCitizenData ().getHungryEating () < 0) {
					// Dibujamos la cruz roja
					Tile tileRedCross = World.getTileRedCross ();
					currentTextureID = setColorShadowLightCellNoLight (cell, tileRedCross, zLevelOffset, currentTextureID, false);
					UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + tileRedCross.getTileWidth (), iYSpecific + tileRedCross.getTileHeight (), tileRedCross.getTileSetTexX0 (), tileRedCross.getTileSetTexY0 (), tileRedCross.getTileSetTexX1 (), tileRedCross.getTileSetTexY1 (), iDepth);
				}

				currentTextureID = setColorShadowLightCellNoLight (cell, tileTask, zLevelOffset, currentTextureID, false);
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + tileTask.getTileWidth (), iYSpecific + tileTask.getTileHeight (), tileTask.getTileSetTexX0 (), tileTask.getTileSetTexY0 (), tileTask.getTileSetTexX1 (), tileTask.getTileSetTexY1 (), iDepth);
			}
		}

		return currentTextureID;
	}


	private static int renderCarrying (Cell cell, Citizen cit, int iXGeneral, int iYGeneral, int currentTextureID, float fColorShadowLight, boolean bLight, int iDepth, int zLevelOffset) {
		int iFacingDirection = cit.getFacingDirection ();

		// Miramos si está cargando algo para dibujarlo
		if (cit.getCarrying () != null) {
			int iXSpecific = iXGeneral + cit.getOffset_carry_x () + (int) cit.getPositionOffset ().x;
			int iYSpecific = iYGeneral - (cit.getCarrying ().getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + cit.getOffset_carry_y () + (int) cit.getPositionOffset ().y;

			currentTextureID = setColorShadowLightCell (cell, cit.getCarrying (), zLevelOffset, currentTextureID, false);
			if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + cit.getCarrying ().getTileWidth (), iYSpecific + cit.getCarrying ().getTileHeight (), cit.getCarrying ().getTileSetTexX1 (), cit.getCarrying ().getTileSetTexY0 (), cit.getCarrying ().getTileSetTexX0 (), cit.getCarrying ().getTileSetTexY1 (), iDepth);
			} else {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + cit.getCarrying ().getTileWidth (), iYSpecific + cit.getCarrying ().getTileHeight (), cit.getCarrying ().getTileSetTexX0 (), cit.getCarrying ().getTileSetTexY0 (), cit.getCarrying ().getTileSetTexX1 (), cit.getCarrying ().getTileSetTexY1 (), iDepth);
			}
			cit.getCarrying ().updateAnimation (cit.getCarrying ().isFacingEast () || cit.getCarrying ().isFacingNorth ());
		}

		if (cit.getCarryingLiving () != null) {
			int iXSpecific = iXGeneral + cit.getOffset_carry_x () + (int) cit.getPositionOffset ().x;
			int iYSpecific = iYGeneral - (cit.getCarryingLiving ().getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + cit.getOffset_carry_y () + (int) cit.getPositionOffset ().y;

			currentTextureID = setColorShadowLightCell (cell, cit.getCarryingLiving (), zLevelOffset, currentTextureID, false);
			if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific + cit.getCarryingLiving ().getTileHeight (), iXSpecific + cit.getCarryingLiving ().getTileWidth (), iYSpecific, cit.getCarryingLiving ().getTileSetTexX1 (), cit.getCarryingLiving ().getTileSetTexY0 (), cit.getCarryingLiving ().getTileSetTexX0 (), cit.getCarryingLiving ().getTileSetTexY1 (), iDepth);
			} else {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific + cit.getCarryingLiving ().getTileHeight (), iXSpecific + cit.getCarryingLiving ().getTileWidth (), iYSpecific, cit.getCarryingLiving ().getTileSetTexX0 (), cit.getCarryingLiving ().getTileSetTexY0 (), cit.getCarryingLiving ().getTileSetTexX1 (), cit.getCarryingLiving ().getTileSetTexY1 (), iDepth);
			}
		}

		EquippedData equippedData = cit.getEquippedData ();
		if (equippedData.isWearing (MilitaryItem.LOCATION_WEAPON)) {
			int iXSpecific = iXGeneral + (int) cit.getPositionOffset ().x;
			int iYSpecific = iYGeneral - (cit.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + (int) cit.getPositionOffset ().y;
			MilitaryItem mi = equippedData.getWeapon ();
			float height = mi.getBaseTileSetTexY1 () - mi.getBaseTileSetTexY0 ();
			float directionOffset = mi.getFacingDirectionYOffset (iFacingDirection);
			currentTextureID = setColorShadowLightCell (cell, mi, zLevelOffset, currentTextureID, false);
			if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
				// Flip
				UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_weapon_x (), iYSpecific + cit.getOffset_weapon_y (), iXSpecific + cit.getOffset_weapon_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_weapon_y () + mi.getTileHeight (), mi.getTileSetTexX1 (), directionOffset, mi.getTileSetTexX0 (), directionOffset + height, iDepth);
			} else {
				UtilsGL.drawTextureZ (iXSpecific + cit.getOffset_weapon_x (), iYSpecific + cit.getOffset_weapon_y (), iXSpecific + cit.getOffset_weapon_x () + mi.getTileWidth (), iYSpecific + cit.getOffset_weapon_y () + mi.getTileHeight (), mi.getTileSetTexX0 (), directionOffset, mi.getTileSetTexX1 (), directionOffset + height, iDepth);
			}
			mi.updateAnimation (false);
		}

		return currentTextureID;
	}


	private static int renderCarrying (Cell cell, Hero hero, int iXGeneral, int iYGeneral, int currentTextureID, float fColorShadowLight, boolean bLight, int iDepth, int zLevelOffset) {
		int iFacingDirection = hero.getFacingDirection ();

		// Miramos si está cargando algo para dibujarlo
		if (hero.getCarrying () != null) {
			int iXSpecific = iXGeneral + hero.getOffset_carry_x () + (int) hero.getPositionOffset ().x;
			int iYSpecific = iYGeneral - (hero.getCarrying ().getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + hero.getOffset_carry_y () + (int) hero.getPositionOffset ().y;

			currentTextureID = setColorShadowLightCell (cell, hero.getCarrying (), zLevelOffset, currentTextureID, false);
			if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + hero.getCarrying ().getTileWidth (), iYSpecific + hero.getCarrying ().getTileHeight (), hero.getCarrying ().getTileSetTexX1 (), hero.getCarrying ().getTileSetTexY0 (), hero.getCarrying ().getTileSetTexX0 (), hero.getCarrying ().getTileSetTexY1 (), iDepth);
			} else {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific, iXSpecific + hero.getCarrying ().getTileWidth (), iYSpecific + hero.getCarrying ().getTileHeight (), hero.getCarrying ().getTileSetTexX0 (), hero.getCarrying ().getTileSetTexY0 (), hero.getCarrying ().getTileSetTexX1 (), hero.getCarrying ().getTileSetTexY1 (), iDepth);
			}
			hero.getCarrying ().updateAnimation (hero.getCarrying ().isFacingEast () || hero.getCarrying ().isFacingNorth ());
		}

		if (hero.getCarryingLiving () != null) {
			int iXSpecific = iXGeneral + hero.getOffset_carry_x () + (int) hero.getPositionOffset ().x;
			int iYSpecific = iYGeneral - (hero.getCarryingLiving ().getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + hero.getOffset_carry_y () + (int) hero.getPositionOffset ().y;

			currentTextureID = setColorShadowLightCell (cell, hero.getCarryingLiving (), zLevelOffset, currentTextureID, false);
			if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific + hero.getCarryingLiving ().getTileHeight (), iXSpecific + hero.getCarryingLiving ().getTileWidth (), iYSpecific, hero.getCarryingLiving ().getTileSetTexX1 (), hero.getCarryingLiving ().getTileSetTexY0 (), hero.getCarryingLiving ().getTileSetTexX0 (), hero.getCarryingLiving ().getTileSetTexY1 (), iDepth);
			} else {
				UtilsGL.drawTextureZ (iXSpecific, iYSpecific + hero.getCarryingLiving ().getTileHeight (), iXSpecific + hero.getCarryingLiving ().getTileWidth (), iYSpecific, hero.getCarryingLiving ().getTileSetTexX0 (), hero.getCarryingLiving ().getTileSetTexY0 (), hero.getCarryingLiving ().getTileSetTexX1 (), hero.getCarryingLiving ().getTileSetTexY1 (), iDepth);
			}
		}

		EquippedData equippedData = hero.getEquippedData ();
		if (equippedData.isWearing (MilitaryItem.LOCATION_WEAPON)) {
			int iXSpecific = iXGeneral + (int) hero.getPositionOffset ().x;
			int iYSpecific = iYGeneral - (hero.getTileHeight () - Tile.TERRAIN_ICON_HEIGHT) + (int) hero.getPositionOffset ().y;
			MilitaryItem mi = equippedData.getWeapon ();
			float height = mi.getBaseTileSetTexY1 () - mi.getBaseTileSetTexY0 ();
			float directionOffset = mi.getFacingDirectionYOffset (iFacingDirection);
			currentTextureID = setColorShadowLightCell (cell, mi, zLevelOffset, currentTextureID, false);
			if (iFacingDirection == LivingEntity.FACING_DIRECTION_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST || iFacingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
				// Flip
				UtilsGL.drawTextureZ (iXSpecific + hero.getOffset_weapon_x (), iYSpecific + hero.getOffset_weapon_y (), iXSpecific + hero.getOffset_weapon_x () + mi.getTileWidth (), iYSpecific + hero.getOffset_weapon_y () + mi.getTileHeight (), mi.getTileSetTexX1 (), directionOffset, mi.getTileSetTexX0 (), directionOffset + height, iDepth);
			} else {
				UtilsGL.drawTextureZ (iXSpecific + hero.getOffset_weapon_x (), iYSpecific + hero.getOffset_weapon_y (), iXSpecific + hero.getOffset_weapon_x () + mi.getTileWidth (), iYSpecific + hero.getOffset_weapon_y () + mi.getTileHeight (), mi.getTileSetTexX0 (), directionOffset, mi.getTileSetTexX1 (), directionOffset + height, iDepth);
			}
			mi.updateAnimation (false);
		}

		return currentTextureID;
	}


	/**
	 * Renderiza el tooltip
	 * 
	 * @param x X de la celda en pantalla
	 * @param y Y de la celda en pantalla
	 * @param pointTileMouse Celda a mostrar info
	 */
	private static void renderTooltip (int x, int y, Point3D pointTileMouse) {
		Cell cell;
		// int iZ3D;
		// if (tDMouseON) {
		// // iZ3D = getMaxZ3DMouse (pointTileMouse.x, pointTileMouse.y, pointTileMouse.z);
		// // cell = World.getCell (pointTileMouse.x, pointTileMouse.y, iZ3D);
		// cell = World.getCell (getMax3DMouse (pointTileMouse.x, pointTileMouse.y, pointTileMouse.z));
		// iZ3D = cell.getCoordinates ().z;
		// } else {
		// iZ3D = pointTileMouse.z;
		// cell = World.getCell (pointTileMouse);
		// }
		cell = World.getCell (pointTileMouse);

		ArrayList<String> alMessages = new ArrayList<String> ();
		ArrayList<ColorGL> alColor = new ArrayList<ColorGL> ();

		// Terrain / Zones
		String sMessage = null;
		ColorGL color;
		if (cell.isDiscovered ()) {
			if (cell.hasZone ()) {
				Zone zone = Zone.getZone (cell.getZoneID ());
				if (zone != null) {
					ZoneManagerItem zmi = ZoneManager.getItem (zone.getIniHeader ());
					if (zmi.getType () == ZoneManagerItem.TYPE_PERSONAL) {
						// Miramos si tiene aldeano
						int iCitID = ((ZonePersonal) zone).getOwnerID ();
						if (iCitID != -1) {
							Citizen cit = (Citizen) World.getLivingEntityByID (iCitID);
							if (cit != null) {
								// Tiene!
								StringBuffer sb = new StringBuffer (Messages.getString ("Zone.6")); //$NON-NLS-1$
								sb.append (cit.getCitizenData ().getFullName ());
								sb.append (Messages.getString ("Zone.7")); //$NON-NLS-1$
								sMessage = sb.toString ();
							}
						}
					} else if (zmi.getType () == ZoneManagerItem.TYPE_HERO_ROOM) {
						// Miramos si tiene héroe
						int iHeroID = ((ZoneHeroRoom) zone).getOwnerID ();
						if (iHeroID != -1) {
							Hero hero = (Hero) World.getLivingEntityByID (iHeroID);
							if (hero != null) {
								// Tiene!
								StringBuffer sb = new StringBuffer (Messages.getString ("Zone.6")); //$NON-NLS-1$
								sb.append (hero.getCitizenData ().getFullName ());
								sb.append (Messages.getString ("Zone.7")); //$NON-NLS-1$
								sMessage = sb.toString ();
							}
						}
					} else if (zmi.getType () == ZoneManagerItem.TYPE_BARRACKS) {
						// Miramos si tiene grupo
						int iGroupID = ((ZoneBarracks) zone).getGroupID ();
						if (iGroupID != -1) {
							if (iGroupID >= 0 && iGroupID < SoldierGroups.MAX_GROUPS) {
								// Tiene
								SoldierGroupData sgd = Game.getWorld ().getSoldierGroups ().getGroup (iGroupID);
								StringBuffer sb = new StringBuffer (Messages.getString ("Zone.8")); //$NON-NLS-1$
								sb.append (sgd.getName ());
								sb.append (Messages.getString ("Zone.9")); //$NON-NLS-1$
								sMessage = sb.toString ();
							}
						}
					}

					if (sMessage == null) {
						sMessage = zone.toString ();
					}
					sMessage = Character.toUpperCase (sMessage.charAt (0)) + sMessage.substring (1);
				}
			} else if (cell.hasStockPile ()) {
				Stockpile stockpile = Stockpile.getStockpile (cell.getCoordinates ());
				if (stockpile != null && stockpile.getType () != null && stockpile.getType ().getName () != null) {
					sMessage = Type.getTypeName (stockpile.getType ().getName ());
				}
			} else {
				if (cell.getTerrain ().hasFluids ()) {
					if (cell.getTerrain ().getFluidType () == Terrain.FLUIDS_WATER) {
						sMessage = Water.getTileName ();
					} else {
						// Lava
						sMessage = Lava.getTileName ();
					}
				} else {
					if (cell.isMined () && pointTileMouse.z < (World.MAP_DEPTH - 1)) {
						sMessage = TerrainManager.getItemByID (World.getCell (pointTileMouse.x, pointTileMouse.y, pointTileMouse.z + 1).getTerrain ().getTerrainID ()).getName ();
					} else {
						sMessage = TerrainManager.getItemByID (cell.getTerrain ().getTerrainID ()).getName ();
					}
				}
			}
		} else {
			sMessage = Messages.getString ("MainPanel.6"); //$NON-NLS-1$
		}

		if (sMessage != null) {
			alMessages.add (sMessage);
			alColor.add (ColorGL.WHITE);
		}

		boolean bNeedABlank = false;

		// Cell
		if (cell.isDiscovered ()) {
			sMessage = null;
			color = null;

			if (cell.hasEntity ()) {
				if (cell.getEntity () instanceof MilitaryItem) {
					sMessage = ((MilitaryItem) cell.getEntity ()).getExtendedTilename ();
					color = new ColorGL (((MilitaryItem) cell.getEntity ()).getItemTextColor ());
				} else {
					sMessage = cell.getEntity ().getTileName ();
				}
			} else {
				if (cell.hasBuilding ()) {
					cell = World.getCell (cell.getBuildingCoordinates ());
					if (cell.hasEntity ()) {
						sMessage = cell.getEntity ().getTileName ();
					}
				}
			}
			if (sMessage != null) {
				alMessages.add (sMessage);
				alColor.add (color);

				Item item = cell.getItem ();
				if (item != null) {
					// Buscamos la descripción
					ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
					if (imi.getDescriptions () != null && imi.getDescriptions ().size () > 0) {
						for (int d = 0; d < imi.getDescriptions ().size (); d++) {
							alMessages.add (imi.getDescriptions ().get (d));
							alColor.add (ColorGL.GRAY);
						}

						bNeedABlank = true;
					}

					// Miramos si puede tener texto
					if (imi.isText ()) {
						ArrayList<String> alTexts = World.getItemsText ().get (Integer.valueOf (item.getID ()));
						if (alTexts != null && alTexts.size () > 0) {
							if (bNeedABlank) {
								alMessages.add (""); //$NON-NLS-1$
								alColor.add (ColorGL.GRAY);
							}
							for (int t = 0; t < alTexts.size (); t++) {
								alMessages.add (alTexts.get (t));
								alColor.add (ColorGL.GRAY);
							}
						}
					}
				}
			}

			// Containers
			if (cell.hasItem ()) {
				if (ItemManager.getItem (cell.getEntity ().getIniHeader ()).isContainer ()) {
					Container container = Game.getWorld ().getContainer (cell.getEntity ().getID ());
					if (container != null) {
						ArrayList<String> alContent = container.getContentString ();
						for (int i = 0; i < alContent.size (); i++) {
							if (bNeedABlank) {
								alMessages.add (""); //$NON-NLS-1$
								alColor.add (ColorGL.GRAY);
								bNeedABlank = false;
							}
							alMessages.add (alContent.get (i));
							alColor.add (ColorGL.GRAY);
						}
					}
				}
			}
		}

		// Living entities
		if (cell.isDiscovered ()) {
			ArrayList<LivingEntity> alLivings = cell.getLivings ();
			if (alLivings != null) {
				LivingEntity le;
				LivingEntityManagerItem lemi;
				for (int i = 0; i < alLivings.size (); i++) {
					le = alLivings.get (i);
					lemi = LivingEntityManager.getItem (le.getIniHeader ());

					if (lemi.getType () == LivingEntity.TYPE_CITIZEN) {
						Citizen cit = (Citizen) le;
						if (bNeedABlank) {
							alMessages.add (""); //$NON-NLS-1$
							alColor.add (ColorGL.GRAY);
							bNeedABlank = false;
						}
						if (cit.getSoldierData ().isSoldier () && cit.getSoldierData ().getState () == SoldierData.STATE_IN_A_GROUP && cit.getSoldierData ().getGroup () >= 0 && cit.getSoldierData ().getGroup () < SoldierGroups.MAX_GROUPS) {
							alMessages.add (cit.getCitizenData ().getFullName () + " (" + cit.getLivingEntityData ().getHealthStatus () + ") (" + Game.getWorld ().getSoldierGroups ().getGroup (cit.getSoldierData ().getGroup ()).getName () + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							alColor.add (ColorGL.YELLOW);
						} else {
							// Grupo
							if (cit.getCitizenData ().getGroupID () != -1 && Game.getWorld ().getCitizenGroups ().getGroup (cit.getCitizenData ().getGroupID ()) != null) {
								alMessages.add (cit.getCitizenData ().getFullName () + " (" + cit.getLivingEntityData ().getHealthStatus () + ") (" + Game.getWorld ().getCitizenGroups ().getGroup (cit.getCitizenData ().getGroupID ()).getName () + ")"); //$NON-NLS-1$ //$NON-NLS-2$);
							} else {
								alMessages.add (cit.getCitizenData ().getFullName () + " (" + cit.getLivingEntityData ().getHealthStatus () + ")"); //$NON-NLS-1$ //$NON-NLS-2$);
							}
							alColor.add (ColorGL.YELLOW);
						}

						// Level / Xp
						if (cit.getSoldierData ().isSoldier ()) {
							alMessages.add (Messages.getString ("Hero.4") + cit.getSoldierData ().getLevel () + " (" + cit.getSoldierData ().getXp () + Messages.getString ("Hero.5") + cit.getSoldierData ().getXpPCT () + "%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							alColor.add (ColorGL.WHITE);
						}

						if (TownsProperties.DEBUG_MODE) {
							alMessages.add ("Hap/HWork/HIdle: " + cit.getCitizenData ().getHappiness () + "/" + cit.getCitizenData ().getHappinessWorkCounter () + "/" + cit.getCitizenData ().getHappinessIdleCounter ());
							alColor.add (ColorGL.YELLOW);
						}
					} else if (lemi.getType () == LivingEntity.TYPE_HERO) {
						Hero hero = (Hero) le;
						if (bNeedABlank) {
							alMessages.add (""); //$NON-NLS-1$
							alColor.add (ColorGL.GRAY);
							bNeedABlank = false;
						}
						alMessages.add (hero.getCitizenData ().getFullName () + " (" + le.getLivingEntityData ().getHealthStatus () + ")"); //$NON-NLS-1$ //$NON-NLS-2$);
						alColor.add (ColorGL.YELLOW);
						alMessages.add (Messages.getString ("Hero.4") + hero.getHeroData ().getLevel () + " (" + hero.getHeroData ().getXp () + Messages.getString ("Hero.5") + hero.getHeroData ().getXpPCT () + "%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						alColor.add (ColorGL.WHITE);
						MilitaryItem mi = hero.getEquippedData ().getHead ();
						if (mi != null) {
							alMessages.add (ItemManager.getItem (mi.getIniHeader ()).getName ());
							alColor.add (ColorGL.GRAY);
						}
						mi = hero.getEquippedData ().getBody ();
						if (mi != null) {
							alMessages.add (ItemManager.getItem (mi.getIniHeader ()).getName ());
							alColor.add (ColorGL.GRAY);
						}
						mi = hero.getEquippedData ().getLegs ();
						if (mi != null) {
							alMessages.add (ItemManager.getItem (mi.getIniHeader ()).getName ());
							alColor.add (ColorGL.GRAY);
						}
						mi = hero.getEquippedData ().getFeet ();
						if (mi != null) {
							alMessages.add (ItemManager.getItem (mi.getIniHeader ()).getName ());
							alColor.add (ColorGL.GRAY);
						}
						mi = hero.getEquippedData ().getWeapon ();
						if (mi != null) {
							alMessages.add (ItemManager.getItem (mi.getIniHeader ()).getName ());
							alColor.add (ColorGL.GRAY);
						}

						// Friendship
						String sHeroFriends = HeroData.getFriendshipString (hero);
						if (sHeroFriends != null) {
							alMessages.add (sHeroFriends);
							alColor.add (ColorGL.WHITE);
						}
					} else {
						if (bNeedABlank) {
							alMessages.add (""); //$NON-NLS-1$
							alColor.add (ColorGL.GRAY);
							bNeedABlank = false;
						}
						if (lemi.getCaravan () != null) {
							alMessages.add (le.getLivingEntityData ().getName ());
						} else {
							alMessages.add (le.getLivingEntityData ().getName () + " (" + le.getLivingEntityData ().getHealthStatus () + ")"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						alColor.add (ColorGL.YELLOW);
					}

					// Effects
					if (le.getLivingEntityData ().getEffects ().size () > 0) {
						StringBuffer sBuffer = new StringBuffer ();
						EffectData eData;
						for (int e = 0; e < le.getLivingEntityData ().getEffects ().size (); e++) {
							eData = le.getLivingEntityData ().getEffects ().get (e);
							sBuffer.append (EffectManager.getItem (eData.getEffectID ()).getName ());
							if ((e + 1) < le.getLivingEntityData ().getEffects ().size ()) {
								sBuffer.append (", "); //$NON-NLS-1$
							}
						}

						if (bNeedABlank) {
							alMessages.add (""); //$NON-NLS-1$
							alColor.add (ColorGL.GRAY);
							bNeedABlank = false;
						}
						alMessages.add (Messages.getString ("LivingEntity.7") + sBuffer.toString ()); //$NON-NLS-1$
						alColor.add (ColorGL.ORANGE);
					}
				}
			}
		}

		// alMessages.add ("x " + cell.getCoordinates ().x + " y " + cell.getCoordinates ().y + " z " + cell.getCoordinates ().z + " XMin " + depthXMin + " YMin " + depthYMin + " Depth " + cell.getDepth ());
		// alColor.add (ColorGL.ORANGE);
		// alMessages.add ("max " + maxTilesWidthHeight);
		// alColor.add (ColorGL.ORANGE);
		// Render
		renderMessages (x, y, renderWidth, renderHeight, Tile.TERRAIN_ICON_WIDTH, alMessages, alColor);
	}


	public static void renderMessages (int x, int y, int renderWidth, int renderHeight, int SEPARADOR, ArrayList<String> alMessages, ArrayList<ColorGL> alColor) {
		if (alMessages.size () > 0) {
			int iWidth = UtilFont.getWidth (alMessages.get (0));
			int iMaxLength = iWidth;
			for (int i = 1; i < alMessages.size (); i++) {
				iMaxLength = UtilFont.getWidth (alMessages.get (i));
				if (iWidth < iMaxLength) {
					iWidth = iMaxLength;
				}
			}
			int iX = x + SEPARADOR;
			int iY = y;
			int iHeight = alMessages.size () * (UtilFont.MAX_HEIGHT + 5) - 5;

			// Miramos si cabe
			if ((iX + iWidth) >= (renderWidth)) {
				iX = iX - iWidth - SEPARADOR;
				if (iX < 0) {
					iX = 0;
				}
			}
			if ((iY + iHeight) >= (renderHeight)) {
				iY = iY - ((iY + iHeight) - (renderHeight));
			}

			GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, tileTooltipBackground.getTextureID ());
			UtilsGL.glBegin (GL11.GL_QUADS);
			UtilsGL.drawTexture (iX, iY, iX + iWidth, iY + iHeight, tileTooltipBackground.getTileSetTexX0 (),tileTooltipBackground.getTileSetTexY0 (), tileTooltipBackground.getTileSetTexX1 (), tileTooltipBackground.getTileSetTexY1 ());
			UtilsGL.glEnd ();
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
			UtilsGL.glBegin (GL11.GL_QUADS);

			for (int i = 0; i < alMessages.size (); i++) {
				UtilsGL.drawString (alMessages.get (i), iX, iY + (i * (UtilFont.MAX_HEIGHT + 5)), alColor.get (i));
			}

			UtilsGL.glEnd ();
		}
	}


	/**
	 * Mouse pressed
	 * 
	 * @param x
	 * @param y
	 * @param mouseButton
	 */
	public void mousePressed (int x, int y, int mouseButton) {
		if (typingPanel != null) {
			return;
		}

		Point3D view = Game.getWorld ().getView ();
		Point3D p3d = getTileMouse (x, y, view.x, view.y, view.z);
		if (p3d == null) {
			return;
		}

		if (Game.getCurrentState () == Game.STATE_CREATING_TASK) {
			// if (tDMouseON) {
			// //Game.getCurrentTask ().setPoint (new Point3D (p3d.x, p3d.y, getMaxZ3DMouse (p3d.x, p3d.y, p3d.z)));
			// Game.getCurrentTask ().setPoint (getMax3DMouse (p3d.x, p3d.y, p3d.z));
			// } else {
			// Game.getCurrentTask ().setPoint (p3d);
			// }
			Game.getCurrentTask ().setPoint (p3d);
		}
	}


	/**
	 * Retorna la celda en la que el mouse está apuntando. Se le pasa la view
	 * 
	 * @param x
	 * @param y
	 * @param xView
	 * @param yView
	 * @param zView
	 * @return
	 */
	private static Point3D getTileMouse (int x, int y, int xView, int yView, int zView) {
		// Mouse en ningún panel (o sea, en la main area)
		y -= Tile.TERRAIN_ICON_HEIGHT;
		int casellaX = (x / 2 - y) / Tile.TERRAIN_ICON_HEIGHT;
		if (x / 2 - y < 0) {
			casellaX--;
		}

		int casellaY = (x / 2 + y) / Tile.TERRAIN_ICON_HEIGHT;

		Point3D pointTileMouse = new Point3D (xView + casellaX - maxTilesX, yView + casellaY - maxTilesY, zView);

		if (tDMouseON) {
			pointTileMouse = getMax3DMouse (pointTileMouse.x, pointTileMouse.y, pointTileMouse.z);
		} else {
			if (pointTileMouse.x < 0 || pointTileMouse.x >= World.MAP_WIDTH || pointTileMouse.y < 0 || pointTileMouse.y >= World.MAP_HEIGHT) {
				return null;
			}
		}

		return pointTileMouse;
	}


	public static Point3D getMax3DMouse (int x, int y, int z) {
		int iX = x;
		int iY = y;
		int iZ = z;
		Cell cell;
		ItemManagerItem imi;
		Item item;

		while (iZ < World.MAP_DEPTH) {
			if (iX >= 0 && iX < World.MAP_WIDTH && iY >= 0 && iY < World.MAP_HEIGHT && iZ >= 0) {
				cell = World.getCell (iX, iY, iZ);

				// No minada o con fluidos
				if (!cell.isMined () || cell.getTerrain ().hasFluids ()) {
					return new Point3D (iX, iY, iZ);
				}

				item = cell.getItem ();
				if (item != null) {
					imi = ItemManager.getItem (item.getIniHeader ());

					// Objeto blocky
					if (imi.isBlocky ()) {
						return new Point3D (iX, iY, iZ);
					}
				}

				// Celda de abajo
				if (iZ < (World.MAP_DEPTH - 1)) {
					cell = World.getCell (iX, iY, iZ + 1);

					// No minada o con fluidos
					if (!cell.isMined () || cell.getTerrain ().hasFluids ()) {
						return new Point3D (iX, iY, iZ);
					}

					item = cell.getItem ();
					if (item != null) {
						imi = ItemManager.getItem (item.getIniHeader ());

						// Objeto base=true
						if (imi.isBase ()) {
							return new Point3D (iX, iY, iZ);
						}
					}
				}
			}

			iX++;
			iY--;
			iZ++;
		}

		return null;
	}


	/**
	 * Devuelve un contextmenúsegun la casilla en la que està
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public ContextMenu getContextMenu (int x, int y) {
		int iMousePanel = UIPanelInputHandler.isMouseOnAPanel(x, y);

		if (iMousePanel != MOUSE_NONE) {
			return null;
		}

		Point3D view = Game.getWorld ().getView ();
		Point3D p3d = getTileMouse (x, y, view.x, view.y, view.z);

		if (p3d == null) {
			return null;
		}

		// Segun lo que haya en el tile cargamos un menú u otro
		SmartMenu sm = new SmartMenu ();
		Cell cell = World.getCell (p3d);

		if (cell.isDiscovered ()) {
			// LIVINGS
			LivingEntity.fillMenu (cell, sm);

			// TERRAIN
			Terrain.fillMenu (cell, sm);

			// EDIFICIOS
			Building.fillMenu (cell, sm);

			// ITEMS
			Item.fillMenu (cell, sm);

			// STOCKPILES
			Stockpile.fillMenu (cell, sm);

			// ZONES
			Zone.fillMenu (cell, sm);
		}

		// Debug
		if (TownsProperties.DEBUG_MODE) {
			sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "A*ZI: " + cell.getAstarZoneID (), null, null, null, null, p3d)); //$NON-NLS-1$
			sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Coord: " + cell.getCoordinates (), null, null, null, null, p3d)); //$NON-NLS-1$
		}

		if (sm.getItems ().size () > 0) {
			sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("MainPanel.57"), null, CommandPanel.COMMAND_CLOSE_CONTEXT, null)); //$NON-NLS-1$

			ContextMenu cm = new ContextMenu ();
			cm.setX (x + 16); // El +16 es el ancho del cursor del mouse
			cm.setY (y);
			cm.setSmartMenu (sm);

			return cm;
		}

		return null;
	}


	/**
	 * Cambia la textura Y añadi brillo
	 * 
	 * @param iTexture
	 * 
	 * private static void unsetBrightNess (int iTexture) { //UtilsGL.glEnd (); // GL11.glBindTexture (GL11.GL_TEXTURE_2D, iTexture); // GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE); // GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE); //UtilsGL.glBegin (GL11.GL_QUADS); }
	 */
	public static void resize (int renderWidth, int renderHeight) {
		MainPanel.renderWidth = renderWidth;
		MainPanel.renderHeight = renderHeight;

		// Obtenemos el número de tiles máximo que caben por pantalla
		maxTilesWidthHeight = renderWidth / Tile.TERRAIN_ICON_WIDTH + renderHeight / Tile.TERRAIN_ICON_HEIGHT + 2;

		// Obtenemos el centro de la pantalla
		xCentro = ((renderWidth / 2) / Tile.TERRAIN_ICON_WIDTH) * Tile.TERRAIN_ICON_WIDTH;
		yCentro = ((renderHeight / 2) / Tile.TERRAIN_ICON_HEIGHT) * Tile.TERRAIN_ICON_HEIGHT - (Tile.TERRAIN_ICON_HEIGHT / 2);

		// Máximo de tiles X y Y
		maxTilesX = (xCentro / 2 - (yCentro + Tile.TERRAIN_ICON_HEIGHT / 2)) / Tile.TERRAIN_ICON_HEIGHT;
		maxTilesY = (xCentro / 2 + (yCentro + Tile.TERRAIN_ICON_HEIGHT / 2)) / Tile.TERRAIN_ICON_HEIGHT;
	}


	/**
	 * Limpia todos los datos (se usa cuando se sale de la partida y se va al menú principal)
	 */
	public void clear () {
	}
}
