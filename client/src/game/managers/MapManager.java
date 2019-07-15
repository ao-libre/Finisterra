package game.managers;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import game.handlers.AOAssetManager;
import game.handlers.AnimationHandler;
import model.textures.AOAnimation;
import model.textures.AOImage;
import model.textures.AOTexture;
import model.textures.BundledAnimation;
import shared.model.map.Map;
import shared.model.map.Tile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapManager extends BaseSystem {

    public static final List<Integer> LOWER_LAYERS = Arrays.asList(0, 1);
    public static final List<Integer> UPPER_LAYERS = Collections.singletonList(3);
    public static final int TILE_BUFFER_SIZE = 7;
    public static final int MAX_MAP_SIZE_WIDTH = 100;
    public static final int MIN_MAP_SIZE_WIDTH = 1;
    public static final int MAX_MAP_SIZE_HEIGHT = 100;
    public static final int MIN_MAP_SIZE_HEIGHT = 1;
    private AnimationHandler animationHandler;
    private AOAssetManager assetManager;

    public void drawTile(Map map, SpriteBatch batch, float delta, int layer, int y, int x) {
        int graphic = map.getTile(x, y).getGraphic(layer);
        if (graphic == 0) {
            return;
        }

        doTileDraw(batch, delta, y, x, graphic);
    }

    public void doTileDraw(SpriteBatch batch, float delta, int y, int x, int graphic) {
        // TODO Refactor maps layers to have animations separated
        AOImage image = assetManager.getImage(graphic);
        TextureRegion tileRegion = null;
        if (image != null) {
            // TODO CACHE
            tileRegion = new AOTexture(image, false).getTexture();
        } else {
            AOAnimation animation = assetManager.getAnimation(graphic);
            if (animation != null) {
                BundledAnimation anim = new BundledAnimation(animation);
                anim.setAnimationTime(anim.getAnimationTime() + delta);
                tileRegion = anim.getGraphic();
            }
        }

        doTileDraw(batch, y, x, tileRegion);
    }

    public void doTileDraw(SpriteBatch batch, int y, int x, TextureRegion tileRegion) {
        if (tileRegion != null) {
            final float mapPosX = (x * Tile.TILE_PIXEL_WIDTH);
            final float mapPosY = (y * Tile.TILE_PIXEL_HEIGHT);
            final float tileOffsetX = mapPosX + (Tile.TILE_PIXEL_WIDTH - tileRegion.getRegionWidth()) / 2;
            final float tileOffsetY = mapPosY - tileRegion.getRegionHeight() + Tile.TILE_PIXEL_HEIGHT;
            batch.draw(tileRegion, tileOffsetX, tileOffsetY);
        }
    }


    @Override
    protected void processSystem() {
    }
}
