package xaos.dungeons;

import java.util.ArrayList;

import xaos.utils.Messages;
import xaos.utils.Utils;

public class DungeonData {

    public final static String TYPE_RANDOM = "__RANDOM__"; //$NON-NLS-1$
    public final static String TYPE_CAVE = "__CAVE__"; //$NON-NLS-1$
    public final static String TYPE_ROOMS = "__ROOMS__"; //$NON-NLS-1$

    private String id;
    private short level;
    private String type;
    private ArrayList<MonsterData> monsters;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) throws Exception {
        if (level <= 0) {
            throw new Exception(Messages.getString("DungeonData.3") + level + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        this.level = level;
    }

    public void setLevel(String sLevel) throws Exception {
        setLevel((short) Utils.launchDice(sLevel));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.trim().length() == 0 || type.equalsIgnoreCase(TYPE_RANDOM)) {
            if (Utils.getRandomBetween(1, 2) == 1) {
                type = TYPE_CAVE;
            } else {
                type = TYPE_ROOMS;
            }

        }

        this.type = type;
    }

    public ArrayList<MonsterData> getMonsters() {
        if (monsters == null) {
            monsters = new ArrayList<MonsterData>();
        }

        return monsters;
    }

    public void setMonsters(ArrayList<MonsterData> alMonsters) {
        monsters = alMonsters;
    }

    public void addMonster(MonsterData monsterData) {
        getMonsters().add(monsterData);
    }
}
