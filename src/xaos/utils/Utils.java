package xaos.utils;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import xaos.Towns;
import xaos.TownsProperties;
import xaos.actions.ActionPriorityManager;
import xaos.campaign.MissionData;
import xaos.data.BuryData;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MainPanel;
import xaos.panels.MessagesPanel;
import xaos.property.MainProperties;
import xaos.property.PropertyFile;
import xaos.stockpiles.Stockpile;
import xaos.tiles.Cell;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;

public final class Utils {

	private static final LocalResourceClassLoader LOCAL_RESOURCE_CLASS_LOADER = new LocalResourceClassLoader();

	public static final int MAX_DISTANCE = World.MAP_WIDTH * World.MAP_HEIGHT * 512; // 512 por poner algo alto

	public static Random random = new Random();

	private Utils() { /*static utility class*/ }

	// ************
	// * 2D UTILS *
	// ************
	/**
	 * Returns a point in the Bézier curve
	 *
	 * @param pointSource Source point
	 * @param pointDest Dstination point
	 * @param pointControlA A-control point
	 * @param pointControlB B-control point
	 * @param dIteration Iteration from 0.0 (first point) to 1.0 (last point)
	 * @return
	 */
	public static Point getBezierPoint(Point pointSource, Point pointDest, Point pointControlA, Point pointControlB, double dIteration) {
		// Primeros puntos intermedios (3 puntos)
		double dS_A_X = pointSource.x + ((pointControlA.x - pointSource.x) * dIteration);
		double dS_A_Y = pointSource.y + ((pointControlA.y - pointSource.y) * dIteration);
		double dA_B_X = pointControlA.x + ((pointControlB.x - pointControlA.x) * dIteration);
		double dA_B_Y = pointControlA.y + ((pointControlB.y - pointControlA.y) * dIteration);
		double dB_D_X = pointControlB.x + ((pointDest.x - pointControlB.x) * dIteration);
		double dB_D_Y = pointControlB.y + ((pointDest.y - pointControlB.y) * dIteration);

		// Segundos puntos intermedios (2 puntos)
		double dSA_AB_X = dS_A_X + ((dA_B_X - dS_A_X) * dIteration);
		double dSA_AB_Y = dS_A_Y + ((dA_B_Y - dS_A_Y) * dIteration);
		double dAB_BD_X = dA_B_X + ((dB_D_X - dA_B_X) * dIteration);
		double dAB_BD_Y = dA_B_Y + ((dB_D_Y - dA_B_Y) * dIteration);

		// Punto final
		double dBezier_X = dSA_AB_X + ((dAB_BD_X - dSA_AB_X) * dIteration);
		double dBezier_Y = dSA_AB_Y + ((dAB_BD_Y - dSA_AB_Y) * dIteration);

		return new Point((int) dBezier_X, (int) dBezier_Y);
	}

	/**
	 * Returns true if point it's inside 0-(width-1),0-(height-1)
	 *
	 * @param iX Point X
	 * @param iY Point Y
	 * @param iWidth Width
	 * @param iHeight Height
	 * @return true if point it's inside 0-(width-1),0-(height-1)
	 */
	public static boolean isValidPoint(int iX, int iY, int iWidth, short iHeight) {
		return iX >= 0 && iX < iWidth && iY >= 0 && iY < iHeight;
	}

	/**
	 * Indica si la coordenada pasada está dentro del mapa
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static boolean isInsideMap(int x, int y, int z) {
		return x >= 0 && x < World.MAP_WIDTH && y >= 0 && y < World.MAP_HEIGHT && z >= 0 && z < World.MAP_DEPTH;
	}

	/**
	 * Indica si el punto (coordenada) pasado está dentro del mapa
	 *
	 * @param p3d
	 * @return
	 */
	public static boolean isInsideMap(Point3DShort p3d) {
		return p3d.x >= 0 && p3d.x < World.MAP_WIDTH && p3d.y >= 0 && p3d.y < World.MAP_HEIGHT && p3d.z >= 0 && p3d.z < World.MAP_DEPTH;
	}

	public static ArrayList<Point3DShort> bresenhamLine(Point3DShort p0, Point3DShort p1, short zLevel) { //, int livingType) {
		return bresenhamLine(p0.x, p0.y, p1.x, p1.y, zLevel); //, livingType);
	}

	public static boolean bresenhamLineExists(int x0, int y0, int x1, int y1, int zLevel) { //, int livingType) {
		ArrayList<Point3DShort> alBresenham = bresenhamLine(x0, y0, x1, y1, zLevel); //, livingType);
		if (alBresenham == null) {
			return false;
		} else {
			// Limpiamos la lista
			Point3DShort.returnToPool(alBresenham);
			return true;
		}
	}

	/**
	 * Returns an array of Point's representing a line between 2 points
	 *
	 * @param x0 Ini X
	 * @param y0 Ini Y
	 * @param x1 End X
	 * @param y1 End Y
	 * @param zLevel Z
	 * @return an array of Point's representing a line between 2 points, null if
	 * found a not allowed point
	 */
	public static ArrayList<Point3DShort> bresenhamLine(int x0, int y0, int x1, int y1, int zLevel) { //, int livingType) {
		if (x0 == x1 && y0 == y1) {
			return null;
		}

		ArrayList<Point3DShort> alReturn = new ArrayList<Point3DShort>();
		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			int iAux = x0;
			x0 = y0;
			y0 = iAux;
			iAux = x1;
			x1 = y1;
			y1 = iAux;
		}
		int deltax = Math.abs(x1 - x0);
		int deltay = Math.abs(y1 - y0);
		int error = deltax / 2;
		int y = y0;

		short inc = 1;
		if (x0 >= x1) {
			inc = -1;
		}

		short ystep = 1;
		if (y0 >= y1) {
			ystep = -1;
		}

		if (inc == 1) {
			for (int x = x0; x <= x1; x += inc) {
				if (steep) {
					if (!LivingEntity.isCellAllowed(y, x, zLevel)) {
						return null;
					}
					alReturn.add(Point3DShort.getPoolInstance(y, x, zLevel));
				} else {
					if (!LivingEntity.isCellAllowed(x, y, zLevel)) {
						return null;
					}
					alReturn.add(Point3DShort.getPoolInstance(x, y, zLevel));
				}
				error = error - deltay;
				if (error < 0) {
					y += ystep;
					error = error + deltax;
				}
			}
		} else {
			for (int x = x0; x >= x1; x += inc) {
				if (steep) {
					if (!LivingEntity.isCellAllowed(y, x, zLevel)) {
						return null;
					}
					alReturn.add(Point3DShort.getPoolInstance(y, x, zLevel));
				} else {
					if (!LivingEntity.isCellAllowed(x, y, zLevel)) {
						return null;
					}
					alReturn.add(Point3DShort.getPoolInstance(x, y, zLevel));
				}
				error = error - deltay;
				if (error < 0) {
					y += ystep;
					error = error + deltax;
				}
			}
		}

