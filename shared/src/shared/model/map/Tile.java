package shared.model.map;

public class Tile {

    public static final int EMPTY_INDEX = -1;
    public static final float TILE_PIXEL_WIDTH = 64.0f;
    public static final float TILE_PIXEL_HEIGHT = 64.0f;

    private int[] graphic;

    private int charIndex;
    private int objIndex;
    private int objCount;
    private int npcIndex;

    private WorldPosition tileExit;
    private boolean blocked;

    private int trigger;

    public Tile() {
    }

    public Tile(int[] graphic, int charIndex, int objCount, int objIndex,
                int npcIndex, WorldPosition tileExit, boolean blocked,
                int trigger) {
        this.setGraphic(graphic);
        this.setCharIndex(charIndex);
        this.setObjIndex(objIndex);
        this.setNpcIndex(npcIndex);
        this.setTileExit(tileExit);
        this.setBlocked(blocked);
        this.setTrigger(trigger);
        this.setObjCount(objCount);
    }

    public void setObjCount(int objCount) {
        this.objCount = objCount;
    }

    public int getObjCount() {
        return objCount;
    }

    public int getGraphic(int index) {
        return this.graphic[index];
    }

    public int[] getGraphic() {
        return this.graphic;
    }

    public void setGraphic(int[] graphic) {
        this.graphic = graphic;
    }

    public int getCharIndex() {
        return charIndex;
    }

    public void setCharIndex(int charIndex) {
        this.charIndex = charIndex;
    }

    public int getObjIndex() {
        return objIndex;
    }

    public void setObjIndex(int objIndex) {
        this.objIndex = objIndex;
    }

    public int getNpcIndex() {
        return npcIndex;
    }

    public void setNpcIndex(int npcIndex) {
        this.npcIndex = npcIndex;
    }

    public WorldPosition getTileExit() {
        return tileExit;
    }

    public void setTileExit(WorldPosition tileExit) {
        this.tileExit = tileExit;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public int getTrigger() {
        return trigger;
    }

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

}
