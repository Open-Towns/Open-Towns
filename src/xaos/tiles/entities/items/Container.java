package xaos.tiles.entities.items;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.caravans.PricesManager;
import xaos.data.Type;
import xaos.data.Types;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu;
import xaos.stockpiles.StockpileTempData;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.Citizen;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.UtilsIniHeaders;


public class Container implements Externalizable {

	private static final long serialVersionUID = 3489353715505376433L;

	private int itemID;
	private ArrayList<Item> itemsInside;
	private Type type;
	private boolean wrongItemsInside;
	private boolean lockedToCopy;
	private transient int spaceLeft;


	public Container () {
	}


	public Container (int iID) {
		String sIniHeader = Item.getItemByID (iID).getIniHeader ();
		ItemManagerItem imi = ItemManager.getItem (sIniHeader);

		setType (Types.getType (imi.getType ()));
		itemID = iID;
		itemsInside = new ArrayList<Item> (imi.getContainerSize ());
		spaceLeft = imi.getContainerSize ();

		if (Game.isDisabledItemsON ()) {
			disableAll ();
		} else {
			enableAll ();
		}
	}


	/**
	 * Refresca el contenido del container
	 * 
	 * @return true si el container aún existe en el mundo
	 */
	public boolean refreshTransients () {
		boolean itemOnWorld = Item.getItemByID (itemID) != null;
		ItemManagerItem imiContainer = null;

		if (itemOnWorld) {
			imiContainer = ItemManager.getItem (Item.getItemByID (itemID).getIniHeader ());
		}

		if (!itemOnWorld) {
			// Item no está en el mundo, miramos el carrying de los citizens
			Citizen citizen;
			for (int i = 0; i < World.getCitizenIDs ().size (); i++) {
				citizen = (Citizen) World.getLivingEntityByID (World.getCitizenIDs ().get (i));
				if (citizen.getCarrying () != null && citizen.getCarrying ().getID () == itemID) {
					imiContainer = ItemManager.getItem (citizen.getCarrying ().getIniHeader ());
					itemOnWorld = true;
					break;
				}
			}
			if (!itemOnWorld) {
				for (int i = 0; i < World.getSoldierIDs ().size (); i++) {
					citizen = (Citizen) World.getLivingEntityByID (World.getSoldierIDs ().get (i));
					if (citizen.getCarrying () != null && citizen.getCarrying ().getID () == itemID) {
						imiContainer = ItemManager.getItem (citizen.getCarrying ().getIniHeader ());
						itemOnWorld = true;
						break;
					}
				}
			}
		}

		if (!itemOnWorld) {
			return false;
		}

		if (imiContainer != null) {
			spaceLeft = imiContainer.getContainerSize ();
		}

		ItemManagerItem imiInside;
		for (int i = 0; i < itemsInside.size (); i++) {
			itemsInside.get (i).refreshTransients ();
			Item.addItem (itemsInside.get (i)); // Para que sume 1 al número de items, ya que el item no existe realmente

			// Space left
			imiInside = ItemManager.getItem (itemsInside.get (i).getIniHeader ());
			spaceLeft -= imiInside.getStackableSize ();
		}
		if (spaceLeft < 0) {
			spaceLeft = 0;
		}

		return true;
	}


	public int getItemID () {
		return itemID;
	}


	public void setItemID (int itemID) {
		this.itemID = itemID;
	}


	public ArrayList<Item> getItemsInside () {
		return itemsInside;
	}


	/**
	 * Añade un item al container si o si, no chequea si cabe ni si es otro container ni nada
	 * 
	 * @param item
	 */
	public void addItem (Item item) {
		itemsInside.add (item);
		Item.addItem (item); // Para que sume 1 al número de items, ya que el item no existe realmente
		World.setTownValue (World.getTownValue () + PricesManager.getPrice (item));
		Item itemContainer = Item.getItemByID (getItemID ());
		if (itemContainer != null) {
			item.setCoordinates (itemContainer.getCoordinates ());
		}
		spaceLeft -= ItemManager.getItem (item.getIniHeader ()).getStackableSize ();
	}


