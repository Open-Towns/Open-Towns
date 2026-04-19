package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.main.Game;
import xaos.skills.SkillManagerItem;

public class EffectData implements Externalizable {

    private static final long serialVersionUID = -332797600116043961L;

    private String effectID;

    private int damagePCT;
    private int defensePCT;
    private int attackPCT;
    private int attackSpeedPCT;
    private int healthPointsPCT;
    private int LOSPCT;
    private int speedPCT;
    private int lasts;
    private String graphicChange;
    private boolean attackAllies;
    private boolean removeTarget;
    private boolean flee;

    private int DOT;
    private int onHitPCT;
    private int onRangedHitPCT;

    private int castCooldown;
    private int castCooldownMAX;
    private int castTrigger;
    private HateData castTargets;

    private int happy;

    ;

	public EffectData() {
    }

    public EffectData(String effectID) {
        setEffectID(effectID);

        setDamagePCT(100);
        setDefensePCT(100);
        setAttackPCT(100);
        setAttackSpeedPCT(100);
        setHealthPointsPCT(100);
        setLOSPCT(100);
        setSpeedPCT(100);
        setLasts(0);
        setAttackAllies(false);
        setDOT(0);
        setOnHitPCT(0);
        setOnRangedHitPCT(0);
        setCastCooldown(0);
        setCastCooldownMAX(0);
        setCastTrigger(SkillManagerItem.USE_UNKNOWN);
        setCastTargets(new HateData(null));
        setHappy(0);
    }

    public void refreshTargets(String sData) {
        if (castTargets == null) {
            castTargets = new HateData(sData);
        }
    }

    public void setEffectID(String effectID) {
        this.effectID = effectID;
    }

    public String getEffectID() {
        return effectID;
    }

    public int getDamagePCT() {
        return damagePCT;
    }

    public void setDamagePCT(int damagePCT) {
        this.damagePCT = damagePCT;
    }

    public int getDefensePCT() {
        return defensePCT;
    }

    public void setDefensePCT(int defensePCT) {
        this.defensePCT = defensePCT;
    }

    public int getAttackPCT() {
        return attackPCT;
    }

    public void setAttackPCT(int attackPCT) {
        this.attackPCT = attackPCT;
    }

    public void setAttackSpeedPCT(int attackSpeedPCT) {
        this.attackSpeedPCT = attackSpeedPCT;
    }

    public int getAttackSpeedPCT() {
        return attackSpeedPCT;
    }

    public int getHealthPointsPCT() {
        return healthPointsPCT;
    }

    public void setHealthPointsPCT(int healthPointsPCT) {
        this.healthPointsPCT = healthPointsPCT;
    }

    public void setLOSPCT(int lOSPCT) {
        LOSPCT = lOSPCT;
    }

    public int getLOSPCT() {
        return LOSPCT;
    }

    public void setSpeedPCT(int speedPCT) {
        this.speedPCT = speedPCT;
    }

    public int getSpeedPCT() {
        return speedPCT;
    }

    public int getLasts() {
        return lasts;
    }

    public void setLasts(int lasts) {
        this.lasts = lasts;
    }

    public void setGraphicChange(String graphicChange) {
        this.graphicChange = graphicChange;
    }

    public String getGraphicChange() {
        return graphicChange;
    }

    public boolean isGraphicChange() {
        return graphicChange != null && graphicChange.length() > 0;
    }

    public void setAttackAllies(boolean attackAllies) {
        this.attackAllies = attackAllies;
    }

    public boolean isAttackAllies() {
        return attackAllies;
    }

    public void setRemoveTarget(boolean removeTarget) {
        this.removeTarget = removeTarget;
    }

    public boolean isRemoveTarget() {
        return removeTarget;
    }

    public void setFlee(boolean flee) {
        this.flee = flee;
    }

    public boolean isFlee() {
        return flee;
    }

    public void setDOT(int dOT) {
        DOT = dOT;
    }

    public int getDOT() {
        return DOT;
    }

    public void setOnHitPCT(int onHitPCT) {
        this.onHitPCT = onHitPCT;
    }

    public int getOnHitPCT() {
        return onHitPCT;
    }

    public void setOnRangedHitPCT(int onRangedHitPCT) {
        this.onRangedHitPCT = onRangedHitPCT;
    }

    public int getOnRangedHitPCT() {
        return onRangedHitPCT;
    }

    public void setCastCooldown(int castCooldown) {
        this.castCooldown = castCooldown;
    }

    public int getCastCooldown() {
        return castCooldown;
    }

    public void setCastCooldownMAX(int castCooldownMAX) {
        this.castCooldownMAX = castCooldownMAX;
    }

    public int getCastCooldownMAX() {
        return castCooldownMAX;
    }

    public void setCastTrigger(int castTrigger) {
        this.castTrigger = castTrigger;
    }

    public int getCastTrigger() {
        return castTrigger;
    }

    public void setCastTargets(HateData castTargets) {
        this.castTargets = castTargets;
    }

    public HateData getCastTargets() {
        return castTargets;
    }

    public void setHappy(int happy) {
        this.happy = happy;
    }

    public int getHappy() {
        return happy;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        effectID = (String) in.readObject();
        damagePCT = in.readInt();
        defensePCT = in.readInt();
        attackPCT = in.readInt();
        attackSpeedPCT = in.readInt();
        healthPointsPCT = in.readInt();
        speedPCT = in.readInt();
        lasts = in.readInt();
        graphicChange = (String) in.readObject();
        attackAllies = in.readBoolean();
        removeTarget = in.readBoolean();
        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V12) {
            flee = in.readBoolean();
        } else {
            flee = false;
        }
        DOT = in.readInt();
        onHitPCT = in.readInt();
        onRangedHitPCT = in.readInt();
        castCooldown = in.readInt();
        castCooldownMAX = in.readInt();
        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V12) {
            castTrigger = in.readInt();
        } else {
            castTrigger = SkillManagerItem.USE_UNKNOWN;
        }

        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V14) {
            LOSPCT = 100;
        } else {
            LOSPCT = 100;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(effectID);
        out.writeInt(damagePCT);
        out.writeInt(defensePCT);
        out.writeInt(attackPCT);
        out.writeInt(attackSpeedPCT);
        out.writeInt(healthPointsPCT);
        out.writeInt(speedPCT);
        out.writeInt(lasts);
        out.writeObject(graphicChange);
        out.writeBoolean(attackAllies);
        out.writeBoolean(removeTarget);
        out.writeBoolean(flee);
        out.writeInt(DOT);
        out.writeInt(onHitPCT);
        out.writeInt(onRangedHitPCT);
        out.writeInt(castCooldown);
        out.writeInt(castCooldownMAX);
        out.writeInt(castTrigger);
        out.writeInt(LOSPCT);
    }
}