		return alReturn;
	}

	/**
	 * Descubre las celdas de la linea hasta que encuentra un no-mined
	 *
	 * @param x0 Ini X
	 * @param y0 Ini Y
	 * @param x1 End X
	 * @param y1 End Y
	 * @param zLevel Z
	 */
	public static void bresenhamLineDiscover(int x0, int y0, int x1, int y1, int zLevel) {
		if (x0 == x1 && y0 == y1) {
			return;
		}

		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			int iAux = x0;
			x0 = y0;
			y0 = iAux;
			iAux = x1;
			x1 = y1;
			y1 = iAux;
		}
		int deltax = Math.abs(x1 - x0);
		int deltay = Math.abs(y1 - y0);
		int error = deltax / 2;
		int y = y0;

		int inc = 1;
		if (x0 >= x1) {
			inc = -1;
		}

		int ystep = 1;
		if (y0 >= y1) {
			ystep = -1;
		}

		Cell cell;
		if (inc == 1) {
			for (int x = x0; x <= x1; x += inc) {
				if (steep) {
					cell = World.getCell(y, x, zLevel);
				} else {
					cell = World.getCell(x, y, zLevel);
				}
				cell.setDiscovered(true);
				if (!cell.isMined()) {
					return;
				} else {
					Item item = cell.getItem();
					if (item != null) {
						ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
						if (imi.isWall() && imi.isBlocky()) {
							return;
						}
					}
				}
				// Cell arriba
				if (zLevel > 0) {
					if (steep) {
						cell = World.getCell(y, x, zLevel - 1);
					} else {
						cell = World.getCell(x, y, zLevel - 1);
					}
					cell.setDiscovered(true);
				}
				error = error - deltay;
				if (error < 0) {
					y = y + ystep;
					error = error + deltax;
				}
			}
		} else {
			for (int x = x0; x >= x1; x += inc) {
				if (steep) {
					cell = World.getCell(y, x, zLevel);
				} else {
					cell = World.getCell(x, y, zLevel);
				}
				cell.setDiscovered(true);
				if (!cell.isMined()) {
					return;
				} else {
					Item item = cell.getItem();
					if (item != null) {
						ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
						if (imi.isWall() && imi.isBlocky()) {
							return;
						}
					}
				}
				// Cell arriba
				if (zLevel > 0) {
					if (steep) {
						cell = World.getCell(y, x, zLevel - 1);
					} else {
						cell = World.getCell(x, y, zLevel - 1);
					}
					cell.setDiscovered(true);
				}
				error = error - deltay;
				if (error < 0) {
					y = y + ystep;
					error = error + deltax;
				}
			}
		}
	}

	private static boolean isCellBlockingLight(Cell cell) {
		if (!cell.isMined()) {
			return true;
		} else {
			Item item = cell.getItem();
			if (item != null) {
				ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
				return isCellBlockingLightItem(imi, item);
			}
		}

		return false;
	}

	public static boolean isCellBlockingLightItem(ItemManagerItem imi, Item item) {
		if (imi == null || item == null) {
			return true;
		}

		if (imi.isTranslucent()) {
			return false;
		}

		if (imi.isWall()) {
			return true;
		}
		if (imi.isDoor() && !item.isDoorStatus(Item.FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED)) {
			return true;
		}

		return false;
	}

	/**
	 * Mira y pone las luzes si hace falta
	 *
	 * @param bNonMinedOrWall
	 * @param cell
	 * @param imiSource
	 * @param iDirection 0 = misma casilla, 1-NW 2-N 3-NE 4-W 5-E 6-SW 7-S 8-SE
	 */
	private static void checkSetLights(boolean bNonMinedOrWall, Cell cell, ItemManagerItem imiSource, int iDirection) {
		if (!bNonMinedOrWall) {
			cell.setLights(imiSource);
		} else {
			// Wall o non-mined, miramos si hay que pintarlo o no
			if (iDirection != 7 && iDirection != 4 && iDirection != 6) {
				if (iDirection == 2 || iDirection == 5 || iDirection == 3 || iDirection == 0) {
					cell.setLights(imiSource);
				} else if (iDirection == 8) {
					// Hay que mirar la casilla oeste
					if (cell.getCoordinates().x > 0) {
						Cell cellTmp = World.getCell(cell.getCoordinates().x - 1, cell.getCoordinates().y, cell.getCoordinates().z);
						if (!isCellBlockingLight(cellTmp)) {
							cell.setLights(imiSource);
						}
					}
				} else if (iDirection == 1) {
					// Hay que mirar la casilla sur
					if (cell.getCoordinates().y < (World.MAP_HEIGHT - 1)) {
						Cell cellTmp = World.getCell(cell.getCoordinates().x, cell.getCoordinates().y + 1, cell.getCoordinates().z);
						if (!isCellBlockingLight(cellTmp)) {
							cell.setLights(imiSource);
						}
					}
				}
			}
		}
	}

	/**
	 * Mira y pone las luzes si hace falta
	 *
	 * @param bNonMinedOrWall
	 * @param cell
	 * @param imiSource
	 * @param iDirection 0 = misma casilla, 1-NW 2-N 3-NE 4-W 5-E 6-SW 7-S 8-SE
	 * @return true si la luz se ha puesto
	 */
	private static boolean checkSetLightsUp(boolean bNonMinedOrWall, Cell cell, ItemManagerItem imiSource, int iDirection) {
		if (!bNonMinedOrWall) {
			cell.setLights(imiSource);
			return true;
		} else {
			// Wall o non-mined, miramos si hay que pintarlo o no
			if (iDirection != 7 && iDirection != 4 && iDirection != 6 && iDirection != 0) {
				if (iDirection == 2) {
					// Hay que mirar la casilla sur
					if (cell.getCoordinates().y < (World.MAP_HEIGHT - 1)) {
						Cell cellTmp = World.getCell(cell.getCoordinates().x, cell.getCoordinates().y + 1, cell.getCoordinates().z);
						if (!isCellBlockingLight(cellTmp)) {
							cell.setLights(imiSource);
							return true;
						}
					}
				} else if (iDirection == 5) {
					// Hay que mirar la casilla oeste
					if (cell.getCoordinates().x > 0) {
						Cell cellTmp = World.getCell(cell.getCoordinates().x - 1, cell.getCoordinates().y, cell.getCoordinates().z);
						if (!isCellBlockingLight(cellTmp)) {
							cell.setLights(imiSource);
							return true;
						}
					}
				} else if (iDirection == 3 || iDirection == 8) {
					// Hay que mirar la casilla sur
					if (cell.getCoordinates().y < (World.MAP_HEIGHT - 1)) {
						Cell cellTmp = World.getCell(cell.getCoordinates().x, cell.getCoordinates().y + 1, cell.getCoordinates().z);
						if (!isCellBlockingLight(cellTmp)) {
							cell.setLights(imiSource);
							return true;
						} else {
							// Hay que mirar la casilla oeste
							if (cell.getCoordinates().x > 0) {
								cellTmp = World.getCell(cell.getCoordinates().x - 1, cell.getCoordinates().y, cell.getCoordinates().z);
								if (!isCellBlockingLight(cellTmp)) {
									cell.setLights(imiSource);
									return true;
								}
							}
						}
					}
				} else if (iDirection == 1) {
					// Hay que mirar la casilla sur
					if (cell.getCoordinates().y < (World.MAP_HEIGHT - 1)) {
						Cell cellTmp = World.getCell(cell.getCoordinates().x, cell.getCoordinates().y + 1, cell.getCoordinates().z);
						if (!isCellBlockingLight(cellTmp)) {
							cell.setLights(imiSource);
							return true;
						} else {
							// Hay que mirar la casilla este
							if (cell.getCoordinates().x < (World.MAP_WIDTH - 1)) {
								cellTmp = World.getCell(cell.getCoordinates().x + 1, cell.getCoordinates().y, cell.getCoordinates().z);
								if (!isCellBlockingLight(cellTmp)) {
									cell.setLights(imiSource);
									return true;
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Pone luz a las celdas de la linea (y las de arriba y abajo teniendo en
	 * cuenta el radio) hasta que encuentra un no-mined o un wall
	 *
	 * @param x0 Ini X
	 * @param y0 Ini Y
	 * @param x1 End X
	 * @param y1 End Y
	 * @param zLevel Z
	 * @param iRadious Radio para poner luz a las casillas de arriba/abajo
	 */
	public static void bresenhamLineLight(int x0, int y0, int x1, int y1, int zLevel, int iRadious, ItemManagerItem imiSource) {
		if (x0 == x1 && y0 == y1) {
			return;
		}

		// Miramos la dirección de la luz
		int iDirection; // 0 = misma casilla, 1-NW 2-N 3-NE 4-W 5-E 6-SW 7-S 8-SE
		if (x0 == x1) {
			if (y0 == y1) {
				iDirection = 0;
			} else if (y0 > y1) {
				// Norte
				iDirection = 2;
			} else {
				// Sur
				iDirection = 7;
			}
		} else {
			if (x0 < x1) {
				if (y0 == y1) {
					// Este
					iDirection = 5;
				} else if (y0 < y1) {
					// Sureste
					iDirection = 8;
				} else {
					// Noreste
					iDirection = 3;
				}
			} else {
				if (y0 == y1) {
					// Oeste
					iDirection = 4;
				} else if (y0 < y1) {
					// Suroeste
					iDirection = 6;
				} else {
					// Noroeste
					iDirection = 1;
				}
			}
		}

		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			int iAux = x0;
			x0 = y0;
			y0 = iAux;
			iAux = x1;
			x1 = y1;
			y1 = iAux;
		}
		int deltax = Math.abs(x1 - x0);
		int deltay = Math.abs(y1 - y0);
		int error = deltax / 2;
		int y = y0;

		int inc = 1;
		if (x0 >= x1) {
			inc = -1;
		}

		int ystep = 1;
		if (y0 >= y1) {
			ystep = -1;
		}

		Cell cell, cellaux;
		int iZAux;
		boolean bNonMinedOrWallUpDown, bNonMinedOrWall;
		if (inc == 1) {
			bNonMinedOrWall = false;
			for (int x = x0; x <= x1; x += inc) {
				if (steep) {
					cell = World.getCell(y, x, zLevel);
				} else {
					cell = World.getCell(x, y, zLevel);
				}

				bNonMinedOrWall = isCellBlockingLight(cell);
				checkSetLights(bNonMinedOrWall, cell, imiSource, iDirection);

				// Arriba y abajo, teniendo en cuenta el radio
				// Arriba
				iZAux = zLevel - iRadious;
				if (iZAux < 0) {
					iZAux = 0;
				}

				bNonMinedOrWallUpDown = false;
				for (int i = (zLevel - 1); i >= iZAux; i--) {
					if (steep) {
						cellaux = World.getCell(y, x, i);
					} else {
						cellaux = World.getCell(x, y, i);
					}

					bNonMinedOrWallUpDown = isCellBlockingLight(cellaux);
					bNonMinedOrWallUpDown = checkSetLightsUp(bNonMinedOrWallUpDown, cellaux, imiSource, iDirection);

					if (!bNonMinedOrWallUpDown) {
						break;
					}
				}

				// Abajo
				iZAux = zLevel + iRadious;
				if (iZAux >= World.MAP_DEPTH) {
					iZAux = (World.MAP_DEPTH - 1);
				}

				bNonMinedOrWallUpDown = false;
				for (int i = (zLevel + 1); i <= iZAux; i++) {
					if (steep) {
						cellaux = World.getCell(y, x, i);
					} else {
						cellaux = World.getCell(x, y, i);
					}

					bNonMinedOrWallUpDown = isCellBlockingLight(cellaux);
					//checkSetLights (bNonMinedOrWallUpDown, cellaux, imiSource, true, iPaintWalls);
					cellaux.setLights(imiSource);

					if (bNonMinedOrWallUpDown) {
						break;
					}
				}

				if (bNonMinedOrWall) {
					return;
				}

				error = error - deltay;
				if (error < 0) {
					y = y + ystep;
					error = error + deltax;
				}
			}
		} else {
			bNonMinedOrWall = false;
			for (int x = x0; x >= x1; x += inc) {
				if (steep) {
					cell = World.getCell(y, x, zLevel);
				} else {
					cell = World.getCell(x, y, zLevel);
				}

				bNonMinedOrWall = isCellBlockingLight(cell);
				checkSetLights(bNonMinedOrWall, cell, imiSource, iDirection);

				// Arriba y abajo, teniendo en cuenta el radio
				// Arriba
				iZAux = zLevel - iRadious;
				if (iZAux < 0) {
					iZAux = 0;
				}

				bNonMinedOrWallUpDown = false;
				for (int i = (zLevel - 1); i >= iZAux; i--) {
					if (steep) {
						cellaux = World.getCell(y, x, i);
					} else {
						cellaux = World.getCell(x, y, i);
					}

					bNonMinedOrWallUpDown = isCellBlockingLight(cellaux);
					bNonMinedOrWallUpDown = checkSetLightsUp(bNonMinedOrWallUpDown, cellaux, imiSource, iDirection);

					if (!bNonMinedOrWallUpDown) {
						break;
					}
				}

				// Abajo
				iZAux = zLevel + iRadious;
				if (iZAux >= World.MAP_DEPTH) {
					iZAux = (World.MAP_DEPTH - 1);
				}

				bNonMinedOrWallUpDown = false;
				for (int i = (zLevel + 1); i <= iZAux; i++) {
					if (steep) {
						cellaux = World.getCell(y, x, i);
					} else {
						cellaux = World.getCell(x, y, i);
					}

					bNonMinedOrWallUpDown = isCellBlockingLight(cellaux);
//					checkSetLights (bNonMinedOrWallUpDown, cellaux, imiSource, true, iPaintWalls);
					cellaux.setLights(imiSource);

					if (bNonMinedOrWallUpDown) {
						break;
					}
				}

				if (bNonMinedOrWall) {
					return;
				}

				error = error - deltay;
				if (error < 0) {
					y = y + ystep;
					error = error + deltax;
				}
			}
		}
	}

	public static int getDistance(Point3DShort p3dSource, Point3DShort p3dDestination) {
		return getDistance(p3dSource.x, p3dSource.y, p3dSource.z, p3dDestination.x, p3dDestination.y, p3dDestination.z);
	}

	public static int getDistance(Point3DShort p3dSource, int xd, int yd, int zd) {
		return getDistance(p3dSource.x, p3dSource.y, p3dSource.z, xd, yd, zd);
	}

	public static int getDistance(int xs, int ys, int zs, Point3DShort p3dDestination) {
		return getDistance(xs, ys, zs, p3dDestination.x, p3dDestination.y, p3dDestination.z);
	}

	public static int getDistance(int xs, int ys, int zs, int xd, int yd, int zd) {
		return Math.abs(xs - xd) + Math.abs(ys - yd) + (Math.abs(zs - zd) * (32));
//		int h = Math.max (Math.abs (xd - xs), Math.abs (yd - ys));
//		if (zs >= (World.MAP_NUM_LEVELS_OUTSIDE - 1)) {
//			if (zd >= (World.MAP_NUM_LEVELS_OUTSIDE - 1)) {
//				// Los 2 puntos son underground
//				h += ((World.MAP_WIDTH / 4) * Math.abs (zd - zs)); // Método Manhattan, funciona mejor incluso permitiendo movimiento diagonal que la "Distancia Chebyshev" (20% mejor)
//			} else {
//				// El origen es underground pero el destino no
//				h += ((World.MAP_WIDTH / 4) * Math.abs (zs - (World.MAP_NUM_LEVELS_OUTSIDE - 1)) + 2 * (World.MAP_NUM_LEVELS_OUTSIDE - 1 - zd)); // Método Manhattan, funciona mejor incluso permitiendo movimiento diagonal que la "Distancia Chebyshev" (20% mejor)
//			}
//		} else {
//			// El origen es outside
//			if (zd >= (World.MAP_NUM_LEVELS_OUTSIDE - 1)) {
//				// El destino es underground (y origen outside)
//				h += ((World.MAP_WIDTH / 4) * Math.abs (zd - (World.MAP_NUM_LEVELS_OUTSIDE - 1)) + 2 * (World.MAP_NUM_LEVELS_OUTSIDE - 1 - zs)); // Método Manhattan, funciona mejor incluso permitiendo movimiento diagonal que la "Distancia Chebyshev" (20% mejor)
//			} else {
//				// El destino es outside, como el origen
//				h += (2 * Math.abs (zd - zs)); // Método Manhattan, funciona mejor incluso permitiendo movimiento diagonal que la "Distancia Chebyshev" (20% mejor)
//			}
//		}
//
//		return h;
	}

	// ****************
	// * NUMBER UTILS *
	// ****************
	/**
	 * Returns a number from a String
	 *
	 * @param sNumber
	 * @param defaultNumber
	 * @return
	 */
	public static int getInteger(String sNumber, int defaultNumber) {
		if (sNumber == null || sNumber.trim().length() == 0) {
			return defaultNumber;
		}

		try {
			return Integer.parseInt(sNumber);
		} catch (NumberFormatException nfe) {
			Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.1") + sNumber + Messages.getString("Utils.2") + defaultNumber + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		return defaultNumber;
	}

	/**
	 * Fast integer SQRT
	 *
	 * @param number
	 * @return
	 */
	public static int sqrt(int number) {
		if (number >= 1 && number < 4) {
			return 1;
		}
		if (number >= 4 && number < 9) {
			return 2;
		}
		if (number >= 9 && number < 16) {
			return 3;
		}
		if (number >= 16 && number < 25) {
			return 4;
		}
		if (number >= 25 && number < 36) {
			return 5;
		}
		if (number >= 36 && number < 49) {
			return 6;
		}
		if (number >= 49 && number < 64) {
			return 7;
		}
		if (number >= 64 && number < 81) {
			return 8;
		}
		if (number >= 81 && number < 100) {
			return 9;
		}
		if (number >= 100 && number < 121) {
			return 10;
		}
		if (number >= 121 && number < 144) {
			return 11;
		}
		if (number >= 144 && number < 169) {
			return 12;
		}
		if (number >= 169 && number < 196) {
			return 13;
		}
		if (number >= 196 && number < 225) {
			return 14;
		}
		if (number >= 225) {
			return 15;
		}

		return 0;
	}

	// **************
	// * DICE UTILS *
	// **************
	/**
	 * Returns a random number between the 2 params
	 *
	 * @param iMin Min
	 * @param iMax Max
	 * @return a random number between the 2 params. If iMin > iMax then return
	 * iMin
	 */
	public static int getRandomBetween(int iMin, int iMax) {
		if (iMin >= iMax) {
			return iMin;
		}

		return random.nextInt(iMax - iMin + 1) + iMin;
	}

	/**
	 * Launch a dice of N sides X times and return the result
	 *
	 * @param iNumber Number of times the dice was launched
	 * @param iSides Number of sides of the dice
	 * @return
	 */
	public static int launchDice(int iNumber, int iSides) {
		return launchDice(iNumber, iSides, 0);
	}

	/**
	 * Launch a dice of N sides X times and then adds a qtty
	 *
	 * @param iNumber Number of times the dice was launched
	 * @param iSides Number of sides of the dice
	 * @param add Number to add (or substract if negative) to the end result
	 * @return
	 */
	public static int launchDice(int iNumber, int iSides, int add) {
		int result = add;

		for (int i = 0; i < iNumber; i++) {
			result += getRandomBetween(1, iSides);
		}

		return result;
	}

	/**
	 * Launch N dices taking a String as an input ex: 4d8+34 (this wil launch 4
	 * times a dice of 8 sides, then addes 34 to the result
	 *
	 * Input string can be passed with commas ',' Ex: 2d8,3d6+1,1d3 (lauch 2
	 * dices of 8 sides, then 3 dices of 6 sides, then adds 1, then launch 1
	 * dice of 3 sides
	 *
	 * @param str
	 * @return
	 */
	public static int launchDice(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0;
		}

		int result = 0;
		String sStr = str.toUpperCase();

		if (sStr.indexOf(',') == -1) {
			// No tokens, just 1 string
			int iIndexD = sStr.indexOf('D');
			if (iIndexD != -1) {
				try {
					int iNumber = Integer.parseInt(sStr.substring(0, iIndexD).trim());
					// Buscamos si hay un "+"
					int iIndexPlus = sStr.indexOf('+');
					if (iIndexPlus == -1) {
						// No hay '+', miramos si hay un "-"
						iIndexPlus = sStr.indexOf('-');
						if (iIndexPlus == -1) {
							return launchDice(iNumber, Integer.parseInt(sStr.substring(iIndexD + 1).trim()));
						} else {
							return launchDice(iNumber, Integer.parseInt(sStr.substring(iIndexD + 1, (iIndexPlus + 1 - iIndexD)).trim()), -Integer.parseInt(sStr.substring(iIndexPlus + 1)));
						}
					} else {
						return launchDice(iNumber, Integer.parseInt(sStr.substring(iIndexD + 1, (iIndexPlus + 1 - iIndexD)).trim()), Integer.parseInt(sStr.substring(iIndexPlus + 1)));
					}
				} catch (Exception e) {
					Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.0") + sStr + "] [" + e.toString() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					return 0;
				}
			} else {
				try {
					// No existe la 'D', quizá es un +X o -X directo
					if (sStr.charAt(0) == '-') {
						return launchDice(0, 0, -Integer.parseInt(sStr.substring(1)));
					} else {
						if (sStr.charAt(0) == '+') {
							return launchDice(0, 0, Integer.parseInt(sStr.substring(1)));
						} else {
							return launchDice(0, 0, Integer.parseInt(sStr.substring(0)));
						}
					}
				} catch (Exception e) {
					Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.0") + sStr + "] [" + e.toString() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					return 0;
				}
			}
		} else {
			// Recorremos los tokens y vamos sumando, llamándose a si misma
			StringTokenizer tokenizer = new StringTokenizer(sStr, ","); //$NON-NLS-1$
			String token;
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				result += launchDice(token);
			}
		}

		return result;
	}

	/**
	 * Returns the minimum and maximum values of a dice String
	 *
	 * @param str
	 * @return the minimum and maximum values of a dice String
	 */
	public static Point getDiceMinMax(String str) {
		if (str == null || str.trim().length() == 0) {
			return new Point(0, 0);
		}

		Point result = new Point(0, 0);
		String sStr = str.toUpperCase();

		if (sStr.indexOf(',') == -1) {
			// No tokens, just 1 string
			int iIndexD = sStr.indexOf('D');
			if (iIndexD != -1) {
				try {
					int iNumber = Integer.parseInt(sStr.substring(0, iIndexD).trim());
					// Buscamos si hay un "+"
					int iIndexPlus = sStr.indexOf('+');
					if (iIndexPlus == -1) {
						// No hay '+', miramos si hay un "-"
						iIndexPlus = sStr.indexOf('-');
						if (iIndexPlus == -1) {
							return new Point(iNumber, iNumber * Integer.parseInt(sStr.substring(iIndexD + 1).trim()));
						} else {
							int iMinus = Integer.parseInt(sStr.substring(iIndexPlus + 1));
							return new Point(iNumber - iMinus, (iNumber * Integer.parseInt(sStr.substring(iIndexD + 1, (iIndexPlus + 1 - iIndexD)).trim())) - iMinus);
						}
					} else {
						int iAdd = Integer.parseInt(sStr.substring(iIndexPlus + 1));
						return new Point(iNumber + iAdd, (iNumber * Integer.parseInt(sStr.substring(iIndexD + 1, (iIndexPlus + 1 - iIndexD)).trim())) + iAdd);
					}
				} catch (Exception e) {
					return new Point(0, 0);
				}
			} else {
				try {
					// No existe la 'D', quizá es un +X o -X directo
					if (sStr.charAt(0) == '-') {
						int iMinus = -Integer.parseInt(sStr.substring(1));
						return new Point(iMinus, iMinus);
					} else {
						if (sStr.charAt(0) == '+') {
							int iAdd = Integer.parseInt(sStr.substring(1));
							return new Point(iAdd, iAdd);
						} else {
							int iAdd = Integer.parseInt(sStr.substring(0));
							return new Point(iAdd, iAdd);
						}
					}
				} catch (Exception e) {
					Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.3") + sStr + "] [" + e.toString() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					return new Point(0, 0);
				}
			}
		} else {
			// Recorremos los tokens y vamos sumando, llamándose a si misma
			StringTokenizer tokenizer = new StringTokenizer(sStr, ","); //$NON-NLS-1$
			String token;
			Point pointTmp;
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				pointTmp = getDiceMinMax(token);
				result.x += pointTmp.x;
				result.y += pointTmp.y;
			}
		}

		return result;
	}

	// ***************
	// * COLOR UTILS *
	// ***************
	/**
	 * Returns a java.awt.Color from a String
	 *
	 * @param sColor Color string (ex: Green or GREEN)
	 * @return a java.awt.Color from a String. Color.BLACK if parameter is null
	 * or empty
	 */
	public static ColorGL getColorFromString(String sColor) {
		if (sColor == null || sColor.length() == 0) {
			return new ColorGL(null);
		}

		sColor = sColor.toUpperCase();
		if (sColor.equals("GREEN")) { //$NON-NLS-1$
			return new ColorGL(Color.GREEN);
		} else if (sColor.equals("DARK_GREEN")) { //$NON-NLS-1$
			return new ColorGL(Color.GREEN.darker());
		} else if (sColor.equals("BLACK")) { //$NON-NLS-1$
			return new ColorGL(Color.BLACK);
		} else if (sColor.equals("BLUE")) { //$NON-NLS-1$
			return new ColorGL(Color.BLUE);
		} else if (sColor.equals("WHITE")) { //$NON-NLS-1$
			return new ColorGL(Color.WHITE);
		} else if (sColor.equals("ORANGE")) { //$NON-NLS-1$
			return new ColorGL(Color.ORANGE);
		} else if (sColor.equals("PINK")) { //$NON-NLS-1$
			return new ColorGL(Color.PINK);
		} else if (sColor.equals("YELLOW")) { //$NON-NLS-1$
			return new ColorGL(Color.YELLOW);
		} else if (sColor.equals("GRAY")) { //$NON-NLS-1$
			return new ColorGL(Color.GRAY);
		} else if (sColor.equals("LIGHT_GRAY")) { //$NON-NLS-1$
			return new ColorGL(Color.LIGHT_GRAY);
		} else if (sColor.equals("DARK_GRAY")) { //$NON-NLS-1$
			return new ColorGL(Color.DARK_GRAY);
		} else if (sColor.equals("RED")) { //$NON-NLS-1$
			return new ColorGL(Color.RED);
		} else if (sColor.equals("BROWN")) { //$NON-NLS-1$
			return new ColorGL(new Color(0.5f, 0.25f, 0f));
		} else {
			// Sacamos los 3 colores
			int r = 0, g = 0, b = 0;
			boolean coloresOK = false;
			try {
				StringTokenizer tokenizer = new StringTokenizer(sColor, ","); //$NON-NLS-1$
				if (tokenizer.hasMoreTokens()) {
					r = Integer.parseInt(tokenizer.nextToken());
					if (tokenizer.hasMoreTokens()) {
						g = Integer.parseInt(tokenizer.nextToken());
						if (tokenizer.hasMoreTokens()) {
							b = Integer.parseInt(tokenizer.nextToken());
							coloresOK = true;
						}
					}
				}

				if (coloresOK) {
					return new ColorGL(new Color(r, g, b));
				} else {
					Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.6") + sColor + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return new ColorGL(null);
				}
			} catch (Exception e) {
				Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.6") + sColor + "] [" + e.toString() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				return new ColorGL(null);
			}
		}
	}

	// **************
	// * DISK UTILS *
	// **************
	/**
	 * Save the game
	 */
	public static void save (boolean bSaveMissionData) throws Exception {
		// Pausamos el thread de pathfinding
		AStarQueue.pause();
		while (!AStarQueue.isPauseOK()) {
			try {
				// Espera X milisegundos
				Thread.sleep(256);
			} catch (InterruptedException e) {
			}
		}

		Game.SAVE_MISSION = bSaveMissionData;

		String sSaveFolder = Game.getUserFolder() + Game.getFileSeparator() + Game.SAVE_FOLDER1 + Game.getFileSeparator();
		String sDestFileName = Game.getSavegameName() + ".zip"; //$NON-NLS-1$

		String sTemporaryFileName = Long.toString(System.currentTimeMillis()) + ".zip"; //$NON-NLS-1$
		File fTmp = new File(sSaveFolder + sTemporaryFileName);
		while (fTmp.exists()) {
			sTemporaryFileName = Long.toString(System.currentTimeMillis()) + ".zip"; //$NON-NLS-1$
			fTmp = new File(sSaveFolder + sTemporaryFileName);
		}
		try {
			Game.iError = 12000;
			// Create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(sSaveFolder + sTemporaryFileName));
			Game.iError = 12001;

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			Game.iError = 12002;

			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry("world1.twn")); //$NON-NLS-1$
			Game.iError = 12003;

			// Version
			oos.writeInt(Game.SAVEGAME_VERSION);

			//POPOoos.writeObject (Game.getWorld ());
			Game.getWorld().writeExternal(oos);
			Game.iError = 12004;

			// Depth
			int iDepth = World.cells[0][0].length;
			Game.iError = 12005;
			oos.writeInt(iDepth);

			// Guardamos en el .zip
			oos.flush();
			baos.flush();
			out.write(baos.toByteArray());
			out.closeEntry();

			// Celdas
			Game.iError = 12006;
			for (int x = 0; x < World.MAP_WIDTH; x++) {
				oos.close();
				baos.close();
				Game.iError = 13000 + (x * 100);
				baos = new ByteArrayOutputStream();
				Game.iError = Game.iError + 1;
				oos = new ObjectOutputStream(baos);
				Game.iError = Game.iError + 1;
				out.putNextEntry(new ZipEntry("row" + x)); //$NON-NLS-1$
				Game.iError = Game.iError + 1;
				//POPOoos.writeObject (World.cells [x]);
				for (int y = 0; y < World.MAP_HEIGHT; y++) {
					for (int z = 0; z < iDepth; z++) {
						World.cells[x][y][z].writeExternal(oos);
					}
				}
				oos.flush();
				Game.iError = Game.iError + 1;
				out.flush();
				Game.iError = Game.iError + 1;
				baos.flush();
				Game.iError = Game.iError + 1;
				out.write(baos.toByteArray());
				Game.iError = Game.iError + 1;
				out.closeEntry();
				Game.iError = Game.iError + 1;
			}

			// El resto
			oos.close();
			baos.close();
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			out.putNextEntry(new ZipEntry("world2.twn")); //$NON-NLS-1$
			oos.writeInt(World.getCurrentEntityID());
			Game.iError = 12007;

			// Town value
			oos.writeInt(World.getTownValue());
			Game.iError = 12008;

			// DEPTHS
			oos.writeShort(World.MAP_DEPTH);
			Game.iError = 12009;
			oos.writeShort(World.MAP_NUM_LEVELS_OUTSIDE);
			Game.iError = 12010;
			oos.writeShort(World.MAP_NUM_LEVELS_UNDERGROUND);
			Game.iError = 12011;

			// Mensajes
			MessagesPanel.writeExternal(oos);

			// Priorities panel
			ActionPriorityManager.save(oos);
			Game.iError = 12017;

			// Pausa
			oos.writeBoolean(Game.isPaused());

			// Mission data
			oos.writeObject (Game.getCurrentMissionData ());

			Game.iError = 12018;
			oos.flush();
			Game.iError = 12019;
			baos.flush();
			Game.iError = 12020;

			// Guardamos en el .zip
			out.write(baos.toByteArray());
			Game.iError = 12021;

			// Complete the entry
			out.closeEntry();
			Game.iError = 12022;

			// Complete the ZIP file
			out.flush();
			out.close();
			Game.iError = 12032;
			oos.close();
			Game.iError = 12033;

			// All ok, copy to the real file
			File fFinal = new File(sSaveFolder + sDestFileName);
			if (fFinal.exists()) {
				fFinal.delete();
			}
			File fTemporary = new File(sSaveFolder + sTemporaryFileName);
			if (fTemporary.exists()) { // Debería, siempre
				fTemporary.renameTo(fFinal);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Game.SAVE_MISSION = true;
			AStarQueue.resume();
		}
	}

	/**
	 * Load a game
	 *
	 * @param sFolder
	 * @param sZipFilename
	 */
	public static void load(String sFolder, String sZipFilename) throws Exception {
		try {
			// Lo descomprimimos
			ZipFile zipFile = new ZipFile(new File(sFolder + sZipFilename));
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(new File(sFolder + sZipFilename))));
			ZipEntry zipEntry = zis.getNextEntry();
			InputStream is = zipFile.getInputStream(zipEntry);

			// Cargamos
			ObjectInputStream ois = new ObjectInputStream(is);
			Game.SAVEGAME_LOADING_VERSION = ois.readInt();
			if (TownsProperties.DEBUG_MODE) {
				System.out.println ("Loading version: " + Game.SAVEGAME_LOADING_VERSION); //$NON-NLS-1$
			}

			World world = new World();
			world.readExternal(ois);
			Game.setWorld(world);
			//POPOGame.setWorld ((World) ois.readObject ());

			// Celdas
			int iDepth = ois.readInt();
			World.cells = new Cell[World.MAP_WIDTH][World.MAP_HEIGHT][iDepth];

			ois.close();
			is.close();

			// Celdas
			for (int x = 0; x < World.MAP_WIDTH; x++) {
				zipEntry = zis.getNextEntry();
				is = zipFile.getInputStream(zipEntry);
				ois = new ObjectInputStream(is);
				//POPOWorld.cells [x] = (Cell [][]) ois.readObject ();
				for (int y = 0; y < World.MAP_HEIGHT; y++) {
					for (int z = 0; z < iDepth; z++) {
						World.cells[x][y][z] = new Cell();
						World.cells[x][y][z].readExternal(ois);
					}
				}
				ois.close();
				is.close();
			}

			zipEntry = zis.getNextEntry();
			is = zipFile.getInputStream(zipEntry);
			ois = new ObjectInputStream(is);

			// El resto
			World.setCurrentEntityID(ois.readInt());

			// Town value
			World.setTownValue(ois.readInt());

			// DEPTHS
			World.MAP_DEPTH = ois.readShort();
			World.MAP_NUM_LEVELS_OUTSIDE = ois.readShort();
			World.MAP_NUM_LEVELS_UNDERGROUND = ois.readShort();

			if (Game.SAVEGAME_LOADING_VERSION < Game.SAVEGAME_V12) {
				world.setRestrictHaulEquippingLevel(World.MAP_DEPTH - 1);
				world.setRestrictExploringLevel(World.MAP_DEPTH - 1);
			}

			// Mensajes
			MessagesPanel.clear();
			MessagesPanel.readExternal(ois);

			// Priorities panel
			ActionPriorityManager.load(ois);

			// Pause
			Game.setPaused(ois.readBoolean());

			// SavegameName
			Game.setSavegameName(removeExtension(sZipFilename));

			// Mission data
			if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V14d) {
				Game.setCurrentMissionData ((MissionData) ois.readObject ());
			}

			ois.close();
			is.close();
			zis.close();
			zipFile.close();

			// Comprobamos que items y livings y types de containers y pilas
			Item item;
			ArrayList<LivingEntity> alLivings;
			for (short x = 0; x < World.MAP_WIDTH; x++) {
				for (short y = 0; y < World.MAP_HEIGHT; y++) {
					for (short z = 0; z < World.MAP_DEPTH; z++) {
						// Items
						item = World.getCell(x, y, z).getItem();
						if (item != null) {
							// Miramos que exista
							if (ItemManager.getItem(item.getIniHeader()) == null) {
								throw new Exception(Messages.getString("Item.10") + " [" + item.getIniHeader() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							}
						}

						// Livings
						alLivings = World.getCell(x, y, z).getLivings();
						if (alLivings != null) {
							for (int i = 0; i < alLivings.size(); i++) {
								if (LivingEntityManager.getItem(alLivings.get(i).getIniHeader()) == null) {
									throw new Exception(Messages.getString("LivingEntity.10") + " [" + alLivings.get(i).getIniHeader() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								}
							}
						}
					}
				}
			}

			// Containers y pilas
			ArrayList<Container> alContainers = Game.getWorld().getContainers();
			ArrayList<Item> alContainerItems;
			Container container;
			for (int i = 0; i < alContainers.size(); i++) {
				container = alContainers.get(i);

				int t = container.getType().getElements().size() - 1;
				while (t >= 0) {
					if (ItemManager.getItem(container.getType().getElements().get(t)) == null) {
						container.getType().removeElement(container.getType().getElements().get(t));
					}

					t--;
				}

				alContainerItems = container.getItemsInside();
				for (int j = 0; j < alContainerItems.size(); j++) {
					item = alContainerItems.get(j);
					if (item != null) {
						if (ItemManager.getItem(item.getIniHeader()) == null) {
							throw new Exception(Messages.getString("Utils.21") + " [" + item.getIniHeader() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
				}
			}

			ArrayList<Stockpile> alPiles = Game.getWorld().getStockpiles();
			Stockpile pile;
			for (int i = 0; i < alPiles.size(); i++) {
				pile = alPiles.get(i);

				int t = pile.getType().getElements().size() - 1;
				while (t >= 0) {
					if (ItemManager.getItem(pile.getType().getElements().get(t)) == null) {
						pile.getType().removeElement(pile.getType().getElements().get(t));
					}

					t--;
				}
			}

			Game.getWorld().refreshTransients();
		} catch (Exception e) {
			e.printStackTrace();
			Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.23") + e.toString() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (e.getMessage() != null) {
				throw new Exception(Messages.getString("Utils.23") + e.getMessage() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				throw new Exception(Messages.getString("Utils.23") + e.toString() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * Bury and save a town
	 */
	public static void saveBury() throws Exception {
		// Pausamos el thread de pathfinding
		AStarQueue.pause();
		while (!AStarQueue.isPauseOK()) {
			try {
				// Espera X milisegundos
				Thread.sleep(256);
			} catch (InterruptedException e) {
			}
		}

		String sBuryFolder = Game.getUserFolder() + Game.getFileSeparator() + Game.BURY_FOLDER1 + Game.getFileSeparator();
		String sDestFileName = "b_" + Game.getSavegameName() + ".zip"; //$NON-NLS-1$ //$NON-NLS-2$

		String sTemporaryFileName = Long.toString(System.currentTimeMillis()) + ".zip"; //$NON-NLS-1$
		File fTmp = new File(sBuryFolder + sTemporaryFileName);
		while (fTmp.exists()) {
			sTemporaryFileName = Long.toString(System.currentTimeMillis()) + ".zip"; //$NON-NLS-1$
			fTmp = new File(sBuryFolder + sTemporaryFileName);
		}

		try {
			Game.iError = 22000;
			// Create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(sBuryFolder + sTemporaryFileName));
			Game.iError = 22001;

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			Game.iError = 22002;

			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry("bury.dat")); //$NON-NLS-1$
			Game.iError = 22003;

			// Data
			BuryData buryData = BuryData.generate(sDestFileName);
			buryData.writeExternal(oos);

			// Guardamos en el .zip
			oos.flush();
			baos.flush();
			out.write(baos.toByteArray());
			out.closeEntry();

			// Complete the ZIP file
			out.close();
			Game.iError = 22032;
			oos.close();
			Game.iError = 22033;

			// All ok, copy to the real file
			File fFinal = new File(sBuryFolder + sDestFileName);
			if (fFinal.exists()) {
				fFinal.delete();
			}
			File fTemporary = new File(sBuryFolder + sTemporaryFileName);
			if (fTemporary.exists()) { // Debería, siempre
				fTemporary.renameTo(fFinal);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			AStarQueue.resume();
		}
	}

	/**
	 * Devuelve un random bury, nunca devuelve null, en todo caso un bury vacío
	 *
	 * @return
	 */
	public static BuryData getRandomBuryData(String sServerName) {
		BuryData bd = new BuryData();
		String sBuryFolder = Game.getUserFolder() + Game.getFileSeparator() + Game.BURY_FOLDER1 + Game.getFileSeparator();
		File fBuryFolder = new File(sBuryFolder);
		if (!fBuryFolder.exists()) {
			return bd;
		}

		// Si le pasamos un servidor nos bajaremos un bury y lo cargaremos
		String sBuryFile = null;
		if (sServerName != null) {
			sBuryFile = UtilsServer.getBuriedTown(sServerName, sBuryFolder);
		}

		// Buscamos un .zip a random
		try {
			File fBuryZip = null;
			if (sBuryFile == null) {
				String[] alFiles = fBuryFolder.list();
				if (alFiles != null && alFiles.length > 0) {
					sBuryFile = alFiles[getRandomBetween(0, (alFiles.length - 1))];
					fBuryZip = new File(sBuryFolder + sBuryFile);
				}
			} else {
				// Fichero fijo, bajado del servidor
				fBuryZip = new File(sBuryFolder + sBuryFile);
			}

			if (fBuryZip != null && fBuryZip.exists()) {
				// Lo descomprimimos
				ZipFile zipFile = new ZipFile(fBuryZip);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(fBuryZip)));
				ZipEntry zipEntry = zis.getNextEntry();
				InputStream is = zipFile.getInputStream(zipEntry);

				// Cargamos
				ObjectInputStream ois = new ObjectInputStream(is);
				bd.readExternal(ois);

				ois.close();
				is.close();
				zis.close();
				zipFile.close();
			} else {
				Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.17"), "Utils"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (Exception e) {
			Log.log(Log.LEVEL_ERROR, Messages.getString("BuryData.1") + " [" + e.toString() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		return bd;
	}

	public static String removeExtension(String sFilename) {
		if (sFilename == null || sFilename.length() == 0) {
			return sFilename;
		}

		int iIndex = sFilename.lastIndexOf("."); //$NON-NLS-1$
		if (iIndex > 0) {
			return sFilename.substring(0, iIndex);
		}
		return sFilename;
	}

	public static boolean existsSavegame(String sName) {
		try {
			String sSaveFolder = Game.getUserFolder() + Game.getFileSeparator() + Game.SAVE_FOLDER1 + Game.getFileSeparator();
			File fAux = new File(sSaveFolder + sName + ".zip"); //$NON-NLS-1$
			return fAux.exists();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Retorna una lista con todos los savegame names o null si no hay
	 *
	 * @return una lista con todos los savegame names o null si no hay
	 */
	public static ArrayList<File> getSaveFiles() {
		String sSaveFolder = Game.getUserFolder() + Game.getFileSeparator() + Game.SAVE_FOLDER1 + Game.getFileSeparator();
		File fFolder = new File(sSaveFolder);
		File[] aFiles = fFolder.listFiles();

		if (aFiles.length == 0) {
			return null;
		}

		ArrayList<File> alFiles = new ArrayList<File>(aFiles.length);
		for (int i = 0; i < aFiles.length; i++) {
			if (aFiles[i] != null && aFiles[i].getName() != null && aFiles[i].isFile() && aFiles[i].getName().endsWith(".zip")) { //$NON-NLS-1$
				// Bingo
				alFiles.add(aFiles[i]);
			}
		}

		if (alFiles.size() == 0) {
			return null;
		}

		// Ordenamos por fecha
		for (int i = 0; i < (alFiles.size() - 1); i++) {
			for (int j = i; j < alFiles.size(); j++) {
				if (alFiles.get(i).lastModified() < alFiles.get(j).lastModified()) {
					// Intercambiamos
					File fAux = alFiles.get(i);
					alFiles.set(i, alFiles.get(j));
					alFiles.set(j, fAux);
				}
			}
		}

		return alFiles;
	}

	public static void deleteSavegame(String sName) {
		String sSaveFolder = Game.getUserFolder() + Game.getFileSeparator() + Game.SAVE_FOLDER1 + Game.getFileSeparator();
		File f = new File(sSaveFolder + sName);
		if (f.exists()) {
			f.delete();
		}
	}

	/**
	 * Retorna una lista con todos los savegame names o null si no hay
	 *
	 * @return una lista con todos los savegame names o null si no hay
	 */
	public static ArrayList<File> getModsFolders() {
		String sModsFolder = Game.getUserFolder() + Game.getFileSeparator() + Game.MODS_FOLDER1 + Game.getFileSeparator();
		File fFolder = new File(sModsFolder);
		File[] aFiles = fFolder.listFiles();

		if (aFiles.length == 0) {
			return null;
		}

		ArrayList<File> alFiles = new ArrayList<File>(aFiles.length);
		for (int i = 0; i < aFiles.length; i++) {
			if (aFiles[i] != null && aFiles[i].getName() != null && aFiles[i].isDirectory()) {
				// Bingo
				alFiles.add(aFiles[i]);
			}
		}

		if (alFiles.size() == 0) {
			return null;
		}

		// Ordenamos por nombre
		for (int i = 0; i < (alFiles.size() - 1); i++) {
			for (int j = i; j < alFiles.size(); j++) {
				if (alFiles.get(i).getName().compareTo(alFiles.get(j).getName()) > 0) {
					// Intercambiamos
					File fAux = alFiles.get(i);
					alFiles.set(i, alFiles.get(j));
					alFiles.set(j, fAux);
				}
			}
		}

		return alFiles;
	}

	/**
	 * Retorna la ruta entera a un fichero, teniendo en cuenta si está en una
	 * misión o no. Tambien mira los mods cargados. Si es misión, mirará primero
	 * la carpeta general y después dentro de las carpetas de campaña
	 *
	 * @param sOriginalFile
	 * @param sCampaignID
	 * @param sMissionID
	 * @return la ruta entera a un fichero, teniendo en cuenta si está en una
	 * misión o no
	 */
	public static ArrayList<String> getPathToFile(String sOriginalFile, String sCampaignID, String sMissionID) {
		ArrayList<String> alReturn = new ArrayList<String>();

		if (sMissionID == null || sMissionID.trim().length() == 0) {
			// Sin misión, lo pillamos de la carpeta data
			String sPath = Towns.getPropertiesString("DATA_FOLDER") + sOriginalFile; //$NON-NLS-1$
			File f = new File(sPath);
			if (f.exists()) {
				alReturn.add(sPath);
			}
		} else {
			// Misión
			// Primero miramos la carpeta data
			String sPath = Towns.getPropertiesString("DATA_FOLDER") + sOriginalFile; //$NON-NLS-1$
			File f = new File(sPath);
			if (f.exists()) {
				alReturn.add(sPath);
			}

			// Después la carpeta de campaña
			sPath = Towns.getPropertiesString("CAMPAIGNS_FOLDER") + sCampaignID + File.separator + sOriginalFile; //$NON-NLS-1$
			f = new File(sPath);
			if (f.exists()) {
				alReturn.add(sPath);
			}

			// Ahora la de campaña + misión
			sPath = Towns.getPropertiesString("CAMPAIGNS_FOLDER") + sCampaignID + File.separator + sMissionID + File.separator + sOriginalFile; //$NON-NLS-1$
			f = new File(sPath);
			if (f.exists()) {
				alReturn.add(sPath);
			}
		}

		// Ahora cargamos los mods
		getPathToFileMods(alReturn, sOriginalFile, sCampaignID, sMissionID);

		return alReturn;
	}

	/**
	 * Retorna la ruta entera a un fichero de mods, teniendo en cuenta si está
	 * en una misión o no Si es misión, mirará primero la carpeta general y
	 * después dentro de las carpetas de campaña
	 *
	 * @param sOriginalFile
	 * @param sCampaignID
	 * @param sMissionID
	 * @return la ruta entera a un fichero, teniendo en cuenta si está en una
	 * misión o no
	 */
	private static void getPathToFileMods(ArrayList<String> alList, String sOriginalFile, String sCampaignID, String sMissionID) {
		File fUserFolder = new File(Game.getUserFolder());
		if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
			return;
		}

		// Mods
		ArrayList<String> alMods = Game.getModsLoaded();
		if (alMods == null || alMods.size() == 0) {
			return;
		}

		String sModName;
		for (int i = 0; i < alMods.size(); i++) {
			sModName = alMods.get(i);

			if (sMissionID == null || sMissionID.trim().length() == 0) {
				// Sin misión, lo pillamos de la carpeta data
				String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + sModName + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + sOriginalFile; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				File f = new File(sModActionsPath);
				if (f.exists()) {
					alList.add(sModActionsPath);
				}
			} else {
				// Primero miramos la carpeta del mod
				String sPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + sModName + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + sOriginalFile; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				if (new File(sPath).exists()) {
					alList.add(sPath);
				}

				// Ahora miramos la carpeta de la campaña
				sPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + sModName + System.getProperty("file.separator") + Towns.getPropertiesString("CAMPAIGNS_FOLDER") + sCampaignID + File.separator + sOriginalFile; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				if (new File(sPath).exists()) {
					alList.add(sPath);
				}

				// Ahora la carpeta de campa+a+misión
				sPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + sModName + System.getProperty("file.separator") + Towns.getPropertiesString("CAMPAIGNS_FOLDER") + sCampaignID + File.separator + sMissionID + File.separator + sOriginalFile; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				if (new File(sPath).exists()) {
					alList.add(sPath);
				}
			}
		}
	}

	/**
	 * Save the user options to the user folder
	 */
	public static void saveOptions() {
		File fSave = new File(Game.getUserFolder(), "towns.ini");
		if (fSave.exists()) {
			fSave.delete();
		}

		try {
			PropertiesWriter pw = new PropertiesWriter(PropertyFile.PROPERTY_FILE_MAIN);
			pw.addSection("General Settings");
			pw.setProperty(MainProperties.WINDOW_WIDTH, UtilsGL.getLastWindowWidth());
			pw.setProperty(MainProperties.WINDOW_HEIGHT, UtilsGL.getLastWindowHeight());
			pw.setProperty(MainProperties.FULLSCREEN, UtilsGL.isFullScreen());
			pw.setProperty(MainProperties.MUSIC, Game.isMusicON());
			pw.setProperty(MainProperties.VOLUME_MUSIC, Game.getVolumeMusic());
			pw.setProperty(MainProperties.FX, Game.isFXON());
			pw.setProperty(MainProperties.VOLUME_FX, Game.getVolumeFX());
			pw.setProperty(MainProperties.MOUSE_SCROLL, Game.isMouseScrollON());
			pw.setProperty(MainProperties.MOUSE_SCROLL_EARS, Game.isMouseScrollEarsON());
			pw.setProperty(MainProperties.MOUSE_2D_CUBES, Game.isMouse2DCubesON());
			pw.setProperty(MainProperties.ZOOM_ON_CURSOR, MainPanel.isZoomOnCursor());
			pw.setProperty(MainProperties.DISABLED_ITEMS, Game.isDisabledItemsON());
			pw.setProperty(MainProperties.DISABLED_GODS, Game.isDisabledGodsON());
			pw.setProperty(MainProperties.PAUSE_START, Game.isPauseStartON());
			pw.setProperty(MainProperties.AUTOSAVE_DAYS, Game.getAutosaveDays());
			pw.setProperty(MainProperties.SIEGES, Game.getSiegeDifficulty());
			pw.setProperty(MainProperties.SIEGE_PAUSE, Game.isSiegePause());
			pw.setProperty(MainProperties.CARAVAN_PAUSE, Game.isCaravanPause());
			pw.setProperty(MainProperties.ALLOW_BURY, Game.isAllowBury());
			pw.setProperty(MainProperties.PATHFINDING_LEVEL, Game.getPathfindingCPULevel());
			UtilsKeyboard.saveShortcuts(pw);

			pw.addSection("Mod/Servers");
			pw.setProperty(MainProperties.MODS, Game.getModsLoadedString());
			pw.setProperty(MainProperties.SERVERS, Game.getServersString());
			pw.store(fSave);
		} catch (Exception e) {
			Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.13") + e.toString() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public static File createUserFolder(String sUserFolder) {
		File fUserFolder = new File(sUserFolder);
		if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
			Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.10") + fUserFolder.getAbsolutePath() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return null;
		}

		File fFolderTowns = new File(fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + ".towns"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!fFolderTowns.exists()) {
			fFolderTowns.mkdir();
		}

		if (!fFolderTowns.exists()) {
			Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.20") + " [" + fFolderTowns.getAbsolutePath() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return null;
		}

		File fFolderSave = new File(fFolderTowns.getAbsolutePath() + System.getProperty("file.separator") + Game.SAVE_FOLDER1); //$NON-NLS-1$
		if (!fFolderSave.exists()) {
			fFolderSave.mkdir();
		}

		if (!fFolderSave.exists()) {
			Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.25") + " [" + fFolderSave.getAbsolutePath() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return null;
		}

		File fFolderMods = new File(fFolderTowns.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator")); //$NON-NLS-1$ //$NON-NLS-2$
		if (!fFolderMods.exists()) {
			fFolderMods.mkdir();
		}

		if (!fFolderMods.exists()) {
			Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.29") + " [" + fFolderMods.getAbsolutePath() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return null;
		}

		File fFolderBury = new File(fFolderTowns.getAbsolutePath() + System.getProperty("file.separator") + Game.BURY_FOLDER1 + System.getProperty("file.separator")); //$NON-NLS-1$ //$NON-NLS-2$
		if (!fFolderBury.exists()) {
			fFolderBury.mkdir();
		}

		if (!fFolderBury.exists()) {
			Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.29") + " [" + fFolderBury.getAbsolutePath() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return null;
		}

		File fFolderScreenshots = new File(fFolderTowns.getAbsolutePath() + System.getProperty("file.separator") + Game.SCREENSHOTS_FOLDER1 + System.getProperty("file.separator")); //$NON-NLS-1$ //$NON-NLS-2$
		if (!fFolderScreenshots.exists()) {
			fFolderScreenshots.mkdir();
		}

		if (!fFolderScreenshots.exists()) {
			Log.log(Log.LEVEL_ERROR, Messages.getString("Utils.18") + " [" + fFolderScreenshots.getAbsolutePath() + "]", "Utils"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return null;
		}

		return fFolderTowns;
	}

	// ***************
	// * STRING UTILS *
	// ***************
	/**
	 * Retorna una cadena haciendo cambios dinámicos (sustituyendo ciertas
	 * cadenas por otras)
	 */
	public static String getDynamicString(String sString) {
		String sAux;
		int iIndex = sString.indexOf("__MUSIC__"); //$NON-NLS-1$

		if (iIndex != -1) {
			// Música ON?
			String sMusicON = Game.isMusicON() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
			sAux = sString.substring(0, iIndex) + sMusicON + sString.substring(iIndex + "__MUSIC__".length()); //$NON-NLS-1$
			return getDynamicString(sAux);
		} else {
			iIndex = sString.indexOf("__FX__"); //$NON-NLS-1$
			if (iIndex != -1) {
				// FX ON?
				String sFXON = Game.isFXON() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
				sAux = sString.substring(0, iIndex) + sFXON + sString.substring(iIndex + "__FX__".length()); //$NON-NLS-1$
				return getDynamicString(sAux);
			} else {
				iIndex = sString.indexOf("__MOUSE_SCROLL__"); //$NON-NLS-1$
				if (iIndex != -1) {
					// Mouse scroll ON?
					String sMouseScrollON = Game.isMouseScrollON() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
					sAux = sString.substring(0, iIndex) + sMouseScrollON + sString.substring(iIndex + "__MOUSE_SCROLL__".length()); //$NON-NLS-1$
					return getDynamicString(sAux);
				} else {
					iIndex = sString.indexOf("__MOUSE_SCROLL_EARS__"); //$NON-NLS-1$
					if (iIndex != -1) {
						// Mouse scroll EARS ON?
						String sMouseScrollEarsON = Game.isMouseScrollEarsON() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
						sAux = sString.substring(0, iIndex) + sMouseScrollEarsON + sString.substring(iIndex + "__MOUSE_SCROLL_EARS__".length()); //$NON-NLS-1$
						return getDynamicString(sAux);
					} else {
						iIndex = sString.indexOf("__MOUSE_2D_CUBES__"); //$NON-NLS-1$
						if (iIndex != -1) {
							String sMouse2DCubes = Game.isMouse2DCubesON() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
							sAux = sString.substring(0, iIndex) + sMouse2DCubes + sString.substring(iIndex + "__MOUSE_2D_CUBES__".length()); //$NON-NLS-1$
							return getDynamicString(sAux);
						} else {
							iIndex = sString.indexOf("__DISABLE_ITEMS__"); //$NON-NLS-1$
							if (iIndex != -1) {
								// Disabled items ON?
								String sDisabledItemsON = Game.isDisabledItemsON() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
								sAux = sString.substring(0, iIndex) + sDisabledItemsON + sString.substring(iIndex + "__DISABLE_ITEMS__".length()); //$NON-NLS-1$
								return getDynamicString(sAux);
							} else {
								iIndex = sString.indexOf("__DISABLE_GODS__"); //$NON-NLS-1$
								if (iIndex != -1) {
									// Disabled gods ON?
									String sDisabledGodsON = Game.isDisabledGodsON() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
									sAux = sString.substring(0, iIndex) + sDisabledGodsON + sString.substring(iIndex + "__DISABLE_GODS__".length()); //$NON-NLS-1$
									return getDynamicString(sAux);
								} else {
									iIndex = sString.indexOf("__PAUSE_START__"); //$NON-NLS-1$
									if (iIndex != -1) {
										// Pause at start ON?
										String sPauseStartON = Game.isPauseStartON() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
										sAux = sString.substring(0, iIndex) + sPauseStartON + sString.substring(iIndex + "__PAUSE_START__".length()); //$NON-NLS-1$
										return getDynamicString(sAux);
									} else {
										iIndex = sString.indexOf("__SAVE_DAYS__"); //$NON-NLS-1$
										if (iIndex != -1) {
											int iDays = Game.getAutosaveDays();
											String sAutosave;
											if (iDays == 0) {
												sAutosave = Messages.getString("Utils.8"); //$NON-NLS-1$
											} else if (iDays == 1) {
												sAutosave = Messages.getString("Utils.7"); //$NON-NLS-1$
											} else {
												sAutosave = iDays + Messages.getString("Utils.9"); //$NON-NLS-1$
											}
											sAux = sString.substring(0, iIndex) + sAutosave + sString.substring(iIndex + "__SAVE_DAYS__".length()); //$NON-NLS-1$
											return getDynamicString(sAux);
										} else {
											iIndex = sString.indexOf("__SIEGES__"); //$NON-NLS-1$
											if (iIndex != -1) {
												String sSiegeDifficulty;
												if (Game.getSiegeDifficulty() == Game.SIEGE_DIFFICULTY_OFF) {
													sSiegeDifficulty = Messages.getString("Utils.8"); //$NON-NLS-1$
												} else if (Game.getSiegeDifficulty() == Game.SIEGE_DIFFICULTY_EASY) {
													sSiegeDifficulty = Messages.getString("Utils.11"); //$NON-NLS-1$
												} else if (Game.getSiegeDifficulty() == Game.SIEGE_DIFFICULTY_NORMAL) {
													sSiegeDifficulty = Messages.getString("Utils.12"); //$NON-NLS-1$
												} else if (Game.getSiegeDifficulty() == Game.SIEGE_DIFFICULTY_HARD) {
													sSiegeDifficulty = Messages.getString("Utils.14"); //$NON-NLS-1$
												} else if (Game.getSiegeDifficulty() == Game.SIEGE_DIFFICULTY_HARDER) {
													sSiegeDifficulty = Messages.getString("Utils.16"); //$NON-NLS-1$
												} else {
													sSiegeDifficulty = Messages.getString("Utils.15"); //$NON-NLS-1$
												}
												sAux = sString.substring(0, iIndex) + sSiegeDifficulty + sString.substring(iIndex + "__SIEGES__".length()); //$NON-NLS-1$
												return getDynamicString(sAux);
											} else {
												iIndex = sString.indexOf("__SIEGE_PAUSE__"); //$NON-NLS-1$
												if (iIndex != -1) {
													String sSiegePauseON = Game.isSiegePause() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
													sAux = sString.substring(0, iIndex) + sSiegePauseON + sString.substring(iIndex + "__SIEGE_PAUSE__".length()); //$NON-NLS-1$
													return getDynamicString(sAux);
												} else {
													iIndex = sString.indexOf("__CARAVAN_PAUSE__"); //$NON-NLS-1$
													if (iIndex != -1) {
														String sCaravanPauseON = Game.isCaravanPause() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
														sAux = sString.substring(0, iIndex) + sCaravanPauseON + sString.substring(iIndex + "__CARAVAN_PAUSE__".length()); //$NON-NLS-1$
														return getDynamicString(sAux);
													} else {
														iIndex = sString.indexOf("__VOLMUSIC__"); //$NON-NLS-1$
														if (iIndex != -1) {
															String sVolMusic = (Game.getVolumeMusic() * 10) + "%"; //$NON-NLS-1$
															sAux = sString.substring(0, iIndex) + sVolMusic + sString.substring(iIndex + "__VOLMUSIC__".length()); //$NON-NLS-1$
															return getDynamicString(sAux);
														} else {
															iIndex = sString.indexOf("__VOLFX__"); //$NON-NLS-1$
															if (iIndex != -1) {
																String sVolFX = (Game.getVolumeFX() * 10) + "%"; //$NON-NLS-1$
																sAux = sString.substring(0, iIndex) + sVolFX + sString.substring(iIndex + "__VOLFX__".length()); //$NON-NLS-1$
																return getDynamicString(sAux);
															} else {
																iIndex = sString.indexOf("__MOD__"); //$NON-NLS-1$
																if (iIndex != -1) {
																	// Mod, miramos el nombre
																	String sSubStr = sString.substring(iIndex);
																	int iIndexEndMod = sSubStr.indexOf("__/MOD__"); //$NON-NLS-1$
																	if (iIndexEndMod != -1) {
																		// Tenemos mod y /mod
																		String sModName = sSubStr.substring("__MOD__".length(), iIndexEndMod); //$NON-NLS-1$
																		String sModON = Game.isModLoaded(sModName) ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
																		sAux = sString.substring(0, iIndex) + sModON + sSubStr.substring(iIndexEndMod + "__/MOD__".length()); //$NON-NLS-1$
																	} else {
																		// Raro, raro, borramos el __MOD__ para que siga mirando dynamic strings
																		sAux = sString.substring(0, iIndex) + sString.substring(iIndex + "__MOD__".length()); //$NON-NLS-1$
																	}
																	return getDynamicString(sAux);
																} else {
																	iIndex = sString.indexOf("__BURY__"); //$NON-NLS-1$
																	if (iIndex != -1) {
																		String sBury = Game.isAllowBury() ? Messages.getString("Utils.4") : Messages.getString("Utils.5"); //$NON-NLS-1$ //$NON-NLS-2$
																		sAux = sString.substring(0, iIndex) + sBury + sString.substring(iIndex + "__BURY__".length()); //$NON-NLS-1$
																		return getDynamicString(sAux);
																	} else {
																		iIndex = sString.indexOf("__CPUPF__"); //$NON-NLS-1$
																		if (iIndex != -1) {
																			String sLevel = Integer.toString(Game.getPathfindingCPULevel());
																			sAux = sString.substring(0, iIndex) + sLevel + sString.substring(iIndex + "__CPUPF__".length()); //$NON-NLS-1$
																			return getDynamicString(sAux);
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
								}
							}
						}
					}
				}
			}
		}

		return sString;
	}

	/**
	 * Devuelve un array de strings a partir de un string con elementos
	 * separados por comas ","
	 *
	 * @param sChain Cadena
	 * @return un array de strings a partir de un string con elementos separados
	 * por comas ",". Null en caso de error/cadena vacía
	 */
	public static ArrayList<String> getArray(String sChain) {
		if (sChain != null && sChain.trim().length() > 0) {
			ArrayList<String> alReturn = new ArrayList<String>();
			StringTokenizer tokenizer = new StringTokenizer(sChain.trim(), ","); //$NON-NLS-1$
			while (tokenizer.hasMoreElements()) {
				alReturn.add(tokenizer.nextToken());
			}

			return alReturn;
		}

		return null;
	}

	/**
	 * Devuelve un array de Integers a partir de un string con elementos
	 * separados por comas ","
	 *
	 * @param sChain Cadena
	 * @return un array de Integers a partir de un string con elementos
	 * separados por comas ",". Null en caso de error/cadena vacía
	 */
	public static ArrayList<Integer> getArrayIntegers(String sChain) throws Exception {
		if (sChain != null && sChain.trim().length() > 0) {
			ArrayList<Integer> alReturn = new ArrayList<Integer>();
			StringTokenizer tokenizer = new StringTokenizer(sChain.trim(), ","); //$NON-NLS-1$
			while (tokenizer.hasMoreElements()) {
				String sToken = tokenizer.nextToken();
				try {
					alReturn.add(Integer.valueOf(sToken));
				} catch (Exception e) {
					throw new Exception(Messages.getString("Utils.22") + tokenizer.nextToken() + Messages.getString("Utils.24")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			return alReturn;
		}

		return null;
	}

	/**
	 * Devuelve una lista de Strings con los languages encontrados (es_ES,
	 * en_US, ....)
	 *
	 * @return una lista de Strings con los languages encontrados (es_ES, en_US,
	 * ....)
	 */
	public static ArrayList<LanguageData> getLanguages() {
		ArrayList<LanguageData> alReturn = new ArrayList<LanguageData>();

		// Vanilla
		File fLanguagesFolder = new File("data/languages/"); //$NON-NLS-1$
		if (fLanguagesFolder.exists()) {
			String[] asFiles = fLanguagesFolder.list();
			for (int i = 0; i < asFiles.length; i++) {
				if (asFiles[i] != null && asFiles[i].startsWith("messages") && asFiles[i].endsWith(".properties")) { //$NON-NLS-1$ //$NON-NLS-2$
					String sID = asFiles[i].substring("messages".length()); //$NON-NLS-1$
					sID = sID.substring(0, sID.length() - ".properties".length()); //$NON-NLS-1$
					if (sID.startsWith("_")) { //$NON-NLS-1$
						sID = sID.substring(1);
					}

					LanguageData ld = new LanguageData();

					// Name
					Properties prop = new Properties();
					try {
						prop.load(new FileInputStream(fLanguagesFolder.getAbsolutePath() + File.separator + asFiles[i]));
						ld.name = prop.getProperty("LANGUAGE_NAME"); //$NON-NLS-1$
					} catch (Exception e) {
						ld.name = null;
					}

					if (ld.name == null || ld.name.trim().length() == 0) {
						ld.name = Messages.getString("Utils.34"); //$NON-NLS-1$
					}

					// Mod
					ld.mod = null;

					String sLanguage, sCountry;
					StringTokenizer tokenizer = new StringTokenizer(sID, "_"); //$NON-NLS-1$
					if (tokenizer.hasMoreTokens()) {
						sLanguage = tokenizer.nextToken();

						if (tokenizer.hasMoreTokens()) {
							sCountry = tokenizer.nextToken();
						} else {
							sLanguage = null;
							sCountry = null;
						}
					} else {
						sLanguage = null;
						sCountry = null;
					}

					if (sLanguage == null) {
						sLanguage = "en"; //$NON-NLS-1$
						sCountry = "US"; //$NON-NLS-1$
					}

					// Language and country
					ld.language = sLanguage;
					ld.country = sCountry;

					alReturn.add(ld);
				}
			}
		}

		// Mods
		File fUserFolder = new File(Game.getUserFolder());
		if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
			return alReturn;
		}

		ArrayList<String> alModsLoaded = Game.getModsLoaded();
		if (alModsLoaded == null || alModsLoaded.size() == 0) {
			return alReturn;
		}

		String sModName;
		for (int m = 0; m < alModsLoaded.size(); m++) {
			sModName = alModsLoaded.get(m);

			String sModLanguagesPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + sModName + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "/languages/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			File f = new File(sModLanguagesPath);
			if (f.exists()) {
				String[] asFiles = f.list();
				for (int i = 0; i < asFiles.length; i++) {
					if (asFiles[i] != null && asFiles[i].startsWith("messages") && asFiles[i].endsWith(".properties")) { //$NON-NLS-1$ //$NON-NLS-2$
						String sID = asFiles[i].substring("messages".length()); //$NON-NLS-1$
						sID = sID.substring(0, sID.length() - ".properties".length()); //$NON-NLS-1$

						if (sID.startsWith("_")) { //$NON-NLS-1$
							sID = sID.substring(1);
						}

						LanguageData ld = new LanguageData();

						// Name
						Properties prop = new Properties();
						try {
							prop.load(new FileInputStream(sModLanguagesPath + asFiles[i]));
							ld.name = prop.getProperty("LANGUAGE_NAME"); //$NON-NLS-1$
						} catch (Exception e) {
							ld.name = null;
						}

						if (ld.name == null || ld.name.trim().length() == 0) {
							ld.name = Messages.getString("Utils.34"); //$NON-NLS-1$
						}

						// Mod
						ld.mod = sModName;

						String sLanguage, sCountry;
						StringTokenizer tokenizer = new StringTokenizer(sID, "_"); //$NON-NLS-1$
						if (tokenizer.hasMoreTokens()) {
							sLanguage = tokenizer.nextToken();

							if (tokenizer.hasMoreTokens()) {
								sCountry = tokenizer.nextToken();
							} else {
								sLanguage = null;
								sCountry = null;
							}
						} else {
							sLanguage = null;
							sCountry = null;
						}

						if (sLanguage == null) {
							sLanguage = "en"; //$NON-NLS-1$
							sCountry = "US"; //$NON-NLS-1$
						}

						// Language and country
						ld.language = sLanguage;
						ld.country = sCountry;

						alReturn.add(ld);
					}
				}
			}
		}

		return alReturn;
	}

	/**
	 * loads a resource bundle that is located in the "./data" directory rather
	 * than from the classpath
	 *
	 * @param name
	 * @return
	 */
	public static ResourceBundle getResourceBundle(String name) {
		return ResourceBundle.getBundle(name, Locale.getDefault(), LOCAL_RESOURCE_CLASS_LOADER);
	}

}
