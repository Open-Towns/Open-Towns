package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MessagesPanel;
import xaos.skills.SkillManager;
import xaos.skills.SkillManagerItem;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.tiles.entities.living.heroes.HeroBehaviour;
import xaos.tiles.entities.living.heroes.HeroManager;
import xaos.tiles.entities.living.heroes.HeroSkills;
import xaos.tiles.entities.living.heroes.HeroTask;
import xaos.utils.ColorGL;
import xaos.utils.Messages;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;

public class HeroData implements Externalizable {

    private static final long serialVersionUID = 3057389473429112206L;

    private final static int MIN_TURNS_TO_STAY = World.TIME_MODIFIER_DAY;
    public final static int TURNS_BETWEEN_EXPLORE = World.TIME_MODIFIER_HOUR;

    private HeroTask heroTask; // Current task
    private HeroBehaviour heroBehaviour; // Behaviour
    private Point3DShort startingPoint; // Point where the hero starts to come
    private int minTurnsToStay; // Minimum turns that hero will stay on the Town

    // Behaviour
    private int currentBehaviourID; // Current behaviour ID (idle/explore/...)
    private int turnsToNextBehaviour; // Turns left to change his behaviour(idle/explore/...)

    // Idle
    public final static int IDLE_NONE = 0;
    public final static int IDLE_STEAL = 1;
    public final static int IDLE_SOCIAL = 2;
    private int idleCounter;
    private int idleTask;

    // Exploring
    private int exploringCounter; // Turns to change between the real exploring and the random moves

    // Level/XP
    private int level; // Nivel
    private int xp; // Experiencia

    // Skills adquiridas
    private ArrayList<SkillData> skills;

    // Friendships (living IDs)
    private ArrayList<Integer> friendships;

    public HeroData() {
    }

    public HeroData(LivingEntityManagerItem lemi) {
        setStartingPoint(Point3DShort.getPoolInstance(0, 0, 0));
        setHeroTask(new HeroTask(HeroTask.TASK_NO_TASK));
        setHeroBehaviour(HeroManager.getBehaviour(lemi.getHeroBehaviour()));
        setMinTurnsToStay(MIN_TURNS_TO_STAY);
        setCurrentBehaviourID(HeroBehaviour.BEHAVIOUR_ID_NONE);
        setTurnsToNextBehaviour(0); // This way the first hero turn will change the behavior to the first one
        setExploringCounter(0);
        setLevel(0);
        setXp(0);
        setSkills(new ArrayList<SkillData>());
        setFriendships(new ArrayList<Integer>());
    }

    public void setHeroTask(HeroTask heroTask) {
        this.heroTask = heroTask;
    }

    public HeroTask getHeroTask() {
        return heroTask;
    }

    public void setHeroBehaviour(HeroBehaviour heroBehaviour) {
        this.heroBehaviour = heroBehaviour;
    }

    public HeroBehaviour getHeroBehaviour() {
        return heroBehaviour;
    }

    public void setStartingPoint(Point3DShort startingPoint) {
        this.startingPoint = startingPoint;
    }

    public Point3DShort getStartingPoint() {
        return startingPoint;
    }

    public void setMinTurnsToStay(int minTurnsToStay) {
        this.minTurnsToStay = minTurnsToStay;
    }

    public int getMinTurnsToStay() {
        return minTurnsToStay;
    }

    public int getCurrentBehaviourID() {
        return currentBehaviourID;
    }

    public void setCurrentBehaviourID(int currentBehaviourID) {
        this.currentBehaviourID = currentBehaviourID;
    }

    public int getTurnsToNextBehaviour() {
        return turnsToNextBehaviour;
    }

    public void setTurnsToNextBehaviour(int turnsToNextBehaviour) {
        this.turnsToNextBehaviour = turnsToNextBehaviour;
    }

    /**
     * Resta 1 a los turnos de behaviour, si llegan a 0 devuelve true para poder
     * cambiar la tarea
     *
     * @return
     */
    public boolean updateBehaviour() {
        setTurnsToNextBehaviour(getTurnsToNextBehaviour() - 1);
        if (getTurnsToNextBehaviour() <= 0) {
            setTurnsToNextBehaviour(0);
            return true;
        }

        return false;
    }

    /**
     * @param idleCounter the idleCounter to set
     */
    public void setIdleCounter(int idleCounter) {
        this.idleCounter = idleCounter;
    }

    /**
     * @return the idleCounter
     */
    public int getIdleCounter() {
        return idleCounter;
    }

    /**
     * @param idleTask the idleTask to set
     */
    public void setIdleTask(int idleTask) {
        this.idleTask = idleTask;
    }

