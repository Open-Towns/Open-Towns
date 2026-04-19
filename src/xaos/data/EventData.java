package xaos.data;

import java.awt.Point;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;

import xaos.actions.Action;
import xaos.actions.ActionManager;
import xaos.actions.QueueData;
import xaos.effects.EffectManager;
import xaos.effects.EffectManagerItem;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.generator.BezierData;
import xaos.generator.ChangeData;
import xaos.generator.HeightSeedData;
import xaos.generator.MapGeneratorItem;
import xaos.generator.ParentMapData;
import xaos.generator.SeedData;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MiniMapPanel;
import xaos.tiles.Cell;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.terrain.Terrain;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsIniHeaders;

public class EventData implements Externalizable {

    private static final long serialVersionUID = 8911321568004507866L;

    private String eventID;

    private int order;
    private int turns;
    private int eventCooldown;
    private int waitPCT;
    private int walkSpeedPCT;
    private int fxRunningTurns;

    private static HashMap<String, ArrayList<Point3D>> hmSeedsIDs = new HashMap<String, ArrayList<Point3D>>();

    public EventData() {
    }

    private void launch() {
        // Cosas globales (el evento se acaba de activar, reseteamos las cosas globales)
        Game.getWorld().checkGlobalEvents();

        // Ejecutamos las cosas directas
        EventManagerItem emi = EventManager.getItem(getEventID());
        if (emi != null) {
            // Sieges
            if (emi.isSiege() || emi.isSiegeUnderground()) {
                Game.getWorld().checkSiege(emi);
            }

            // Happiness
            if (emi.getHappinessPCT() != null) {
                int iPCT = Utils.launchDice(emi.getHappinessPCT());
                if (iPCT < 0) {
                    iPCT = 0;
                }

                LivingEntity le;

                // Townies
                for (int h = 0; h < World.getCitizenIDs().size(); h++) {
                    le = World.getLivingEntityByID(World.getCitizenIDs().get((h)));
                    if (le != null) {
                        Citizen cit = (Citizen) le;
                        cit.getCitizenData().setHappiness((cit.getCitizenData().getHappiness() * iPCT) / 100);
                        if (cit.getCitizenData().getHappiness() > 100) {
                            cit.getCitizenData().setHappiness(100);
                        }
                    }
                }

                // Soldados
                for (int h = 0; h < World.getSoldierIDs().size(); h++) {
                    le = World.getLivingEntityByID(World.getSoldierIDs().get((h)));
                    if (le != null && Utils.getRandomBetween(1, 100) <= iPCT) {
                        Citizen cit = (Citizen) le;
                        cit.getCitizenData().setHappiness((cit.getCitizenData().getHappiness() * iPCT) / 100);
                        if (cit.getCitizenData().getHappiness() > 100) {
                            cit.getCitizenData().setHappiness(100);
                        }
                    }
                }
            }

            // Targets (effects)
            launchEffects(emi, emi.getEffects(), null);

            // Items
            checkAllItemsMaxAgePCTs(emi, false);
            checkAllItemsDeletePCTs(emi);
            checkAllItemsSpawnLiving(emi);

            // Miramos si hay que hacer sonar un fichero de audio
            if (emi.getFxAfterCooldown() != null) {
                UtilsAL.play(emi.getFxAfterCooldown());
            }

            // Injects
            if (emi.getInjectActions() != null && emi.getInjectActions().size() > 0) {
                for (int i = 0; i < emi.getInjectActions().size(); i++) {
                    Action action = new Action(emi.getInjectActions().get(i));
                    action.setQueue(ActionManager.getItem(emi.getInjectActions().get(i)).getQueue());
                    action.setQueueData(new QueueData());
                    Game.getWorld().getTaskManager().addCustomAction(action, false, false);
                }
            }

            // Generators
            if (emi.getUseFileIDs() != null && emi.getUseFileIDs().size() > 0) {
                //hmSeedsIDs.clear ();

                for (int i = 0; i < emi.getUseFileIDs().size(); i++) {
                    ParentMapData pmd = emi.getParentMapData(emi.getUseFileIDs().get(i));

                    if (pmd != null) {
                        if (pmd instanceof BezierData) {
                            launchBezierData((BezierData) pmd);
                        } else if (pmd instanceof SeedData) {
                            launchSeedData((SeedData) pmd);
                        } else if (pmd instanceof HeightSeedData) {
                            launchHeightSeedData((HeightSeedData) pmd);
                        } else if (pmd instanceof ChangeData) {
                            launchChangeData((ChangeData) pmd);
                        }
                    }
                }

                // Diggeds, mineds, blockys
                Cell.generateDiggedsMinedsAndBlockys(World.getCells());

                Cell.setAllZoneIDs();

                // Should paint unders
                Cell.generatePaintUnder(World.getCells());

                Terrain.changeSlopes(World.getCells());

                // Shadows
                Cell.generateShadows();

                // Light
                Cell.generateAllLights();

                // Open cell
                Cell.generateAllOpen();

                // Accesing points para las tareas de MINE pueden haber cambiado
                Game.getWorld().getTaskManager().setReCheckMinePlaces(true);
            }
        }
    }

