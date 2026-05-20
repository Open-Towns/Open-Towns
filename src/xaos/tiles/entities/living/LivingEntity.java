package xaos.tiles.entities.living;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import xaos.Towns;
import xaos.TownsProperties;
import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.campaign.TutorialTrigger;
import xaos.data.CaravanData;
import xaos.data.CitizenData;
import xaos.data.DropData;
import xaos.data.EffectData;
import xaos.data.EquippedData;
import xaos.data.FocusData;
import xaos.data.HateData;
import xaos.data.HeroPrerequisite;
import xaos.data.LivingEntityData;
import xaos.data.SoldierData;
import xaos.effects.EffectManager;
import xaos.effects.EffectManagerItem;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.MessagesPanel;
import xaos.panels.UI.UIPanel;
import xaos.panels.menus.SmartMenu;
import xaos.property.PropertyFile;
import xaos.skills.SkillManagerItem;
import xaos.tasks.Task;
import xaos.tiles.Cell;
import xaos.tiles.Tile;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.items.military.PrefixSuffixManager;
import xaos.tiles.entities.living.enemies.Enemy;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.tiles.entities.living.heroes.HeroManager;
import xaos.tiles.entities.living.heroes.HeroTask;
import xaos.tiles.entities.living.projectiles.Projectile;
import xaos.tiles.terrain.Terrain;
import xaos.utils.AStarNodo;
import xaos.utils.AStarQueue;
import xaos.utils.AStarQueueItem;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.UtilFont;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsIniHeaders;
import xaos.zones.Zone;
import xaos.zones.ZoneHeroRoom;
import xaos.zones.ZonePersonal;


public abstract class LivingEntity extends Entity implements Externalizable {

	private static final long serialVersionUID = 3084403150672553278L;

	public final static int TYPE_UNKNOWN = -1;
	public final static int TYPE_CITIZEN = 1;
	public final static int TYPE_ENEMY = 2;
	public final static int TYPE_FRIENDLY = 3;
	public final static int TYPE_HERO = 4;
	public final static int TYPE_ALLY = 5;

	public final static byte FACING_DIRECTION_NORTH = 1;
	public final static byte FACING_DIRECTION_SOUTH = 2;
	public final static byte FACING_DIRECTION_EAST = 3;
	public final static byte FACING_DIRECTION_WEST = 4;
	public final static byte FACING_DIRECTION_NORTH_EAST = 5;
	public final static byte FACING_DIRECTION_NORTH_WEST = 6;
	public final static byte FACING_DIRECTION_SOUTH_EAST = 7;
	public final static byte FACING_DIRECTION_SOUTH_WEST = 8;

	public final static int DAMAGE_ANIMATION_FPS = 8;
	public final static int ATTACK_ANIMATION_MAX_COUNTER = 8;


	/**
	 * Número de livings por tipo (discovered)
	 */
	private static HashMap<String, Integer> mapLivingsDiscovered = new HashMap<String, Integer> ();

	/**
	 * Número de livings por tipo (UNdiscovered)
	 */
	private static HashMap<String, Integer> mapLivingsUndiscovered = new HashMap<String, Integer> ();

	// Lista de puntos hasta destino (nunca será nulo)
	private ArrayList<Point3DShort> path = new ArrayList<Point3DShort> ();

	// Flags
	private final static byte FLAG_WAITING_FOR_PATH = 1; // 00000001 (Indica si el ser viviente está esperando camino)
	private final static byte FLAG_GROUPING = 2; // 00000010 (Indica si llama a sus amigos cuando le atacan)
	private final static byte FLAG_FIGHTING = 4; // 00000100 (Indica si está luchando)

	private byte flags;

	private transient int offset_carry_x;
	private transient int offset_carry_y;
	private transient int offset_head_x;
	private transient int offset_head_y;
	private transient int offset_body_x;
	private transient int offset_body_y;
	private transient int offset_legs_x;
	private transient int offset_legs_y;
	private transient int offset_feet_x;
	private transient int offset_feet_y;
	private transient int offset_weapon_x;
	private transient int offset_weapon_y;

	private LivingEntityData livingEntityData;
	private EquippedData equippedData;

	// Age (Edad, para saber cuando muere)
	private int age; // Edad actual del friendly
	private int maxAge; // Edad máxima del friendly

	private byte facingDirection;

	private FocusData focusData; // Guarda el ID + type de un entity al que está atacando

	private int attackSpeedCounter; // Cuando attackTurn > que el atackTurnCurrent es que ya puede atacar, sino suma 1 y espera
	private int walkSpeedCounter; // Cuando walk > que el walkTurnCurrent es que ya puede caminar, sino suma 1 y espera

	private Point2D.Float positionOffset = new Point2D.Float (0, 0); // Se usa en el dibujado, para ir moviendo poco a poco a la living (en vez de celda a celda)
	private Point2D.Float positionOffsetConstants = new Point2D.Float (0, 0); // Se usa en el dibujado, es lo que hay que añadir al offset a cada frame

	private transient int checkLOSCounter;
	private transient int skillAnimationCounter;

	// Food
	private int foodNeededTurns;
	private int fleeing;

	// Damage animation
	private transient int damageAnimationCounter;
	private transient String damageAnimationText;
	private transient int damageAnimationTextWidth;

	// Attack animation
	private transient int attackAnimationCounter;


	public LivingEntity () {
		super ();
	}


