package xaos.tiles.entities.living.heroes;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import xaos.TownsProperties;

import xaos.data.CitizenData;
import xaos.data.EventData;
import xaos.data.FocusData;
import xaos.data.HeroData;
import xaos.data.SkillData;
import xaos.effects.EffectManager;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MessagesPanel;
import xaos.panels.menus.SmartMenu;
import xaos.skills.SkillEffectItem;
import xaos.skills.SkillManager;
import xaos.skills.SkillManagerItem;
import xaos.tiles.Cell;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.terrain.Terrain;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;
import xaos.zones.Zone;
import xaos.zones.ZoneManager;
import xaos.zones.ZoneManagerItem;

public class Hero extends LivingEntity implements Externalizable {

    private static final long serialVersionUID = 3221178311971531048L;

    private CitizenData citizenData;
    private HeroData heroData;

    public Hero() {
        super();
    }

    // Constructor principal, actualiza el ID de enemigo
    public Hero(String iniHeader) {
        super(iniHeader);
        setWaitingForPath(false);

        LivingEntityManagerItem lemi = LivingEntityManager.getItem(getIniHeader());

        // Data (nombre, vida, ...)
        citizenData = new CitizenData(lemi);
        heroData = new HeroData(lemi);

        // Name
        getLivingEntityData().setName(citizenData.getFullName());

        refreshTransients();
    }

    public void refreshTransients() {
        super.refreshTransients();

        if (getLivingEntityData() != null && getCitizenData() != null) {
            // Name
            getLivingEntityData().setName(getCitizenData().getFullName());
        }

        if (getCitizenData() != null) {
            if (getCarrying() != null) {
                getCarrying().refreshTransients();
            }

            if (getCarryingLiving() != null) {
                getCarryingLiving().refreshTransients();
            }
        }

        if (getEquippedData() != null) {
            getEquippedData().refreshTransients();
        }
    }

    /**
     * Resta los contadores de stay, comida y sueño, suma el contador de puntos
     * de vida
     *
     * @return
     */
    public void updateCounters() {
        // Eat
        getCitizenData().setHungry(getCitizenData().getHungry() - 1);
        if (getCitizenData().getHungry() < 0) {
            getCitizenData().setHungry(0);

            if (getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_EATING && getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_SLEEPING && getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_LEAVING) {
                // Buscamos comida
                Point3DShort p3d = Item.searchFood(getCoordinates(), getID()); // Héroes no le quitan la comida a los aldeanos, pero sí al revés
                if (p3d == null) {
                    // No hay comida
                    getCitizenData().setHungryEating(getCitizenData().getHungryEating() - 1);

                    if (getCitizenData().getHungryEating() < -(2 * World.TIME_MODIFIER_HOUR)) { // 2 horas sin comida en el mundo, se pira
                        if (canChangeTask(false, false)) { // Falses pq comida y sleep ya están mirados arriba
                            getHeroData().getHeroTask().setTaskID(HeroTask.TASK_LEAVING);
                            MessagesPanel.addMessage(MessagesPanel.TYPE_HEROES, getCitizenData().getFullName() + Messages.getString("Hero.1"), ColorGL.ORANGE, getCoordinates(), getID()); //$NON-NLS-1$
                        }
                    }
                } else {
                    getHeroData().getHeroTask().setTaskID(HeroTask.TASK_EATING);
                }
            }
        }

        // Sleep
        getCitizenData().setSleep(getCitizenData().getSleep() - 1);
        if (getCitizenData().getSleep() < 0) {
            getCitizenData().setSleep(0);

            if (getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_EATING && getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_SLEEPING && getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_LEAVING) {
                // A dormir
                getHeroData().getHeroTask().setTaskID(HeroTask.TASK_SLEEPING);
            }
        }

        // Turnos de stay
        if (getHeroData().getMinTurnsToStay() > 0) {
            getHeroData().setMinTurnsToStay(getHeroData().getMinTurnsToStay() - 1);
        }
    }

    public void setCitizenData(CitizenData citizenData) {
        this.citizenData = citizenData;
    }

    public CitizenData getCitizenData() {
        return citizenData;
    }

    public void setHeroData(HeroData heroData) {
        this.heroData = heroData;
    }

    public HeroData getHeroData() {
        return heroData;
    }

    public Item getCarrying() {
        return getCitizenData().getCarryingData().getCarrying();
    }

    public LivingEntity getCarryingLiving() {
        return getCitizenData().getCarryingData().getCarryingLiving();
    }

    public boolean isSleeping() {
        return getCitizenData().getSleep() == 0;
    }

    public void doHeroSkills() {
        // Reducimos el cooldown
        SkillData sd;
        for (int i = 0; i < getHeroData().getSkills().size(); i++) {
            sd = getHeroData().getSkills().get(i);
            if (sd.getCoolDown() > 0) {
                sd.setCoolDown(sd.getCoolDown() - 1);
            }
        }

        // Lanzamos la skill
        checkAndUseSkills(SkillManagerItem.USE_ALWAYS);

        // Skill que se lanza si no tiene el max de vida
        if (getLivingEntityData().getHealthPoints() < getLivingEntityData().getHealthPointsMAXCurrent()) {
            checkAndUseSkills(SkillManagerItem.USE_NOT_MAX_HP);
        }
    }

    public void checkAndUseSkills(int useType) {
        if (getHeroData().getHeroTask() != null && getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_EATING && getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_SLEEPING && getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_LEAVING) {
            SkillData sd;
            for (int i = 0; i < getHeroData().getSkills().size(); i++) {
                sd = getHeroData().getSkills().get(i);
                if (sd.getCoolDown() <= 0) {
                    // La skill ha llegado a su cooldown, la lanzamos si es de tipo adecuado
                    if (sd.getUse() == useType) {
                        SkillManagerItem smi = SkillManager.getItem(sd.getSkillID());
                        useSkill(smi);
                        // Reseteamos el cooldown
                        sd.setCoolDown(Utils.launchDice(smi.getCoolDown()));
                    }
                }
            }
        }
    }

