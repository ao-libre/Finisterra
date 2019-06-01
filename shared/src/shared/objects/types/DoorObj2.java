package shared.objects.types;

import org.ini4j.Profile;
import shared.objects.factory.ObjectFactory;

public class DoorObj2 extends Obj {

    private boolean open = false;
    private int closedGrh;
    private int tileHeight = 1;
    private int tileWidth = 1;

    public DoorObj2() {
    }

    public DoorObj2(int id, String name, int grhIndex) {
        super(id, name, grhIndex);
    }

    @Override
    public Type getType() {
        return Type.DOOR;
    }

    public int getClosedGrh() {
        return closedGrh;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setClosedGrh(int closedGrh) {
        this.closedGrh = closedGrh;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getOpenGrh() {
        return getGrhIndex();
    }

}
