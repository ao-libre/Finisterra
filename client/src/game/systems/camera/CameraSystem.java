package game.systems.camera;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Setup and manages basic orthographic camera.
 *
 * @author Daan van Yperen
 */
public class CameraSystem extends BaseSystem {

    private final float zoom;
    public OrthographicCamera camera;
    public OrthographicCamera guiCamera;

    public CameraSystem(float zoom, float width, float height) {
        this.zoom = zoom;
        float zoomFactorInverter = 1f / zoom;
        setupViewport(width * zoomFactorInverter,
                height * zoomFactorInverter);
    }

    /**
     * @param zoom How much
     */
    public CameraSystem(float zoom) {
        this(zoom, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    protected void setupViewport(float width, float height) {
        createGameCamera(width, height);
        createGuiCamera(width, height);
    }

    private void createGuiCamera(float width, float height) {
        guiCamera = new OrthographicCamera(width, height);
        guiCamera.setToOrtho(false, width, height);
        guiCamera.update();
    }

    private void createGameCamera(float width, float height) {
        camera = new OrthographicCamera(width, height);
        camera.setToOrtho(true, width, height);
        camera.update();
    }

    @Override
    protected void processSystem() {

    }
}
