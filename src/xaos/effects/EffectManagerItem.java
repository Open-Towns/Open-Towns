package xaos.effects;

import java.util.ArrayList;

import xaos.data.EffectData;
import xaos.data.HateData;
import xaos.skills.SkillManagerItem;
import xaos.tiles.Tile;
import xaos.utils.Messages;
import xaos.utils.Utils;

/**
 * Clase de tipo "managerItem", no es la que se añade a las livings.
 */
public class EffectManagerItem {

    private String id;
    private String name;
    private Tile icon;
    private String damagePCT;
    private String defensePCT;
    private String attackPCT;
    private String attackSpeedPCT;
    private String DOT;
    private String healthPointsPCT;
    private String LOSPCT;
    private String speedPCT;
    private String lasts;
    private boolean attackAllies;
    private boolean removeTarget;
    private boolean flee;
    private String graphicChange;
    private String raiseDead;
    private String maxRaised;

    private String onHitPCT;
    private ArrayList<String> onHitEffects;
    private String onRangedHitPCT;
    private ArrayList<String> onRangedHitEffects;

    private ArrayList<String> afterEffects;

    private ArrayList<String> castEffects;
    private String castCooldown;
    private int castTrigger;
    private String castTargets;

    private ArrayList<String> effectsImmune;
    private ArrayList<String> effectsPrerequisite;

    private boolean messageWhenGain;
    private boolean messageWhenVanish;

    private String happy;

