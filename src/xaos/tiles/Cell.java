package xaos.tiles;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;

import xaos.campaign.TutorialTrigger;
import xaos.caravans.PricesManager;
import xaos.data.BuryData;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MiniMapPanel;
import xaos.stockpiles.Stockpile;
import xaos.tasks.Task;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.entities.living.projectiles.Projectile;
import xaos.tiles.terrain.Terrain;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.utils.UtilsIniHeaders;


public class Cell implements Externalizable {

	private static final long serialVersionUID = -3099620916063413505L;

	private static int[][] depths = new int [World.MAP_WIDTH] [World.MAP_HEIGHT];
	private static int[] neighborIDs = new int [27]; // Será 13 o 26, pongo 27 para poder poner el -1 al final
	private static int neighborIDsIndex;

	public static int MAX_ASTAR_ZONE_ID = 0;
	public static HashMap<Integer, Integer> HASH_ASTAR_ZONE_RELATIONS = new HashMap<Integer, Integer> ();

	// Flags
	public final static int FLAG_ORDERS = 1; // 00000001 (Indica si la celda tiene órdenes)
	public final static int FLAG_DISCOVERED = 2; // 00000010 (Indica si la celda está descubierta)
	public final static int FLAG_CAVE = 4; // 00000100 (Indica si la celda es de tipo caverna (se usa para descubrir la caverna entera cuando descubres una celda tipo cave))
	public final static int FLAG_PATROL_POINT = 8; // 0000001000 (Indica si algún soldado tiene ese punto de patrulla)
	public final static int FLAG_HERO_EXPLORING_POINT = 16; // 0000010000 (Indica si el punto es uno de los puntos de exploring de los heroes)
	public final static int FLAG_SHOULD_PAINT_UNDER = 32; // 0000100000 (Indica si hay que pintar casillas de niveles inferiores)
	public final static int FLAG_DIGGED = 64; // 0001000000 (Indica si la casilla de abajo está minada)
	public final static int FLAG_SHADOW = 128; // 0010000000 (Indica si la casilla está en la sombra)
	public final static int FLAG_BLOCKY = 256; // 0100000000 (Indica si la casilla está ocupada por un bloque que tapa las cosas de debajo)
	public final static int FLAG_MINED = 512; // 10 00000000 (Indica si la casilla está minada)
	public final static int FLAG_LIGHT_RFULL = 1024; // 100 00000000 (Indica si la casilla tiene luz roja full cerca)
	public final static int FLAG_LIGHT_GFULL = 2048; // 1000 00000000 (Indica si la casilla tiene luz verde full cerca)
	public final static int FLAG_LIGHT_BFULL = 4096; // 10000 00000000 (Indica si la casilla tiene luz azul full cerca)
	public final static int FLAG_LIGHT_RHALF = 8192; // 100000 00000000 (Indica si la casilla tiene luz roja floja cerca)
	public final static int FLAG_LIGHT_GHALF = 16384; // 1000000 00000000 (Indica si la casilla tiene luz verde floja cerca)
	public final static int FLAG_LIGHT_BHALF = 32768; // 10000000 00000000 (Indica si la casilla tiene luz azul floja cerca)
	public final static int FLAG_BURY = 65536; // 1 00000000 00000000 (Indica si la casilla tiene luz azul floja cerca)
	public final static int FLAG_FLUID_CHECK_LIST = 131072; // 10 00000000 00000000 (Indica si la casilla está en la lista de fluidos a chequear
	public final static int FLAG_OPEN = 262144; // 10 00000000 00000000 (Indica si la casilla está en la lista de fluidos a chequear
	public final static int FLAG_BLINK = 524288; // 100 00000000 00000000 (Indica si la casilla está en la lista de fluidos a chequear

	private int flags;

	private Terrain terrain;
	private Entity entity;
	private ArrayList<LivingEntity> livings;

	private int stockPileID; // Si > 0 indica el ID de stockpile
	private int zoneID; // Si > 0 es que es una zone
	private transient Point3DShort buildingCoordinates; // Si no es nulo es que la casilla contiene un edificio
	private transient Point3DShort coordinates;
	// private transient int depth;

	/**
	 * Es un ID para saber rápidamente si hay camino entre 2 puntos 2 celdas con distinto ID significa que no hay camino
	 */
	private int astarZoneID;


	/**
	 * Constructor principal
	 */
	public Cell () {
		this (null);
	}


	/**
	 * Constructor alternativo, se le pasa un Terrain, deja la entity vacía
	 */
	public Cell (Terrain terrain) {
		this (terrain, null);
	}


	/**
	 * Constructor alternativo, se le pasa un Terrain y una entity
	 */
	public Cell (Terrain terrain, Entity entity) {
		this.terrain = terrain;
		this.entity = entity;

		setAstarZoneID (-1);

		this.livings = null; // new ArrayList<LivingEntity> (2);
	}


	public void refreshTransients (short x, short y, short z) {
		setCoordinates (Point3DShort.getPoolInstance (x, y, z));

		if (terrain != null) {
			terrain.refreshTransients ();
		}

		if (entity != null) {
			entity.refreshTransients ();
		}

		if (livings != null) {
			for (int i = 0; i < livings.size (); i++) {
				livings.get (i).refreshTransients ();
				livings.get (i).setWaitingForPath (false);
			}
		}

		checkConnectors (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z);
	}


	/**
	 * @param flags the flags to set
	 */
	public void setFlags (int flags) {
		this.flags = flags;
	}


	/**
	 * @return the flags
	 */
	public int getFlags () {
		return flags;
	}


	public Terrain getTerrain () {
		return terrain;
	}


	public void setTerrain (Terrain terrain) {
		this.terrain = terrain;
	}


	public Entity getEntity () {
		return entity;
	}


	public ArrayList<LivingEntity> getLivings () {
		return livings;
	}


	public void addLiving (LivingEntity le) {
		if (livings == null) {
			livings = new ArrayList<LivingEntity> (1);
		}

		livings.add (le);
	}


	public void freeLivingsMem () {
		livings = null;
	}


	/**
	 * Indica si la celda contiene una living específica por tipo, la devuelve si la encuentra (la primera que pilla)
	 * 
	 * @param type
	 * @return
	 */
	public LivingEntity containsSpecificLiving (int type) {
		if (getLivings () != null && getLivings ().size () > 0) {
			for (int i = 0; i < getLivings ().size (); i++) {
				if (LivingEntityManager.getItem (getLivings ().get (i).getIniHeader ()).getType () == type) {
					return getLivings ().get (i);
				}
			}
		}

		return null;
	}


	public Item getItem () {
		if (entity != null && entity instanceof Item) {
			return (Item) entity;
		}

		return null;
	}


	public void setEntity (Entity entity) {
		boolean bCheckFluids = false;

		Stockpile stockpile = null;
		if (hasStockPile ()) {
			stockpile = Stockpile.getStockpile (getCoordinates ());
			if (stockpile != null) {
				// Updateamos los filledPoints de la pile
				if (this.entity == null && entity != null) {
					stockpile.setFilledPoints (stockpile.getFilledPoints () + 1);
				} else if (this.entity != null && entity == null) {
					stockpile.setFilledPoints (stockpile.getFilledPoints () - 1);
				}
			}
		}

		int iLightRadiusItemRemoved = -1;
		if (entity == null || (this.entity != null)) {
			// Se va a borrar la celda (o se va a meter algo encima de otro algo), restamos el townvalue de lo que hubiera
			// También eliminamos el item actual de la lista de hauling
			if (this.entity != null && this.entity instanceof Item) {
				Item cellItem = (Item) this.entity;
				ItemManagerItem imi = ItemManager.getItem (this.entity.getIniHeader ());
				World.setTownValue (World.getTownValue () - PricesManager.getPrice (cellItem));

				// Si era un container, restamos también el precio de lo de dentro
				if (imi.isContainer ()) {
					Container container = Game.getWorld ().getContainer (cellItem.getID ());
					if (container != null) {
						for (int i = 0; i < container.getItemsInside ().size (); i++) {
							World.setTownValue (World.getTownValue () - PricesManager.getPrice (container.getItemsInside ().get (i)));
						}
					}
				}

				// Eliminamos el item de la lista de hauling (si existe)
				Game.getWorld ().removeItemToBeHauledByItemID (cellItem.getID ());

				// Si la casilla de abajo no está discovered la ponemos discovered ahora
				if (getCoordinates ().z < (World.MAP_DEPTH - 1)) {
					Cell cellUnder = World.getCell (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z + 1);
					if (!cellUnder.isDiscovered ()) {
						cellUnder.setDiscovered (true);
					}
				}

				// Si lo que se quita es un fluid blocker comprobamos los fluidos adyacentes
				if (imi.isBlockFluids () || imi.isFluidsElevator ()) {
					bCheckFluids = true;
				}

				// Lights
				if (imi.getLightRadius () > 0) {
					iLightRadiusItemRemoved = imi.getLightRadius ();
				} else if (Utils.isCellBlockingLightItem (imi, cellItem)) {
					// Habia un item que bloquea luz
					iLightRadiusItemRemoved = 0;
				}
			}
		}

		this.entity = entity;

		// Miramos si el item puede ir aquí, en caso contrario lo meteremos en la lista de objetos para haulear
		if (entity != null && entity instanceof Item) {
			if (stockpile != null) {
				// Hay pila, si la misma no lo acepta lo metemos en la lista de haul
				if (!stockpile.itemAllowed (entity.getIniHeader ())) {
					Game.getWorld ().addItemToBeHauled ((Item) entity);
				}
			} else {
				// No hay pila, lo metemos en la lista de haul, el taskmanager ya mirará si es un item haul o no
				Game.getWorld ().addItemToBeHauled ((Item) entity);
			}
		}

		// Seteamos la celda indicando que ya no tiene órdenes (se usa para el dibujado)
		setFlagOrders (false);

		Point3DShort coord = getCoordinates ();

		ItemManagerItem imi = null;
		Item newCellItem = null;
		if (entity != null && entity instanceof Item) {
			newCellItem = (Item) entity;
			// Lo añadimos a la lista de check para fall
			World.addFallItem (newCellItem.getID ());

			imi = ItemManager.getItem (newCellItem.getIniHeader ());
			// Zone mergers
			if (imi.isZoneMergerUp () || imi.isWall () || imi.isBase ()) {
				if (newCellItem.isLocked () && newCellItem.isOperative ()) {
					// XCNCell.setAllZoneIDs ();
					if (imi.isWall ()) {
						setAstarZoneID (-1);
						// Si tiene base, merge de la de arriba
						if (coord.z > 0 && imi.isBase ()) {
							mergeZoneID (coord.x, coord.y, coord.z - 1, false);
						}
						// if (!Cell.splitTrickZoneID (getCoordinates ())) {
						World.setRecheckASZID (true);
						// Cell.setAllZoneIDs ();
						// }
					} else {
						World.setRecheckASZID (true);
					}
				}
			}

			// Town value
			World.setTownValue (World.getTownValue () + imi.getValue ());
			if (imi.isContainer ()) {
				// Containers, town value y coordenadas de los items de dentro, importante
				Container container = Game.getWorld ().getContainer (newCellItem.getID ());
				if (container != null) {
					for (int i = 0; i < container.getItemsInside ().size (); i++) {
						World.setTownValue (World.getTownValue () - PricesManager.getPrice (container.getItemsInside ().get (i)));
						container.getItemsInside ().get (i).setCoordinates (getCoordinates ());
					}
				}
			}

			// Tutorial flow?
			Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_PLACE, imi.getNumericalIniHeader (), getCoordinates ().toPoint3D ());
		}

