package game.systems.render.ui;

import camera.Focused;
import com.artemis.Aspect;
import com.artemis.E;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import game.systems.OrderedEntityProcessingSystem;
import game.systems.camera.CameraSystem;
import game.utils.Fonts;
import model.textures.TextureUtils;
import position.WorldPos;

import java.util.Comparator;

import static com.artemis.E.E;

@Wire
public class CoordinatesRenderingSystem extends OrderedEntityProcessingSystem {

    public static final float ALPHA = 0.5f;
    private static final int BORDER = 6;
    private SpriteBatch batch;
    private CameraSystem cameraSystem;

    public CoordinatesRenderingSystem(SpriteBatch batch) {
        super(Aspect.all(Focused.class, WorldPos.class));
        this.batch = batch;
    }

    @Override
    protected void begin() {
        cameraSystem.guiCamera.update();
        batch.setProjectionMatrix(cameraSystem.guiCamera.combined);
        batch.begin();
    }

    @Override
    protected void end() {
        batch.end();
    }

    @Override
    protected void process(Entity e) {
        E player = E(e);
        WorldPos worldPos = player.getWorldPos();

        drawCoordinates(50, 50, worldPos);
    }

    private void drawCoordinates(int offsetX, int offsetY, WorldPos worldPos) {
        String worldPosString = "[" + worldPos.map + "-" + worldPos.x + "-" + worldPos.y + "]";
        BitmapFont font = Fonts.CONSOLE_FONT;
        Fonts.layout.setText(font, worldPosString);
        float fontX = cameraSystem.guiCamera.viewportWidth - Fonts.layout.width - offsetX;
        float fontY = cameraSystem.guiCamera.viewportHeight - offsetY;
        //background
        Color oldColor = batch.getColor();
        Color black = Color.BLACK.cpy();
        batch.setColor(black.r, black.g, black.b, ALPHA);
        batch.draw(TextureUtils.white, fontX - (BORDER >> 1), fontY - (BORDER >> 1), Fonts.layout.width + BORDER, Fonts.layout.height + BORDER);
        //text
        batch.setColor(Color.WHITE.cpy());
        font.draw(batch, Fonts.layout, fontX, fontY + Fonts.layout.height);
        batch.setColor(oldColor);
    }

    @Override
    protected Comparator<? super Entity> getComparator() {
        return Comparator.comparingInt(entity -> E(entity).getWorldPos().y);
    }
}
