package server.systems.ai;

import com.artemis.Aspect;
import com.artemis.E;
import com.artemis.FluidIteratingSystem;
import com.artemis.annotations.Wire;
import entity.npc.Respawn;
import server.systems.manager.WorldManager;

@Wire
public class RespawnSystem extends FluidIteratingSystem {

    private WorldManager worldManager;

    public RespawnSystem () {
        super(Aspect.all(Respawn.class));
    }

    @Override
    protected void process(E e) {
        Respawn respawn = e.getRespawn();
        respawn.setTime(respawn.getTime() - world.getDelta());
        if (respawn.getTime() <= 0) {
            worldManager.createNPC(respawn.getNpcId(), respawn.getPos().toWorldPos());
            e.deleteFromWorld();
        }
    }
}