    public boolean doHeroStuff() {
        // Hacemos las cosas de hero
        if (getHeroData().updateBehaviour()) {
            // Toca cambiar el behaviour
            getHeroData().setCurrentBehaviourID(HeroBehaviour.next(getHeroData().getCurrentBehaviourID(), getHeroData().getHeroBehaviour()));
            // Turnos
            getHeroData().setTurnsToNextBehaviour(HeroBehaviour.getTurns(getHeroData().getCurrentBehaviourID(), getHeroData().getHeroBehaviour()));

            // Reset task si no está durmiendo/comiendo o tratando de irse/en furia
            if (canChangeTask(true, true)) {
                getHeroData().getHeroTask().setTaskID(HeroTask.TASK_NO_TASK);
            }
        }

        // Primero miramos si tiene task
        if (getHeroData().getHeroTask().getTaskID() != HeroTask.TASK_NO_TASK) {
            // Tiene task
            if (getHeroData().getHeroTask().getTaskID() == HeroTask.TASK_LEAVING) {
                if (getHeroData().getStartingPoint().equals(getCoordinates())) {
                    // Se acabó, nos piramos
                    return true;
                } else {
                    startLeaving();
                }
            } else if (getHeroData().getHeroTask().getTaskID() == HeroTask.TASK_RAGE) {
                doRage();
            } else if (getHeroData().getHeroTask().getTaskID() == HeroTask.TASK_DESTROY_BLOCKING) {
                doDestroyBlockingItems();
            } else if (getHeroData().getHeroTask().getTaskID() == HeroTask.TASK_IDLE) {
                doIdle();
            } else if (getHeroData().getHeroTask().getTaskID() == HeroTask.TASK_EXPLORING) {
                doExploring();
            } else if (getHeroData().getHeroTask().getTaskID() == HeroTask.TASK_EQUIPING) {
                doEquipping();
            } else if (getHeroData().getHeroTask().getTaskID() == HeroTask.TASK_EATING) {
                doEating();
            } else if (getHeroData().getHeroTask().getTaskID() == HeroTask.TASK_SLEEPING) {
                doSleeping();
            }

        } else {
            // No tiene task, le metemos la tarea de su behaviour

            switch (getHeroData().getCurrentBehaviourID()) {
                case HeroBehaviour.BEHAVIOUR_ID_IDLE:
                    getHeroData().getHeroTask().setTaskID(HeroTask.TASK_IDLE);
                    getHeroData().setIdleCounter(0);
                    getHeroData().setIdleTask(HeroData.IDLE_NONE);
                    break;
                case HeroBehaviour.BEHAVIOUR_ID_EXPLORE:
                    getHeroData().getHeroTask().setTaskID(HeroTask.TASK_EXPLORING);
                    getHeroData().setExploringCounter(0);
                    break;
                default:
                    // System.err.println ("Esto no debería pasar nunca");
                    moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_HERO);
            }
        }

