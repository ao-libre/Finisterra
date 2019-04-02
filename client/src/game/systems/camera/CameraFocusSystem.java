package game.systems.camera;

import camera.Focused;
import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.IteratingSystem;
import position.Pos2D;
import position.WorldPos;
import shared.util.Util;

import static com.artemis.E.E;

@Wire
public class CameraFocusSystem extends IteratingSystem {

    public CameraFocusSystem() {
        super(Aspect.all(Focused.class, WorldPos.class));
    }

    @Override
    protected void process(int player) {
        Entity camera = world.getSystem(TagManager.class).getEntity("camera");
        Pos2D cameraPos = camera.getComponent(Pos2D.class);
        Pos2D pos = Util.toScreen(E(player).worldPosPos2D());
        cameraPos.x = pos.x;
        cameraPos.y = pos.y;
    }

}
