package xaos.generator;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import xaos.TownsProperties;

import xaos.events.EventManager;
import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.Cell;
import xaos.tiles.terrain.Terrain;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;


public class MapGenerator extends Generator {

	public final static String XML_FILE = "gen_map.xml"; //$NON-NLS-1$

	public static int NUM_CITIZENS = -1;
	public static int NUM_GODS = -1;
	public static short STARTING_X = -1;
	public static short STARTING_Y = -1;
	public static short STARTING_LEVEL = -1;
	public static boolean ALLOW_BURY = true;
	public static ArrayList<String> STARTING_EVENTS = null;
	public static ArrayList<String> IMAGES_AT_START = null;

	private static HashMap<String, ArrayList<Point3DShort>> hmSeedsIDs;


	/**
	 * Lee del gen_map.xml y genera todo el mapa
	 */
	public static Cell[][][] generateMap (String sCampaignID, String sMissionID) {
		String sLog = null;
		if (TownsProperties.DEBUG_MODE) {
			sLog = Messages.getString ("MapGenerator.7"); //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, "MapGenerator"); //$NON-NLS-1$
		}
		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("MapGenerator.7")); //$NON-NLS-1$

		hmSeedsIDs = new HashMap<String, ArrayList<Point3DShort>> ();

		// Leemos el gen_map.xml (si está en una misión se carga de otro sitio)
		Generator generator = new Generator ();
		ArrayList<String> alPaths = Utils.getPathToFile (XML_FILE, sCampaignID, sMissionID);

		for (int i = 0; i < alPaths.size (); i++) {
			Generator.read (alPaths.get (i), generator, i == 0);
		}