        return false;
    }

    /**
     * Equipping
     */
    private void doEquipping() {
		// Si hay un item a la vista, vamos a por el
        // Primero miramos que no estemos ya encima
        Cell cell = World.getCell(getCoordinates());
        boolean bEquipped = false;
        if (cell.hasItem()) {
            Item item = cell.getItem();
            if (item != null) {
                ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
                if (imi.isMilitaryItem()) {
                    if (MilitaryItem.isBetterItem(this, (MilitaryItem) item)) {
                        // Miramos si el item tiene tags
                        boolean bTagsOK = true;
                        if (imi.getTags() != null && imi.getTags().size() > 0) {
                            // El item tiene tags

                            LivingEntityManagerItem lemi = LivingEntityManager.getItem(getIniHeader());
                            if (lemi.getEquipAllowed() != null && lemi.getEquipAllowed().size() > 0) {
                                // El personaje tiene tags

                                for (int i = 0; i < imi.getTags().size(); i++) {
                                    if (!lemi.getEquipAllowed().contains(imi.getTags().get(i))) {
                                        bTagsOK = false;
                                        break;
                                    }
                                }
                            } else {
                                bTagsOK = false;
                            }
                        }

                        if (bTagsOK) {
                            bEquipped = true;

                            // Nos quitamos el item equipado
                            Item oldItem = unequip(imi.getLocation(), LivingEntity.TYPE_HERO);

                            // Borramos el de la celda
                            item.delete();

                            // Lo equipamos
                            equip((MilitaryItem) item);

                            // Dejamos en la celda el antiguo item (si tenía)
                            if (oldItem != null) {
                                oldItem.init(getX(), getY(), getZ());
                                ItemManagerItem oldImi = ItemManager.getItem(oldItem.getIniHeader());
                                oldItem.setOperative(oldImi.isAlwaysOperative());
                                oldItem.setLocked(oldImi.isLocked());
                            }
                            cell.setEntity(oldItem);
                        }
                    }
                }
            }
        }

        if (!bEquipped) {
            // Aquí no hay item, miramos si hay alguno en LOS
            MilitaryItem mi = hasBetterItemInLOS(entityTypeInLOS(TYPE_HERO, TYPE_CITIZEN));
            if (mi != null) {
                setDestination(mi.getCoordinates());
            } else {
                getHeroData().getHeroTask().setTaskID(HeroTask.TASK_NO_TASK);
            }
        } else {
            getHeroData().getHeroTask().setTaskID(HeroTask.TASK_NO_TASK);
        }
    }

    /**
     * Idle
     */
    private void doIdle() {
        if (getHeroData().getIdleCounter() == 0) {
            // Empieza el idle, escogemos qué hacer
            getHeroData().setIdleTask(HeroData.IDLE_SOCIAL);
            getHeroData().setIdleCounter(World.TIME_MODIFIER_HOUR * 4); // 4 horitas
        } else {
            getHeroData().setIdleCounter(getHeroData().getIdleCounter() - 1);
            if (heroData.getIdleCounter() < 0) {
                heroData.setIdleCounter(0);
            }

            switch (getHeroData().getIdleTask()) {
                case HeroData.IDLE_NONE:
                case HeroData.IDLE_STEAL:
                    moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_HERO);
                    break;
                case HeroData.IDLE_SOCIAL:
					// Buscamos una zona social, si no la encuentra pondremos idle_none
                    // Primero miramos si ya estamos en una
                    Cell cell = World.getCell(getCoordinates());
                    boolean bEnZona = false;
                    if (cell.hasZone()) {
                        Zone zone = Zone.getZone(cell.getZoneID());
                        if (zone != null) {
                            if (ZoneManager.getItem(zone.getIniHeader()).getType() == ZoneManagerItem.TYPE_SOCIAL) {
                                // Está en zona social, INTENTAMOS movernos a random sólo 1 de cada 20
                                bEnZona = true;
                                if (Utils.getRandomBetween(1, 20) == 1) {
                                    moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_HERO);
                                }
                            }
                        }
                    }

                    if (!bEnZona) {
                        // Si no está en zona buscamos una
                        Zone zone;
                        ArrayList<Zone> alZones = Game.getWorld().getZones();
                        Point3DShort p3dZoneNear = null;
                        int iMinDistance = Utils.MAX_DISTANCE;
                        for (int i = 0; i < alZones.size(); i++) {
                            zone = alZones.get(i);
                            if (ZoneManager.getItem(zone.getIniHeader()).getType() == ZoneManagerItem.TYPE_SOCIAL) {
                                // Zona social encontrada
                                Point3DShort p3d = Zone.getFreeSitItemAtRandom(zone, cell.getAstarZoneID());
                                if (p3d == null) {
                                    p3d = Zone.getFreeCellAtRandom(zone, cell.getAstarZoneID());
                                }

                                if (p3d != null) {
                                    // Punto de la zona libre encontrado, miramos distancia
                                    if (p3dZoneNear == null) {
                                        p3dZoneNear = Point3DShort.getPoolInstance(p3d);
                                        iMinDistance = Utils.getDistance(getCoordinates(), p3d);
                                    } else {
                                        int iDistance = Utils.getDistance(getCoordinates(), p3d);
                                        if (iDistance < iMinDistance) {
                                            p3dZoneNear.setPoint(p3d);
                                            iMinDistance = iDistance;
                                        }
                                    }
                                }
                            }
                        }

                        if (p3dZoneNear != null) {
                            // Encontrada! Vamos a ella
                            setDestination(p3dZoneNear);
                        } else {
                            // No hay zona social
                            getHeroData().setIdleTask(HeroData.IDLE_NONE);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Come
     */
    private void doEating() {
        if (getCarrying() == null) {
            Point3DShort p3dItem = Item.searchFood(getCoordinates(), getID());
            if (p3dItem == null) {
                // No hay comida en el mundo, raro, salimos de la tarea
                getHeroData().getHeroTask().setTaskID(HeroTask.TASK_NO_TASK);
            } else {
                // Comida en el mundo
                if (p3dItem.equals(getCoordinates())) {
                    // En la casilla actual, la pillamos
                    Cell cell = World.getCell(getCoordinates());
                    if (cell.getEntity() != null && cell.getEntity() instanceof Item) {
                        if (ItemManager.getItem(cell.getEntity().getIniHeader()).isContainer()) {
                            Container container = Game.getWorld().getContainer(cell.getEntity().getID());
                            if (container != null) {
                                Item item = container.removeFoodItem();
                                if (item != null) {
                                    getCitizenData().getCarryingData().setCarrying(item);
                                    getCitizenData().setHungryEating(0);
                                }
                            } else {
								// Algo ha pasado, en la casilla no hay nada que nos sirva (no debería pasar, quizá están moviendo un container)
                                // De momento pasamos y cuando vuelva a entrar buscará comida otra vez
                            }
                        } else {
                            getCitizenData().getCarryingData().setCarrying((Item) cell.getEntity());
                            cell.getEntity().delete();
                            getCitizenData().setHungryEating(0);
                        }
                    } else {
						// Algo ha pasado, en la casilla no hay nada que nos sirva (no debería pasar, quizá están moviendo un container)
                        // De momento pasamos y cuando vuelva a entrar buscará comida otra vez
                    }
                } else {
                    // Por ahí
                    setDestination(p3dItem);
                }
            }
        } else {
            ItemManagerItem imiCarrying = ItemManager.getItem(getCarrying().getIniHeader());
            // Tenemos item, miramos si es comida
            if (imiCarrying.canBeEaten()) {
                // Tenemos comida pillada
                String sZoneToEat = LivingEntityManager.getItem(getIniHeader()).getEatZone();
                // Obtenemos la zona en la que estamos (si es que estamos en alguna)
                ArrayList<Zone> zones = Game.getWorld().getZones();
                Cell cell = World.getCell(getCoordinates());
                boolean bEstamosEnComedor = false;
                if (sZoneToEat != null && sZoneToEat.length() > 0) {
                    if (cell.hasZone()) {
                        Zone zone;
                        for (int i = 0; i < zones.size(); i++) {
                            zone = zones.get(i);
                            if (zone.getID() == cell.getZoneID()) {
                                // Tenemos la zona actual
                                if (zone.isOperative() && zone.getIniHeader().equalsIgnoreCase(sZoneToEat)) {
                                    // Estamos en el comedor, comemos
                                    bEstamosEnComedor = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!bEstamosEnComedor) {
                        // No estamos en comedor, buscamos uno y vamos hacia allí, si lo encontramos
                        Point3DShort p3d = null;
                        Point3DShort p3dMinDistance = null;
                        int iMinDistance = Utils.MAX_DISTANCE;
                        Zone zone;
                        for (int i = 0; i < zones.size(); i++) {
                            zone = zones.get(i);
                            if (zone.isOperative() && zone.getIniHeader().equalsIgnoreCase(sZoneToEat)) {
                                // Miramos si alguna celda del comedor es accesible
                                p3d = Zone.getFreeSitItemAtRandom(zone, cell.getAstarZoneID());
                                if (p3d == null) {
                                    continue;
                                } else {
                                    // Tenemos una celda, miramos la distancia
                                    if (p3dMinDistance == null) {
                                        p3dMinDistance = Point3DShort.getPoolInstance(p3d);
                                        iMinDistance = Utils.getDistance(getCoordinates(), p3d);
                                    } else {
                                        int iAuxDistance = Utils.getDistance(getCoordinates(), p3d);
                                        if (iAuxDistance < iMinDistance) {
                                            p3dMinDistance.setPoint(p3d);
                                            iMinDistance = iAuxDistance;
                                        }
                                    }
                                }
                            }
                        }

                        if (p3dMinDistance == null) {
                            // Si no hay sillas miraremos cualquier casilla del comedor
                            for (int i = 0; i < zones.size(); i++) {
                                zone = zones.get(i);
                                if (zone.isOperative() && zone.getIniHeader().equalsIgnoreCase(sZoneToEat)) {
                                    // Miramos si alguna celda del comedor es accesible
                                    p3d = Zone.getFreeCellAtRandom(zone, cell.getAstarZoneID());
                                    if (p3d == null) {
                                        continue;
                                    } else {
                                        // Tenemos una celda, miramos la distancia
                                        if (p3dMinDistance == null) {
                                            p3dMinDistance = Point3DShort.getPoolInstance(p3d);
                                            iMinDistance = Utils.getDistance(getCoordinates(), p3d);
                                        } else {
                                            int iAuxDistance = Utils.getDistance(getCoordinates(), p3d);
                                            if (iAuxDistance < iMinDistance) {
                                                p3dMinDistance.setPoint(p3d);
                                                iMinDistance = iAuxDistance;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (p3dMinDistance != null) {
                            setDestination(p3dMinDistance);
                            return;
                        }
                    }
                }

                getCitizenData().setHungryEating(getCitizenData().getHungryEating() + 1);
                if ((getCitizenData().getHungryEating() % (World.TIME_MODIFIER_HOUR / 5)) == 0) {
                    UtilsAL.play(UtilsAL.SOURCE_FX_EAT, getZ());
                }
                ItemManagerItem imi = ItemManager.getItem(getCarrying().getIniHeader());
                int iEatTime = imi.getFoodEatTime();
                if (iEatTime <= 0) {
                    iEatTime = World.TIME_MODIFIER_HOUR;
                }
                if (getCitizenData().getHungryEating() >= iEatTime) {
                    getCitizenData().setHungryEating(0);
                    getHeroData().getHeroTask().setTaskID(HeroTask.TASK_NO_TASK);
                    int iFillPCT = imi.getFoodFillPCT();
                    if (iFillPCT <= 0) {
                        iFillPCT = 100;
                    }
                    getCitizenData().setHungry((getCitizenData().getMaxHungry() * iFillPCT) / 100);
                    getCitizenData().getCarryingData().setCarrying(null);

                    // Effects después de comer
                    if (imi.getFoodEffects() != null) {
                        for (int i = 0; i < imi.getFoodEffects().size(); i++) {
                            addEffect(EffectManager.getItem(imi.getFoodEffects().get(i)), true);
                        }
                    }

                    // Eventos después de comer
                    for (int i = 0; i < Game.getWorld().getEvents().size(); i++) {
                        EventData ed = Game.getWorld().getEvents().get(i);
                        EventManagerItem emi = EventManager.getItem(ed.getEventID());
                        if (emi != null) {
                            ed.launchEffects(emi, emi.getEffectsAfterEat(), this);
                        }
                    }
                }
            } else {
                // No es comida, petamos el item (son así de bestias)
                getCitizenData().getCarryingData().setCarrying(null);
                MessagesPanel.addMessage(MessagesPanel.TYPE_HEROES, getCitizenData().getFullName() + Messages.getString("Hero.3") + imiCarrying.getName(), ColorGL.ORANGE, getCoordinates(), getID()); //$NON-NLS-1$
            }
        }
    }

    /**
     * Duerme
     */
    private void doSleeping() {
		// Si tiene zona personal vamos a ella
        // Antes de mirar nada miraremos si está en una cama, en ese caso no lo movemos pq ya está durmiendo
        boolean bEstaEnCama = false;
        Cell cell = World.getCell(getCoordinates());
        if (cell.hasItem()) {
            ItemManagerItem imi = ItemManager.getItem(cell.getEntity().getIniHeader());
            if (imi != null && imi.canBeUsedToSleep()) {
                bEstaEnCama = true;
            }
        }

        if (!bEstaEnCama) {
            if (getCitizenData().hasZone()) {
                Zone zone = null;
                // Buscamos su zona personal
                for (int i = 0; i < Game.getWorld().getZones().size(); i++) {
                    zone = Game.getWorld().getZones().get(i);
                    if (zone.getID() == getCitizenData().getZoneID()) {
                        break;
                    }
                }

                if (zone != null) {
                    // Tenemos su zona, miramos si estamos en ella
                    if (cell.hasZone() && zone.isOperative() && cell.getZoneID() == zone.getID()) {
                        // Está en ella
                    } else {
                        // No estamos en su zona personal, vamos a ella
                        Point3DShort p3d = Zone.getFreeSleepItemAtRandom(zone, cell.getAstarZoneID());
                        if (p3d == null) {
                            // No hay cama, buscamos cualquier casilla de la zona personal
                            p3d = Zone.getFreeCellAtRandom(zone, cell.getAstarZoneID());
                        }
                        if (p3d != null) {
                            setDestination(p3d);
                            return;
                        } else {
                            // Zona personal no accesible, no hacemos nada y que duerma allí donde esté
                        }
                    }
                } else {
                    // Raro, no existe su zona, se la quitamos
                    Log.log(Log.LEVEL_ERROR, Messages.getString("Citizen.28"), getClass().toString()); //$NON-NLS-1$
                    getCitizenData().setZoneID(0);
                }
            }
        }

        // Si llega aquí es que ya tenemos que dormir
        getCitizenData().setSleepSleeping(getCitizenData().getSleepSleeping() + 1);
        if (getCitizenData().getSleepSleeping() >= (6 * World.TIME_MODIFIER_HOUR)) {
            // Ya ha dormido
            getCitizenData().setSleepSleeping(0);
            getCitizenData().setSleep(getCitizenData().getMaxSleep());

            getHeroData().getHeroTask().setTaskID(HeroTask.TASK_NO_TASK);

            // Events después de dormir
            for (int i = 0; i < Game.getWorld().getEvents().size(); i++) {
                EventData ed = Game.getWorld().getEvents().get(i);
                EventManagerItem emi = EventManager.getItem(ed.getEventID());
                if (emi != null) {
                    ed.launchEffects(emi, emi.getEffectsAfterSleep(), this);
                }
            }
        }
    }

    /**
     * Explora por ahí
     */
    private void doExploring() {
        if (getHeroData().getExploringCounter() == 0) {
            // Toca ir a explorar, esperamos 1 hora antes de volver a buscar
            getHeroData().setExploringCounter(HeroData.TURNS_BETWEEN_EXPLORE);

            // Miramos si hay algún hotpoint para explorar
            Point3DShort p3dExploringHP = Game.getWorld().getRandomExploringHotpoint(World.getCell(getCoordinates()).getAstarZoneID());

            if (p3dExploringHP == null) {
                // Y lo movemos a random a algún punto de por ahí
                int iMaxLevelDiscovered = Game.getWorld().getNumFloorsDiscovered() - 1;
                if (iMaxLevelDiscovered >= World.MAP_DEPTH) { // No debería
                    iMaxLevelDiscovered = World.MAP_DEPTH - 1;
                }

                iMaxLevelDiscovered = Math.min(iMaxLevelDiscovered, Game.getWorld().getRestrictExploringLevel());
                boolean bEncontrado = false;
                Point3DShort p3d;
                Cell cell;
                int iTryes = 256; // 256 intentos por nivel
                while (!bEncontrado && iTryes > 0) {
                    for (int i = iMaxLevelDiscovered; i > 0; i--) {
                        // Buscamos 1 punto a random para cada nivel
                        p3d = Point3DShort.getPoolInstance(Utils.getRandomBetween(0, World.MAP_WIDTH - 1), Utils.getRandomBetween(0, World.MAP_HEIGHT - 1), i);
                        cell = World.getCell(p3d);
                        if (cell.isDiscovered() && cell.getAstarZoneID() == World.getCell(getCoordinates()).getAstarZoneID()) {
                            // Encontrado, finish
                            setDestination(p3d);
                            moveFriendsTo(p3d);
                            Point3DShort.returnToPool(p3d);
                            bEncontrado = true;
                            break;
                        }

                        Point3DShort.returnToPool(p3d);
                    }

                    iTryes--;
                }

                if (!bEncontrado) {
                    // No ha encontrado punto, miramos en la superficie
                    for (int i = 0; i < 256; i++) {
                        // Buscamos 1 punto a random
                        p3d = Point3DShort.getPoolInstance(Utils.getRandomBetween(0, World.MAP_WIDTH - 1), Utils.getRandomBetween(0, World.MAP_WIDTH - 1), 0);
                        cell = World.getCell(p3d);
                        if (cell.isDiscovered() && cell.getAstarZoneID() == World.getCell(getCoordinates()).getAstarZoneID()) {
                            // Encontrado, finish
                            setDestination(p3d);
                            moveFriendsTo(p3d);
                            Point3DShort.returnToPool(p3d);
                            break;
                        }

                        Point3DShort.returnToPool(p3d);
                    }
                }
            } else {
                setDestination(p3dExploringHP);
                moveFriendsTo(p3dExploringHP);
            }
        } else {
            if (getHeroData().getExploringCounter() == HeroData.TURNS_BETWEEN_EXPLORE) {
                // Eliminamos el punto de la lista de hotpoints
                Game.getWorld().getExploringHotPoints().remove(getCoordinates());
                World.getCell(getCoordinates()).setHeroExploringPoint(false);
            }

            getHeroData().setExploringCounter(getHeroData().getExploringCounter() - 1);
            if (getHeroData().getExploringCounter() < 0) {
                getHeroData().setExploringCounter(0);
            }

            moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_HERO);
        }
    }

    private void moveFriendsTo(Point3DShort p3ds) {
//		getHeroData ().setExploringCounter (HeroData.TURNS_BETWEEN_EXPLORE);
        // Miramos friends
        if (getHeroData().getFriendships().size() > 0) {
            Hero hero;
            for (int h = 0; h < World.getHeroIDs().size(); h++) {
                hero = (Hero) World.getLivingEntityByID(World.getHeroIDs().get(h));
                if (hero != null && hero.getID() != getID() && hero.getHeroData().getHeroTask().getTaskID() == HeroTask.TASK_EXPLORING) {
					// Amigo encontrado que también está explorando
                    // Miramoas que no esté luchando
                    if (hero.getFocusData() == null || hero.getFocusData().getEntityID() == -1) {
                        // Miramos que no esté muriendo (v13a)
                        if ((hero.getLivingEntityData().getHealthPointsMAXCurrent() / 3) <= hero.getLivingEntityData().getHealthPoints()) {
                            // Lo más importante, que el heroe sea un amigo (v13a)
                            if (getHeroData().getFriendships().contains(Integer.valueOf(hero.getID()))) {
                                // Perfecto, que se venga
                                hero.setDestination(p3ds);
                                hero.getHeroData().setExploringCounter(HeroData.TURNS_BETWEEN_EXPLORE);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Usa una habilidad
     */
    private void useSkill(SkillManagerItem smi) {
        ArrayList<SkillEffectItem> alSkillEffects = smi.getEffects();
        if (alSkillEffects != null && alSkillEffects.size() > 0) {
            SkillEffectItem sei;
            boolean bVerbosed = false;
            for (int i = 0; i < alSkillEffects.size(); i++) {
                sei = alSkillEffects.get(i);
                if (sei.getTarget() != SkillEffectItem.TARGET_INT_NONE) {
                    // Tiene target, hay que añadir el effect al target
                    ArrayList<LivingEntity> alTargets = null;
                    if (sei.getTarget() == SkillEffectItem.TARGET_INT_SELF) {
                        // SELF
                        alTargets = new ArrayList<LivingEntity>(1);
                        alTargets.add(this);
                        bVerbosed = true;
                    } else if (sei.getTarget() == SkillEffectItem.TARGET_INT_FRIENDLIES) {
                        // FRIENDLIES
                        alTargets = getAllLivingsInRadius(getLivingEntityData().getLOSCurrent() * 2, false);
                    } else if (sei.getTarget() == SkillEffectItem.TARGET_INT_ENEMIES) {
                        // ENEMIES
                        alTargets = getAllLivingsInRadius(getLivingEntityData().getLOSCurrent() * 2, true);
                    }

                    if (alTargets != null) {
                        if (!bVerbosed) {
                            MessagesPanel.addMessage(MessagesPanel.TYPE_HEROES, getCitizenData().getFullName() + Messages.getString("Hero.6") + smi.getName() + "]", ColorGL.WHITE, getCoordinates(), getID()); //$NON-NLS-1$ //$NON-NLS-2$
                            bVerbosed = true;
                        }
                        for (int t = 0; t < alTargets.size(); t++) {
                            alTargets.get(t).addEffect(EffectManager.getItem(sei.getId()), true);
                        }
                    }
                }
            }
        }

		// Ahora los efectos que no aplican a targets
        // Vamos a ver qué hay que hacer
        if (smi.isTaunt()) {
			// TAUNT
            // Obtenemos hateds para cambiarles el target
            ArrayList<LivingEntity> alTargets = getAllLivingsInRadius(getLivingEntityData().getLOSCurrent() * 2, true);
            LivingEntity le;
            if (alTargets.size() > 0) {
                MessagesPanel.addMessage(MessagesPanel.TYPE_HEROES, getCitizenData().getFullName() + Messages.getString("Hero.6") + smi.getName() + "]", ColorGL.WHITE, getCoordinates()); //$NON-NLS-1$ //$NON-NLS-2$

                for (int t = 0; t < alTargets.size(); t++) {
                    le = alTargets.get(t);
                    le.setFocusData(new FocusData(getID(), TYPE_HERO));
                }
            }
        }
        if (smi.getRaiseDead() != null) {
            // RAISE DEAD
            LivingEntityManagerItem lemi = LivingEntityManager.getItem(smi.getRaiseDead());
            if (lemi != null) {
                int iMax = smi.getMaxRaised();
                if (iMax > 0) {
                    int iCurrent = LivingEntity.getNumLivings(smi.getRaiseDead(), true);
                    if (iCurrent < iMax) {
                        // Puede raisear
                        World.addNewLiving(smi.getRaiseDead(), lemi.getType(), true, getX(), getY(), getZ(), true);
                    }
                }
            }
        }
    }

    /**
     * Se mueve a saco y peta items que bloqueen
     */
    private void doDestroyBlockingItems() {
        Point3DShort p3dBlockingItem = checkBlockingNeighbourItem();
        if (p3dBlockingItem != null) {
            // Hay item para petar, le hostiamos
            Cell cell = World.getCell(p3dBlockingItem);
            Item item = cell.getItem();
            if (item != null) {
                getPath().clear();
                getPath().add(p3dBlockingItem);
                updatePathConstantOffsets();
                item.doHit(this, getLivingEntityData().getDamageCurrent());
            }
        } else {
            // No hay nada para petar, nos movemos a saco
            moveAtRandom(100, TYPE_HERO);
        }
    }

    /**
     * Devuelve la coordenada de un item vecino que bloquee (ASZID == -1)
     *
     * @return la coordenada de un item vecino que bloquee (ASZID == -1)
     */
    private Point3DShort checkBlockingNeighbourItem() {
        int x = getX();
        int y = getY();
        int z = getZ();

        // Arriba
        if (z > 0) {
            if (isBlockingNeighbourItemAt(x, y, z - 1) && Terrain.canGoUp(x, y, z)) {
                return Point3DShort.getPoolInstance(x, y, z - 1);
            }
        }

        boolean[] abNeighbour = new boolean[8];
        boolean bNeighbours = false;
        if (x > 0) {
            if (y > 0) {
                if (isBlockingNeighbourItemAt(x - 1, y - 1, z)) {
                    bNeighbours = true;
                    abNeighbour[0] = true;
                }
            }
            if (isBlockingNeighbourItemAt(x - 1, y, z)) {
                bNeighbours = true;
                abNeighbour[1] = true;
            }
            if (y < (World.MAP_HEIGHT - 1)) {
                if (isBlockingNeighbourItemAt(x - 1, y + 1, z)) {
                    bNeighbours = true;
                    abNeighbour[2] = true;
                }
            }
        }
        if (y > 0) {
            if (isBlockingNeighbourItemAt(x, y - 1, z)) {
                bNeighbours = true;
                abNeighbour[3] = true;
            }
        }
        if (y < (World.MAP_HEIGHT - 1)) {
            if (isBlockingNeighbourItemAt(x, y + 1, z)) {
                bNeighbours = true;
                abNeighbour[4] = true;
            }
        }
        if (x < (World.MAP_WIDTH - 1)) {
            if (y > 0) {
                if (isBlockingNeighbourItemAt(x + 1, y - 1, z)) {
                    bNeighbours = true;
                    abNeighbour[5] = true;
                }
            }
            if (isBlockingNeighbourItemAt(x + 1, y, z)) {
                bNeighbours = true;
                abNeighbour[6] = true;
            }
            if (y < (World.MAP_HEIGHT - 1)) {
                if (isBlockingNeighbourItemAt(x + 1, y + 1, z)) {
                    bNeighbours = true;
                    abNeighbour[7] = true;
                }
            }
        }

        if (bNeighbours) {
            int iRandom = Utils.getRandomBetween(0, 7);
            while (!abNeighbour[iRandom]) {
                iRandom = Utils.getRandomBetween(0, 7);
            }

            switch (iRandom) {
                case 0:
                    return Point3DShort.getPoolInstance(x - 1, y - 1, z);
                case 1:
                    return Point3DShort.getPoolInstance(x - 1, y, z);
                case 2:
                    return Point3DShort.getPoolInstance(x - 1, y + 1, z);
                case 3:
                    return Point3DShort.getPoolInstance(x, y - 1, z);
                case 4:
                    return Point3DShort.getPoolInstance(x, y + 1, z);
                case 5:
                    return Point3DShort.getPoolInstance(x + 1, y - 1, z);
                case 6:
                    return Point3DShort.getPoolInstance(x + 1, y, z);
                case 7:
                    return Point3DShort.getPoolInstance(x + 1, y + 1, z);
            }
        }

        // Abajo
        if (z < (World.MAP_DEPTH - 1)) {
            if (isBlockingNeighbourItemAt(x, y, z + 1) && Terrain.canGoDown(x, y, z)) {
                return Point3DShort.getPoolInstance(x, y, z + 1);
            }
        }

        return null;
    }

    /**
     * Indica si en la celda pasada hay un item bloqueador
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    private boolean isBlockingNeighbourItemAt(int x, int y, int z) {
        Cell cell = World.getCell(x, y, z);

        return cell.isDiscovered() && cell.getAstarZoneID() == -1 && cell.hasItem();
    }

    /**
     * Focus a citizen in the zone (to attack him). If no citizen he enters in
     * "DESTROY_BLOCKING_ITEMS" mode
     */
    private void doRage() {
        // Si tiene algún aldeano en la zona se lo pone de focus
        int iASZID = World.getCell(getCoordinates()).getAstarZoneID();

        // Buscamos aldeanos en la zona
        Citizen cit;
        boolean bFocusSet = false;
        for (int i = 0; i < World.getCitizenIDs().size(); i++) {
            cit = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i).intValue());
            if (cit != null) {
                if (World.getCell(cit.getCoordinates()).getAstarZoneID() == iASZID) {
                    getFocusData().setEntityID(cit.getID());
                    getFocusData().setEntityType(TYPE_CITIZEN);
                    setFighting (true);
                    bFocusSet = true;
                    break;
                }
            }
        }
        if (!bFocusSet) {
            // Buscamos soldiers
            for (int i = 0; i < World.getSoldierIDs().size(); i++) {
                cit = (Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i).intValue());
                if (cit != null) {
                    if (World.getCell(cit.getCoordinates()).getAstarZoneID() == iASZID) {
                        getFocusData().setEntityID(cit.getID());
                        getFocusData().setEntityType(TYPE_CITIZEN);
                        setFighting (true);
                        bFocusSet = true;
                        break;
                    }
                }
            }
        }

        if (!bFocusSet) {
            // No hay aldeano, empieza a romper items que bloqueen (items con zona -1)
            getHeroData().setHeroTask(new HeroTask(HeroTask.TASK_DESTROY_BLOCKING));
        }
    }

    /**
     * Hace lo necesario para empezar a irse
     */
    public void startLeaving() {
        // Miramos si tiene camino al punto por donde llegó
        int iASZID = World.getCell(getCoordinates()).getAstarZoneID();
        if (getHeroData().getStartingPoint() != null && World.getCell(getHeroData().getStartingPoint()).getAstarZoneID() == iASZID) {
            // Hay camino, lo movemos
            getHeroData().getHeroTask().setTaskID(HeroTask.TASK_LEAVING);
            setDestination(getHeroData().getStartingPoint());
        } else {
            // No hay salida por donde llegó, buscamos un punto
            Point3DShort p3dLeave = World.getRandomBorderPoint(iASZID);
            if (p3dLeave != null) {
                // Encontrado, se lo metemos
                getHeroData().setStartingPoint(p3dLeave);
                getHeroData().getHeroTask().setTaskID(HeroTask.TASK_LEAVING);
                setDestination(Point3DShort.getPoolInstance(p3dLeave));
            } else {
                // Oh, oh, no hay salida
                getHeroData().setHeroTask(new HeroTask(HeroTask.TASK_RAGE));

                MessagesPanel.addMessage(MessagesPanel.TYPE_HEROES, getCitizenData().getFullName() + Messages.getString("Hero.2"), ColorGL.RED, getCoordinates(), getID()); //$NON-NLS-1$
            }
        }
    }

    /**
     * Fills a contextual menú refering heros of a cell
     *
     * @param cell
     * @param sm
     */
    public static void fillMenu(Cell cell, SmartMenu sm) {
        Point3DShort p3d = cell.getCoordinates();
        if (cell.containsSpecificLiving(TYPE_HERO) != null) {
            Hero hero;
            LivingEntity le;
            LivingEntityManagerItem lemi;
            ArrayList<LivingEntity> alLivings = cell.getLivings();
            if (alLivings != null) {
                for (int i = 0; i < alLivings.size(); i++) {
                    le = alLivings.get(i);
                    lemi = LivingEntityManager.getItem(le.getIniHeader());
                    if (lemi.getType() == TYPE_HERO) {
                        hero = (Hero) le;
                        // Debug
                        if (TownsProperties.DEBUG_MODE) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Task ID: " + hero.getHeroData().getHeroTask().getTaskID(), null, null, null, null, p3d.toPoint3D())); //$NON-NLS-1$
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Exploring counter: " + hero.getHeroData().getExploringCounter(), null, null, null, null, p3d.toPoint3D())); //$NON-NLS-1$
                        }

                        // Información del héroe
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, hero.getCitizenData().getFullName(), null, null, null, null, null, Color.YELLOW));
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Hero.4") + hero.getHeroData().getLevel() + " (" + hero.getHeroData().getXp() + Messages.getString("Hero.5") + hero.getHeroData().getXpPCT() + "%)", null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, hero.getLivingEntityData().toString(), null, null, null));
                        MilitaryItem mi = hero.getEquippedData().getHead();
                        if (mi != null) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, mi.getTileName(), null, null, null, null, null, Color.GRAY));
                        }
                        mi = hero.getEquippedData().getBody();
                        if (mi != null) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, mi.getTileName(), null, null, null, null, null, Color.GRAY));
                        }
                        mi = hero.getEquippedData().getLegs();
                        if (mi != null) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, mi.getTileName(), null, null, null, null, null, Color.GRAY));
                        }
                        mi = hero.getEquippedData().getFeet();
                        if (mi != null) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, mi.getTileName(), null, null, null, null, null, Color.GRAY));
                        }
                        mi = hero.getEquippedData().getWeapon();
                        if (mi != null) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, mi.getTileName(), null, null, null, null, null, Color.GRAY));
                        }
                    }
                }
            }
        }
    }

    public void refreshCounters() {
        // Contador para las animaciones de comer/dormir
        getCitizenData().setBlinkAnimationTurns(getCitizenData().getBlinkAnimationTurns() + 1);
        if (getCitizenData().getBlinkAnimationTurns() > CitizenData.MAX_BLINK_ANIMATION_TURNS) {
            getCitizenData().setBlinkAnimationTurns(0);
        }

        // Contador de boost
        if (getCitizenData().getBoostCounter() > 0) {
            getCitizenData().setBoostCounter(getCitizenData().getBoostCounter() - 1);
        }
    }

    public boolean canChangeTask(boolean checkEat, boolean checkSleep) {
        int iTaskID = getHeroData().getHeroTask().getTaskID();

        if (checkEat && iTaskID == HeroTask.TASK_EATING) {
            return false;
        }
        if (checkSleep && iTaskID == HeroTask.TASK_SLEEPING) {
            return false;
        }

        return iTaskID != HeroTask.TASK_LEAVING && iTaskID != HeroTask.TASK_RAGE && iTaskID != HeroTask.TASK_DESTROY_BLOCKING;
    }

    /**
     * Este método es para notificar a una living entity que ha sido golpeado,
     * se le pasa el atacante
     *
     * @param le El atacante
     * @param bHitted Indica si le ha pegado o sólo lo ha intentado
     */
    public void hitted(LivingEntity le, boolean bHitted, int iDamage) {
        // super.hitted (le, bHitted); // El comentario es para que no responda a las hostias, ya se hace abajo si hace falta

        if (bHitted) {
            // Si tiene los puntos de vida bajos se va a la habitación personal (o taberna si no tiene) a descansar
            boolean bADescansar = false;
            boolean bYaEstabaDescansando = false;
            if ((getLivingEntityData().getHealthPointsMAXCurrent() / 3) > getLivingEntityData().getHealthPoints()) {
                Cell cell = World.getCell(getCoordinates());
                int iCurrentASZID = cell.getAstarZoneID();

                // Puntos de vida bajos y no hay enemigo a la vista, buscamos un sitio donde descansar
                if (getCitizenData().getZoneID() > 0) {
                    if (cell.getZoneID() != getCitizenData().getZoneID()) {
                        // Tiene zona y no está en ella, vamos a buscar un punto de guays
                        Zone zone = Zone.getZone(getCitizenData().getZoneID());
                        if (zone != null) {
                            Point3DShort p3d = Zone.getFreeSleepItemAtRandom(zone, iCurrentASZID);
                            if (p3d == null) {
                                p3d = Zone.getFreeSitItemAtRandom(zone, iCurrentASZID);
                                if (p3d == null) {
                                    p3d = Zone.getFreeCellAtRandom(zone, iCurrentASZID);
                                }
                            }

                            if (p3d != null) {
                                bADescansar = true;
                                setDestination(p3d);
                            }
                        } else {
                            // Raro, tiene zona pero no existe?? Se la quitamos
                            getCitizenData().setZoneID(0);
                        }
                    } else {
                        // Ya está en la zona, no hacemos nada, esperamos a que se recupere
                        bYaEstabaDescansando = true;
                    }
                }

                if (!bADescansar && !bYaEstabaDescansando) {
                    // Miramos que no esté en una DINING
                    if (cell.getZoneID() > 0) {
                        Zone zone = Zone.getZone(cell.getZoneID());
                        if (zone != null && ZoneManager.getItem(zone.getIniHeader()).getType() == ZoneManagerItem.TYPE_DINING) {
                            // Está en una dining, pacasa
                            bYaEstabaDescansando = true;
                        }
                    }

                    if (!bADescansar && !bYaEstabaDescansando) {
                        // No tiene zona ni está en una DINING, buscamos la DINING zone más cercana
                        ArrayList<Zone> zones = Game.getWorld().getZones();
                        Zone zone;
                        int iMaxDistance = Utils.MAX_DISTANCE;
                        Point3DShort p3dNear = null;
                        for (int i = 0; i < zones.size(); i++) {
                            zone = (Zone) zones.get(i);
                            if (ZoneManager.getItem(zone.getIniHeader()).getType() == ZoneManagerItem.TYPE_DINING) {
                                Point3DShort p3d = Zone.getFreeSleepItemAtRandom(zone, iCurrentASZID);
                                if (p3d == null) {
                                    p3d = Zone.getFreeSitItemAtRandom(zone, iCurrentASZID);
                                    if (p3d == null) {
                                        p3d = Zone.getFreeCellAtRandom(zone, iCurrentASZID);
                                    }
                                }

                                if (p3d != null) {
                                    // Encontrada, miramos distancia
                                    if (p3dNear == null) {
                                        p3dNear = Point3DShort.getPoolInstance(p3d);
                                        iMaxDistance = Utils.getDistance(getCoordinates(), p3d);
                                    } else {
                                        int iDistance = Utils.getDistance(getCoordinates(), p3d);
                                        if (iDistance < iMaxDistance) {
                                            p3dNear.setPoint(p3d);
                                            iMaxDistance = iDistance;
                                        }
                                    }
                                }
                            }
                        }

                        if (p3dNear != null) {
                            bADescansar = true;
                            setDestination(p3dNear);
                        }
                    }
                }
            } else {
                bYaEstabaDescansando = true; // Para que devuelva las hostias
            }

            if (bADescansar) {
                // El hero se va a descansar, le quitamos el focus y le pondremos el exploring counter alto para que no se vaya de marcha hasta al cabo de un rato
                getFocusData().setEntityID(-1);
                getFocusData().setEntityType (TYPE_UNKNOWN);
                setFighting (false);
                int turnosDescansando = getLivingEntityData().getHealthPointsMAXCurrent() - (getLivingEntityData().getHealthPointsMAXCurrent() / 3);
                if (turnosDescansando < (6 * World.TIME_MODIFIER_HOUR)) {
                    turnosDescansando = (6 * World.TIME_MODIFIER_HOUR);
                }
                getHeroData().setExploringCounter(turnosDescansando);
            }
            if (bYaEstabaDescansando) { // Esto es para que responda a ataques si ya estaba en la zona de descanso
                super.hitted(le, bHitted, iDamage);
            }
        }

        // En todos los casos, miramos si debe lanzar una skill con use=hitted
        checkAndUseSkills(SkillManagerItem.USE_HITTED);

        // Y llamamos a los amigos
        moveFriendsTo(getCoordinates());

        // Si está al borde de la muerte (10% HP) miramemos también si puede usar skills de tipo NEAR_DEATH
        if ((getLivingEntityData().getHealthPointsMAXCurrent() / 3) >= getLivingEntityData().getHealthPoints()) {
            checkAndUseSkills(SkillManagerItem.USE_NEAR_DEATH);
        }
    }

    public void delete() {
        delete(true);
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        citizenData = (CitizenData) in.readObject();
        heroData = (HeroData) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(citizenData);
        out.writeObject(heroData);
    }
}