		// Item Blocky?
		setBlocky (imi != null && imi.isBlocky ());
		setShouldPaintUnders (World.getCells (), getCoordinates ());

		// Shadows
		generateFullShadows (coord.x, coord.y, coord.z);

		// Light item removed
		if (iLightRadiusItemRemoved >= 0) {
			// Se va a borrar un item que producía luz, regeneramos
			generateLightsItemRemovedCellMined (coord.x, coord.y, coord.z, iLightRadiusItemRemoved);
			// Si el nuevo item tiene light el método generateLightsItemRemoved ya lo habrá checkeado
		} else {
			// Light new item
			if (imi != null && imi.getLightRadius () > 0) {
				generateLights (World.getCells (), coord.x, coord.y, coord.z);
			} else {
				// Nuevo item no produce luz, pero quizá es un lightblocker
				if (Utils.isCellBlockingLightItem (imi, newCellItem)) {
					generateLightsItemRemovedCellMined (coord.x, coord.y, coord.z, ItemManagerItem.MAX_LIGHT_RADIUS);
				}
			}
		}

		// Open cell
		Cell.generateOpen (World.getCells (), coord.x, coord.y);

		// Connectors
		checkConnectors (coord.x, coord.y, coord.z);
		checkConnectors (coord.x - 1, coord.y - 1, coord.z);
		checkConnectors (coord.x - 1, coord.y, coord.z);
		checkConnectors (coord.x - 1, coord.y + 1, coord.z);
		checkConnectors (coord.x + 1, coord.y - 1, coord.z);
		checkConnectors (coord.x + 1, coord.y, coord.z);
		checkConnectors (coord.x + 1, coord.y + 1, coord.z);
		checkConnectors (coord.x, coord.y - 1, coord.z);
		checkConnectors (coord.x, coord.y + 1, coord.z);