	public LivingEntity (String iniHeader) {
		super (getAltGraphics (iniHeader));

		// Alternative graphics
		LivingEntityManagerItem lemi = LivingEntityManager.getItem (getIniHeader ()); // Ojo, aquí hay que usar el getIniHeader, pq puede estar cambiado

		// Data (nombre, vida, ...)
		setLivingEntityData (lemi.getRandom ());

		// Equipo
		setEquippedData (new EquippedData ());
		if (lemi.getEquip () != null) {
			for (int i = 0; i < lemi.getEquip ().size (); i++) {
				Item item = Item.createItem (ItemManager.getItem (lemi.getEquip ().get (i)));
				if (item instanceof MilitaryItem) {
					equip ((MilitaryItem) item);
				} else {
					Log.log (Log.LEVEL_ERROR, Messages.getString ("LivingEntity.6") + lemi.getEquip ().get (i) + "]", getClass ().toString ()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		// Age
		setAge (0);
		setMaxAge (Utils.launchDice (lemi.getMaxAge ()));

		// Enemigo al que focusea
		this.focusData = new FocusData (-1, TYPE_UNKNOWN);

		// Facing
		setFacingDirection ((byte) Utils.getRandomBetween (1, 8));

		// Efectos
		if (lemi.getEffects () != null) {
			for (int i = 0; i < lemi.getEffects ().size (); i++) {
				addEffect (EffectManager.getItem (lemi.getEffects ().get (i)), true, false);
			}
		}

		// Food needed (animals)
		setFoodNeededTurns (lemi.getFoodNeededTurns ());

		refreshTransients ();
	}


	public static String getAltGraphics (String sIniHeader) {
		LivingEntityManagerItem lemi = LivingEntityManager.getItem (sIniHeader);

		if (lemi.getAltGraphics () != null && lemi.getAltGraphics ().size () > 0) {
			int iRandom = Utils.getRandomBetween (0, lemi.getAltGraphics ().size ());
			// Si el random == altGraphics.size() usamos el gráfico original (por lo tanto no hacemos nada)
			if (iRandom < lemi.getAltGraphics ().size ()) {
				return lemi.getAltGraphics ().get (iRandom);
			}
		}

		return sIniHeader;
	}


	public void init (int x, int y, int z) {
		init (x, y, z, World.getCell (x, y, z).isDiscovered ());
	}


	public void init (int x, int y, int z, boolean bDiscovered) {
		super.init (x, y, z);

		// Num items
		addLiving (this, bDiscovered);

		// Lo metemos en la lista de livings que toque
		World.getLivings (bDiscovered).put (Integer.valueOf (getID ()), this);

		// Caso aldeanos o heros, se mete en la lista pertinente y se suma 1 al num cits/soldiers (si es el caso)
		LivingEntityManagerItem lemi = LivingEntityManager.getItem (getIniHeader ());
		if (lemi.getType () == TYPE_CITIZEN) {
			if (((Citizen) this).getSoldierData ().isSoldier ()) {
				World.getSoldierIDs ().add (getID ());
			} else {
				World.getCitizenIDs ().add (getID ());
			}
		} else if (lemi.getType () == TYPE_HERO) {
			World.getHeroIDs ().add (getID ());
		}
	}


	public void refreshTransients () {
		super.refreshTransients ();

		loadOffsets ();
		if (getLivingEntityData () != null) {
			getLivingEntityData ().refreshTransients (LivingEntityManager.getItem (getIniHeader ()));

			if (getEquippedData () != null) {
				getEquippedData ().refreshTransients ();
			}

			// Effects de changegraphics
			if (getLivingEntityData ().getEffects () != null) {
				EffectManagerItem emi;
				for (int i = 0; i < getLivingEntityData ().getEffects ().size (); i++) {
					emi = EffectManager.getItem (getLivingEntityData ().getEffects ().get (i).getEffectID ());

					// changeGraphics
					if (getLivingEntityData ().getEffects ().get (i).isGraphicChange ()) {
						changeGraphic (getLivingEntityData ().getEffects ().get (i).getGraphicChange ());
					}
					// castTargets
					if (emi != null) {
						getLivingEntityData ().getEffects ().get (i).setCastTargets (new HateData (emi.getCastTargets ()));
					}
				}
			}

			getLivingEntityData ().setModifiers (this);

			// Check LOS counter
			setCheckLOSCounter (Utils.getRandomBetween (0, getLivingEntityData ().getLOSCurrent ()));
		}

		refreshFacingDirection (getFacingDirection ());
	}


	/**
	 * Recorre todas las celdas del mundo y lanza un init por cada living encontrado
	 */
	public static void loadLivings () {
		World.getItems (); // Por si no hay items en la partida, que cree la hash

		ArrayList<LivingEntity> alLivings;
		for (short x = 0; x < World.MAP_WIDTH; x++) {
			for (short y = 0; y < World.MAP_HEIGHT; y++) {
				for (short z = 0; z < World.MAP_DEPTH; z++) {
					alLivings = World.getCell (x, y, z).getLivings ();
					if (alLivings != null) {
						if (alLivings.size () == 0) {
							World.getCell (x, y, z).freeLivingsMem ();
						} else {
							for (int i = 0; i < alLivings.size (); i++) {
								alLivings.get (i).init (x, y, z);
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Añade el Living a la lista y suma 1 al número de los mismos
	 * 
	 * @param itemIniHeader IniHeader del living
	 * 
	 * @return el número de livings que hay de ese tipo, incluyendo el acabado de añadir
	 */
	public static int addLiving (LivingEntity living, boolean bDiscovered) {
		String sIniHeader = living.getIniHeader ();
		int iNum = getNumLivings (sIniHeader, bDiscovered);
		setNumLivings (sIniHeader, (iNum + 1), bDiscovered);

		return (iNum + 1);
	}


	/**
	 * Elimina 1 unidad de item de la lista
	 * 
	 * @param itemIniHeader IniHeader del item
	 */
	public static void removeLiving (LivingEntity living, boolean bDiscovered) {
		String sIniHeader = living.getIniHeader ();
		int iNum = getNumLivings (sIniHeader, bDiscovered);

		if (iNum > 0) {
			iNum--;
			setNumLivings (sIniHeader, iNum, bDiscovered);
		}
	}


	/**
	 * Retorna el Hashmap con los livings/cantidad
	 * 
	 * @return el Hashmap con los livings/cantidad
	 */
	public static HashMap<String, Integer> getMapLivings (boolean bDiscovered) {
		return (bDiscovered) ? mapLivingsDiscovered : mapLivingsUndiscovered;
	}


	/**
	 * Devuelve el número de livings en el mundo del tipo pasado
	 * 
	 * @param sIniHeader IniHeader del living
	 * 
	 * @return el número de livings en el mundo del tipo pasado
	 */
	public static int getNumLivings (String sIniHeader, boolean bDiscovered) {
		Integer iNumber = getMapLivings (bDiscovered).get (sIniHeader);

		if (iNumber == null) {
			return 0;
		}

		return iNumber.intValue ();
	}


	/**
	 * Setea el número de livings para un living dado
	 * 
	 * @param sIniHeader IniHeader del living
	 * @param iNumber Cantidad
	 */
	public static void setNumLivings (String sIniHeader, int iNumber, boolean bDiscovered) {
		getMapLivings (bDiscovered).put (sIniHeader, Integer.valueOf(iNumber));
	}


	/**
	 * Carga todos los offsets para el dibujado
	 */
	private void loadOffsets () {
		String sAux = Towns.getPropertiesString (PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + getIniHeader () + "]CARRY_OFFSET"); //$NON-NLS-1$ //$NON-NLS-2$
		setOffset_carry_x (getOffsetValue (sAux, true));
		setOffset_carry_y (getOffsetValue (sAux, false));
		sAux = Towns.getPropertiesString (PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + getIniHeader () + "]HEAD_OFFSET"); //$NON-NLS-1$ //$NON-NLS-2$
		setOffset_head_x (getOffsetValue (sAux, true));
		setOffset_head_y (getOffsetValue (sAux, false));
		sAux = Towns.getPropertiesString (PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + getIniHeader () + "]BODY_OFFSET"); //$NON-NLS-1$ //$NON-NLS-2$
		setOffset_body_x (getOffsetValue (sAux, true));
		setOffset_body_y (getOffsetValue (sAux, false));
		sAux = Towns.getPropertiesString (PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + getIniHeader () + "]LEGS_OFFSET"); //$NON-NLS-1$ //$NON-NLS-2$
		setOffset_legs_x (getOffsetValue (sAux, true));
		setOffset_legs_y (getOffsetValue (sAux, false));
		sAux = Towns.getPropertiesString (PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + getIniHeader () + "]FEET_OFFSET"); //$NON-NLS-1$ //$NON-NLS-2$
		setOffset_feet_x (getOffsetValue (sAux, true));
		setOffset_feet_y (getOffsetValue (sAux, false));
		sAux = Towns.getPropertiesString (PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + getIniHeader () + "]WEAPON_OFFSET"); //$NON-NLS-1$ //$NON-NLS-2$
		setOffset_weapon_x (getOffsetValue (sAux, true));
		setOffset_weapon_y (getOffsetValue (sAux, false));
	}


	/**
	 * Analiza la cadena de offset (que es "x, y") y devuelve el valor numérico de una de las 2
	 * 
	 * @param sValue Cadena de offset
	 * @param firstValue Indica si hay que devolver el primer valor de la cadena o el segundo
	 * @return
	 */
	private int getOffsetValue (String sValue, boolean firstValue) {
		if (sValue == null || sValue.length () == 0) {
			return 0;
		}

		// Buscamos la coma
		int iIndex = sValue.indexOf (',');
		if (iIndex == -1 || (iIndex + 1) == sValue.length ()) {
			return 0;
		}

		if (firstValue) {
			// La X
			try {
				return Integer.parseInt (sValue.substring (0, iIndex));
			}
			catch (Exception e) {
				Log.log (Log.LEVEL_ERROR, Messages.getString ("LivingEntity.0") + sValue + "][" + e.toString () + "]", getClass ().toString ()); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				return 0;
			}
		} else {
			// La Y
			try {
				return Integer.parseInt (sValue.substring (iIndex + 1));
			}
			catch (Exception e) {
				Log.log (Log.LEVEL_ERROR, Messages.getString ("LivingEntity.3") + sValue + "][" + e.toString () + "]", getClass ().toString ()); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				return 0;
			}
		}
	}


	public void setCoordinates (Point3DShort point) {
		setCoordinates (point.x, point.y, point.z);
	}


	public void setCoordinates (short iX, short iY, short iZ) {
		int iIndex = -1;
		if (getX () != -1) { // Con esto miro que no sea una living nueva (que no tendría celda source)

			if (getX () != iX || getY () != iY || getZ () != iZ) {
				// Hay que moverlo de celda
				Cell cellSrc = World.getCell (getCoordinates ());
				if (cellSrc.getLivings () != null) {
					LivingEntity le;
					for (int i = 0; i < cellSrc.getLivings ().size (); i++) {
						le = cellSrc.getLivings ().get (i);
						if (le.getID () == getID ()) {
							// Encontrado
							iIndex = i;
							break;
						}
					}

					if (iIndex != -1) {
						// Debería pasar siempre
						le = cellSrc.getLivings ().remove (iIndex);
						World.getCell (iX, iY, iZ).addLiving (le);
					}
				}
			}
		}

		super.setCoordinates (iX, iY, iZ);

		if (iIndex != -1) {
			// Ha cambiado de celda
			updatePathConstantOffsets ();
		}
	}


	public ArrayList<Point3DShort> getPath () {
		if (path == null) {
			path = new ArrayList<Point3DShort> ();
		}

		return path;
	}


	public void setPath (ArrayList<Point3DShort> path) {
		if (path != null) {
			// Removemos puntos repetidos
			Point3DShort pLastPoint = getCoordinates ();
			int iIndex = 0;
			while (iIndex < path.size ()) {
				if (path.get (iIndex).equals (pLastPoint)) {
					path.remove (iIndex);
				} else {
					pLastPoint = path.get (iIndex);
					iIndex++;
				}
			}
		}

		this.path = path;
		updatePathConstantOffsets ();
	}


	/**
	 * Actualiza el offset segun la constante
	 */
	public void updatePathOffsets () {
		if (!Game.isPaused ()) {
			Point2D.Float pTMP = getPositionOffset ();
			Point2D.Float pTMP2 = getPositionOffsetConstants ();
			pTMP.x += pTMP2.x;
			pTMP.y += pTMP2.y;
		}
	}


	public void resetPathOffset () {
		Point2D.Float pTMP = getPositionOffset ();
		pTMP.x = 0;
		pTMP.y = 0;
	}


	/**
	 * Setea la constante, también cambia el facing direction
	 */
	protected void updatePathConstantOffsets () {
		// Current offset
		Point2D.Float pTMP = getPositionOffset ();
		pTMP.x = 0;
		pTMP.y = 0;

		if (getPath ().size () == 0) {
			// Constants offset
			Point2D.Float pTMP2 = getPositionOffsetConstants ();
			pTMP2.x = 0;
			pTMP2.y = 0;
		} else {
			Point3DShort p3dDestino = getPath ().get (0);
			updateFacingDirection (getX (), getY (), getZ (), p3dDestino.x, p3dDestino.y, p3dDestino.z);

			// Constants offset
			int walkNeeded = Cell.getCellWalkNeeded (p3dDestino) - getWalkSpeedCounter ();
			if (walkNeeded < 1) {
				walkNeeded = 1;
			}

			int speed = getLivingEntityData ().getWalkSpeedCurrent ();
			if (LivingEntityManager.getItem (getIniHeader ()).getType () == TYPE_CITIZEN) {
				// Miramos la reducción de velocidad debido a hambre y felicidad
				int iMalusPCT = ((Citizen) this).getMalusSpeedPCT ();
				if (iMalusPCT > 0 && iMalusPCT != 100) {
					speed = (speed * iMalusPCT) / 100;
				}
				// Miramos si tiene boost debido a un soldado BOSS_AROUND cercano
				if (((Citizen) this).getCitizenData ().getBoostCounter () > 0) {
					// Tiene boost
					speed = (speed * SoldierData.BOOST_PCT_BOSS_AROUND_WALK) / 100;
				}

				// Eventos globales, sólo a townies
				if (Game.getWorld ().getGlobalEvents ().getWalkSpeedPCT () != 100) {
					speed = (speed * Game.getWorld ().getGlobalEvents ().getWalkSpeedPCT ()) / 100;
				}
			}

			// Reducimos si tiene effects con speedPCT != 100
			if (getLivingEntityData ().getEffects ().size () > 0) {
				ArrayList<EffectData> alEffects = getLivingEntityData ().getEffects ();
				EffectData effectData;
				for (int i = 0; i < alEffects.size (); i++) {
					effectData = alEffects.get (i);
					if (effectData.getSpeedPCT () != 100) {
						speed = (speed * effectData.getSpeedPCT ()) / 100;
					}
				}
			}

			if (speed < 1) {
				speed = 1;
			} else if (speed > 100) {
				speed = 100;
			}

			int turnsToCross;
			if (speed >= walkNeeded) {
				// 1 turno para cruzar la celda
				turnsToCross = 1;
			} else {
				turnsToCross = (walkNeeded / speed) + ((walkNeeded % speed == 0) ? 0 : 1);
			}

			// Calculamos las constants según hacia donde vaya
			float totalFrames = World.FRAMES_PER_TURN * turnsToCross;
			Point2D.Float pTMP2 = getPositionOffsetConstants ();
			pTMP2.x = (((p3dDestino.x - getX ()) + (p3dDestino.y - getY ())) * (Tile.TERRAIN_ICON_WIDTH / 2)) / totalFrames;
			pTMP2.y = (((p3dDestino.y - getY ()) - (p3dDestino.x - getX ())) * (Tile.TERRAIN_ICON_HEIGHT / 2)) / totalFrames;

			// Si está cambiando de nivel hay que sumar, restar a la Y
			if (p3dDestino.z != getZ ()) {
				pTMP2.y += ((p3dDestino.z - getZ ()) * (Tile.TERRAIN_ICON_HEIGHT / 2)) / totalFrames;
			}
		}
	}


	public void updateAnimation (boolean bAnimatedWhenIdle) {
		if (!bAnimatedWhenIdle && getPath ().isEmpty ()) {
			super.resetAnimationLiving (false);
		} else {
			super.updateAnimation (false);
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


	public boolean isWaitingForPath () {
		return (getFlags () & FLAG_WAITING_FOR_PATH) > 0;
	}


	public void setWaitingForPath (boolean waitingForPath) {
		if (waitingForPath) {
			setFlags ((byte) (getFlags () | FLAG_WAITING_FOR_PATH));
		} else {
			setFlags ((byte) (getFlags () & ~FLAG_WAITING_FOR_PATH));
		}
	}


	public int getOffset_carry_x () {
		return offset_carry_x;
	}


	public void setOffset_carry_x (int offset_carry_x) {
		this.offset_carry_x = offset_carry_x;
	}


	public int getOffset_carry_y () {
		return offset_carry_y;
	}


	public void setOffset_carry_y (int offset_carry_y) {
		this.offset_carry_y = offset_carry_y;
	}


	public int getOffset_head_x () {
		return offset_head_x;
	}


	public void setOffset_head_x (int offset_head_x) {
		this.offset_head_x = offset_head_x;
	}


	public int getOffset_head_y () {
		return offset_head_y;
	}


	public void setOffset_head_y (int offset_head_y) {
		this.offset_head_y = offset_head_y;
	}


	public int getOffset_body_x () {
		return offset_body_x;
	}


	public void setOffset_body_x (int offset_body_x) {
		this.offset_body_x = offset_body_x;
	}


	public int getOffset_body_y () {
		return offset_body_y;
	}


	public void setOffset_body_y (int offset_body_y) {
		this.offset_body_y = offset_body_y;
	}


	public int getOffset_legs_x () {
		return offset_legs_x;
	}


	public void setOffset_legs_x (int offset_legs_x) {
		this.offset_legs_x = offset_legs_x;
	}


	public int getOffset_legs_y () {
		return offset_legs_y;
	}


	public void setOffset_legs_y (int offset_legs_y) {
		this.offset_legs_y = offset_legs_y;
	}


	public int getOffset_feet_x () {
		return offset_feet_x;
	}


	public void setOffset_feet_x (int offset_feet_x) {
		this.offset_feet_x = offset_feet_x;
	}


	public int getOffset_feet_y () {
		return offset_feet_y;
	}


	public void setOffset_feet_y (int offset_feet_y) {
		this.offset_feet_y = offset_feet_y;
	}


	public int getOffset_weapon_x () {
		return offset_weapon_x;
	}


	public void setOffset_weapon_x (int offset_weapon_x) {
		this.offset_weapon_x = offset_weapon_x;
	}


	public int getOffset_weapon_y () {
		return offset_weapon_y;
	}


	public void setOffset_weapon_y (int offset_weapon_y) {
		this.offset_weapon_y = offset_weapon_y;
	}


	public void setLivingEntityData (LivingEntityData livingEntityData) {
		this.livingEntityData = livingEntityData;
	}


	public LivingEntityData getLivingEntityData () {
		return livingEntityData;
	}


	public void setEquippedData (EquippedData equippedData) {
		this.equippedData = equippedData;
	}


	public EquippedData getEquippedData () {
		return equippedData;
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


	public void setGrouping (boolean grouping) {
		if (grouping) {
			setFlags ((byte) (getFlags () | FLAG_GROUPING));
		} else {
			setFlags ((byte) (getFlags () & ~FLAG_GROUPING));
		}
	}


	public boolean hasGrouping () {
		return (getFlags () & FLAG_GROUPING) > 0;
	}


	public void setFighting (boolean fighting) {
		if (fighting) {
			setFlags ((byte) (getFlags () | FLAG_FIGHTING));
		} else {
			setFlags ((byte) (getFlags () & ~FLAG_FIGHTING));
		}
	}


	public boolean isFighting () {
		return (getFlags () & FLAG_FIGHTING) > 0;
	}


	public void setFacingDirection (byte facingDirection) {
		this.facingDirection = facingDirection;
	}


	public byte getFacingDirection () {
		return facingDirection;
	}


	public void updateFacingDirection (int x1, int y1, int z1, int x2, int y2, int z2) {
		byte facingReturn;
		// if (z1 != z2) {
		// // Sube/baja, no cambiamos la dirección
		// facingReturn = getFacingDirection ();
		// } else {
		// Miramos las 8 direcciones
		if (x1 == x2) {
			// Sólo se ha movido en el plano Y
			if (y1 < y2) {
				facingReturn = FACING_DIRECTION_SOUTH;
			} else if (y1 > y2) {
				facingReturn = FACING_DIRECTION_NORTH;
			} else {
				facingReturn = getFacingDirection ();
			}
		} else {
			if (y1 == y2) {
				// Sólo se ha movido en el plano X
				if (x1 < x2) {
					facingReturn = FACING_DIRECTION_EAST;
				} else if (x1 > x2) {
					facingReturn = FACING_DIRECTION_WEST;
				} else {
					facingReturn = getFacingDirection ();
				}
			} else {
				// Si llega aquí es que es una diagonal si o si
				if (x1 < x2) {
					if (y1 < y2) {
						facingReturn = FACING_DIRECTION_SOUTH_EAST;
					} else {
						facingReturn = FACING_DIRECTION_NORTH_EAST;
					}
				} else {
					if (y1 < y2) {
						facingReturn = FACING_DIRECTION_SOUTH_WEST;
					} else {
						facingReturn = FACING_DIRECTION_NORTH_WEST;
					}
				}
			}
			// }
		}

		// Miramos si hay que cambiar las coordenadas del tileset
		if (facingReturn != getFacingDirection ()) {
			refreshFacingDirection (facingReturn);
		}

		setFacingDirection (facingReturn);
	}


	private void refreshFacingDirection (int facingDirection) {
		if (LivingEntityManager.getItem (getIniHeader ()).isFacingDirections ()) {
			float tamanyo = getBaseTileSetTexY1 () - getBaseTileSetTexY0 ();

			setTileSetTexY0 (getFacingDirectionYOffset (facingDirection));
			setTileSetTexY1 (getTileSetTexY0 () + tamanyo);
		}
	}


	public float getFacingDirectionYOffset (int facingDirection) {
		float tamanyo = getBaseTileSetTexY1 () - getBaseTileSetTexY0 ();

		if (facingDirection == FACING_DIRECTION_NORTH) {
			return (getBaseTileSetTexY0 () + ((float) 2 * tamanyo));
		} else if (facingDirection == FACING_DIRECTION_SOUTH) {
			return (getBaseTileSetTexY0 () + ((float) 4 * tamanyo));
		} else if (facingDirection == FACING_DIRECTION_EAST) {
			return (getBaseTileSetTexY0 () + ((float) 2 * tamanyo));
		} else if (facingDirection == FACING_DIRECTION_WEST) {
			return (getBaseTileSetTexY0 () + ((float) 4 * tamanyo));
		} else if (facingDirection == FACING_DIRECTION_NORTH_EAST) {
			return (getBaseTileSetTexY0 () + tamanyo);
		} else if (facingDirection == FACING_DIRECTION_NORTH_WEST) {
			return (getBaseTileSetTexY0 () + ((float) 3 * tamanyo));
		} else if (facingDirection == FACING_DIRECTION_SOUTH_EAST) {
			return (getBaseTileSetTexY0 () + ((float) 3 * tamanyo));
		}

		// else if (facingDirection == FACING_DIRECTION_SOUTH_WEST) {
		return (getBaseTileSetTexY0 ());
		// }
	}


	public void setFocusData (FocusData fd) {
		this.focusData.setEntityID (fd.getEntityID ());
		this.focusData.setEntityType (fd.getEntityType ());

        setFighting (fd.getEntityID () != -1);
	}


	public FocusData getFocusData () {
		return focusData;
	}


	public void setAttackSpeedCounter (int attackSpeedCounter) {
		this.attackSpeedCounter = attackSpeedCounter;
	}


	public int getAttackSpeedCounter () {
		return attackSpeedCounter;
	}


	public void setWalkSpeedCounter (int walkSpeedCounter) {
		this.walkSpeedCounter = walkSpeedCounter;
	}


	public int getWalkSpeedCounter () {
		return walkSpeedCounter;
	}


	/**
	 * @param positionOffset the positionOffset to set
	 */
	public void setPositionOffset (Point2D.Float positionOffset) {
		this.positionOffset = positionOffset;
	}


	/**
	 * @return the positionOffset
	 */
	public Point2D.Float getPositionOffset () {
		return positionOffset;
	}


	/**
	 * @param positionOffsetConstants the positionOffsetConstants to set
	 */
	public void setPositionOffsetConstants (Point2D.Float positionOffsetConstants) {
		this.positionOffsetConstants = positionOffsetConstants;
	}


	/**
	 * @return the positionOffsetConstants
	 */
	public Point2D.Float getPositionOffsetConstants () {
		return positionOffsetConstants;
	}


	public int getCheckLOSCounter () {
		return checkLOSCounter;
	}


	public void setCheckLOSCounter (int iCheckLOSCounter) {
		this.checkLOSCounter = iCheckLOSCounter;
	}


	public void setSkillAnimationCounter (int skillAnimationCounter) {
		this.skillAnimationCounter = skillAnimationCounter;
	}


	public int getSkillAnimationCounter () {
		return skillAnimationCounter;
	}


	public void setFoodNeededTurns (int foodNeededTurns) {
		this.foodNeededTurns = foodNeededTurns;
	}


	public int getFoodNeededTurns () {
		return foodNeededTurns;
	}


	public void setDamageAnimationCounter (int damageAnimationCounter) {
		this.damageAnimationCounter = damageAnimationCounter;
	}


	public int getDamageAnimationCounter () {
		return damageAnimationCounter;
	}


	public void setDamageAnimationText (String damageAnimationText) {
		this.damageAnimationText = damageAnimationText;

		if (damageAnimationText != null) {
			setDamageAnimationTextWidth (UtilFont.getWidth (damageAnimationText));
		}
	}


	public String getDamageAnimationText () {
		return damageAnimationText;
	}


	public void setDamageAnimationTextWidth (int damageAnimationTextWidth) {
		this.damageAnimationTextWidth = damageAnimationTextWidth;
	}


	public int getDamageAnimationTextWidth () {
		return damageAnimationTextWidth;
	}


	public void setAttackAnimationCounter (int attackAnimationCounter) {
		this.attackAnimationCounter = attackAnimationCounter;
	}


	public int getAttackAnimationCounter () {
		return attackAnimationCounter;
	}


	/**
	 * Mueve a la living SI o SI (si puede, claro) Se usa cuando está encima de un muro (ocurre cuando se acaba de construir), encima de fluidos o en encima de un hole
	 * 
	 * @return true si ha podido, false si no puede (en ese caso debería palmar o algo)
	 */
	public boolean forceMove () {
		short currentX = getX ();
		short currentY = getY ();
		short currentZ = getZ ();
		HateData hateData = LivingEntityManager.getHateData (getIniHeader ());

		// Miramos las casillas posibles a las que puede ir
		// Hay que mirar que no haya un hated ahí
		ArrayList<Point3DShort> alPoints = new ArrayList<Point3DShort> ();
		for (int i = -1; i <= 1; i++) {
			nextcell: for (int j = -1; j <= 1; j++) {
				if (i != 0 || j != 0) {
					if (Utils.isInsideMap (currentX + i, currentY + j, currentZ)) {
						if (isCellAllowed (currentX + i, currentY + j, currentZ)) {
							// Miramos el hate

							// Por cada living en el destino miramos si la entity los odia
							ArrayList<LivingEntity> alLivings = World.getCell (currentX + i, currentY + j, currentZ).getLivings ();

							if (alLivings != null) {
								LivingEntity le;
								for (int h = 0; h < alLivings.size (); h++) {
									le = alLivings.get (h);
									if (hateData.isHate (le)) {
										continue nextcell; // Hay hate, la celda no está disponible para el move
									}
								}
							}

							alPoints.add (Point3DShort.getPoolInstance (currentX + i, currentY + j, currentZ));
						}
					}
				}
			}
		}

		// Arriba y abajo
		if (Terrain.canGoUp (currentX, currentY, currentZ)) {
			alPoints.add (Point3DShort.getPoolInstance (currentX, currentY, currentZ - 1));
		}
		if (Terrain.canGoDown (currentX, currentY, currentZ)) {
			alPoints.add (Point3DShort.getPoolInstance (currentX, currentY, currentZ + 1));
		}

		// Si la casilla de arriba está minada, miramos las casillas diagonal/parriba
		if (currentZ > 0 && World.getCell (currentX, currentY, currentZ - 1).isMined ()) {
			// Miramos las casillas posibles a las que puede ir
			// Hay que mirar que no haya un hated ahí
			for (int i = -1; i <= 1; i++) {
				nextcell: for (int j = -1; j <= 1; j++) {
					if (i != 0 || j != 0) {
						if (Utils.isInsideMap (currentX + i, currentY + j, currentZ - 1)) {
							if (isCellAllowed (currentX + i, currentY + j, currentZ - 1)) {
								// Miramos el hate

								// Por cada living en el destino miramos si la entity los odia
								ArrayList<LivingEntity> alLivings = World.getCell (currentX + i, currentY + j, currentZ - 1).getLivings ();

								if (alLivings != null) {
									LivingEntity le;
									for (int h = 0; h < alLivings.size (); h++) {
										le = alLivings.get (h);
										if (hateData.isHate (le)) {
											continue nextcell; // Hay hate, la celda no está disponible para el move
										}
									}
								}

								alPoints.add (Point3DShort.getPoolInstance (currentX + i, currentY + j, currentZ - 1));
							}
						}
					}
				}
			}
		}
		// Bajar colinillas?
		if (currentZ < (World.MAP_DEPTH - 1)) {
			// Miramos las casillas posibles a las que puede ir
			// Hay que mirar que no haya un hated ahí
			for (int i = -1; i <= 1; i++) {
				nextcell: for (int j = -1; j <= 1; j++) {
					if (i != 0 || j != 0) {
						if (Utils.isInsideMap (currentX + i, currentY + j, currentZ + 1)) {
							if (isCellAllowed (currentX + i, currentY + j, currentZ + 1)) {
								// Miramos el hate

								// Por cada living en el destino miramos si la entity los odia
								ArrayList<LivingEntity> alLivings = World.getCell (currentX + i, currentY + j, currentZ + 1).getLivings ();

								if (alLivings != null) {
									LivingEntity le;
									for (int h = 0; h < alLivings.size (); h++) {
										le = alLivings.get (h);
										if (hateData.isHate (le)) {
											continue nextcell; // Hay hate, la celda no está disponible para el move
										}
									}
								}

								alPoints.add (Point3DShort.getPoolInstance (currentX + i, currentY + j, currentZ + 1));
							}
						}
					}
				}
			}
		}

		if (alPoints.size () > 0) {
			// Calculamos los A*ZID
			ArrayList<Integer> alASZIDS = new ArrayList<Integer> ();
			for (int i = 0; i < alPoints.size (); i++) {
				alASZIDS.add (Integer.valueOf (World.getCell (alPoints.get (i)).getAstarZoneID ()));
			}

			// Creamos una lista con los A*ZID de los otros aldeanos
			Citizen citizen;
			Cell cell;
			ArrayList<Integer> alASZIDSOtros = new ArrayList<Integer> ();
			for (int i = 0; i < World.getCitizenIDs ().size (); i++) {
				citizen = (Citizen) World.getLivingEntityByID (World.getCitizenIDs ().get (i));
				if (citizen.getID () != getID ()) {
					cell = World.getCell (citizen.getCoordinates ());
					if (cell.getAstarZoneID () != -1) {
						alASZIDSOtros.add (Integer.valueOf (cell.getAstarZoneID ()));
					}
				}
			}
			for (int i = 0; i < World.getSoldierIDs ().size (); i++) {
				citizen = (Citizen) World.getLivingEntityByID (World.getSoldierIDs ().get (i));
				if (citizen.getID () != getID ()) {
					cell = World.getCell (citizen.getCoordinates ());
					if (cell.getAstarZoneID () != -1) {
						alASZIDSOtros.add (Integer.valueOf (cell.getAstarZoneID ()));
					}
				}
			}

			// Creamos una lista de puntos buenos mirando la lista de A*ZID de los otros aldeanos
			if (alASZIDSOtros.size () > 0) {
				ArrayList<Point3DShort> alPointsOK = new ArrayList<Point3DShort> ();
				for (int i = 0; i < alASZIDS.size (); i++) {
					if (alASZIDSOtros.contains (alASZIDS.get (i))) {
						alPointsOK.add (alPoints.get (i));
					}
				}

				if (alPointsOK.size () > 0) {
					// Tenemos la lista buena, hazamos random y pacasa
					setCoordinates (alPointsOK.get (Utils.getRandomBetween (0, alPointsOK.size () - 1)));
				} else {
					// Nada de nada, hacemos random de la lista "mala"
					setCoordinates (alPoints.get (Utils.getRandomBetween (0, alPoints.size () - 1)));
				}
			} else {
				// Hacemos un random
				setCoordinates (alPoints.get (Utils.getRandomBetween (0, alPoints.size () - 1)));
			}

			// Devolvemos al pool los puntos usados
			Point3DShort.returnToPool (alPoints);

			return true;
		}

		// Si llega aquí es que no hay ni un punto bueno
		// En caso de hole (digged) caerá abajo, en otro caso palmará
		if (World.getCell (currentX, currentY, currentZ).isDigged ()) {
			// Hole, pabajo nos vamos
			setCoordinates (Point3DShort.getPoolInstance (currentX, currentY, currentZ + 1));
			return true;
		}

		if (World.isRecheckASZID ()) {
			Cell.setAllZoneIDs ();
			return forceMove ();
		}

		return false;
	}


	/**
	 * Pone en juego un item munición (ej: flecha, bola de fuego, ...) Tiene en cuenta las cargas del arma y controla si se acaban y se rompe
	 * 
	 * @param attacker
	 * @param victim
	 */
	public static void doRangeAttack (LivingEntity attacker, LivingEntity victim, boolean bCheckAttackTurn) {
		if (bCheckAttackTurn && !canLaunchNextAttack (attacker)) {
			return;
		}

		// Cargas
		MilitaryItem weapon = attacker.getEquippedData ().getWeapon ();
		int damage = attacker.getLivingEntityData ().getDamageCurrent (); // Se pone aquí ppq más abajo el arma puede estar destruida
		int maxDistance = attacker.getLivingEntityData ().getLOSCurrent (); // Se pone aquí pq más abajo el arma puede estar destruida
		int attackerType = LivingEntityManager.getItem (attacker.getIniHeader ()).getType ();
		ItemManagerItem imiWeapon = ItemManager.getItem (weapon.getIniHeader ());

		// 1 shoot
		if (imiWeapon.isRangedOneShoot ()) {
			attacker.unequip (MilitaryItem.LOCATION_WEAPON, attackerType);
		}

		// Si hay visión disparamos
		if (attacker.getCoordinates ().z == victim.getCoordinates ().z) {
			ArrayList<Point3DShort> path = Utils.bresenhamLine (attacker.getCoordinates (), victim.getCoordinates (), attacker.getZ ()); // , attackerType);
			if (path != null) {
				ItemManagerItem imi = ItemManager.getItem (imiWeapon.getRangedAmmo ());
				Projectile projectile = new Projectile (imi.getIniHeader ());
				projectile.setDamage (damage);
				projectile.setMaxDistance (maxDistance);
				projectile.setAttacker (attacker);
				projectile.setAttackerWeapon (weapon.getIniHeader ());
				projectile.setVictim (victim);
				projectile.setPath (path);
				projectile.setCoordinates (attacker.getCoordinates ());
				Game.getWorld ().getProjectiles ().add (projectile);
			} else {
				// No hay visión, pasando
				attacker.getFocusData ().setEntityID (-1);
				attacker.getFocusData ().setEntityType (TYPE_UNKNOWN);
				attacker.setFighting (false);
			}
		} else {
			// No hay visión, pasando
			attacker.getFocusData ().setEntityID (-1);
			attacker.getFocusData ().setEntityType (TYPE_UNKNOWN);
			attacker.setFighting (false);
		}
	}


	/**
	 * Indica si el personaje está listo para caminar. Actualiza el contador de turnos
	 * 
	 * @param living
	 * @return
	 */
	public static boolean canWalkNext (LivingEntity living, int livingType) {
		// Primero miramos si puede caminar
		int iCurrent = living.getLivingEntityData ().getWalkSpeedCurrent ();

		if (livingType == TYPE_CITIZEN) {
			// Miramos la reducción de velocidad debido a hambre y felicidad
			int iMalusPCT = ((Citizen) living).getMalusSpeedPCT ();
			if (iMalusPCT > 0 && iMalusPCT != 100) {
				iCurrent = (iCurrent * iMalusPCT) / 100;
			}

			// Miramos si tiene boost debido a un soldado BOSS_AROUND cercano
			if (((Citizen) living).getCitizenData ().getBoostCounter () > 0) {
				// Tiene boost
				iCurrent = (iCurrent * SoldierData.BOOST_PCT_BOSS_AROUND_WALK) / 100;
			}

			// Eventos globales, sólo a townies
			if (Game.getWorld ().getGlobalEvents ().getWalkSpeedPCT () != 100) {
				iCurrent = (iCurrent * Game.getWorld ().getGlobalEvents ().getWalkSpeedPCT ()) / 100;
			}
		}

		// Reducimos si tiene effects con speedPCT != 100
		if (living.getLivingEntityData ().getEffects ().size () > 0) {
			ArrayList<EffectData> alEffects = living.getLivingEntityData ().getEffects ();
			EffectData effectData;
			for (int i = 0; i < alEffects.size (); i++) {
				effectData = alEffects.get (i);
				if (effectData.getSpeedPCT () != 100) {
					iCurrent = (iCurrent * effectData.getSpeedPCT ()) / 100;
				}
			}
		}

		if (iCurrent < 1) {
			iCurrent = 1;
		} else if (iCurrent > 100) {
			iCurrent = 100;
		}
		int newCounter = living.getWalkSpeedCounter () + iCurrent;
		int iCellWalkNeeded = Cell.getCellWalkNeeded (living.getCoordinates ());
		if (iCellWalkNeeded < 1) {
			iCellWalkNeeded = 1;
		}

		boolean canWalk;
		if (newCounter >= iCellWalkNeeded) {
			canWalk = true;
			while (newCounter >= iCellWalkNeeded) {
				newCounter -= iCellWalkNeeded;
			}
		} else {
			canWalk = false;
		}
		living.setWalkSpeedCounter (newCounter);

		return canWalk;
	}


	/**
	 * Indica si el personaje está listo para lanzar otro ataque. Actualiza el contador de turnos
	 * 
	 * @param attacker
	 * @return
	 */
	public static boolean canLaunchNextAttack (LivingEntity attacker) {
		// Primero miro si puede lanzar el siguiente ataque
		int iCurrent = attacker.getLivingEntityData ().getAttackSpeedCurrent ();
		if (iCurrent < 1) {
			iCurrent = 1;
		} else if (iCurrent > 100) {
			iCurrent = 100;
		}

		// Effects que modifiquen esto
		EffectData effectData;
		for (int i = 0; i < attacker.getLivingEntityData ().getEffects ().size (); i++) {
			effectData = attacker.getLivingEntityData ().getEffects ().get (i);
			if (effectData.getAttackSpeedPCT () != 100) {
				iCurrent = (iCurrent * effectData.getAttackSpeedPCT ()) / 100;
			}
		}

		int newCounter = attacker.getAttackSpeedCounter () + iCurrent;
		boolean canLaunchAttack;
		if (newCounter >= 100) {
			canLaunchAttack = true;
			newCounter %= 100;
		} else {
			canLaunchAttack = false;
		}
		attacker.setAttackSpeedCounter (newCounter);

		return canLaunchAttack;
	}


	/**
	 * Intenta dar un golpe de atacante a víctima. Si lo consigue resta puntos de vida a la víctima
	 * 
	 * @param attacker Atacante
	 * @param victim Víctima
	 * @return true si el golpe se da con éxito
	 */
	public static boolean doHit (LivingEntity attacker, LivingEntity victim, boolean bCheckAttackTurn) {
		return doHit (attacker, victim, bCheckAttackTurn, null, 0, 0, false);
	}


	/**
	 * Intenta dar un golpe de atacante a víctima. Si lo consigue resta puntos de vida a la víctima
	 * 
	 * @param attacker Atacante
	 * @param victim Víctima
	 * @param bCheckAttackTurn Indica si hay que comprobar si el atacante está listo para el siguiente ataque
	 * @param rangedWeapon Indica si el ataque es ranged. En otro caso valdrá null. Se usa para obtener los verbos
	 * @param iRangedDamage Daño fijo, se usa para los ataques ranged, ya que el daño se calcula en el momento de lanzar el proyectil. Aquí se aplicará el % en base a la distancia del enemigo
	 * @param iRangedMaxDistance Distancia máxima en el momento del disparo.
	 * @param bRangedHit Indica si el ammo ha tocado al enemigo o no (no quiere decir que acierte)
	 * @return true si el golpe se da con éxito
	 */
	public static boolean doHit (LivingEntity attacker, LivingEntity victim, boolean bCheckAttackTurn, ItemManagerItem rangedWeapon, int iRangedDamage, int iRangedMaxDistance, boolean bRangedHit) {
		attacker.updatePathConstantOffsets ();
		if (bCheckAttackTurn && !canLaunchNextAttack (attacker)) {
			return false;
		}

		attacker.setAttackAnimationCounter (ATTACK_ANIMATION_MAX_COUNTER);

		LivingEntityManagerItem lemiAttacker = LivingEntityManager.getItem (attacker.getIniHeader ());
		LivingEntityManagerItem lemiVictim = LivingEntityManager.getItem (victim.getIniHeader ());

		// Message type
		int iMessageType;
		if (lemiAttacker.getType () == TYPE_HERO || lemiVictim.getType () == TYPE_HERO) {
			iMessageType = MessagesPanel.TYPE_HEROES;
		} else {
			iMessageType = MessagesPanel.TYPE_COMBAT;
		}

		// Calculamos el porcentaje de ataque en base a la defensa de la víctima
		// Hay un mínimo de 5% de atacar y un máximo de 95%
		int iPCT;
		if (rangedWeapon != null && !bRangedHit) {
			iPCT = 0;
		} else {
			int attack = attacker.getLivingEntityData ().getAttackCurrent ();
			int defense = victim.getLivingEntityData ().getDefenseCurrent ();

			if (attack < 1) {
				attack = 1;
			}
			if (defense < 1) {
				defense = 1;
			}

			// Miramos el porcentaje de uno respecto al otro
			if (attack >= defense) {
				iPCT = 50 + (((100 - ((defense * 100) / attack))) / 2);
			} else {
				iPCT = 50 - (((100 - ((attack * 100) / defense))) / 2);
			}

			if (iPCT > 95) {
				iPCT = 95;
			} else if (iPCT < 5) {
				iPCT = 5;
			}
		}

		if (Utils.getRandomBetween (1, 100) <= iPCT) {
			// Hit
			// Si es ranged miramos la distáncia
			int iDanyo;
			if (rangedWeapon != null) {
				// Calculamos el % de daño que hará según la distancia (de 100% a 20%)
				int iDistancia = Math.max (Math.abs (attacker.getX () - victim.getX ()), Math.abs (attacker.getX () - victim.getX ()));
				if (iRangedMaxDistance < 1) {
					iRangedMaxDistance = 1;
				}
				if (iDistancia > iRangedMaxDistance) {
					iDistancia = iRangedMaxDistance;
				} else if (iDistancia < 1) {
					iDistancia = 1;
				}
				// Calculamos el porcentaje
				int iDistanciaPCT = iDistancia * 100 / iRangedMaxDistance;
				if (iDistanciaPCT < 50) {
					iDistanciaPCT = 50;
				}

				iDanyo = ((iRangedDamage * iDistanciaPCT) / 100);
			} else {
				iDanyo = attacker.getLivingEntityData ().getDamageCurrent ();
			}
			if (iDanyo < 1) {
				iDanyo = 1;
			}
			// Antes de restar el daño miramos que no estubiera ya muerto (para el medallero)
			boolean bPreviousDead = victim.getLivingEntityData ().getHealthPoints () <= 0;
			victim.getLivingEntityData ().setHealthPoints (victim.getLivingEntityData ().getHealthPoints () - iDanyo);

			String verb;
			if (rangedWeapon != null) {
				verb = rangedWeapon.getVerb ();
			} else {
				if (attacker.getEquippedData ().isWearing (MilitaryItem.LOCATION_WEAPON)) {
					verb = ItemManager.getItem (attacker.getEquippedData ().getWeapon ().getIniHeader ()).getVerb ();
				} else {
					verb = lemiAttacker.getAttackVerb ();
				}
			}

			MessagesPanel.addMessage (iMessageType, attacker.getLivingEntityData ().getName () + " " + verb + Messages.getString ("LivingEntity.5") + victim.getLivingEntityData ().getName () + " (" + iDanyo + Messages.getString ("LivingEntity.11"), (lemiAttacker.getType () == TYPE_CITIZEN || lemiAttacker.getType () == TYPE_ALLY || (lemiAttacker.getType () == TYPE_HERO && lemiVictim.getType () != TYPE_CITIZEN)) ? ColorGL.GREEN : ColorGL.RED, attacker.getCoordinates (), attacker.getID ()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			// Notificamos a la víctima
			victim.hitted (attacker, true, iDanyo);

			// Si el atacante es héroe o soldado le sumamos experiencia
			// Si es héroe o citizen sumamos 1 a los enemigos de ese tipo matados
			if (lemiAttacker.getType () == TYPE_HERO) {
				int victimLevel = LivingEntityManager.getItem (victim.getIniHeader ()).getLevel ();
				if (victimLevel == 0) {
					victimLevel = 1;
				}
				victimLevel *= 5;

				Hero hero = (Hero) attacker;

				int xpGained = (victimLevel - hero.getHeroData ().getLevel ()) * 15;
				if (xpGained < 5) {
					xpGained = 5;
				}
				hero.getHeroData ().setXp (hero.getHeroData ().getXp () + xpGained);
				if (Game.getWorld ().getMaxHeroXP () < hero.getHeroData ().getXp ()) {
					Game.getWorld ().setMaxHeroXP (hero.getHeroData ().getXp ());
				}
				if (hero.getHeroData ().checkAndUpdateLevelUp (hero, true)) {
					hero.getLivingEntityData ().setModifiers (hero);
					MessagesPanel.addMessage (iMessageType, hero.getCitizenData ().getFullName () + Messages.getString ("LivingEntity.1"), ColorGL.GREEN, hero.getCoordinates (), hero.getID ()); //$NON-NLS-1$
				}
			} else if (lemiAttacker.getType () == TYPE_CITIZEN) {
				Citizen citizen = (Citizen) attacker;
				if (citizen.getSoldierData ().isSoldier ()) {
					int victimLevel = LivingEntityManager.getItem (victim.getIniHeader ()).getLevel ();
					if (victimLevel == 0) {
						victimLevel = 1;
					}
					victimLevel *= 5;

					int xpGained = (victimLevel - citizen.getSoldierData ().getLevel ()) * 15;
					if (xpGained < 5) {
						xpGained = 5;
					}
					citizen.getSoldierData ().setXp (citizen.getSoldierData ().getXp () + xpGained);

					if (citizen.getSoldierData ().checkAndUpdateLevelUp (citizen)) {
						citizen.getLivingEntityData ().setModifiers (citizen);
					}
				}
			}

			// La víctima ha muerto de este golpe, sumamos 1 al medallero en caso de HERO o CITIZEN
			if (!bPreviousDead && victim.getLivingEntityData ().getHealthPoints () <= 0) {
				if (lemiAttacker.getType () == TYPE_HERO || lemiAttacker.getType () == TYPE_CITIZEN) {
					// +1 a los muertos
					Game.getWorld ().addKilledEnemy (victim.getIniHeader ());
				}
			}
			// Si el atacante tiene un effect con attackDOT, le metemos un efecto de DOT a la víctima
			ArrayList<EffectData> effects = attacker.getLivingEntityData ().getEffects ();
			EffectData effectData;
			EffectManagerItem emi;
			for (int i = 0; i < effects.size (); i++) {
				effectData = effects.get (i);
				if (rangedWeapon == null) {
					// Melee
					if (effectData.getOnHitPCT () > 0 && Utils.getRandomBetween (1, 100) <= effectData.getOnHitPCT ()) {
						emi = EffectManager.getItem (effectData.getEffectID ());
						for (int e = 0; e < emi.getOnHitEffects ().size (); e++) {
							victim.addEffect (EffectManager.getItem (emi.getOnHitEffects ().get (e)), true);
						}
					}
				} else {
					// Ranged
					if (effectData.getOnRangedHitPCT () > 0 && Utils.getRandomBetween (1, 100) <= effectData.getOnRangedHitPCT ()) {
						emi = EffectManager.getItem (effectData.getEffectID ());
						for (int e = 0; e < emi.getOnRangedHitEffects ().size (); e++) {
							victim.addEffect (EffectManager.getItem (emi.getOnRangedHitEffects ().get (e)), true);
						}
					}
				}
			}

			return true;
		} else {
			// Miss
			String verb;
			if (rangedWeapon != null) {
				verb = rangedWeapon.getVerbInfinitive ();
			} else {
				if (attacker.getEquippedData ().isWearing (MilitaryItem.LOCATION_WEAPON)) {
					verb = ItemManager.getItem (attacker.getEquippedData ().getWeapon ().getIniHeader ()).getVerbInfinitive ();
				} else {
					verb = LivingEntityManager.getItem (attacker.getIniHeader ()).getAttackVerbInfinitive ();
				}
			}
			MessagesPanel.addMessage (iMessageType, attacker.getLivingEntityData ().getName () + Messages.getString ("LivingEntity.2") + verb + Messages.getString ("LivingEntity.5") + victim.getLivingEntityData ().getName () + Messages.getString ("LivingEntity.4"), ColorGL.WHITE, attacker.getCoordinates (), attacker.getID ()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			victim.hitted (attacker, false, 0);
			return false;
		}
	}


	/**
	 * Equipa un objeto, modifica los livingEntityData
	 * 
	 * @param militaryItem
	 * @return true si ha podido equiparlo
	 */
	public boolean equip (MilitaryItem militaryItem) {
		if (getEquippedData ().isWearing (ItemManager.getItem (militaryItem.getIniHeader ()).getLocation ())) {
			return false;
		}

		getEquippedData ().equip (militaryItem);
		getLivingEntityData ().setModifiers (this);

		return true;
	}


	/**
	 * Indica si en la celda indicada hay un item mejor. Tiene en cuanta la moral
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param iCurrentASZID
	 * 
	 * @return
	 */
	public MilitaryItem hasBetterItemInLOSAtCell (int x, int y, int z, int iCurrentASZID, boolean citizenAround, LivingEntityManagerItem lemi) {
		Cell cell = World.getCell (x, y, z);
		if (cell.getAstarZoneID () == iCurrentASZID) {
			Item item = cell.getItem ();

			if (item != null && item instanceof MilitaryItem && MilitaryItem.isBetterItem (this, (MilitaryItem) item)) {
				// Miramos si el item tiene tags
				ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
				boolean bTagsOK = true;
				if (imi.getTags () != null && imi.getTags ().size () > 0) {
					// El item tiene tags

					if (lemi.getEquipAllowed () != null && lemi.getEquipAllowed ().size () > 0) {
						// El personaje tiene tags

						for (int i = 0; i < imi.getTags ().size (); i++) {
							if (!lemi.getEquipAllowed ().contains (imi.getTags ().get (i))) {
								bTagsOK = false;
								break;
							}
						}
					} else {
						bTagsOK = false;
					}
				}

				if (bTagsOK) {
					boolean bItemOK = false;
					if (!cell.hasStockPile ()) {
						bItemOK = true;
					} else if (getLivingEntityData ().getMoral () <= 50 && !citizenAround) {
						bItemOK = true;
					}

					if (bItemOK) {
						if (Utils.bresenhamLineExists (x, y, getX (), getY (), z) /* , TYPE_HERO) */|| Utils.bresenhamLineExists (getX (), getY (), x, y, z)) { // , TYPE_HERO)) {
							return (MilitaryItem) item;
						}
					}
				}
			}
		}

		return null;
	}


	/**
	 * Mira si hay una entity del tipo pasado en LOS
	 * 
	 * @return
	 */
	public boolean entityTypeInLOS (int iEntitySource, int iEntityToCheck) {
		int iTmp;
		short x = getX ();
		short y = getY ();
		short z = getZ ();
		int iASZID = World.getCell (x, y, z).getAstarZoneID ();
		Cell cell;

		for (int radio = 1; radio <= getLivingEntityData ().getLOSCurrent (); radio++) {
			// Miramos sólo los bordes
			// Arriba
			iTmp = y - radio;
			if (iTmp >= 0) {
				for (int i = (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						cell = World.getCell (i, iTmp, z);
						if (cell.getAstarZoneID () == iASZID && cell.containsSpecificLiving (iEntityToCheck) != null) {
							// if (Utils.bresenhamLineExists (x, y, i, iTmp, z, iEntitySource) || Utils.bresenhamLineExists (i, iTmp, x, y, z, iEntitySource)) {
							if (Utils.bresenhamLineExists (x, y, i, iTmp, z) || Utils.bresenhamLineExists (i, iTmp, x, y, z)) {
								return true;
							}
						}
					}
				}

			}
			// Abajo
			iTmp = y + radio;
			if (iTmp < World.MAP_HEIGHT) {
				for (int i = (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						cell = World.getCell (i, iTmp, z);
						if (cell.getAstarZoneID () == iASZID && cell.containsSpecificLiving (iEntityToCheck) != null) {
							// if (Utils.bresenhamLineExists (x, y, i, iTmp, z, iEntitySource) || Utils.bresenhamLineExists (i, iTmp, x, y, z, iEntitySource)) {
							if (Utils.bresenhamLineExists (x, y, i, iTmp, z) || Utils.bresenhamLineExists (i, iTmp, x, y, z)) {
								return true;
							}
						}
					}
				}

			}
			// Izquierda
			iTmp = x - radio;
			if (iTmp >= 0) {
				for (int i = (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						cell = World.getCell (iTmp, i, z);
						if (cell.getAstarZoneID () == iASZID && cell.containsSpecificLiving (iEntityToCheck) != null) {
							// if (Utils.bresenhamLineExists (x, y, iTmp, i, z, iEntitySource) || Utils.bresenhamLineExists (iTmp, i, x, y, z, iEntitySource)) {
							if (Utils.bresenhamLineExists (x, y, iTmp, i, z) || Utils.bresenhamLineExists (iTmp, i, x, y, z)) {
								return true;

							}
						}
					}
				}

			}
			// Derecha
			iTmp = x + radio;
			if (iTmp < World.MAP_WIDTH) {
				for (int i = (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						cell = World.getCell (iTmp, i, z);
						if (cell.getAstarZoneID () == iASZID && cell.containsSpecificLiving (iEntityToCheck) != null) {
							// if (Utils.bresenhamLineExists (x, y, iTmp, i, z, iEntitySource) || Utils.bresenhamLineExists (iTmp, i, x, y, z, iEntitySource)) {
							if (Utils.bresenhamLineExists (x, y, iTmp, i, z) || Utils.bresenhamLineExists (iTmp, i, x, y, z)) {
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
	 * Mira si hay un item mejor en LOS
	 * 
	 * @return
	 */
	public MilitaryItem hasBetterItemInLOS (boolean citizenAround) {
		int iTmp;
		int x = getX ();
		int y = getY ();
		int z = getZ ();
		int iASZID = World.getCell (getCoordinates ()).getAstarZoneID ();
		LivingEntityManagerItem lemi = LivingEntityManager.getItem (getIniHeader ());

		for (int radio = 1; radio <= getLivingEntityData ().getLOSCurrent (); radio++) {
			// Miramos sólo los bordes
			// Arriba
			iTmp = y - radio;
			if (iTmp >= 0) {
				for (int i = (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						MilitaryItem mi = hasBetterItemInLOSAtCell (i, iTmp, z, iASZID, citizenAround, lemi);
						if (mi != null) {
							return mi;
						}
					}
				}

			}
			// Abajo
			iTmp = y + radio;
			if (iTmp < World.MAP_HEIGHT) {
				for (int i = (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						MilitaryItem mi = hasBetterItemInLOSAtCell (i, iTmp, z, iASZID, citizenAround, lemi);
						if (mi != null) {
							return mi;
						}
					}
				}

			}
			// Izquierda
			iTmp = x - radio;
			if (iTmp >= 0) {
				for (int i = (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						MilitaryItem mi = hasBetterItemInLOSAtCell (iTmp, i, z, iASZID, citizenAround, lemi);
						if (mi != null) {
							return mi;
						}
					}
				}

			}
			// Derecha
			iTmp = x + radio;
			if (iTmp < World.MAP_WIDTH) {
				for (int i = (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						MilitaryItem mi = hasBetterItemInLOSAtCell (iTmp, i, z, iASZID, citizenAround, lemi);
						if (mi != null) {
							return mi;
						}
					}
				}
			}
		}

		return null;
	}


	/**
	 * Descubre las casillas en LOS
	 * 
	 * @param iLivingSource
	 * @param p3d
	 * @param iRadius
	 */
	public static void discoverInLOS (int iLivingSource, Point3DShort p3d, int iRadius) {
		discoverInLOS (iLivingSource, p3d.x, p3d.y, p3d.z, iRadius);
	}


	/**
	 * Descubre las casillas en LOS
	 * 
	 * @param iLivingSource
	 * @param x
	 * @param y
	 * @param z
	 * @param iRadius
	 */
	public static void discoverInLOS (int iLivingSource, short x, short y, short z, int iRadius) {
		int iTmp;
		for (int radio = 1; radio <= iRadius; radio++) {
			// Miramos sólo los bordes
			// Arriba
			iTmp = y - radio;
			if (iTmp >= 0) {
				for (int i = (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						Utils.bresenhamLineDiscover (x, y, i, iTmp, z);
					}
				}

			}
			// Abajo
			iTmp = y + radio;
			if (iTmp < World.MAP_HEIGHT) {
				for (int i = (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						Utils.bresenhamLineDiscover (x, y, i, iTmp, z);
					}
				}

			}
			// Izquierda
			iTmp = x - radio;
			if (iTmp >= 0) {
				for (int i = (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						Utils.bresenhamLineDiscover (x, y, iTmp, i, z);
					}
				}

			}
			// Derecha
			iTmp = x + radio;
			if (iTmp < World.MAP_WIDTH) {
				for (int i = (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						Utils.bresenhamLineDiscover (x, y, iTmp, i, z);
					}
				}
			}
		}

	}


	public ArrayList<LivingEntity> getAllLivingsInRadius (int iRadius, boolean hated) {
		ArrayList<LivingEntity> alLivings = new ArrayList<LivingEntity> ();
		short iTmp;
		short x = getX ();
		short y = getY ();
		short z = getZ ();

		// Buscamos
		Cell cell;
		HateData hateData = LivingEntityManager.getHateData (getIniHeader ());
		// int iLivingType = LivingEntityManager.getItem (getIniHeader ()).getType ();

		for (int radio = 1; radio <= iRadius; radio++) {
			// Miramos sólo los bordes
			// Arriba
			iTmp = (short) (y - radio);
			if (iTmp >= 0) {
				for (short i = (short) (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						// if (Utils.bresenhamLineExists (x, y, i, iTmp, z, iLivingType) && Utils.bresenhamLineExists (i, iTmp, x, y, z, iLivingType)) {
						if (Utils.bresenhamLineExists (x, y, i, iTmp, z) && Utils.bresenhamLineExists (i, iTmp, x, y, z)) {
							cell = World.getCell (i, iTmp, z);
							if (cell.isDiscovered () && cell.getLivings () != null) {
								for (int l = 0; l < cell.getLivings ().size (); l++) {
									if (hateData.isHate (cell.getLivings ().get (l))) {
										if (hated) {
											alLivings.add (cell.getLivings ().get (l));
										}
									} else {
										if (!hated) {
											alLivings.add (cell.getLivings ().get (l));
										}
									}
								}
							}
						}
					}
				}

			}
			// Abajo
			iTmp = (short) (y + radio);
			if (iTmp < World.MAP_HEIGHT) {
				for (short i = (short) (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						// if (Utils.bresenhamLineExists (x, y, i, iTmp, z, iLivingType) && Utils.bresenhamLineExists (i, iTmp, x, y, z, iLivingType)) {
						if (Utils.bresenhamLineExists (x, y, i, iTmp, z) && Utils.bresenhamLineExists (i, iTmp, x, y, z)) {
							cell = World.getCell (i, iTmp, z);
							if (cell.isDiscovered () && cell.getLivings () != null) {
								for (int l = 0; l < cell.getLivings ().size (); l++) {
									if (hateData.isHate (cell.getLivings ().get (l))) {
										if (hated) {
											alLivings.add (cell.getLivings ().get (l));
										}
									} else {
										if (!hated) {
											alLivings.add (cell.getLivings ().get (l));
										}
									}
								}
							}
						}
					}
				}

			}
			// Izquierda
			iTmp = (short) (x - radio);
			if (iTmp >= 0) {
				for (short i = (short) (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						// if (Utils.bresenhamLineExists (x, y, iTmp, i, z, iLivingType) && Utils.bresenhamLineExists (iTmp, i, x, y, z, iLivingType)) {
						if (Utils.bresenhamLineExists (x, y, iTmp, i, z) && Utils.bresenhamLineExists (iTmp, i, x, y, z)) {
							cell = World.getCell (iTmp, i, z);
							if (cell.isDiscovered () && cell.getLivings () != null) {
								for (int l = 0; l < cell.getLivings ().size (); l++) {
									if (hateData.isHate (cell.getLivings ().get (l))) {
										if (hated) {
											alLivings.add (cell.getLivings ().get (l));
										}
									} else {
										if (!hated) {
											alLivings.add (cell.getLivings ().get (l));
										}
									}
								}
							}
						}
					}
				}

			}
			// Derecha
			iTmp = (short) (x + radio);
			if (iTmp < World.MAP_WIDTH) {
				for (short i = (short) (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						// if (Utils.bresenhamLineExists (x, y, iTmp, i, z, iLivingType) && Utils.bresenhamLineExists (iTmp, i, x, y, z, iLivingType)) {
						if (Utils.bresenhamLineExists (x, y, iTmp, i, z) && Utils.bresenhamLineExists (iTmp, i, x, y, z)) {
							cell = World.getCell (iTmp, i, z);
							if (cell.isDiscovered () && cell.getLivings () != null) {
								for (int l = 0; l < cell.getLivings ().size (); l++) {
									if (hateData.isHate (cell.getLivings ().get (l))) {
										if (hated) {
											alLivings.add (cell.getLivings ().get (l));
										}
									} else {
										if (!hated) {
											alLivings.add (cell.getLivings ().get (l));
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return alLivings;
	}


	/**
	 * Indica si hay algún enemigo de la livingentity pasada en LOS del punto pasado, en ese caso devuelve el FocusData del mismo
	 * 
	 * @return el FocusData de enemigo en LOS o null
	 */
	public static FocusData hasEnemyInLOS (String sLivingSource, Point3DShort p3d, int iRadius, boolean bAttackAllies) {
		return hasEnemyInLOS (sLivingSource, p3d.x, p3d.y, p3d.z, iRadius, bAttackAllies);
	}


	/**
	 * Indica si hay algún enemigo de la livingentity pasada en LOS del punto pasado, en ese caso devuelve el FocusData del mismo
	 * 
	 * @return el FocusData de enemigo en LOS o null
	 */
	public static FocusData hasEnemyInLOS (String sLivingSource, short x, short y, short z, int iRadius, boolean bAttackAllies) {
		FocusData fdReturn = null;
		short iTmp;

		// Buscamos enemy
		for (short radio = 1; radio <= iRadius; radio++) {
			// Miramos sólo los bordes
			// Arriba
			iTmp = (short) (y - radio);
			if (iTmp >= 0) {
				for (short i = (short) (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						fdReturn = hasEnemyInLOSAtCell (sLivingSource, x, y, z, i, iTmp, bAttackAllies);
						if (fdReturn != null) {
							// En el caso de heroes, miramos si tiene que lanzar una skill de tipo use=ENEMIES_IN_LOS
							return fdReturn;
						}
					}
				}

			}
			// Abajo
			iTmp = (short) (y + radio);
			if (iTmp < World.MAP_HEIGHT) {
				for (short i = (short) (x - radio); i <= (x + radio); i++) {
					if (i >= 0 && i < World.MAP_WIDTH) {
						fdReturn = hasEnemyInLOSAtCell (sLivingSource, x, y, z, i, iTmp, bAttackAllies);
						if (fdReturn != null) {
							return fdReturn;
						}
					}
				}

			}
			// Izquierda
			iTmp = (short) (x - radio);
			if (iTmp >= 0) {
				for (short i = (short) (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						fdReturn = hasEnemyInLOSAtCell (sLivingSource, x, y, z, iTmp, i, bAttackAllies);
						if (fdReturn != null) {
							return fdReturn;
						}
					}
				}

			}
			// Derecha
			iTmp = (short) (x + radio);
			if (iTmp < World.MAP_WIDTH) {
				for (short i = (short) (y - radio); i <= (y + radio); i++) {
					if (i >= 0 && i < World.MAP_HEIGHT) {
						fdReturn = hasEnemyInLOSAtCell (sLivingSource, x, y, z, iTmp, i, bAttackAllies);
						if (fdReturn != null) {
							return fdReturn;
						}
					}
				}
			}
		}

		return null;
	}


	/**
	 * Indica si hay algún enemigo en LOS de la livingentity pasada, en ese caso devuelve el FocusData del mismo
	 * 
	 * @return el FocusData de enemgo en la celda o null si no hay ninguno
	 */
	private static FocusData hasEnemyInLOSAtCell (String sLivingSource, short sourceX, short sourceY, short sourceZ, short pointX, short pointY, boolean bAttackAllies) {
		LivingEntity livingHated = getLivingEntityHate (sLivingSource, pointX, pointY, sourceZ, null, bAttackAllies);
		if (livingHated == null) {
			return null;
		}

		// Hay living odiada en la celda, miramos que haya linea recta
		// Enemigo a la vista, go go go
		if (World.getCell (sourceX, sourceY, sourceZ).getAstarZoneID () == World.getCell (pointX, pointY, sourceZ).getAstarZoneID ()) {
			// Misma zona
			// int sourceType = LivingEntityManager.getItem (sLivingSource).getType ();
			// Miramos que haya vista "directa", teniendo en cuenta muros y demás panochadas
			// if (Utils.bresenhamLineExists (sourceX, sourceY, pointX, pointY, sourceZ, sourceType) || Utils.bresenhamLineExists (pointX, pointY, sourceX, sourceY, sourceZ, sourceType)) {
			if (Utils.bresenhamLineExists (sourceX, sourceY, pointX, pointY, sourceZ) || Utils.bresenhamLineExists (pointX, pointY, sourceX, sourceY, sourceZ)) {
				// Hay linea, pasamos el focusData
				int iTypeEnemy = LivingEntityManager.getItem (livingHated.getIniHeader ()).getType ();
				return new FocusData (livingHated.getID (), iTypeEnemy);
			}
		}

		return null;
	}


	/**
	 * Devuelve una lista de livings en uso por aldeanos que NO sea el aldeano pasado como parámetro
	 * 
	 * @param iCitID ID del aldeano a excluir o -1 en caso de no excluir a nadie
	 * @return una lista de en uso por aldeanos que no es el pasado, o nulo si no hay ninguno
	 */
	public static ArrayList<Integer> searchLivingsInUse (int iCitID) {
		ArrayList<Integer> alItemsInUse = new ArrayList<Integer> ();
		Citizen citizen;
		for (int i = 0; i < World.getCitizenIDs ().size (); i++) {
			citizen = (Citizen) World.getLivingEntityByID (World.getCitizenIDs ().get (i));
			if (iCitID == -1 || citizen.getID () != iCitID) {
				if (citizen.getCurrentCustomAction () != null && citizen.getCurrentCustomAction ().getQueueData () != null) {
					// Pick_living
					if (citizen.getCurrentCustomAction ().getQueueData ().getLivingIDPick () != -1) {
						alItemsInUse.add (Integer.valueOf(citizen.getCurrentCustomAction ().getQueueData ().getLivingIDPick ()));
					}
				}
			}
		}
		for (int i = 0; i < World.getSoldierIDs ().size (); i++) {
			citizen = (Citizen) World.getLivingEntityByID (World.getSoldierIDs ().get (i));
			if (iCitID == -1 || citizen.getID () != iCitID) {
				if (citizen.getCurrentCustomAction () != null && citizen.getCurrentCustomAction ().getQueueData () != null) {
					// Pick_living
					if (citizen.getCurrentCustomAction ().getQueueData ().getLivingIDPick () != -1) {
						alItemsInUse.add (Integer.valueOf(citizen.getCurrentCustomAction ().getQueueData ().getLivingIDPick ()));
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


	/**
	 * Devuelve un living a partir de sus coordenadas y su iniheader o null si no lo encuentra
	 * 
	 * @param p3d
	 * @param sIniHeader
	 * @return un living a partir de sus coordenadas y su iniheader o null si no lo encuentra
	 */
	public static LivingEntity getLivingByCoordinatesAndIniHeader (Point3DShort p3d, String sIniHeader) {
		if (getNumLivings (sIniHeader, true) == 0) {
			return null;
		}

		ArrayList<LivingEntity> alLivings = World.getCell (p3d).getLivings ();
		if (alLivings != null) {
			for (int i = 0; i < alLivings.size (); i++) {
				if (alLivings.get (i).getIniHeader ().equals (sIniHeader)) {
					return alLivings.get (i);
				}
			}
		}

		return null;
	}


	/**
	 * Devuelve el punto de una lista de livings pasada (discovered)
	 * 
	 * @param p3dIni Punto inicial de búsqueda
	 * @param alHeaders Lista de numeric Iniheaders de los livings
	 * @param near Indica si hay que buscar la más cercana o cualquiera
	 * @param alLivingsToAvoid Lista de IDs de las livings a evitar
	 * @return
	 */
	public static LivingEntity searchLiving (Point3DShort p3dIni, int[] alHeaders, boolean near, ArrayList<Integer> alLivingsToAvoid) {
		// CUIDADO, MÉTODO DUPLICADO ABAJO, SI SE MODIFICA HAY QUE MODIFICAR EL OTRO

		// Primero miramos si hay livings de esos
		int iNum = 0;
		for (int f = 0; f < alHeaders.length; f++) {
			iNum += getNumLivings (UtilsIniHeaders.getStringIniHeader (alHeaders[f]), true);
		}

		if (iNum == 0) {
			return null;
		}

		int iNearest = Utils.MAX_DISTANCE;
		int iASZID = World.getCell (p3dIni).getAstarZoneID ();
		Point3DShort p3dNearest = null;
		int iLivingNearest = -1;

		// Recorremos todos los livings !!! Hasta encontrar los que estan en la misma zona
		Iterator<Integer> iterator = World.getLivings (true).keySet ().iterator ();
		LivingEntity le;
		while (iterator.hasNext ()) {
			le = World.getLivingEntityByID (iterator.next (), true);

			if (World.getCell (le.getCoordinates ()).getAstarZoneID () == iASZID) {
				if (UtilsIniHeaders.contains (alHeaders, le.getNumericIniHeader ())) {
					if (alLivingsToAvoid == null || !alLivingsToAvoid.contains (Integer.valueOf (le.getID ()))) {
						if (!near) {
							return le;
						} else {
							int iDistancia = Utils.getDistance (le.getCoordinates (), p3dIni);
							if (p3dNearest == null || iDistancia < iNearest) {
								iNearest = iDistancia;
								iLivingNearest = le.getID ();
								p3dNearest = le.getCoordinates ();
							}

							// Usaremos el iNum (número de livings en el mundo) para saber si hemos acabado (performance trick)
							iNum--;
							if (iNum == 0) {
								break;
							}
						}
					}
				}
			}
		}

		if (iLivingNearest == -1) {
			return null;
		} else {
			return World.getLivingEntityByID (iLivingNearest);
		}
	}


	/**
	 * Devuelve el punto de una lista de livings pasada (discovered)
	 * 
	 * @param p3dIni Punto inicial de búsqueda
	 * @param alHeaders Lista de numeric Iniheaders de los livings
	 * @param near Indica si hay que buscar la más cercana o cualquiera
	 * @param alLivingsToAvoid Lista de IDs de las livings a evitar
	 * @param iASZID ASZID del origen
	 * @return
	 */
	public static LivingEntity searchLivingForcedASZID (Point3DShort p3dIni, int[] alHeaders, boolean near, ArrayList<Integer> alLivingsToAvoid, int iASZID) {
		// CUIDADO, MÉTODO DUPLICADO ARRIBA, SI SE MODIFICA HAY QUE MODIFICAR EL OTRO

		// Primero miramos si hay livings de esos
		int iNum = 0;
		for (int f = 0; f < alHeaders.length; f++) {
			iNum += getNumLivings (UtilsIniHeaders.getStringIniHeader (alHeaders[f]), true);
		}

		if (iNum == 0) {
			return null;
		}

		int iNearest = Utils.MAX_DISTANCE;
		Point3DShort p3dNearest = null;
		int iLivingNearest = -1;

		// Recorremos todos los livings !!! Hasta encontrar los que estan en la misma zona
		Iterator<Integer> iterator = World.getLivings (true).keySet ().iterator ();
		LivingEntity le;
		while (iterator.hasNext ()) {
			le = World.getLivingEntityByID (iterator.next (), true);

			if (World.getCell (le.getCoordinates ()).getAstarZoneID () == iASZID) {
				if (UtilsIniHeaders.contains (alHeaders, le.getNumericIniHeader ())) {
					if (alLivingsToAvoid == null || !alLivingsToAvoid.contains (Integer.valueOf (le.getID ()))) {
						if (!near) {
							return le;
						} else {
							int iDistancia = Utils.getDistance (le.getCoordinates (), p3dIni);
							if (p3dNearest == null || iDistancia < iNearest) {
								iNearest = iDistancia;
								iLivingNearest = le.getID ();
								p3dNearest = le.getCoordinates ();
							}

							// Usaremos el iNum (número de livings en el mundo) para saber si hemos acabado (performance trick)
							iNum--;
							if (iNum == 0) {
								break;
							}
						}
					}
				}
			}
		}

		if (iLivingNearest == -1) {
			return null;
		} else {
			return World.getLivingEntityByID (iLivingNearest);
		}
	}


	/**
	 * Busca un camino al punto indicado
	 * 
	 * @param world
	 * @param x X
	 * @param y Y
	 * @param z Z
	 */
	public void setDestination (int x, int y, int z) {
		setDestination (x, y, z, true, 0);
	}


	/**
	 * Borra el path actual y actualiza los offset de animation
	 */
	public void clearPath () {
		getPath ().clear ();
		updatePathConstantOffsets ();
	}


	/**
	 * Busca un camino al punto indicado
	 * 
	 * @param world
	 * @param x X
	 * @param y Y
	 * @param z Z
	 * @param bRemoveTaskIfFails Indica si hay que sacar al aldeano de la tarea si falla
	 */
	public void setDestination (int x, int y, int z, boolean bRemoveTaskIfFails, int iNumCellsToRemove) {
		// Borramos el antiguo camino
		clearPath ();

		// Si el destino no es allowed lo sacamos de la tarea (si tiene) (sólo en caso de aldeanos)
		int iLivingType = LivingEntityManager.getItem (getIniHeader ()).getType ();
		if (!isCellAllowed (x, y, z)) {
			if (iLivingType == TYPE_CITIZEN) {
				if (bRemoveTaskIfFails && ((Citizen) this).getCurrentTask () != null) {
					Game.getWorld ().getTaskManager ().removeCitizen ((Citizen) this);
				}
			}
			return;
		}

		// Si origen y destino tienen distinto A* zone ID, es que no existe camino posible, lo sacamos de la tarea (si tiene)
		if (World.getCells ()[x][y][z].getAstarZoneID () != World.getCells ()[getX ()][getY ()][getZ ()].getAstarZoneID ()) {
			if (iLivingType == TYPE_CITIZEN) {
				if (bRemoveTaskIfFails && ((Citizen) this).getCurrentTask () != null) {
					Game.getWorld ().getTaskManager ().removeCitizen ((Citizen) this);
				}
			}
			return;
		}

		// Buscamos camino
		// Bresenham para empezar (linea recta) (si está en el mismo nivel)
		// if (getZ () == z) {
		// setPath (Utils.bresenhamLine (getX (), getY (), x, y, z, livingType));
		// }
		// Camino vacío, eso es que en linea recta topa con algo (o está en distintos niveles), usamos A* para buscar la ruta
		// if (getPath ().isEmpty ()) {
		boolean bSiege = false;
		if (iLivingType == TYPE_ENEMY) {
			Enemy enemy = (Enemy) this;
			if (enemy.getSiegeData () != null) {
				bSiege = true;
			}
		}
		AStarQueueItem item = new AStarQueueItem (getID (), iLivingType, Point3DShort.getPoolInstance (getX (), getY (), getZ ()), Point3DShort.getPoolInstance (x, y, z), bSiege);
		item.setNumCellsToRemove (iNumCellsToRemove);
		AStarQueue.addRequest (item);
		setWaitingForPath (true);
		// }
	}


	/**
	 * Busca un camino al punto indicado. Una vez encontrado quita X (iNumCellsToRemove) casillas del camino
	 * 
	 * @param pointDestination Punto
	 * @param iNumCellsToRemove
	 */
	public void setDestination (Point3DShort pointDestination, int iNumCellsToRemove) {
		setDestination (pointDestination.x, pointDestination.y, pointDestination.z, true, iNumCellsToRemove);
	}


	/**
	 * Busca un camino al punto indicado
	 * 
	 * @param pointDestination Punto
	 */
	public void setDestination (Point3DShort pointDestination) {
		setDestination (pointDestination.x, pointDestination.y, pointDestination.z, true, 0);
	}


	public void setDestination (Point3D pointDestination) {
		setDestination (pointDestination.x, pointDestination.y, pointDestination.z, true, 0);
	}


	/**
	 * Busca un camino al punto indicado
	 * 
	 * @param pointDestination Punto
	 * @param bRemoveTaskIfFails Indica si hay que sacar al aldeano de la tarea si falla
	 */
	public void setDestination (Point3DShort pointDestination, boolean bRemoveTaskIfFails, int iNumCellsToRemove) {
		setDestination (pointDestination.x, pointDestination.y, pointDestination.z, bRemoveTaskIfFails, iNumCellsToRemove);
	}


	public void delete () {
		if (getMaxAge () > 0 && getAge () >= getMaxAge ()) {
			delete (false);
		} else {
			delete (true);
		}
	}


	public void delete (boolean dead) {
		super.deleteLiving ();

		boolean bDiscovered = World.getCell (getCoordinates ()).isDiscovered ();
		removeLiving (this, bDiscovered); // Restamos 1 al número de livings de ese tipo

		// Lo sacamos de la lista de Livings
		World.getLivings (bDiscovered).remove (Integer.valueOf (getID ()));

		LivingEntityManagerItem lemi = LivingEntityManager.getItem (getIniHeader ());

		boolean bHeroLeaved = false;
		if (dead) {
			if (lemi.getType () == TYPE_HERO) {
				// Caso héroes, si se han pirado del mapa no hay que sacar drop
				Hero hero = (Hero) this;
				if (hero.getLivingEntityData ().getHealthPoints () > 0) {
					dead = false;
					bHeroLeaved = true;
				}
			} else if (lemi.getType () == TYPE_ENEMY) {
				Enemy enemy = (Enemy) this;
				if (enemy.getLivingEntityData ().getHealthPoints () > 0) {
					dead = false;
				}
			}
		}

		if (dead) {
			// Miramos si tiene drop
			ArrayList<DropData> alDrops = lemi.getDropData ();
			if (alDrops != null && alDrops.size () > 0) {
				// Tiene!! Lanzamos los PCT a ver si sacamos algo
				for (int i = 0; i < alDrops.size (); i++) {
					if (Utils.getRandomBetween (1, 100) <= Utils.launchDice (alDrops.get (i).getPCT ())) {
						// Ta-dá !!!
						// Miramos si es item random
						String sItemID = null;
						if (alDrops.get (i).getItem () != null && !alDrops.get (i).getItem ().equalsIgnoreCase (DropData.ITEM_RANDOM)) {
							// Item fijo
							sItemID = alDrops.get (i).getItem ();
						} else {
							// Item by level (random)
							ItemManagerItem imi = ItemManager.getRandomItemByLevel (alDrops.get (i).getLevelMin (), alDrops.get (i).getLevelMax ());
							if (imi != null) {
								sItemID = imi.getIniHeader ();
							}
						}

						if (sItemID != null && sItemID.length () > 0) {
							ItemManagerItem imi = ItemManager.getItem (sItemID);
							Point3DShort p3dDrop = searchDropCell (imi, getX (), getY (), getZ ());

							if (p3dDrop != null) {
								Item item = Item.createItem (imi);
								item.init (p3dDrop.x, p3dDrop.y, p3dDrop.z);
								item.setOperative (true);

								if (item instanceof MilitaryItem) {
									// Prefijo
									if (Utils.getRandomBetween (1, 100) <= 5) { // 5% de tener prefijo
										((MilitaryItem) item).setPrefix (PrefixSuffixManager.getRandomPrefix ().getRandom ());
									}
									// Sufijo
									if (Utils.getRandomBetween (1, 100) <= 5) { // 5% de tener sufijo
										((MilitaryItem) item).setSuffix (PrefixSuffixManager.getRandomSuffix ().getRandom ());
									}
								}
								World.getCell (p3dDrop).setEntity (item);
							}
						}

						break;
					}
				}
			}

			// Drop de equipment
			if (lemi.getType () == TYPE_CITIZEN || lemi.getType () == TYPE_HERO) {
				EquippedData equippedData = getEquippedData ();
				ArrayList<MilitaryItem> alItems = new ArrayList<MilitaryItem> ();
				if (equippedData.getHead () != null) {
					alItems.add (equippedData.getHead ());
				}
				if (equippedData.getBody () != null) {
					alItems.add (equippedData.getBody ());
				}
				if (equippedData.getLegs () != null) {
					alItems.add (equippedData.getLegs ());
				}
				if (equippedData.getFeet () != null) {
					alItems.add (equippedData.getFeet ());
				}
				if (equippedData.getWeapon () != null) {
					alItems.add (equippedData.getWeapon ());
				}

				if (alItems.size () > 0) {
					// Hay cosas para dropear
					// Sólo soltaremos el 60% de las cosas
					int iIndex = alItems.size () - 1;
					while (iIndex >= 0) {
						if (Utils.getRandomBetween (1, 10) > 6) {
							alItems.remove (iIndex);
						}
						iIndex--;
					}

					if (alItems.size () > 0) {
						// Aún hay cosas para soltar, miramos si cabe en las casillas vecinas
						ItemManagerItem imi;
						for (int i = 0; i < alItems.size (); i++) {
							imi = ItemManager.getItem (alItems.get (i).getIniHeader ());
							Point3DShort p3dDrop = searchDropCell (imi, getX (), getY (), getZ ());
							if (p3dDrop != null) {
								alItems.get (i).init (p3dDrop.x, p3dDrop.y, p3dDrop.z);
								alItems.get (i).setOperative (imi.isAlwaysOperative ());
								alItems.get (i).setLocked (false);
								World.getCell (p3dDrop).setEntity (alItems.get (i));
							}
						}
					}
				}

				// Carrying
				Item itemCarrying;
				if (lemi.getType () == TYPE_CITIZEN) {
					itemCarrying = ((Citizen) this).getCarrying ();
					((Citizen) this).getCitizenData ().getCarryingData ().setCarrying (null);
				} else {
					itemCarrying = ((Hero) this).getCarrying ();
					((Hero) this).getCitizenData ().getCarryingData ().setCarrying (null);
				}
				if (itemCarrying != null) {
					ItemManagerItem imi = ItemManager.getItem (itemCarrying.getIniHeader ());
					Point3DShort p3dDrop = searchDropCell (imi, getX (), getY (), getZ ());
					if (p3dDrop != null) {
						itemCarrying.init (p3dDrop.x, p3dDrop.y, p3dDrop.z);
						itemCarrying.setOperative (imi.isAlwaysOperative ());
						itemCarrying.setLocked (false);
						World.getCell (p3dDrop).setEntity (itemCarrying);
					} else {
						if (imi.isContainer ()) {
							Game.getWorld ().deleteContainer (itemCarrying.getID ());
						}
					}
				}

			} else if (lemi.getType () == TYPE_ENEMY) {
				Enemy enemy = (Enemy) this;
				if (enemy.getCarryingData () != null) {
					// Carrying item
					if (enemy.getCarryingData ().getCarrying () != null) {
						ItemManagerItem imi = ItemManager.getItem (enemy.getCarryingData ().getCarrying ().getIniHeader ());
						Point3DShort p3dDrop = searchDropCell (imi, getX (), getY (), getZ ());
						Item itemCarrying = enemy.getCarryingData ().getCarrying ();
						enemy.getCarryingData ().setCarrying (null);
						if (p3dDrop != null) {
							itemCarrying.init (p3dDrop.x, p3dDrop.y, p3dDrop.z);
							itemCarrying.setOperative (imi.isAlwaysOperative ());
							itemCarrying.setLocked (false);
							World.getCell (p3dDrop).setEntity (itemCarrying);
						} else {
							// Si no puede soltarlo nos lo trincamos, sólo hay que mirar containers para que se limpien bien las listas
							if (imi.isContainer ()) {
								Game.getWorld ().deleteContainer (itemCarrying.getID ());
							}
						}
					}
					// Carrying living
					if (enemy.getCarryingData ().getCarryingLiving () != null) {
						LivingEntity leCarrying = enemy.getCarryingData ().getCarryingLiving ();
						leCarrying.init (getX (), getY (), getZ ());
						World.getCell (getCoordinates ()).addLiving (leCarrying);
						enemy.getCarryingData ().setCarryingLiving (null);
					}
				}
			}

			// FX
			if (lemi.getFxDead () != null && lemi.getFxDead ().length () > 0) {
				UtilsAL.play (lemi.getFxDead (), getZ ());
			} else {
				UtilsAL.play (UtilsAL.SOURCE_FX_DEAD, getZ ());
			}

			// Tutorial flow?
			if (lemi.getType () == TYPE_CITIZEN) {
				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_POPULATION, (World.getNumCitizens () + World.getNumSoldiers ()), null);
			}
		} else {
			// No lo matamos, eso es que lo ha pillado algún aldeano, le ponemos la direction pertinente para que quede bien
			// También podría ser un hero que se pira (o un enemy robbery, en los 2 casos borramos containers si llevaban alguno)
			setFacingDirection (FACING_DIRECTION_NORTH_WEST);
			refreshFacingDirection (FACING_DIRECTION_NORTH_WEST);

			if (lemi.getType () == TYPE_CITIZEN || lemi.getType () == TYPE_HERO) {
				// Carrying
				Item itemCarrying;
				if (lemi.getType () == TYPE_CITIZEN) {
					itemCarrying = ((Citizen) this).getCarrying ();
					((Citizen) this).getCitizenData ().getCarryingData ().setCarrying (null);
				} else {
					itemCarrying = ((Hero) this).getCarrying ();
					((Hero) this).getCitizenData ().getCarryingData ().setCarrying (null);
				}
				if (itemCarrying != null) {
					ItemManagerItem imi = ItemManager.getItem (itemCarrying.getIniHeader ());
					if (imi.isContainer ()) {
						Game.getWorld ().deleteContainer (itemCarrying.getID ());
					}
				}

			} else if (lemi.getType () == TYPE_ENEMY) {
				Enemy enemy = (Enemy) this;
				if (enemy.getCarryingData () != null) {
					if (enemy.getCarryingData ().getCarrying () != null) {
						ItemManagerItem imi = ItemManager.getItem (enemy.getCarryingData ().getCarrying ().getIniHeader ());
						if (imi.isContainer ()) {
							Item itemCarrying = enemy.getCarryingData ().getCarrying ();
							enemy.getCarryingData ().setCarrying (null);
							Game.getWorld ().deleteContainer (itemCarrying.getID ());
						}
					}
				}
			}
		}

		// Stuff de aldeanos
		if (lemi.getType () == TYPE_CITIZEN) {
			// Lo eliminamos de la lista de Citizens (o soldiers)
			boolean bSoldier = false;
			int iIndex = World.getCitizenIDs ().indexOf (getID ());
			if (iIndex == -1) {
				iIndex = World.getSoldierIDs ().indexOf (getID ());
				bSoldier = true;
			}

			if (iIndex != -1) {
				// Lo sacamos de la lista de citizens (o soldiers)
				if (bSoldier) {
					World.getSoldierIDs ().remove (iIndex);
				} else {
					World.getCitizenIDs ().remove (iIndex);
				}

				// Lo sacamos de las tareas
				Game.getWorld ().getTaskManager ().removeCitizen ((Citizen) this);

				// Liberamos la zona
				ZonePersonal.unAssignZone ((Citizen) this);

				// Soldados
				((Citizen) this).deleteSoldierStuff ();

				// Citizens (job groups)
				((Citizen) this).deleteJobGroupsStuff ();
			}

			// Reducimos happiness
			Game.getWorld ().updateHappiness (80);
		} else if (lemi.getType () == TYPE_HERO) {
			// Stuff de héroes
			// Lo eliminamos de la lista de Heroes
			int iIndex = -1;
			int heroID;
			for (int i = 0; i < World.getHeroIDs ().size (); i++) {
				heroID = World.getHeroIDs ().get (i);
				if (heroID == getID ()) {
					iIndex = i;
					break;
				}
			}

			if (iIndex != -1) {
				// Lo sacamos de la lista de héroes
				World.getHeroIDs ().remove (iIndex);

				// Liberamos la zona
				ZoneHeroRoom.unAssignZone ((Hero) this);
			}

			if (bHeroLeaved) {
				// Lo metemos también en la lista de héroes que se han pirado (para que quizá vuelvan en el futuro)
				Game.getWorld ().getOldHeroes ().add (this);
			} else {
				// Héroe muerto, si tiene nombre especial (caso Sips, punchwood, ...) lo metemos en la lista de muertos para que no aparezca de nuevo
				if (lemi.getNamePoolTag () == null) {
					Game.getWorld ().getOldHeroesDied ().add (getIniHeader ());
				}
			}

			// Lo eliminamos de la lista de amigos
			Hero hero = (Hero) this;
			hero.getHeroData ().getFriendships ().clear ();

			ArrayList<Integer> alHeros = World.getHeroIDs ();
			Hero heroAux;
			for (int h = 0; h < alHeros.size (); h++) {
				// Si está en la lista de amigos de otros, lo borramos
				heroAux = (Hero) World.getLivingEntityByID (alHeros.get (h));
				if (heroAux != null) {
					while (heroAux.getHeroData ().getFriendships ().remove (Integer.valueOf (getID ()))) {
						// Vamos borrando (aunque no debería haber más de 1)
					}
				}
			}
		}

		// Caravanas
		if (lemi.getCaravan () != null) {
			// Caravana, borramos los datos
			Game.getWorld ().setCurrentCaravanData (null);
			UIPanel.deleteTradePanel ();
		}

		// Edad
		if (getMaxAge () > 0 && getAge () >= getMaxAge ()) {
			// Muere de viejo, miramos si tiene living
			if (lemi.getMaxAgeLiving () != null) {
				lemi = LivingEntityManager.getItem (lemi.getMaxAgeLiving ());
				World.addNewLiving (lemi.getIniHeader (), lemi.getType (), World.getCell (getCoordinates ()).isDiscovered (), getX (), getY (), getZ (), true);
			}
		}
	}


	private static Point3DShort searchDropCell (ItemManagerItem imi, short x, short y, short z) {
		if (Item.isCellAvailableForItem (imi, x, y, z, false, true)) {
			return Point3DShort.getPoolInstance (x, y, z);
		} else {
			// Buscamos un punto que vaya bien
			boolean[] bNeighbours = new boolean [8];
			bNeighbours[0] = Item.isCellAvailableForItem (imi, (short) (x - 1), (short) (y - 1), z, false, true);
			bNeighbours[1] = Item.isCellAvailableForItem (imi, x, (short) (y - 1), z, false, true);
			bNeighbours[2] = Item.isCellAvailableForItem (imi, (short) (x + 1), (short) (y - 1), z, false, true);
			bNeighbours[3] = Item.isCellAvailableForItem (imi, (short) (x - 1), y, z, false, true);
			bNeighbours[4] = Item.isCellAvailableForItem (imi, (short) (x + 1), y, z, false, true);
			bNeighbours[5] = Item.isCellAvailableForItem (imi, (short) (x - 1), (short) (y + 1), z, false, true);
			bNeighbours[6] = Item.isCellAvailableForItem (imi, x, (short) (y + 1), z, false, true);
			bNeighbours[7] = Item.isCellAvailableForItem (imi, (short) (x + 1), (short) (y + 1), z, false, true);

			if (bNeighbours[0] || bNeighbours[1] || bNeighbours[2] || bNeighbours[3] || bNeighbours[4] || bNeighbours[5] || bNeighbours[6] || bNeighbours[7]) {
				int iRandom = Utils.getRandomBetween (0, 7);
				while (!bNeighbours[iRandom]) {
					iRandom = Utils.getRandomBetween (0, 7);
				}

				switch (iRandom) {
					case 0:
						return Point3DShort.getPoolInstance ((short) (x - 1), (short) (y - 1), z);
					case 1:
						return Point3DShort.getPoolInstance (x, (short) (y - 1), z);
					case 2:
						return Point3DShort.getPoolInstance ((short) (x + 1), (short) (y - 1), z);
					case 3:
						return Point3DShort.getPoolInstance ((short) (x - 1), y, z);
					case 4:
						return Point3DShort.getPoolInstance ((short) (x + 1), y, z);
					case 5:
						return Point3DShort.getPoolInstance ((short) (x - 1), (short) (y + 1), z);
					case 6:
						return Point3DShort.getPoolInstance (x, (short) (y + 1), z);
					case 7:
						return Point3DShort.getPoolInstance ((short) (x + 1), (short) (y + 1), z);
				}
			}
		}

		return null;
	}


	/**
	 * Desequipa un objeto, modifica los livingEntityData. Se le pasa la zona (cabeza, cuerpo, ....) que quiere desequipar
	 * 
	 * @param location Zona a desequipar
	 * @return el objeto si ha podido desequiparlo o null en caso contrario
	 */
	public MilitaryItem unequip (int location, int iLivingType) {
		if (getEquippedData ().isWearing (location)) {
			MilitaryItem mi = getEquippedData ().unequip (location);
			if (mi != null) {
				// UNWear effects
				ItemManagerItem imi = ItemManager.getItem (mi.getIniHeader ());
				if (imi.getWearEffects () != null && imi.getWearEffects ().size () > 0) {
					// A quitar efectos
					for (int i = 0, n = imi.getWearEffects ().size (); i < n; i++) {
						removeEffect (imi.getWearEffects ().get (i), iLivingType, false);
					}
				}

				getLivingEntityData ().setModifiers (this);
			}

			return mi;
		}

		return null;
	}


	/**
	 * Devuelve true si un living puede caminar por esa celda
	 * 
	 * @param Cell Celda
	 * @return true si un living puede caminar por esa celda
	 */
	public static boolean isCellAllowed (Point3DShort p3d) {
		return isCellAllowed (World.getCell (p3d.x, p3d.y, p3d.z));
	}


	/**
	 * Devuelve true si un living puede caminar por esa celda
	 * 
	 * @return true si un living puede caminar por esa celda
	 */
	public static boolean isCellAllowed (short x, short y, short z) {
		return isCellAllowed (World.getCell (x, y, z));
	}


	public static boolean isCellAllowed (int x, int y, int z) {
		return isCellAllowed (World.getCell (x, y, z));
	}


	/**
	 * Devuelve true si un living puede caminar por esa celda
	 * 
	 * @return true si un living puede caminar por esa celda
	 */
	public static boolean isCellAllowed (Cell cell) {
		if (!cell.isDiscovered () || !cell.isMined () || cell.getTerrain ().hasFluids ()) {
			return false;
		}

		// Caso especial, ladders
		Item item = null;
		if (cell.isDigged ()) {
			if (!Terrain.canGoDown (cell.getCoordinates ())) {
				item = cell.getItem ();
				boolean bItemHere = item != null;
				if (bItemHere) {
					ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
					if (item.isOperative () && item.isLocked () && imi.isWall ()) {
						return false;
					} else {
						// if (!imi.canBeBuiltOnHoles ()) {
						if (!imi.isZoneMergerUp ()) {
							bItemHere = false;
						}
						// }
					}
				}

				if (!bItemHere) {
					// No hay item relevante, miramos si abajo hay un item base=true
					if (cell.getCoordinates ().z < (World.MAP_DEPTH - 1)) {
						Cell cellDown = World.getCell (cell.getCoordinates ().x, cell.getCoordinates ().y, cell.getCoordinates ().z + 1);
						Item itemDown = cellDown.getItem ();
						if (itemDown != null) {
							if (!ItemManager.getItem (itemDown.getIniHeader ()).isBase ()) {
								return false;
							}
						} else {
							return false;
						}
					} else {
						return false;
					}
				}
			}
		}

		if (cell.hasBuilding ()) {
			// Hay edificio, miramos sea celda transitable
			Building building = Building.getBuilding (cell.getBuildingCoordinates ());
			BuildingManagerItem bmi = BuildingManager.getItem (building.getIniHeader ());
			char groundDataChar = bmi.getGroundData ().charAt ((cell.getCoordinates ().y - cell.getBuildingCoordinates ().y) * bmi.getWidth () + (cell.getCoordinates ().x - cell.getBuildingCoordinates ().x));
			if (groundDataChar == Building.GROUND_TRANSITABLE || groundDataChar == Building.GROUND_ENTRANCE) {
				// Celda transitable, puede pasar siempre
				return true;
			} else {
				// Celda NO principal, sólo se puede pasar si el edificio no está aún operativo
				return !(building.isOperative ());
			}
		} else {
			if (item == null) {
				item = cell.getItem ();
			}
			if (item != null && item.isLocked () && item.isOperative ()) {
				ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());

				// Items tipo "wall" no se puede, locked doors tampoco
				if (imi.isWall () || (imi.isDoor () && item.isDoorStatus (Item.FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED))) {
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * Fills a contextual menú refering a living entity of a cell
	 * 
	 * @param cell
	 * @param sm
	 */
	public static void fillMenu (Cell cell, SmartMenu sm) {
		Citizen.fillMenu (cell, sm);
		Hero.fillMenu (cell, sm);
		Enemy.fillMenu (cell, sm);

		ArrayList<LivingEntity> alLivings = cell.getLivings ();

		if (alLivings != null) {
			LivingEntity le;
			LivingEntityManagerItem lemi;
			for (int liv = 0; liv < alLivings.size (); liv++) {
				le = alLivings.get (liv);
				lemi = LivingEntityManager.getItem (le.getIniHeader ());
				if (TownsProperties.DEBUG_MODE) {
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "ID: " + le.getID () + " (" + le.getIniHeader () + ")", null, null, null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "path size: " + le.getPath ().size (), null, null, null)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "path pos. offset: " + le.getPositionOffset ().x + "," + le.getPositionOffset ().y, null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "path pos. offset constant: " + le.getPositionOffsetConstants ().x + "," + le.getPositionOffsetConstants ().y, null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Focus: " + le.getFocusData ().getEntityID (), null, null, null)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "LOS: " + le.getLivingEntityData ().getLOSCurrent (), null, null, null)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "HPs " + le.getLivingEntityData ().getHealthPoints () + "/" + le.getLivingEntityData ().getHealthPointsMAXCurrent (), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Defense " + le.getLivingEntityData ().getDefenseCurrent (), null, null, null)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Damage " + le.getLivingEntityData ().getDamageCurrent (), null, null, null)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "attackSpeed " + le.getAttackSpeedCounter () + "/" + le.getLivingEntityData ().getAttackSpeedCurrent (), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "walkSpeed" + le.getWalkSpeedCounter () + "/" + le.getLivingEntityData ().getWalkSpeedCurrent (), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "movePCT " + le.getLivingEntityData ().getMovePCTCurrent () + "/" + le.getLivingEntityData ().getMovePCTBase (), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
					if (le.getFoodNeededTurns () != 0) {
						sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "foodNeededTurns " + le.getFoodNeededTurns (), null, null, null)); //$NON-NLS-1$
					}
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Num in world (discovered): " + getNumLivings (le.getIniHeader (), true), null, null, null)); //$NON-NLS-1$
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "Num in world (undiscovered): " + getNumLivings (le.getIniHeader (), false), null, null, null)); //$NON-NLS-1$
					if (lemi.getType () == TYPE_ENEMY) {
						Enemy enemy = (Enemy) le;
						if (enemy.getSiegeData () != null) {
							sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "siegeType " + enemy.getSiegeData ().getType (), null, null, null)); //$NON-NLS-1$
							sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "siegecount " + enemy.getSiegeData ().getCount (), null, null, null)); //$NON-NLS-1$
						}
					}

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

					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, Messages.getString ("LivingEntity.7") + sBuffer.toString (), null, null, null, null, null, Color.ORANGE)); //$NON-NLS-1$
				}

				// Actions
				if (lemi.hasActions ()) {
					ActionManagerItem ami;
					for (int j = 0; j < lemi.getActions ().size (); j++) {
						ami = ActionManager.getItem (lemi.getActions ().get (j));
						sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, ami.getName () + " " + lemi.getName ().toLowerCase (), null, CommandPanel.COMMAND_CUSTOM_ACTION_DIRECT_LIVING, ami.getId (), Integer.toString (le.getID ()))); //$NON-NLS-1$
					}
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
				}

				// Caravan
				if (lemi.getCaravan () != null && Game.getWorld ().getCurrentCaravanData () != null) {
					Game.getWorld ().getCurrentCaravanData ().updateCaravanStatus ();
					if (Game.getWorld ().getCurrentCaravanData ().getStatus () == CaravanData.STATUS_IN_PLACE) {
						sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("LivingEntity.9"), null, CommandPanel.COMMAND_TRADE, null)); //$NON-NLS-1$
						sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
					}
				}
			}
		}
	}


	public String getTileName () {
		return LivingEntityManager.getItem (getIniHeader ()).getName ();
	}


	public String getDeadMessage () {
		return getLivingEntityData ().getName () + Messages.getString ("Enemy.0"); //$NON-NLS-1$
	}


	/**
	 * Retorna el mensaje a sacar cuando alguien ha muerto
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	private String getCellDeadMessage (int x, int y, int z, int livingType) {
		boolean bUnknownDead = true;

		String sDeadMessage;
		if (livingType == TYPE_CITIZEN) {
			sDeadMessage = ((Citizen) this).getCitizenData ().getFullName ();
		} else { // Hero
			sDeadMessage = ((Hero) this).getCitizenData ().getFullName ();
		}

		Cell cell = World.getCell (x, y, z);
		if (cell.getTerrain ().hasFluids ()) {
			if (cell.getTerrain ().getFluidType () == Terrain.FLUIDS_WATER) {
				// Agua
				sDeadMessage += Messages.getString ("Citizen.9"); //$NON-NLS-1$
				bUnknownDead = false;
			} else {
				// Lava
				sDeadMessage += Messages.getString ("Citizen.10"); //$NON-NLS-1$
				bUnknownDead = false;
			}
		} else {
			if (cell.hasBuilding ()) {
				if (Building.getBuilding (cell.getBuildingCoordinates ()).isOperative ()) {
					sDeadMessage += Messages.getString ("Citizen.11"); //$NON-NLS-1$
					bUnknownDead = false;
				}
			}
		}

		if (bUnknownDead) {
			sDeadMessage += Messages.getString ("Citizen.36"); //$NON-NLS-1$
		}

		sDeadMessage += "."; //$NON-NLS-1$

		return sDeadMessage;
	}


	/**
	 * Devuelve una living entity hate de la celda y living pasada Se le pasa un focus, si tiene alguien ahí, eso tiene preferencia
	 * 
	 * @param sIniHeader
	 * @param p3d
	 * @return
	 */
	public static LivingEntity getLivingEntityHate (String sIniHeader, int x, int y, int z, FocusData focusData, boolean bAttackAllies) {
		// Primero de todo miramos el focus
		if (focusData != null && focusData.getEntityID () != -1) {
			LivingEntity leFocus = World.getLivingEntityByID (focusData.getEntityID ());
			if (leFocus != null && leFocus.getCoordinates ().x == x && leFocus.getCoordinates ().y == y && leFocus.getCoordinates ().z == z) {
				if (!bAttackAllies) {
					return leFocus;
				} else {
					// Attack allies
					if (LivingEntityManager.getItem (leFocus.getIniHeader ()).getType () == LivingEntityManager.getItem (sIniHeader).getType ()) {
						return leFocus;
					}
				}
			}
		}

		// Por cada living en el destino miramos si la entity los odia
		ArrayList<LivingEntity> alLivings = World.getCell (x, y, z).getLivings ();

		if (alLivings != null) {
			if (!bAttackAllies) {
				HateData hateData = LivingEntityManager.getHateData (sIniHeader);

				LivingEntity le;
				for (int i = 0; i < alLivings.size (); i++) {
					le = alLivings.get (i);
					if (hateData.isHate (le)) {
						return le;
					}
				}
			} else {
				// Ataca aliados
				int iSourceType = LivingEntityManager.getItem (sIniHeader).getType ();

				LivingEntity le;
				for (int i = 0; i < alLivings.size (); i++) {
					le = alLivings.get (i);
					if (LivingEntityManager.getItem (le.getIniHeader ()).getType () == iSourceType) {
						return le;
					}
				}
			}
		}

		return null;
	}


	protected void doFocus (LivingEntityManagerItem lemi) {
		// Si el "enemigo" (<hate>) aún existe vamos hacia él
		LivingEntity leHate = World.getLivingEntityByID (getFocusData ().getEntityID ());

		if (leHate != null) {
			// Lo tenemos
			// Ataque melee o ranged?
			boolean bRanged = getEquippedData ().getWeapon () != null && ItemManager.getItem (getEquippedData ().getWeapon ().getIniHeader ()).isRanged ();
			if (bRanged) {
				if (Math.abs (leHate.getCoordinates ().x - getX ()) <= getLivingEntityData ().getLOSCurrent () && Math.abs (leHate.getCoordinates ().y - getY ()) <= getLivingEntityData ().getLOSCurrent ()) {
					// Está a rango, disparamos
					doRangeAttack (this, leHate, true);
					setPath (null); // Importante
				} else {
					// No está a rango, nos olvidamos, pero nos acercamos si podemos
					if (World.getCell (leHate.getCoordinates ()).getAstarZoneID () == World.getCell (getCoordinates ()).getAstarZoneID ()) {
						// Misma zona
						int iCellsToRemove = getLivingEntityData ().getLOSCurrent () - 1;
						if (iCellsToRemove < 1) {
							iCellsToRemove = 1;
						}
						setDestination (leHate.getCoordinates (), iCellsToRemove);
					}
					getFocusData ().setEntityID (-1);
					getFocusData ().setEntityType (TYPE_UNKNOWN);
					setFighting (false);
				}
			} else {
				// Melee
				if (World.getCell (leHate.getCoordinates ()).getAstarZoneID () == World.getCell (getCoordinates ()).getAstarZoneID ()) {
					setDestination (leHate.getCoordinates ());
				} else {
					getFocusData ().setEntityID (-1);
					getFocusData ().setEntityType (TYPE_UNKNOWN);
					setFighting (false);
				}
			}

			if (lemi.getType () == TYPE_CITIZEN) {
				// Citizen, le quitamos el sueño en caso de lucha
				if (((Citizen) this).getCitizenData ().getSleepSleeping () <= 0) {
					((Citizen) this).getCitizenData ().setSleepSleeping (1);
				}
			}

			// Si tiene grouping buscamos amigos cercanos para que vayan a atacar en grupo
			if (hasGrouping ()) {
				for (int x = getX () - (getLivingEntityData ().getLOSCurrent () * 2); x <= (getX () + (getLivingEntityData ().getLOSCurrent () * 2)); x++) {
					for (int y = getY () - (getLivingEntityData ().getLOSCurrent () * 2); y <= (getY () + (getLivingEntityData ().getLOSCurrent () * 2)); y++) {
						if (x >= 0 && x < World.MAP_WIDTH && y >= 0 && y < World.MAP_HEIGHT) {
							Cell cell = World.getCell (x, y, getZ ());
							LivingEntity le = cell.containsSpecificLiving (lemi.getType ());
							if (le != null && le.getFocusData ().getEntityID () == -1 && le.getIniHeader ().equals (getIniHeader ())) {
								le.setFocusData (getFocusData ());
							}
						}
					}
				}
			}
		} else {
			// El enemigo ya no existe
			getFocusData ().setEntityID (-1);
			getFocusData ().setEntityType (TYPE_UNKNOWN);
			setFighting (false);
		}
	}


	private void castEffects (EffectManagerItem emi, EffectData effectData) {
		// Miramos en un radio de LOS * 2 si hay livings
		int radio = getLivingEntityData ().getLOSCurrent () * 2;
		ArrayList<LivingEntity> alLivings = new ArrayList<LivingEntity> ();

		int minX = getX () - radio;
		if (minX < 0) {
			minX = 0;
		}
		int minY = getY () - radio;
		if (minY < 0) {
			minY = 0;
		}
		int maxX = getX () + radio;
		if (maxX >= World.MAP_WIDTH) {
			maxX = World.MAP_WIDTH - 1;
		}
		int maxY = getY () + radio;
		if (maxY >= World.MAP_HEIGHT) {
			maxY = World.MAP_HEIGHT - 1;
		}
		int z = getZ ();

		Cell cell;
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				cell = World.getCell (x, y, z);
				if (cell.isDiscovered ()) {
					ArrayList<LivingEntity> alLivingsCell = cell.getLivings ();
					LivingEntity le;
					if (alLivingsCell != null && alLivingsCell.size () > 0) {
						for (int l = 0; l < alLivingsCell.size (); l++) {
							le = alLivingsCell.get (l);
							if (effectData.getCastTargets ().isHate (le)) {
								alLivings.add (le);
							}
						}
					}
				}
			}
		}

		// Tenemos las livings targets, les aplicamos los efectos
		for (int i = 0; i < alLivings.size (); i++) {
			for (int e = 0; e < emi.getCastEffects ().size (); e++) {
				alLivings.get (i).addEffect (EffectManager.getItem (emi.getCastEffects ().get (e)), true);
			}
		}
	}


	public boolean nextTurn () {
		LivingEntityManagerItem lemi = LivingEntityManager.getItem (getIniHeader ());
		// Contadores de citizen
		if (lemi.getType () == TYPE_CITIZEN) {
			((Citizen) this).refreshCounters ();
		} else if (lemi.getType () == TYPE_HERO) {
			((Hero) this).refreshCounters ();
		}

		// Damage animation
		if (getDamageAnimationCounter () > 0) {
			setDamageAnimationCounter (getDamageAnimationCounter () - 1);
		}

		// Attack animation
		if (getAttackAnimationCounter () > 0) {
			setAttackAnimationCounter (getAttackAnimationCounter () - 1);
		}

		// Effects
		EffectData effectData;
		boolean bAttackAllies = false;
		for (int i = (getLivingEntityData ().getEffects ().size () - 1); i >= 0; i--) {
			effectData = getLivingEntityData ().getEffects ().get (i);

			// Aplicamos DOTs
			if (effectData.getDOT () != 0) {
				getLivingEntityData ().setHealthPoints (getLivingEntityData ().getHealthPoints () - effectData.getDOT ());
				if (getLivingEntityData ().getHealthPoints () > getLivingEntityData ().getHealthPointsMAXCurrent ()) {
					getLivingEntityData ().setHealthPoints (getLivingEntityData ().getHealthPointsMAXCurrent ());
				}
			}

			// attackAllies
			if (effectData.isAttackAllies ()) {
				bAttackAllies = true;
			}

			// removeTarget
			if (effectData.isRemoveTarget ()) {
				getFocusData ().setEntityID (-1);
				getFocusData ().setEntityType (TYPE_UNKNOWN);
                setFighting (false);
				clearPath ();
			}

			// SpeedPCT = 0 hace que no se mueva ni haga nada
			if (effectData.getSpeedPCT () == 0) {
				clearPath ();
			}

			// Happy
			if (effectData.getHappy () != 0) {
				if (lemi.getType () == TYPE_CITIZEN) {
					Citizen cit = (Citizen) this;
					if (!cit.getSoldierData ().isSoldier ()) {
						cit.getCitizenData ().setHappiness (cit.getCitizenData ().getHappiness () + effectData.getHappy ());
					}
					// No hace falta mirar si se sale de rango, ya lo hace el setHappiness ()
				}
			}

			// Flee
			if (effectData.isFlee () && !isWaitingForPath ()) {
				if (lemi.getType () == TYPE_CITIZEN) {
					// Si es un citizen lo mandamos a la personal room (si tiene)
					Citizen cit = (Citizen) this;
					if (cit.getCitizenData ().getZoneID () != -1) {
						// Tiene room, lo mandamos pacasa
						Zone zone = Zone.getZone (cit.getCitizenData ().getZoneID ());
						if (zone != null) {
							Point3DShort p3ds = Zone.getFreeCellAtRandom (zone, World.getCell (getCoordinates ()).getAstarZoneID ());
							if (p3ds != null) {
								effectData.setFlee (false); // Para evitar stucks
								getLivingEntityData ().getEffects ().set (i, effectData);
								setDestination (p3ds);
								fleeing = getLivingEntityData ().getLOSCurrent () * 8;
							}
						}
					}
				} else if (lemi.getType () == TYPE_HERO) {
					// Si es un heroe lo mandamos a la starting zone
					ArrayList<HeroPrerequisite> alPrerequisites = HeroManager.getComePrerequisites (getIniHeader ());
					if (alPrerequisites != null) {
						HeroPrerequisite hp;
						for (int p = 0; p < alPrerequisites.size (); p++) {
							hp = alPrerequisites.get (p);
							if (hp.getId () == HeroPrerequisite.ID_ZONE) {
								// Bingo
								String sZone = hp.getValueString ();
								if (sZone != null) {
									// Buscamos una zona de estas y mandamos al heroe
									ArrayList<Point3DShort> alPoints = new ArrayList<Point3DShort> ();

									Zone zone;
									if (Game.getWorld ().getZones () != null) {
										for (int z = 0; z < Game.getWorld ().getZones ().size (); z++) {
											zone = Game.getWorld ().getZones ().get (z);
											if (zone.getIniHeader ().equals (sZone)) {
												Point3DShort p3ds = Zone.getFreeCellAtRandom (zone, World.getCell (getCoordinates ()).getAstarZoneID ());
												if (p3ds != null) {
													alPoints.add (p3ds);
												}
											}
										}
									}

									if (alPoints.size () > 0) {
										effectData.setFlee (false); // Para evitar stucks
										getLivingEntityData ().getEffects ().set (i, effectData);

										// Pillamos un punto a random
										setDestination (alPoints.get (Utils.getRandomBetween (0, (alPoints.size () - 1))));
										fleeing = getLivingEntityData ().getLOSCurrent () * 8;
										break;
									}
								}
							}
						}
					}
				}
			}

			// Casts
			if (effectData.getCastCooldownMAX () > 0) {
				effectData.setCastCooldown (effectData.getCastCooldown () + 1);

				if (effectData.getCastCooldown () >= effectData.getCastCooldownMAX ()) {
					effectData.setCastCooldown (effectData.getCastCooldownMAX ());
					// Lanza el efecto si no hay trigger (si hay trigger lo evaluamos)
					boolean bCanCast = false;
					if (effectData.getCastTrigger () == SkillManagerItem.USE_UNKNOWN) {
						bCanCast = true;
					} else {
						// Miramos si salta el trigger
						if (effectData.getCastTrigger () == SkillManagerItem.USE_ALWAYS) {
							bCanCast = true;
						} else if (effectData.getCastTrigger () == SkillManagerItem.USE_NOT_MAX_HP) {
							if (getLivingEntityData ().getHealthPoints () < getLivingEntityData ().getHealthPointsMAXCurrent ()) {
								bCanCast = true;
							}
						}
					}

					if (bCanCast) {
						EffectManagerItem emi = EffectManager.getItem (effectData.getEffectID ());

						// Lanza efectos a los castTargets que tenga definidos
						castEffects (emi, effectData);

						// Reseteamos el cooldown
						effectData.setCastCooldownMAX (Utils.launchDice (emi.getCastCooldown ()));
						effectData.setCastCooldown (0);
					}
				}
			}

			// Miramos si acaba
			effectData.setLasts (effectData.getLasts () - 1);
			if (effectData.getLasts () <= 0) {
				EffectManagerItem emi = EffectManager.getItem (effectData.getEffectID ());

				if (emi.getLasts () != null) { // Tiene lasts, pues puede acabar
					removeEffect (emi.getId (), lemi.getType (), true);
				} else {
					// No tiene lasts, reseteo para que no se pase de rango (aunque podrían pasar años reales para que esto pase, creo)
					effectData.setLasts (100000);
				}
			}
		}

		// Si llega a 0 puntos de vida palma
		if (getLivingEntityData ().getHealthPoints () <= 0) {
			if (lemi.getType () == TYPE_CITIZEN) {
				MessagesPanel.addMessage (MessagesPanel.TYPE_COMBAT, ((Citizen) this).getCitizenData ().getFullName () + Messages.getString ("Citizen.3"), ColorGL.RED, getCoordinates ()); //$NON-NLS-1$
				MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, ((Citizen) this).getCitizenData ().getFullName () + Messages.getString ("Citizen.3"), ColorGL.RED, getCoordinates ()); //$NON-NLS-1$
			} else if (lemi.getType () == TYPE_HERO) {
				MessagesPanel.addMessage (MessagesPanel.TYPE_HEROES, ((Hero) this).getCitizenData ().getFullName () + Messages.getString ("Citizen.3"), ColorGL.RED, getCoordinates ()); //$NON-NLS-1$
				MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, ((Hero) this).getCitizenData ().getFullName () + Messages.getString ("Citizen.3"), ColorGL.RED, getCoordinates ()); //$NON-NLS-1$
			} else if (lemi.getType () == TYPE_ALLY) {
				MessagesPanel.addMessage (MessagesPanel.TYPE_COMBAT, getDeadMessage (), new ColorGL (Color.ORANGE), getCoordinates ());
			} else {
				if (getFocusData () != null && getFocusData ().getEntityType () == TYPE_HERO) {
					MessagesPanel.addMessage (MessagesPanel.TYPE_HEROES, getDeadMessage (), new ColorGL (Color.GREEN.darker ()), getCoordinates ());
				} else {
					MessagesPanel.addMessage (MessagesPanel.TYPE_COMBAT, getDeadMessage (), new ColorGL (Color.GREEN.darker ()), getCoordinates ());
				}
			}
			return true;
		}

		// Food needed (nunca valdrá 0, si vale 0 es que el bicho no necesita comer)
		if (getFoodNeededTurns () != 0) {
			setFoodNeededTurns (getFoodNeededTurns () - 1);
			if (getFoodNeededTurns () == 0) {
				// Ha LLEGADO a 0, creamos la tarea de comer, y lo ponemos a -1 (ya que nunca vale 0, sólo cuando el living no come)
				setFoodNeededTurns (-1);

				Task taskFood = new Task (Task.TASK_FOOD_NEEDED);
				taskFood.setPointIni (getCoordinates ());
				taskFood.setPointEnd (getCoordinates ());
				taskFood.setParameter (Integer.toString (getID ()));
				taskFood.setZoneHotPoints ();
				Game.getWorld ().getTaskManager ().addTask (taskFood);
			} else if (getFoodNeededTurns () < 0) {
				// Está en negativo, miramos si ya toca palmar
				if (Math.abs (getFoodNeededTurns ()) >= lemi.getFoodNeededDieTurns ()) {
					// BAM, muere de hambre
					MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, lemi.getName () + Messages.getString ("Citizen.8"), new ColorGL (Color.ORANGE), getCoordinates ()); //$NON-NLS-1$
					return true;
				}
			}
		}

		if (isWaitingForPath ()) {
			return false;
		}

		short x = getX ();
		short y = getY ();
		short z = getZ ();

		boolean bWallFluidsHole = false;
		if (!isCellAllowed (x, y, z)) {
			boolean palma = true;
			// En caso de muro (o puerta bloqueada) o agua o hole no palma, son así de guais
			Cell cell = World.getCell (x, y, z);
			if (cell.getTerrain ().hasFluids ()) {
				palma = false;
				bWallFluidsHole = true;
			} else if (cell.hasItem ()) {
				Item item = cell.getItem ();
				if (item.isOperative () && item.isLocked ()) {
					ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
					if (imi.isWall () || (imi.isDoor () && item.isDoorStatus (Item.FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED))) {
						// Caso de wall, no palma
						palma = false;
						bWallFluidsHole = true;
					}
				}
			}

			if (cell.isDigged ()) { // Cuidado con el órden, la comprobación de digged debe ir aquí ya que sabemos que no hay item en la celda
				palma = false;
				bWallFluidsHole = true;
			}

			// If the townie is on a non-mined cell (only happens when terraforming) they won't die
			if (!cell.isMined ()) {
				palma = false;
				bWallFluidsHole = true;
			}

			if (palma) {
				if (lemi.getType () == TYPE_CITIZEN || lemi.getType () == TYPE_HERO) {
					MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, getCellDeadMessage (x, y, z, lemi.getType ()), ColorGL.RED, getCoordinates ());
				}
				return true;
			}
		}

		Game.iError = 980;

		// HP Regeneration
		if (lemi.getHPRegeneration () > 0) {
			// Health points
			if (getLivingEntityData ().getHealthPoints () < getLivingEntityData ().getHealthPointsMAXCurrent ()) {
				getLivingEntityData ().setHealthPoints (getLivingEntityData ().getHealthPoints () + lemi.getHPRegeneration ());
				if (getLivingEntityData ().getHealthPoints () > getLivingEntityData ().getHealthPointsMAXCurrent ()) {
					getLivingEntityData ().setHealthPoints (getLivingEntityData ().getHealthPointsMAXCurrent ());
				}
			}
		}

		// Comer y/o dormir y happiness (CITIZENS)
		if (lemi.getType () == TYPE_CITIZEN) {
			Citizen cit = (Citizen) this;
			if (cit.updateEatSleep ()) {
				return true;
			}
			// Happiness
			CitizenData cd = cit.getCitizenData ();
			if (!cit.getSoldierData ().isSoldier ()) {
				if (Task.isWorkingTask (cit.getCurrentTask ())) {
					// Working
					if (cd.getHappinessWorkCounter () <= 0) {
						if (Game.getWorld ().getTurn () % (World.TIME_MODIFIER_HOUR / 2) == 0) {
							cd.setHappiness (cd.getHappiness () - 1);
						}
					} else {
						cd.setHappinessWorkCounter (cd.getHappinessWorkCounter () - 1);
					}

					cd.setHappinessIdleCounter (cd.getHappinessIdleCounter () + 1);
					if (cd.getHappinessIdleCounter () > cd.getHappinessIdleCounterMax ()) {
						cd.setHappinessIdleCounter (cd.getHappinessIdleCounterMax ());
					}
				} else {
					// Not working
					if (cd.getHappinessIdleCounter () <= 0) {
						if (Game.getWorld ().getTurn () % (World.TIME_MODIFIER_HOUR / 2) == 0) {
							// Sólo restamos happiness si realmente está idle, no cuando come o duerme
							if (cit.getCurrentTask () == null || cit.getCurrentTask ().getTask () == Task.TASK_NO_TASK) {
								cd.setHappiness (cd.getHappiness () - 1);
							}
						}
					} else {
						cd.setHappinessIdleCounter (cd.getHappinessIdleCounter () - 1);
					}

					cd.setHappinessWorkCounter (cd.getHappinessWorkCounter () + 1);
					if (cd.getHappinessWorkCounter () > cd.getHappinessWorkCounterMax ()) {
						cd.setHappinessWorkCounter (cd.getHappinessWorkCounterMax ());
					}
				}
			}
		} else if (lemi.getType () == TYPE_HERO) {
			// Eat sleep + turnos mínimos de stay (HEROS)
			((Hero) this).updateCounters ();
		}

		// Si está en un muro o fluidos lo movemos si o si
		if (bWallFluidsHole) {
			if (!forceMove ()) {
				if (lemi.getType () == TYPE_CITIZEN || lemi.getType () == TYPE_HERO) {
					MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, getCellDeadMessage (x, y, z, lemi.getType ()), ColorGL.RED, getCoordinates ());
				}
				return true;
			}
			return false;
		}

		Game.iError = 981;

		// Edad (NO citizens, NO heroes)
		if (lemi.getType () != TYPE_CITIZEN && lemi.getType () != TYPE_HERO) {
			if (getMaxAge () > 0) {
				// Tiene fecha de caducidad, lo hacemos "crecer" y controlamos que no muera de viejo
				setAge (getAge () + 1);

				if (getAge () >= getMaxAge ()) {
					return true;
				}
			}
		}

		// LOS counter
		setCheckLOSCounter (getCheckLOSCounter () + 1);
		if (lemi.getType () == TYPE_CITIZEN) {
			if (getCheckLOSCounter () > (2 * getLivingEntityData ().getLOSCurrent ())) {
				setCheckLOSCounter (0);
			}
		} else if (lemi.getType () == TYPE_HERO) {
			if (getCheckLOSCounter () > (getLivingEntityData ().getLOSCurrent ())) {
				setCheckLOSCounter (0);
			}
		} else {
			if (getCheckLOSCounter () > (8 * getLivingEntityData ().getLOSCurrent ())) {
				setCheckLOSCounter (0);
			}
		}

		// Fleeing
		if (fleeing > 0) {
			fleeing--;
		}

		// Discover in LOS
		if (lemi.getType () == TYPE_CITIZEN || lemi.getType () == TYPE_HERO) {
			// Ésto es para que descubran celdas NO discovered
			if (getCheckLOSCounter () == 0) {
				if (getCoordinates ().z > 0) { // Las celdas de arriba están todas discovered
					discoverInLOS (lemi.getType (), getCoordinates (), getLivingEntityData ().getLOSCurrent ());
				}
			}
		}

		// Antes de seguir el camino miramos si tiene focus
		if (getFocusData ().getEntityID () == -1 && fleeing == 0) {
			FocusData fd = null;
			if (getCheckLOSCounter () == 0) {
				if (lemi.getType () == TYPE_HERO && (getLivingEntityData ().getHealthPointsMAXCurrent () / 3) > getLivingEntityData ().getHealthPoints ()) {
					// Hero con puntos de vida bajos, no hacemos nada
				} else {
					fd = hasEnemyInLOS (getIniHeader (), getCoordinates (), getLivingEntityData ().getLOSCurrent (), bAttackAllies);

					// En el caso de heroes, miramos si tiene una skill lista para ser lanzada con use=ENEMIES_IN_LOS
					if (fd != null && lemi.getType () == TYPE_HERO) {
						((Hero) this).checkAndUseSkills (SkillManagerItem.USE_ENEMIES_IN_LOS);
					}

					// Miramos si hay que lanzar un effecto de ENEMIES_IN_LOS
					checkAndUseEffects (lemi, SkillManagerItem.USE_ENEMIES_IN_LOS);
				}

				if (fd != null && fd.getEntityID () != -1) {
					// Tenemos enemigo
					setFocusData (fd);
					doFocus (lemi);
					return false;
				} else {
					// No ve a nadie en LOS, miramos si hay algún item para pillar (caso heroes)
					if (lemi.getType () == TYPE_HERO && getCheckLOSCounter () == 0) { // Cuidado con esto, se resetea a cero arriba
						Hero hero = (Hero) this;
						if (hero.canChangeTask (true, true) && hero.getHeroData ().getHeroTask ().getTaskID () != HeroTask.TASK_EQUIPING) {
							if (hasBetterItemInLOS (entityTypeInLOS (TYPE_HERO, TYPE_CITIZEN)) != null) {
								hero.getHeroData ().getHeroTask ().setTaskID (HeroTask.TASK_EQUIPING);
								hero.getPath ().clear ();
							}
						}
					}
				}
			}
		}

		// Hero skills
		if (lemi.getType () == TYPE_HERO) {
			((Hero) this).doHeroSkills ();
		}

		// Comprobamos si tiene camino
		if (getPath ().isEmpty ()) {
			Game.iError = 982;
			if (getFocusData ().getEntityID () != -1) {
				// Tiene focus
				doFocus (lemi);
			} else {
				// No hay nadie en linea de visión
				// Primero miramos si tiene un follow
				if (lemi.getFollowEntity () != null && lemi.getFollowEntity ().length > 0) {
					getLivingEntityData ().setFollowTurnsCounter (getLivingEntityData ().getFollowTurnsCounter () - 1);
					if (getLivingEntityData ().getFollowTurnsCounter () <= 0) {
						// Toca seguir al "amo"
						getLivingEntityData ().setFollowTurnsCounter (Utils.launchDice (lemi.getFollowTurns ()));
						// Buscamos una living de estas (normalmente sólo habrá 1 en el mundo, caso Sips) y le hacemos un setdestination
						int[] alLivings = new int [1];
						// Obtenemos una living al azar
						alLivings[0] = lemi.getFollowEntity ()[Utils.getRandomBetween (0, (lemi.getFollowEntity ().length - 1))];
						int iTrys = 4;
						boolean bSearching = true;
						while (bSearching) {
							if (getNumLivings (UtilsIniHeaders.getStringIniHeader (alLivings[0]), true) > 0) {
								bSearching = false;
							} else {
								iTrys--;
								if (iTrys == 0) {
									bSearching = false;
								} else {
									alLivings[0] = lemi.getFollowEntity ()[Utils.getRandomBetween (0, (lemi.getFollowEntity ().length - 1))];
								}
							}
						}

						if (iTrys > 0) {
							// Para hacer que vaya a items a random, pillaré unas coordenadas a random y buscaremos la living más cercana
							Point3DShort p3dRandom = Point3DShort.getPoolInstance (Utils.getRandomBetween (0, World.MAP_WIDTH - 1), Utils.getRandomBetween (0, World.MAP_HEIGHT - 1), Utils.getRandomBetween (0, World.MAP_DEPTH - 1));
							LivingEntity le = searchLivingForcedASZID (p3dRandom, alLivings, true, null, World.getCell (getCoordinates ()).getAstarZoneID ());
							if (le != null) {
								setDestination (le.getCoordinates ());
								return false;
							}
						}
					}
				}
				if (lemi.getType () == TYPE_ENEMY) {
					if (((Enemy) this).doSiegeStuff (lemi)) {
						return true;
					}
				} else if (lemi.getType () == TYPE_CITIZEN) {
					// No tiene focus, miramos si tiene tarea
					((Citizen) this).doTasksStuff ();
				} else if (lemi.getType () == TYPE_HERO) {
					// No tiene focus, hacemos sus cosas
					if (((Hero) this).doHeroStuff ()) {
						return true;
					}
				} else {
					if (lemi.getCaravan () != null && Game.getWorld ().getCurrentCaravanData () != null) {
						// Caravana sin path, si aún tiene el estado de coming hay que updatearlo
						if (Game.getWorld ().getCurrentCaravanData ().updateCaravanStatus ()) {
							return true;
						}
					} else {
						moveAtRandom (getLivingEntityData ().getMovePCTCurrent (), lemi.getType ());
					}
				}
			}
		} else {
			Game.iError = 983;

			// Tiene path, lo seguimos
			// Obtenemos el primer punto, NO se elimina aún del path
			Point3DShort point = getPath ().get (0);

			if (point == null || (point.x == x && point.y == y && point.z == z)) {
				// Hemos llegado al final del camino, path vacío
				clearPath ();
			} else {
				updateFacingDirection (getX (), getY (), getZ (), point.x, point.y, point.z);
				// Miramos si en ese punto hay una living hate
				LivingEntity leHate = getLivingEntityHate (getIniHeader (), point.x, point.y, point.z, getFocusData (), bAttackAllies);
				if (leHate != null) {
					doHit (this, leHate, true);
					if (leHate.getLivingEntityData ().getHealthPoints () <= 0) {
						// clearPath ();
						getFocusData ().setEntityID (-1);
						getFocusData ().setEntityType (TYPE_UNKNOWN);
	                    setFighting (false);
					} else {
						resetPathOffset ();
						if (getAttackAnimationCounter () > 0 && getAttackAnimationCounter () < ATTACK_ANIMATION_MAX_COUNTER) {
							setAttackAnimationCounter (0);
						}
					}
				} else {
					// No hay hater, intentamos movernos
					// Miramos que no haya un wallConector (puerta), en ese caso le metemos hostias hasta que pete (sólo si no está abierta y es enemy)
					// En caso de friendly no puede pasar si está cerrada o locked (excepto caravanas)
					boolean bWallConector = false;
					if (lemi.getType () != TYPE_CITIZEN && lemi.getType () != TYPE_HERO && lemi.getType () != TYPE_ALLY && lemi.getCaravan () == null) {
						Cell cell = World.getCell (point);
						Item item = cell.getItem ();
						if (item != null && item.isOperative ()) {
							ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
							if (lemi.getType () == TYPE_FRIENDLY) {
								// Friendly
								// Miramos que no haya un wallConector (puerta), en ese caso lo paramos
								if (imi != null && imi.isDoor () && !item.isDoorStatus (Item.FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED)) {
									bWallConector = true;

									// Lo sacamos del path
									clearPath ();
								}
							} else if (lemi.getType () == TYPE_ENEMY) {
								// Enemigo
								if (imi != null && imi.isDoor () && item.isDoorStatus (Item.FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED)) {
									bWallConector = true;

									// Le metemos una hostia
									item.doHit (this, getLivingEntityData ().getDamageCurrent ());
									resetPathOffset ();
								}
							}
						}
					}

					if (!bWallConector) {
						boolean bCamina = false;
						if (lemi.getType () != TYPE_CITIZEN || (((Citizen) this).getCurrentTask () == null || ((Citizen) this).getCurrentTask ().getTask () != Task.TASK_HEAL)) {
							// No hay enemigo, miramos si tenemos enemigo en una casilla al lado (esto evita lo de citizen y enemigo corriendo arriba y abajo sin tocarse)
							FocusData fd = null;
							if (lemi.getType () == TYPE_HERO && (getLivingEntityData ().getHealthPointsMAXCurrent () / 3) > getLivingEntityData ().getHealthPoints ()) {
								// Hero con puntos de vida bajos, no hacemos nada
							} else {
								if (fleeing == 0) {
									fd = hasEnemyInLOS (getIniHeader (), getCoordinates (), 1, bAttackAllies);
								}
							}
							if (fd != null && fd.getEntityID () != -1) {
								// Nos paramos
								clearPath ();
								setFocusData (fd);

								// En el caso de heroes, miramos si tiene una skill lista para ser lanzada con use=ENEMIES_IN_LOS
								if (fd != null && lemi.getType () == TYPE_HERO) {
									((Hero) this).checkAndUseSkills (SkillManagerItem.USE_ENEMIES_IN_LOS);
								}

								// Miramos si hay que lanzar un effecto de ENEMIES_IN_LOS
								checkAndUseEffects (lemi, SkillManagerItem.USE_ENEMIES_IN_LOS);
							} else {
								// No hay enemigo, intentamos movernos
								bCamina = true;
							}
						} else {
							// Citizen heleandose (curándose, si)
							bCamina = true;
						}

						// Seguimos el camino
						if (bCamina) {
							if (canWalkNext (this, lemi.getType ())) {
								point = getPath ().remove (0); // En principio no haría falta igualar otra vez el point a esto
								if (isCellAllowed (point.x, point.y, point.z)) {
									boolean bPuede = true;
									if (point.z != getZ ()) {
										if (getX () == point.x && getY () == point.y) {
											// Cambio de nivel, miramos si hay escalera
											if (point.z > getZ ()) {
												// Quiere ir abajo
												if (!Terrain.canGoDown (getX (), getY (), getZ ())) {
													bPuede = false;
												}
											} else {
												// Quiere ir arriba
												if (!Terrain.canGoUp (getX (), getY (), getZ ())) {
													bPuede = false;
												}
											}
										}
									}
									if (bPuede) {
										setCoordinates (point);

										// Se acaba de mover, si es un citizen con tarea de drop y puede soltar el item, lo soltamos aquí mismo
										if (lemi.getType () == TYPE_CITIZEN) {
											Citizen cit = (Citizen) this;
											if (cit.getCurrentTask () != null && cit.getCurrentTask ().getTask () == Task.TASK_DROP) {
												if (cit.getCurrentTask ().getParameter () == null || !cit.getCurrentTask ().getParameter ().equals ("P")) {
													Cell cell = World.getCell (point);
													if (cit.checkCellToDrop (cell)) {
														if (cit.doDrop (cell)) {
															clearPath ();
														}
													}
												}
											}
										}
									} else {
										clearPath ();
									}
								} else {
									// Casilla chunga, lo paramos
									clearPath ();
								}

								// Devolvemos el punto al pool
								if (point instanceof AStarNodo) {
									AStarNodo.returnToPool ((AStarNodo) point);
								} else {
									Point3DShort.returnToPool (point);
								}
							}
						}
					}
				}
			}
		}

		if (lemi.getType () == TYPE_CITIZEN) {
			// Comer y/o dormir (CITIZENS)
			Game.iError = 984;
			((Citizen) this).checkEatSleep ();

			// Puntos de vida bajos, hospitales (CITIZENS)
			Game.iError = 985;
			((Citizen) this).checkHealthPoints ();
		}

		return false;
	}


	/**
	 * Mueve un living a random, comprueba que no caiga en una casilla con gente de su tipo
	 * 
	 * @param iPCT Porcentaje de moverse (suele ser menor que el parámetro, ya que intentará ir a un sitio y si no es allowed no se moverá)
	 * 
	 * @return Punto donde puede ir o null si no se ha podido mover a random
	 */
	protected void moveAtRandom (int iPCT, int iType) {
		moveAtRandom (iPCT, iType, 8);
	}


	protected void moveAtRandom (int iPCT, int iType, int counter) {
		short currentX = getX ();
		short currentY = getY ();
		short currentZ = getZ ();

		if (Utils.getRandomBetween (1, 100) <= iPCT) {
			// Toca moverse
			boolean bMoved = false;

			// 30% de seguir el mismo camino que llevaba
			if (Utils.getRandomBetween (1, 10) <= 3) {
				if (getFacingDirection () == FACING_DIRECTION_NORTH) {
					currentY--;
				} else if (getFacingDirection () == FACING_DIRECTION_SOUTH) {
					currentY++;
				} else if (getFacingDirection () == FACING_DIRECTION_EAST) {
					currentX++;
				} else if (getFacingDirection () == FACING_DIRECTION_WEST) {
					currentX--;
				} else if (getFacingDirection () == FACING_DIRECTION_NORTH_WEST) {
					currentX--;
					currentY--;
				} else if (getFacingDirection () == FACING_DIRECTION_NORTH_EAST) {
					currentX++;
					currentY--;
				} else if (getFacingDirection () == FACING_DIRECTION_SOUTH_WEST) {
					currentX--;
					currentY++;
				} else if (getFacingDirection () == FACING_DIRECTION_SOUTH_EAST) {
					currentX++;
					currentY++;
				} else {
					currentX += (Utils.getRandomBetween (1, 3) - 2);
					currentY += (Utils.getRandomBetween (1, 3) - 2);
				}

				if (currentX >= 0 && currentY >= 0 && currentX < World.MAP_WIDTH && currentY < World.MAP_HEIGHT) {
					Cell cell = World.getCell (currentX, currentY, currentZ);
					if (isCellAllowed (cell)) {
						if (counter > 0 && cell.hasBuilding () && !Building.getBuilding (cell.getBuildingCoordinates ()).isOperative () || cell.containsSpecificLiving (iType) != null) {
						} else {
							Point3DShort p3d = Point3DShort.getPoolInstance (currentX, currentY, currentZ);
							getPath ().add (p3d);
							updatePathConstantOffsets ();
							bMoved = true;
						}
					}
				}
			}

			// No sigue el camino que llevaba, vamos al random
			if (!bMoved) {
				// Obtenemos los puntos donde puede ir y pillamos uno al azar

				currentX = getX ();
				currentY = getY ();

				int iAuxX, iAuxY, iAuxZ;

				// Haremos 1 intentos de momento
				int iTrys = 1;
				int iRandom;
				int iDirection; // DE 0 a 7 (NW, N, NE, W, E, SW, S, SE)
				while (iTrys > 0) {
					iTrys--;
					iAuxX = -1;
					iAuxY = -1;
					iAuxZ = -1;
					iDirection = -1;

					// Del 1 al 9 son los puntos de arriba
					// del 10 al 17 los del nivel
					// del 18 al 26 los 9 de abajo
					iRandom = Utils.getRandomBetween (1, 26);

					switch (iRandom) {
						case 1:
							// if (nodo.z > 0 && nodo.z < World.MAP_NUM_LEVELS_OUTSIDE && World.getCell (nodo.x, nodo.y, nodo.z - 1).isMined ()) {
							if (currentZ > 0 && World.getCell (currentX, currentY, currentZ - 1).isMined ()) {
								iAuxX = currentX - 1;
								iAuxY = currentY - 1;
								iAuxZ = currentZ - 1;
							}
							break;
						case 2:
							if (currentZ > 0 && World.getCell (currentX, currentY, currentZ - 1).isMined ()) {
								iAuxX = currentX;
								iAuxY = currentY - 1;
								iAuxZ = currentZ - 1;
							}
							break;
						case 3:
							if (currentZ > 0 && World.getCell (currentX, currentY, currentZ - 1).isMined ()) {
								iAuxX = currentX + 1;
								iAuxY = currentY - 1;
								iAuxZ = currentZ - 1;
							}
							break;
						case 4:
							if (currentZ > 0 && World.getCell (currentX, currentY, currentZ - 1).isMined ()) {
								iAuxX = currentX - 1;
								iAuxY = currentY;
								iAuxZ = currentZ - 1;
							}
							break;
						case 5:
							if (Terrain.canGoUp (currentX, currentY, currentZ)) {
								iAuxX = currentX;
								iAuxY = currentY;
								iAuxZ = currentZ - 1;
							}
							break;
						case 6:
							if (currentZ > 0 && World.getCell (currentX, currentY, currentZ - 1).isMined ()) {
								iAuxX = currentX + 1;
								iAuxY = currentY;
								iAuxZ = currentZ - 1;
							}
							break;
						case 7:
							if (currentZ > 0 && World.getCell (currentX, currentY, currentZ - 1).isMined ()) {
								iAuxX = currentX - 1;
								iAuxY = currentY + 1;
								iAuxZ = currentZ - 1;
							}
							break;
						case 8:
							if (currentZ > 0 && World.getCell (currentX, currentY, currentZ - 1).isMined ()) {
								iAuxX = currentX;
								iAuxY = currentY + 1;
								iAuxZ = currentZ - 1;
							}
							break;
						case 9:
							if (currentZ > 0 && World.getCell (currentX, currentY, currentZ - 1).isMined ()) {
								iAuxX = currentX + 1;
								iAuxY = currentY + 1;
								iAuxZ = currentZ - 1;
							}
							break;

						case 10:
							iAuxX = currentX - 1;
							iAuxY = currentY - 1;
							iAuxZ = currentZ;
							iDirection = 0;
							break;
						case 11:
							iAuxX = currentX;
							iAuxY = currentY - 1;
							iAuxZ = currentZ;
							iDirection = 1;
							break;
						case 12:
							iAuxX = currentX + 1;
							iAuxY = currentY - 1;
							iAuxZ = currentZ;
							iDirection = 2;
							break;
						case 13:
							iAuxX = currentX - 1;
							iAuxY = currentY;
							iAuxZ = currentZ;
							iDirection = 3;
							break;
						case 14:
							iAuxX = currentX + 1;
							iAuxY = currentY;
							iAuxZ = currentZ;
							iDirection = 4;
							break;
						case 15:
							iAuxX = currentX - 1;
							iAuxY = currentY + 1;
							iAuxZ = currentZ;
							iDirection = 5;
							break;
						case 16:
							iAuxX = currentX;
							iAuxY = currentY + 1;
							iAuxZ = currentZ;
							iDirection = 6;
							break;
						case 17:
							iAuxX = currentX + 1;
							iAuxY = currentY + 1;
							iAuxZ = currentZ;
							iDirection = 7;
							break;

						case 18:
							if (currentZ < (World.MAP_DEPTH - 1) && currentX > 0 && currentY > 0 && World.getCell (currentX - 1, currentY - 1, currentZ).isMined ()) {
								iAuxX = currentX - 1;
								iAuxY = currentY - 1;
								iAuxZ = currentZ + 1;
							}
							break;
						case 19:
							if (currentZ < (World.MAP_DEPTH - 1) && currentY > 0 && World.getCell (currentX, currentY - 1, currentZ).isMined ()) {
								iAuxX = currentX;
								iAuxY = currentY - 1;
								iAuxZ = currentZ + 1;
							}
							break;
						case 20:
							if (currentZ < (World.MAP_DEPTH - 1) && currentX < (World.MAP_WIDTH - 1) && currentY > 0 && World.getCell (currentX + 1, currentY - 1, currentZ).isMined ()) {
								iAuxX = currentX + 1;
								iAuxY = currentY - 1;
								iAuxZ = currentZ + 1;
							}
							break;
						case 21:
							if (currentZ < (World.MAP_DEPTH - 1) && currentX > 0 && World.getCell (currentX - 1, currentY, currentZ).isMined ()) {
								iAuxX = currentX - 1;
								iAuxY = currentY;
								iAuxZ = currentZ + 1;
							}
							break;
						case 22:
							if (Terrain.canGoDown (currentX, currentY, currentZ)) {
								iAuxX = currentX;
								iAuxY = currentY;
								iAuxZ = currentZ + 1;
							}
							break;
						case 23:
							if (currentZ < (World.MAP_DEPTH - 1) && currentX < (World.MAP_WIDTH - 1) && World.getCell (currentX + 1, currentY, currentZ).isMined ()) {
								iAuxX = currentX + 1;
								iAuxY = currentY;
								iAuxZ = currentZ + 1;
							}
							break;
						case 24:
							if (currentZ < (World.MAP_DEPTH - 1) && currentX > 0 && currentY < (World.MAP_HEIGHT - 1) && World.getCell (currentX - 1, currentY + 1, currentZ).isMined ()) {
								iAuxX = currentX - 1;
								iAuxY = currentY + 1;
								iAuxZ = currentZ + 1;
							}
							break;
						case 25:
							if (currentZ < (World.MAP_DEPTH - 1) && currentY < (World.MAP_HEIGHT - 1) && World.getCell (currentX, currentY + 1, currentZ).isMined ()) {
								iAuxX = currentX;
								iAuxY = currentY + 1;
								iAuxZ = currentZ + 1;
							}
							break;
						case 26:
							if (currentZ < (World.MAP_DEPTH - 1) && currentX < (World.MAP_WIDTH - 1) && currentY < (World.MAP_HEIGHT - 1) && World.getCell (currentX + 1, currentY + 1, currentZ).isMined ()) {
								iAuxX = currentX + 1;
								iAuxY = currentY + 1;
								iAuxZ = currentZ + 1;
							}
							break;
					}

					if (iAuxX < 0 || iAuxY < 0 || iAuxX >= World.MAP_WIDTH || iAuxY >= World.MAP_HEIGHT) {
						continue;
					}

					if (isCellAllowed (iAuxX, iAuxY, iAuxZ)) {
						Cell cell = World.getCell (iAuxX, iAuxY, iAuxZ);
						if (counter > 0 && cell.hasBuilding () && !Building.getBuilding (cell.getBuildingCoordinates ()).isOperative () || cell.containsSpecificLiving (iType) != null) {
							moveAtRandom (iPCT, iType, (counter - 1));
						} else {
							Point3DShort p3d = Point3DShort.getPoolInstance (iAuxX, iAuxY, iAuxZ);
							getPath ().add (p3d);
							updatePathConstantOffsets ();

							// Miramos en un 50% de caminar 2 celdas
							if (iDirection >= 0 && iDirection < 8 && Utils.getRandomBetween (1, 2) == 1) {
								switch (iDirection) {
									case 0:
										iAuxX--;
										iAuxY--;
										break;
									case 1:
										iAuxY--;
										break;
									case 2:
										iAuxX++;
										iAuxY--;
										break;
									case 3:
										iAuxX--;
										break;
									case 4:
										iAuxX++;
										break;
									case 5:
										iAuxX--;
										iAuxY++;
										break;
									case 6:
										iAuxY++;
										break;
									case 7:
										iAuxX++;
										iAuxY++;
										break;
									default:
								}

								if (iAuxX >= 0 && iAuxX < World.MAP_WIDTH && iAuxY >= 0 && iAuxY < World.MAP_HEIGHT) {
									if (isCellAllowed (iAuxX, iAuxY, iAuxZ)) {
										Point3DShort p3d2 = Point3DShort.getPoolInstance (iAuxX, iAuxY, iAuxZ);
										getPath ().add (p3d2);

										// Un 10% más de moverse otra casilla
										if (Utils.getRandomBetween (1, 10) == 1) {
											switch (iDirection) {
												case 0:
													iAuxX--;
													iAuxY--;
													break;
												case 1:
													iAuxY--;
													break;
												case 2:
													iAuxX++;
													iAuxY--;
													break;
												case 3:
													iAuxX--;
													break;
												case 4:
													iAuxX++;
													break;
												case 5:
													iAuxX--;
													iAuxY++;
													break;
												case 6:
													iAuxY++;
													break;
												case 7:
													iAuxX++;
													iAuxY++;
													break;
												default:
											}
											if (iAuxX >= 0 && iAuxX < World.MAP_WIDTH && iAuxY >= 0 && iAuxY < World.MAP_HEIGHT) {
												if (isCellAllowed (iAuxX, iAuxY, iAuxZ)) {
													Point3DShort p3d3 = Point3DShort.getPoolInstance (iAuxX, iAuxY, iAuxZ);
													getPath ().add (p3d3);
												}
											}
										}
									}
								}

							}
							return;
						}
					}
				}
			}
		}
	}


	private void checkAndUseEffects (LivingEntityManagerItem lemi, int iTrigger) {
		// Miramos los efectos
		EffectData effectData;
		for (int i = (getLivingEntityData ().getEffects ().size () - 1); i >= 0; i--) {
			effectData = getLivingEntityData ().getEffects ().get (i);

			// A ver si alguno tiene el trigger correcto
			if (effectData.getCastTrigger () == iTrigger) {
				// Bingo
				if (effectData.getCastCooldown () >= effectData.getCastCooldownMAX ()) {
					EffectManagerItem emi = EffectManager.getItem (effectData.getEffectID ());
					castEffects (emi, effectData);

					// Reseteamos el cooldown (si tiene)
					if (effectData.getCastCooldownMAX () > 0) {
						effectData.setCastCooldownMAX (Utils.launchDice (emi.getCastCooldown ()));
						effectData.setCastCooldown (0);
					}
				}
			}
		}
	}


	/**
	 * Este método es para notificar a una living entity que ha sido golpeado, se le pasa el atacante
	 * 
	 * @param le El atacante
	 * @param bHitted Indica si le ha pegado o sólo lo ha intentado
	 */
	public void hitted (LivingEntity le, boolean bHitted, int iDamage) {
		// Alguien le ha pegado, seteamos el focus ID
		if (getFocusData ().getEntityID () == -1) {
			getFocusData ().setEntityID (le.getID ());
			getFocusData ().setEntityType (LivingEntityManager.getItem (le.getIniHeader ()).getType ());
            setFighting (true);
		}

		// Animación de hit
		if (bHitted && iDamage > 0) {
			setDamageAnimationCounter (DAMAGE_ANIMATION_FPS);
			setDamageAnimationText (Integer.toString (iDamage * -1));
		}
		
		LivingEntityManagerItem lemi = LivingEntityManager.getItem (getIniHeader ());
		checkAndUseEffects (lemi, SkillManagerItem.USE_HITTED);

		if ((getLivingEntityData ().getHealthPointsMAXCurrent () / 3) >= getLivingEntityData ().getHealthPoints ()) {
			checkAndUseEffects (lemi, SkillManagerItem.USE_NEAR_DEATH);
		}
	}


	/**
	 * Añade un efecto a la living, si ya lo tiene reseteamos el contador (lasts)
	 * 
	 * @param edi
	 */
	public void addEffect (EffectManagerItem emi, boolean applyEffectStuff) {
		addEffect (emi, applyEffectStuff, true);
	}


	/**
	 * Añade un efecto a la living, si ya lo tiene reseteamos el contador (lasts)
	 * 
	 * @param edi
	 */
	public void addEffect (EffectManagerItem emi, boolean applyEffectStuff, boolean verbose) {
		// Le metemos el efecto si no lo tiene ya (o es immune), si ya lo tiene reseteamos el contador de lasts
		ArrayList<EffectData> alCurrentEffects = getLivingEntityData ().getEffects ();
		int iIndexSameEffect = -1;
		EffectData efData;
		EffectManagerItem emiCurrent;
		for (int e = 0; e < alCurrentEffects.size (); e++) {
			efData = alCurrentEffects.get (e);
			if (efData.getEffectID ().equals (emi.getId ())) {
				iIndexSameEffect = e;
				// break; // No hago el break para mirar todos los efectos que tiene, buscando immunities
			}

			emiCurrent = EffectManager.getItem (efData.getEffectID ());
			if (emiCurrent.getEffectsImmune () != null) {
				if (emiCurrent.getEffectsImmune ().contains (emi.getId ())) {
					return;
				}
			}
		}

		// Miramos si el efecto tiene prerequisitos
		if (emi.getEffectsPrerequisite () != null) {
			// Todos los efectos deben existir
			String sEffectID;
			for (int i = 0; i < emi.getEffectsPrerequisite ().size (); i++) {
				sEffectID = emi.getEffectsPrerequisite ().get (i);

				boolean bExiste = false;
				for (int e = 0; e < alCurrentEffects.size (); e++) {
					efData = alCurrentEffects.get (e);
					if (efData.getEffectID ().equals (sEffectID)) {
						bExiste = true;
						break;
					}
				}

				if (!bExiste) {
					return;
				}
			}
		}

		// Si llega aquí es que no es immune al efecto por via efectos
		if (iIndexSameEffect == -1) {
			// Efecto nuevo, miramos immunidades (sólo falta mirar immunidades por living)
			boolean bImmune = false;

			// Immunity por living
			LivingEntityManagerItem lemi = LivingEntityManager.getItem (getIniHeader ());
			ArrayList<String> alImmuneEffects = lemi.getEffectsImmune ();
			if (alImmuneEffects != null) {
				for (int i = 0; i < alImmuneEffects.size (); i++) {
					if (alImmuneEffects.contains (emi.getId ())) {
						bImmune = true;
						break;
					}
				}
			}

			if (!bImmune) {
				EffectData effectData = emi.getEffectDataInstance ();
				getLivingEntityData ().getEffects ().add (effectData);
				if (applyEffectStuff) {
					getLivingEntityData ().setModifiers (this);
				}

				// Si el efecto tiene speedPCT != 100, actualizamos su path constant offsets
				if (effectData.getSpeedPCT () != 100) {
					updatePathConstantOffsets ();
				}
			}
		} else {
			// Ya lo tenía
			getLivingEntityData ().getEffects ().get (iIndexSameEffect).setLasts (Utils.launchDice (emi.getLasts ()));
		}

		if (emi.getGraphicChange () != null) {
			// GRAPHIC CHANGE
			changeGraphic (emi.getGraphicChange ());
		}

		if (iIndexSameEffect == -1 && emi.isMessageWhenGain ()) { // Efecto nuevo que muestra mensajes de gain
			if (verbose && emi.getName () != null && emi.getName ().length () > 0) {
				int iLivingType = LivingEntityManager.getItem (getIniHeader ()).getType ();
				if (iLivingType == TYPE_HERO) {
					MessagesPanel.addMessage (MessagesPanel.TYPE_HEROES, getLivingEntityData ().getName () + Messages.getString ("Hero.9") + emi.getName () + "]", ColorGL.WHITE, getCoordinates (), getID ()); //$NON-NLS-1$ //$NON-NLS-2$
				} else if (iLivingType == TYPE_ENEMY) {
					MessagesPanel.addMessage (MessagesPanel.TYPE_COMBAT, getLivingEntityData ().getName () + Messages.getString ("Hero.9") + emi.getName () + "]", ColorGL.WHITE, getCoordinates (), getID ()); //$NON-NLS-1$ //$NON-NLS-2$
				} else if (iLivingType == TYPE_CITIZEN) {
					MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, getLivingEntityData ().getName () + Messages.getString ("Hero.9") + emi.getName () + "]", ColorGL.WHITE, getCoordinates (), getID ()); //$NON-NLS-1$ //$NON-NLS-2$
				} // else friendlies y allies, no los ponemos
			}
		}
	}


	public void removeEffect (String id, int iLivingType, boolean verbose) {
		EffectData effectData;
		for (int i = (getLivingEntityData ().getEffects ().size () - 1); i >= 0; i--) {
			effectData = getLivingEntityData ().getEffects ().get (i);
			if (effectData.getEffectID ().equals (id)) {
				getLivingEntityData ().getEffects ().remove (i);
				getLivingEntityData ().setModifiers (this);
				if (effectData.isGraphicChange ()) {
					changeGraphic (getIniHeader ());
				}

				EffectManagerItem emi = EffectManager.getItem (effectData.getEffectID ());
				if (verbose && emi.isMessageWhenVanish ()) {
					if (iLivingType == TYPE_HERO) {
						MessagesPanel.addMessage (MessagesPanel.TYPE_HEROES, "[" + EffectManager.getItem (effectData.getEffectID ()).getName () + Messages.getString ("LivingEntity.8") + getLivingEntityData ().getName (), ColorGL.ORANGE, getCoordinates (), getID ()); //$NON-NLS-1$ //$NON-NLS-2$
					} else if (iLivingType == TYPE_CITIZEN || iLivingType == TYPE_ENEMY) {
						MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, "[" + EffectManager.getItem (effectData.getEffectID ()).getName () + Messages.getString ("LivingEntity.8") + getLivingEntityData ().getName (), ColorGL.ORANGE, getCoordinates (), getID ()); //$NON-NLS-1$ //$NON-NLS-2$
					} // else fiendlies y allies, no los ponemos
				}

				// Miramos si tiene afterEffects para meter
				if (emi.getAfterEffects () != null && emi.getAfterEffects ().size () > 0) {
					for (int e = 0; e < emi.getAfterEffects ().size (); e++) {
						addEffect (EffectManager.getItem (emi.getAfterEffects ().get (e)), true);
					}
				}

				// Si era un effect de atacar aliados, borramos el target para que no siga atacándolos
				if (effectData.isAttackAllies ()) {
					getFocusData ().setEntityID (-1);
					getFocusData ().setEntityType (TYPE_UNKNOWN);
                    setFighting (false);
					clearPath ();
				}
				break;
			}
		}
	}


	public static void clear () {
		mapLivingsDiscovered = new HashMap<String, Integer> ();
		mapLivingsUndiscovered = new HashMap<String, Integer> ();
	}


	@SuppressWarnings("unchecked")
	public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal (in);
		path = (ArrayList<Point3DShort>) in.readObject ();
		flags = in.readByte ();
		livingEntityData = (LivingEntityData) in.readObject ();
		equippedData = (EquippedData) in.readObject ();
		age = in.readInt ();
		maxAge = in.readInt ();
		facingDirection = in.readByte ();
		focusData = (FocusData) in.readObject ();
		attackSpeedCounter = in.readInt ();
		walkSpeedCounter = in.readInt ();
		positionOffset = (Point2D.Float) in.readObject ();
		positionOffsetConstants = (Point2D.Float) in.readObject ();
		foodNeededTurns = in.readInt ();
		if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V12) {
			fleeing = in.readInt ();
		} else {
			fleeing = 0;
		}
	}


	public void writeExternal (ObjectOutput out) throws IOException {
		super.writeExternal (out);
		out.writeObject (path);
		out.writeByte (flags);
		out.writeObject (livingEntityData);
		out.writeObject (equippedData);
		out.writeInt (age);
		out.writeInt (maxAge);
		out.writeByte (facingDirection);
		out.writeObject (focusData);
		out.writeInt (attackSpeedCounter);
		out.writeInt (walkSpeedCounter);
		out.writeObject (positionOffset);
		out.writeObject (positionOffsetConstants);
		out.writeInt (foodNeededTurns);
		out.writeInt (fleeing);
	}
}