    public EffectData getEffectDataInstance() {
        EffectData effectData = new EffectData(id);

        effectData.setDamagePCT(Utils.launchDice(getDamagePCT()));
        effectData.setDefensePCT(Utils.launchDice(getDefensePCT()));
        effectData.setAttackPCT(Utils.launchDice(getAttackPCT()));
        effectData.setAttackSpeedPCT(Utils.launchDice(getAttackSpeedPCT()));
        effectData.setDOT(Utils.launchDice(getDOT()));
        effectData.setHealthPointsPCT(Utils.launchDice(getHealthPointsPCT()));
        effectData.setLOSPCT(Utils.launchDice(getLOSPCT()));
        effectData.setSpeedPCT(Utils.launchDice(getSpeedPCT()));
        effectData.setLasts(Utils.launchDice(getLasts()));
        effectData.setAttackAllies(isAttackAllies());
        effectData.setRemoveTarget(isRemoveTarget());
        effectData.setFlee(isFlee());
        effectData.setOnHitPCT(Utils.launchDice(getOnHitPCT()));
        effectData.setOnRangedHitPCT(Utils.launchDice(getOnRangedHitPCT()));
        if (getGraphicChange() != null) {
            effectData.setGraphicChange(new String(getGraphicChange()));
        }
        effectData.setCastCooldownMAX(Utils.launchDice(getCastCooldown()));
        effectData.setCastCooldown(0);
        effectData.setCastTrigger(getCastTrigger());
        effectData.setCastTargets(new HateData(getCastTargets()));
        effectData.setHappy(Utils.launchDice(getHappy()));

        return effectData;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIcon(String icon) {
        if (icon != null) {
            setIcon(new Tile(icon));
        }
    }

    public void setIcon(Tile icon) {
        this.icon = icon;
    }

    public Tile getIcon() {
        return icon;
    }

    public String getDamagePCT() {
        return damagePCT;
    }

    public void setDamagePCT(String damagePCT) {
        if (damagePCT == null || damagePCT.trim().toString().length() == 0) {
            this.damagePCT = "100"; //$NON-NLS-1$
        } else {
            this.damagePCT = damagePCT;
        }
    }

    public String getDefensePCT() {
        return defensePCT;
    }

    public void setDefensePCT(String defensePCT) {
        if (defensePCT == null || defensePCT.trim().toString().length() == 0) {
            this.defensePCT = "100"; //$NON-NLS-1$
        } else {
            this.defensePCT = defensePCT;
        }
    }

    public String getAttackPCT() {
        return attackPCT;
    }

    public void setAttackPCT(String attackPCT) {
        if (attackPCT == null || attackPCT.trim().toString().length() == 0) {
            this.attackPCT = "100"; //$NON-NLS-1$
        } else {
            this.attackPCT = attackPCT;
        }
    }

    public String getAttackSpeedPCT() {
        return attackSpeedPCT;
    }

    public void setAttackSpeedPCT(String attackSpeedPCT) {
        if (attackSpeedPCT == null || attackSpeedPCT.trim().toString().length() == 0) {
            this.attackSpeedPCT = "100"; //$NON-NLS-1$
        } else {
            this.attackSpeedPCT = attackSpeedPCT;
        }
    }

    public void setDOT(String sDOT) {
        this.DOT = sDOT;
    }

    public String getDOT() {
        return DOT;
    }

    public String getHealthPointsPCT() {
        return healthPointsPCT;
    }

    public void setHealthPointsPCT(String healthPointsPCT) {
        if (healthPointsPCT == null || healthPointsPCT.trim().toString().length() == 0) {
            this.healthPointsPCT = "100"; //$NON-NLS-1$
        } else {
            this.healthPointsPCT = healthPointsPCT;
        }
    }

    public void setLOSPCT(String lOSPCT) {
        if (lOSPCT == null || lOSPCT.trim().toString().length() == 0) {
            this.LOSPCT = "100"; //$NON-NLS-1$
        } else {
            this.LOSPCT = lOSPCT;
        }
    }

    public String getLOSPCT() {
        return LOSPCT;
    }

    public String getSpeedPCT() {
        return speedPCT;
    }

    public void setSpeedPCT(String speedPCT) {
        if (speedPCT == null || speedPCT.trim().toString().length() == 0) {
            this.speedPCT = "100"; //$NON-NLS-1$
        } else {
            this.speedPCT = speedPCT;
        }
    }

    public String getLasts() {
        return lasts;
    }

    public void setLasts(String lasts) {
        this.lasts = lasts;
    }

    public boolean isAttackAllies() {
        return attackAllies;
    }

    public void setAttackAllies(boolean attackAllies) {
        this.attackAllies = attackAllies;
    }

    public void setAttackAllies(String sAttackAllies) {
        setAttackAllies(Boolean.parseBoolean(sAttackAllies));
    }

    public void setRemoveTarget(boolean removeTarget) {
        this.removeTarget = removeTarget;
    }

    public void setRemoveTarget(String sRemoveTarget) {
        setRemoveTarget(Boolean.parseBoolean(sRemoveTarget));
    }

    public boolean isRemoveTarget() {
        return removeTarget;
    }

    public void setFlee(boolean flee) {
        this.flee = flee;
    }

    public void setFlee(String sFlee) {
        setFlee(Boolean.parseBoolean(sFlee));
    }

    public boolean isFlee() {
        return flee;
    }

    public String getGraphicChange() {
        return graphicChange;
    }

    public void setGraphicChange(String graphicChange) {
        this.graphicChange = graphicChange;
    }

    public String getRaiseDead() {
        return raiseDead;
    }

    public void setRaiseDead(String raiseDead) {
        this.raiseDead = raiseDead;
    }

    public void setMaxRaised(String maxRaised) {
        this.maxRaised = maxRaised;
    }

    public String getMaxRaised() {
        return maxRaised;
    }

    public void setOnHitPCT(String onHitPCT) {
        if (onHitPCT == null || onHitPCT.trim().toString().length() == 0) {
            this.onHitPCT = "0"; //$NON-NLS-1$
        } else {
            this.onHitPCT = onHitPCT;
        }
    }

    public String getOnHitPCT() {
        return onHitPCT;
    }

    public void setOnHitEffects(String sOnHitEffects) {
        setOnHitEffects(Utils.getArray(sOnHitEffects));
    }

    public void setOnHitEffects(ArrayList<String> onHitEffects) {
        this.onHitEffects = onHitEffects;
    }

    public ArrayList<String> getOnHitEffects() {
        return onHitEffects;
    }

    public void setOnRangedHitPCT(String onRangedHitPCT) {
        if (onRangedHitPCT == null || onRangedHitPCT.trim().toString().length() == 0) {
            this.onRangedHitPCT = "0"; //$NON-NLS-1$
        } else {
            this.onRangedHitPCT = onRangedHitPCT;
        }
    }

    public String getOnRangedHitPCT() {
        return onRangedHitPCT;
    }

    public void setOnRangedHitEffects(String sOnRangedHitEffects) {
        setOnRangedHitEffects(Utils.getArray(sOnRangedHitEffects));
    }

    public void setOnRangedHitEffects(ArrayList<String> onRangedHitEffects) {
        this.onRangedHitEffects = onRangedHitEffects;
    }

    public ArrayList<String> getOnRangedHitEffects() {
        return onRangedHitEffects;
    }

    public void setAfterEffects(ArrayList<String> afterEffects) {
        this.afterEffects = afterEffects;
    }

    public void setAfterEffects(String sAfterEffects) {
        setAfterEffects(Utils.getArray(sAfterEffects));
    }

    public ArrayList<String> getAfterEffects() {
        return afterEffects;
    }

    public void setCastEffects(ArrayList<String> castEffects) {
        this.castEffects = castEffects;
    }

    public void setCastEffects(String sCastEffects) {
        setCastEffects(Utils.getArray(sCastEffects));
    }

    public ArrayList<String> getCastEffects() {
        return castEffects;
    }

    public void setCastCooldown(String castCooldown) {
        this.castCooldown = castCooldown;
    }

    public String getCastCooldown() {
        return castCooldown;
    }

    public void setCastTrigger(int iCastTrigger) {
        this.castTrigger = iCastTrigger;
    }

    public void setCastTrigger(String sCastTrigger) throws Exception {
        if (sCastTrigger == null || sCastTrigger.length() == 0) {
            setCastTrigger(SkillManagerItem.USE_UNKNOWN);
        } else {
            if (sCastTrigger.equalsIgnoreCase(SkillManagerItem.USE_STR_ALWAYS)) {
                setCastTrigger(SkillManagerItem.USE_ALWAYS);
            } else if (sCastTrigger.equalsIgnoreCase(SkillManagerItem.USE_STR_HITTED)) {
                setCastTrigger(SkillManagerItem.USE_HITTED);
            } else if (sCastTrigger.equalsIgnoreCase(SkillManagerItem.USE_STR_ENEMIES_IN_LOS)) {
                setCastTrigger(SkillManagerItem.USE_ENEMIES_IN_LOS);
            } else if (sCastTrigger.equalsIgnoreCase(SkillManagerItem.USE_STR_NEAR_DEATH)) {
                setCastTrigger(SkillManagerItem.USE_NEAR_DEATH);
            } else if (sCastTrigger.equalsIgnoreCase(SkillManagerItem.USE_STR_NOT_MAX_HP)) {
                setCastTrigger(SkillManagerItem.USE_NOT_MAX_HP);
            } else {
                throw new Exception(Messages.getString("EffectManager.6") + sCastTrigger + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    public int getCastTrigger() {
        return castTrigger;
    }

    public void setCastTargets(String sCastTargets) {
        this.castTargets = sCastTargets;
    }

    public String getCastTargets() {
        return castTargets;
    }

    public void setEffectsImmune(ArrayList<String> effectsImmune) {
        this.effectsImmune = effectsImmune;
    }

    public void setEffectsImmune(String sEffectsImmune) throws Exception {
        setEffectsImmune(Utils.getArray(sEffectsImmune));
    }

    public ArrayList<String> getEffectsImmune() {
        return effectsImmune;
    }

    public void setEffectsPrerequisite(ArrayList<String> effectsPrerequisite) {
        this.effectsPrerequisite = effectsPrerequisite;
    }

    public void setEffectsPrerequisite(String sEffectsPrerequisite) throws Exception {
        setEffectsPrerequisite(Utils.getArray(sEffectsPrerequisite));
    }

    public ArrayList<String> getEffectsPrerequisite() {
        return effectsPrerequisite;
    }

    public void setMessageWhenGain(boolean messageWhenGain) {
        this.messageWhenGain = messageWhenGain;
    }

    public void setMessageWhenGain(String sMessageWhenGain) {
        if (sMessageWhenGain != null && sMessageWhenGain.equals("false")) {
            setMessageWhenGain(false);
        } else {
            setMessageWhenGain(true);
        }
    }

    public boolean isMessageWhenGain() {
        return messageWhenGain;
    }

    public void setMessageWhenVanish(boolean messageWhenVanish) {
        this.messageWhenVanish = messageWhenVanish;
    }

    public void setMessageWhenVanish(String sMessageWhenVanish) {
        if (sMessageWhenVanish != null && sMessageWhenVanish.equals("false")) {
            setMessageWhenVanish(false);
        } else {
            setMessageWhenVanish(true);
        }
    }

    public boolean isMessageWhenVanish() {
        return messageWhenVanish;
    }

    public void setHappy(String happy) {
        this.happy = happy;
    }

    public String getHappy() {
        return happy;
    }
}