    /**
     * Aplica la lista de efectos a quien toque
     *
     * @param emi
     * @param effectsList afterEat, afterSleep, effects, ...
     * @param singleLiving Si se pasa una living sólo intenta aplicarlo a ella,
     * no a todos
     */
    public void launchEffects(EventManagerItem emi, ArrayList<String> effectsList, LivingEntity singleLiving) {
        if (emi.getTargets() != null && emi.getTargets().size() > 0) {
            // Tiene targets, vamos a ver si aplicamos efectos de 1 sólo uso (<effects>)
            if (effectsList != null && effectsList.size() > 0) {
                ArrayList<String> alTargets = emi.getTargets();
                ArrayList<String> alPCTs = emi.getTargetsPCT();

                // Por si acaso han cambiado el xml, comprobamos tamaños y esas cosas
                if (alTargets != null && alPCTs != null && alTargets.size() == alPCTs.size()) {
					// Go, go, go

                    // Miramos que no sea una single living, para aplicar ya de raiz el openCell
                    if (singleLiving != null && emi.isTargetsOpenCell() && !World.getCell(singleLiving.getCoordinates()).isOpen()) {
                        return;
                    }

                    if (emi.getTargetsRandomCell() == null || emi.getTargetsRandomCell().length() == 0) {
						// No hay random cell, recorremos effects y targets normalmente
                        // Recorremos todos los efectos y los vamos metiendo
                        EffectManagerItem efmi;
                        foreffects:
                        for (int ef = 0; ef < effectsList.size(); ef++) {
                            efmi = EffectManager.getItem(effectsList.get(ef));
                            if (efmi != null) {
                                int iPCT;
                                int iTargetType;

                                String sTarget;
                                LivingEntity le;
                                for (int i = 0; i < alTargets.size(); i++) {
                                    sTarget = alTargets.get(i);
                                    iPCT = Utils.launchDice(alPCTs.get(i));
                                    if (iPCT <= 0) {
                                        continue;
                                    }
                                    if (iPCT > 100) {
                                        iPCT = 100;
                                    }

                                    if (singleLiving != null) {
                                        LivingEntityManagerItem lemi = LivingEntityManager.getItem(singleLiving.getIniHeader());
                                        if (lemi != null) {
                                            if (Utils.getRandomBetween(1, 100) <= iPCT && emi.getTargetsHateData().isHate(singleLiving)) {
                                                singleLiving.addEffect(efmi, true);
                                            }
                                        }
                                        continue foreffects;
                                    }

                                    if (sTarget.equals(LivingEntityManagerItem.TYPE_HERO)) {
                                        iTargetType = LivingEntity.TYPE_HERO;
                                    } else if (sTarget.equals(LivingEntityManagerItem.TYPE_CITIZEN)) {
                                        iTargetType = LivingEntity.TYPE_CITIZEN;
                                    } else if (sTarget.equals(LivingEntityManagerItem.TYPE_ALLY)) {
                                        iTargetType = LivingEntity.TYPE_ALLY;
                                    } else if (sTarget.equals(LivingEntityManagerItem.TYPE_FRIENDLY)) {
                                        iTargetType = LivingEntity.TYPE_FRIENDLY;
                                    } else if (sTarget.equals(LivingEntityManagerItem.TYPE_ENEMY)) {
                                        iTargetType = LivingEntity.TYPE_ENEMY;
                                    } else {
                                        iTargetType = LivingEntity.TYPE_UNKNOWN;
                                    }

                                    if (iTargetType == LivingEntity.TYPE_HERO) {
                                        // Héroes
                                        for (int h = 0; h < World.getHeroIDs().size(); h++) {
                                            le = World.getLivingEntityByID(World.getHeroIDs().get((h)));
                                            if (le != null && Utils.getRandomBetween(1, 100) <= iPCT) {
                                                // Miramos el openCell
                                                if (emi.isTargetsOpenCell() && !World.getCell(le.getCoordinates()).isOpen()) {
                                                    continue;
                                                }
                                                le.addEffect(efmi, true);
                                            }
                                        }
                                    } else if (iTargetType == LivingEntity.TYPE_CITIZEN) {
                                        // Townies
                                        for (int h = 0; h < World.getCitizenIDs().size(); h++) {
                                            le = World.getLivingEntityByID(World.getCitizenIDs().get((h)));
                                            if (le != null && Utils.getRandomBetween(1, 100) <= iPCT) {
                                                // Miramos el openCell
                                                if (emi.isTargetsOpenCell() && !World.getCell(le.getCoordinates()).isOpen()) {
                                                    continue;
                                                }
                                                le.addEffect(efmi, true);
                                            }
                                        }
                                        // Soldados
                                        for (int h = 0; h < World.getSoldierIDs().size(); h++) {
                                            le = World.getLivingEntityByID(World.getSoldierIDs().get((h)));
                                            if (le != null && Utils.getRandomBetween(1, 100) <= iPCT) {
                                                // Miramos el openCell
                                                if (emi.isTargetsOpenCell() && !World.getCell(le.getCoordinates()).isOpen()) {
                                                    continue;
                                                }
                                                le.addEffect(efmi, true);
                                            }
                                        }
                                    } else if (iTargetType == LivingEntity.TYPE_ALLY || iTargetType == LivingEntity.TYPE_FRIENDLY || iTargetType == LivingEntity.TYPE_ENEMY) {
                                        HashMap<Integer, LivingEntity> hmLivings = World.getLivings(true);
                                        if (hmLivings != null) {
                                            Integer[] aLivings = hmLivings.keySet().toArray(new Integer[0]);
                                            LivingEntityManagerItem lemi;
                                            for (int h = 0; h < aLivings.length; h++) {
                                                le = hmLivings.get(aLivings[h]);
                                                if (le != null && Utils.getRandomBetween(1, 100) <= iPCT) {
                                                    // Miramos el openCell
                                                    if (emi.isTargetsOpenCell() && !World.getCell(le.getCoordinates()).isOpen()) {
                                                        continue;
                                                    }

                                                    lemi = LivingEntityManager.getItem(le.getIniHeader());
                                                    if (lemi != null && lemi.getType() != iTargetType) {
                                                        // BAM !!
                                                        le.addEffect(efmi, true);
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Target directo
                                        HashMap<Integer, LivingEntity> hmLivings = World.getLivings(true);
                                        if (hmLivings != null) {
                                            Integer[] aLivings = hmLivings.keySet().toArray(new Integer[0]);
                                            LivingEntityManagerItem lemi;
                                            for (int h = 0; h < aLivings.length; h++) {
                                                le = hmLivings.get(aLivings[h]);
                                                if (le != null && Utils.getRandomBetween(1, 100) <= iPCT) {
                                                    // Miramos el openCell
                                                    if (emi.isTargetsOpenCell() && !World.getCell(le.getCoordinates()).isOpen()) {
                                                        continue;
                                                    }

                                                    lemi = LivingEntityManager.getItem(le.getIniHeader());
                                                    if (lemi != null && sTarget.equals(lemi.getIniHeader())) {
                                                        // BAM !!
                                                        le.addEffect(efmi, true);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Hay random cell, allá vamos!
                        int iNumCells = Utils.launchDice(emi.getTargetsRandomCell());
                        Cell cell;
                        while (iNumCells > 0) {
                            // Pillamos una cell a random
                            int x = Utils.getRandomBetween(0, World.MAP_WIDTH - 1);
                            int y = Utils.getRandomBetween(0, World.MAP_HEIGHT - 1);
                            int z = Utils.getRandomBetween(0, World.MAP_DEPTH - 1);

                            cell = World.getCell(x, y, z);
                            if (cell.isDiscovered() && cell.getLivings() != null && cell.getLivings().size() > 0) {
                                // Miramos si la celda tiene que ser open
                                if (emi.isTargetsOpenCell() && !cell.isOpen()) {
                                    continue;
                                }
								// Cell con livings
                                // Por cada livings que esté en el target le meteremos todos los efectos
                                LivingEntityManagerItem lemi;
                                LivingEntity le;
                                for (int l = 0; l < cell.getLivings().size(); l++) {
                                    le = cell.getLivings().get(l);
                                    lemi = LivingEntityManager.getItem(le.getIniHeader());
                                    if (lemi != null) {
                                        if (emi.getTargetsHateData().isHate(le)) {
                                            // Miramos si es un target válido
                                            int iTargetIndex = -1;
                                            int iType = lemi.getType();
                                            for (int t = 0; t < alTargets.size(); t++) {
                                                if (iType == LivingEntity.TYPE_HERO && alTargets.get(t).equals(LivingEntityManagerItem.TYPE_HERO)) {
                                                    iTargetIndex = t;
                                                    break;
                                                } else if (iType == LivingEntity.TYPE_CITIZEN && alTargets.get(t).equals(LivingEntityManagerItem.TYPE_CITIZEN)) {
                                                    iTargetIndex = t;
                                                    break;
                                                } else if (iType == LivingEntity.TYPE_ALLY && alTargets.get(t).equals(LivingEntityManagerItem.TYPE_ALLY)) {
                                                    iTargetIndex = t;
                                                    break;
                                                } else if (iType == LivingEntity.TYPE_FRIENDLY && alTargets.get(t).equals(LivingEntityManagerItem.TYPE_FRIENDLY)) {
                                                    iTargetIndex = t;
                                                    break;
                                                } else if (iType == LivingEntity.TYPE_ENEMY && alTargets.get(t).equals(LivingEntityManagerItem.TYPE_ENEMY)) {
                                                    iTargetIndex = t;
                                                    break;
                                                } else {
                                                    // Direct living
                                                    LivingEntityManagerItem lemiTarget = LivingEntityManager.getItem(alTargets.get(t));
                                                    if (lemiTarget != null && lemiTarget.getType() == iType) {
                                                        iTargetIndex = t;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (iTargetIndex != -1) {
                                                // Lanzamos el dado de PCT
                                                if (Utils.getRandomBetween(1, 100) <= Utils.launchDice(alPCTs.get(iTargetIndex))) {
                                                    // BAM, efectos
                                                    EffectManagerItem efmi;
                                                    for (int ef = 0; ef < effectsList.size(); ef++) {
                                                        efmi = EffectManager.getItem(effectsList.get(ef));
                                                        if (efmi != null) {
                                                            le.addEffect(efmi, true);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            iNumCells--;
                        }
                    }
                }
            }
        }
    }

    public boolean nextTurn() {
        // Cooldown
        if (getEventCooldown() >= 0) {
            setEventCooldown(getEventCooldown() - 1);

            if (getEventCooldown() <= 0) {
                launch();
                setEventCooldown(-1);
            }
//		} else {
            // Menor que 0, ya no hay que hacer nada, esperar a que el evento se acabe (turns)
        }

        // Audio
        if (getEventCooldown() == -1) {
            if (getFxRunningTurns() > 0) {
                setFxRunningTurns(getFxRunningTurns() - 1);

                if (getFxRunningTurns() == 0) {
                    EventManagerItem emi = EventManager.getItem(getEventID());
                    if (emi != null && emi.getFxRunning() != null) {
                        UtilsAL.play(emi.getFxRunning());
                    }

                    setFxRunningTurns(emi.getFxRunningTurns());
                }
            }
        }

        // Turns
        if (getTurns() > 0) {
            setTurns(getTurns() - 1);
        }

        if (turns <= 0) {
            // Se acabó
            return true;
        }

        return false;
    }

    /**
     * Changes all the ages and maxAges of the specified items on the event
     *
     * @param emi
     * @param bRestore Restoring items to the original state
     */
    public void checkAllItemsMaxAgePCTs(EventManagerItem emi, boolean bRestore) {
        if (emi.getItems() != null && emi.getItemsMaxAgePCT() != null && emi.getItems().size() > 0 && emi.getItems().size() == emi.getItemsMaxAgePCT().size()) {
            // Obtenemos todos los items y le aplicamos lo que toque

            String sIniHeader;
            ItemManagerItem imi;
            for (int i = 0; i < emi.getItems().size(); i++) {
                sIniHeader = emi.getItems().get(i);

                if (Item.getNumItemsTotal(sIniHeader, World.MAP_DEPTH - 1) > 0) {
                    imi = ItemManager.getItem(sIniHeader);
                    if (imi != null) {
                        int iPCT = Utils.launchDice(emi.getItemsMaxAgePCT().get(i));
                        if (iPCT < 1) {
                            iPCT = 1;
                        }

                        // Buscamos los items de este tipo
                        ArrayList<Integer> alItems = Item.getMapItemsLocked().get(UtilsIniHeaders.getIntIniHeader(sIniHeader));
                        if (alItems != null) {
                            Item item;
                            for (int it = 0; it < alItems.size(); it++) {
                                item = Item.getItemByID(alItems.get(it));

                                if (item != null) {
									// maxAgePCT
                                    // Le aplicamos el maxAgePCT al age y al maxAge
                                    if (bRestore) {
                                        item.setAge((item.getAge() * 100) / iPCT);
                                        item.setMaxAge((item.getMaxAge() * 100) / iPCT);
                                    } else {
                                        item.setAge((item.getAge() * iPCT) / 100);
                                        item.setMaxAge((item.getMaxAge() * iPCT) / 100);
                                    }
                                }
                            }
                        }
                        alItems = Item.getMapItems().get(UtilsIniHeaders.getIntIniHeader(sIniHeader));
                        if (alItems != null) {
                            Item item;
                            for (int it = 0; it < alItems.size(); it++) {
                                item = Item.getItemByID(alItems.get(it));

                                if (item != null) {
									// maxAgePCT
                                    // Le aplicamos el maxAgePCT al age y al maxAge
                                    if (bRestore) {
                                        item.setAge((item.getAge() * 100) / iPCT);
                                        item.setMaxAge((item.getMaxAge() * 100) / iPCT);
                                    } else {
                                        item.setAge((item.getAge() * iPCT) / 100);
                                        item.setMaxAge((item.getMaxAge() * iPCT) / 100);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Deletes the items that hit the PCT
     *
     * @param emi
     */
    public void checkAllItemsDeletePCTs(EventManagerItem emi) {
        if (emi.getItems() != null && emi.getItemsDeletePCT() != null && emi.getItems().size() > 0 && emi.getItems().size() == emi.getItemsDeletePCT().size()) {
            // Obtenemos todos los items

            String sIniHeader;
            ItemManagerItem imi;
            for (int i = 0; i < emi.getItems().size(); i++) {
                sIniHeader = emi.getItems().get(i);

                if (Item.getNumItemsTotal(sIniHeader, World.MAP_DEPTH - 1) > 0) {
                    imi = ItemManager.getItem(sIniHeader);
                    if (imi != null) {
                        int iPCT = Utils.launchDice(emi.getItemsDeletePCT().get(i));
                        if (iPCT < 1) {
                            iPCT = 1;
                        }

                        // Buscamos los items de este tipo
                        ArrayList<Integer> alItems = Item.getMapItemsLocked().get(UtilsIniHeaders.getIntIniHeader(sIniHeader));
                        if (alItems != null) {
                            Item item;
                            for (int it = 0; it < alItems.size(); it++) {
                                if (Utils.launchDice(1, 100) < iPCT) {
                                    item = Item.getItemByID(alItems.get(it));

                                    if (item != null) {
                                        // deletePCT
                                        item.delete();
                                    }
                                }
                            }
                        }
                        alItems = Item.getMapItems().get(UtilsIniHeaders.getIntIniHeader(sIniHeader));
                        if (alItems != null) {
                            Item item;
                            for (int it = 0; it < alItems.size(); it++) {
                                if (Utils.launchDice(1, 100) < iPCT) {
                                    item = Item.getItemByID(alItems.get(it));

                                    if (item != null) {
                                        // deletePCT
                                        item.delete();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Spawn livings from items
     *
     * @param emi
     */
    public void checkAllItemsSpawnLiving(EventManagerItem emi) {
        if (emi.getItems() != null && emi.getItemsSpawnLiving() != null && emi.getItemsSpawnLivingSize() != null && emi.getItems().size() > 0 && emi.getItems().size() == emi.getItemsSpawnLiving().size() && emi.getItemsSpawnLiving().size() == emi.getItemsSpawnLivingSize().size()) {
            // Obtenemos todos los items

            String sIniHeader;
            ItemManagerItem imi;
            for (int i = 0; i < emi.getItems().size(); i++) {
                sIniHeader = emi.getItems().get(i);

                if (Item.getNumItemsTotal(sIniHeader, World.MAP_DEPTH - 1) > 0) {
                    imi = ItemManager.getItem(sIniHeader);
                    if (imi != null) {
                        int iSize = Utils.launchDice(emi.getItemsSpawnLivingSize().get(i));
                        if (iSize < 0) {
                            iSize = 0;
                        }
                        String sLivingID = emi.getItemsSpawnLiving().get(i);
                        LivingEntityManagerItem lemi = LivingEntityManager.getItem(sLivingID);
                        if (lemi != null) {
                            // Buscamos los items de este tipo
                            ArrayList<Integer> alItems = Item.getMapItemsLocked().get(UtilsIniHeaders.getIntIniHeader(sIniHeader));
                            if (alItems != null) {
                                Item item;
                                for (int it = 0; it < alItems.size(); it++) {
                                    item = Item.getItemByID(alItems.get(it));

                                    if (item != null) {
                                        // Spawn
                                        for (int n = 0; n < iSize; n++) {
                                            World.addNewLiving(sLivingID, lemi.getType(), true, item.getCoordinates().x, item.getCoordinates().y, item.getCoordinates().z, true);
                                        }
                                    }
                                }
                            }
                            alItems = Item.getMapItems().get(UtilsIniHeaders.getIntIniHeader(sIniHeader));
                            if (alItems != null) {
                                Item item;
                                for (int it = 0; it < alItems.size(); it++) {
                                    item = Item.getItemByID(alItems.get(it));

                                    if (item != null) {
                                        // Spawn
                                        for (int n = 0; n < iSize; n++) {
                                            World.addNewLiving(sLivingID, lemi.getType(), true, item.getCoordinates().x, item.getCoordinates().y, item.getCoordinates().z, true);
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

    /**
     * Adds the afterEvents of the eventID parameter
     *
     * @param sEventID
     */
    public void addAfterEvents(String sEventID) {
        EventManagerItem emi = EventManager.getItem(sEventID);
        if (emi != null && emi.getAfterEvents() != null && emi.getAfterEvents().size() > 0) {
            EventManagerItem emiAfter;
            for (int i = 0; i < emi.getAfterEvents().size(); i++) {
                emiAfter = EventManager.getItem(emi.getAfterEvents().get(i));
                if (emiAfter != null) {
                    Game.getWorld().addEvent(emiAfter);
                }
            }
        }
    }

    private void launchBezierData(BezierData bd) {
        if (bd == null) {
            return;
        }

        // Comprobamos que tenemos todos los datos
        if (bd.type == null || bd.type.trim().length() == 0) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MapGenerator.18"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
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
            Log.log(Log.LEVEL_ERROR, Messages.getString("MapGenerator.20"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        if ((bd.level + bd.depth) > World.MAP_DEPTH) {
            Log.log(Log.LEVEL_ERROR, "Bezier. (level + depth) > MAX_DEPTH", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        if (bd.point1xmin == -1 || bd.point1xmax == -1 || bd.point1ymin == -1 || bd.point1ymax == -1 || bd.point2xmin == -1 || bd.point2xmax == -1 || bd.point2ymin == -1 || bd.point2ymax == -1 || bd.controlpoint1xmin == -1 || bd.controlpoint1xmax == -1 || bd.controlpoint1ymin == -1 || bd.controlpoint1ymax == -1 || bd.controlpoint2xmin == -1 || bd.controlpoint2xmax == -1 || bd.controlpoint2ymin == -1 || bd.controlpoint2ymax == -1) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MapGenerator.26"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Todo ok, seguimos
        boolean bBezierPintado = false;
        while (!bBezierPintado) {
            Point pointPuntoInicial = new Point(Utils.getRandomBetween(bd.point1xmin, bd.point1xmax), Utils.getRandomBetween(bd.point1ymin, bd.point1ymax));
            Point pointPuntoControlA = new Point(Utils.getRandomBetween(bd.controlpoint1xmin, bd.controlpoint1xmax), Utils.getRandomBetween(bd.controlpoint1ymin, bd.controlpoint1ymax));
            Point pointPuntoControlB = new Point(Utils.getRandomBetween(bd.controlpoint2xmin, bd.controlpoint2xmax), Utils.getRandomBetween(bd.controlpoint2ymin, bd.controlpoint2ymax));
            Point pointPuntoFinal = new Point(Utils.getRandomBetween(bd.point2xmin, bd.point2xmax), Utils.getRandomBetween(bd.point2ymin, bd.point2ymax));
            int iGrosor = Utils.launchDice(bd.wide);
            if (iGrosor < 1) {
                iGrosor = 1;
            }

            // Pinto la linea
            int iSpecialType = MapGeneratorItem.getSpecialInt(bd.type);
            int iTerrainID = 0;
            if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE) {
                iTerrainID = TerrainManager.getItem(bd.type).getTerrainID();
            }
            Point pointPuntoBezier;
            for (int i = 0; i < ((World.MAP_WIDTH + World.MAP_HEIGHT) * 10); i++) {
                pointPuntoBezier = Utils.getBezierPoint(pointPuntoInicial, pointPuntoFinal, pointPuntoControlA, pointPuntoControlB, (((double) i) / (double) ((World.MAP_WIDTH + World.MAP_HEIGHT) * 10)));
                for (int x = 0; x < iGrosor; x++) {
                    for (int y = 0; y < iGrosor; y++) {
                        if (Utils.isValidPoint(pointPuntoBezier.x + x, pointPuntoBezier.y + y, World.MAP_WIDTH, World.MAP_HEIGHT)) {
                            // Metemos el tipo (o special type)
                            if (iSpecialType != MapGeneratorItem.SPECIAL_INT_NONE) {
                                for (int d = 0; d < bd.depth; d++) {
                                    generateTerrainSpecial(iSpecialType, pointPuntoBezier.x + x, pointPuntoBezier.y + y, bd.level + d);
                                }
                            } else {
                                for (int d = 0; d < bd.depth; d++) {
                                    generateTerrain(iTerrainID, pointPuntoBezier.x + x, pointPuntoBezier.y + y, bd.level + d, false);
                                }
                            }
                            bBezierPintado = true;
                        }
                    }
                }
            }
        }
    }

    private void generateTerrain(int iTerrainID, int x, int y, int z, boolean bJustChange) {
        Cell cell = World.getCell(x, y, z);

        // Borramos cosas (Como si fueran fluidos)
        World.deleteCellAll(cell, true);

        if (z > 0) {
            World.deleteCellAll(World.getCell(x, y, z - 1), true);
        }

        TerrainManagerItem tmi = TerrainManager.getItemByID(iTerrainID);

		// Eliminamos fluidos en caso de no ser AIR
        // La des-minamos en caso de no ser AIR, minamos en caso de air
        if (iTerrainID != TerrainManagerItem.TERRAIN_AIR_ID) {
            if (!bJustChange) {
                cell.getTerrain().setFluidType(Terrain.FLUIDS_NONE);
                cell.getTerrain().setFluidCount(0);

                cell.getTerrain().setMineTurns(tmi.getMineTurns());
                cell.setMined(tmi.getMineTurns() <= 0);
                if (z > 0) {
                    World.getCell(x, y, z - 1).setDigged(tmi.getMineTurns() <= 0);
                }
            }
            cell.setBlocky(tmi.isBlocky());
        } else {
            cell.getTerrain().setMineTurns(0);
            cell.setMined(true);
            if (z > 0) {
                World.getCell(x, y, z - 1).setDigged(true);
            }
            cell.setBlocky(false);
        }

        // Cambiamos el terreno
        if (iTerrainID != TerrainManagerItem.TERRAIN_AIR_ID) {
            int iAdd = cell.getTerrain().getTerrainTileID() - (cell.getTerrain().getTerrainID() * TerrainManager.SLOPES_INIHEADER.length);
            cell.getTerrain().setTerrainID(tmi.getTerrainID());
            cell.getTerrain().setTerrainTileID((tmi.getTerrainID() * TerrainManager.SLOPES_INIHEADER.length) + iAdd);
        } else {
            cell.getTerrain().setTerrainID(TerrainManagerItem.TERRAIN_AIR_ID);
            cell.getTerrain().setTerrainTileID(TerrainManagerItem.TERRAIN_AIR_ID * TerrainManager.SLOPES_INIHEADER.length);
        }

        generateCellChanges(cell, (short) x, (short) y, (short) z);
    }

    private void generateTerrainSpecial(int iSpecialType, int x, int y, int z) {
        Cell cell = World.getCell(x, y, z);

		// Los special son simplemente fluidos, minamos la celda y metemos el fluido que toque
        // Borramos cosas (fluidos)
        World.deleteCellAll(cell, true);

        if (z > 0) {
            World.deleteCellAll(World.getCell(x, y, z - 1), true);
        }

		// Eliminamos fluidos en caso de no ser AIR
        // La des-minamos en caso de no ser AIR, minamos en caso de air
        cell.getTerrain().setMineTurns(0);
        cell.setMined(true);
        if (z > 0) {
            World.getCell(x, y, z - 1).setDigged(true);
        }
        if (cell.getTerrain().getMineTurns() > 0) {
            cell.getTerrain().setMineTurns(0);
        }

        if (iSpecialType == MapGeneratorItem.SPECIAL_INT_WATER_1) {
            cell.getTerrain().setFluidType(Terrain.FLUIDS_WATER);
            cell.getTerrain().setFluidCount(1);
        } else if (iSpecialType == MapGeneratorItem.SPECIAL_INT_WATER) {
            cell.getTerrain().setFluidType(Terrain.FLUIDS_WATER);
            cell.getTerrain().setFluidCount(Terrain.FLUIDS_COUNT_MAX);
        } else if (iSpecialType == MapGeneratorItem.SPECIAL_INT_WATER_INF) {
            cell.getTerrain().setFluidType(Terrain.FLUIDS_WATER);
            cell.getTerrain().setFluidCount(Terrain.FLUIDS_COUNT_INFINITE);
        } else if (iSpecialType == MapGeneratorItem.SPECIAL_INT_LAVA_1) {
            cell.getTerrain().setFluidType(Terrain.FLUIDS_LAVA);
            cell.getTerrain().setFluidCount(1);
        } else if (iSpecialType == MapGeneratorItem.SPECIAL_INT_LAVA) {
            cell.getTerrain().setFluidType(Terrain.FLUIDS_LAVA);
            cell.getTerrain().setFluidCount(Terrain.FLUIDS_COUNT_MAX);
        } else if (iSpecialType == MapGeneratorItem.SPECIAL_INT_LAVA_INF) {
            cell.getTerrain().setFluidType(Terrain.FLUIDS_LAVA);
            cell.getTerrain().setFluidCount(Terrain.FLUIDS_COUNT_INFINITE);
        }

        generateCellChanges(cell, (short) x, (short) y, (short) z);
    }

    private void generateCellChanges(Cell cell, short x, short y, short z) {
        Game.getWorld().addFluidCellToProcess(cell.getCoordinates(), true);

        cell.setDiscovered(true);
        if (z < (World.MAP_DEPTH - 1)) {
            World.getCell(x, y, z + 1).setDiscovered(true);
            Terrain.discoverNeighbours(x, y, z + 1);
        }

        // Minimap
        MiniMapPanel.setMinimapReload(z);
    }

    private void launchSeedData(SeedData sd) {
        if (sd == null) {
            return;
        }

        if (sd.type == null || sd.type.trim().length() == 0) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MapGenerator.13"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Todo cargado, procedemos
        boolean[][][] abMap = new boolean[World.MAP_WIDTH][World.MAP_HEIGHT][World.MAP_DEPTH];

        // Metemos las seeds
        int iX = 0, iY = 0, iLevel = 0;
        boolean bOK;
        for (int i = 0; i < sd.num; i++) {
            bOK = false;
            if (sd.startingPointID != null) {
                ArrayList<Point3D> alPoints = hmSeedsIDs.get(sd.startingPointID);
                if (alPoints != null && alPoints.size() > 0) {
                    Point3D p3d = alPoints.get(Utils.getRandomBetween(0, alPoints.size() - 1));
                    iX = p3d.x;
                    iY = p3d.y;
                    iLevel = p3d.z;
                    bOK = true;
                }
            }

            if (!bOK) {
                iLevel = Utils.launchDice(sd.level);
                if (iLevel < 0) {
                    iLevel = 0;
                } else if (iLevel >= World.MAP_DEPTH) {
                    iLevel = World.MAP_DEPTH - 1;
                }

                if (sd.pointx == null || sd.pointx.length() == 0) {
                    iX = Utils.getRandomBetween(0, World.MAP_WIDTH - 1);
                } else {
                    iX = Utils.launchDice(sd.pointx);
                }
                if (sd.pointy == null || sd.pointy.length() == 0) {
                    iY = Utils.getRandomBetween(0, World.MAP_HEIGHT - 1);
                } else {
                    iY = Utils.launchDice(sd.pointy);
                }
            }

            abMap[iX][iY][iLevel] = true;
        }

        int[] aiX = new int[World.MAP_WIDTH * World.MAP_HEIGHT * World.MAP_DEPTH];
        int[] aiY = new int[World.MAP_WIDTH * World.MAP_HEIGHT * World.MAP_DEPTH];
        int[] aiZ = new int[World.MAP_WIDTH * World.MAP_HEIGHT * World.MAP_DEPTH];
        int iNumPoints;

        // Las hacemos crecer
        for (int i = 0; i < sd.turns; i++) {

            iNumPoints = 0;
            // Recorremos el mapa buscando las seeds
            for (int x = 0; x < World.MAP_WIDTH; x++) {
                for (int y = 0; y < World.MAP_HEIGHT; y++) {
                    for (int z = 0; z < World.MAP_DEPTH; z++) {
                        if (abMap[x][y][z]) {
							// 6 randoms para ver si crece hacia algun lado
                            // Norte
                            if ((y - 1) >= 0) {
                                if (Utils.getRandomBetween(1, 100) <= sd.northPCT) {
                                    if (!abMap[x][y - 1][z]) {
                                        aiX[iNumPoints] = x;
                                        aiY[iNumPoints] = y - 1;
                                        aiZ[iNumPoints] = z;
                                        iNumPoints++;
                                    }
                                }
                            }
                            // Sur
                            if ((y + 1) < World.MAP_HEIGHT) {
                                if (Utils.getRandomBetween(1, 100) <= sd.southPCT) {
                                    if (!abMap[x][y + 1][z]) {
                                        aiX[iNumPoints] = x;
                                        aiY[iNumPoints] = y + 1;
                                        aiZ[iNumPoints] = z;
                                        iNumPoints++;
                                    }
                                }
                            }
                            // Este
                            if ((x - 1) >= 0) {
                                if (Utils.getRandomBetween(1, 100) <= sd.eastPCT) {
                                    if (!abMap[x - 1][y][z]) {
                                        aiX[iNumPoints] = x - 1;
                                        aiY[iNumPoints] = y;
                                        aiZ[iNumPoints] = z;
                                        iNumPoints++;
                                    }
                                }
                            }
                            // Oeste
                            if ((x + 1) < World.MAP_WIDTH) {
                                if (Utils.getRandomBetween(1, 100) <= sd.westPCT) {
                                    if (!abMap[x + 1][y][z]) {
                                        aiX[iNumPoints] = x + 1;
                                        aiY[iNumPoints] = y;
                                        aiZ[iNumPoints] = z;
                                        iNumPoints++;
                                    }
                                }
                            }

                            // Arriba
                            if (z > 0) {
                                if (Utils.getRandomBetween(1, 100) <= sd.upPCT) {
                                    if (!abMap[x][y][z - 1]) {
                                        aiX[iNumPoints] = x;
                                        aiY[iNumPoints] = y;
                                        aiZ[iNumPoints] = z - 1;
                                        iNumPoints++;
                                    }
                                }
                            }

                            // Abajo
                            if (z < (World.MAP_DEPTH - 1)) {
                                if (Utils.getRandomBetween(1, 100) <= sd.downPCT) {
                                    if (!abMap[x][y][z + 1]) {
                                        aiX[iNumPoints] = x;
                                        aiY[iNumPoints] = y;
                                        aiZ[iNumPoints] = z + 1;
                                        iNumPoints++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (int s = 0; s < iNumPoints; s++) {
                abMap[aiX[s]][aiY[s]][aiZ[s]] = true;
            }
        }

        // Seeds crecidas, metemos el type en el mapa pasado
        int iSpecialType = MapGeneratorItem.getSpecialInt(sd.type);
        int iTerrainID = 0;
        if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE) {
            TerrainManagerItem tmi = TerrainManager.getItem(sd.type);
            if (tmi == null) {
                Log.log(Log.LEVEL_ERROR, Messages.getString("MapGenerator.5") + sd.type + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                return;
            }
            iTerrainID = tmi.getTerrainID();
        }

        // Si la seed tiene ID guardamos los puntos en la hash
        if (sd.id != null) {
            ArrayList<Point3D> alPoints = new ArrayList<Point3D>();
            for (int x = 0; x < World.MAP_WIDTH; x++) {
                for (int y = 0; y < World.MAP_HEIGHT; y++) {
                    for (int z = 0; z < World.MAP_DEPTH; z++) {
                        if (abMap[x][y][z]) {
                            alPoints.add(new Point3D(x, y, z));
                        }
                    }
                }
            }

            if (alPoints.size() > 0) {
                hmSeedsIDs.put(sd.id, alPoints);
            }
        }

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

        for (int z = zMin; z <= zMax; z++) {
            for (int x = 0; x < World.MAP_WIDTH; x++) {
                for (int y = 0; y < World.MAP_HEIGHT; y++) {
                    if (abMap[x][y][z]) {
                        if (iSpecialType != MapGeneratorItem.SPECIAL_INT_NONE) {
                            generateTerrainSpecial(iSpecialType, x, y, z);
                        } else {
                            generateTerrain(iTerrainID, x, y, z, true);
                        }
                    }
                }
            }
        }
    }

    private void launchHeightSeedData(HeightSeedData hsd) {
        if (hsd == null) {
            return;
        }

        if (hsd.flatsBetweenLevels < 1) {
            hsd.flatsBetweenLevels = 1;
        }

		// Todo cargado, procedemos
        boolean[][] abMap = new boolean[World.MAP_WIDTH][World.MAP_HEIGHT];

        // Metemos las seeds
        int iX = 0, iY = 0;
        boolean bOK;
        for (int i = 0; i < hsd.num; i++) {
            bOK = false;
            if (hsd.startingPointID != null) {
                ArrayList<Point3D> alPoints = hmSeedsIDs.get(hsd.startingPointID);
                if (alPoints != null && alPoints.size() > 0) {
                    Point3D p3d = alPoints.get(Utils.getRandomBetween(0, alPoints.size() - 1));
                    iX = p3d.x;
                    iY = p3d.y;
                    bOK = true;
                }
            }

            if (!bOK) {
                if (hsd.pointx == null || hsd.pointx.length() == 0) {
                    iX = Utils.getRandomBetween(0, World.MAP_WIDTH - 1);
                } else {
                    iX = Utils.launchDice(hsd.pointx);
                }
                if (hsd.pointy == null || hsd.pointy.length() == 0) {
                    iY = Utils.getRandomBetween(0, World.MAP_HEIGHT - 1);
                } else {
                    iY = Utils.launchDice(hsd.pointy);
                }
            }

            abMap[iX][iY] = true;
        }

        int[] aiX = new int[World.MAP_WIDTH * World.MAP_HEIGHT];
        int[] aiY = new int[World.MAP_WIDTH * World.MAP_HEIGHT];
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
                            if (Utils.getRandomBetween(1, 100) <= hsd.northPCT) {
                                if (!abMap[x][y - 1]) {
                                    aiX[iNumPoints] = x;
                                    aiY[iNumPoints] = y - 1;
                                    iNumPoints++;
                                }
                            }
                        }
                        // Sur
                        if ((y + 1) < World.MAP_HEIGHT) {
                            if (Utils.getRandomBetween(1, 100) <= hsd.southPCT) {
                                if (!abMap[x][y + 1]) {
                                    aiX[iNumPoints] = x;
                                    aiY[iNumPoints] = y + 1;
                                    iNumPoints++;
                                }
                            }
                        }
                        // Este
                        if ((x - 1) >= 0) {
                            if (Utils.getRandomBetween(1, 100) <= hsd.eastPCT) {
                                if (!abMap[x - 1][y]) {
                                    aiX[iNumPoints] = x - 1;
                                    aiY[iNumPoints] = y;
                                    iNumPoints++;
                                }
                            }
                        }
                        // Oeste
                        if ((x + 1) < World.MAP_WIDTH) {
                            if (Utils.getRandomBetween(1, 100) <= hsd.westPCT) {
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
        boolean[][] abMapAux = new boolean[abMap.length][abMap[0].length];

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
            Cell cell;
            for (int x = 0; x < World.MAP_WIDTH; x++) {
                for (int y = 0; y < World.MAP_HEIGHT; y++) {
                    if (abMap[x][y]) {
						// Raiseamos
                        // Buscamos el último _AIR
                        boolean bHayAIRs = false;
                        for (int z = 0; z <= World.MAP_NUM_LEVELS_OUTSIDE; z++) {
                            cell = World.getCell(x, y, z);
                            if (cell.getTerrain().getTerrainID() == TerrainManagerItem.TERRAIN_AIR_ID) {
                                bHayAIRs = true;
                            } else {
                                // Tenemos un no-air
                                if (bHayAIRs) {
                                    // Tiene espacio por encima, así que simplemente cambiamos el terrain por el de abajo
                                    generateTerrain(cell.getTerrain().getTerrainID(), x, y, z - 1, false);
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
                        breakNeighbours:
                        for (int i = -hsd.flatsBetweenLevels; i <= hsd.flatsBetweenLevels; i++) {
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

    private void launchChangeData(ChangeData cd) {
        if (cd == null) {
            return;
        }

        if (cd.destination == null || cd.destination.length() == 0 || (cd.iHeightMin > cd.iHeightMax && cd.iHeightMax != -1)) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MapGenerator.3"), "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        boolean[][][] abMap = new boolean[World.MAP_WIDTH][World.MAP_HEIGHT][World.MAP_DEPTH];

        int iSourceTerrainID = 0;
        if (cd.source != null) {
            TerrainManagerItem tmi = TerrainManager.getItem(cd.source);
            if (tmi != null) {
                iSourceTerrainID = tmi.getTerrainID();
            } else {
                Log.log(Log.LEVEL_ERROR, Messages.getString("MapGenerator.5") + cd.source + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                return;
            }
        }

        int iSpecialType = MapGeneratorItem.getSpecialInt(cd.terrain);
        int iTerrainTerrainID = 0;
        if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE && cd.terrain != null) {
            TerrainManagerItem tmi = TerrainManager.getItem(cd.terrain);
            if (tmi == null) {
                Log.log(Log.LEVEL_ERROR, Messages.getString("MapGenerator.5") + cd.terrain + "]", "MapGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                iTerrainTerrainID = tmi.getTerrainID();
            }
        }
        // Recorremos todo el mundo buscando el source, lanzando dados y sustituyendo por destination
        for (int z = 0; z < World.MAP_DEPTH; z++) {
            // Miramos heights
            if ((z >= cd.iHeightMin || cd.iHeightMin == -1) && (z <= cd.iHeightMax || cd.iHeightMax == -1)) {
                for (int x = 0; x < World.MAP_WIDTH; x++) {
                    for (int y = 0; y < World.MAP_HEIGHT; y++) {
                        if (cd.source == null || (World.getCell(x, y, z).getTerrain().getTerrainID() == iSourceTerrainID)) {
                            if (iSourceTerrainID != TerrainManagerItem.TERRAIN_AIR_ID) {
                                // Lanzamos dado
                                if (Utils.getRandomBetween(1, 100) <= cd.pct) {
                                    // Miramos vecinos
                                    if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE) {
                                        if (cd.terrain == null || checkNeighbors(x, y, z, iTerrainTerrainID, cd.radius)) {
                                            abMap[x][y][z] = true;
                                        }
                                    } else {
                                        if (checkNeighborsSpecial(x, y, z, iSpecialType, cd.radius)) {
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

        // Cambiamos el terreno en el mapa
        iSpecialType = MapGeneratorItem.getSpecialInt(cd.destination);
        int iTerrainID = 0;
        if (iSpecialType == MapGeneratorItem.SPECIAL_INT_NONE) {
            iTerrainID = TerrainManager.getItem(cd.destination).getTerrainID();
        }
        for (int x = 0; x < World.MAP_WIDTH; x++) {
            for (int y = 0; y < World.MAP_HEIGHT; y++) {
                for (int z = 0; z < World.MAP_DEPTH; z++) {
                    if (abMap[x][y][z]) {
                        if (iSpecialType != MapGeneratorItem.SPECIAL_INT_NONE) {
                            generateTerrainSpecial(iSpecialType, x, y, z);
                        } else {
                            generateTerrain(iTerrainID, x, y, z, true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Indica si hay alguna casilla de tipo (terrain) alrrededor del punto
     * pasado
     *
     * @param asMap
     * @param x
     * @param y
     * @param z
     * @return
     */
    private static boolean checkNeighbors(int x, int y, int z, int iTerrainID, int radius) {
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int n = -radius; n <= radius; n++) {
                    if (i != 0 || j != 0 || n != 0) {
                        if (Utils.isInsideMap(x + i, y + j, n + z)) {
                            // Miramos si tiene el terrain
                            if (World.getCell(x + i, y + j, n + z).getTerrain().getTerrainID() == iTerrainID) {
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
     * Indica si hay alguna casilla de tipo especial (terrain) alrrededor del
     * punto pasado
     *
     * @param asMap
     * @param x
     * @param y
     * @param z
     * @return
     */
    private static boolean checkNeighborsSpecial(int x, int y, int z, int iSpecialTerrain, int radius) {
        Cell cell;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int n = -radius; n <= radius; n++) {
                    if (i != 0 || j != 0 || n != 0) {
                        if (Utils.isInsideMap(x + i, y + j, n + z)) {
                            // Miramos si tiene el terrain
                            cell = World.getCell(x + i, y + j, n + z);
                            if (cell.getTerrain().hasFluids()) {
                                // Miramos que sea el special correcto
                                if (iSpecialTerrain == MapGeneratorItem.SPECIAL_INT_WATER || iSpecialTerrain == MapGeneratorItem.SPECIAL_INT_WATER_1 || iSpecialTerrain == MapGeneratorItem.SPECIAL_INT_WATER_INF) {
                                    // Agua
                                    if (cell.getTerrain().getFluidType() == Terrain.FLUIDS_WATER) {
                                        return true;
                                    }
                                } else if (iSpecialTerrain == MapGeneratorItem.SPECIAL_INT_LAVA || iSpecialTerrain == MapGeneratorItem.SPECIAL_INT_LAVA_1 || iSpecialTerrain == MapGeneratorItem.SPECIAL_INT_LAVA_INF) {
                                    // Lava
                                    if (cell.getTerrain().getFluidType() == Terrain.FLUIDS_LAVA) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public EventData(String effectID) {
        setEventID(effectID);
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventID() {
        return eventID;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public int getTurns() {
        return turns;
    }

    public void setEventCooldown(int eventCooldown) {
        this.eventCooldown = eventCooldown;
    }

    public int getEventCooldown() {
        return eventCooldown;
    }

    public void setWaitPCT(int waitPCT) {
        this.waitPCT = waitPCT;
    }

    public int getWaitPCT() {
        return waitPCT;
    }

    public void setWalkSpeedPCT(int walkSpeedPCT) {
        this.walkSpeedPCT = walkSpeedPCT;
    }

    public int getWalkSpeedPCT() {
        return walkSpeedPCT;
    }

    public void setFxRunningTurns(int fxRunningTurns) {
        this.fxRunningTurns = fxRunningTurns;
    }

    public int getFxRunningTurns() {
        return fxRunningTurns;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        eventID = (String) in.readObject();
        order = in.readInt();
        turns = in.readInt();
        eventCooldown = in.readInt();
        waitPCT = in.readInt();
        walkSpeedPCT = in.readInt();
        fxRunningTurns = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(eventID);
        out.writeInt(order);
        out.writeInt(turns);
        out.writeInt(eventCooldown);
        out.writeInt(waitPCT);
        out.writeInt(walkSpeedPCT);
        out.writeInt(fxRunningTurns);
    }
}
