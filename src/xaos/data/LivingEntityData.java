package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.effects.EffectManager;
import xaos.effects.EffectManagerItem;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.utils.Messages;

public class LivingEntityData implements Externalizable {

    private static final long serialVersionUID = -7658874688434177141L;

    private transient String name;

    private int healthPoints; // Éste va aparte pq los items no lo modifican. Modifican el healthPointsMAX.

    private int attackBase;
    private int attackSpeedBase;
    private int defenseBase;
    private int healthPointsMAXBase;
    private int damageBase;
    private int LOSBase;
    private int movePCTBase;
    private int walkSpeedBase;

    private int moral;
    private int followTurnsCounter;

    private ArrayList<EffectData> effects;

    private transient int attackCurrent;
    private transient int attackSpeedCurrent;
    private transient int defenseCurrent;
    private transient int healthPointsMAXCurrent;
    private transient int damageCurrent;
    private transient int LOSCurrent;
    private transient int movePCTCurrent;
    private transient int walkSpeedCurrent;

    public LivingEntityData() {
        setEffects(new ArrayList<EffectData>());
    }

    public void refreshTransients(LivingEntityManagerItem lemi) {
        setName(lemi.getName());
        setAttackCurrent(getAttackBase());
        setAttackSpeedCurrent(getAttackSpeedBase());
        setDefenseCurrent(getDefenseBase());
        setHealthPointsMAXCurrent(getHealthPointsMAXBase());
        setDamageCurrent(getDamageBase());
        setLOSCurrent(getLOSBase());
        setMovePCTCurrent(getMovePCTBase());
        setWalkSpeedCurrent(getWalkSpeedBase());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getAttackBase() {
        return attackBase;
    }

    public void setAttackBase(int attackBase) {
        this.attackBase = attackBase;
        setAttackCurrent(attackBase);
    }

    public void setAttackSpeedBase(int attackSpeedBase) {
        this.attackSpeedBase = attackSpeedBase;
        setAttackSpeedCurrent(attackSpeedBase);
    }

    public int getAttackSpeedBase() {
        return attackSpeedBase;
    }

    public int getDefenseBase() {
        return defenseBase;
    }

    public void setDefenseBase(int defenseBase) {
        this.defenseBase = defenseBase;
        setDefenseCurrent(defenseBase);
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public int getHealthPointsMAXBase() {
        return healthPointsMAXBase;
    }

    public void setDamageBase(int damageBase) {
        this.damageBase = damageBase;
        setDamageCurrent(damageBase);
    }

    public int getDamageBase() {
        return damageBase;
    }

    public void setHealthPointsMAXBase(int healthPointsMAXBase) {
        this.healthPointsMAXBase = healthPointsMAXBase;
        setHealthPointsMAXCurrent(healthPointsMAXBase);
    }

    public int getLOSBase() {
        return LOSBase;
    }

    public void setLOSBase(int lOSBase) {
        LOSBase = lOSBase;
        setLOSCurrent(lOSBase);
    }

    public int getMovePCTBase() {
        return movePCTBase;
    }

    public void setMovePCTBase(int movePCTBase) {
        this.movePCTBase = movePCTBase;
        setMovePCTCurrent(movePCTBase);
    }

    public void setWalkSpeedBase(int walkSpeedBase) {
        this.walkSpeedBase = walkSpeedBase;
        setWalkSpeedCurrent(walkSpeedBase);
    }

    public int getWalkSpeedBase() {
        return walkSpeedBase;
    }

    /**
     * @param moral the moral to set
     */
    public void setMoral(int moral) {
        this.moral = moral;
    }

    /**
     * @return the moral
     */
    public int getMoral() {
        return moral;
    }

    public void setFollowTurnsCounter(int followTurnsCounter) {
        this.followTurnsCounter = followTurnsCounter;
    }

    public int getFollowTurnsCounter() {
        return followTurnsCounter;
    }

    /**
     * @param effects the effects to set
     */
    public void setEffects(ArrayList<EffectData> effects) {
        this.effects = effects;
    }

    /**
     * @return the effects
     */
    public ArrayList<EffectData> getEffects() {
        return effects;
    }

    public int getAttackCurrent() {
        return attackCurrent;
    }

    public void setAttackCurrent(int attackCurrent) {
        this.attackCurrent = attackCurrent;
    }

    public void setAttackSpeedCurrent(int attackSpeedCurrent) {
        this.attackSpeedCurrent = attackSpeedCurrent;
    }

    public int getAttackSpeedCurrent() {
        return attackSpeedCurrent;
    }

    public int getDefenseCurrent() {
        return defenseCurrent;
    }

    public void setDefenseCurrent(int defenseCurrent) {
        this.defenseCurrent = defenseCurrent;
    }

    public int getHealthPointsMAXCurrent() {
        return healthPointsMAXCurrent;
    }

    public void setHealthPointsMAXCurrent(int healthPointsMAXCurrent) {
        this.healthPointsMAXCurrent = healthPointsMAXCurrent;
    }

    public int getDamageCurrent() {
        return damageCurrent;
    }

    public void setDamageCurrent(int damageCurrent) {
        this.damageCurrent = damageCurrent;
    }

    public int getLOSCurrent() {
        return LOSCurrent;
    }

    public void setLOSCurrent(int lOSCurrent) {
        LOSCurrent = lOSCurrent;
    }

    public int getMovePCTCurrent() {
        return movePCTCurrent;
    }

    public void setMovePCTCurrent(int movePCTCurrent) {
        this.movePCTCurrent = movePCTCurrent;
    }

    public void setWalkSpeedCurrent(int walkSpeedCurrent) {
        this.walkSpeedCurrent = walkSpeedCurrent;
    }

    public int getWalkSpeedCurrent() {
        return walkSpeedCurrent;
    }

    public String getHealthStatus() {
        // Ésto nos dará un número entre 0 (casi muerto) y 10 (vida máxima)
        int iStatus;
        if (getHealthPointsMAXCurrent() <= 0) {
            iStatus = 0;
        } else {
            iStatus = ((getHealthPoints() * 100) / getHealthPointsMAXCurrent()) / 10;
        }

        if (iStatus < 0) {
            return Messages.getString("LivingEntityData.8"); //$NON-NLS-1$
        }

        switch (iStatus) {
            case 0:
            case 1:
                return Messages.getString("LivingEntityData.0"); //$NON-NLS-1$
            case 2:
                return Messages.getString("LivingEntityData.1"); //$NON-NLS-1$
            case 3:
                return Messages.getString("LivingEntityData.2"); //$NON-NLS-1$
            case 4:
                return Messages.getString("LivingEntityData.3"); //$NON-NLS-1$
            case 5:
            case 6:
                return Messages.getString("LivingEntityData.4"); //$NON-NLS-1$
            case 7:
                return Messages.getString("LivingEntityData.5"); //$NON-NLS-1$
            case 8:
                return Messages.getString("LivingEntityData.6"); //$NON-NLS-1$
            case 9:
            default:
                return Messages.getString("LivingEntityData.7"); //$NON-NLS-1$
        }
    }

    /**
     * Setea los modificadores según lo que lleve puesto
     *
     * @param mi
     * @param level Se le pasa 0 o el nivel del héroe
     */
    public void setModifiers(LivingEntity le) {
        int level = 0;
        LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
        if (lemi.getType() == LivingEntity.TYPE_HERO) {
            if (le instanceof Hero) { // Per evitar problemes amb el mod del bluesteel
                Hero hero = (Hero) le;
                if (hero.getHeroData() != null) {
                    level = hero.getHeroData().getLevel();
                }
            }
        } else if (lemi.getType() == LivingEntity.TYPE_CITIZEN) {
            if (le instanceof Citizen) { // Per evitar problemes amb el mod del bluesteel
                Citizen citizen = (Citizen) le;
                if (citizen.getSoldierData() != null && citizen.getSoldierData().isSoldier()) {
                    level = citizen.getSoldierData().getLevel();
                }
            }
        }

        // Reseteamos
        setAttackCurrent(getAttackBase() + ((level * getAttackBase()) / 32));
        setAttackSpeedCurrent(getAttackSpeedBase() + ((level * getAttackSpeedBase()) / 32));
        setDefenseCurrent(getDefenseBase() + ((level * getDefenseBase()) / 32));
        setHealthPointsMAXCurrent(getHealthPointsMAXBase() + ((level * getHealthPointsMAXBase()) / 32));
        setDamageCurrent(getDamageBase() + ((level * getDamageBase()) / 32));
        setLOSCurrent(getLOSBase());
        setMovePCTCurrent(getMovePCTBase());
        setWalkSpeedCurrent(getWalkSpeedBase());

        // Equipo
        EquippedData equippedData = le.getEquippedData();
        if (equippedData != null) {
            setModifiers(equippedData.getHead(), le);
            setModifiers(equippedData.getBody(), le);
            setModifiers(equippedData.getLegs(), le);
            setModifiers(equippedData.getFeet(), le);
            setModifiers(equippedData.getWeapon(), le);
        }

        // Efectos
        ArrayList<EffectData> effects = le.getLivingEntityData().getEffects();
        EffectData effect;
        for (int i = 0; i < effects.size(); i++) {
            effect = effects.get(i);
            if (effect.getAttackPCT() != 100) {
                setAttackCurrent((getAttackCurrent() * effect.getAttackPCT()) / 100);
            }
            if (effect.getDefensePCT() != 100) {
                setDefenseCurrent((getDefenseCurrent() * effect.getDefensePCT()) / 100);
            }
            if (effect.getHealthPointsPCT() != 100) {
                setHealthPointsMAXCurrent((getHealthPointsMAXCurrent() * effect.getHealthPointsPCT()) / 100);
            }
            if (effect.getLOSPCT() != 100) {
                setLOSCurrent((getLOSCurrent() * effect.getLOSPCT()) / 100);
            }
            if (effect.getDamagePCT() != 100) {
                setDamageCurrent((getDamageCurrent() * effect.getDamagePCT()) / 100);
            }
        }

        // Current HPs
        if (getHealthPoints() > getHealthPointsMAXCurrent()) {
            setHealthPoints(getHealthPointsMAXCurrent());
        }
    }

    /**
     * Setea los modificadores según un objeto dado, mete efectos si hace falta
     * pero no les activa los modificadores
     *
     * @param mi Objeto
     */
    private void setModifiers(MilitaryItem mi, LivingEntity le) {
        if (mi != null) {
            setAttackCurrent(getAttackCurrent() + mi.getAttackModifier());
            setAttackSpeedCurrent(getAttackSpeedCurrent() + mi.getAttackSpeedModifier());
            setDefenseCurrent(getDefenseCurrent() + mi.getDefenseModifier());
            setHealthPointsMAXCurrent(getHealthPointsMAXCurrent() + mi.getHealthModifier());
            setDamageCurrent(getDamageCurrent() + mi.getDamageModifier());
            setLOSCurrent(getLOSCurrent() + mi.getLOSModifier());
            setMovePCTCurrent(getMovePCTCurrent() + mi.getMovePCTModifier());
            setWalkSpeedCurrent(getWalkSpeedCurrent() + mi.getWalkSpeedModifier());

            // Wear effects
            ItemManagerItem imi = ItemManager.getItem(mi.getIniHeader());
            if (imi.getWearEffects() != null && imi.getWearEffects().size() > 0) {
                // A meter efectos
                EffectManagerItem emi;
                for (int i = 0, n = imi.getWearEffects().size(); i < n; i++) {
                    emi = EffectManager.getItem(imi.getWearEffects().get(i));
                    if (emi != null) { // Debería
                        le.addEffect(emi, false);
                    }
                }
            }
        }
    }

    public String toString() {
        return Messages.getString("LivingEntityData.10") + getHealthPoints() + "/" + getHealthPointsMAXCurrent() + Messages.getString("LivingEntityData.13") + getAttackCurrent() + Messages.getString("LivingEntityData.14") + getDefenseCurrent() + Messages.getString("LivingEntityData.15") + getDamageCurrent(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        healthPoints = in.readInt();
        attackBase = in.readInt();
        attackSpeedBase = in.readInt();
        defenseBase = in.readInt();
        healthPointsMAXBase = in.readInt();
        damageBase = in.readInt();
        LOSBase = in.readInt();
        movePCTBase = in.readInt();
        walkSpeedBase = in.readInt();
        moral = in.readInt();
        followTurnsCounter = in.readInt();
        effects = (ArrayList<EffectData>) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(healthPoints);
        out.writeInt(attackBase);
        out.writeInt(attackSpeedBase);
        out.writeInt(defenseBase);
        out.writeInt(healthPointsMAXBase);
        out.writeInt(damageBase);
        out.writeInt(LOSBase);
        out.writeInt(movePCTBase);
        out.writeInt(walkSpeedBase);
        out.writeInt(moral);
        out.writeInt(followTurnsCounter);
        out.writeObject(effects);
    }
}
