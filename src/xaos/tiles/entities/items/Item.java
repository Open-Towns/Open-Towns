package xaos.tiles.entities.items;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import xaos.TownsProperties;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.campaign.TutorialTrigger;
import xaos.data.HateData;
import xaos.effects.EffectManager;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.generator.MapGenerator;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.MessagesPanel;
import xaos.panels.MiniMapPanel;
import xaos.panels.menus.SmartMenu;
import xaos.stockpiles.Stockpile;
import xaos.tiles.Cell;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.entities.living.enemies.Enemy;
import xaos.tiles.terrain.Terrain;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.utils.UtilsIniHeaders;
import xaos.zones.Zone;


public class Item extends Entity implements Externalizable {

	private static final long serialVersionUID = 5447839029431730457L;

	// Estos 3 se usan para buscar un item, indicando para cada cosa (locked, operative) si tiene que estar a true a false o que no importa
	public final static int SEARCH_DOESNTMATTER = 0;
	public final static int SEARCH_FALSE = 1;
	public final static int SEARCH_TRUE = 2;

	public final static int FACE_WEST = 0;
	public final static int FACE_NORTH = 1;
	public final static int FACE_EAST = 2;
	public final static int FACE_SOUTH = 3;

	// Flags (operativo y locked de momento)
	private final static byte FLAG_OPERATIVE = 1; // 00000001 (Indica si está operativo)
	private final static byte FLAG_LOCKED = 2; // 00000010 (Indica si está bloqueado en una celda (vamos, que no se puede move para llevarlo a una stockpile por ejemplo))
	public final static byte FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED = 4;
	public final static byte FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED = 8;
	public final static byte FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED = 16;
	public final static byte FLAG_FACING_BIT_0 = 32;
	public final static byte FLAG_FACING_BIT_1 = 64; // 00 - west, 01 - east, 10 - north, 11 - south
	private byte flags;

	/**
	 * Número de items de cada tipo NO LOCKEDS. Se guarda key->iniheader_int Value->Lista de item IDs
	 */
	private static HashMap<Integer, ArrayList<Integer>> mapItems = new HashMap<Integer, ArrayList<Integer>> ();
	/**
	 * Número de items de cada tipo LOCKEDS. Se guarda key->iniheader_int Value->Lista de item IDs
	 */
	private static HashMap<Integer, ArrayList<Integer>> mapItemsLocked = new HashMap<Integer, ArrayList<Integer>> ();

	/**
	 * Son los prerequisitos para construirse, irá desapareciendo durante la construcción Cuando lleguen a 0 el item pasa a estar operativo
	 */
	private ArrayList<String> prerequisites;

	// Age (Edad, para saber cuando muere)
	private int age; // Edad actual del item
	private int maxAge; // Edad máxima del item

	// HPs
	private int hp; // Puntos de vida, si llega a 0 palma
	private int maxHp; // Puntos de vida máximos (se usa para sacar un mensaje a la primera hostia que le meten)

	// Spawns
	private int spawnTurns;

	// Trampas
	private int trapCooldown;


	public Item () {
		super ();
	}


	public Item (String iniHeader) {
		super (iniHeader);

		// setTileName (tileName);
		setAge (0);

		ItemManagerItem imi = ItemManager.getItem (getIniHeader ());

		// Age
		setMaxAge (Utils.launchDice (imi.getMaxAge ()));
		// Eventos para el age
		if (Game.getWorld () != null && Game.getWorld ().getEvents () != null) {
			for (int i = 0; i < Game.getWorld ().getEvents ().size (); i++) {
				EventManagerItem emi = EventManager.getItem (Game.getWorld ().getEvents ().get (i).getEventID ());
				if (emi != null && emi.getItems () != null && emi.getItems ().size () > 0 && emi.getItemsMaxAgePCT () != null && emi.getItems ().size () == emi.getItemsMaxAgePCT ().size ()) {
					// Miramos si tiene items
					int iIndex = emi.getItems ().indexOf (iniHeader);
					if (iIndex != -1) {
						int iPCT = Utils.launchDice (emi.getItemsMaxAgePCT ().get (iIndex));
						if (iPCT < 1) {
							iPCT = 1;
						}
						setMaxAge ((getMaxAge () * iPCT) / 100);
					}
				}
			}
		}

		// HPs
		setHp (Utils.launchDice (imi.getHp ()));
		if (getHp () < 1) {
			setHp (1);
		}
		setMaxHp (getHp ());

		// Spawn
		if (imi.getSpawn () == null) {
			setSpawnTurns (-1);
		} else {
			setSpawnTurns (Utils.launchDice (imi.getSpawnTurns ()));
		}

		// Trampas
		if (imi.isTrap ()) {
			setTrapCooldown (Utils.launchDice (imi.getTrapCooldown ()));
			if (getTrapCooldown () < 0) {
				setTrapCooldown (0);
			}
		} else {
			setTrapCooldown (-1);
		}

		// Wall connector status
		if (imi.isWallConnector ()) {
			setWallConnectorStatus (FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED, false);
		}

		refreshTransients ();
	}


	public void refreshTransients () {
		super.refreshTransients ();

		// Trap (graphic change)
		if (getTrapCooldown () > 0) {
			ItemManagerItem imi = ItemManager.getItem (getIniHeader ());
			if (imi.isTrap () && imi.getTrapOnIcon () != null && imi.getTrapOnIcon ().length () > 0) {
				changeGraphic (imi.getTrapOnIcon ());
			}
		}
	}


	/**
	 * @param flags the flags to set
	 */
	public void setFlags (byte flags) {
		this.flags = flags;
	}


	/**
	 * @return the flags
	 */
	public byte getFlags () {
		return flags;
	}

	/**
	 * Indica donde dejar el item una vez construido
	 */
	private Point3D destination;


	public boolean isOperative () {
		return (getFlags () & FLAG_OPERATIVE) > 0;
	}


	public void setOperative (boolean operative) {
		boolean bOperativeRemoved = (isOperative () && !operative);

		if (operative) {
			setFlags ((byte) (getFlags () | FLAG_OPERATIVE));
		} else {
			setFlags ((byte) (getFlags () & ~FLAG_OPERATIVE));
		}

		if (bOperativeRemoved) {
			checkConnectors (ItemManager.getItem (getIniHeader ()));
		}
	}


	public boolean isLocked () {
		return (getFlags () & FLAG_LOCKED) > 0;
	}


	public void setLocked (boolean locked) {
		// Cambia de estado, cambiamos el numerito en las listas (primero añadimos y luego sacamos, para que no coincida con un chequeo del APS)
		boolean swap = (isLocked () != locked) && (Item.getItemByID (getID ()) != null);
		boolean beforeLocked = isLocked ();
		if (swap) {
			Item.addItem (UtilsIniHeaders.getIntIniHeader (getIniHeader ()), getID (), !beforeLocked);
		}

		if (locked) {
			setFlags ((byte) (getFlags () | FLAG_LOCKED));
		} else {
			setFlags ((byte) (getFlags () & ~FLAG_LOCKED));
		}

		if (swap) {
			Item.removeItem (UtilsIniHeaders.getIntIniHeader (getIniHeader ()), getID (), beforeLocked);
		}
	}


	public boolean isFacingWest () {
		return ((getFlags () & FLAG_FACING_BIT_0) == 0) && ((getFlags () & FLAG_FACING_BIT_1) == 0);
	}


	public boolean isFacingEast () {
		return ((getFlags () & FLAG_FACING_BIT_0) == 0) && ((getFlags () & FLAG_FACING_BIT_1) > 0);
	}


	public boolean isFacingNorth () {
		return ((getFlags () & FLAG_FACING_BIT_0) > 0) && ((getFlags () & FLAG_FACING_BIT_1) == 0);
	}


	public boolean isFacingSouth () {
		return ((getFlags () & FLAG_FACING_BIT_0) > 0) && ((getFlags () & FLAG_FACING_BIT_1) > 0);
	}


	public int getFacing () {
		if (isFacingWest ()) {
			return Item.FACE_WEST;
		} else if (isFacingNorth ()) {
			return Item.FACE_NORTH;
		} else if (isFacingEast ()) {
			return Item.FACE_EAST;
		} else {
			return Item.FACE_SOUTH;
		}
	}


	public void setFacing (int iFace) {
		if (iFace == Item.FACE_WEST) {
			setFacingBit0 (false);
			setFacingBit1 (false);
			resetAnimationItem (false);
		} else if (iFace == Item.FACE_EAST) {
			setFacingBit0 (false);
			setFacingBit1 (true);
			resetAnimationItem (true);
		} else if (iFace == Item.FACE_NORTH) {
			setFacingBit0 (true);
			setFacingBit1 (false);
			resetAnimationItem (true);
		} else {
			// South
			setFacingBit0 (true);
			setFacingBit1 (true);
			resetAnimationItem (false);
		}
	}


	private void setFacingBit0 (boolean bit0) {
		if (bit0) {
			setFlags ((byte) (getFlags () | FLAG_FACING_BIT_0));
		} else {
			setFlags ((byte) (getFlags () & ~FLAG_FACING_BIT_0));
		}
	}


	private void setFacingBit1 (boolean bit1) {
		if (bit1) {
			setFlags ((byte) (getFlags () | FLAG_FACING_BIT_1));
		} else {
			setFlags ((byte) (getFlags () & ~FLAG_FACING_BIT_1));
		}
	}


	public ArrayList<String> getPrerequisites () {
		return prerequisites;
	}


	public void setPrerequisites (ArrayList<String> prerequisites) {
		this.prerequisites = prerequisites;
	}


	/**
	 * Indica si en las coordenadas pasadas se puede construir un item
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * 
	 * @return true si en las coordenadas pasadas se puede construir un item
	 */
	public static boolean isCellAvailableForItem (ItemManagerItem imi, Point3DShort point, boolean checkZones, boolean checkDiscovered) {
		return isCellAvailableForItem (imi, point.x, point.y, point.z, checkZones, checkDiscovered);
	}


	/**
	 * Indica si en las coordenadas pasadas se puede construir un item
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param cells
	 * 
	 * @return true si en las coordenadas pasadas se puede construir un item
	 */
	public static boolean isCellAvailableForItem (ItemManagerItem imi, int x, int y, int z, boolean checkZones, boolean checkDiscovered) {
		return isCellAvailableForItem (imi, x, y, z, checkZones, checkDiscovered, true, true);
	}


	/**
	 * Indica si en las coordenadas pasadas se puede construir un item
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param cells
	 * 
	 * @return true si en las coordenadas pasadas se puede construir un item
	 */
	public static boolean isCellAvailableForItem (ItemManagerItem imi, Point3DShort point, boolean checkZones, boolean checkDiscovered, boolean checkHabitat) {
		return isCellAvailableForItem (imi, point.x, point.y, point.z, checkZones, checkDiscovered, checkHabitat, true);
	}


