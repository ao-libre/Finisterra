package game.systems.render.world;

import com.artemis.Aspect;
import com.artemis.E;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.esotericsoftware.minlog.Log;
import entity.character.Character;
import entity.world.Ground;
import game.handlers.DescriptorHandler;
import game.managers.WorldManager;
import game.systems.camera.CameraSystem;
import graphics.FX;
import model.descriptors.FXDescriptor;
import model.textures.BundledAnimation;
import position.Pos2D;
import position.WorldPos;
import shared.model.map.Tile;
import shared.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.artemis.E.E;
// TODO deduplicate code (see FXsRenderingSystem)
@Wire
public class GroundFXsRenderingSystem extends IteratingSystem {

    private SpriteBatch batch;

    private CameraSystem cameraSystem;

    private Map<Integer, Map<Integer, BundledAnimation>> fxs = new HashMap<>();
    private int srcFunc;
    private int dstFunc;

    public GroundFXsRenderingSystem(SpriteBatch batch) {
        super(Aspect.all(FX.class, WorldPos.class, Ground.class).exclude(Character.class));
        this.batch = batch;
    }

    @Override
    protected void begin() {
        cameraSystem.camera.update();
        srcFunc = batch.getBlendSrcFunc();
        dstFunc = batch.getBlendDstFunc();
        batch.setProjectionMatrix(cameraSystem.camera.combined);
        batch.enableBlending();
        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_DST_ALPHA);
    }

    @Override
    protected void end() {
        batch.end();
        batch.setBlendFunction(srcFunc, dstFunc);
    }

    @Override
    protected void removed(int entityId) {
        super.removed(entityId);
        fxs.remove(entityId);
    }

    @Override
    protected void process(int entityId) {
        E entity = E(entityId);
        Pos2D screenPos = Util.toScreen(entity.worldPosPos2D());
        final FX fx = entity.getFX();
        if (fx.fxs.isEmpty()) {
            return;
        }
        List<Integer> removeFXs = new ArrayList<>();
        drawFXs(entityId, screenPos, fx, removeFXs);
        removeFXs.forEach(remove -> fx.removeFx(remove));
        if (fx.fxs.isEmpty()) {
            fxs.remove(entityId);


        }
    }

    private void drawFXs(int entityId, Pos2D screenPos, FX fx, List<Integer> removeFXs) {
        if (fx == null || fx.fxs == null) {
            return;
        }
        fx.fxs.forEach(fxId -> {
            FXDescriptor fxDescriptor = DescriptorHandler.getFX(fxId);
            Map<Integer, BundledAnimation> anims = this.fxs.computeIfAbsent(entityId, id -> new HashMap<>());
            BundledAnimation anim = anims.computeIfAbsent(fxId, fxGraphic -> new BundledAnimation(DescriptorHandler.getGraphic(fxDescriptor.getIndexs()[0]), false));
            TextureRegion graphic = anim.getGraphic(false);
            batch.draw(graphic, screenPos.x + (Tile.TILE_PIXEL_WIDTH - graphic.getRegionWidth()) / 2 + fxDescriptor.getOffsetX(), screenPos.y - graphic.getRegionHeight() + fxDescriptor.getOffsetY());
            anim.setAnimationTime(anim.getAnimationTime() + getWorld().getDelta() * (anim.getFrames().size * 0.33f));
            float animationTime = anim.getAnimationTime();
            float animationDuration = anim.getAnimation().getAnimationDuration();
            Log.info(E(entityId).networkId() + "- Time: " + animationTime  + " .Duration: " + animationDuration);
            if (anim.isAnimationFinished()) {
                removeFXs.add(fxId);
                anims.remove(fxId);
                Log.info("Removing entity: " + E(entityId).networkId());
                WorldManager.unregisterEntity(E(entityId).networkId());
            }
        });
    }


}
