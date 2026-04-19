package xaos.dungeons;

import xaos.utils.Utils;

public class MonsterData {

    public static final String ID_RANDOM = "__RANDOM__";

    private String id;
    private int number;
    private int levelMin;
    private int levelMax;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setNumber(String sNumber) {
        setNumber(Utils.launchDice(sNumber));
    }

    public int getLevelMin() {
        return levelMin;
    }

    public void setLevelMin(int levelMin) {
        this.levelMin = levelMin;
    }

    public void setLevelMin(String sLevelMin) {
        setLevelMin(Utils.launchDice(sLevelMin));
    }

    public int getLevelMax() {
        return levelMax;
    }

    public void setLevelMax(int levelMax) {
        this.levelMax = levelMax;
    }

    public void setLevelMax(String sLevelMax) {
        setLevelMax(Utils.launchDice(sLevelMax));
    }
}
