package game.systems.render.world;

import com.artemis.Aspect;
import com.artemis.E;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import entity.world.Object;
import game.handlers.ObjectHandler;
import game.systems.render.BatchRenderingSystem;
import position.WorldPos;
import position.WorldPosOffsets;
import shared.model.map.Tile;
import shared.objects.types.Obj;
import shared.util.Util;

import java.util.Optional;

@Wire(injectInherited = true)
public class ObjectRenderingSystem extends RenderingSystem {

    private ObjectHandler objectHandler;
    private BatchRenderingSystem batchRenderingSystem;

    public ObjectRenderingSystem() {
        super(Aspect.all(Object.class, WorldPos.class));
    }

    @Override
    protected void process(E e) {
        Optional<Obj> object = objectHandler.getObject(e.getObject().index);
        object.ifPresent(obj -> {
            WorldPos objectPos = e.getWorldPos();
            WorldPosOffsets screenPos = Util.toScreen(objectPos);
            if (!e.hasScale()) {
                e.scale(0f);
            } else if (e.getScale().scale >= 1.0f) {
                e.getScale().scale = 1f;
            } else {
                e.getScale().scale += world.delta * 2;
            }
            float scale = Interpolation.swingOut.apply(e.getScale().scale);
            TextureRegion texture = objectHandler.getIngameGraphic(obj);
            float width = scale * texture.getRegionWidth();
            float height = scale * texture.getRegionHeight();
            float x = screenPos.x + (Tile.TILE_PIXEL_WIDTH - width) / 2;
            float y = screenPos.y + (Tile.TILE_PIXEL_HEIGHT - height) / 2;
            batchRenderingSystem.addTask(batch -> batch.draw(texture, x, y, width, height));
        });
    }
}