    /**
     * @return the idleTask
     */
    public int getIdleTask() {
        return idleTask;
    }

    public void setExploringCounter(int exploringCounter) {
        this.exploringCounter = exploringCounter;
    }

    public int getExploringCounter() {
        return exploringCounter;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getXp() {
        return xp;
    }

    /**
     * Devuelve la experiencia que lleva en el nivel (en %)
     *
     * @return
     */
    public int getXpPCT() {
        if (getLevel() > 0) {
            return ((getXp() % 300)) / 3;
        }

        return 0;
    }

    /**
     * Mira si tiene que subir de nivel (o niveles), en ese caso lo hace y
     * devuelve true. También añade las skills que se obtienen en ese nivel
     *
     * @return
     */
    public boolean checkAndUpdateLevelUp(Hero hero, boolean verboseSkills) {
        if (getLevel() < 50 && getXp() >= (300 * getLevel())) {
            while (getLevel() < 50 && getXp() >= (300 * getLevel())) {
                setLevel(getLevel() + 1);

                HeroSkills hSkills = HeroManager.getSkills(LivingEntityManager.getItem(hero.getIniHeader()).getHeroSkills());
                if (hSkills != null) {
                    ArrayList<String> newSkills = hSkills.getSkillsWhenReachLevel(getLevel());
                    if (newSkills != null) {
                        for (int i = 0; i < newSkills.size(); i++) {
                            SkillManagerItem smi = SkillManager.getItem(newSkills.get(i));
                            SkillData sd = new SkillData();
                            sd.setSkillID(newSkills.get(i));
                            sd.setCoolDown(Utils.launchDice(smi.getCoolDown()));
                            sd.setUse(smi.getUse());
                            getSkills().add(sd);

                            if (verboseSkills) {
                                MessagesPanel.addMessage(MessagesPanel.TYPE_HEROES, hero.getCitizenData().getFullName() + Messages.getString("HeroData.0") + SkillManager.getItem(newSkills.get(i)).getName() + "]", ColorGL.GREEN, hero.getCoordinates(), hero.getID()); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                }
            }
            return true;
        }

        return false;
    }

    public void setSkills(ArrayList<SkillData> skills) {
        this.skills = skills;
    }

    public ArrayList<SkillData> getSkills() {
        return skills;
    }

    public void setFriendships(ArrayList<Integer> friendships) {
        this.friendships = friendships;
    }

    public ArrayList<Integer> getFriendships() {
        return friendships;
    }

    public static String getFriendshipString(Hero hero) {
        if (hero.getHeroData().getFriendships().size() > 0) {
            StringBuffer sBuffer = new StringBuffer(Messages.getString("HeroData.1") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
            ArrayList<Integer> alFriends = hero.getHeroData().getFriendships();
            LivingEntity heroFriend;
            for (int h = 0; h < (alFriends.size() - 1); h++) {
                heroFriend = World.getLivingEntityByID(alFriends.get(h).intValue());
                if (heroFriend != null) {
                    sBuffer.append(heroFriend.getLivingEntityData().getName());
                    sBuffer.append(", "); //$NON-NLS-1$
                }
            }
            heroFriend = (Hero) World.getLivingEntityByID(alFriends.get(alFriends.size() - 1).intValue());
            if (heroFriend != null) {
                sBuffer.append(heroFriend.getLivingEntityData().getName());
            }
            return sBuffer.toString();
        }

        return null;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        heroTask = (HeroTask) in.readObject();
        heroBehaviour = (HeroBehaviour) in.readObject();
        startingPoint = (Point3DShort) in.readObject();
        minTurnsToStay = in.readInt();
        currentBehaviourID = in.readInt();
        turnsToNextBehaviour = in.readInt();
        idleCounter = in.readInt();
        idleTask = in.readInt();
        exploringCounter = in.readInt();
        level = in.readInt();
        xp = in.readInt();
        skills = (ArrayList<SkillData>) in.readObject();

        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V13) {
            friendships = (ArrayList<Integer>) in.readObject();
        } else {
            friendships = new ArrayList<Integer>();
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(heroTask);
        out.writeObject(heroBehaviour);
        out.writeObject(startingPoint);
        out.writeInt(minTurnsToStay);
        out.writeInt(currentBehaviourID);
        out.writeInt(turnsToNextBehaviour);
        out.writeInt(idleCounter);
        out.writeInt(idleTask);
        out.writeInt(exploringCounter);
        out.writeInt(level);
        out.writeInt(xp);
        out.writeObject(skills);
        out.writeObject(friendships);
    }
}