		// Si es un fluid blocker comprobamos las celdas adyacentes para que corra el agua
		if (bCheckFluids) {
			Game.getWorld ().addFluidCellToProcess (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z, true);
		} else if (imi != null) {
			if (imi.isFluidsElevator () || imi.isBlockFluids ()) {
				Game.getWorld ().addFluidCellToProcess (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z, true);
			}
		}
	}


	/**
	 * Comprueba los connectors y hace los cambios necesarios al item
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void checkConnectors (int x, int y, int z) {
		if (Game.getWorld () == null) {
			return;
		}
		if (x >= 0 && x < World.MAP_WIDTH && y >= 0 && y < World.MAP_HEIGHT && z >= 0 && z < World.MAP_DEPTH) {
			// Miramos si tiene items iguales a los lados (N,S,E,W por este orden, importante);
			Cell cell = World.getCell (x, y, z);
			Item item = cell.getItem ();
			if (item == null || !item.isOperative ()) {
				return;
			}

			ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
			if (imi == null) {
				return;
			}

			if (imi.isWall () || imi.isWallConnector ()) {
				checkConnectorsWalls (x, y, z, item, imi);
			} else if (imi.getFloorWalkSpeed () != 100) {
				checkConnectorsRoads (x, y, z, item, imi);
			}
		}
	}


	private static void checkConnectorsWalls (int x, int y, int z, Item item, ItemManagerItem imi) {
		ItemManagerItem imiTemp;
		StringBuffer sbConnectors = new StringBuffer ("_"); //$NON-NLS-1$
		Cell cellTemp;
		Item entityTemp;

		// Norte
		if (y > 0) {
			cellTemp = World.getCell (x, y - 1, z);
			if (!cellTemp.isMined ()) {
				sbConnectors.append ("N"); //$NON-NLS-1$
			} else {
				entityTemp = cellTemp.getItem ();
				if (entityTemp != null && entityTemp.isOperative ()) {
					imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
					// popoif (entityTemp.isOperative () && ((imi.isWallConnector () && imiTemp.isWall ()) || (imiTemp.isWallConnector () && imi.isWall ()) || entityTemp.getIniHeader ().equals (entity.getIniHeader ()))) {
					if (imiTemp.isWall () || imiTemp.isWallConnector ()) {
						sbConnectors.append ("N"); //$NON-NLS-1$
					}
				}
			}
		}
		// Sur
		if (y < (World.MAP_HEIGHT - 1)) {
			cellTemp = World.getCell (x, y + 1, z);
			if (!cellTemp.isMined ()) {
				sbConnectors.append ("S"); //$NON-NLS-1$
			} else {
				entityTemp = cellTemp.getItem ();
				if (entityTemp != null && entityTemp.isOperative ()) {
					imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
					// popoif (entityTemp.isOperative () && ((imi.isWallConnector () && imiTemp.isWall ()) || (imiTemp.isWallConnector () && imi.isWall ()) || entityTemp.getIniHeader ().equals (entity.getIniHeader ()))) {
					if (imiTemp.isWall () || imiTemp.isWallConnector ()) {
						sbConnectors.append ("S"); //$NON-NLS-1$
					}
				}
			}
		}
		// Este
		if (x < (World.MAP_WIDTH - 1)) {
			cellTemp = World.getCell (x + 1, y, z);
			if (!cellTemp.isMined ()) {
				sbConnectors.append ("E"); //$NON-NLS-1$
			} else {
				entityTemp = cellTemp.getItem ();
				if (entityTemp != null && entityTemp.isOperative ()) {
					imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
					// popoif (entityTemp.isOperative () && ((imi.isWallConnector () && imiTemp.isWall ()) || (imiTemp.isWallConnector () && imi.isWall ()) || entityTemp.getIniHeader ().equals (entity.getIniHeader ()))) {
					if (imiTemp.isWall () || imiTemp.isWallConnector ()) {
						sbConnectors.append ("E"); //$NON-NLS-1$
					}
				}
			}
		}
		// Oeste
		if (x > 0) {
			cellTemp = World.getCell (x - 1, y, z);
			if (!cellTemp.isMined ()) {
				sbConnectors.append ("W"); //$NON-NLS-1$
			} else {
				entityTemp = cellTemp.getItem ();
				if (entityTemp != null && entityTemp.isOperative ()) {
					imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
					// popoif (entityTemp.isOperative () && ((imi.isWallConnector () && imiTemp.isWall ()) || (imiTemp.isWallConnector () && imi.isWall ()) || entityTemp.getIniHeader ().equals (entity.getIniHeader ()))) {
					if (imiTemp.isWall () || imiTemp.isWallConnector ()) {
						sbConnectors.append ("W"); //$NON-NLS-1$
					}
				}
			}
		}

		boolean bInteriors = false;
		if (sbConnectors.toString ().length () == 5) {
			// Interiors
			// Northwest
			if (x > 0 && y > 0) {
				cellTemp = World.getCell (x - 1, y - 1, z);
				if (cellTemp.isMined ()) {
					entityTemp = cellTemp.getItem ();
					if (entityTemp != null && entityTemp.isOperative ()) {
						imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
						if (imiTemp.isWall () || imiTemp.isWallConnector ()) {
						} else {
							sbConnectors.setCharAt (1, 'n');
							bInteriors = true;
						}
					} else {
						sbConnectors.setCharAt (1, 'n');
						bInteriors = true;
					}
				}
			}
			// Southeast
			if (x < (World.MAP_WIDTH - 1) && y < (World.MAP_HEIGHT - 1)) {
				cellTemp = World.getCell (x + 1, y + 1, z);
				if (cellTemp.isMined ()) {
					entityTemp = cellTemp.getItem ();
					if (entityTemp != null && entityTemp.isOperative ()) {
						imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
						if (imiTemp.isWall () || imiTemp.isWallConnector ()) {
						} else {
							sbConnectors.setCharAt (2, 's');
							bInteriors = true;
						}
					} else {
						sbConnectors.setCharAt (2, 's');
						bInteriors = true;
					}
				}
			}
			// Northeast
			if (x < (World.MAP_WIDTH - 1) && y > 0) {
				cellTemp = World.getCell (x + 1, y - 1, z);
				if (cellTemp.isMined ()) {
					entityTemp = cellTemp.getItem ();
					if (entityTemp != null && entityTemp.isOperative ()) {
						imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
						if (imiTemp.isWall () || imiTemp.isWallConnector ()) {
						} else {
							sbConnectors.setCharAt (3, 'e');
							bInteriors = true;
						}
					} else {
						sbConnectors.setCharAt (3, 'e');
						bInteriors = true;
					}
				}
			}
			// Southwest
			if (x > 0 && y < (World.MAP_HEIGHT - 1)) {
				cellTemp = World.getCell (x - 1, y + 1, z);
				if (cellTemp.isMined ()) {
					entityTemp = cellTemp.getItem ();
					if (entityTemp != null && entityTemp.isOperative ()) {
						imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
						if (imiTemp.isWall () || imiTemp.isWallConnector ()) {
						} else {
							sbConnectors.setCharAt (4, 'w');
							bInteriors = true;
						}
					} else {
						sbConnectors.setCharAt (4, 'w');
						bInteriors = true;
					}
				}
			}
		}

		if (sbConnectors.toString ().length () > 1) {
			// Buscamos el connector que toca
			if (bInteriors) {
				// Si tiene interiors y no existen miraremos el _NSEW (sin minúsculas), si tampoco existe miraremos el iniHeader normal
				boolean bOK = item.setTileSetCoordinatesReturn (item.getIniHeader () + sbConnectors.toString (), item.getIniHeader () + "_NSEW"); //$NON-NLS-1$
				if (!bOK) {
					item.setTileSetCoordinates (item.getIniHeader (), item.getIniHeader ());
				}
			} else {
				item.setTileSetCoordinates (item.getIniHeader () + sbConnectors.toString (), item.getIniHeader ());
			}

			item.setTileSetTexCoordinates ();
			if (imi.isDoor ()) {
				item.refreshDoorTile ();
			}
		}
	}


	private static void checkConnectorsRoads (int x, int y, int z, Item item, ItemManagerItem imi) {
		ItemManagerItem imiTemp;
		StringBuffer sbConnectors = new StringBuffer ("_"); //$NON-NLS-1$
		Item entityTemp;

		// Norte
		if (y > 0) {
			entityTemp = World.getCell (x, y - 1, z).getItem ();
			if (entityTemp != null && entityTemp.isOperative ()) {
				imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
				if (imiTemp.getFloorWalkSpeed () != 100) {
					sbConnectors.append ("N"); //$NON-NLS-1$
				}
			}
		}
		// Sur
		if (y < (World.MAP_HEIGHT - 1)) {
			entityTemp = World.getCell (x, y + 1, z).getItem ();
			if (entityTemp != null && entityTemp.isOperative ()) {
				imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
				if (imiTemp.getFloorWalkSpeed () != 100) {
					sbConnectors.append ("S"); //$NON-NLS-1$
				}
			}
		}
		// Este
		if (x < (World.MAP_WIDTH - 1)) {
			entityTemp = World.getCell (x + 1, y, z).getItem ();
			if (entityTemp != null && entityTemp.isOperative ()) {
				imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
				if (imiTemp.getFloorWalkSpeed () != 100) {
					sbConnectors.append ("E"); //$NON-NLS-1$
				}
			}
		}
		// Oeste
		if (x > 0) {
			entityTemp = World.getCell (x - 1, y, z).getItem ();
			if (entityTemp != null && entityTemp.isOperative ()) {
				imiTemp = ItemManager.getItem (entityTemp.getIniHeader ());
				if (imiTemp.getFloorWalkSpeed () != 100) {
					sbConnectors.append ("W"); //$NON-NLS-1$
				}
			}
		}

		if (sbConnectors.toString ().length () > 1) {
			// Buscamos el connector que toca
			item.setTileSetCoordinates (item.getIniHeader () + sbConnectors.toString (), item.getIniHeader ());
			item.setTileSetTexCoordinates ();
			if (imi.isDoor ()) {
				item.refreshDoorTile ();
			}
		}
	}


	public boolean isDiscovered () {
		return (getFlags () & FLAG_DISCOVERED) > 0;
	}


	/**
	 * Marca una casilla como discovered/undiscovered.
	 * 
	 * @param discovered
	 */
	public void setDiscovered (boolean discovered) {
		// Si no estaba descubierta de antes, actualizamos el contador de livings y le decimos al minimapa que se actualice
		boolean bFromNonDiscoveredToDiscovered = (discovered && !isDiscovered () && getCoordinates () != null);

		if (discovered) {
			setFlags (getFlags () | FLAG_DISCOVERED);
		} else {
			setFlags (getFlags () & ~FLAG_DISCOVERED);
		}

		if (bFromNonDiscoveredToDiscovered) {
			Game.getWorld ().discoverFloor (getCoordinates ().z);

			if (LivingEntity.isCellAllowed (getCoordinates ())) {
				Cell.mergeZoneID (getCoordinates (), false);
			}

			// Al descubrir una casilla miramos si hay enemigo undiscovered
			ArrayList<LivingEntity> alLivings = World.getCell (getCoordinates ()).getLivings ();
			if (alLivings != null && alLivings.size () > 0) { // Hay livings, los sacamos de undiscovered y los ponemos en discovereds
				LivingEntity le;
				for (int i = 0; i < alLivings.size (); i++) {
					le = World.getLivingEntityByID (alLivings.get (i).getID ());
					if (le != null) {
						LivingEntity.removeLiving (le, false);
						World.getLivings (false).remove (le.getID ());

						LivingEntity.addLiving (le, true);
						World.getLivings (true).put (le.getID (), le);
					}
				}
				setBury (false);
			} else {
				// Si no hay living, miramos los casos bury
				if (isBury ()) {
					BuryData bd;
					// Bingo, hay bury, creamos un nuevo item en la celda
					ArrayList<BuryData> alBuryData = Game.getWorld ().getBuryData ();
					if (alBuryData != null && alBuryData.size () > 0) {
						boolean bExitBury = false;
						int iIndexBuryData = 0;
						while (!bExitBury) {
							bd = null;
							if (alBuryData.get (iIndexBuryData).getHash () != null) {
								bd = alBuryData.get (iIndexBuryData);
								// Obtenemos el item
								Point3DShort p3ds = Point3DShort.getPoolInstance (getCoordinates ());
								Integer iIniHeader = bd.getHash ().remove (p3ds);

								if (iIniHeader != null) {
									setBury (false);
									bExitBury = true;

									// Bingo
									String sIniHeader = UtilsIniHeaders.getStringIniHeader (iIniHeader.intValue ());
									if (sIniHeader != null) {
										ItemManagerItem imi = ItemManager.getItem (sIniHeader);
										if (imi != null && isMined ()) {
											boolean bWall = false;
											// buryItem
											if (!imi.isBuryDestroyItem ()) {
												ArrayList<String> alBuryItem = imi.getBuryItem ();
												String sBuryItem = null;
												if (alBuryItem != null && alBuryItem.size () > 0) {
													// Lanzamos porcentajes
													for (int p = 0; p < alBuryItem.size (); p++) {
														if (Utils.getRandomBetween (1, 100) <= imi.getBuryItemPCT ().get (p).intValue ()) {
															// Bingo
															sBuryItem = alBuryItem.get (p);
															break;
														}
													}
												}
												if (sBuryItem == null) {
													sBuryItem = imi.getIniHeader ();
												}

												ItemManagerItem imiItem = ItemManager.getItem (sBuryItem);

												if (imiItem != null && Item.isCellAvailableForItem (imiItem, getCoordinates (), false, false, false)) {
													bWall = imiItem.isWall ();
													Item newItem = Item.createItem (imiItem);
													if (newItem != null) {
														newItem.setOperative (true);
														newItem.setLocked (imi.isBuryLocked ());
														newItem.init (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z);
														setEntity (newItem);

														// Miramos si tiene text
														ArrayList<String> alTexts = bd.getHashTexts ().remove (p3ds);
														if (alTexts != null && alTexts.size () > 0) {
															World.getItemsText ().put (Integer.valueOf (newItem.getID ()), alTexts);
														}
													}
												}
											}

											// buryLivings
											if (!bWall && imi.getBuryLivings () != null && imi.getBuryLivings ().size () > 0) {
												String sLiving;
												for (int i = 0; i < imi.getBuryLivings ().size (); i++) {
													int iPCT = imi.getBuryLivingsPCT ().get (i).intValue ();
													if (Utils.getRandomBetween (1, 100) <= iPCT) {
														sLiving = imi.getBuryLivings ().get (i);
														LivingEntityManagerItem lemi = LivingEntityManager.getItem (sLiving);
														if (lemi != null && LivingEntity.isCellAllowed (getCoordinates ())) {
															World.addNewLiving (sLiving, lemi.getType (), true, getCoordinates ().x, getCoordinates ().y, getCoordinates ().z, true);
														}
													}
												}
											}
										}
									}
								}
							}

							// Seguimos
							iIndexBuryData++;
							if (iIndexBuryData >= alBuryData.size ()) {
								bExitBury = true;
							}
						}
					}
				}
			}

			MiniMapPanel.setMinimapReload (getCoordinates ().z);

			// Exploring hotpoint
			if (getAstarZoneID () != -1) {
				// Eliminamos de la lista de World las vecinas con el mismo ASZID
				Point3DShort coord = getCoordinates ();
				Point3DShort p3dTemp;
				Cell cell;
				if (coord.y > 0) {
					if (coord.x > 0) {
						p3dTemp = Point3DShort.getPoolInstance (coord.x - 1, coord.y - 1, coord.z);
						cell = World.getCell (p3dTemp);
						if (cell.getAstarZoneID () == getAstarZoneID () && cell.isHeroExploringPoint ()) {
							Game.getWorld ().getExploringHotPoints ().remove (p3dTemp);
							cell.setHeroExploringPoint (false);
						}
						Point3DShort.returnToPool (p3dTemp);
					}
					p3dTemp = Point3DShort.getPoolInstance (coord.x, coord.y - 1, coord.z);
					cell = World.getCell (p3dTemp);
					if (cell.getAstarZoneID () == getAstarZoneID () && cell.isHeroExploringPoint ()) {
						Game.getWorld ().getExploringHotPoints ().remove (p3dTemp);
						cell.setHeroExploringPoint (false);
					}
					Point3DShort.returnToPool (p3dTemp);
					if (coord.x < (World.MAP_WIDTH - 1)) {
						p3dTemp = Point3DShort.getPoolInstance (coord.x + 1, coord.y - 1, coord.z);
						cell = World.getCell (p3dTemp);
						if (cell.getAstarZoneID () == getAstarZoneID () && cell.isHeroExploringPoint ()) {
							Game.getWorld ().getExploringHotPoints ().remove (p3dTemp);
							cell.setHeroExploringPoint (false);
						}
						Point3DShort.returnToPool (p3dTemp);
					}
				}
				if (coord.x > 0) {
					p3dTemp = Point3DShort.getPoolInstance (coord.x - 1, coord.y, coord.z);
					cell = World.getCell (p3dTemp);
					if (cell.getAstarZoneID () == getAstarZoneID () && cell.isHeroExploringPoint ()) {
						Game.getWorld ().getExploringHotPoints ().remove (p3dTemp);
						cell.setHeroExploringPoint (false);
					}
					Point3DShort.returnToPool (p3dTemp);
				}
				if (coord.x < (World.MAP_WIDTH - 1)) {
					p3dTemp = Point3DShort.getPoolInstance (coord.x + 1, coord.y, coord.z);
					cell = World.getCell (p3dTemp);
					if (cell.getAstarZoneID () == getAstarZoneID () && cell.isHeroExploringPoint ()) {
						Game.getWorld ().getExploringHotPoints ().remove (p3dTemp);
						cell.setHeroExploringPoint (false);
					}
					Point3DShort.returnToPool (p3dTemp);
				}
				if (coord.y < (World.MAP_HEIGHT - 1)) {
					if (coord.x > 0) {
						p3dTemp = Point3DShort.getPoolInstance (coord.x - 1, coord.y + 1, coord.z);
						cell = World.getCell (p3dTemp);
						if (cell.getAstarZoneID () == getAstarZoneID () && cell.isHeroExploringPoint ()) {
							Game.getWorld ().getExploringHotPoints ().remove (p3dTemp);
							cell.setHeroExploringPoint (false);
						}
						Point3DShort.returnToPool (p3dTemp);
					}
					p3dTemp = Point3DShort.getPoolInstance (coord.x, coord.y + 1, coord.z);
					cell = World.getCell (p3dTemp);
					if (cell.getAstarZoneID () == getAstarZoneID () && cell.isHeroExploringPoint ()) {
						Game.getWorld ().getExploringHotPoints ().remove (p3dTemp);
						cell.setHeroExploringPoint (false);
					}
					Point3DShort.returnToPool (p3dTemp);
					if (coord.x < (World.MAP_WIDTH - 1)) {
						p3dTemp = Point3DShort.getPoolInstance (coord.x + 1, coord.y + 1, coord.z);
						cell = World.getCell (p3dTemp);
						if (cell.getAstarZoneID () == getAstarZoneID () && cell.isHeroExploringPoint ()) {
							Game.getWorld ().getExploringHotPoints ().remove (p3dTemp);
							cell.setHeroExploringPoint (false);
						}
						Point3DShort.returnToPool (p3dTemp);
					}
				}

				Game.getWorld ().getExploringHotPoints ().add (getCoordinates ());
				setHeroExploringPoint (true);

				if (getCoordinates ().z < (World.MAP_DEPTH - 1)) {
					World.getCell (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z + 1).setDiscovered (discovered);
				}
			}
		}

		// En todos los casos, miramos si cambia el flag de "shouldPaintUnder"
		setShouldPaintUnders (World.getCells (), getCoordinates ());
		// En todos los casos, miramos si cambia el flag de "digged"
		Point3DShort p3dCell = getCoordinates ();
		if (p3dCell.z < (World.MAP_DEPTH - 1)) {
			setDigged (World.getCell (p3dCell.x, p3dCell.y, p3dCell.z + 1).isMined ());
		}
	}


	public void setDiscoveredPregenerate (boolean discovered) {
		// Si no estaba descubierta de antes, le decimos al minimapa que se actualice
		if (discovered && !isDiscovered () && getCoordinates () != null) {
			MiniMapPanel.setMinimapReload (getCoordinates ().z);
		}

		if (discovered) {
			setFlags (getFlags () | FLAG_DISCOVERED);
		} else {
			setFlags (getFlags () & ~FLAG_DISCOVERED);
		}
	}


	/**
	 * Devuelve true si la celda tiene entity
	 * 
	 * @return true si la celda tiene entity
	 */
	public boolean hasEntity () {
		return entity != null;
	}


	public int getAstarZoneID () {
		// Miramos en la HASH
		Integer iReturn = Integer.valueOf (astarZoneID);
		int iFirstValue = astarZoneID;
		boolean bIterations = HASH_ASTAR_ZONE_RELATIONS.containsKey (iReturn);
		if (bIterations) {
			while (HASH_ASTAR_ZONE_RELATIONS.containsKey (iReturn)) {
				iReturn = HASH_ASTAR_ZONE_RELATIONS.get (iReturn);
			}

			setAstarZoneID (iReturn.intValue ());
			HASH_ASTAR_ZONE_RELATIONS.put (Integer.valueOf (iFirstValue), iReturn);
		}

		return iReturn.intValue ();
	}


	public void setAstarZoneID (int astarZoneID) {
		this.astarZoneID = astarZoneID;
	}


	/**
	 * Dada una casilla actualiza su A*zoneID según las adyacentes
	 * 
	 * @param p3d
	 */
	public static void mergeZoneID (Point3DShort p3d, boolean bFull) {
		mergeZoneID (p3d.x, p3d.y, p3d.z, bFull);
	}


	/**
	 * Comprueba que el array de IDs contenga el pasado- Usa la variable global neighborIDIndex para saber el tamaño
	 * 
	 * @param iID
	 * @return
	 */
	private static boolean neighborIDContainsID (int iID) {
		for (int i = 0; i < neighborIDsIndex; i++) {
			if (neighborIDs[i] == iID) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Devuelve los IDs de las casillas adyacentes que no sean -1, no mete repetidos. Las de abajo no hacen falta.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static void setNeighborIDs (int x, int y, int z, boolean bFull) {
		// ArrayList<Integer> alIDS = new ArrayList<Integer> ((bFull) ? 13 : 26);
		neighborIDsIndex = 0;

		Cell[][][] cells = World.getCells ();
		int iTmp;

		// Las 3 del norte
		if (y > 0) {
			if (x > 0) {
				iTmp = cells[x - 1][y - 1][z].getAstarZoneID ();
				if (iTmp != -1) {
					neighborIDs[neighborIDsIndex] = iTmp;
					neighborIDsIndex++;
				}
			}
			iTmp = cells[x][y - 1][z].getAstarZoneID ();
			if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
				neighborIDs[neighborIDsIndex] = iTmp;
				neighborIDsIndex++;
			}
			if (x < (World.MAP_WIDTH - 1)) {
				iTmp = cells[x + 1][y - 1][z].getAstarZoneID ();
				if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
					neighborIDs[neighborIDsIndex] = iTmp;
					neighborIDsIndex++;
				}
			}
		}
		// Oeste
		if (x > 0) {
			iTmp = cells[x - 1][y][z].getAstarZoneID ();
			if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
				neighborIDs[neighborIDsIndex] = iTmp;
				neighborIDsIndex++;
			}
		}
		// Las de arriba
		if (z > 0 && cells[x][y][z - 1].isMined ()) {
			// Las 3 del norte
			if (y > 0) {
				if (x > 0) {
					iTmp = cells[x - 1][y - 1][z - 1].getAstarZoneID ();
					if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
				}
				iTmp = cells[x][y - 1][z - 1].getAstarZoneID ();
				if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
					neighborIDs[neighborIDsIndex] = iTmp;
					neighborIDsIndex++;
				}
				if (x < (World.MAP_WIDTH - 1)) {
					iTmp = cells[x + 1][y - 1][z - 1].getAstarZoneID ();
					if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
				}
			}

			// Este
			if (x < (World.MAP_WIDTH - 1)) {
				iTmp = cells[x + 1][y][z - 1].getAstarZoneID ();
				if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
					neighborIDs[neighborIDsIndex] = iTmp;
					neighborIDsIndex++;
				}
			}

			// Las 3 del sur
			if (y < (World.MAP_HEIGHT - 1)) {
				if (x > 0) {
					iTmp = cells[x - 1][y + 1][z - 1].getAstarZoneID ();
					if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
				}
				iTmp = cells[x][y + 1][z - 1].getAstarZoneID ();
				if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
					neighborIDs[neighborIDsIndex] = iTmp;
					neighborIDsIndex++;
				}
				if (x < (World.MAP_WIDTH - 1)) {
					iTmp = cells[x + 1][y + 1][z - 1].getAstarZoneID ();
					if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
				}
			}

			// Oeste
			if (x > 0) {
				iTmp = cells[x - 1][y][z - 1].getAstarZoneID ();
				if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
					neighborIDs[neighborIDsIndex] = iTmp;
					neighborIDsIndex++;
				}
			}
		}

		if (Terrain.canGoUp (x, y, z)) {
			iTmp = cells[x][y][z - 1].getAstarZoneID ();
			if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
				neighborIDs[neighborIDsIndex] = iTmp;
				neighborIDsIndex++;
			}
		}

		if (!bFull) {
			// En caso de !bFull hay que mirar las celdas restantes y las 9 de abajo si hace falta
			// Este
			if (x < (World.MAP_WIDTH - 1)) {
				iTmp = cells[x + 1][y][z].getAstarZoneID ();
				if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
					neighborIDs[neighborIDsIndex] = iTmp;
					neighborIDsIndex++;
				}
			}

			// Las 3 del sur
			if (y < (World.MAP_HEIGHT - 1)) {
				if (x > 0) {
					iTmp = cells[x - 1][y + 1][z].getAstarZoneID ();
					if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
				}
				iTmp = cells[x][y + 1][z].getAstarZoneID ();
				if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
					neighborIDs[neighborIDsIndex] = iTmp;
					neighborIDsIndex++;
				}
				if (x < (World.MAP_WIDTH - 1)) {
					iTmp = cells[x + 1][y + 1][z].getAstarZoneID ();
					if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
				}
			}

			// Abajo
			if (Terrain.canGoDown (x, y, z)) {
				iTmp = cells[x][y][z + 1].getAstarZoneID ();
				if (iTmp != -1 && !neighborIDContainsID (iTmp)) {
					neighborIDs[neighborIDsIndex] = iTmp;
					neighborIDsIndex++;
				}
			}

			// Las de abajo
			if (z < (World.MAP_DEPTH - 1)) {
				// Las 3 del norte
				if (y > 0) {
					if (x > 0) {
						iTmp = cells[x - 1][y - 1][z + 1].getAstarZoneID ();
						if (iTmp != -1 && cells[x - 1][y - 1][z].isMined () && !neighborIDContainsID (iTmp)) {
							neighborIDs[neighborIDsIndex] = iTmp;
							neighborIDsIndex++;
						}
					}
					iTmp = cells[x][y - 1][z + 1].getAstarZoneID ();
					if (iTmp != -1 && cells[x][y - 1][z].isMined () && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
					if (x < (World.MAP_WIDTH - 1)) {
						iTmp = cells[x + 1][y - 1][z + 1].getAstarZoneID ();
						if (iTmp != -1 && cells[x + 1][y - 1][z].isMined () && !neighborIDContainsID (iTmp)) {
							neighborIDs[neighborIDsIndex] = iTmp;
							neighborIDsIndex++;
						}
					}
				}

				// Este
				if (x < (World.MAP_WIDTH - 1)) {
					iTmp = cells[x + 1][y][z + 1].getAstarZoneID ();
					if (iTmp != -1 && cells[x + 1][y][z].isMined () && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
				}

				// Las 3 del sur
				if (y < (World.MAP_HEIGHT - 1)) {
					if (x > 0) {
						iTmp = cells[x - 1][y + 1][z + 1].getAstarZoneID ();
						if (iTmp != -1 && cells[x - 1][y + 1][z].isMined () && !neighborIDContainsID (iTmp)) {
							neighborIDs[neighborIDsIndex] = iTmp;
							neighborIDsIndex++;
						}
					}
					iTmp = cells[x][y + 1][z + 1].getAstarZoneID ();
					if (iTmp != -1 && cells[x][y + 1][z].isMined () && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
					if (x < (World.MAP_WIDTH - 1)) {
						iTmp = cells[x + 1][y + 1][z + 1].getAstarZoneID ();
						if (iTmp != -1 && cells[x + 1][y + 1][z].isMined () && !neighborIDContainsID (iTmp)) {
							neighborIDs[neighborIDsIndex] = iTmp;
							neighborIDsIndex++;
						}
					}
				}

				// Oeste
				if (x > 0) {
					iTmp = cells[x - 1][y][z + 1].getAstarZoneID ();
					if (iTmp != -1 && cells[x - 1][y][z].isMined () && !neighborIDContainsID (iTmp)) {
						neighborIDs[neighborIDsIndex] = iTmp;
						neighborIDsIndex++;
					}
				}
			}
		}

		// Ponemos un -1 al final para saber que ya estamos
		neighborIDs[neighborIDsIndex] = -1;
	}


	/**
	 * Dada una casilla actualiza su A*zoneID según las adyacentes
	 * 
	 * @param x X
	 * @param y Y
	 * @param z Z
	 * @param bFull Indica si se está haciendo un merge de todo, en ese caso el getNeighbour no hace falta que mire todos los vecinos
	 */
	public static void mergeZoneID (int x, int y, int z, boolean bFull) {
		// Actualizamos el ID de zona A* según las casillas adyacentes
		setNeighborIDs (x, y, z, bFull);

		if (neighborIDsIndex == 0) {
			// Todos los IDs adyacentes son -1
			// Nuevo punto
			MAX_ASTAR_ZONE_ID++;
			World.getCell (x, y, z).setAstarZoneID (MAX_ASTAR_ZONE_ID);
		} else if (neighborIDsIndex == 1) {
			// Sólo hay una zona adyacente
			World.getCell (x, y, z).setAstarZoneID (neighborIDs[0]);
		} else {
			// IDs distintos, acabamos de abrir el punto que une varias zonas, actualizamos todas las celdas que tengan estos IDs

			int i = 1; // Empezamos por el 1 ya que la primera zona la dejaremos tal cual
			while (i < neighborIDsIndex) {
				HASH_ASTAR_ZONE_RELATIONS.put (neighborIDs[i], neighborIDs[0]);
				i++;
			}
			World.getCell (x, y, z).setAstarZoneID (neighborIDs[0]);
		}
	}


	/**
	 * Genera los A* zone IDs de todo el mapa
	 */
	public static void setAllZoneIDs () {
		// Ponemos todo a -1, el MAX_ZONE a 0 y actualizamos todo
		Cell[][][] cells = World.getCells ();
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				for (int z = 0; z < World.MAP_DEPTH; z++) {
					cells[x][y][z].setAstarZoneID (-1);
				}
			}
		}

		MAX_ASTAR_ZONE_ID = 0;
		HASH_ASTAR_ZONE_RELATIONS.clear ();

		Cell cell;
		for (int z = 0; z < World.MAP_DEPTH; z++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				for (int x = 0; x < World.MAP_WIDTH; x++) {
					cell = cells[x][y][z];
					if (LivingEntity.isCellAllowed (cell)) {
						mergeZoneID (x, y, z, true);
					}
				}
			}
		}

		// Ponemos el false el check de recheckear
		World.setRecheckASZID (false);
	}


	public Point3DShort getBuildingCoordinates () {
		return buildingCoordinates;
	}


	public void setBuildingCoordinates (Point3DShort buildingCoordinates) {
		this.buildingCoordinates = buildingCoordinates;
	}


	public boolean hasBuilding () {
		return getBuildingCoordinates () != null;
	}


	public static int getDepth (int x, int y, int z) {
		return depths[x][y] + (World.MAP_DEPTH - z);
		// return ((getCoordinates ().y - MainPanel.depthYMin) * MainPanel.maxTilesWidthHeight + (MainPanel.maxTilesWidthHeight - 1 - (getCoordinates ().x - MainPanel.depthXMin))) + (World.MAP_DEPTH - getCoordinates ().z);
	}


	public static void generateDepths () {
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				depths[x][y] = y * World.MAP_HEIGHT * 1 + (World.MAP_WIDTH - 1 - x) * 1;
			}
		}
	}


	public boolean hasItem () {
		return entity != null && entity instanceof Item;
	}


	public void setStockPileID (int iSPID) {
		this.stockPileID = iSPID;

		if (stockPileID == 0) {
			// Ha borrado la pile de esta celda, si había un item lo metemos en la lista de hauling
			Game.getWorld ().addItemToBeHauled (getItem ());
		}
	}


	public int getStockPileID () {
		return stockPileID;
	}


	public boolean hasStockPile () {
		return stockPileID > 0;
	}


	public void setCave (boolean bCave) {
		if (bCave) {
			setFlags (getFlags () | FLAG_CAVE);
		} else {
			setFlags (getFlags () & ~FLAG_CAVE);
		}
	}


	public boolean isCave () {
		return (getFlags () & FLAG_CAVE) > 0;
	}


	public void setHeroExploringPoint (boolean bHeroExploringPoint) {
		if (bHeroExploringPoint) {
			setFlags (getFlags () | FLAG_HERO_EXPLORING_POINT);
		} else {
			setFlags (getFlags () & ~FLAG_HERO_EXPLORING_POINT);
		}
	}


	public boolean isHeroExploringPoint () {
		return (getFlags () & FLAG_HERO_EXPLORING_POINT) > 0;
	}


	public void setShouldPaintUnder (boolean bShouldPaintUnder) {
		if (bShouldPaintUnder) {
			setFlags (getFlags () | FLAG_SHOULD_PAINT_UNDER);
		} else {
			setFlags (getFlags () & ~FLAG_SHOULD_PAINT_UNDER);
		}
	}


	public static void setShouldPaintUnders (Cell[][][] cells, Point3DShort p3d) {
		if (p3d != null) {
			setShouldPaintUnders (cells, p3d.x, p3d.y, p3d.z);
		}
	}


	public static void setShouldPaintUnders (Cell[][][] cells, short x, short y, short z) {
		Cell cell = cells[x][y][z];
		cell.setShouldPaintUnder (checkPaintUnder (cells, x, y, z));
		if (y > 0) {
			cell = cells[x][y - 1][z];
			cell.setShouldPaintUnder (checkPaintUnder (cells, x, y - 1, z)); // Celda norte
		}
		if (x < (World.MAP_WIDTH - 1)) {
			cell = cells[x + 1][y][z];
			cell.setShouldPaintUnder (checkPaintUnder (cells, x + 1, y, z)); // Celda este
		}
	}


	public boolean isShouldPaintUnder () {
		return (getFlags () & FLAG_SHOULD_PAINT_UNDER) > 0;
	}


	public void setDigged (boolean bDigged) {
		if (bDigged) {
			setFlags (getFlags () | FLAG_DIGGED);
		} else {
			setFlags (getFlags () & ~FLAG_DIGGED);
		}
	}


	public boolean isDigged () {
		return (getFlags () & FLAG_DIGGED) > 0;
	}


	public void setShadow (boolean bShadow) {
		if (bShadow) {
			setFlags (getFlags () | FLAG_SHADOW);
		} else {
			setFlags (getFlags () & ~FLAG_SHADOW);
		}
	}


	public boolean isShadow (boolean bCheckGlobalEvents) {
		if (bCheckGlobalEvents && Game.getWorld ().getGlobalEvents ().isShadows ()) {
			return true;
		} else {
			return (getFlags () & FLAG_SHADOW) > 0;
		}
	}


	public void setLightsFalse () {
		setLightRedFull (false);
		setLightGreenFull (false);
		setLightBlueFull (false);

		setLightRedHalf (false);
		setLightGreenHalf (false);
		setLightBlueHalf (false);
	}


	public void setLights (ItemManagerItem imi) {
		if (imi.getLightRed () == ItemManagerItem.LIGHT_I_FULL) {
			setLightRedFull (true);
		} else if (imi.getLightRed () == ItemManagerItem.LIGHT_I_HALF) {
			setLightRedHalf (true);
		}

		if (imi.getLightGreen () == ItemManagerItem.LIGHT_I_FULL) {
			setLightGreenFull (true);
		} else if (imi.getLightGreen () == ItemManagerItem.LIGHT_I_HALF) {
			setLightGreenHalf (true);
		}

		if (imi.getLightBlue () == ItemManagerItem.LIGHT_I_FULL) {
			setLightBlueFull (true);
		} else if (imi.getLightBlue () == ItemManagerItem.LIGHT_I_HALF) {
			setLightBlueHalf (true);
		}
	}


	public void setLightRedFull (boolean bLight) {
		if (bLight) {
			setFlags (getFlags () | FLAG_LIGHT_RFULL);
		} else {
			setFlags (getFlags () & ~FLAG_LIGHT_RFULL);
		}
	}


	public boolean isLightRedFull () {
		return (getFlags () & FLAG_LIGHT_RFULL) > 0;
	}


	public void setLightGreenFull (boolean bLight) {
		if (bLight) {
			setFlags (getFlags () | FLAG_LIGHT_GFULL);
		} else {
			setFlags (getFlags () & ~FLAG_LIGHT_GFULL);
		}
	}


	public boolean isLightGreenFull () {
		return (getFlags () & FLAG_LIGHT_GFULL) > 0;
	}


	public void setLightBlueFull (boolean bLight) {
		if (bLight) {
			setFlags (getFlags () | FLAG_LIGHT_BFULL);
		} else {
			setFlags (getFlags () & ~FLAG_LIGHT_BFULL);
		}
	}


	public boolean isLightBlueFull () {
		return (getFlags () & FLAG_LIGHT_BFULL) > 0;
	}


	public void setLightRedHalf (boolean bLight) {
		if (bLight) {
			setFlags (getFlags () | FLAG_LIGHT_RHALF);
		} else {
			setFlags (getFlags () & ~FLAG_LIGHT_RHALF);
		}
	}


	public boolean isLightRedHalf () {
		return (getFlags () & FLAG_LIGHT_RHALF) > 0;
	}


	public void setLightGreenHalf (boolean bLight) {
		if (bLight) {
			setFlags (getFlags () | FLAG_LIGHT_GHALF);
		} else {
			setFlags (getFlags () & ~FLAG_LIGHT_GHALF);
		}
	}


	public boolean isLightGreenHalf () {
		return (getFlags () & FLAG_LIGHT_GHALF) > 0;
	}


	public void setLightBlueHalf (boolean bLight) {
		if (bLight) {
			setFlags (getFlags () | FLAG_LIGHT_BHALF);
		} else {
			setFlags (getFlags () & ~FLAG_LIGHT_BHALF);
		}
	}


	public boolean isLightBlueHalf () {
		return (getFlags () & FLAG_LIGHT_BHALF) > 0;
	}


	public void setBury (boolean bBury) {
		if (bBury) {
			setFlags (getFlags () | FLAG_BURY);
		} else {
			setFlags (getFlags () & ~FLAG_BURY);
		}
	}


	public boolean isBury () {
		return (getFlags () & FLAG_BURY) > 0;
	}


	public void setFluidCheckList (boolean bFluidCheckList) {
		if (bFluidCheckList) {
			setFlags (getFlags () | FLAG_FLUID_CHECK_LIST);
		} else {
			setFlags (getFlags () & ~FLAG_FLUID_CHECK_LIST);
		}
	}


	public boolean isFluidCheckList () {
		return (getFlags () & FLAG_FLUID_CHECK_LIST) > 0;
	}


	public void setOpen (boolean bOpen) {
		if (bOpen) {
			setFlags (getFlags () | FLAG_OPEN);
		} else {
			setFlags (getFlags () & ~FLAG_OPEN);
		}
	}


	public boolean isOpen () {
		return (getFlags () & FLAG_OPEN) > 0;
	}


	public void setBlink (boolean bBlink) {
		if (bBlink) {
			setFlags (getFlags () | FLAG_BLINK);
		} else {
			setFlags (getFlags () & ~FLAG_BLINK);
		}
	}


	public boolean isBlink () {
		return (getFlags () & FLAG_BLINK) > 0;
	}


	public boolean isLight () {
		return (getFlags () & (FLAG_LIGHT_RFULL | FLAG_LIGHT_GFULL | FLAG_LIGHT_BFULL | FLAG_LIGHT_RHALF | FLAG_LIGHT_GHALF | FLAG_LIGHT_BHALF)) > 0;
	}


	public void setBlocky (boolean bBlocky) {
		if (bBlocky) {
			setFlags (getFlags () | FLAG_BLOCKY);
		} else {
			setFlags (getFlags () & ~FLAG_BLOCKY);
		}
	}


	public boolean isBlocky () {
		return (getFlags () & FLAG_BLOCKY) > 0;
	}


	public void setMined (boolean bMined) {
		if (bMined) {
			setFlags (getFlags () | FLAG_MINED);
		} else {
			setFlags (getFlags () & ~FLAG_MINED);
		}
	}


	public boolean isMined () {
		return (getFlags () & FLAG_MINED) > 0;
	}


	public void setZoneID (int zoneID) {
		this.zoneID = zoneID;
	}


	public int getZoneID () {
		return zoneID;
	}


	public boolean hasZone () {
		return getZoneID () > 0;
	}


	public Point3DShort getCoordinates () {
		return coordinates;
	}


	public void setCoordinates (Point3DShort coordinates) {
		this.coordinates = coordinates;
	}


	public boolean isDiggable () {
		return isDiggable (this);
	}


	public boolean isEmpty () {
		return !hasBuilding () && !hasItem () && !hasEntity ();
	}


	/**
	 * Indica si la celda es allowed para dig. Sin fluidos, sin diggar, descubierta, sin stockpile, sin zona, minada, sin edificio
	 * 
	 * @param cell
	 * @return si la celda es allowed para dig.
	 */
	public static boolean isDiggable (Cell cell) {
		if (cell.getCoordinates ().z < (World.MAP_DEPTH - 2) && !cell.isDigged () && cell.isDiscovered () && !cell.hasStockPile () && !cell.hasZone ()) {
			if (!cell.getTerrain ().hasFluids () && cell.isMined ()) {
				if (cell.isEmpty ()) {
					return true;
				}
			}
		}

		return false;
	}


	public void setFlagOrders (boolean flagOrders) {
		if (flagOrders) {
			setFlags (getFlags () | FLAG_ORDERS);
		} else {
			setFlags (getFlags () & ~FLAG_ORDERS);
		}
	}


	public boolean isFlagOrders () {
		return (getFlags () & FLAG_ORDERS) > 0;
	}


	public void setFlagPatrol (boolean flagPatrol) {
		if (flagPatrol) {
			setFlags (getFlags () | FLAG_PATROL_POINT);
		} else {
			setFlags (getFlags () & ~FLAG_PATROL_POINT);
		}
	}


	public boolean isFlagPatrol () {
		return (getFlags () & FLAG_PATROL_POINT) > 0;
	}


	public static int getCellWalkNeeded (Point3DShort p3d) {
		return getCellWalkNeeded (p3d.x, p3d.y, p3d.z);
	}


	public static int getCellWalkNeeded (int x, int y, int z) {
		Cell cell = World.getCell (x, y, z);
		Item it = cell.getItem ();
		if (it != null && it.isOperative () && it.isLocked ()) {
			return ItemManager.getItem (it.getIniHeader ()).getFloorWalkSpeed ();
		} else {
			return 100;
		}
	}


	/**
	 * NO Hace caer items. Destruye edificios NO hace caer livings, eso ya lo hacen ellas solas en su nextturn
	 */
	public void fallThings () {
		if (isDigged ()) {
			// Miramos la casilla de abajo (en teoría no hace falta pq si o si estará mined)
			// Buildings
			if (getBuildingCoordinates () != null) {
				// Tarea de petar edificio
				Task task = new Task (Task.TASK_DESTROY_BUILDING);
				task.setPointIni (getBuildingCoordinates ().toPoint3D ());
				Game.getWorld ().getTaskManager ().addTask (task);
			}
		}
	}


	public static void generatePaintUnder (Cell[][][] cells) {
		Item item;
		ItemManagerItem imi;
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				for (int z = 0; z < (World.MAP_DEPTH - 1); z++) { // La última layer es false
					// Si tiene item reseteamos el blocky
					item = cells[x][y][z].getItem ();
					if (item != null) {
						imi = ItemManager.getItem (item.getIniHeader ());
						cells[x][y][z].setBlocky (imi != null && imi.isBlocky ());
					}
					cells[x][y][z].setShouldPaintUnder (checkPaintUnder (cells, x, y, z));
				}
			}
		}
	}


	public static boolean checkPaintUnder (Cell[][][] cells, int x, int y, int z) {
		if (z >= (World.MAP_DEPTH - 1)) {
			// Última layer es false
			return false;
		}

		Cell cellTmp = cells[x][y][z];
		// if (cellTmp.isDiscovered () && !cellTmp.isBlocky () && cellTmp.isMined () && cellTmp.getTerrain ().getFluidCount () <= 4) {
		if (cellTmp.isDiscovered () && !cellTmp.isBlocky () && cellTmp.getTerrain ().getFluidCount () <= 4) {
			return true;
		}

		// Fluids encima de un allowFluids
		if (cellTmp.getTerrain ().getFluidCount () > 4) {
			// Miramos si hay un item allowFluids
			Item item = cellTmp.getItem ();
			if (item != null && ItemManager.getItem (item.getIniHeader ()).isAllowFluids ()) {
				return true;
			}
		}

		if (x < (World.MAP_WIDTH - 1) && y > 0 && z < (World.MAP_DEPTH - 1)) {
			cellTmp = cells[x + 1][y - 1][z + 1];
			if (cellTmp.hasBuilding ()) {
				return true;
			}
		}

		return false;
	}


	public static void generateShadows () {
		Cell[][][] cells = World.getCells ();
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				for (int z = (World.MAP_DEPTH - 2); z >= 0; z--) {
					generateShadow (cells, x, y, z);
				}
			}
		}
	}


	public static void generateAllLights () {
		Cell[][][] cells = World.getCells ();
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				for (int z = (World.MAP_DEPTH - 1); z >= 0; z--) {
					cells[x][y][z].setLightsFalse ();
				}
			}
		}
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				for (int z = (World.MAP_DEPTH - 1); z >= 0; z--) {
					generateLights (cells, x, y, z);
				}
			}
		}
	}


	public static void generateAllOpen () {
		Cell[][][] cells = World.getCells ();
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				generateOpen (cells, x, y);
			}
		}
	}


	public static void generateOpen (Cell[][][] cells, int x, int y) {
		boolean bOpen = true;
		for (int z = 0; z < World.MAP_DEPTH; z++) {
			cells[x][y][z].setOpen (bOpen);

			if (bOpen) {
				cells[x][y][z].setOpen (true);

				bOpen = cells[x][y][z].isMined ();

				if (bOpen) {
					Item item = cells[x][y][z].getItem ();
					if (item != null) {
						ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
						if (imi != null && (imi.isWall () || imi.isWallConnector ())) {
							bOpen = false;
						}
					}
				}
			}
		}
	}


	/**
	 * Regenera las celdas con el light que toque
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param iRadius
	 */
	public static void generateLightsItemRemovedCellMined (int x, int y, int z, int iRadius) {
		// Primero quitamos el light de todas las celdas del cubo
		int iXMin = x - iRadius;
		int iXMax = x + iRadius;
		int iYMin = y - iRadius;
		int iYMax = y + iRadius;
		int iZMin = z - iRadius;
		int iZMax = z + iRadius;
		if (iXMin < 0) {
			iXMin = 0;
		}
		if (iYMin < 0) {
			iYMin = 0;
		}
		if (iZMin < 0) {
			iZMin = 0;
		}
		if (iXMax >= World.MAP_WIDTH) {
			iXMax = (World.MAP_WIDTH - 1);
		}
		if (iYMax >= World.MAP_HEIGHT) {
			iYMax = (World.MAP_HEIGHT - 1);
		}
		if (iZMax >= World.MAP_DEPTH) {
			iZMax = (World.MAP_DEPTH - 1);
		}

		Cell[][][] cells = World.getCells ();

		for (int iX = iXMin; iX <= iXMax; iX++) {
			for (int iY = iYMin; iY <= iYMax; iY++) {
				for (int iZ = iZMin; iZ <= iZMax; iZ++) {
					cells[iX][iY][iZ].setLightsFalse ();
				}
			}
		}

		// Ahora, teniendo en cuenta el radio máximo de todos los objetos que producen luz, miramos las celdas a iluminar
		iXMin -= ItemManagerItem.MAX_LIGHT_RADIUS;
		iXMax += ItemManagerItem.MAX_LIGHT_RADIUS;
		iYMin -= ItemManagerItem.MAX_LIGHT_RADIUS;
		iYMax += ItemManagerItem.MAX_LIGHT_RADIUS;
		iZMin -= ItemManagerItem.MAX_LIGHT_RADIUS;
		iZMax += ItemManagerItem.MAX_LIGHT_RADIUS;
		if (iXMin < 0) {
			iXMin = 0;
		}
		if (iYMin < 0) {
			iYMin = 0;
		}
		if (iZMin < 0) {
			iZMin = 0;
		}
		if (iXMax >= World.MAP_WIDTH) {
			iXMax = (World.MAP_WIDTH - 1);
		}
		if (iYMax >= World.MAP_HEIGHT) {
			iYMax = (World.MAP_HEIGHT - 1);
		}
		if (iZMax >= World.MAP_DEPTH) {
			iZMax = (World.MAP_DEPTH - 1);
		}

		for (int iX = iXMin; iX <= iXMax; iX++) {
			for (int iY = iYMin; iY <= iYMax; iY++) {
				for (int iZ = iZMin; iZ <= iZMax; iZ++) {
					generateLights (cells, iX, iY, iZ);
				}
			}
		}
	}


	/**
	 * Dada una celda mira si tiene un item de light y regenera las celdas adyacentes
	 * 
	 * @param cells
	 */
	public static void generateLights (Cell[][][] cells, int x, int y, int z) {
		generateLights (cells, x, y, z, false);
		generateLights (cells, x, y, z, true);
	}


	public static void generateLights (Cell[][][] cells, int x, int y, int z, boolean bProjectiles) {
		Cell cell = cells[x][y][z];
		ItemManagerItem imi = null;

		if (bProjectiles && Projectile.getLocations () != null) {
			if (Projectile.getLocations ()[x][y][z] > 0) {
				Projectile projectile;
				for (int c = 0; c < Game.getWorld ().getProjectiles ().size (); c++) {
					projectile = Game.getWorld ().getProjectiles ().get (c);
					if (projectile.getCoordinates ().x == x && projectile.getCoordinates ().y == y && projectile.getCoordinates ().z == z) {
						imi = ItemManager.getItem (projectile.getIniHeader ());
						break;
					}
				}
			}
		} else {
			Item item = cell.getItem ();
			if (item != null) {
				imi = ItemManager.getItem (item.getIniHeader ());
			}
		}

		if (imi != null) {
			if (imi.getLightRadius () != 0) {
				// Vamos a generar las lights
				int iRadius = imi.getLightRadius ();
				cell.setLights (imi);

				// Trazamos lineas a todos los bordes de un cuadrado, con radio pasado
				// Todas las casillas de las lineas (y las que están por arriba y abajo, teniendo en cuenta el radio) tendrán luz, hasta que lleguen a un non-mined o wall
				int iX, iY;
				// Norte del cuadrado
				iY = y - iRadius;
				if (iY < 0) {
					iY = 0;
				}

				for (iX = (x - iRadius + 1); iX <= (x + iRadius - 1); iX++) {
					if (iX >= 0 && iX < World.MAP_WIDTH) {
						// Linea recta
						Utils.bresenhamLineLight (x, y, iX, iY, z, iRadius, imi);
					}
				}

				// Sur del cuadrado
				iY = y + iRadius;
				if (iY >= World.MAP_HEIGHT) {
					iY = (World.MAP_HEIGHT - 1);
				}

				for (iX = (x - iRadius + 1); iX <= (x + iRadius - 1); iX++) {
					if (iX >= 0 && iX < World.MAP_WIDTH) {
						// Linea recta
						Utils.bresenhamLineLight (x, y, iX, iY, z, iRadius, imi);
					}
				}

				// Este del cuadrado
				iX = x + iRadius;
				if (iX >= World.MAP_WIDTH) {
					iX = (World.MAP_WIDTH - 1);
				}

				for (iY = (y - iRadius + 1); iY <= (y + iRadius - 1); iY++) {
					if (iY >= 0 && iY < World.MAP_HEIGHT) {
						// Linea recta
						Utils.bresenhamLineLight (x, y, iX, iY, z, iRadius, imi);
					}
				}

				// Oeste del cuadrado
				iX = x - iRadius;
				if (iX < 0) {
					iX = 0;
				}

				for (iY = (y - iRadius + 1); iY <= (y + iRadius - 1); iY++) {
					if (iY >= 0 && iY < World.MAP_HEIGHT) {
						// Linea recta
						Utils.bresenhamLineLight (x, y, iX, iY, z, iRadius, imi);
					}
				}

				// Diagonales sin llegar al final
				// Noroeste
				iX = x - iRadius + 1;
				iY = y - iRadius + 1;
				while (iX < 0 || iY < 0) {
					iX++;
					iY++;
				}
				Utils.bresenhamLineLight (x, y, iX, iY, z, iRadius, imi);

				// Noreste
				iX = x + iRadius - 1;
				iY = y - iRadius + 1;
				while (iX >= (World.MAP_WIDTH) || iY < 0) {
					iX--;
					iY++;
				}
				Utils.bresenhamLineLight (x, y, iX, iY, z, iRadius, imi);

				// Suroeste
				iX = x - iRadius + 1;
				iY = y + iRadius - 1;
				while (iX < 0 || iY >= (World.MAP_HEIGHT)) {
					iX++;
					iY--;
				}
				Utils.bresenhamLineLight (x, y, iX, iY, z, iRadius, imi);

				// Sureste
				iX = x + iRadius - 1;
				iY = y + iRadius - 1;
				while (iX >= (World.MAP_WIDTH) || iY >= (World.MAP_HEIGHT)) {
					iX--;
					iY--;
				}
				Utils.bresenhamLineLight (x, y, iX, iY, z, iRadius, imi);
			}
		}
	}


	/**
	 * Dada una celda busca la celda destino a la que hace sombra
	 * 
	 * @param cells
	 */
	public static void generateShadow (Cell[][][] cells, int x, int y, int z) {
		Cell cell = cells[x][y][z];
		boolean bMakeShadow = !cell.isMined ();
		if (!bMakeShadow) {
			// No hace sombra de terreno, miramos si tiene un item que haga sombra
			Item item = cell.getItem ();
			if (item != null && ItemManager.getItem (item.getIniHeader ()).isWall ()) {
				bMakeShadow = true;
			}
		}

		if (bMakeShadow) {
			// Celda no minada (o con item que hace sombra), hace sombra, metemos sombras hasta el final
			boolean bFinish = false;
			while (!bFinish) {
				x++;
				z++;
				if (x < World.MAP_WIDTH && z < World.MAP_DEPTH) {
					cells[x][y][z].setShadow (true);
				} else {
					bFinish = true;
				}
			}
		}
	}


	/**
	 * Dada una celda regenera las shadow de toda la diagonal
	 * 
	 * @param cells
	 */
	public static void generateFullShadows (int x, int y, int z) {
		Cell[][][] cells = World.getCells ();

		// Nos situamos en la primera celda
		int iMin = Math.min (x, z);
		x -= iMin;
		z -= iMin;

		boolean bPutShadowOnNext = false;
		while (x < (World.MAP_WIDTH) && z < (World.MAP_DEPTH)) {
			if (bPutShadowOnNext) {
				cells[x][y][z].setShadow (true);
			} else {
				Cell cell = cells[x][y][z];
				// Aquí sólo entra en las primeras iteraciones
				cell.setShadow (false);
				if (!cell.isMined ()) {
					bPutShadowOnNext = true;
				} else {
					// Celda minada, miramos si hay item que hace sombra
					Item item = cell.getItem ();
					if (item != null && ItemManager.getItem (item.getIniHeader ()).isWall ()) {
						bPutShadowOnNext = true;
					}
				}
			}

			x++;
			z++;
		}
	}


	/**
	 * Create a new terrain, used on terraforming, when a fake item dies
	 * @param p3d
	 * @param sTerrainID
	 */
	public static void createTerrain (Point3DShort p3d, String sTerrainID) {
		TerrainManagerItem tmi = TerrainManager.getItem (sTerrainID);
		if (tmi != null) {
			Cell cell = World.getCell (p3d);
			int x = p3d.x;
			int y = p3d.y;
			int z = p3d.z;

//			cell.getTerrain ().setTerrainID (TerrainManagerItem.TERRAIN_AIR_ID);
//			cell.getTerrain ().setTerrainTileID(TerrainManagerItem.TERRAIN_AIR_ID * TerrainManager.SLOPES_INIHEADER.length);
            cell.setTerrain (new Terrain (sTerrainID));
			cell.getTerrain ().setMineTurns (tmi.getMineTurns ());

			boolean bMined = cell.getTerrain ().getMineTurns () <= 0;
			cell.setMined (bMined);
			if (z > 0) {
				Cell cellUpper = World.getCell (x, y, z - 1);
				cellUpper.setDigged (bMined);
			}

			if (cell.isMined ()) {
				cell.setBlocky (false);
			} else {
				cell.setBlocky (TerrainManager.getItemByID (cell.getTerrain ().getTerrainID ()).isBlocky ());
			}

			cell.setFlagOrders (false);
			setShouldPaintUnders (World.getCells (), cell.getCoordinates ());
			// Shadows
			generateFullShadows (x, y, z);
			// Open cell
			Cell.generateOpen (World.getCells (), x, y);
			// Connectors
            for (short i = -1; i <= 1; i++) {
                for (short j = -1; j <= 1; j++) {
                    for (short k = 0; k <= 1; k++) {
                        Terrain.checkSlope(World.getCells(), (short) (x + i), (short) (y + j), (short) (z + k));
//                        Game.getWorld().addFluidCellToProcess(x + i, y + j, z + k, false);
                    }
                }
            }
            // Minimap reload
            MiniMapPanel.setMinimapReload(z);
            MiniMapPanel.setMinimapReload(z - 1);
            // ShouldPaintUnders 2 (potser pot anar a dalt, per si acàs no li poso :D )
            if (z < (World.MAP_DEPTH - 1)) {
                setShouldPaintUnders(World.getCells(), (short) x, (short) y, (short) (z + 1));
            }
		}
	}


	public static void generateDiggedsMinedsAndBlockys (Cell[][][] cells) {
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				for (int z = 0; z < (World.MAP_DEPTH - 1); z++) {
					cells[x][y][z].setMined (cells[x][y][z].getTerrain ().getMineTurns () <= 0);
					cells[x][y][z].setDigged (cells[x][y][z + 1].getTerrain ().getMineTurns () <= 0);
					if (cells[x][y][z].isMined ()) {
						cells[x][y][z].setBlocky (false);
					} else {
						cells[x][y][z].setBlocky (TerrainManager.getItemByID (cells[x][y][z].getTerrain ().getTerrainID ()).isBlocky ());
					}
				}
			}
		}

		// Mineds de la última
		int z = World.MAP_DEPTH - 1;
		for (int x = 0; x < World.MAP_WIDTH; x++) {
			for (int y = 0; y < World.MAP_HEIGHT; y++) {
				cells[x][y][z].setMined (cells[x][y][z].getTerrain ().getMineTurns () <= 0);
				if (cells[x][y][z].isMined ()) {
					cells[x][y][z].setBlocky (false);
				} else {
					cells[x][y][z].setBlocky (TerrainManager.getItemByID (cells[x][y][z].getTerrain ().getTerrainID ()).isBlocky ());
				}
			}
		}
	}


	@SuppressWarnings("unchecked")
	public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
		if (Game.SAVEGAME_LOADING_VERSION <= Game.SAVEGAME_V10b) {
			flags = (int) in.readShort ();
		} else {
			flags = in.readInt ();
		}
		terrain = (Terrain) in.readObject ();
		entity = (Entity) in.readObject ();
		livings = (ArrayList<LivingEntity>) in.readObject ();
		stockPileID = in.readInt ();
		zoneID = in.readInt ();
	}


	public void writeExternal (ObjectOutput out) throws IOException {
		out.writeInt (flags);
		out.writeObject (terrain);
		out.writeObject (entity);
		out.writeObject (livings);
		out.writeInt (stockPileID);
		out.writeInt (zoneID);
	}
}
