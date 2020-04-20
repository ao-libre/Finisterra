package server.systems;

import com.artemis.Aspect;
import com.artemis.E;
import com.artemis.annotations.Wire;
import component.entity.character.Character;
import component.entity.character.info.Name;
import server.systems.user.UserSystem;

@Wire
public class WorldSaveSystem extends IntervalFluidIteratingSystem {

    private UserSystem userSystem;

    public WorldSaveSystem(float interval) {
        super(Aspect.all(Character.class, Name.class), interval);
    }

    @Override
    protected void process(E e) {
        userSystem.save(e);
    }
}
