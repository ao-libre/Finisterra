package game.systems.render.world;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import game.managers.MapManager;
import game.systems.camera.CameraSystem;
import game.systems.map.TiledMapSystem;
import shared.model.map.Map;
import shared.model.map.Tile;

@Wire
public class MapLowerLayerRenderingSystem extends BaseSystem {

    public SpriteBatch batch;
    private TiledMapSystem mapSystem;
    private CameraSystem cameraSystem;

    public MapLowerLayerRenderingSystem(SpriteBatch spriteBatch) {
        this.batch = spriteBatch;
    }

    @Override
    protected void begin() {
        this.cameraSystem.camera.update();
        this.batch.setProjectionMatrix(this.cameraSystem.camera.combined);
        this.batch.begin();
    }

    private void renderWorld() {
        Map map = this.mapSystem.map;
        if (map == null) return;
        // Variable Declarations
        int screenMinX, screenMaxX, screenMinY, screenMaxY, minAreaX, minAreaY, maxAreaX, maxAreaY;

        // Calculate visible part of the map
        int cameraPosX = (int) (this.cameraSystem.camera.position.x / Tile.TILE_PIXEL_WIDTH);
        int cameraPosY = (int) (this.cameraSystem.camera.position.y / Tile.TILE_PIXEL_HEIGHT);
        int halfWindowTileWidth = (int) ((this.cameraSystem.camera.viewportWidth / Tile.TILE_PIXEL_WIDTH) / 2f);
        int halfWindowTileHeight = (int) ((this.cameraSystem.camera.viewportHeight / Tile.TILE_PIXEL_HEIGHT) / 2f);

        screenMinX = cameraPosX - halfWindowTileWidth - 2;
        screenMaxX = cameraPosX + halfWindowTileWidth + 2;
        screenMinY = cameraPosY - halfWindowTileHeight - 2;
        screenMaxY = cameraPosY + halfWindowTileHeight + 2;

        if (screenMinX < Map.MIN_MAP_SIZE_WIDTH) screenMinX = Map.MIN_MAP_SIZE_WIDTH;
        if (screenMaxX > Map.MAX_MAP_SIZE_WIDTH) screenMaxX = Map.MAX_MAP_SIZE_WIDTH;
        if (screenMinY < Map.MIN_MAP_SIZE_HEIGHT) screenMinY = Map.MIN_MAP_SIZE_HEIGHT;
        if (screenMaxY > Map.MAX_MAP_SIZE_HEIGHT) screenMaxY = Map.MAX_MAP_SIZE_HEIGHT;

        // LAYER 1 - ANIMATED
        MapManager.renderLayer(map, this.batch, world.getDelta(), 0, screenMinX, screenMaxX, screenMinY, screenMaxY);

        // LAYER 2 - STATIC - FRAMEBUFFERED
        this.batch.draw(MapManager.getBufferedLayer(map), 0, 0);
    }

    @Override
    protected void processSystem() {
        this.renderWorld();
    }

    @Override
    protected void end() {
        this.batch.end();
    }
}
