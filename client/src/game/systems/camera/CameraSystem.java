package game.systems.camera;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.*;

import static com.artemis.E.E;

@Wire
public class CameraSystem extends BaseSystem {

    public static final float CAMERA_MIN_ZOOM = 1f;
    public static final float CAMERA_MAX_ZOOM = 1f;
    public static final float ZOOM_TIME = 0.5f;
    private final float minZoom;
    private final float maxZoom;
    public OrthographicCamera camera;
    private float desiredZoom;
    private float timeToCameraZoomTarget, cameraZoomOrigin, cameraZoomDuration;
    private Viewport viewport;

    public CameraSystem() {
        this(CAMERA_MIN_ZOOM, CAMERA_MAX_ZOOM);
    }

    public CameraSystem(float minZoom, float maxZoom) {
        this(minZoom, maxZoom, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    // design hecho publico para poder dar el tamaño correcto
    public CameraSystem(float minZoom, float maxZoom, float width, float height) {
        this.maxZoom = maxZoom;
        this.minZoom = minZoom;
        this.desiredZoom = maxZoom;
        setupViewport(width, height);
        camera.zoom = desiredZoom;
    }

    private void setupViewport(float width, float height) {
        createGameCamera(width, height);
    }

    private void createGameCamera(float width, float height) {
        camera = new OrthographicCamera();
        float newWidth = width - 260;
        viewport = new ScalingViewport(Scaling.none, newWidth, height, camera);
        viewport.setScreenBounds(0, 0, (int) newWidth, (int) height);
        viewport.apply(true);
        camera.setToOrtho(true, newWidth, height);
        camera.update();
    }

    @Override
    protected void processSystem() {
        final int width = Gdx.graphics.getWidth() - 260;
        final int height = Gdx.graphics.getHeight();
        Gdx.gl.glViewport(0, 0, width, height);
        viewport.apply(true);
        camera.setToOrtho(true, width, height);
        camera.update();
        if (timeToCameraZoomTarget >= 0) {
            timeToCameraZoomTarget -= getWorld().getDelta();
            float progress = timeToCameraZoomTarget < 0 ? 1 : 1f - timeToCameraZoomTarget / cameraZoomDuration;
            camera.zoom = Interpolation.fastSlow.apply(cameraZoomOrigin, desiredZoom, progress);
        }
    }

    public void zoom(float inout, float duration) {
        cameraZoomOrigin = camera.zoom;
        desiredZoom += inout * 0.025f;
        desiredZoom = MathUtils.clamp(desiredZoom, minZoom, maxZoom);

        //design center es mejor gradual como esta arriba
//        desiredZoom = inout < 0 ? minZoom : maxZoom;
        timeToCameraZoomTarget = cameraZoomDuration = duration;
    }

    @Override
    protected void initialize() {
        super.initialize();
        E(world.create())
                .aOCamera()
                .worldPosOffsets();
    }

}
