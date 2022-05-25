package game.systems.map;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import game.systems.render.BatchSystem;
import game.systems.resources.AnimationsSystem;
import model.textures.AOTexture;
import model.textures.BundledAnimation;
import shared.model.map.Map;
import shared.model.map.Tile;
import shared.model.map.WorldPosition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapManager extends BaseSystem {

    public static final List<Integer> LOWER_LAYERS = Arrays.asList(0, 1);
    public static final List<Integer> MIDDLE_LAYERS = Collections.singletonList(2);
    public static final List<Integer> UPPER_LAYERS = Collections.singletonList(3);
    public static boolean layer4Disable = false;
    private AnimationsSystem animationsSystem;
    private BatchSystem batchSystem;

    public void drawLayer(Map map, int layer) {
        drawLayer(map, 0, layer, false, false, false);
    }

    private void drawLayer(Map map, float delta, int layer, boolean drawExit, boolean drawBlock, boolean flip) {
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = map.getHeight() - 1; y >= 0; y--) {
                Tile tile = map.getTile(x, y);
                if (tile == null) {
                    continue;
                }
                int graphic = tile.getGraphic(layer);
                if (graphic == 0) {
                    continue;
                }
                if (flip) {
                    doTileDrawFlipped(delta, x, y, graphic);
                } else {
                    doTileDraw(delta, x, y, graphic);
                }
                if (drawBlock && tile.isBlocked()) {
                    // draw block
                    doTileDraw(delta, x, y, 4);
                }
                if (drawExit && tile.getTileExit() != null && !(new WorldPosition().equals(tile.getTileExit()))) {
                    // draw exit
                    doTileDraw(delta, x, y, 3);
                }
                if (drawExit && tile.getTrigger() > 0 ){
                    doTileDraw(delta, x, y, 23652);
                }
            }
        }
    }

    public void drawLayer(Map map, float delta, int layer, boolean drawExit, boolean drawBlock) {
        drawLayer(map, delta, layer, drawExit, drawBlock, true);
    }

    private void doTileDrawFlipped(float delta, int x, int y, int graphic) {
        TextureRegion tileRegion = animationsSystem.hasTexture(graphic) ?
                getTextureRegion(animationsSystem.getTexture(graphic)) :
                getAnimation(delta, graphic);
        if (tileRegion != null && !tileRegion.isFlipY()) {
            tileRegion.flip(false, true);
        }
        doTileDraw(y, x, tileRegion);
    }

    public TextureRegion getAnimation(float delta, int graphic) {
        TextureRegion tileRegion = null;
        BundledAnimation animation = animationsSystem.getTiledAnimation(graphic);
        if (animation != null) {
            tileRegion = animation.getGraphic();
        }
        return tileRegion;
    }

    public TextureRegion getTextureRegion(AOTexture texture) {
        TextureRegion tileRegion = null;
        if (texture != null) {
            tileRegion = texture.getTexture();
        }
        return tileRegion;
    }

    public void doTileDraw(float delta, int x, int y, int graphic) {
        TextureRegion tileRegion = animationsSystem.hasTexture(graphic) ?
                getTextureRegion(animationsSystem.getTexture(graphic)) :
                getAnimation(delta, graphic);
        doTileDraw(y, x, tileRegion);
    }

    private void doTileDraw(int y, int x, TextureRegion tileRegion) {
        if (tileRegion != null) {
            final float mapPosX = (x * Tile.TILE_PIXEL_WIDTH);
            final float mapPosY = (y * Tile.TILE_PIXEL_HEIGHT);
            final float tileOffsetX = mapPosX + (Tile.TILE_PIXEL_WIDTH - tileRegion.getRegionWidth()) / 2;
            final float tileOffsetY = mapPosY - tileRegion.getRegionHeight() + Tile.TILE_PIXEL_HEIGHT;

            batchSystem.getBatch().draw(tileRegion, tileOffsetX, tileOffsetY);
        }
    }

    @Override
    protected void processSystem() {
    }
}
