package shared.model.map;

import shared.util.MapHelper;
import shared.util.MapHelper.Dir;

import static shared.util.MapHelper.Dir.LEFT;

public class Map {

    private static final int LEFT = 0;
    private static final int UP = 1;
    private static final int RIGHT = 2;
    private static final int DOWN = 3;

    public static final int MAX_MAP_SIZE_WIDTH = 100;
    public static final int MIN_MAP_SIZE_WIDTH = 1;
    public static final int MAX_MAP_SIZE_HEIGHT = 100;
    public static final int MIN_MAP_SIZE_HEIGHT = 1;

    public static final int TILE_BUFFER_SIZE = 7;
    protected Tile tiles[][];
    private boolean secureZone;
    private int[] neighbours = new int[4];

    public Map() {
        this.tiles = new Tile[MAX_MAP_SIZE_WIDTH + 1][MAX_MAP_SIZE_HEIGHT + 1];
    }

    public Map(int width, int height) {
        this.tiles = new Tile[width][height];
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public Tile getTile(int x, int y) {
        return this.tiles[x][y];
    }

    public void setTile(int x, int y, Tile tile) {
        this.tiles[x][y] = tile;
    }

    public int getWidth() {
        return tiles.length;
    }

    public int getHeight() {
        return tiles[0].length;
    }

    public void setNeighbours(int left, int up, int right, int down) {
        neighbours[LEFT] = left;
        neighbours[UP] = up;
        neighbours[RIGHT] = right;
        neighbours[DOWN] = down;
    }

    public int getNeighbour(Dir dir) {
        int n = 0;
        switch (dir) {
            case LEFT:
                n = LEFT;
                break;
            case DOWN:
                n = DOWN;
                break;
            case RIGHT:
                n = RIGHT;
                break;
            case UP:
                n = UP;
                break;
        }
        return neighbours[n];
    }

    public boolean isSecureZone() {
        return secureZone;
    }

    public void setSecureZone(boolean secureZone) {
        this.secureZone = secureZone;
    }


}
