package xaos.stockpiles;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.data.Type;
import xaos.data.Types;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.MiniMapPanel;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Cell;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;


public class Stockpile implements Externalizable {

	private static final long serialVersionUID = 5009637447929137812L;

	public static int ID_INDEX = 0;

	private int ID;

	private Type type;
	private ArrayList<Point3DShort> points;

	private int filledPoints;
	private boolean lockedToCopy;


	public Stockpile () {
	}


	public Stockpile (String sType) {
		ID_INDEX++;
		setID (ID_INDEX);
		setType (Types.getType (sType));
		setPoints (new ArrayList<Point3DShort> ());
		setLockedToCopy (false);
	}


	public void setID (int iD) {
		ID = iD;
	}


	public int getID () {
		return ID;
	}


	public Type getType () {
		return type;
	}


	public void setType (Type type) {
		this.type = type;
	}


	public ArrayList<Point3DShort> getPoints () {
		return points;
	}


	private void setPoints (ArrayList<Point3DShort> points) {
		this.points = points;

		if (points.size () > 0) {
			// Minimap reload
			MiniMapPanel.setMinimapReload (points.get (0).z);
		}

		for (int i = 0; i < points.size (); i++) {
			if (World.getCell (points.get (i)).getEntity () != null) {
				setFilledPoints (getFilledPoints () + 1);
			}

			Game.getWorld ().addItemToBeHauled (World.getCell (points.get (i)).getItem ());
		}
	}


	public void addPoint (Point3DShort p3d) {
		getPoints ().add (p3d);

		// Minimap reload
		MiniMapPanel.setMinimapReload (p3d.z);

		if (World.getCell (p3d).getEntity () != null) {
			setFilledPoints (getFilledPoints () + 1);

			Game.getWorld ().addItemToBeHauled (World.getCell (p3d).getItem ());
		}
	}


	public void setFilledPoints (int filledPoints) {
		this.filledPoints = filledPoints;
		if (this.filledPoints > getPoints ().size ()) {
			// Raro, raro
			this.filledPoints = getPoints ().size ();
		}
	}


	public int getFilledPoints () {
		return filledPoints;
	}


	public void setLockedToCopy (boolean lockedToCopy) {
		this.lockedToCopy = lockedToCopy;
	}


	public boolean isLockedToCopy () {
		return lockedToCopy;
	}


	public boolean isFull () {
		return getFilledPoints () >= getPoints ().size ();
	}


	public boolean isEmpty () {
		return getFilledPoints () == 0;
	}


	/**
	 * Indica si el item pasado (iniheader) está permitido por la stockpiles
	 * 
	 * @param sItemIniHeader
	 * @return true si el item pasado (iniheader) está permitido por la stockpiles
	 */
	public boolean itemAllowed (String sItemIniHeader) {
		return getType ().contains (sItemIniHeader);
	}


	/**
	 * Borra la stockpile con el ID indicado
	 * 
	 * @param iID ID
	 */
	public static void deleteStockpile (int iID) {
		ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();
		Stockpile stockpile;

		for (int i = 0; i < stockpiles.size (); i++) {
			stockpile = stockpiles.get (i);
			if (stockpile.getID () == iID) {
				// Eliminamos el flag a todos los puntos
				for (int j = 0; j < stockpile.getPoints ().size (); j++) {
					World.getCell (stockpile.getPoints ().get (j)).setStockPileID (0);

					// Minimap reload
					MiniMapPanel.setMinimapReload (stockpile.getPoints ().get (j).z);
				}

				// Eliminamos la stockpile
				stockpiles.remove (i);
				break;
			}
		}
	}