	/**
	 * Indica si hay items que no deben ir ahí
	 * 
	 * @return
	 */
	private boolean hasWrongItems () {
		ItemManagerItem imi;
		for (int i = 0; i < getItemsInside ().size (); i++) {
			imi = ItemManager.getItem (getItemsInside ().get (i).getIniHeader ());
			if (!getType ().contains (imi.getIniHeader ())) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Elimina un item del container y lo devuelve
	 * 
	 * @param iID ID del item a devolver
	 * @return item del container o null si no lo encuentra
	 */
	public Item removeItem (int iID) {
		for (int i = 0; i < itemsInside.size (); i++) {
			if (itemsInside.get (i).getID () == iID) {
				Item item = itemsInside.remove (i);
				Item.removeItem (item); // Para que reste 1 al número de items, ya que el item no existe realmente
				World.setTownValue (World.getTownValue () - PricesManager.getPrice (item));
				setWrongItemsInside (hasWrongItems ());

				spaceLeft += ItemManager.getItem (item.getIniHeader ()).getStackableSize ();
				return item;
			}
		}

		return null;
	}


	/**
	 * Elimina un objeto militar del tipo pasado
	 * 
	 * @param iLocation
	 * @return
	 */
	public MilitaryItem removeMilitaryItem (int iLocation) {
		ItemManagerItem imi;
		for (int i = 0; i < itemsInside.size (); i++) {
			if (itemsInside.get (i) instanceof MilitaryItem) {
				imi = ItemManager.getItem (itemsInside.get (i).getIniHeader ());
				if (imi.getLocation () == iLocation) {
					Item item = itemsInside.remove (i);
					Item.removeItem (item); // Para que reste 1 al número de items, ya que el item no existe realmente
					World.setTownValue (World.getTownValue () - PricesManager.getPrice (item));
					setWrongItemsInside (hasWrongItems ());

					spaceLeft += ItemManager.getItem (item.getIniHeader ()).getStackableSize ();
					return (MilitaryItem) item;
				}
			}
		}

		return null;
	}


	/**
	 * Elimina un item del container y lo devuelve basándose en una lista de iniheaders
	 * 
	 * @param iID ID del item a devolver
	 * @return item del container o null si no lo encuentra
	 */
	public Item removeItemWithPrerequisites (ArrayList<String> alPrerequisites) {
		for (int i = 0; i < itemsInside.size (); i++) {
			if (alPrerequisites.contains (itemsInside.get (i).getIniHeader ())) {
				Item item = itemsInside.remove (i);
				Item.removeItem (item); // Para que reste 1 al número de items, ya que el item no existe realmente
				World.setTownValue (World.getTownValue () - PricesManager.getPrice (item));
				setWrongItemsInside (hasWrongItems ());

				spaceLeft += ItemManager.getItem (item.getIniHeader ()).getStackableSize ();
				return item;
			}
		}

		return null;
	}


	/**
	 * Elimina un item del container y lo devuelve basándose en una lista de iniheaders
	 * 
	 * @param iID ID del item a devolver
	 * @return item del container o null si no lo encuentra
	 */
	public Item removeItemWithPrerequisitesInts (ArrayList<int[]> alPrerequisites) {
		for (int i = 0; i < itemsInside.size (); i++) {
			if (UtilsIniHeaders.contains (alPrerequisites, itemsInside.get (i).getNumericIniHeader ()) != -1) {
				Item item = itemsInside.remove (i);
				Item.removeItem (item); // Para que reste 1 al número de items, ya que el item no existe realmente
				World.setTownValue (World.getTownValue () - PricesManager.getPrice (item));
				setWrongItemsInside (hasWrongItems ());

				spaceLeft += ItemManager.getItem (item.getIniHeader ()).getStackableSize ();
				return item;
			}
		}

		return null;
	}


	/**
	 * Elimina un item del container y lo devuelve basándose en una lista de iniheaders
	 * 
	 * @param iID ID del item a devolver
	 * @return item del container o null si no lo encuentra
	 */
	public Item removeItemWithPrerequisites (int[] aiPrerequisites) {
		for (int i = 0; i < itemsInside.size (); i++) {
			if (UtilsIniHeaders.contains (aiPrerequisites, itemsInside.get (i).getNumericIniHeader ())) {
				Item item = itemsInside.remove (i);
				Item.removeItem (item); // Para que reste 1 al número de items, ya que el item no existe realmente
				World.setTownValue (World.getTownValue () - PricesManager.getPrice (item));
				setWrongItemsInside (hasWrongItems ());

				spaceLeft += ItemManager.getItem (item.getIniHeader ()).getStackableSize ();
				return item;
			}
		}

		return null;
	}


	/**
	 * Elimina un item del container y lo devuelve basándose en un iniheader
	 * 
	 * @param iID ID del item a devolver
	 * @return item del container o null si no lo encuentra
	 */
	public Item removeItem (String iniHeader) {
		for (int i = 0; i < itemsInside.size (); i++) {
			if (iniHeader.equals (itemsInside.get (i).getIniHeader ())) {
				Item item = itemsInside.remove (i);
				Item.removeItem (item); // Para que reste 1 al número de items, ya que el item no existe realmente
				World.setTownValue (World.getTownValue () - PricesManager.getPrice (item));
				setWrongItemsInside (hasWrongItems ());

				spaceLeft += ItemManager.getItem (item.getIniHeader ()).getStackableSize ();
				return item;
			}
		}

		return null;
	}


	/**
	 * Elimina un item de comida del container y lo devuelve. Pillamos el de más value
	 * 
	 * @param iID ID del item a devolver
	 * @return item del container o null si no lo encuentra
	 */
	public Item removeFoodItem () {
		ItemManagerItem imi;
		int iMaxValue = -1;
		int iIndex = -1;
		for (int i = 0; i < itemsInside.size (); i++) {
			imi = ItemManager.getItem (itemsInside.get (i).getIniHeader ());
			if (imi.canBeEaten ()) {
				if (iIndex == -1) {
					iMaxValue = imi.getFoodValue ();
					iIndex = i;
				} else {
					if (imi.getFoodValue () > iMaxValue) {
						iMaxValue = imi.getFoodEatTime ();
						iIndex = i;
					}
				}
			}
		}

		if (iIndex != -1) {
			Item item = itemsInside.remove (iIndex);
			Item.removeItem (item); // Para que reste 1 al número de items, ya que el item no existe realmente
			World.setTownValue (World.getTownValue () - PricesManager.getPrice (item));
			setWrongItemsInside (hasWrongItems ());

			spaceLeft += ItemManager.getItem (item.getIniHeader ()).getStackableSize ();
			return item;
		}

		return null;
	}


	/**
	 * Elimina un item malo del container y lo devuelve
	 * 
	 * @return
	 */
	public Item removeBadItem () {
		ItemManagerItem imi;
		for (int i = 0; i < itemsInside.size (); i++) {
			imi = ItemManager.getItem (itemsInside.get (i).getIniHeader ());
			if (!getType ().contains (imi.getIniHeader ())) {
				Item item = itemsInside.remove (i);
				Item.removeItem (item); // Para que reste 1 al número de items, ya que el item no existe realmente
				World.setTownValue (World.getTownValue () - PricesManager.getPrice (item));
				setWrongItemsInside (hasWrongItems ());

				spaceLeft += ItemManager.getItem (item.getIniHeader ()).getStackableSize ();
				return item;
			}
		}

		setWrongItemsInside (false);
		return null;
	}


	/**
	 * Indica si el item pasado cabe en el container. Chequea que el item NO sea un container, que el item sea stackable y que los tipos coincidan
	 * 
	 * @param item
	 * @return
	 */
	public boolean itemAllowed (Item item) {
		ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
		if (imi.isContainer () || !imi.isStackable ()) {
			return false;
		}

		if (spaceLeft < imi.getStackableSize ()) {
			return false;
		}

		// Si llega aquí es que no es container y es stackable, miramos si cabe
		Item itemContainer = Item.getItemByID (itemID);
		if (itemContainer == null) {
			return false;
		}

		ItemManagerItem imiContainer = ItemManager.getItem (itemContainer.getIniHeader ());
		if (imiContainer.getType () == null) {
			return false;
		}

		// Type type = Types.getType (Type.getMainType (imiContainer.getType ()));
		// if (!type.contains (imi.getIniHeader ())) {
		// return false;
		// }
		// int iCurrentSize = 0;
		// ItemManagerItem imiInside;
		// for (int i = 0; i < itemsInside.size (); i++) {
		// imiInside = ItemManager.getItem (itemsInside.get (i).getIniHeader ());
		// iCurrentSize += imiInside.getStackableSize ();
		// }
		if (!getType ().contains (item.getIniHeader ())) {
			return false;
		}

		return true;
	}


	public boolean containsAny (ArrayList<String> alHeaders) {
		for (int i = 0; i < itemsInside.size (); i++) {
			if (alHeaders.contains (itemsInside.get (i).getIniHeader ())) {
				return true;
			}
		}

		return false;
	}


	public boolean containsAny (int[] aiHeaders) {
		for (int i = 0; i < itemsInside.size (); i++) {
			for (int h = 0; h < aiHeaders.length; h++) {
				if (aiHeaders[h] == itemsInside.get (i).getNumericIniHeader ()) {
					return true;
				}
			}
		}

		return false;
	}


	public boolean containsAnyFood () {
		ItemManagerItem imi;
		for (int i = 0; i < itemsInside.size (); i++) {
			imi = ItemManager.getItem (itemsInside.get (i).getIniHeader ());
			if (imi.canBeEaten ()) {
				return true;
			}
		}

		return false;
	}


	public boolean containsItem (int iItemID) {
		for (int i = 0; i < itemsInside.size (); i++) {
			if (itemsInside.get (i).getID () == iItemID) {
				return true;
			}
		}

		return false;
	}


	public ArrayList<String> getContentString () {
		ArrayList<String> alContent = new ArrayList<String> (itemsInside.size ());
		int[] aiNumber = new int [itemsInside.size ()];
		String sName;
		int iIndex;
		for (int i = 0; i < itemsInside.size (); i++) {
			if (itemsInside.get (i) instanceof MilitaryItem) {
				sName = ((MilitaryItem) itemsInside.get (i)).getExtendedTilename ();
			} else {
				sName = itemsInside.get (i).getTileName ();
			}
			iIndex = alContent.indexOf (sName);

			if (iIndex != -1) {
				aiNumber[iIndex]++;
			} else {
				alContent.add (sName);
				aiNumber[alContent.size () - 1] = 1;
			}
		}

		for (int i = 0; i < alContent.size (); i++) {
			if (aiNumber[i] > 1) {
				alContent.set (i, aiNumber[i] + "x " + alContent.get (i)); //$NON-NLS-1$
			}
		}
		return alContent;
	}


	public void setType (Type type) {
		this.type = type;
	}


	public Type getType () {
		return type;
	}


	public boolean isFull () {
		return spaceLeft <= 0;
	}


	/**
	 * Habilita todos los elementos del container. Si se le pasa un subtipo habilita sólo esos
	 */
	public void enableAll (String sSubType) {
		if (sSubType == null || sSubType.length () == 0) {
			enableAll ();
		} else {
			Type globalType = Types.getType (getType ().getID ());
			ItemManagerItem imi;
			for (int t = 0; t < globalType.getElements ().size (); t++) {
				imi = ItemManager.getItem (globalType.getElements ().get (t));
				if (imi.getType () != null && imi.getType ().equals (sSubType)) {
					if (!getType ().contains (globalType.getElements ().get (t))) {
						getType ().addElement (globalType.getElements ().get (t), globalType.getElementNames ().get (t));
					}
				}
			}

			setWrongItemsInside (hasWrongItems ());
		}
	}


	/**
	 * Habilita todos los elementos del container
	 */
	public void enableAll () {
		disableAll ();

		Type type = Types.getType (getType ().getID ());
		String sHeader;
		boolean bContainer;
		ItemManagerItem imi;
		for (int e = 0; e < type.getElements ().size (); e++) {
			sHeader = type.getElements ().get (e);
			imi = ItemManager.getItem (sHeader);
			bContainer = (imi != null && imi.isContainer ());

			if (!bContainer) {
				getType ().addElement (sHeader, type.getElementNames ().get (e));
			}
		}

		setWrongItemsInside (hasWrongItems ());
	}


	/**
	 * Inhabilita todos los elementos del container. Si se le pasa un subtipo elimina sólo esos
	 */
	public void disableAll (String sSubType) {
		if (sSubType == null || sSubType.length () == 0) {
			disableAll ();
		} else {
			Type globalType = Types.getType (getType ().getID ());
			ItemManagerItem imi;
			for (int t = 0; t < globalType.getElements ().size (); t++) {
				imi = ItemManager.getItem (globalType.getElements ().get (t));
				if (imi.getType () != null && imi.getType ().equals (sSubType)) {
					getType ().removeElement (globalType.getElements ().get (t));
				}
			}

			setWrongItemsInside (hasWrongItems ());
		}
	}


	/**
	 * Inhabilita todos los elementos del container
	 */
	public void disableAll () {
		getType ().removeElements ();

		// Marcamos el flag de wrongItemsInside si tiene algún item
		if (getItemsInside ().size () > 0) {
			setWrongItemsInside (true);
		}
	}


	public void enableItem (String sItemType) {
		if (sItemType != null) {
			ItemManagerItem imi = ItemManager.getItem (sItemType);
			if (imi != null) {
				getType ().addElement (sItemType, imi.getName ());
			}
		}

		setWrongItemsInside (hasWrongItems ());
	}


	public void disableItem (String sItemType) {
		if (sItemType != null) {
			getType ().removeElement (sItemType);
		}

		setWrongItemsInside (hasWrongItems ());
	}


	/**
	 * Fills a contextual menu with the container options
	 * 
	 * @param cell
	 * @param sm
	 */
	public void fillMenu (SmartMenu sm, String sItemName) {
		sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Container.2") + sItemName, null, CommandPanel.COMMAND_CONTAINER_MANAGE, Integer.toString (getItemID ()), null, null, Color.GREEN)); //$NON-NLS-1$

		// Information about lock/unlock status
		if (isLockedToCopy ()) {
			sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "(" + Messages.getString ("Stockpile.2") + ")", null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, "(" + Messages.getString ("Stockpile.9") + ")", null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}


	public static SmartMenu createContainerMenu (int iContainerID) {
		Container container = Game.getWorld ().getContainer (iContainerID);

		if (container == null || container.getType () == null) {
			return null;
		}

		String sContainerID = Integer.toString (container.getItemID ());
		SmartMenu smContainerMenu = new SmartMenu (SmartMenu.TYPE_MENU, Messages.getString ("Container.2"), null, null, sContainerID); //$NON-NLS-1$

		// Añadimos el enable todo/disable todo
		SmartMenu smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Container.3"), null, CommandPanel.COMMAND_CONTAINER_ENABLE_ALL, Integer.toString (iContainerID), null, null, Color.GREEN); //$NON-NLS-1$
		smAux.setIcon ("iconenableall"); //$NON-NLS-1$
		smContainerMenu.addItem (smAux);
		smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Container.4"), null, CommandPanel.COMMAND_CONTAINER_DISABLE_ALL, Integer.toString (iContainerID), null, null, Color.ORANGE); //$NON-NLS-1$
		smAux.setIcon ("icondisableall"); //$NON-NLS-1$
		smContainerMenu.addItem (smAux);

		// Añadimos 1 linea por cada objeto del tipo de container (para poner/quitar)
		Type fullType = Types.getType (container.getType ().getID ());

		ItemManagerItem imi;
		String sItemType;
		StockpileTempData elementsWithoutSubtype = new StockpileTempData ();
		ArrayList<String> alElementsWithSubtypeName = new ArrayList<String> ();
		ArrayList<StockpileTempData> alElementsWithSubtype = new ArrayList<StockpileTempData> ();

		// Parseamos todo
		for (int i = 0; i < fullType.getElements ().size (); i++) {
			sItemType = ItemManager.getItem (fullType.getElements ().get (i)).getType ();

			// Miramos si es subtipo
			int iIndex = sItemType.indexOf ('.');
			if (iIndex != -1) {
				// Tiene subtipo
				int iIndexSubtype = alElementsWithSubtypeName.indexOf (sItemType);
				if (iIndexSubtype != -1) {
					// Ya existente, metemos el objeto
					alElementsWithSubtype.get (iIndexSubtype).addElement (fullType.getElements ().get (i), container.getType ().contains (fullType.getElements ().get (i)));
				} else {
					// No existe, subtipo nuevo
					StockpileTempData sptd = new StockpileTempData ();
					sptd.addElement (fullType.getElements ().get (i), container.getType ().contains (fullType.getElements ().get (i)));

					alElementsWithSubtypeName.add (sItemType);
					alElementsWithSubtype.add (sptd);
				}
			} else {
				// No tiene
				elementsWithoutSubtype.addElement (fullType.getElements ().get (i), container.getType ().contains (fullType.getElements ().get (i)));
			}
		}

		// Todo parseado, creamos los menues
		String sItemName;
		for (int i = 0; i < alElementsWithSubtypeName.size (); i++) {
			SmartMenu smSubMenu = new SmartMenu (SmartMenu.TYPE_MENU, Type.getTypeName (alElementsWithSubtypeName.get (i)), smContainerMenu, null, sContainerID);
			if (Type.getIcon (alElementsWithSubtypeName.get (i)) != null) {
				smSubMenu.setIcon (Type.getIcon (alElementsWithSubtypeName.get (i)));
			}

			// Añadimos el enable todo/disable todo
			smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Container.3"), null, CommandPanel.COMMAND_CONTAINER_ENABLE_ALL, Integer.toString (iContainerID), alElementsWithSubtypeName.get (i), null, Color.GREEN); //$NON-NLS-1$
			smAux.setIcon ("iconenableall"); //$NON-NLS-1$
			smSubMenu.addItem (smAux);
			smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Container.4"), null, CommandPanel.COMMAND_CONTAINER_DISABLE_ALL, Integer.toString (iContainerID), alElementsWithSubtypeName.get (i), null, Color.ORANGE); //$NON-NLS-1$
			smAux.setIcon ("icondisableall"); //$NON-NLS-1$
			smSubMenu.addItem (smAux);

			ArrayList<String> alElements = alElementsWithSubtype.get (i).getAlElements ();
			ArrayList<Boolean> alElementsStatus = alElementsWithSubtype.get (i).getAlElementsStatus ();
			for (int j = 0; j < alElements.size (); j++) {
				imi = ItemManager.getItem (alElements.get (j));
				if (imi != null && !imi.isContainer () && imi.isStackable ()) {
					sItemName = imi.getName ();
					if (alElementsStatus.get (j).booleanValue ()) {
						smAux = new SmartMenu (SmartMenu.TYPE_ITEM, sItemName, null, CommandPanel.COMMAND_CONTAINER_DISABLE_ITEM, Integer.toString (iContainerID), alElements.get (j), null, Color.GREEN);
						smAux.setIcon (imi.getIniHeader ());
						smSubMenu.addItem (smAux);
					} else {
						smAux = new SmartMenu (SmartMenu.TYPE_ITEM, sItemName, null, CommandPanel.COMMAND_CONTAINER_ENABLE_ITEM, Integer.toString (iContainerID), alElements.get (j), null, Color.ORANGE);
						smAux.setIcon (imi.getIniHeader ());
						smSubMenu.addItem (smAux);
					}
				}
			}

			smAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Building.4"), smContainerMenu, CommandPanel.COMMAND_BACK, null); //$NON-NLS-1$
			smAux.setIcon ("ui_back"); //$NON-NLS-1$
			smSubMenu.addItem (smAux);

			smContainerMenu.addItem (smSubMenu);
		}

		// Ahora los items sin tipo
		for (int j = 0; j < elementsWithoutSubtype.getAlElements ().size (); j++) {
			imi = ItemManager.getItem (elementsWithoutSubtype.getAlElements ().get (j));
			if (imi != null && !imi.isContainer () && imi.isStackable ()) {
				sItemName = imi.getName ();
				if (elementsWithoutSubtype.getAlElementsStatus ().get (j).booleanValue ()) {
					smAux = new SmartMenu (SmartMenu.TYPE_ITEM, sItemName, null, CommandPanel.COMMAND_CONTAINER_DISABLE_ITEM, Integer.toString (iContainerID), elementsWithoutSubtype.getAlElements ().get (j), null, Color.GREEN);
					smAux.setIcon (imi.getIniHeader ());
					smContainerMenu.addItem (smAux);
				} else {
					smAux = new SmartMenu (SmartMenu.TYPE_ITEM, sItemName, null, CommandPanel.COMMAND_CONTAINER_ENABLE_ITEM, Integer.toString (iContainerID), elementsWithoutSubtype.getAlElements ().get (j), null, Color.ORANGE);
					smAux.setIcon (imi.getIniHeader ());
					smContainerMenu.addItem (smAux);
				}
			}
		}

		return smContainerMenu;
	}


	/**
	 * Se recorre todo el menú y setea el valor de los items a enabled o disabled según la configuración del contenedor
	 * 
	 * @param iContainerID
	 * @param menuPile
	 * @return true si todo está ok
	 */
	public static boolean regenerateContainerPanelMenu (int iContainerID, SmartMenu menuPile) {
		if (menuPile == null) {
			return false;
		}

		Container container = Game.getWorld ().getContainer (iContainerID);

		if (container == null || container.getType () == null) {
			return false;
		}

		// Tenemos el container, ahora recorremos todo recursivamente para enablear o disablear
		regenerateContainerPanelMenu (container, menuPile);

		return true;
	}


	private static void regenerateContainerPanelMenu (Container container, SmartMenu menuPile) {
		SmartMenu smAux;
		for (int i = 0; i < menuPile.getItems ().size (); i++) {
			smAux = menuPile.getItems ().get (i);
			if (smAux.getType () == SmartMenu.TYPE_MENU) {
				regenerateContainerPanelMenu (container, smAux);
			} else {
				// Item
				if (smAux.getCommand ().equals (CommandPanel.COMMAND_CONTAINER_ENABLE_ITEM) || smAux.getCommand ().equals (CommandPanel.COMMAND_CONTAINER_DISABLE_ITEM)) {
					// Toca mirar si va a ser enabled o disabled
					if (container.getType ().contains (smAux.getParameter2 ())) {
						// Existe, pués lo ponemos para disable
						smAux.setCommand (CommandPanel.COMMAND_CONTAINER_DISABLE_ITEM);
					} else {
						// No existe, lo ponemos para enable
						smAux.setCommand (CommandPanel.COMMAND_CONTAINER_ENABLE_ITEM);
					}

				}
			}
		}
	}


	public void setWrongItemsInside (boolean wrongItemsInside) {
		this.wrongItemsInside = wrongItemsInside;
	}


	public boolean isWrongItemsInside () {
		return wrongItemsInside;
	}


	public void setLockedToCopy (boolean lockedToCopy) {
		this.lockedToCopy = lockedToCopy;
	}


	public boolean isLockedToCopy () {
		return lockedToCopy;
	}


	/**
	 * Locks/unlocks all the containers configurations
	 */
	public static void lockUnlockAllConfigurations (boolean bLock) {
		ArrayList<Container> alContainers = Game.getWorld ().getContainers ();
		Container container;

		for (int i = 0; i < alContainers.size (); i++) {
			container = alContainers.get (i);
			container.setLockedToCopy (bLock);
		}
	}


	@SuppressWarnings("unchecked")
	public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
		itemID = in.readInt ();
		itemsInside = (ArrayList<Item>) in.readObject ();
		type = (Type) in.readObject ();
		wrongItemsInside = in.readBoolean ();

		if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V14e) {
			lockedToCopy = in.readBoolean ();
		} else {
			lockedToCopy = false;
		}
	}


	public void writeExternal (ObjectOutput out) throws IOException {
		out.writeInt (itemID);
		out.writeObject (itemsInside);
		out.writeObject (type);
		out.writeBoolean (wrongItemsInside);
		out.writeBoolean (lockedToCopy);
	}
}