	/**
	 * Indica si en las coordenadas pasadas se puede construir un item
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param cells
	 * 
	 * @return true si en las coordenadas pasadas se puede construir un item
	 */
	public static boolean isCellAvailableForItem (ItemManagerItem imi, int x, int y, int z, boolean checkZones, boolean checkDiscovered, boolean checkHabitat, boolean checkPiles) {
		// Fuera del mapa
		if (x < 0 || x >= World.MAP_WIDTH || y < 0 || y >= World.MAP_HEIGHT || z < 0 || z >= World.MAP_DEPTH) {
			return false;
		}

		// Casilla NO descubierta
		Cell cell = World.getCell (x, y, z);

		// No descubierta, no minada, llena
		if (checkDiscovered && !cell.isDiscovered ()) {
			return false;
		}

		// No minada, no vacía, con fluidos
		if (!cell.isMined () || !cell.isEmpty () || cell.getTerrain ().hasFluids ()) {
			return false;
		}

		// Holes/floor
		if (cell.isDigged ()) {
			if (!imi.canBeBuiltOnHoles ()) {
				// Miramos si abajo hay un item de tipo "base"
				if (z < (World.MAP_DEPTH - 1)) {
					Item itemDown = World.getCell (x, y, z + 1).getItem ();
					if (itemDown == null || !ItemManager.getItem (itemDown.getIniHeader ()).isBase ()) {
						return false;
					}
				} else {
					return false;
				}
			}
		} else {
			if (!imi.canBeBuiltOnFloor ()) {
				return false;
			}
		}

		// Built on holes, deben ir en una casilla digada
		// if (imi.canBeBuiltOnHoles ()) {
		// if (!cell.getTerrain ().isDigged ()) {
		// return false;
		// }
		// }
		// Habitat
		// Primero las height
		// POPOif (z == 0) {
		if (checkHabitat && z >= World.MAP_NUM_LEVELS_OUTSIDE) {
			if (imi.getHabitatHeightMin () != -1 && z <= (World.MAP_NUM_LEVELS_OUTSIDE - imi.getHabitatHeightMin ())) {
				return false;
			}
			if (imi.getHabitatHeightMax () != -1 && z >= (World.MAP_NUM_LEVELS_OUTSIDE - imi.getHabitatHeightMin ())) {
				return false;
			}
			// POPO
			/*
			 * if (imi.getHabitatHeightMin () != -1) { if ((World.getMapMaxHeight (x, y, z) / MapGenerator.PIXELS_BETWEEN_HEIGHTS) < imi.getHabitatHeightMin ()) { return false; } } if (imi.getHabitatHeightMax () != -1) { if ((World.getMapMaxHeight (x, y, z) / MapGenerator.PIXELS_BETWEEN_HEIGHTS) > imi.getHabitatHeightMax ()) { return false; } }
			 */
		}
		// Hay que mirar que sea habitat puro, vamos, que si hay una zona o pila encima ya no vale
		if (checkHabitat && z < (World.MAP_DEPTH - 1)) {
			ArrayList<Integer> alHabitat = imi.getHabitat ();
			if (alHabitat != null && alHabitat.size () > 0) {
				Cell cellUnder = World.getCell (x, y, z + 1);
				if (!alHabitat.contains (cellUnder.getTerrain ().getTerrainID ())) {
					return false;
				} else {
					// Habitat correcto, miramos que sea habitat puro
					if (cell.hasZone () || cell.hasStockPile ()) {
						return false;
					}
				}
			}
		}

		// Zones
		if (checkZones) {
			if (imi.getZones () != null && imi.getZones ().size () > 0) {
				if (!cell.hasZone ()) {
					return false;
				}

				Zone zone = Zone.getZone (cell.getZoneID ());
				if (zone != null && !imi.getZones ().contains (zone.getIniHeader ())) {
					return false;
				}
			}
		}

		// Stockpile adecuada
		if (checkPiles && cell.hasStockPile ()) {
			Stockpile pile = Stockpile.getStockpile (x, y, z);
			if (pile != null) {
				if (!pile.itemAllowed (imi.getIniHeader ())) {
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * Crea un objeto a partir de su nombre
	 * 
	 * @param sParameter Nombre
	 * 
	 * @return un objeto a partir de su nombre
	 */
	public static Item createItem (ItemManagerItem imi) {
		Item item = getItem (imi);

		if (item != null) {
			item.setLocked (imi.isLocked ());

			if (item instanceof MilitaryItem) {
				((MilitaryItem) item).setAttackModifier (Utils.launchDice (imi.getAttackModifier ()));
				((MilitaryItem) item).setAttackSpeedModifier (Utils.launchDice (imi.getAttackSpeedModifier ()));
				((MilitaryItem) item).setDefenseModifier (Utils.launchDice (imi.getDefenseModifier ()));
				((MilitaryItem) item).setHealthModifier (Utils.launchDice (imi.getHealthModifier ()));
				((MilitaryItem) item).setDamageModifier (Utils.launchDice (imi.getDamageModifier ()));
				((MilitaryItem) item).setLOSModifier (Utils.launchDice (imi.getLOSModifier ()));
				((MilitaryItem) item).setMovePCTModifier (Utils.launchDice (imi.getMovePCTModifier ()));
				((MilitaryItem) item).setWalkSpeedModifier (Utils.launchDice (imi.getWalkSpeedModifier ()));
			}

			// Item created, we check the tutorial flow
			Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_COLLECT, imi.getNumericalIniHeader (), null);
		}

		return item;
	}


	private static Item getItem (ItemManagerItem imi) {
		if (imi == null) {
			return null;
		}

		String sIniHeader = imi.getIniHeader ();
		if (imi.isMilitaryItem ()) {
			return new MilitaryItem (sIniHeader);
		}

		return new Item (sIniHeader);
	}


	public void setDestination (Point3D destination) {
		this.destination = destination;
	}


	public Point3D getDestination () {
		return destination;
	}


	public int getAge () {
		return age;
	}


	public void setAge (int age) {
		this.age = age;
	}


	public int getMaxAge () {
		return maxAge;
	}


	public void setMaxAge (int maxAge) {
		this.maxAge = maxAge;
	}


	/**
	 * @param hp the hp to set
	 */
	public void setHp (int hp) {
		this.hp = hp;
	}


	/**
	 * @return the hp
	 */
	public int getHp () {
		return hp;
	}


	/**
	 * @param maxHp the maxHp to set
	 */
	public void setMaxHp (int maxHp) {
		this.maxHp = maxHp;
	}


	/**
	 * @return the maxHp
	 */
	public int getMaxHp () {
		return maxHp;
	}


	/**
	 * @param spawnTurns the spawnTurns to set
	 */
	public void setSpawnTurns (int spawnTurns) {
		this.spawnTurns = spawnTurns;
	}


	/**
	 * @return the spawnTurns
	 */
	public int getSpawnTurns () {
		return spawnTurns;
	}


	public void setTrapCooldown (int trapCooldown) {
		this.trapCooldown = trapCooldown;
	}


	public int getTrapCooldown () {
		return trapCooldown;
	}


	public void doHit (LivingEntity livingEntity, int damage) {
		if (!LivingEntity.canLaunchNextAttack (livingEntity)) {
			return;
		}

		// Checks para sacar mensajes o no
		boolean bFirstHit = getHp () == getMaxHp ();
		int hpToShowMessage = getMaxHp ();
		int hp10PCT = getMaxHp () / 10;
		if (hp10PCT <= 0) {
			hp10PCT = 1;
		}
		while (hpToShowMessage > getHp ()) {
			hpToShowMessage -= hp10PCT;
		}

		if (damage < 1) {
			damage = 1;
		}
		setHp (getHp () - damage);

		// Mensaje (sólo a la primera hostia) y cada 10% de vida del item)
		if (bFirstHit || getHp () < hpToShowMessage) {
			MessagesPanel.addMessage (MessagesPanel.TYPE_COMBAT, livingEntity.getLivingEntityData ().getName () + Messages.getString ("Item.1") + Messages.getString ("LivingEntity.5") + ItemManager.getItem (getIniHeader ()).getName (), ColorGL.RED, getCoordinates ()); //$NON-NLS-1$//$NON-NLS-2$
		}

		if (getHp () <= 0) {
			// Palma
			delete ();

			// Mensaje
			MessagesPanel.addMessage (MessagesPanel.TYPE_COMBAT, livingEntity.getLivingEntityData ().getName () + Messages.getString ("Item.2") + ItemManager.getItem (getIniHeader ()).getName (), ColorGL.RED, getCoordinates ()); //$NON-NLS-1$
		}
	}


	public void setWallConnectorStatus (byte doorStatus, boolean setZoneIDs) {
		byte iAnterior = this.flags;

		if (doorStatus == FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED) {
			setFlags ((byte) (flags | FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED));
			setFlags ((byte) (flags & ~FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED));
			setFlags ((byte) (flags & ~FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED));
		} else if (doorStatus == FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED) {
			setFlags ((byte) (flags | FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED));
			setFlags ((byte) (flags & ~FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED));
			setFlags ((byte) (flags & ~FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED));
		} else {
			setFlags ((byte) (flags | FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED));
			setFlags ((byte) (flags & ~FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED));
			setFlags ((byte) (flags & ~FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED));
		}
		// this.doorStatus = doorStatus;

		if ((iAnterior & doorStatus) > 0) {
			return;
		}

		if (setZoneIDs) {
			if (doorStatus == FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED) {
				World.setRecheckASZID (true);
			} else {
				if (((iAnterior & FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED) == 0) && (doorStatus != FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED)) {
					// Ha pasado de un unlocked a otro, no hacemos nada
				} else {
					Cell.mergeZoneID (getCoordinates (), false);
				}
			}
		}

		refreshDoorTile ();

		// Block fluids & Lights
		if (getCoordinates ().x != -1) {
			// Block fluids
			if (isDoorStatus (FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED)) {
				// Si se ha abierto miramos si es un item block y en ese caso seteamos para que fluya el agua
				ItemManagerItem imi = ItemManager.getItem (getIniHeader ());
				if (imi != null && imi.isBlockFluids ()) {
					Game.getWorld ().addFluidCellToProcess (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z, true);
				}
			} else {
				// Puerta se ha cerrado o cerrado y bloqueado, quitamos fluidos si es un fluidblocker
				ItemManagerItem imi = ItemManager.getItem (getIniHeader ());
				if (imi != null && imi.isBlockFluids ()) {
					Cell cell = World.getCell (getCoordinates ());
					cell.getTerrain ().setFluidType (Terrain.FLUIDS_NONE);
					cell.getTerrain ().setFluidCount (0);
					World.checkNewEvaporation (cell);
					Cell.mergeZoneID (cell.getCoordinates (), false);
				}
			}

			// Lights
			Cell.generateLightsItemRemovedCellMined (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z, ItemManagerItem.MAX_LIGHT_RADIUS);
		}

	}


	public void refreshDoorTile () {
		// Gráfico (por defecto es cerrada, en caso de opened cambiamos el gráfico)
		if (isDoorStatus (FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED)) {
			float width = getBaseTileSetTexX1 () - getBaseTileSetTexX0 ();
			setTileSetTexX0 (getBaseTileSetTexX0 () + width);
			setTileSetTexX1 (getBaseTileSetTexX1 () + width);
		} else {
			setTileSetTexX0 (getBaseTileSetTexX0 ());
			setTileSetTexX1 (getBaseTileSetTexX1 ());
		}
	}


	public boolean isDoorStatus (byte status) {
		return (getFlags () & status) > 0;
	}


	public static Item getItemByID (int iID) {
		return World.getItems ().get (Integer.valueOf (iID));
	}


	public static Point3DShort searchItemByID (int iItemID) {
		Item item = getItemByID (iItemID);

		if (item != null) {
			return item.getCoordinates ();
		}

		// Item no existe o está en containers, lo buscamos
		ArrayList<Container> containers = Game.getWorld ().getContainers ();
		Container container;
		for (int i = 0; i < containers.size (); i++) {
			container = containers.get (i);
			if (container.containsItem (iItemID)) {
				Item containerItem = Item.getItemByID (container.getItemID ());
				if (containerItem != null) {
					return containerItem.getCoordinates ();
				} else {
					return null;
				}
			}
		}

		// Item no encontrado
		return null;
	}


	public static Item getItemByID (int iItemID, boolean bSearchInContainers) {
		Item item = getItemByID (iItemID);

		if (item != null) {
			return item;
		}

		if (bSearchInContainers) {
			// Item no existe o está en containers, lo buscamos
			ArrayList<Container> containers = Game.getWorld ().getContainers ();
			Container container;
			for (int i = 0; i < containers.size (); i++) {
				container = containers.get (i);
				if (container.containsItem (iItemID)) {
					for (int c = 0; c < container.getItemsInside ().size (); c++) {
						if (container.getItemsInside ().get (c).getID () == iItemID) {
							return container.getItemsInside ().get (c);
						}
					}
				}
			}
		}

		// Item no encontrado
		return null;
	}


	/**
	 * Elimina el item del punto dado
	 * 
	 * @param p3d Punto
	 */
	public static void delete (Point3DShort p3d) {
		Cell cell = World.getCell (p3d);

		if (cell.hasItem ()) {
			Item item = (Item) cell.getEntity ();
			if (item != null) {
				item.delete ();
			}
		}
	}


	/**
	 * Retorna el Hashmap con los items/cantidad. Nunca retorna null
	 * 
	 * @return el Hashmap con los items/cantidad. Nunca retorna null
	 */
	public static HashMap<Integer, ArrayList<Integer>> getMapItems () {
		return mapItems;
	}


	/**
	 * Retorna el Hashmap con los items/cantidad. Nunca retorna null
	 * 
	 * @return el Hashmap con los items/cantidad. Nunca retorna null
	 */
	public static HashMap<Integer, ArrayList<Integer>> getMapItemsLocked () {
		return mapItemsLocked;
	}


	/**
	 * Añade el item a la lista y suma 1 al número de los mismos
	 * 
	 * @param itemIniHeader IniHeader del item
	 * 
	 * @return el número de items que hay de ese tipo, incluyendo el acabado de añadir
	 */
	public static int addItem (Item item) {
		return addItem (UtilsIniHeaders.getIntIniHeader (item.getIniHeader ()), item.getID (), item.isLocked ());
	}


	/**
	 * Elimina 1 unidad de item de la lista
	 * 
	 * @param itemIniHeader IniHeader del item
	 */
	public static void removeItem (Item item) {
		removeItem (UtilsIniHeaders.getIntIniHeader (item.getIniHeader ()), item.getID (), item.isLocked ());
	}


	/**
	 * Devuelve el número de items en el mundo, sumando lockeds y no-lockeds
	 * 
	 * @return el número de items en el mundo, sumando lockeds y no-lockeds
	 */
	public static int getNumItemsTotal (String sIniHeader, int iMaxLevelToCheck) {
		int iIniHeader = UtilsIniHeaders.getIntIniHeader (sIniHeader);
		return getNumItems (iIniHeader, true, iMaxLevelToCheck) + getNumItems (iIniHeader, false, iMaxLevelToCheck);
	}


	/**
	 * Devuelve el número de items en el mundo del tipo pasado
	 * 
	 * @param itemIniHeader IniHeader del item
	 * @param locked Indica si devuelve el número de items locked o los no lockeds
	 * @param iMaxLevelToCheck Max level to check the number of items
	 * 
	 * @return el número de items en el mundo del tipo pasado
	 */
	public static int getNumItems (int itemIniHeader, boolean locked, int iMaxLevelToCheck) {
		ArrayList<Integer> iIDs;

		if (locked) {
			iIDs = getMapItemsLocked ().get (itemIniHeader);
		} else {
			iIDs = getMapItems ().get (itemIniHeader);
		}

		if (iIDs == null) {
			return 0;
		}

		if (iMaxLevelToCheck >= Game.getWorld ().getNumFloorsDiscovered ()) {
			return iIDs.size ();
		} else {
			// Toca contar
			int iNum = 0;
			Item item;
			for (int i = 0; i < iIDs.size (); i++) {
				item = Item.getItemByID (iIDs.get (i), true);
				if (item != null && item.getCoordinates ().z <= iMaxLevelToCheck) {
					iNum++;
				}
			}
			return iNum;
		}
	}


	/**
	 * Añade un item a las listas
	 * 
	 * @param itemIniHeader IniHeader del item
	 * @param iID Item ID
	 * @param locked Indica si es la lista de lockeds o no
	 */
	public static int addItem (int itemIniHeader, int iID, boolean locked) {
		if (locked) {
			ArrayList<Integer> alList = getMapItemsLocked ().get (itemIniHeader);
			if (alList == null) {
				alList = new ArrayList<Integer> ();
			}
			alList.add (iID);
			getMapItemsLocked ().put (itemIniHeader, alList);
			return alList.size ();
		} else {
			ArrayList<Integer> alList = getMapItems ().get (itemIniHeader);
			if (alList == null) {
				alList = new ArrayList<Integer> ();
			}
			alList.add (iID);
			getMapItems ().put (itemIniHeader, alList);
			return alList.size ();
		}
	}


	/**
	 * Elimina un item de las listas
	 * 
	 * @param itemIniHeader IniHeader del item
	 * @param iID Item ID
	 * @param locked Indica si es la lista de lockeds o no
	 * 
	 */
	public static void removeItem (int itemIniHeader, int iID, boolean locked) {
		if (locked) {
			ArrayList<Integer> alList = getMapItemsLocked ().get (itemIniHeader);
			if (alList != null) {
				alList.remove ((Integer) iID);
				getMapItemsLocked ().put (itemIniHeader, alList);
			}
		} else {
			ArrayList<Integer> alList = getMapItems ().get (itemIniHeader);
			if (alList != null) {
				alList.remove ((Integer) iID);
				getMapItems ().put (itemIniHeader, alList);
			}
		}
	}


	public void init (int x, int y, int z) {
		super.init (x, y, z);

		// Num items
		addItem (this);

		// Lo metemos en la lista de items
		World.getItems ().put (Integer.valueOf (getID ()), this);

		resetAnimationItem (isFacingEast () || isFacingNorth ());

		ItemManagerItem imi = ItemManager.getItem (getIniHeader ());
		// Containers
		if (imi.isContainer ()) {
			Game.getWorld ().addContainer (getID ()); // Añadimos los datos del container al mundo (si es que no existe ya)
		}

		// Minimapa
		if (getColorMiniMap () != null) {
			MiniMapPanel.setMinimapReload (z);
		}
	}


	private void checkDeleteContainer (ItemManagerItem imi) {
		if (imi.isContainer ()) {
			// Sólo se borran si nadie los tiene pillados
			Item itemCarrying;
			boolean bDeleteContainer = true;
			for (int i = 0; i < World.getCitizenIDs ().size (); i++) {
				itemCarrying = ((Citizen) World.getLivingEntityByID (World.getCitizenIDs ().get (i))).getCarrying ();
				if (itemCarrying != null && getID () == itemCarrying.getID ()) {
					bDeleteContainer = false;
					break;
				}
			}
			if (bDeleteContainer) {
				for (int i = 0; i < World.getSoldierIDs ().size (); i++) {
					itemCarrying = ((Citizen) World.getLivingEntityByID (World.getSoldierIDs ().get (i))).getCarrying ();
					if (itemCarrying != null && getID () == itemCarrying.getID ()) {
						bDeleteContainer = false;
						break;
					}
				}
			}
			if (bDeleteContainer) {
				// Enemigos con carrying
				HashMap<Integer, LivingEntity> hmLivings = World.getLivings (true);
				Iterator<LivingEntity> itLiving = hmLivings.values ().iterator ();
				LivingEntity le;
				Enemy enemy;
				while (itLiving.hasNext ()) {
					le = itLiving.next ();
					if (LivingEntityManager.getItem (le.getIniHeader ()).getType () == LivingEntity.TYPE_ENEMY) {
						enemy = (Enemy) le;
						if (enemy.getCarryingData () != null && enemy.getCarryingData ().getCarrying () != null && enemy.getCarryingData ().getCarrying ().getID () == getID ()) {
							// Bingo
							bDeleteContainer = false;
							break;
						}
					}
				}
			}

			if (bDeleteContainer) {
				Game.getWorld ().deleteContainer (getID ());
			}
		}
	}


	/**
	 * Elimina el item del juego
	 * 
	 * @param p3d Punto
	 */
	public void delete () {
		super.delete (); // Lo sacamos de la casilla actual

		ItemManagerItem imi = ItemManager.getItem (getIniHeader ());
		removeItem (this); // Restamos 1 al número de items de ese tipo

		// Containers
		checkDeleteContainer (imi);

		// Lo sacamos de la lista de Items
		Item item = World.getItems ().remove (Integer.valueOf (getID ()));

		// Miramos si ha muerto de viejo, en ese caso miramos si se convierte en algo (ej: bush -> tree)
		// Edad
		if (getAge () >= getMaxAge ()) {
			// Miramos si suelta algo
			if (imi.getMaxAgeItem () != null) {
				// Miramos si puede ir ahí
				if (isCellAvailableForItem (ItemManager.getItem (imi.getMaxAgeItem ()), getX (), getY (), getZ (), true, false)) {
					Item newItem = Item.getItem (ItemManager.getItem (imi.getMaxAgeItem ()));
					newItem.init (getX (), getY (), getZ ());
					newItem.setOperative (true);
					newItem.setLocked (isLocked ());
					World.getCell (getCoordinates ()).setEntity (newItem);
				}
			} else {
				if (imi.getMaxAgeTerrain () != null) {
					// Terrain?
					// Let's turn the current terrain into the new one
					// We'll also set the non-mined flag
					Cell.createTerrain (getCoordinates (), imi.getMaxAgeTerrain ());
					Cell.setAllZoneIDs ();
				}
			}
		}

		// Si era un zoneMerger, zoneMergerUpDown, muro (splitter) o connector locked (splitter) reasignamos los A*Zone ID
		checkConnectors (imi);

		// Añadimos el item de arriba de la celda (si tiene) a la lista de items posibles a caer
		// También los items de los lados por si tienen glue
		if (getCoordinates ().z > 0) {
			Item itemUp = World.getCell (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z - 1).getItem ();
			if (itemUp != null) {
				World.addFallItem (itemUp.getID ());
			}
		}
		if (getCoordinates ().x > 0) {
			Item itemUp = World.getCell (getCoordinates ().x - 1, getCoordinates ().y, getCoordinates ().z).getItem ();
			if (itemUp != null) {
				World.addFallItem (itemUp.getID ());
			}
		}
		if (getCoordinates ().x < (World.MAP_WIDTH - 1)) {
			Item itemUp = World.getCell (getCoordinates ().x + 1, getCoordinates ().y, getCoordinates ().z).getItem ();
			if (itemUp != null) {
				World.addFallItem (itemUp.getID ());
			}
		}
		if (getCoordinates ().y > 0) {
			Item itemUp = World.getCell (getCoordinates ().x, getCoordinates ().y - 1, getCoordinates ().z).getItem ();
			if (itemUp != null) {
				World.addFallItem (itemUp.getID ());
			}
		}
		if (getCoordinates ().y < (World.MAP_WIDTH - 1)) {
			Item itemUp = World.getCell (getCoordinates ().x, getCoordinates ().y + 1, getCoordinates ().z).getItem ();
			if (itemUp != null) {
				World.addFallItem (itemUp.getID ());
			}
		}

		// Base=true, en ese caso las zonas y pilas de arriba se borran
		if (imi.isBase () && getCoordinates ().z > 0) {
			Stockpile.deleteStockpilePoint (getCoordinates ().x, getCoordinates ().y, (short) (getCoordinates ().z - 1));
			Zone.deleteZonePoint (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z - 1);
		}

		// Fluidos
		if (imi.isBlockFluids ()) {
			Game.getWorld ().addFluidCellToProcess (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z, true);
		}

		// Minimapa
		if (getColorMiniMap () != null && item != null) {
			MiniMapPanel.setMinimapReload (item.getZ ());
		}
	}


	/**
	 * Mira si el item A BORRAR/PONER NO OPERATIVO era un connector/door para setear los zoneIDs
	 */
	private void checkConnectors (ItemManagerItem imi) {
		if (isOperative () && isLocked () && (imi.isZoneMergerUp () || imi.isWall () || (imi.isDoor () && isDoorStatus (FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED)))) {
			World.setRecheckASZID (true); // Ojo! No nos vale con un simple merge (caso escaleras que tienen un dig arriba con -1)
		}
	}


	/**
	 * Recorre todas las celdas del mundo y lanza un init por cada item encontrado
	 */
	public static void loadItems (World world) {
		Item item;
		World.getItems (); // Por si no hay items en la partida, que cree la hash

		for (short x = 0; x < World.MAP_WIDTH; x++) {
			for (short y = 0; y < World.MAP_HEIGHT; y++) {
				for (short z = 0; z < World.MAP_DEPTH; z++) {
					item = World.getCell (x, y, z).getItem ();
					if (item != null) {
						item.init (x, y, z);
						world.addItemToBeHauled (item);
					}
				}
			}
		}
	}


	/**
	 * Comprueba que un item esté locked
	 * 
	 * @param entity
	 * @return
	 */
	private static boolean checkLocked (Entity entity) {
		return (entity != null && entity instanceof Item && ((Item) entity).isLocked ());
	}


	/**
	 * Comprueba que un item esté operative
	 * 
	 * @param entity
	 * @return
	 */
	private static boolean checkOperative (Entity entity) {
		return (entity != null && entity instanceof Item && ((Item) entity).isOperative ());
	}


	/**
	 * Comprueba que un item esté operativo, locked y que no esté en la lista de items a evitar
	 * 
	 * @param entity
	 * @param locked
	 * @param operative
	 * @param alItemsToAvoid
	 * @return true si un item está operativo, locked y que no está en la lista de items a evitar
	 */
	private static boolean checkLockedOperativeAndItems (Entity entity, int locked, int operative, ArrayList<Integer> alItemsToAvoid) {
		if (entity == null) {
			return false;
		}

		boolean bItemOK = true;
		if (locked != SEARCH_DOESNTMATTER) {
			if (locked == SEARCH_TRUE) {
				bItemOK = checkLocked (entity);
			} else {
				bItemOK = !checkLocked (entity);
			}
		}
		if (bItemOK && operative != SEARCH_DOESNTMATTER) {
			if (operative == SEARCH_TRUE) {
				bItemOK = checkOperative (entity);
			} else {
				bItemOK = !checkOperative (entity);
			}
		}
		if (bItemOK && alItemsToAvoid != null && alItemsToAvoid.size () > 0) {
			bItemOK = !alItemsToAvoid.contains (Integer.valueOf(entity.getID ()));
		}

		return bItemOK;
	}


	/**
	 * Devuelve una lista de items en uso por aldeanos que NO sea el aldeano pasado como parámetro
	 * 
	 * @param iCitID ID del aldeano a excluir o -1 en caso de no excluir a nadie
	 * @return una lista de items en uso por aldeanos que no es el pasado, o nulo si no hay ninguno
	 */
	public static ArrayList<Integer> searchItemInUse (int iCitID) {
		ArrayList<Integer> alItemsInUse = new ArrayList<Integer> ();
		Citizen citizen;
		for (int i = 0; i < World.getCitizenIDs ().size (); i++) {
			citizen = (Citizen) World.getLivingEntityByID (World.getCitizenIDs ().get (i));
			if (iCitID == -1 || citizen.getID () != iCitID) {
				if (citizen.getCurrentCustomAction () != null && citizen.getCurrentCustomAction ().getQueueData () != null) {
					// Move
					if (citizen.getCurrentCustomAction ().getQueueData ().getItemIDCurrentPlace () != -1) {
						alItemsInUse.add (Integer.valueOf(citizen.getCurrentCustomAction ().getQueueData ().getItemIDCurrentPlace ()));
					}
					// Pick
					if (citizen.getCurrentCustomAction ().getQueueData ().getItemIDPick () != -1) {
						alItemsInUse.add (Integer.valueOf(citizen.getCurrentCustomAction ().getQueueData ().getItemIDPick ()));
					}
				}
			}
		}

		if (alItemsInUse.size () > 0) {
			return alItemsInUse;
		} else {
			return null;
		}
	}


	public static Point3DShort searchItem (boolean bJustKnowIfExists, Point3DShort p3dCurrentPoint, int[] aiIniHeaders, boolean near, int locked, int operative, ArrayList<Integer> alItemsToAvoid, int iMaxLevelToCheck) {
		return searchItem (bJustKnowIfExists, p3dCurrentPoint, aiIniHeaders, near, locked, operative, alItemsToAvoid, false, iMaxLevelToCheck);
	}


	/**
	 * Devuelve la coordenada de un item que cumpla alguno de los prerequisitos. Intenta devolver el item más cercano al punto de origen. Primero mira en la casilla de origen. Si no lo encuentra mira en containers. Si no lo encuentra mira en stockpiles. Si no lo encuentra mira en todos los materiales del mundo
	 * 
	 * @param p3dCurrentPoint Punto origen
	 * @param aiIniHeaders Prerequisitos
	 * @param near Indica si hay que buscar el item más cercano
	 * @param locked Indica si el item tiene que estar locked
	 * @param operative Indica si el item tiene que estar operative
	 * @param alItemsToAvoid Lista de IDs de items que no hay que devolver
	 * @param searchItemsInOrder Hace que, de la lista pasada de items primero busca el primero, si no lo encuentra buscará el 2o, ...
	 * @param iMaxLevelToCheck Max level to check
	 * @return la coordenada de un item que cumpla alguno de los prerequisitos o null si no existe el item
	 */
	public static Point3DShort searchItem (boolean bJustKnowIfExists, Point3DShort p3dCurrentPoint, int[] aiIniHeaders, boolean near, int locked, int operative, ArrayList<Integer> alItemsToAvoid, boolean searchItemsInOrder, int iMaxLevelToCheck) {
		return searchItem (bJustKnowIfExists, p3dCurrentPoint, aiIniHeaders, near, locked, operative, alItemsToAvoid, searchItemsInOrder, true, iMaxLevelToCheck);
	}


	/**
	 * Devuelve la coordenada de un item que cumpla alguno de los prerequisitos. Intenta devolver el item más cercano al punto de origen. Primero mira en la casilla de origen. Si no lo encuentra mira en containers. Si no lo encuentra mira en stockpiles. Si no lo encuentra mira en todos los materiales del mundo
	 * 
	 * @param p3dCurrentPoint Punto origen
	 * @param aiIniHeaders Prerequisitos
	 * @param near Indica si hay que buscar el item más cercano
	 * @param locked Indica si el item tiene que estar locked
	 * @param operative Indica si el item tiene que estar operative
	 * @param alItemsToAvoid Lista de IDs de items que no hay que devolver
	 * @param searchItemsInOrder Hace que, de la lista pasada de items primero busca el primero, si no lo encuentra buscará el 2o, ...
	 * @param checkContainers Mira contenedores
	 * @param iMaxLevelToCheck Max nivel a mirar
	 * @return la coordenada de un item que cumpla alguno de los prerequisitos o null si no existe el item
	 */
	public static Point3DShort searchItem (boolean bJustKnowIfExists, Point3DShort p3dCurrentPoint, int[] aiIniHeaders, boolean near, int locked, int operative, ArrayList<Integer> alItemsToAvoid, boolean searchItemsInOrder, boolean bCheckContainers, int iMaxLevelToCheck) {
		if (searchItemsInOrder && aiIniHeaders.length > 1) {
			int[] aiItemToSearch = new int [1];
			// ArrayList<String> alItemToSearch = new ArrayList<String> ();
			Point3DShort p3dItem;
			for (int i = 0; i < aiIniHeaders.length; i++) {
				// alItemToSearch.clear ();
				// alItemToSearch.add (aiIniHeaders [i]);
				aiItemToSearch[0] = aiIniHeaders[i];

				p3dItem = searchItem (bJustKnowIfExists, p3dCurrentPoint, aiItemToSearch, near, locked, operative, alItemsToAvoid, false, iMaxLevelToCheck);

				if (p3dItem != null) {
					return p3dItem;
				}
			}

			return null;
		}

		// Items del tipo que toca en el mundo
		boolean bHayAlguno = false;
		int iNum;
		for (int i = 0; i < aiIniHeaders.length; i++) {
			if (locked == SEARCH_DOESNTMATTER) {
				iNum = Item.getNumItemsTotal (UtilsIniHeaders.getStringIniHeader (aiIniHeaders[i]), iMaxLevelToCheck);
			} else if (locked == SEARCH_TRUE) {
				iNum = Item.getNumItems (aiIniHeaders[i], true, iMaxLevelToCheck);
			} else {
				iNum = Item.getNumItems (aiIniHeaders[i], false, iMaxLevelToCheck);
			}
			if (iNum > 0) {
				bHayAlguno = true;
				break;
			}
		}
		// No hay items de los que queremos
		if (!bHayAlguno) {
			return null;
		} else {
			if (bJustKnowIfExists) {
				// Miramos que no esté en uso
				if (alItemsToAvoid == null || alItemsToAvoid.size () == 0) {
					return Point3DShort.getPoolInstance (0, 0, 0);
					// } else {
					// Hay que hacer la búsqueda entera, para saber si los items encontrados estarán en uso o no
				}
			}
		}

		// Si llega aquí es que en el mundo existen items, vamos a ver si en la casilla que estamos hay alguno que nos vaya bien
		Item itemAux;
		Cell cell = World.getCell (p3dCurrentPoint);

		if (cell.hasEntity ()) {
			int iItem = cell.getEntity ().getNumericIniHeader ();
			for (int i = 0; i < aiIniHeaders.length; i++) {
				if (aiIniHeaders[i] == iItem) {
					if (checkLockedOperativeAndItems (cell.getEntity (), locked, operative, alItemsToAvoid)) {
						return p3dCurrentPoint;
					}
				}
			}
		}

		// Si llegamos aquí es que no hay material en nuestra casilla, vamos a buscar uno en containers
		int iCurrentASZID = cell.getAstarZoneID ();
		Point3DShort p3dMin = null;
		Point3DShort p3d;
		if (bCheckContainers) {
			ArrayList<Container> containers = Game.getWorld ().getContainers ();
			Container container;
			int distanciaMin = Utils.MAX_DISTANCE;
			for (int i = 0; i < containers.size (); i++) {
				container = containers.get (i);
				Item containerItem = Item.getItemByID (container.getItemID ());
				if (containerItem != null && World.getCell (containerItem.getCoordinates ()).getAstarZoneID () == iCurrentASZID && containerItem.getCoordinates ().z <= iMaxLevelToCheck) {
					if (container.containsAny (aiIniHeaders)) {
						if (near) {
							p3d = containerItem.getCoordinates ();
							if (p3dMin == null) {
								p3dMin = p3d;
								distanciaMin = Utils.getDistance (p3dCurrentPoint, p3d);
							} else {
								int distAux = Utils.getDistance (p3dCurrentPoint, p3d);
								if (distAux < distanciaMin) {
									p3dMin = p3d;
									distanciaMin = distAux;
								}
							}
						} else {
							return containerItem.getCoordinates ();
						}
					}
				}
			}
			if (p3dMin != null) {
				return p3dMin;
			}

			p3dMin = null;
		}

		// Si llegamos aquí es que no hay material en nuestra casilla ni containers, vamos a buscar uno en stockpiles
		Stockpile stockPile;
		int distanciaMin = Utils.MAX_DISTANCE;
		boolean bAllowed;
		stockpiles1: for (int i = 0; i < Game.getWorld ().getStockpiles ().size (); i++) {
			stockPile = Game.getWorld ().getStockpiles ().get (i);

			if (stockPile.isEmpty () || stockPile.getPoints ().size () == 0 || stockPile.getPoints ().get(0).z > iMaxLevelToCheck) {
				continue;
			}
			// Miramos si la stockpile puede contener alguno de los prerequisitos
			bAllowed = false;
			ArrayList<String> alElements = stockPile.getType ().getElements ();
			stockElements: for (int p = 0; p < aiIniHeaders.length; p++) {
				for (int h = 0; h < alElements.size (); h++) {
					if (UtilsIniHeaders.getIntIniHeader (alElements.get (h)) == aiIniHeaders[p]) {
						// Stockpile permite el prerequisito
						bAllowed = true;
						break stockElements;
					}
				}
			}

			if (!bAllowed) {
				continue stockpiles1;
			}

			for (int j = 0; j < stockPile.getPoints ().size (); j++) {
				p3d = stockPile.getPoints ().get (j);

				// Punto de stockpile, si está en la zona A* del aldeano y luego miramos si hay item
				if (World.getCell (p3d).getAstarZoneID () == iCurrentASZID) {
					cell = World.getCell (p3d);
					if (checkLockedOperativeAndItems (cell.getEntity (), locked, operative, alItemsToAvoid)) {
						boolean bItemInCellOK = false;
						for (int m = 0; m < aiIniHeaders.length; m++) {
							if (aiIniHeaders[m] == cell.getEntity ().getNumericIniHeader ()) {
								bItemInCellOK = true;
								break;
							}
						}
						if (bItemInCellOK) {
							if (!near) {
								return p3d;
							}

							// Tenemos item, miramos distancia y nos vamos a la siguiente stockpile
							if (p3dMin == null) {
								p3dMin = p3d;
								distanciaMin = Utils.getDistance (p3dCurrentPoint, p3d);
							} else {
								int distAux = Utils.getDistance (p3dCurrentPoint, p3d);
								if (distAux < distanciaMin) {
									p3dMin = p3d;
									distanciaMin = distAux;
								}
							}
							continue stockpiles1;
						}
					}
				}
			}
		}

		// Stockpiles miradas, vamos a ver si hemos encontrado un item
		if (p3dMin != null) {
			return p3dMin;
		}

		// Si llega aquí es que en stockpiles no hay nada, miramos en todos los materiales del mundo
		for (int searchingItemsIndex = 0; searchingItemsIndex < aiIniHeaders.length; searchingItemsIndex++) {
			int iHeaderID = aiIniHeaders[searchingItemsIndex];

			if (locked == SEARCH_TRUE || locked == SEARCH_DOESNTMATTER) {
				ArrayList<Integer> alItems = Item.getMapItemsLocked ().get (iHeaderID);
				if (alItems != null) {
					for (int i = 0; i < alItems.size (); i++) {
						itemAux = getItemByID (alItems.get (i));

						if (itemAux != null && itemAux.getCoordinates ().z <= iMaxLevelToCheck) {
							if (checkLockedOperativeAndItems (World.getCell (itemAux.getCoordinates ()).getEntity (), locked, operative, alItemsToAvoid)) {
								if (World.getCell (itemAux.getCoordinates ()).getAstarZoneID () == World.getCell (p3dCurrentPoint).getAstarZoneID ()) {
									p3d = itemAux.getCoordinates ();
									if (near) {
										if (p3dMin == null) {
											p3dMin = p3d;
											distanciaMin = Utils.getDistance (p3dCurrentPoint, p3d);
										} else {
											int distAux = Utils.getDistance (p3dCurrentPoint, p3d);
											if (distAux < distanciaMin) {
												p3dMin = p3d;
												distanciaMin = distAux;
											}
										}
									} else {
										return itemAux.getCoordinates ();
									}
								}
							}
						}
					}
				}
			}

			if (locked == SEARCH_FALSE || locked == SEARCH_DOESNTMATTER) {
				ArrayList<Integer> alItems = Item.getMapItems ().get (iHeaderID);
				if (alItems != null) {
					for (int i = 0; i < alItems.size (); i++) {
						itemAux = getItemByID (alItems.get (i));

						if (itemAux != null && itemAux.getCoordinates ().z <= iMaxLevelToCheck) {
							if (checkLockedOperativeAndItems (World.getCell (itemAux.getCoordinates ()).getEntity (), locked, operative, alItemsToAvoid)) {
								if (World.getCell (itemAux.getCoordinates ()).getAstarZoneID () == World.getCell (p3dCurrentPoint).getAstarZoneID ()) {
									p3d = itemAux.getCoordinates ();
									if (near) {
										if (p3dMin == null) {
											p3dMin = p3d;
											distanciaMin = Utils.getDistance (p3dCurrentPoint, p3d);
										} else {
											int distAux = Utils.getDistance (p3dCurrentPoint, p3d);
											if (distAux < distanciaMin) {
												p3dMin = p3d;
												distanciaMin = distAux;
											}
										}
									} else {
										return itemAux.getCoordinates ();
									}
								}
							}
						}
					}
				}
			}
		}
		// Integer[] aItems = World.getItems ().keySet ().toArray (new Integer [0]);
		// for (int i = 0; i < aItems.length; i++) {
		// itemAux = World.getItems ().get (aItems[i]);
		//
		// if (itemAux != null) {
		// if (checkLockedOperativeAndItems (World.getCell (itemAux.getCoordinates ()).getEntity (), locked, operative, alItemsToAvoid)) {
		// boolean bItemInCellOK = false;
		// for (int m = 0; m < aiIniHeaders.length; m++) {
		// if (aiIniHeaders [m] == itemAux.getNumericIniHeader ()) {
		// bItemInCellOK = true;
		// break;
		// }
		// }
		// if (bItemInCellOK) {
		// if (World.getCell (itemAux.getCoordinates ()).getAstarZoneID () == World.getCell (p3dCurrentPoint).getAstarZoneID ()) {
		// p3d = itemAux.getCoordinates ();
		// if (near) {
		// if (p3dMin == null) {
		// p3dMin = p3d;
		// distanciaMin = Utils.getDistance (p3dCurrentPoint, p3d);
		// } else {
		// int distAux = Utils.getDistance (p3dCurrentPoint, p3d);
		// if (distAux < distanciaMin) {
		// p3dMin = p3d;
		// distanciaMin = distAux;
		// }
		// }
		// } else {
		// return itemAux.getCoordinates ();
		// }
		// }
		// }
		// }
		// }
		// }
		//
		if (p3dMin != null) {
			return p3dMin;
		}

		// Si llega aquí es que en el mundo hay items pero ninguno nos sirve, salimos de la tarea
		return null;
	}


	/**
	 * Devuelve la coordenada de un item DE COMIDA Busca, de los que tengan valores más altos el más cercano Primero containers y pilas, en otro caso cualquier item del mundo
	 * 
	 * Evita comidas que tienen "lockeada" otros aldeanos con tarea de EAT
	 * 
	 * @param p3dCurrentPoint Punto origen
	 * @param citID ID del aldeano que busca la comida
	 * @return la coordenada de un item que cumpla alguno de los prerequisitos o null si no existe el item
	 */
	public static Point3DShort searchFood (Point3DShort p3dCurrentPoint, int citID) {
		if (World.getItems ().size () == 0) {
			// No hay materiales en el mundo
			return null;
		}

		int iCurrentPointASZID = World.getCell (p3dCurrentPoint).getAstarZoneID ();

		ArrayList<Item> alItemsMaxFood = new ArrayList<Item> ();
		int iMaxFoodValue = 0;
		int distanciaMin = Utils.MAX_DISTANCE;
		int itemIndex = -1;
		Point3DShort p3d;
		Item foodItem;
		ItemManagerItem imi;

		// Containers y pilas
		ArrayList<Container> alContainers = Game.getWorld ().getContainers ();
		ArrayList<Item> alContainerItems;
		boolean bContainerChecked = false; // Para ver si el contenedor está en su sitio
		for (int i = 0; i < alContainers.size (); i++) {
			alContainerItems = alContainers.get (i).getItemsInside ();
			bContainerChecked = false;
			for (int j = 0; j < alContainerItems.size (); j++) {
				foodItem = alContainerItems.get (j);
				if (foodItem != null) {
					imi = ItemManager.getItem (foodItem.getIniHeader ());
					if (imi.canBeEaten () && imi.getFoodValue () >= iMaxFoodValue) {
						// Item bueno para comer, miramos si está en la zona y ningún otro aldeano va a por el
						p3d = foodItem.getCoordinates ();
						// Miramos que el container esté ahí (caso robbery siege o algún aldeano moviéndolo)
						if (p3d != null && !bContainerChecked) {
							Item item = World.getCell (p3d).getItem ();
							if (item != null) {
								ItemManagerItem imiContainer = ItemManager.getItem (item.getIniHeader ());
								if (imiContainer != null && imiContainer.isContainer ()) {
									bContainerChecked = true;
								}
							}
						}

						if (bContainerChecked && iCurrentPointASZID == World.getCell (foodItem.getCoordinates ()).getAstarZoneID () && !Citizen.isCitizenWalkingToFood (foodItem.getCoordinates (), citID)) {
							// Está en la zona y el item no es de nadie, si el valor de comida es igual al max simplemente lo metemos en la lista
							// En otro caso borramos la lista y metemos el nuevo item
							if (imi.getFoodValue () > iMaxFoodValue) {
								iMaxFoodValue = imi.getFoodValue ();
								alItemsMaxFood.clear ();
								distanciaMin = Utils.MAX_DISTANCE;
								itemIndex = -1;
							}
							alItemsMaxFood.add (foodItem);
							int distAux = Utils.getDistance (p3dCurrentPoint, p3d);
							if (distAux < distanciaMin) {
								distanciaMin = distAux;
								itemIndex = alItemsMaxFood.size () - 1;
							}
						}
					}
				}
			}
		}

		ArrayList<Stockpile> alPiles = Game.getWorld ().getStockpiles ();
		Stockpile pile;
		boolean bCanContainFood;
		Cell cell;
		for (int i = 0; i < alPiles.size (); i++) {
			pile = alPiles.get (i);
			if (pile.isEmpty ()) {
				continue;
			}
			bCanContainFood = false;
			// Miramos si puede contener comida
			for (int t = 0; t < pile.getType ().getElements ().size (); t++) {
				imi = ItemManager.getItem (pile.getType ().getElements ().get (t));
				if (imi.canBeEaten ()) {
					bCanContainFood = true;
					break;
				}
			}

			if (bCanContainFood) {
				// Puede contener comida, miramos todas las celdas
				for (int c = 0; c < pile.getPoints ().size (); c++) {
					cell = World.getCell (pile.getPoints ().get (c));
					if (cell.hasItem ()) {
						foodItem = (Item) cell.getEntity ();
						if (foodItem != null) {
							imi = ItemManager.getItem (foodItem.getIniHeader ());
							if (imi.canBeEaten () && imi.getFoodValue () >= iMaxFoodValue) {
								// Item bueno para comer, miramos si está en la zona y ningún otro aldeano va a por el
								p3d = foodItem.getCoordinates ();
								if (iCurrentPointASZID == World.getCell (foodItem.getCoordinates ()).getAstarZoneID () && !Citizen.isCitizenWalkingToFood (foodItem.getCoordinates (), citID)) {
									// Está en la zona y el item no es de nadie, si el valor de comida es igual al max simplemente lo metemos en la lista
									// En otro caso borramos la lista y metemos el nuevo item
									if (imi.getFoodValue () > iMaxFoodValue) {
										iMaxFoodValue = imi.getFoodValue ();
										alItemsMaxFood.clear ();
										distanciaMin = Utils.MAX_DISTANCE;
										itemIndex = -1;
									}
									alItemsMaxFood.add (foodItem);
									int distAux = Utils.getDistance (p3dCurrentPoint, p3d);
									if (distAux < distanciaMin) {
										distanciaMin = distAux;
										itemIndex = alItemsMaxFood.size () - 1;
									}
								}
							}
						}
					}
				}
			}
		}

		// if (itemIndex != -1) {
		// return alItemsMaxFood.get (itemIndex).getCoordinates ();
		// }
		// All items
		// alItemsMaxFood.clear ();
		// distanciaMin = AStarNodo.MAX_HEURISTIC;
		// iMaxFoodValue = 0;
		Integer[] aItems = World.getItems ().keySet ().toArray (new Integer [0]);
		for (int i = 0; i < aItems.length; i++) {
			foodItem = World.getItems ().get (aItems[i]);
			if (foodItem != null) {
				imi = ItemManager.getItem (foodItem.getIniHeader ());
				if (imi.canBeEaten () && imi.getFoodValue () >= iMaxFoodValue) {
					// Item bueno para comer, miramos si está en la zona y ningún otro aldeano va a por el
					p3d = foodItem.getCoordinates ();
					if (iCurrentPointASZID == World.getCell (p3d).getAstarZoneID () && !Citizen.isCitizenWalkingToFood (p3d, citID)) {
						// Está en la zona y el item no es de nadie, si el valor de comida es igual al max simplemente lo metemos en la lista
						// En otro caso borramos la lista y metemos el nuevo item
						if (imi.getFoodValue () > iMaxFoodValue) {
							iMaxFoodValue = imi.getFoodValue ();
							alItemsMaxFood.clear ();
							distanciaMin = Utils.MAX_DISTANCE;
							itemIndex = -1;
						}
						alItemsMaxFood.add (foodItem);
						int distAux = Utils.getDistance (p3dCurrentPoint, p3d);
						if (distAux < distanciaMin) {
							distanciaMin = distAux;
							itemIndex = alItemsMaxFood.size () - 1;
						}
					}
				}
			}
		}

		if (itemIndex != -1) {
			return alItemsMaxFood.get (itemIndex).getCoordinates ();
		}

		return null;
	}


	/**
	 * Fills a contextual menú refering an item of a cell
	 * 
	 * @param cell
	 * @param sm
	 */
	public static void fillMenu (Cell cell, SmartMenu sm) {
		Item item = cell.getItem ();
		if (item != null) {
			if (TownsProperties.DEBUG_MODE) {
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Operative: " + item.isOperative (), null, null, null)); //$NON-NLS-1$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Locked: " + item.isLocked (), null, null, null)); //$NON-NLS-1$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "ID: " + item.getID () + " (" + item.getIniHeader () + ")", null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Num in world: " + Item.getNumItemsTotal (item.getIniHeader (), World.MAP_DEPTH - 1), null, null, null)); //$NON-NLS-1$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Num in world over restriction (" + Game.getWorld ().getRestrictHaulEquippingLevel () + "): " + Item.getNumItemsTotal (item.getIniHeader (), Game.getWorld ().getRestrictHaulEquippingLevel ()), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Num locked: " + Item.getNumItems (UtilsIniHeaders.getIntIniHeader (item.getIniHeader ()), true, World.MAP_DEPTH - 1), null, null, null)); //$NON-NLS-1$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Num locked over restriction (" + Game.getWorld ().getRestrictHaulEquippingLevel () + "): " + Item.getNumItems (UtilsIniHeaders.getIntIniHeader (item.getIniHeader ()), true, Game.getWorld ().getRestrictHaulEquippingLevel ()), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Num unlocked: " + Item.getNumItems (UtilsIniHeaders.getIntIniHeader (item.getIniHeader ()), false, World.MAP_DEPTH - 1), null, null, null)); //$NON-NLS-1$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Num unlocked over restriction (" + Game.getWorld ().getRestrictHaulEquippingLevel () + "): " + Item.getNumItems (UtilsIniHeaders.getIntIniHeader (item.getIniHeader ()), false, Game.getWorld ().getRestrictHaulEquippingLevel ()), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "age/maxAge: " + item.getAge () + " / " + item.getMaxAge (), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
			}

			ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());

			// Wall connectors (doors)
			if (imi.isDoor () && item.isOperative () && item.isLocked ()) {
				if (item.isDoorStatus (FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED)) {
					sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.5") + imi.getName (), null, CommandPanel.COMMAND_UNLOCK_CLOSE, Integer.toString (item.getID ()), null, null, Color.GREEN)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.6") + imi.getName (), null, CommandPanel.COMMAND_LOCK, Integer.toString (item.getID ()), null, null, Color.GREEN)); //$NON-NLS-1$
				} else if (item.isDoorStatus (FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED)) {
					sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.7") + imi.getName (), null, CommandPanel.COMMAND_UNLOCK_OPEN, Integer.toString (item.getID ()), null, null, Color.GREEN)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.3") + imi.getName (), null, CommandPanel.COMMAND_LOCK, Integer.toString (item.getID ()), null, null, Color.GREEN)); //$NON-NLS-1$
				} else if (item.isDoorStatus (FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED)) {
					sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.4") + imi.getName (), null, CommandPanel.COMMAND_UNLOCK_CLOSE, Integer.toString (item.getID ()), null, null, Color.GREEN)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.8") + imi.getName (), null, CommandPanel.COMMAND_UNLOCK_OPEN, Integer.toString (item.getID ()), null, null, Color.GREEN)); //$NON-NLS-1$
				}
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
			}

			// Text
			if (imi.isText ()) {
				sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.11"), null, CommandPanel.COMMAND_ITEM_TEXT_ADD, Integer.toString (item.getID ()), null, null)); //$NON-NLS-1$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.12"), null, CommandPanel.COMMAND_ITEM_TEXT_DELETE, Integer.toString (item.getID ()), null, null)); //$NON-NLS-1$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
			}

			// Actions
			if (imi.hasActions ()) {
				ActionManagerItem ami;
				for (int i = 0; i < imi.getActions ().size (); i++) {
					ami = ActionManager.getItem (imi.getActions ().get (i));
					sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, ami.getName () + " " + imi.getName ().toLowerCase (), null, CommandPanel.COMMAND_CUSTOM_ACTION_DIRECT_ITEM, ami.getId (), Integer.toString (item.getID ()))); //$NON-NLS-1$
				}
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
			}

			// Build like this one
			if (imi.getBuildAction () != null) {
				ActionManagerItem ami = ActionManager.getItem (imi.getBuildAction ());
				if (ami != null) {
					sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.19") + imi.getName (), null, CommandPanel.COMMAND_QUEUE_AND_PLACE, ami.getId (), null)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
				}
			}

			// Unlock
			if (item.isLocked () && imi.canBeUnlocked ()) {
				sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.18"), null, CommandPanel.COMMAND_UNLOCK, Integer.toString (item.getID ()), null)); //$NON-NLS-1$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
			}

			// Rotate
			if (imi.isCanBeRotated ()) {
				SmartMenu menuRotate = new SmartMenu (SmartMenu.TYPE_MENU, Messages.getString ("Item.13"), sm, null, null); //$NON-NLS-1$

				if (item.isFacingWest ()) {
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.14"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_NORTH))); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.15"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_SOUTH))); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.16"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_EAST))); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, Messages.getString ("Item.17"), null, null, null, null, null, Color.GRAY)); //$NON-NLS-1$
				} else if (item.isFacingEast ()) {
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.14"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_NORTH))); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.15"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_SOUTH))); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, Messages.getString ("Item.16"), null, null, null, null, null, Color.GRAY)); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.17"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_WEST))); //$NON-NLS-1$
				} else if (item.isFacingNorth ()) {
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, Messages.getString ("Item.14"), null, null, null, null, null, Color.GRAY)); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.15"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_SOUTH))); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.16"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_EAST))); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.17"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_WEST))); //$NON-NLS-1$
				} else {
					// South
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.14"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_NORTH))); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, Messages.getString ("Item.15"), null, null, null, null, null, Color.GRAY)); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.16"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_EAST))); //$NON-NLS-1$
					menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.17"), null, CommandPanel.COMMAND_ITEM_ROTATE, Integer.toString (item.getID ()), Integer.toString (FACE_WEST))); //$NON-NLS-1$
				}
				menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
				menuRotate.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Terrain.8"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$

				sm.addItem (menuRotate);
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
			}

			Container container = Game.getWorld ().getContainer (item.getID ());
			if (container != null) {
				container.fillMenu (sm, imi.getName ());
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
			}
			sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Item.0") + imi.getName (), null, CommandPanel.COMMAND_DESTROY_ENTITY, null, null, cell.getCoordinates ().toPoint3D (), Color.ORANGE)); //$NON-NLS-1$
			sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
		}
	}


	public boolean nextTurn () {
		ItemManagerItem imi = ItemManager.getItem (getIniHeader ());

		// Edad
		if (getMaxAge () > 0) {
			// Tiene fecha de caducidad, lo hacemos "crecer" y controlamos que no muera de viejo
			setAge (getAge () + 1);

			if (getAge () >= getMaxAge ()) {
				// En teoría muere, pero vamos a mirar el maxAgeNeedsWater
				if (imi.isMaxAgeNeedsWater ()) {
					// Ok, sólo muere si tiene agua cerca (se usa para el wheat por ejemplo)
					int iRadius = imi.getMaxAgeNeedsWaterRadius ();
					boolean bWaterNear = false;
					forWater: for (int x = -iRadius; x <= iRadius; x++) {
						for (int y = -iRadius; y <= iRadius; y++) {
							for (int z = 0; z <= 1; z++) {
								if (Utils.isInsideMap (x + getX (), y + getY (), getZ () + z)) {
									Terrain terrain = World.getCell (x + getX (), y + getY (), getZ () + z).getTerrain ();
									if (terrain.getFluidType () == Terrain.FLUIDS_WATER && terrain.getFluidCount () > 0) {
										bWaterNear = true;
										break forWater;
									}
								}
							}
						}
					}

					if (bWaterNear) {
						return true;
					} else {
						// Ponemos la edad a 0
						setAge (0);
					}
				} else {
					// No tiene needsWater, miramos el needsItems
					if (imi.getMaxAgeNeedsItems () != null && imi.getMaxAgeNeedsItems ().size () > 0) {
						// Vamos a ver si tiene items cerca para poder morir, tiene que tenerlos todos
						int iRadius = imi.getMaxAgeNeedsItemsRadius ();
						String sItem;
						int iItemIniHeader;
						boolean bAllItemsNear = true;
						for (int i = 0; i < imi.getMaxAgeNeedsItems ().size (); i++) {
							sItem = imi.getMaxAgeNeedsItems ().get (i);
							iItemIniHeader = UtilsIniHeaders.getIntIniHeader (sItem);

							boolean bItemNear = false;
							forRadius: for (int x = -iRadius; x <= iRadius; x++) {
								for (int y = -iRadius; y <= iRadius; y++) {
									for (int z = 0; z <= 1; z++) {
										if (x != 0 || y != 0 || z != 0) {
											if (Utils.isInsideMap (x + getX (), y + getY (), getZ () + z)) {
												Item it = World.getCell (x + getX (), y + getY (), getZ () + z).getItem ();
												if (it != null && it.getNumericIniHeader () == iItemIniHeader) {
													// Tengui, pasamos al siguiente
													bItemNear = true;
													break forRadius;
												}
											}
										}
									}
								}
							}

							if (!bItemNear) {
								// Item no encontrado, no puede morir y salimos
								bAllItemsNear = false;
								break;
							}
						}

						if (!bAllItemsNear) {
							setAge (0);
						} else {
							return true;
						}
					} else {
						return true;
					}
				}
			}
		}

		// Spawn
		if (getSpawnTurns () != -1) {
			setSpawnTurns (getSpawnTurns () - 1);
			if (getSpawnTurns () <= 0) {
				// Toca spawn
				if (imi.getSpawn () == null) {
					setSpawnTurns (-1);
				} else {
					setSpawnTurns (Utils.launchDice (imi.getSpawnTurns ()));

					// Miramos si es living o item
					boolean bItem = ItemManager.getItem (imi.getSpawn ()) != null;
					if (!bItem) {
						if (LivingEntityManager.getItem (imi.getSpawn ()) == null) {
							// Miramos que sea living, sino, error y pafuera
							Log.log (Log.LEVEL_ERROR, Messages.getString ("Item.9") + imi.getSpawn () + "]", getClass ().toString ()); //$NON-NLS-1$ //$NON-NLS-2$
							return false;
						}
					}

					// Miramos que no tenga el máximo al lado (sólo aplica con items)
					boolean bMaxReached = false;
					if (bItem && imi.getSpawnMaxItems () > 0) {
						int numChilds = getNumNeighbors (imi.getSpawn ());
						bMaxReached = (numChilds >= imi.getSpawnMaxItems ());

						// Máximo de items (performance issue)
						if (!bMaxReached && Item.getNumItemsTotal (imi.getSpawn (), World.MAP_DEPTH - 1) > 2048) {
							bMaxReached = true;
						}
					}

					if (!bMaxReached) {
						// Ok, no hay suficientes vecinos, toca meter uno
						switch (Utils.getRandomBetween (1, 8)) {
							case 1:
								newChild ((short) (getX () - 1), (short) (getY () - 1), (short) getZ (), imi.getSpawn (), bItem);
								break;
							case 2:
								newChild ((short) (getX () - 1), getY (), getZ (), imi.getSpawn (), bItem);
								break;
							case 3:
								newChild ((short) (getX () - 1), (short) (getY () + 1), getZ (), imi.getSpawn (), bItem);
								break;
							case 4:
								newChild (getX (), (short) (getY () - 1), getZ (), imi.getSpawn (), bItem);
								break;
							case 5:
								newChild (getX (), (short) (getY () + 1), getZ (), imi.getSpawn (), bItem);
								break;
							case 6:
								newChild ((short) (getX () + 1), (short) (getY () - 1), getZ (), imi.getSpawn (), bItem);
								break;
							case 7:
								newChild ((short) (getX () + 1), getY (), getZ (), imi.getSpawn (), bItem);
								break;
							case 8:
								newChild ((short) (getX () + 1), (short) (getY () + 1), getZ (), imi.getSpawn (), bItem);
								break;
							default:
						}
					}
				}
			}
		}

		// Trampas
		if (getTrapCooldown () != -1) {
			if (isLocked () && isOperative ()) {
				if (getTrapCooldown () > 0) {
					setTrapCooldown (getTrapCooldown () - 1);
					// Si llega a 0 le ponemos el gráfico de ready
					if (getTrapCooldown () == 0) {
						changeGraphic (getIniHeader ());
					}
				} else {
					// Ya toca spawn, miramos si hay livings encima
					ArrayList<LivingEntity> alLivings = World.getCell (getCoordinates ()).getLivings ();
					boolean salta = false;
					if (alLivings != null) {
						HateData hateData = imi.getTrapTargets ();
						for (int i = 0; i < alLivings.size (); i++) {
							if (!LivingEntityManager.getItem (alLivings.get (i).getIniHeader ()).isEvadeTraps ()) {
								for (int e = 0; e < imi.getTrapEffects ().size (); e++) {
									if (hateData.isHate (alLivings.get (i))) {
										alLivings.get (i).addEffect (EffectManager.getItem (imi.getTrapEffects ().get (e)), true);
										salta = true;
									}
								}
							}
						}
					}

					if (salta) {
						// Reseteamos el cooldown
						setTrapCooldown (Utils.launchDice (imi.getTrapCooldown ()));
						if (getTrapCooldown () < 0) {
							setTrapCooldown (0);
						}

						// Gráfico "ON"
						if (imi.getTrapOnIcon () != null && imi.getTrapOnIcon ().length () > 0) {
							changeGraphic (imi.getTrapOnIcon ());
						}
					}
				}
			}
		}

		/*
		 * // Fluids elevator if (getZ () > 0 && imi.isFluidsElevator ()) { Cell cell = World.getCell (getCoordinates ()); if (cell.getTerrain ().getFluidCount () > 3) { // Fuerza 3 o más, sube 2 fluidos arriba Cell upperCell = World.getCell (getX (), getY (), getZ () - 1); if (upperCell.getTerrain ().getFluidCount () < Terrain.FLUIDS_COUNT_MAX) { World.moveSingleFluid (cell, upperCell, false,
		 * Point3DShort.getPoolInstance (cell.getCoordinates ()), Point3DShort.getPoolInstance (upperCell.getCoordinates ()), new ArrayList<Point3DShort> ());
		 * 
		 * if (upperCell.getTerrain ().getFluidCount () < Terrain.FLUIDS_COUNT_MAX) { World.moveSingleFluid (cell, upperCell, false, Point3DShort.getPoolInstance (cell.getCoordinates ()), Point3DShort.getPoolInstance (upperCell.getCoordinates ()), new ArrayList<Point3DShort> ());
		 * 
		 * if (upperCell.getTerrain ().getFluidCount () < Terrain.FLUIDS_COUNT_MAX) { World.moveSingleFluid (cell, upperCell, false, Point3DShort.getPoolInstance (cell.getCoordinates ()), Point3DShort.getPoolInstance (upperCell.getCoordinates ()), new ArrayList<Point3DShort> ()); }
		 * 
		 * }
		 * 
		 * Game.getWorld ().addFluidCellToProcess (getX (), getY (), getZ () - 1); } } }
		 */
		return false;
	}


	/**
	 * Crea un nuevo hijo en la coordenada pasada, comprueba que esté vacía
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param sIniHeader Iniheader del item
	 * @param bItem Indica si es item o living
	 */
	private void newChild (short x, short y, short z, String sIniHeader, boolean bItem) {
		if (!Utils.isInsideMap (x, y, z)) {
			return;
		}

		Cell cell = World.getCell (x, y, z);
		if (bItem) {
			// Creamos el item si en la celda destino no hay otro item
			ItemManagerItem imi = ItemManager.getItem (sIniHeader);
			if (Item.isCellAvailableForItem (imi, x, y, z, true, true)) {
				Item newItem = Item.createItem (imi);
				newItem.setOperative (true);
				newItem.setLocked (imi.isLocked ());
				newItem.init (x, y, z);
				cell.setEntity (newItem);
			}
		} else {
			// Living
			LivingEntityManagerItem lemi = LivingEntityManager.getItem (sIniHeader);
			if (LivingEntity.isCellAllowed (cell)) {
				// Cabe, lo metemos
				boolean bDiscovered = cell.isDiscovered ();
				World.addNewLiving (lemi.getIniHeader (), lemi.getType (), bDiscovered, getX (), getY (), getZ (), true);
			}
		}
	}


	/**
	 * Devuelve el número de items adyacentes del tipo pasado
	 * 
	 * @param sIniHeader
	 * @param bItem Indica si es item o living
	 * @return el número de items adyacentes del tipo pasado
	 */
	private int getNumNeighbors (String sIniHeader) {
		int iNum = 0;
		Entity entity;
		for (int x = getX () - 1; x <= getX () + 1; x++) {
			for (int y = getY () - 1; y <= getY () + 1; y++) {
				if (x >= 0 && x < World.MAP_WIDTH && y >= 0 && y < World.MAP_HEIGHT) {
					if (x != getX () || y != getY ()) {
						// Item
						entity = World.getCell (x, y, getZ ()).getEntity ();
						if (entity != null && entity.getIniHeader ().equals (sIniHeader)) {
							iNum++;
						}
					}
				}
			}
		}

		return iNum;
	}


	public String getTileName () {
		return ItemManager.getItem (getIniHeader ()).getName ();
	}


	public float getFacingDirectionYOffset (int facingDirection) {
		float tamanyo = getBaseTileSetTexY1 () - getBaseTileSetTexY0 ();

		if (facingDirection == LivingEntity.FACING_DIRECTION_NORTH) {
			return (getBaseTileSetTexY0 () + ((float) 2 * tamanyo));
		} else if (facingDirection == LivingEntity.FACING_DIRECTION_SOUTH) {
			return (getBaseTileSetTexY0 () + ((float) 4 * tamanyo));
		} else if (facingDirection == LivingEntity.FACING_DIRECTION_EAST) {
			return (getBaseTileSetTexY0 () + ((float) 2 * tamanyo));
		} else if (facingDirection == LivingEntity.FACING_DIRECTION_WEST) {
			return (getBaseTileSetTexY0 () + ((float) 4 * tamanyo));
		} else if (facingDirection == LivingEntity.FACING_DIRECTION_NORTH_EAST) {
			return (getBaseTileSetTexY0 () + tamanyo);
		} else if (facingDirection == LivingEntity.FACING_DIRECTION_NORTH_WEST) {
			return (getBaseTileSetTexY0 () + ((float) 3 * tamanyo));
		} else if (facingDirection == LivingEntity.FACING_DIRECTION_SOUTH_EAST) {
			return (getBaseTileSetTexY0 () + ((float) 3 * tamanyo));
		}

		// else if (facingDirection == FACING_DIRECTION_SOUTH_WEST) {
		return (getBaseTileSetTexY0 ());
		// }
	}


	public static boolean shouldFall (Cell[][][] cells, ItemManagerItem imi, Point3DShort p3dItem) {
		// Fall
		Cell cell = cells[p3dItem.x][p3dItem.y][p3dItem.z];
		if (cell.isDigged ()) {
			Cell cellDown = cells[p3dItem.x][p3dItem.y][p3dItem.z + 1]; // Si la de arriba se puede diggar siempre habrá un z+1
			// Toca caer
			boolean bFall = true;
			// Miramos si abajo hay un item base=true
			Item itemDown = cellDown.getItem ();
			ItemManagerItem imiDown = (itemDown == null) ? null : ItemManager.getItem (itemDown.getIniHeader ());
			if (imiDown != null && (imiDown.isBase () || imiDown.isZoneMergerUp ())) {
				bFall = false;
			} else {
				// Miramos si hay glue
				if (imi.isGlue ()) {
					// Si tiene terreno o otro item glue al lado no se cae
					if (p3dItem.x > 0) {
						Cell cellAux = cells[p3dItem.x - 1][p3dItem.y][p3dItem.z];
						if (cellAux.isMined ()) {
							Item itemAux = cellAux.getItem ();
							if (itemAux != null && ItemManager.getItem (itemAux.getIniHeader ()).isGlue ()) {
								bFall = false;
							}
						} else {
							bFall = false;
						}
					}
					if (bFall && p3dItem.x < (World.MAP_WIDTH - 1)) {
						Cell cellAux = cells[p3dItem.x + 1][p3dItem.y][p3dItem.z];
						if (cellAux.isMined ()) {
							Item itemAux = cellAux.getItem ();
							if (itemAux != null && ItemManager.getItem (itemAux.getIniHeader ()).isGlue ()) {
								bFall = false;
							}
						} else {
							bFall = false;
						}
					}
					if (bFall && p3dItem.y > 0) {
						Cell cellAux = cells[p3dItem.x][p3dItem.y - 1][p3dItem.z];
						if (cellAux.isMined ()) {
							Item itemAux = cellAux.getItem ();
							if (itemAux != null && ItemManager.getItem (itemAux.getIniHeader ()).isGlue ()) {
								bFall = false;
							}
						} else {
							bFall = false;
						}
					}
					if (bFall && p3dItem.y < (World.MAP_HEIGHT - 1)) {
						Cell cellAux = cells[p3dItem.x][p3dItem.y + 1][p3dItem.z];
						if (cellAux.isMined ()) {
							Item itemAux = cellAux.getItem ();
							if (itemAux != null && ItemManager.getItem (itemAux.getIniHeader ()).isGlue ()) {
								bFall = false;
							}
						} else {
							bFall = false;
						}
					}
					if (bFall && p3dItem.z > 0) {
						Cell cellAux = cells[p3dItem.x][p3dItem.y][p3dItem.z - 1];
						if (cellAux.isMined ()) {
							Item itemAux = cellAux.getItem ();
							if (itemAux != null && ItemManager.getItem (itemAux.getIniHeader ()).isGlue ()) {
								bFall = false;
							}
						} else {
							bFall = false;
						}
					}
				}
			}
			return bFall;
		}

		return false;
	}


	public void checkFall () {
		ItemManagerItem imi = ItemManager.getItem (getIniHeader ());

		if (shouldFall (World.getCells (), imi, getCoordinates ())) {
			delete ();

			setLocked (imi.isLocked ());
			setOperative (imi.isAlwaysOperative ());
			Cell cellDown = World.getCell (getCoordinates ().x, getCoordinates ().y, getCoordinates ().z + 1);
			Item itemDown = cellDown.getItem ();

			// Si abajo hay un item, buscamos una celda adyacente, si no hay celda adyacente el item se pierde
			if (itemDown != null) {
				ArrayList<Cell> alNeighbours = new ArrayList<Cell> ();
				Cell cellTmp;
				for (short i = (getCoordinates ().x > 0) ? (short) -1 : (short) 0, iMax = (getCoordinates ().x < (World.MAP_WIDTH - 1)) ? (short) 1 : (short) 0; i <= iMax; i++) {
					for (short j = (getCoordinates ().y > 0) ? (short) -1 : (short) 0, jMax = (getCoordinates ().y < (World.MAP_HEIGHT - 1)) ? (short) 1 : (short) 0; j <= jMax; j++) {
						cellTmp = World.getCell (cellDown.getCoordinates ().x + i, cellDown.getCoordinates ().y + j, cellDown.getCoordinates ().z);
						if (cellTmp.isEmpty ()) {
							if (cellTmp.isDigged () || Item.isCellAvailableForItem (imi, (short) (cellDown.getCoordinates ().x + i), (short) (cellDown.getCoordinates ().y + j), cellDown.getCoordinates ().z, false, true, false, true)) {
								alNeighbours.add (cellTmp);
							}
						}
					}
				}

				if (alNeighbours.size () > 0) {
					int iRand = Utils.getRandomBetween (0, alNeighbours.size () - 1);
					Cell cellRandom = alNeighbours.get (iRand);
					init (cellRandom.getCoordinates ().x, cellRandom.getCoordinates ().y, cellRandom.getCoordinates ().z);
					cellRandom.setEntity (this);
					// } else {
					// No cabe, se destruye
				}
			} else {
				if (Item.isCellAvailableForItem (imi, getCoordinates ().x, getCoordinates ().y, (short) (getCoordinates ().z + 1), true, true)) {
					init (getCoordinates ().x, getCoordinates ().y, (short) (getCoordinates ().z + 1));
					cellDown.setEntity (this);

				}
			}
			// cell.setEntity ();
		}
	}


	public static void clear () {
		mapItems = new HashMap<Integer, ArrayList<Integer>> ();
		mapItemsLocked = new HashMap<Integer, ArrayList<Integer>> ();
	}


	@SuppressWarnings("unchecked")
	public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal (in);
		flags = in.readByte ();
		prerequisites = (ArrayList<String>) in.readObject ();
		age = in.readInt ();
		maxAge = in.readInt ();
		hp = in.readInt ();
		maxHp = in.readInt ();
		spawnTurns = in.readInt ();
		trapCooldown = in.readInt ();
	}


	public void writeExternal (ObjectOutput out) throws IOException {
		super.writeExternal (out);
		out.writeByte (flags);
		out.writeObject (prerequisites);
		out.writeInt (age);
		out.writeInt (maxAge);
		out.writeInt (hp);
		out.writeInt (maxHp);
		out.writeInt (spawnTurns);
		out.writeInt (trapCooldown);
	}
}