	/**
	 * Habilita todos los elementos de un subtipo. Si no se le pasa subtipo habilita todo
	 * 
	 * @param iID
	 * @param sSubType
	 */
	public static void enableAll (int iID, String sSubType) {
		if (sSubType == null || sSubType.length () == 0) {
			enableAll (iID);
		} else {
			Type type;
			ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();
			Stockpile stockpile;

			for (int i = 0; i < stockpiles.size (); i++) {
				stockpile = stockpiles.get (i);
				if (stockpile.getID () == iID) {
					type = Types.getType (stockpile.getType ().getID ());
					ItemManagerItem imi;
					for (int t = 0; t < type.getElements ().size (); t++) {
						imi = ItemManager.getItem (type.getElements ().get (t));
						if (imi.getType () != null && imi.getType ().equals (sSubType)) {
							// Item a habilitar si no lo está ya
							if (!stockpile.getType ().contains (imi.getIniHeader ())) {
								stockpile.getType ().addElement (type.getElements ().get (t), type.getElementNames ().get (t));
							}
						}
					}
					break;
				}
			}
		}
	}


	/**
	 * Habilita todos los elementos de la pila
	 * 
	 * @param iID
	 */
	public static void enableAll (int iID) {
		ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();
		Stockpile stockpile;

		for (int i = 0; i < stockpiles.size (); i++) {
			stockpile = stockpiles.get (i);
			if (stockpile.getID () == iID) {
				stockpile.getType ().removeElements ();

				Type type = Types.getType (stockpile.getType ().getID ());
				for (int e = 0; e < type.getElements ().size (); e++) {
					stockpile.getType ().addElement (type.getElements ().get (e), type.getElementNames ().get (e));
				}
				break;
			}
		}
	}


	/**
	 * Inhabilita todos los elementos de la pila. Si se le pasa un subtype deshabilita sólo esos
	 * 
	 * @param iID
	 */
	public static void disableAll (int iID, String sSubType) {
		if (sSubType == null || sSubType.length () == 0) {
			disableAll (iID);
		} else {
			Type type;
			ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();
			Stockpile stockpile;

			for (int i = 0; i < stockpiles.size (); i++) {
				stockpile = stockpiles.get (i);
				if (stockpile.getID () == iID) {
					type = Types.getType (stockpile.getType ().getID ());
					ItemManagerItem imi;
					for (int t = 0; t < type.getElements ().size (); t++) {
						imi = ItemManager.getItem (type.getElements ().get (t));
						if (imi.getType () != null && imi.getType ().equals (sSubType)) {
							// Item a deshabilitar
							stockpile.getType ().removeElement (imi.getIniHeader ());
						}
					}

					// Metemos todos los items en la pila para hauling
					for (int h = 0; h < stockpile.getPoints ().size (); h++) {
						Game.getWorld ().addItemToBeHauled (World.getCell (stockpile.getPoints ().get (h)).getItem ());
					}
					break;
				}
			}
		}
	}


	/**
	 * Inhabilita todos los elementos de la pila
	 * 
	 * @param iID
	 */
	private static void disableAll (int iID) {
		ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();
		Stockpile stockpile;

		for (int i = 0; i < stockpiles.size (); i++) {
			stockpile = stockpiles.get (i);
			if (stockpile.getID () == iID) {
				stockpile.disableAll ();
				break;
			}
		}
	}


	public void disableAll () {
		getType ().removeElements ();

		// Metemos todos los items en la pila para hauling
		for (int h = 0; h < getPoints ().size (); h++) {
			Game.getWorld ().addItemToBeHauled (World.getCell (getPoints ().get (h)).getItem ());
		}
	}


	/**
	 * Lock/unlocks all the piles configurations
	 */
	public static void lockUnlockAllConfigurations (boolean bLock) {
		ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();
		Stockpile stockpile;

		for (int i = 0; i < stockpiles.size (); i++) {
			stockpile = stockpiles.get (i);
			stockpile.setLockedToCopy (bLock);
		}
	}


