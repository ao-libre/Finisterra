package ar.com.tamborindeguy.client.systems.physics;

import camera.Focused;
import com.artemis.Aspect;
import com.artemis.E;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import physics.AOPhysics;

import static ar.com.tamborindeguy.client.utils.AlternativeKeys.*;
import static com.artemis.E.E;

public class PlayerInputSystem extends IteratingSystem {


    public PlayerInputSystem() {
        super(Aspect.all(Focused.class, AOPhysics.class));
    }

    @Override
    protected void process(int entityId) {
        E player = E(entityId);
        AOPhysics aoPhysics = player.getAOPhysics();
        boolean isWriting = player.isWriting();
        move(aoPhysics, AOPhysics.Movement.UP, !isWriting && Gdx.input.isKeyPressed(MOVE_UP));
        move(aoPhysics, AOPhysics.Movement.DOWN, !isWriting && Gdx.input.isKeyPressed(MOVE_DOWN));
        move(aoPhysics, AOPhysics.Movement.LEFT, !isWriting && Gdx.input.isKeyPressed(MOVE_LEFT));
        move(aoPhysics, AOPhysics.Movement.RIGHT, !isWriting && Gdx.input.isKeyPressed(MOVE_RIGHT));
    }

    private void move(AOPhysics aoPhysics, AOPhysics.Movement movement, boolean moving) {
        if (moving) {
            if (!aoPhysics.intentions.contains(movement)) {
                aoPhysics.addIntention(movement);
            }
        } else if (!moving) {
            aoPhysics.removeIntention(movement);
        }
    }

}
