package xaos.data;

import java.util.ArrayList;
import java.util.StringTokenizer;

import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.utils.Log;
import xaos.utils.Messages;

/**
 * Contiene la información de Hate para una living
 *
 */
public class HateData {

    private ArrayList<String> friendlies;
    private ArrayList<String> allies;
    private ArrayList<String> enemies;
    private ArrayList<String> heros;
    private ArrayList<String> citizens;

    private boolean allFriendlies;
    private boolean allAllies;
    private boolean allEnemies;
    private boolean allHeros;
    private boolean allCitizens;

    public HateData(String sHateData) {
        friendlies = new ArrayList<String>();
        allies = new ArrayList<String>();
        enemies = new ArrayList<String>();
        heros = new ArrayList<String>();
        citizens = new ArrayList<String>();
        allFriendlies = false;
        allEnemies = false;
        allHeros = false;
        allCitizens = false;

        if (sHateData != null && sHateData.trim().length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(sHateData, ","); //$NON-NLS-1$
            String token;
            LivingEntityManagerItem lemi;
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken().trim();

                if (token.length() > 0) {
					// Miramos a ver que es

                    // Primero los tipos genéricos
                    if (token.equalsIgnoreCase(LivingEntityManagerItem.TYPE_FRIENDLY)) {
                        setAllFriendlies(true);
                    } else if (token.equalsIgnoreCase(LivingEntityManagerItem.TYPE_ALLY)) {
                        setAllAllies(true);
                    } else if (token.equalsIgnoreCase(LivingEntityManagerItem.TYPE_ENEMY)) {
                        setAllEnemies(true);
                    } else if (token.equalsIgnoreCase(LivingEntityManagerItem.TYPE_HERO)) {
                        setAllHeros(true);
                    } else if (token.equalsIgnoreCase(LivingEntityManagerItem.TYPE_CITIZEN)) {
                        setAllCitizens(true);
                    } else {
                        lemi = LivingEntityManager.getItem(token);
                        if (lemi != null) {
                            if (lemi.getType() == LivingEntity.TYPE_FRIENDLY) {
                                friendlies.add(token);
                            } else if (lemi.getType() == LivingEntity.TYPE_ALLY) {
                                allies.add(token);
                            } else if (lemi.getType() == LivingEntity.TYPE_ENEMY) {
                                enemies.add(token);
                            } else if (lemi.getType() == LivingEntity.TYPE_CITIZEN) {
                                citizens.add(token);
                            } else if (lemi.getType() == LivingEntity.TYPE_HERO) {
                                heros.add(token);
                            } else {
                                Log.log(Log.LEVEL_ERROR, Messages.getString("HateData.1") + token + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } else {
                            Log.log(Log.LEVEL_ERROR, Messages.getString("HateData.3") + token + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
            }
        }
    }

    public ArrayList<String> getFriendlies() {
        return friendlies;
    }

    public void setFriendlies(ArrayList<String> friendlies) {
        this.friendlies = friendlies;
    }

    public ArrayList<String> getAllies() {
        return allies;
    }

    public void setAllies(ArrayList<String> allies) {
        this.allies = allies;
    }

    public ArrayList<String> getEnemies() {
        return enemies;
    }

    public void setEnemies(ArrayList<String> enemies) {
        this.enemies = enemies;
    }

    public ArrayList<String> getHeros() {
        return heros;
    }

    public void setHeros(ArrayList<String> heros) {
        this.heros = heros;
    }

    public ArrayList<String> getCitizens() {
        return citizens;
    }

    public void setCitizens(ArrayList<String> citizens) {
        this.citizens = citizens;
    }

    public boolean isAllFriendlies() {
        return allFriendlies;
    }

    public void setAllFriendlies(boolean allFriendlies) {
        this.allFriendlies = allFriendlies;
    }

    public boolean isAllAllies() {
        return allAllies;
    }

    public void setAllAllies(boolean allAllies) {
        this.allAllies = allAllies;
    }

    public boolean isAllEnemies() {
        return allEnemies;
    }

    public void setAllEnemies(boolean allEnemies) {
        this.allEnemies = allEnemies;
    }

    public boolean isAllHeros() {
        return allHeros;
    }

    public void setAllHeros(boolean allHeros) {
        this.allHeros = allHeros;
    }

    public boolean isAllCitizens() {
        return allCitizens;
    }

    public void setAllCitizens(boolean allCitizens) {
        this.allCitizens = allCitizens;
    }

    /**
     * Indica si la living pasada es hate
     *
     * @param le
     * @return true si la living pasada es hate
     */
    public boolean isHate(LivingEntity le) {
        LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());

        if (lemi == null) {
            return false;
        }

        if (lemi.getType() == LivingEntity.TYPE_CITIZEN) {
            if (isAllCitizens()) {
                return true;
            } else if (getCitizens().size() > 0) {
                // Odia sólo a algunos citizens, miramos si el de la celda es odiado
                if (getCitizens().contains(le.getIniHeader())) {
                    return true;
                }
            }
        } else if (lemi.getType() == LivingEntity.TYPE_FRIENDLY) {
            if (isAllFriendlies()) {
                return true;
            } else if (getFriendlies().size() > 0) {
                if (getFriendlies().contains(le.getIniHeader())) {
                    return true;
                }
            }
        } else if (lemi.getType() == LivingEntity.TYPE_ENEMY) {
            if (isAllEnemies()) {
                return true;
            } else if (getEnemies().size() > 0) {
                if (getEnemies().contains(le.getIniHeader())) {
                    return true;
                }
            }
        } else if (lemi.getType() == LivingEntity.TYPE_HERO) {
            if (isAllHeros()) {
                return true;
            } else if (getHeros().size() > 0) {
                // Odia sólo a algunos heros, miramos si el de la celda es odiado
                if (getHeros().contains(le.getIniHeader())) {
                    return true;
                }
            }
        } else if (lemi.getType() == LivingEntity.TYPE_ALLY) {
            if (isAllAllies()) {
                return true;
            } else if (getAllies().size() > 0) {
                // Odia sólo a algunos allies, miramos si el de la celda es odiado
                if (getAllies().contains(le.getIniHeader())) {
                    return true;
                }
            }
        }

        return false;
    }
}