	/**
	 * Elimina un punto de stockpile, comprueba que la stockpile no se haya quedado sin puntos
	 * 
	 * @param p3d
	 */
	public static void deleteStockpilePoint (Point3DShort p3d) {
		Cell cell = World.getCell (p3d);

		if (cell.hasStockPile ()) {
			int iPileID = cell.getStockPileID ();

			// Buscamos la stockpile
			ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();
			Stockpile stockpile;

			for (int i = 0; i < stockpiles.size (); i++) {
				stockpile = stockpiles.get (i);
				if (stockpile.getID () == iPileID) {
					// Tenemos la stockpile, quitamos el punto del a pila y el flag de la celda
					stockpile.getPoints ().remove (p3d);
					cell.setStockPileID (0);

					if (!cell.isEmpty ()) {
						stockpile.setFilledPoints (stockpile.getFilledPoints () - 1);
					}
					if (stockpile.getPoints ().size () == 0) {
						// Stockpile sin puntos, la borramos
						deleteStockpile (iPileID);
					}
					break;
				}
			}

			// Minimap reload
			MiniMapPanel.setMinimapReload (p3d.z);
		}
	}


	/**
	 * Elimina un punto de stockpile, comprueba que la stockpile no se haya quedado sin puntos
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void deleteStockpilePoint (short x, short y, short z) {
		deleteStockpilePoint (Point3DShort.getPoolInstance (x, y, z));
	}


	/**
	 * Indica si en la casilla pasada se puede crear un stockpile
	 * 
	 * @param x X
	 * @param y Y
	 * @param z Z
	 * @return true si en la casilla pasada se puede crear un stockpile, false en otro caso
	 */
	public static boolean isCellAvailableForStockpile (int x, int y, int z) {
		// Fuera del mapa
		if (x < 0 || x >= World.MAP_WIDTH || y < 0 || y >= World.MAP_HEIGHT || z < 0 || z >= World.MAP_DEPTH) {
			return false;
		}

		Cell cell = World.getCell (x, y, z);

		boolean checkBasics = cell.isDiscovered () && cell.isMined () && !cell.hasZone () && !cell.getTerrain ().hasFluids () && !cell.hasStockPile ();

		if (!checkBasics) {
			return false;
		}

		if (cell.hasBuilding ()) {
			return false;
		}

		// Falta mirar items
		if (!cell.isEmpty ()) {
			Entity entity = cell.getEntity ();
			if (entity != null && entity instanceof Item) {
				if (((Item) entity).isLocked ()) {
					return false;
				}

				// Miramos que tenga tipo
				ItemManagerItem imi = ItemManager.getItem (entity.getIniHeader ());
				if (imi == null || imi.getType () == null) {
					return false;
				}
			}
		}

		// Digged y base=true debajo
		if (cell.isDigged ()) {
			// Si no hay un base=true debajo la casilla es mala
			if (z < (World.MAP_DEPTH - 1)) {
				Item itemUnder = World.getCell (x, y, z + 1).getItem ();
				if (itemUnder != null) {
					if (!ItemManager.getItem (itemUnder.getIniHeader ()).isBase ()) {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		return true;
	}


	/**
	 * Devuelve el objeto Stockpile de una coordenada, lo saca de la lista de stockpiles Devuelve nulo si no la encuentra
	 * 
	 * @param p3d
	 * @return
	 */
	public static Stockpile getStockpile (Point3DShort p3d) {
		ArrayList<Stockpile> alStockpiles = Game.getWorld ().getStockpiles ();
		ArrayList<Point3DShort> alPoints;
		for (int i = 0; i < alStockpiles.size (); i++) {
			alPoints = alStockpiles.get (i).getPoints ();
			if (alPoints.contains (p3d)) {
				// Tenemos la stockpile
				return alStockpiles.get (i);
			}
		}

		return null;
	}


	/**
	 * Returns a Stockpile from a pile ID
	 * 
	 * @param sPileID
	 * @return
	 */
	public static Stockpile getStockpile (String sPileID) {
		return getStockpile (Integer.parseInt (sPileID));
	}


	/**
	 * Returns a Stockpile from a pile ID
	 * 
	 * @param iPileID
	 * @return
	 */
	public static Stockpile getStockpile (int iPileID) {
		ArrayList<Stockpile> alStockpiles = Game.getWorld ().getStockpiles ();
		for (int i = 0; i < alStockpiles.size (); i++) {
			if (alStockpiles.get (i).getID () == iPileID) {
				return alStockpiles.get (i);
			}
		}

		return null;
	}


	/**
	 * Devuelve el objeto Stockpile de una coordenada, lo saca de la lista de stockpiles
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static Stockpile getStockpile (int x, int y, int z) {
		return getStockpile (Point3DShort.getPoolInstance (x, y, z));
	}


	public static void updateIndexID () {
		/**
		 * Obtiene el ID más alto de Stockpile
		 */
		ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();
		Stockpile pile;
		int iMaxID = -1;
		for (int i = 0; i < stockpiles.size (); i++) {
			pile = stockpiles.get (i);
			if (pile.getID () > iMaxID) {
				iMaxID = pile.getID ();
			}
		}

		ID_INDEX = (iMaxID + 1);
	}


	/**
	 * Fills a contextual menú refering citizens of a cell
	 * 
	 * @param cell
	 * @param sm
	 */
	public static void fillMenu (Cell cell, SmartMenu sm) {
		if (cell.hasStockPile ()) {
			Point3DShort p3d = cell.getCoordinates ();
			Stockpile stockpile = Stockpile.getStockpile (p3d);
			if (stockpile != null) {
				// Manage stockpile
				sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Stockpile.6"), null, CommandPanel.COMMAND_STOCKPILE_MANAGE, Integer.toString (stockpile.getID ()), null, null, Color.GREEN)); //$NON-NLS-1$

				// Information about lock/unlock status
				if (stockpile.isLockedToCopy ()) {
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "(" + Messages.getString ("Stockpile.2") + ")", null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "(" + Messages.getString ("Stockpile.9") + ")", null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}

				// Eliminar stockpile
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
				sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Stockpile.3"), null, CommandPanel.COMMAND_DELETE_STOCKPILE, Integer.toString (stockpile.getID ()), null, null, Color.ORANGE)); //$NON-NLS-1$
				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
			}
		}
	}


	/**
	 * Se recorre todo el menú y setea el valor de los items a enabled o disabled según la configuración de la pila
	 * 
	 * @param iStockpileID
	 * @param menuPile
	 * @return true si todo está ok
	 */
	public static boolean regeneratePilePanelMenu (int iStockpileID, SmartMenu menuPile) {
		if (menuPile == null) {
			return false;
		}

		Stockpile stockpile = null;

		// Obtenemos la pile
		ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();

		for (int i = 0; i < stockpiles.size (); i++) {
			stockpile = stockpiles.get (i);
			if (stockpile.getID () == iStockpileID) {
				// La tenemos
				break;
			}
		}

		if (stockpile == null) {
			return false;
		}

		// Tenemos la pila, ahora recorremos todo recursivamente para enablear o disablear
		regeneratePilePanelMenu (stockpile, menuPile);

		return true;
	}


	private static void regeneratePilePanelMenu (Stockpile stockpile, SmartMenu menuPile) {
		SmartMenu smAux;
		for (int i = 0; i < menuPile.getItems ().size (); i++) {
			smAux = menuPile.getItems ().get (i);
			if (smAux.getType () == SmartMenu.TYPE_MENU) {
				regeneratePilePanelMenu (stockpile, smAux);
			} else {
				// Item
				if (smAux.getCommand ().equals (CommandPanel.COMMAND_STOCKPILE_ENABLE_ITEM) || smAux.getCommand ().equals (CommandPanel.COMMAND_STOCKPILE_DISABLE_ITEM)) {
					// Toca mirar si va a ser enabled o disabled
					if (stockpile.getType ().contains (smAux.getParameter ())) {
						// Existe, pués lo ponemos para disable
						smAux.setCommand (CommandPanel.COMMAND_STOCKPILE_DISABLE_ITEM);
					} else {
						// No existe, lo ponemos para enable
						smAux.setCommand (CommandPanel.COMMAND_STOCKPILE_ENABLE_ITEM);
					}

				}
			}
		}
	}


	public static SmartMenu createPilePanelMenu (int iStockpileID) {
		Stockpile stockpile = null;

		// Obtenemos la pile
		ArrayList<Stockpile> stockpiles = Game.getWorld ().getStockpiles ();

		for (int i = 0; i < stockpiles.size (); i++) {
			stockpile = stockpiles.get (i);
			if (stockpile.getID () == iStockpileID) {
				// La tenemos
				break;
			}
		}

		if (stockpile == null) {
			return null;
		}

		Point3DShort p3d = null;
		if (stockpile.getPoints () != null && stockpile.getPoints ().size () > 0) {
			p3d = stockpile.getPoints ().get (0);
		}

		if (p3d == null) {
			return null;
		}

		String sPileID = Integer.toString (stockpile.getID ());
		// Ya tenemos la pila y un punto donde se encuentra
		SmartMenu smReturn = new SmartMenu (SmartMenu.TYPE_MENU, Messages.getString ("Stockpile.6"), null, null, sPileID); //$NON-NLS-1$

		// Añadimos el enable todo/disable todo
		SmartMenu smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Stockpile.4"), null, CommandPanel.COMMAND_STOCKPILE_ENABLE_ALL, Integer.toString (iStockpileID), null, null, Color.GREEN); //$NON-NLS-1$
		smAux.setIcon ("iconenableall"); //$NON-NLS-1$
		smReturn.addItem (smAux);

		smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Stockpile.5"), null, CommandPanel.COMMAND_STOCKPILE_DISABLE_ALL, Integer.toString (iStockpileID), null, null, Color.ORANGE); //$NON-NLS-1$
		smAux.setIcon ("icondisableall"); //$NON-NLS-1$
		smReturn.addItem (smAux);

		// Añadimos 1 linea por cada objeto del tipo de stockpile (para poner/quitar)
		Type stockpileType = stockpile.getType ();
		Type type = Types.getType (stockpileType.getID ());
		String sItemType;
		StockpileTempData elementsWithoutSubtype = new StockpileTempData ();
		ArrayList<String> alElementsWithSubtypeName = new ArrayList<String> ();
		ArrayList<StockpileTempData> alElementsWithSubtype = new ArrayList<StockpileTempData> ();

		// Parseamos todo
		for (int i = 0; i < type.getElements ().size (); i++) {
			sItemType = ItemManager.getItem (type.getElements ().get (i)).getType ();

			// Miramos si es subtipo
			int iIndex = sItemType.indexOf ('.');
			if (iIndex != -1) {
				// Tiene subtipo
				int iIndexSubtype = alElementsWithSubtypeName.indexOf (sItemType);
				if (iIndexSubtype != -1) {
					// Ya existente, metemos el objeto
					alElementsWithSubtype.get (iIndexSubtype).addElement (type.getElements ().get (i), stockpileType.contains (type.getElements ().get (i)));
				} else {
					// No existe, subtipo nuevo
					StockpileTempData sptd = new StockpileTempData ();
					sptd.addElement (type.getElements ().get (i), stockpileType.contains (type.getElements ().get (i)));

					alElementsWithSubtypeName.add (sItemType);
					alElementsWithSubtype.add (sptd);
				}
			} else {
				// No tiene
				elementsWithoutSubtype.addElement (type.getElements ().get (i), stockpileType.contains (type.getElements ().get (i)));
			}
		}

		// Todo parseado, creamos los menues
		String sItemName;
		for (int i = 0; i < alElementsWithSubtypeName.size (); i++) {
			SmartMenu smSubMenu = new SmartMenu (SmartMenu.TYPE_MENU, Type.getTypeName (alElementsWithSubtypeName.get (i)), smReturn, null, sPileID);
			if (Type.getIcon (alElementsWithSubtypeName.get (i)) != null) {
				smSubMenu.setIcon (Type.getIcon (alElementsWithSubtypeName.get (i)));
			}

			// Añadimos el enable todo/disable todo (del subtipo)
			smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Stockpile.4"), null, CommandPanel.COMMAND_STOCKPILE_ENABLE_ALL, Integer.toString (iStockpileID), alElementsWithSubtypeName.get (i), null, Color.GREEN); //$NON-NLS-1$
			smAux.setIcon ("iconenableall"); //$NON-NLS-1$
			smSubMenu.addItem (smAux);
			smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Stockpile.5"), null, CommandPanel.COMMAND_STOCKPILE_DISABLE_ALL, Integer.toString (iStockpileID), alElementsWithSubtypeName.get (i), null, Color.ORANGE); //$NON-NLS-1$
			smAux.setIcon ("icondisableall"); //$NON-NLS-1$
			smSubMenu.addItem (smAux);

			ArrayList<String> alElements = alElementsWithSubtype.get (i).getAlElements ();
			ArrayList<Boolean> alElementsStatus = alElementsWithSubtype.get (i).getAlElementsStatus ();
			ItemManagerItem imi;
			for (int j = 0; j < alElements.size (); j++) {
				imi = ItemManager.getItem (alElements.get (j));
				if (imi != null) {
					sItemName = imi.getName ();
					if (alElementsStatus.get (j).booleanValue ()) {
						smAux = new SmartMenu (SmartMenu.TYPE_ITEM, sItemName, null, CommandPanel.COMMAND_STOCKPILE_DISABLE_ITEM, alElements.get (j), null, p3d.toPoint3D (), Color.GREEN);
						smAux.setIcon (imi.getIniHeader ());
						smSubMenu.addItem (smAux);
					} else {
						smAux = new SmartMenu (SmartMenu.TYPE_ITEM, sItemName, null, CommandPanel.COMMAND_STOCKPILE_ENABLE_ITEM, alElements.get (j), null, p3d.toPoint3D (), Color.ORANGE);
						smAux.setIcon (imi.getIniHeader ());
						smSubMenu.addItem (smAux);
					}
				}
			}
			smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Building.4"), null, CommandPanel.COMMAND_BACK, null); //$NON-NLS-1$
			smAux.setIcon ("ui_back"); //$NON-NLS-1$
			smSubMenu.addItem (smAux);

			smReturn.addItem (smSubMenu);
		}

		// Ahora los items sin tipo
		ItemManagerItem imi;
		for (int j = 0; j < elementsWithoutSubtype.getAlElements ().size (); j++) {
			imi = ItemManager.getItem (elementsWithoutSubtype.getAlElements ().get (j));
			if (imi != null) {
				sItemName = imi.getName ();
				if (elementsWithoutSubtype.getAlElementsStatus ().get (j).booleanValue ()) {
					smAux = new SmartMenu (SmartMenu.TYPE_ITEM, sItemName, null, CommandPanel.COMMAND_STOCKPILE_DISABLE_ITEM, elementsWithoutSubtype.getAlElements ().get (j), null, p3d.toPoint3D (), Color.GREEN);
					smAux.setIcon (imi.getIniHeader ());
					smReturn.addItem (smAux);
				} else {
					smAux = new SmartMenu (SmartMenu.TYPE_ITEM, sItemName, null, CommandPanel.COMMAND_STOCKPILE_ENABLE_ITEM, elementsWithoutSubtype.getAlElements ().get (j), null, p3d.toPoint3D (), Color.ORANGE);
					smAux.setIcon (imi.getIniHeader ());
					smReturn.addItem (smAux);
				}
			}
		}

		return smReturn;
	}


	@SuppressWarnings("unchecked")
	public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
		ID = in.readInt ();
		type = (Type) in.readObject ();
		points = (ArrayList<Point3DShort>) in.readObject ();
		filledPoints = in.readInt ();
		if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V14e) {
			lockedToCopy = in.readBoolean ();
		} else {
			lockedToCopy = false;
		}

	}


	public void writeExternal (ObjectOutput out) throws IOException {
		out.writeInt (ID);
		out.writeObject (type);
		out.writeObject (points);
		out.writeInt (filledPoints);
		out.writeBoolean (lockedToCopy);
	}
}