		// Buscamos el MapGeneratorItem que se llame "init"
		ArrayList<GeneratorItem> lista = generator.getList ();
		GeneratorItem item = null;
		for (int i = 0; i < lista.size (); i++) {
			item = lista.get (i);
			if (item.getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT)) {
				item = lista.remove (i);
				break;
			}
		}

		if (item == null || !item.getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT)) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.2"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			Game.exit ();
		}

		short iNumLevelsUnderground = 0, iNumLevelsOutside = 0;
		NUM_CITIZENS = -1;
		NUM_GODS = -1;
		STARTING_LEVEL = -1;
		STARTING_X = -1;
		STARTING_Y = -1;
		ArrayList<String> alStartingEventsPCT = null;
		String MAINTERRAIN = null;
		ALLOW_BURY = true;
		STARTING_EVENTS = null;
		IMAGES_AT_START = null;
		for (int i = 0; i < item.getList ().size (); i++) {
			if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_NUM_LEVELES_UNDERGROUND)) {
				iNumLevelsUnderground = (short) Utils.launchDice (item.getList ().get (i).getValue ());
			} else if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_NUM_LEVELS_OUTSIDE)) {
				iNumLevelsOutside = (short) Utils.launchDice (item.getList ().get (i).getValue ());
			} else if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_MAINTERAIN)) {
				MAINTERRAIN = item.getList ().get (i).getValue ();
			} else if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_NUM_CITIZENS)) {
				NUM_CITIZENS = Utils.launchDice (item.getList ().get (i).getValue ());
			} else if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_STARTING_POINT)) {
				String sX = null;
				String sY = null;
				String sValue = item.getList ().get (i).getValue ();
				if (sValue != null) {
					StringTokenizer tokenizer = new StringTokenizer (sValue, ",");
					if (tokenizer.hasMoreTokens ()) {
						sX = tokenizer.nextToken ();
						if (tokenizer.hasMoreTokens ()) {
							sY = tokenizer.nextToken ();
						}
					}
				}
				if (sX != null && sY != null) {
					try {
						short iX = Short.parseShort (sX.trim ());
						short iY = Short.parseShort (sY.trim ());

						if (iX >= 0 && iY >= 0 && iX < World.MAP_WIDTH && iY < World.MAP_HEIGHT) {
							STARTING_X = iX;
							STARTING_Y = iY;
						}
					}
					catch (NumberFormatException nfe) {
					}
				}
			} else if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_STARTING_LEVEL)) {
				STARTING_LEVEL = (short) Utils.launchDice (item.getList ().get (i).getValue ());
			} else if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_ALLOW_BURY)) {
				ALLOW_BURY = Boolean.parseBoolean (item.getList ().get (i).getValue ());
			} else if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_STARTING_EVENTS)) {
				STARTING_EVENTS = Utils.getArray (item.getList ().get (i).getValue ());
			} else if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_STARTING_EVENTS_PCT)) {
				alStartingEventsPCT = Utils.getArray (item.getList ().get (i).getValue ());
			} else if (item.getList ().get (i).getName ().equalsIgnoreCase (MapGeneratorItem.ITEM_INIT_NUM_GODS)) {
				NUM_GODS = Utils.launchDice (item.getList ().get (i).getValue ());
			}
		}

		if (iNumLevelsUnderground <= 0) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.8"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			Game.exit ();
		}
		if (iNumLevelsOutside <= 0) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.11"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			Game.exit ();
		}
		if (MAINTERRAIN == null || MAINTERRAIN.trim ().length () == 0) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.4"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			Game.exit ();
		}
		if (NUM_CITIZENS < 1) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.12") + NUM_CITIZENS + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Game.exit ();
		}

		if (NUM_GODS < 1) {
			NUM_GODS = 0;
		}

		// Events
		if (STARTING_EVENTS != null && STARTING_EVENTS.size () > 0) {
			// Si hay eventos miramos que la cola de PCTs sea del mismo tamaño
			if (alStartingEventsPCT == null || alStartingEventsPCT.size () != STARTING_EVENTS.size ()) {
				Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.14"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
				Game.exit ();
			}

			// Miramos que los eventos existan
			for (int i = 0; i < STARTING_EVENTS.size (); i++) {
				if (EventManager.getItem (STARTING_EVENTS.get (i)) == null) {
					Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.15") + " [" + STARTING_EVENTS.get (i) + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					Game.exit ();
				}
			}

			// Lanzamos PCTs y borramos los eventos que no hiteen
			int iPCT;
			int iIndex = STARTING_EVENTS.size () - 1;
			while (iIndex >= 0) {
				iPCT = Utils.launchDice (alStartingEventsPCT.get (iIndex));

				if (iPCT < 0) {
					iPCT = 0;
				}

				if (Utils.launchDice (1, 100) > iPCT) {
					STARTING_EVENTS.remove (iIndex);
				}

				iIndex--;
			}
		} else {
			STARTING_EVENTS = null;
		}

		World.MAP_DEPTH = (short) (iNumLevelsOutside + iNumLevelsUnderground);
		if (World.MAP_DEPTH > World.MAX_DEPTH) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.9") + World.MAX_DEPTH + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Game.exit ();
		}
		World.MAP_NUM_LEVELS_OUTSIDE = iNumLevelsOutside;
		World.MAP_NUM_LEVELS_UNDERGROUND = iNumLevelsUnderground;

		if (STARTING_LEVEL < 0 || STARTING_LEVEL >= World.MAP_DEPTH) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.16") + STARTING_LEVEL + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Game.exit ();
		}

		// Array de MAINTERRAINS
		StringTokenizer tokenizer = new StringTokenizer (MAINTERRAIN, ","); //$NON-NLS-1$
		ArrayList<String> alBaseTerrains = new ArrayList<String> ();
		while (tokenizer.hasMoreTokens ()) {
			alBaseTerrains.add (tokenizer.nextToken ());
		}
		if (alBaseTerrains.size () <= 0) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.6") + MAINTERRAIN + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Game.exit ();
		}

		int iTerrainIDTmp = TerrainManager.getItem (alBaseTerrains.get (0)).getTerrainID ();

		// Mapa temporal, cada celda contiene una lista, el primer elemento es el terreno, los siguientes son tipos especiales (ej: WATER, LAVA, ...)
		MapGeneratorItem[][][] asMap = new MapGeneratorItem [World.MAP_WIDTH] [World.MAP_HEIGHT] [World.MAP_DEPTH];
		// Incializo todo a mainTerrain
		for (int z = 0; z < World.MAP_DEPTH; z++) {
			if (z < iNumLevelsOutside) {
				// Terreno aire (celdas null)
				for (int x = 0; x < World.MAP_WIDTH; x++) {
					for (int y = 0; y < World.MAP_HEIGHT; y++) {
						asMap[x][y][z] = new MapGeneratorItem ();
						asMap[x][y][z].setTerrainID (TerrainManagerItem.TERRAIN_AIR_ID);
					}
				}
			} else {
				if ((z - iNumLevelsOutside) < alBaseTerrains.size ()) {
					iTerrainIDTmp = TerrainManager.getItem (alBaseTerrains.get (z - iNumLevelsOutside)).getTerrainID ();
				}

				for (int x = 0; x < World.MAP_WIDTH; x++) {
					for (int y = 0; y < World.MAP_HEIGHT; y++) {
						asMap[x][y][z] = new MapGeneratorItem ();
						asMap[x][y][z].setTerrainID (iTerrainIDTmp);
					}
				}
			}
		}

		long lTime = TownsProperties.DEBUG_MODE ? System.currentTimeMillis() : 0L;
		// Ya tenemos todo inicializado, procedemos a recorrer el generator y ir haciendo las cosas
		for (int i = 0; i < generator.getList ().size (); i++) {
			item = generator.getList ().get (i);
			if (item.getName () == MapGeneratorItem.ITEM_SEED) {
				if (TownsProperties.DEBUG_MODE) {
					sLog = Messages.getString ("MapGenerator.44") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
					lTime = System.currentTimeMillis ();
				}
				Game.getPanelMainMenu ().setLoadingText (Messages.getString ("MapGenerator.44")); //$NON-NLS-1$

				generateSeed (asMap, item);
				if (TownsProperties.DEBUG_MODE) {
					sLog += (System.currentTimeMillis () - lTime) + " ms)"; //$NON-NLS-1$
					Log.log (Log.LEVEL_DEBUG, sLog, "MapGenerator"); //$NON-NLS-1$
				}
			} else if (item.getName () == MapGeneratorItem.ITEM_HEIGHTSEED) {
				if (TownsProperties.DEBUG_MODE) {
					sLog = Messages.getString ("MapGenerator.45") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
					lTime = System.currentTimeMillis ();
				}
				Game.getPanelMainMenu ().setLoadingText (Messages.getString ("MapGenerator.45")); //$NON-NLS-1$
				generateHeightSeed (asMap, item);
				if (TownsProperties.DEBUG_MODE) {
					sLog += (System.currentTimeMillis () - lTime) + " ms)"; //$NON-NLS-1$
					Log.log (Log.LEVEL_DEBUG, sLog, "MapGenerator"); //$NON-NLS-1$
				}
			} else if (item.getName () == MapGeneratorItem.ITEM_BEZIER) {
				if (TownsProperties.DEBUG_MODE) {
					sLog = Messages.getString ("MapGenerator.46") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
					lTime = System.currentTimeMillis ();
				}
				Game.getPanelMainMenu ().setLoadingText (Messages.getString ("MapGenerator.46")); //$NON-NLS-1$
				generateBezier (asMap, item);
				if (TownsProperties.DEBUG_MODE) {
					sLog += (System.currentTimeMillis () - lTime) + " ms)"; //$NON-NLS-1$
					Log.log (Log.LEVEL_DEBUG, sLog, "MapGenerator"); //$NON-NLS-1$
				}
			} else if (item.getName () == MapGeneratorItem.ITEM_CHANGE) {
				if (TownsProperties.DEBUG_MODE) {
					sLog = Messages.getString ("MapGenerator.47") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
					lTime = System.currentTimeMillis ();
				}
				Game.getPanelMainMenu ().setLoadingText (Messages.getString ("MapGenerator.47")); //$NON-NLS-1$
				generateChange (asMap, item);
				if (TownsProperties.DEBUG_MODE) {
					sLog += (System.currentTimeMillis () - lTime) + " ms)"; //$NON-NLS-1$
					Log.log (Log.LEVEL_DEBUG, sLog, "MapGenerator"); //$NON-NLS-1$
				}
			} else {
				Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.0") + item.getName () + Messages.getString ("MapGenerator.1") + XML_FILE + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}

		if (TownsProperties.DEBUG_MODE) {
			sLog = Messages.getString ("MapGenerator.48") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("MapGenerator.48")); //$NON-NLS-1$

		// Mapa generado, vamos a eliminar las celdas AIR del último nivel
		iTerrainIDTmp = TerrainManager.getItem (alBaseTerrains.get (alBaseTerrains.size () - 1)).getTerrainID ();

		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				if (asMap[x][y][World.MAP_DEPTH - 1].getTerrainID () == TerrainManagerItem.TERRAIN_AIR_ID) {
					asMap[x][y][World.MAP_DEPTH - 1].setTerrainID (iTerrainIDTmp);
				}
			}
		}

		// Ya tenemos el mapa generado, lo creamos en la estructura de verdad
		Cell[][][] cells = new Cell [World.MAP_WIDTH] [World.MAP_HEIGHT] [World.MAP_DEPTH];
		TerrainManagerItem tmi;
		for (int z = 0; z < World.MAP_DEPTH; z++) {
			for (int x = 0; x < World.MAP_WIDTH; x++) {
				for (int y = 0; y < World.MAP_HEIGHT; y++) {
					// Obtenemos el String con el terreno y los tipos especiales
					tmi = TerrainManager.getItemByID (asMap[x][y][z].getTerrainID ());
					if (tmi == null) {
						Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.5") + asMap[x][y][z].getTerrainID () + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						Game.exit ();
					}
					if (!asMap[x][y][z].hasSpecial ()) {
						cells[x][y][z] = new Cell (tmi.getTerrain ());
					} else {
						cells[x][y][z] = new Cell (TerrainManager.getItemByID (TerrainManagerItem.TERRAIN_AIR_ID).getTerrain ());
						// Tipos especiales
						if (asMap[x][y][z].hasSpecial ()) {
							if (asMap[x][y][z].getSpecial () == MapGeneratorItem.SPECIAL_INT_WATER_1) {
								cells[x][y][z].getTerrain ().setFluidType (Terrain.FLUIDS_WATER);
								cells[x][y][z].getTerrain ().setFluidCount (1);
							} else if (asMap[x][y][z].getSpecial () == MapGeneratorItem.SPECIAL_INT_WATER) {
								cells[x][y][z].getTerrain ().setFluidType (Terrain.FLUIDS_WATER);
								cells[x][y][z].getTerrain ().setFluidCount (Terrain.FLUIDS_COUNT_MAX);
							} else if (asMap[x][y][z].getSpecial () == MapGeneratorItem.SPECIAL_INT_WATER_INF) {
								cells[x][y][z].getTerrain ().setFluidType (Terrain.FLUIDS_WATER);
								cells[x][y][z].getTerrain ().setFluidCount (Terrain.FLUIDS_COUNT_INFINITE);
							} else if (asMap[x][y][z].getSpecial () == MapGeneratorItem.SPECIAL_INT_LAVA_1) {
								cells[x][y][z].getTerrain ().setFluidType (Terrain.FLUIDS_LAVA);
								cells[x][y][z].getTerrain ().setFluidCount (1);
							} else if (asMap[x][y][z].getSpecial () == MapGeneratorItem.SPECIAL_INT_LAVA) {
								cells[x][y][z].getTerrain ().setFluidType (Terrain.FLUIDS_LAVA);
								cells[x][y][z].getTerrain ().setFluidCount (Terrain.FLUIDS_COUNT_MAX);
							} else if (asMap[x][y][z].getSpecial () == MapGeneratorItem.SPECIAL_INT_LAVA_INF) {
								cells[x][y][z].getTerrain ().setFluidType (Terrain.FLUIDS_LAVA);
								cells[x][y][z].getTerrain ().setFluidCount (Terrain.FLUIDS_COUNT_INFINITE);
							} else {
								Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.10") + asMap[x][y][z].getSpecial () + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							}

							// Flag fluid_1 de la celda
							// No hace falta pq se chequean todas al acabar la generación
							// cells[x][y][z].setFlagFluid1 (cells[x][y][z].getTerrain ().getFluidCount () == 1);
						}
					}
				}
			}
		}

		// Discovereds
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				cells[x][y][0].setCoordinates (Point3DShort.getPoolInstance (x, y, 0));
				cells[x][y][0].getTerrain ().setMineTurns (0);
				cells[x][y][0].getTerrain ().setTerrainID (TerrainManagerItem.TERRAIN_AIR_ID);
				cells[x][y][0].getTerrain ().setTerrainTileID (TerrainManagerItem.TERRAIN_AIR_ID * TerrainManager.SLOPES_INIHEADER.length);
				cells[x][y][0].setDiscoveredPregenerate (true);
			}
		}
		for (int z = 1; z < World.MAP_DEPTH; z++) {
			for (int x = 0; x < World.MAP_WIDTH; x++) {
				for (int y = 0; y < World.MAP_HEIGHT; y++) {
					cells[x][y][z].setCoordinates (Point3DShort.getPoolInstance (x, y, z));
					cells[x][y][z].setDiscoveredPregenerate (airAround (cells, x, y, z));
				}
			}
		}

		hmSeedsIDs.clear ();

		// Depths
		// Cell.generateDepths (cells);
		// Diggeds
		Cell.generateDiggedsMinedsAndBlockys (cells);

		// Cambiamos los gráficos (al final pq usa el ismined de Cell)
		// Terrain.changeSlopes (cells);
		if (TownsProperties.DEBUG_MODE) {
			Log.log (Log.LEVEL_DEBUG, Messages.getString ("MapGenerator.51"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("MapGenerator.51")); //$NON-NLS-1$

		return cells;
	}


	private static boolean airAround (Cell[][][] cells, int x, int y, int z) {
		// Miramos si algún vecino tiene AIR, incluido arriba y abajo

		// Arriba
		if (z > 0 && cells[x][y][z - 1].getTerrain ().getTerrainID () == TerrainManagerItem.TERRAIN_AIR_ID) {
			return true;
		}

		// Abajo
		if (z < (World.MAP_DEPTH - 1) && cells[x][y][z + 1].getTerrain ().getTerrainID () == TerrainManagerItem.TERRAIN_AIR_ID) {
			return true;
		}

		// Vecinos
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i != x || j != y) {
					if ((i != 0 && j == 0) || (i == 0 && j != 0)) {
						if (Utils.isInsideMap (i + x, j + y, z)) {
							if (cells[i + x][j + y][z].getTerrain ().getTerrainID () == TerrainManagerItem.TERRAIN_AIR_ID) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}


	/**
	 * Genera y hace crecer una semilla
	 * 
	 * @param asMap
	 * @param item
	 */
	private static void generateSeed (MapGeneratorItem[][][] asMap, GeneratorItem item) {
		// Leemos todos los nodos
		SeedData sd = new SeedData (item);

		if (sd.type == null || sd.type.trim ().length () == 0) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.13"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Todo cargado, procedemos
		boolean[][][] abMap = new boolean [asMap.length] [asMap[0].length] [asMap[0][0].length];
		ArrayList<Point3DShort> alSeedPoints = new ArrayList<Point3DShort> ();

		int zMin, zMax;
		if (sd.heightMin == -1) {
			zMin = 0;
		} else {
			zMin = sd.heightMin;
		}
		if (sd.heightMax == -1) {
			zMax = World.MAP_DEPTH - 1;
		} else {
			zMax = sd.heightMax;
		}

		// Metemos las seeds
		int iX = 0, iY = 0, iLevel = 0;
		boolean bOK;
		for (int i = 0; i < sd.num; i++) {
			bOK = false;
			if (sd.startingPointID != null) {
				ArrayList<Point3DShort> alPoints = hmSeedsIDs.get (sd.startingPointID);
				if (alPoints != null && alPoints.size () > 0) {
					Point3DShort p3d = alPoints.get (Utils.getRandomBetween (0, alPoints.size () - 1));
					iX = p3d.x;
					iY = p3d.y;
					iLevel = p3d.z;
					bOK = true;
				}
			}

			if (!bOK) {
				iLevel = Utils.launchDice (sd.level);
				if (iLevel < 0) {
					iLevel = 0;
				} else if (iLevel >= World.MAP_DEPTH) {
					iLevel = World.MAP_DEPTH - 1;
				}

				if (sd.pointx == null || sd.pointx.length () == 0) {
					iX = Utils.getRandomBetween (0, World.MAP_WIDTH - 1);
				} else {
					iX = Utils.launchDice (sd.pointx);
				}
				if (sd.pointy == null || sd.pointy.length () == 0) {
					iY = Utils.getRandomBetween (0, World.MAP_HEIGHT - 1);
				} else {
					iY = Utils.launchDice (sd.pointy);
				}
			}

			if (iLevel >= zMin && iLevel <= zMax) {
				abMap[iX][iY][iLevel] = true;
				alSeedPoints.add (Point3DShort.getPoolInstance (iX, iY, iLevel));
			}
		}

		// Las hacemos crecer
		Point3DShort p3dAux;
		// System.out.println ("Turns: " + sd.turns);
		for (int i = 0; i < sd.turns; i++) {
			// System.out.println ("Turn: " + (i + 1) + " -> Size: " + alSeedPoints.size ());

			int iSize = alSeedPoints.size ();
			for (int iSeeds = 0; iSeeds < iSize; iSeeds++) {
				p3dAux = alSeedPoints.get (iSeeds);
				// 6 randoms para ver si crece hacia algun lado
				// Norte
				if ((p3dAux.y - 1) >= 0) {
					if (Utils.getRandomBetween (1, 100) <= sd.northPCT) {
						if (!abMap[p3dAux.x][p3dAux.y - 1][p3dAux.z]) {
							abMap[p3dAux.x][p3dAux.y - 1][p3dAux.z] = true;
							alSeedPoints.add (Point3DShort.getPoolInstance (p3dAux.x, p3dAux.y - 1, p3dAux.z));
						}
					}
				}
				// Sur
				if ((p3dAux.y + 1) < World.MAP_HEIGHT) {
					if (Utils.getRandomBetween (1, 100) <= sd.southPCT) {
						if (!abMap[p3dAux.x][p3dAux.y + 1][p3dAux.z]) {
							abMap[p3dAux.x][p3dAux.y + 1][p3dAux.z] = true;
							alSeedPoints.add (Point3DShort.getPoolInstance (p3dAux.x, p3dAux.y + 1, p3dAux.z));
						}
					}
				}
				// Este
				if ((p3dAux.x - 1) >= 0) {
					if (Utils.getRandomBetween (1, 100) <= sd.eastPCT) {
						if (!abMap[p3dAux.x - 1][p3dAux.y][p3dAux.z]) {
							abMap[p3dAux.x - 1][p3dAux.y][p3dAux.z] = true;
							alSeedPoints.add (Point3DShort.getPoolInstance (p3dAux.x - 1, p3dAux.y, p3dAux.z));
						}
					}
				}
				// Oeste
				if ((p3dAux.x + 1) < World.MAP_WIDTH) {
					if (Utils.getRandomBetween (1, 100) <= sd.westPCT) {
						if (!abMap[p3dAux.x + 1][p3dAux.y][p3dAux.z]) {
							abMap[p3dAux.x + 1][p3dAux.y][p3dAux.z] = true;
							alSeedPoints.add (Point3DShort.getPoolInstance (p3dAux.x + 1, p3dAux.y, p3dAux.z));
						}
					}
				}

				// Arriba
				if (p3dAux.z > 0 && (p3dAux.z - 1) >= zMin) {
					if (Utils.getRandomBetween (1, 100) <= sd.upPCT) {
						if (!abMap[p3dAux.x][p3dAux.y][p3dAux.z - 1]) {
							abMap[p3dAux.x][p3dAux.y][p3dAux.z - 1] = true;
							alSeedPoints.add (Point3DShort.getPoolInstance (p3dAux.x, p3dAux.y, p3dAux.z - 1));
						}
					}
				}

				// Abajo
				if (p3dAux.z < (World.MAP_DEPTH - 1) && (p3dAux.z + 1) <= zMax) {
					if (Utils.getRandomBetween (1, 100) <= sd.downPCT) {
						if (!abMap[p3dAux.x][p3dAux.y][p3dAux.z + 1]) {
							abMap[p3dAux.x][p3dAux.y][p3dAux.z + 1] = true;
							alSeedPoints.add (Point3DShort.getPoolInstance (p3dAux.x, p3dAux.y, p3dAux.z + 1));
						}
					}
				}
			}
		}

		// Seeds crecidas, metemos el type en el mapa pasado
		int iSpecialType = MapGeneratorItem.getSpecialInt (sd.type);
		int iTerrainID = 0;
		if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE) {
			TerrainManagerItem tmi = TerrainManager.getItem (sd.type);
			if (tmi == null) {
				Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.5") + sd.type + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			}
			iTerrainID = tmi.getTerrainID ();
		}

		// Si la seed tiene ID guardamos los puntos en la hash
		if (sd.id != null) {
			if (alSeedPoints.size () > 0) {
				hmSeedsIDs.put (sd.id, alSeedPoints);
			}
		}

		for (int iSeeds = 0; iSeeds < alSeedPoints.size (); iSeeds++) {
			p3dAux = alSeedPoints.get (iSeeds);

			if (iSpecialType != MapGeneratorItem.SPECIAL_INT_NONE) {
				asMap[p3dAux.x][p3dAux.y][p3dAux.z].setSpecial (iSpecialType);
			} else {
				asMap[p3dAux.x][p3dAux.y][p3dAux.z].setTerrainID (iTerrainID);
				asMap[p3dAux.x][p3dAux.y][p3dAux.z].setSpecial (MapGeneratorItem.SPECIAL_INT_NONE);
			}
		}

		// System.out.println (alSeedPoints.size ());
		Point3DShort.returnToPool (alSeedPoints);
	}


	/**
	 * Genera y hace crecer una semilla de height
	 * 
	 * @param asMap
	 * @param item
	 */
	private static void generateHeightSeed (MapGeneratorItem[][][] asMap, GeneratorItem item) {
		// Leemos todos los nodos
		HeightSeedData hsd = new HeightSeedData (item);

		if (hsd.flatsBetweenLevels < 1) {
			hsd.flatsBetweenLevels = 1;
		}

		// Todo cargado, procedemos
		boolean[][] abMap = new boolean [World.MAP_WIDTH] [World.MAP_HEIGHT];

		// Metemos las seeds
		int iX = 0, iY = 0;
		boolean bOK;
		for (int i = 0; i < hsd.num; i++) {
			bOK = false;
			if (hsd.startingPointID != null) {
				ArrayList<Point3DShort> alPoints = hmSeedsIDs.get (hsd.startingPointID);
				if (alPoints != null && alPoints.size () > 0) {
					Point3DShort p3d = alPoints.get (Utils.getRandomBetween (0, alPoints.size () - 1));
					iX = p3d.x;
					iY = p3d.y;
					bOK = true;
				}
			}

			if (!bOK) {
				if (hsd.pointx == null || hsd.pointx.length () == 0) {
					iX = Utils.getRandomBetween (0, World.MAP_WIDTH - 1);
				} else {
					iX = Utils.launchDice (hsd.pointx);
				}
				if (hsd.pointy == null || hsd.pointy.length () == 0) {
					iY = Utils.getRandomBetween (0, World.MAP_HEIGHT - 1);
				} else {
					iY = Utils.launchDice (hsd.pointy);
				}
			}

			abMap[iX][iY] = true;
		}

		int[] aiX = new int [World.MAP_WIDTH * World.MAP_HEIGHT];
		int[] aiY = new int [World.MAP_WIDTH * World.MAP_HEIGHT];
		int iNumPoints;

		// Las hacemos crecer
		for (int i = 0; i < hsd.turns; i++) {
			iNumPoints = 0;

			// Recorremos el mapa buscando las seeds
			for (int x = 0; x < World.MAP_WIDTH; x++) {
				for (int y = 0; y < World.MAP_HEIGHT; y++) {
					if (abMap[x][y]) {
						// 4 randoms para ver si crece hacia algun lado
						// Norte
						if ((y - 1) >= 0) {
							if (Utils.getRandomBetween (1, 100) <= hsd.northPCT) {
								if (!abMap[x][y - 1]) {
									aiX[iNumPoints] = x;
									aiY[iNumPoints] = y - 1;
									iNumPoints++;
								}
							}
						}
						// Sur
						if ((y + 1) < World.MAP_HEIGHT) {
							if (Utils.getRandomBetween (1, 100) <= hsd.southPCT) {
								if (!abMap[x][y + 1]) {
									aiX[iNumPoints] = x;
									aiY[iNumPoints] = y + 1;
									iNumPoints++;
								}
							}
						}
						// Este
						if ((x - 1) >= 0) {
							if (Utils.getRandomBetween (1, 100) <= hsd.eastPCT) {
								if (!abMap[x - 1][y]) {
									aiX[iNumPoints] = x - 1;
									aiY[iNumPoints] = y;
									iNumPoints++;
								}
							}
						}
						// Oeste
						if ((x + 1) < World.MAP_WIDTH) {
							if (Utils.getRandomBetween (1, 100) <= hsd.westPCT) {
								if (!abMap[x + 1][y]) {
									aiX[iNumPoints] = x + 1;
									aiY[iNumPoints] = y;
									iNumPoints++;
								}
							}
						}

					}
				}
			}

			for (int s = 0; s < iNumPoints; s++) {
				abMap[aiX[s]][aiY[s]] = true;
			}
		}

		// Seeds crecidas, toca raisear el terreno
		// Creamos un auxiliar
		boolean[][] abMapAux = new boolean [abMap.length] [abMap[0].length];

		boolean bHaySeeds = true;
		while (bHaySeeds) {
			// Copiamos el auxiliar (y de paso miramos que aún haya seeds)
			bHaySeeds = false;
			for (int x = 0; x < World.MAP_WIDTH; x++) {
				for (int y = 0; y < World.MAP_HEIGHT; y++) {
					abMapAux[x][y] = abMap[x][y];
					if (abMap[x][y]) {
						bHaySeeds = true;
					}
				}
			}

			if (!bHaySeeds) {
				break;
			}

			// Recorremos las seeds para raisear
			for (int x = 0; x < World.MAP_WIDTH; x++) {
				for (int y = 0; y < World.MAP_HEIGHT; y++) {
					if (abMap[x][y]) {
						// Raiseamos
						// Buscamos el último _AIR
						boolean bHayAIRs = false;
						for (int z = 0; z <= World.MAP_NUM_LEVELS_OUTSIDE; z++) {
							if (asMap[x][y][z].getTerrainID () == TerrainManagerItem.TERRAIN_AIR_ID) {
								bHayAIRs = true;
							} else {
								// Tenemos un no-air
								if (bHayAIRs) {
									// Tiene espacio por encima, así que simplemente cambiamos el terrain por el de abajo
									asMap[x][y][z - 1].setTerrainID (asMap[x][y][z].getTerrainID ());
									asMap[x][y][z].setSpecial (MapGeneratorItem.SPECIAL_INT_NONE);
								}
							}
						}
					}
				}
			}

			// Ahora eliminamos las seeds sin vecinos
			boolean bAlgoEliminado = false;
			for (int x = 0; x < World.MAP_WIDTH; x++) {
				for (int y = 0; y < World.MAP_HEIGHT; y++) {
					if (abMapAux[x][y]) {
						breakNeighbours: for (int i = -hsd.flatsBetweenLevels; i <= hsd.flatsBetweenLevels; i++) {
							for (int j = -hsd.flatsBetweenLevels; j <= hsd.flatsBetweenLevels; j++) {
								if (x + i >= 0 && y + j >= 0 && x + i < World.MAP_WIDTH && y + j < World.MAP_HEIGHT) {
									if (!abMapAux[x + i][y + j]) {
										abMap[x][y] = false;
										bAlgoEliminado = true;
										break breakNeighbours;
									}
								} else {
									// Fuera del mapa
									abMap[x][y] = false;
									bAlgoEliminado = true;
									break breakNeighbours;
								}
							}
						}
					}
				}
			}

			if (!bAlgoEliminado) {
				// No se han podido quitar seeds, pues paramos
				break;
			}
		}
	}


	/**
	 * Genera una linea Bezier
	 * 
	 * @param asMap
	 * @param item
	 */
	private static void generateBezier (MapGeneratorItem[][][] asMap, GeneratorItem item) {
		// Leemos todos los nodos
		BezierData bd = new BezierData (item);

		// Comprobamos que tenemos todos los datos
		if (bd.type == null || bd.type.trim ().length () == 0) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.18"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if (bd.depth < 1) {
			bd.depth = 1;
		}

		if (bd.level < 0) {
			bd.level = -1;
		} else if (bd.level >= World.MAP_DEPTH) {
			bd.level = -1;
		}

		if (bd.level == -1) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.20"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if ((bd.level + bd.depth) > World.MAP_DEPTH) {
			Log.log (Log.LEVEL_ERROR, "Bezier. (level + depth) > MAX_DEPTH", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if (bd.point1xmin == -1 || bd.point1xmax == -1 || bd.point1ymin == -1 || bd.point1ymax == -1 || bd.point2xmin == -1 || bd.point2xmax == -1 || bd.point2ymin == -1 || bd.point2ymax == -1 || bd.controlpoint1xmin == -1 || bd.controlpoint1xmax == -1 || bd.controlpoint1ymin == -1 || bd.controlpoint1ymax == -1 || bd.controlpoint2xmin == -1 || bd.controlpoint2xmax == -1 || bd.controlpoint2ymin == -1 || bd.controlpoint2ymax == -1) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.26"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Todo ok, seguimos
		boolean bBezierPintado = false;
		while (!bBezierPintado) {
			Point pointPuntoInicial = new Point (Utils.getRandomBetween (bd.point1xmin, bd.point1xmax), Utils.getRandomBetween (bd.point1ymin, bd.point1ymax));
			Point pointPuntoControlA = new Point (Utils.getRandomBetween (bd.controlpoint1xmin, bd.controlpoint1xmax), Utils.getRandomBetween (bd.controlpoint1ymin, bd.controlpoint1ymax));
			Point pointPuntoControlB = new Point (Utils.getRandomBetween (bd.controlpoint2xmin, bd.controlpoint2xmax), Utils.getRandomBetween (bd.controlpoint2ymin, bd.controlpoint2ymax));
			Point pointPuntoFinal = new Point (Utils.getRandomBetween (bd.point2xmin, bd.point2xmax), Utils.getRandomBetween (bd.point2ymin, bd.point2ymax));
			int iGrosor = Utils.launchDice (bd.wide);
			if (iGrosor < 1) {
				iGrosor = 1;
			}

			// Pinto la linea
			int iSpecialType = MapGeneratorItem.getSpecialInt (bd.type);
			int iTerrainID = 0;
			if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE) {
				iTerrainID = TerrainManager.getItem (bd.type).getTerrainID ();
			}
			Point pointPuntoBezier;
			for (int i = 0; i < ((World.MAP_WIDTH + World.MAP_HEIGHT) * 10); i++) {
				pointPuntoBezier = Utils.getBezierPoint (pointPuntoInicial, pointPuntoFinal, pointPuntoControlA, pointPuntoControlB, (((double) i) / (double) ((World.MAP_WIDTH + World.MAP_HEIGHT) * 10)));
				for (int x = 0; x < iGrosor; x++) {
					for (int y = 0; y < iGrosor; y++) {
						if (Utils.isValidPoint (pointPuntoBezier.x + x, pointPuntoBezier.y + y, World.MAP_WIDTH, World.MAP_HEIGHT)) {
							// Metemos el tipo (o special type)
							if (iSpecialType != MapGeneratorItem.SPECIAL_INT_NONE) {
								for (int d = 0; d < bd.depth; d++) {
									asMap[pointPuntoBezier.x + x][pointPuntoBezier.y + y][bd.level + d].setSpecial (iSpecialType);
								}
							} else {
								for (int d = 0; d < bd.depth; d++) {
									asMap[pointPuntoBezier.x + x][pointPuntoBezier.y + y][bd.level + d].setTerrainID (iTerrainID);
									asMap[pointPuntoBezier.x + x][pointPuntoBezier.y + y][bd.level + d].setSpecial (MapGeneratorItem.SPECIAL_INT_NONE);
								}
							}
							bBezierPintado = true;
						}
					}
				}
			}
		}
	}


	/**
	 * Cambia el tipo de algunas celdas segun lo que tenga cerca
	 * 
	 * @param asMap
	 * @param item
	 */
	private static void generateChange (MapGeneratorItem[][][] asMap, GeneratorItem item) {
		// Leemos todos los nodos
		ChangeData cd = new ChangeData (item);

		if (cd.destination == null || cd.destination.length () == 0 || (cd.iHeightMin > cd.iHeightMax && cd.iHeightMax != -1)) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.3"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		boolean[][][] abMap = new boolean [asMap.length] [asMap[0].length] [asMap[0][0].length];

		int iSourceTerrainID = 0;
		if (cd.source != null) {
			TerrainManagerItem tmi = TerrainManager.getItem (cd.source);
			if (tmi != null) {
				iSourceTerrainID = tmi.getTerrainID ();
			} else {
				Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.5") + cd.source + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			}
		}

		int iSpecialType = MapGeneratorItem.getSpecialInt (cd.terrain);
		int iTerrainTerrainID = 0;
		if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE && cd.terrain != null) {
			TerrainManagerItem tmi = TerrainManager.getItem (cd.terrain);
			if (tmi == null) {
				Log.log (Log.LEVEL_ERROR, Messages.getString ("MapGenerator.5") + cd.terrain + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				iTerrainTerrainID = tmi.getTerrainID ();
			}
		}
		// Recorremos todo el mundo buscando el source, lanzando dados y sustituyendo por destination
		for (int z = 0; z < World.MAP_DEPTH; z++) {
			// Miramos heights
			if ((z >= cd.iHeightMin || cd.iHeightMin == -1) && (z <= cd.iHeightMax || cd.iHeightMax == -1)) {
				for (int x = 0; x < World.MAP_WIDTH; x++) {
					for (int y = 0; y < World.MAP_HEIGHT; y++) {
						if (cd.source == null || (asMap[x][y][z].getTerrainID () == iSourceTerrainID && !asMap[x][y][z].hasSpecial ())) {
							if (iSourceTerrainID != TerrainManagerItem.TERRAIN_AIR_ID) {
								// Lanzamos dado
								if (Utils.getRandomBetween (1, 100) <= cd.pct) {
									// Miramos vecinos
									if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE) {
										if (cd.terrain == null || checkNeighbors (asMap, x, y, z, iTerrainTerrainID, cd.radius)) {
											abMap[x][y][z] = true;
										}
									} else {
										if (checkNeighborsSpecial (asMap, x, y, z, iSpecialType, cd.radius)) {
											abMap[x][y][z] = true;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		iSpecialType = MapGeneratorItem.getSpecialInt (cd.destination);
		int iTerrainID = 0;
		if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE) {
			iTerrainID = TerrainManager.getItem (cd.destination).getTerrainID ();
		}
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				for (int z = 0; z < World.MAP_DEPTH; z++) {
					if (abMap[x][y][z]) {
						if (iSpecialType != MapGeneratorItem.SPECIAL_INT_NONE) {
							asMap[x][y][z].setSpecial (iSpecialType);
						} else {
							asMap[x][y][z].setTerrainID (iTerrainID);
							asMap[x][y][z].setSpecial (MapGeneratorItem.SPECIAL_INT_NONE);
						}
					}
				}
			}
		}
	}


	/**
	 * Indica si hay alguna casilla de tipo (terrain) alrrededor del punto pasado
	 * 
	 * @param asMap
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static boolean checkNeighbors (MapGeneratorItem[][][] asMap, int x, int y, int z, int iTerrainID, int radius) {
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				for (int n = -radius; n <= radius; n++) {
					if (i != 0 || j != 0 || n != 0) {
						if (Utils.isInsideMap (x + i, y + j, n + z)) {
							// Miramos si tiene el terrain
							if (asMap[x + i][y + j][n + z].getTerrainID () == iTerrainID) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}


	/**
	 * Indica si hay alguna casilla de tipo especial (terrain) alrrededor del punto pasado
	 * 
	 * @param asMap
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static boolean checkNeighborsSpecial (MapGeneratorItem[][][] asMap, int x, int y, int z, int iSpecialTerrain, int radius) {
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				for (int n = -radius; n <= radius; n++) {
					if (i != 0 || j != 0 || n != 0) {
						if (Utils.isInsideMap (x + i, y + j, n + z)) {
							// Miramos si tiene el terrain
							if (asMap[x + i][y + j][n + z].hasSpecial () && asMap[x + i][y + j][n + z].getSpecial () == iSpecialTerrain) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}
}
