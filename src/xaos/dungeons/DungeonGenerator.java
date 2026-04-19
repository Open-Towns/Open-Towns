package xaos.dungeons;

import java.util.ArrayList;

import xaos.main.World;
import xaos.tiles.Cell;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Utils;

public class DungeonGenerator {

    public static void generateDungeons(String sCampaignID, String sMissionID) {
        ArrayList<DungeonData> alDungeons = DungeonManager.getDungeons(sCampaignID, sMissionID);
        DungeonData dungeonData;

        for (int i = 0; i < alDungeons.size(); i++) {
            dungeonData = alDungeons.get(i);
            if (dungeonData.getLevel() >= World.MAP_DEPTH) {
                continue;
            }

            Cell[][][] cells = World.getCells();
            if (dungeonData.getType().equalsIgnoreCase(DungeonData.TYPE_CAVE)) {
                generateDungeonCave(cells, dungeonData.getLevel());
            } else if (dungeonData.getType().equalsIgnoreCase(DungeonData.TYPE_ROOMS)) {
                generateDungeonRooms(cells, dungeonData.getLevel());
            } else {
                Log.log(Log.LEVEL_ERROR, Messages.getString("DungeonGenerator.1") + dungeonData.getType() + "]", "DungeonGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }

    /**
     * Genera un dungeon de tipo caverna
     *
     * @param iLevel Nivel
     */
    private static void generateDungeonCave(Cell[][][] cells, int iLevel) {
        // Genero un dungeon
        boolean[][] cellsMain = new boolean[World.MAP_WIDTH][World.MAP_HEIGHT];
        boolean[][] cellsCopia = new boolean[World.MAP_WIDTH][World.MAP_HEIGHT];

        // A random meto "gujeros"
        for (int x = 0; x < World.MAP_WIDTH; x++) {
            for (int y = 0; y < World.MAP_HEIGHT; y++) {
                if (Utils.getRandomBetween(1, 100) <= 40) {
                    cellsMain[x][y] = true;
                }
            }
        }

        // Copia
        for (int x = 0; x < World.MAP_WIDTH; x++) {
            for (int y = 0; y < World.MAP_HEIGHT; y++) {
                cellsCopia[x][y] = cellsMain[x][y];
            }
        }

        // Autómata (5 iteraciones)
        for (int i = 0; i < 4; i++) {
            // Igualamos
            for (int x = 0; x < World.MAP_WIDTH; x++) {
                for (int y = 0; y < World.MAP_HEIGHT; y++) {
                    if (getNumNeighboursDiscovered(cellsCopia, x, y) >= 5) {
                        cellsMain[x][y] = true;
                    }
                }
            }

            // Copia
            for (int x = 0; x < World.MAP_WIDTH; x++) {
                for (int y = 0; y < World.MAP_HEIGHT; y++) {
                    cellsCopia[x][y] = cellsMain[x][y];
                }
            }
        }

        // Quitamos los blocks
        for (int x = 0; x < World.MAP_WIDTH; x++) {
            for (int y = 0; y < World.MAP_HEIGHT; y++) {
                if (cellsCopia[x][y] && getNumNeighboursDiscovered(cellsCopia, x, y) > 7) {
                    if (!hasFluidsAround(cells, x, y, iLevel)) {
                        removeBlock(cells, x, y, iLevel);
                        cells[x][y][iLevel].setCave(true);
                    }
                }
            }
        }
    }

    public static void removeBlock(Cell[][][] cells, int x, int y, int z) {
        cells[x][y][z].getTerrain().setMineTurns(0);
        cells[x][y][z].getTerrain().setTerrainID(TerrainManagerItem.TERRAIN_AIR_ID);
        cells[x][y][z].getTerrain().setTerrainTileID(TerrainManagerItem.TERRAIN_AIR_ID * TerrainManager.SLOPES_INIHEADER.length);
        cells[x][y][z].setMined(true);
        cells[x][y][z].setBlocky(false);
    }

    /**
     * Genera un dungeon de habitaciones (tipo ADOM)
     *
     * @param cells
     * @param iLevel
     */
    private static void generateDungeonRooms(Cell[][][] cells, int iLevel) {
        boolean[][] cellsMain = new boolean[World.MAP_WIDTH][World.MAP_HEIGHT];

        // Metemos la primera room de 5x5 en un sitio sin líquidos
        final int ROOM_WIDTH = 5;
        final int ROOM_HEIGHT = 5;
        int xRoom, yRoom;
        for (;;) {
            xRoom = Utils.getRandomBetween(ROOM_WIDTH, World.MAP_WIDTH - 1 - ROOM_WIDTH);
            yRoom = Utils.getRandomBetween(ROOM_WIDTH, World.MAP_WIDTH - 1 - ROOM_WIDTH);

            // Miro que no haya líquidos
            boolean bLiquids = false;
            liquidos:
            for (int x = xRoom; x < (xRoom + ROOM_WIDTH); x++) {
                for (int y = yRoom; y < (yRoom + ROOM_HEIGHT); y++) {
                    if (hasFluidsAround(cells, x, y, iLevel)) {
                        bLiquids = true;
                        break liquidos;
                    }
                }
            }

            if (!bLiquids) {
                break;
            }
        }

        // Tenemos sitio para la primera room, la metemos
        for (int x = xRoom; x < (xRoom + ROOM_WIDTH); x++) {
            for (int y = yRoom; y < (yRoom + ROOM_HEIGHT); y++) {
                cellsMain[x][y] = true;
            }
        }

        // Ahora vamos generando pasillos y rooms a partir de ésta
        generateDungeonRooms(cells, cellsMain, xRoom, yRoom, ROOM_WIDTH, ROOM_HEIGHT, iLevel);

        // Quitamos los blocks
        for (int x = 0; x < World.MAP_WIDTH; x++) {
            for (int y = 0; y < World.MAP_HEIGHT; y++) {
                if (cellsMain[x][y] && !hasFluidsAround(cells, x, y, iLevel)) {
                    removeBlock(cells, x, y, iLevel);
                    cells[x][y][iLevel].setCave(true);
                }
            }
        }
    }

    /**
     * Genera pasillos y habitaciones a partir de una habitación
     *
     * @param cells
     * @param cellsCopia
     * @param roomX
     * @param roomY
     * @param roomWidth
     * @param roomHeight
     * @param iLevel
     */
    private static void generateDungeonRooms(Cell[][][] cells, boolean[][] cellsCopia, int roomX, int roomY, int roomWidth, int roomHeight, int iLevel) {
		// Pasillos en las 4 direcciones y después room al final de cada pasillo
        // Se llamará recursivamente hasta que no quepan más

        int xNorth = -1, xSouth = -1, yEast = -1, yWest = -1;
        int xNorthLength = 0, xSouthLength = 0, yEastLength = 0, yWestLength = 0;
        final int PASILLO_INTENTOS = 5;
        final int PASILLO_MIN_LENGTH = 5;
        final int PASILLO_MAX_LENGTH = 28;
        final int ROOM_WIDTH_MIN = 5;
        final int ROOM_WIDTH_MAX = 15;
        final int ROOM_HEIGHT_MIN = 5;
        final int ROOM_HEIGHT_MAX = 15;

		// Para cada punto cardinal miramos si cabe un pasillo
        // El orden (n, s, e, o) será aleatorio, usamos un array de ints (1=N, 2=S, 4=E, 8=O)
        int[] orden = new int[4];
        while ((orden[0] + orden[1] + orden[2] + orden[3]) != 15) { // Truquito para tener los 4 puntos
            orden[0] = Utils.getRandomBetween(1, 8);
            while (orden[0] != 1 && orden[0] != 2 && orden[0] != 4 && orden[0] != 8) { // Chapuza
                orden[0] = Utils.getRandomBetween(1, 8);
            }
            orden[1] = Utils.getRandomBetween(1, 8);
            while (orden[1] != 1 && orden[1] != 2 && orden[1] != 4 && orden[1] != 8) { // Chapuza
                orden[1] = Utils.getRandomBetween(1, 8);
            }
            orden[2] = Utils.getRandomBetween(1, 8);
            while (orden[2] != 1 && orden[2] != 2 && orden[2] != 4 && orden[2] != 8) { // Chapuza
                orden[2] = Utils.getRandomBetween(1, 8);
            }
            orden[3] = Utils.getRandomBetween(1, 8);
            while (orden[3] != 1 && orden[3] != 2 && orden[3] != 4 && orden[3] != 8) { // Chapuza
                orden[3] = Utils.getRandomBetween(1, 8);
            }
        }

        // Ya tenemos el orden, go go go
        for (int o = 0; o < orden.length; o++) {
            boolean bCabe = true;

            if (orden[o] == 1) {
                // Norte
                for (int i = 0; i < PASILLO_INTENTOS; i++) {
                    bCabe = true;
                    xNorth = Utils.getRandomBetween(roomX + 1, (roomX + roomWidth - 2));
                    xNorthLength = Utils.getRandomBetween(PASILLO_MIN_LENGTH, PASILLO_MAX_LENGTH);

                    // Miramos si cabe
                    for (int j = 0; j < xNorthLength; j++) {
                        if ((roomY - 1 - j) < 0) {
                            bCabe = false;
                            break;
                        } else {
                            if (cellsCopia[xNorth][(roomY - 1 - j)]) {
                                bCabe = false;
                                break;
                            }
                        }
                    }

                    if (bCabe) {
                        break;
                    }
                }
                if (bCabe) {
                    // Lo metemos
                    for (int i = 0; i < xNorthLength; i++) {
                        cellsCopia[xNorth][roomY - 1 - i] = true;
                    }

                    // Intentamos meter una room
                    int newRoomWidth = Utils.getRandomBetween(ROOM_WIDTH_MIN, ROOM_WIDTH_MAX);
                    int newRoomHeigth = Utils.getRandomBetween(ROOM_HEIGHT_MIN, ROOM_HEIGHT_MAX);
                    if (!placeRoom(cells, cellsCopia, xNorth - newRoomWidth / 2, roomY - xNorthLength - newRoomHeigth, newRoomWidth, newRoomHeigth, iLevel)) {
                        for (int i = 0; i < xNorthLength; i++) {
                            cellsCopia[xNorth][roomY - 1 - i] = false;
                        }
                    }
                }
            } else if (orden[o] == 2) {
                // Sur
                for (int i = 0; i < PASILLO_INTENTOS; i++) {
                    bCabe = true;
                    xSouth = Utils.getRandomBetween(roomX + 1, (roomX + roomWidth - 2));
                    xSouthLength = Utils.getRandomBetween(PASILLO_MIN_LENGTH, PASILLO_MAX_LENGTH);

                    // Miramos si cabe
                    for (int j = 0; j < xSouthLength; j++) {
                        if ((roomY + roomHeight + j) >= World.MAP_HEIGHT) {
                            bCabe = false;
                            break;
                        } else {
                            if (cellsCopia[xSouth][(roomY + roomHeight + j)]) {
                                bCabe = false;
                                break;
                            }
                        }
                    }

                    if (bCabe) {
                        break;
                    }
                }
                if (bCabe) {
                    // Lo metemos
                    for (int i = 0; i < xSouthLength; i++) {
                        cellsCopia[xSouth][roomY + roomHeight + i] = true;
                    }
                    // Intentamos meter una room
                    int newRoomWidth = Utils.getRandomBetween(ROOM_WIDTH_MIN, ROOM_WIDTH_MAX);
                    int newRoomHeigth = Utils.getRandomBetween(ROOM_HEIGHT_MIN, ROOM_HEIGHT_MAX);
                    if (!placeRoom(cells, cellsCopia, xSouth - newRoomWidth / 2, roomY + roomHeight + xSouthLength, newRoomWidth, newRoomHeigth, iLevel)) {
                        for (int i = 0; i < xSouthLength; i++) {
                            cellsCopia[xSouth][roomY + roomHeight + i] = false;
                        }
                    }
                }
            } else if (orden[o] == 4) {
                // Este
                for (int i = 0; i < PASILLO_INTENTOS; i++) {
                    bCabe = true;
                    yEast = Utils.getRandomBetween(roomY + 1, (roomY + roomHeight - 2));
                    yEastLength = Utils.getRandomBetween(PASILLO_MIN_LENGTH, PASILLO_MAX_LENGTH);

                    // Miramos si cabe
                    for (int j = 0; j < yEastLength; j++) {
                        if ((roomX + roomWidth + j) >= World.MAP_WIDTH) {
                            bCabe = false;
                            break;
                        } else {
                            if (cellsCopia[(roomX + roomWidth + j)][yEast]) {
                                bCabe = false;
                                break;
                            }
                        }
                    }

                    if (bCabe) {
                        break;
                    }
                }
                if (bCabe) {
                    // Lo metemos
                    for (int i = 0; i < yEastLength; i++) {
                        cellsCopia[roomX + roomWidth + i][yEast] = true;
                    }
                    // Intentamos meter una room
                    int newRoomWidth = Utils.getRandomBetween(ROOM_WIDTH_MIN, ROOM_WIDTH_MAX);
                    int newRoomHeigth = Utils.getRandomBetween(ROOM_HEIGHT_MIN, ROOM_HEIGHT_MAX);
                    if (!placeRoom(cells, cellsCopia, roomX + roomWidth + yEastLength, yEast - newRoomHeigth / 2, newRoomWidth, newRoomHeigth, iLevel)) {
                        for (int i = 0; i < yEastLength; i++) {
                            cellsCopia[roomX + roomWidth + i][yEast] = false;
                        }
                    }
                }
            } else if (orden[o] == 8) {
                // Oeste
                for (int i = 0; i < PASILLO_INTENTOS; i++) {
                    bCabe = true;
                    yWest = Utils.getRandomBetween(roomY + 1, (roomY + roomHeight - 2));
                    yWestLength = Utils.getRandomBetween(PASILLO_MIN_LENGTH, PASILLO_MAX_LENGTH);

                    // Miramos si cabe
                    for (int j = 0; j < yWestLength; j++) {
                        if ((roomX - 1 - j) < 0) {
                            bCabe = false;
                            break;
                        } else {
                            if (cellsCopia[(roomX - 1 - j)][yWest]) {
                                bCabe = false;
                                break;
                            }
                        }
                    }

                    if (bCabe) {
                        break;
                    }
                }
                if (bCabe) {
                    // Lo metemos
                    for (int i = 0; i < yWestLength; i++) {
                        cellsCopia[roomX - 1 - i][yWest] = true;
                    }
                    // Intentamos meter una room
                    int newRoomWidth = Utils.getRandomBetween(ROOM_WIDTH_MIN, ROOM_WIDTH_MAX);
                    int newRoomHeigth = Utils.getRandomBetween(ROOM_HEIGHT_MIN, ROOM_HEIGHT_MAX);
                    if (!placeRoom(cells, cellsCopia, roomX - yWestLength - newRoomWidth, yWest - newRoomHeigth / 2, newRoomWidth, newRoomHeigth, iLevel)) {
                        for (int i = 0; i < yWestLength; i++) {
                            cellsCopia[roomX - 1 - i][yWest] = false;
                        }
                    }
                }
            } else {
                // No es posible
                Log.log(Log.LEVEL_ERROR, Messages.getString("DungeonGenerator.0") + orden[o] + "]", "DungeonGenerator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }

    /**
     * Intenta meter una room en el sitio pasado, si lo consigue llama al
     * generateDungeonRooms(....) para que meta pasillos, más rooms, ....
     *
     * @param cells
     * @param cellsCopia
     * @param roomX
     * @param roomY
     * @param roomWidth
     * @param roomHeight
     * @param iLevel
     */
    private static boolean placeRoom(Cell[][][] cells, boolean[][] cellsCopia, int roomX, int roomY, int roomWidth, int roomHeight, int iLevel) {
        if (roomX < 0 || roomY < 0 || (roomX + roomWidth - 1) >= World.MAP_WIDTH || (roomY + roomHeight - 1) >= World.MAP_HEIGHT) {
            return false;
        }

        // Habitación dentro del mapa, comprobamos que ninguna casilla ya está ocupada o tenga fluidos alrededor
        boolean bOK = true;
        room1:
        for (int x = roomX; x < (roomX + roomWidth); x++) {
            for (int y = roomY; y < (roomY + roomHeight); y++) {
                if (cellsCopia[x][y]) {
                    bOK = false;
                    break room1;
                }
            }
        }

        if (bOK) {
            // La habitación cabe de guais, la metemos
            for (int x = roomX; x < (roomX + roomWidth); x++) {
                for (int y = roomY; y < (roomY + roomHeight); y++) {
                    cellsCopia[x][y] = true;
                }
            }

            // Llamamos al generateDungeon (...) para que genere pasillos y más rooms
            generateDungeonRooms(cells, cellsCopia, roomX, roomY, roomWidth, roomHeight, iLevel);
            return true;
        }

        return false;
    }

    /**
     * Devuelve el número de vecinos discovereds Se usa en la generación de
     * dungeons
     *
     * @param cellsCopia Array de booleans indicando los discovered
     * @param x
     * @param y
     * @return
     */
    private static int getNumNeighboursDiscovered(boolean[][] cellsCopia, int x, int y) {
        int iDiscovereds = 0;

        for (int i = (x - 1); i <= (x + 1); i++) {
            for (int j = (y - 1); j <= (y + 1); j++) {
                if (i >= 0 && i < World.MAP_WIDTH && j >= 0 && j < World.MAP_HEIGHT && (i != x || j != y)) {
                    if (cellsCopia[i][j]) {
                        iDiscovereds++;
                    }
                } else {
                    // Fuera del mapa, sólo el 20% son discovereds
                    if (Utils.getRandomBetween(1, 5) == 1) {
                        iDiscovereds++;
                    }
                }
            }
        }

        return iDiscovereds;
    }

    /**
     * Indica si algun vecino tiene fluidos
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static boolean hasFluidsAround(Cell[][][] cells, int x, int y, int z) {
        // Arriba
        if (z > 0) {
            if (cells[x][y][z - 1].getTerrain().hasFluids()) {
                return true;
            }
        }

        // Colindantes
        for (int i = (x - 1); i <= (x + 1); i++) {
            for (int j = (y - 1); j <= (y + 1); j++) {
                if (i >= 0 && i < World.MAP_WIDTH && j >= 0 && j < World.MAP_HEIGHT) {
                    if (cells[i][j][z].getTerrain().hasFluids()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
