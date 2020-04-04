package server.systems.network;

import com.artemis.BaseSystem;
import com.artemis.Component;
import com.artemis.annotations.Wire;
import server.systems.manager.ComponentManager;
import server.systems.manager.WorldManager;
import shared.network.notifications.EntityUpdate;
import shared.network.notifications.RemoveEntity;
import shared.util.EntityUpdateBuilder;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.artemis.E.E;

@Wire
public class EntityUpdateSystem extends BaseSystem {

    private WorldManager worldManager;
    private ComponentManager componentManager;

    private final Map<Integer, Deque<EntityUpdate>> entityUpdates;
    private final Map<Integer, Deque<EntityUpdate>> publicUpdates;

    public EntityUpdateSystem() {
        entityUpdates = new ConcurrentHashMap<>();
        publicUpdates = new ConcurrentHashMap<>();
    }

    @Override
    protected void processSystem() {
        // send all updates
        entityUpdates.forEach((id, update) -> worldManager.sendEntityUpdate(id, update.toArray(new EntityUpdate[0])));
        entityUpdates.clear();

        publicUpdates.forEach((id, update) -> worldManager.notifyToNearEntities(id, update.toArray(new EntityUpdate[0])));
        publicUpdates.clear();
    }

    public void add(EntityUpdate update, UpdateTo updateTo) {
        add(update.entityId, update, updateTo);
    }

    public void add(int entity, EntityUpdate update, UpdateTo updateTo) {
        // search all updates of this update component.entity and remove them
        if (update instanceof RemoveEntity) {
            if (entityUpdates.containsKey(entity)) {
                entityUpdates.get(entity).removeIf(entityUpdate -> entityUpdate.entityId == update.entityId);
            }
            if (publicUpdates.containsKey(entity)) {
                publicUpdates.get(entity).removeIf(entityUpdate -> entityUpdate.entityId == update.entityId);
            }
        }

        switch (updateTo) {
            case ALL:
                addUpdate(entity, update, entityUpdates);
                addUpdate(entity, update, publicUpdates);
                break;
            case NEAR:
                addUpdate(entity, update, publicUpdates);
                break;
            case ENTITY:
                addUpdate(entity, update, entityUpdates);
                break;
        }
    }

    private void addUpdate(int entity, EntityUpdate update, Map<Integer, Deque<EntityUpdate>> updates) {
        updates.putIfAbsent(entity, new ConcurrentLinkedDeque<>());
        updates.computeIfPresent(entity, (id, otherUpdate) -> {
            Optional<EntityUpdate> toJoin = otherUpdate.stream().filter(u -> u.entityId == update.entityId).findFirst();
            if (toJoin.isPresent()) {
                EntityUpdateBuilder.join(toJoin.get(), update);
            } else {
                otherUpdate.add(update);
            }
            return otherUpdate;
        });

    }

    // Attach component.entity to another component.entity and send update to all near entities including component.entity
    public void attach(int entity, int entityToAttach) {
        E(entityToAttach).refId(entity);
        List<Component> components = componentManager.getComponents(entityToAttach, ComponentManager.Visibility.CLIENT_PUBLIC);
        EntityUpdate update = EntityUpdateBuilder.of(entityToAttach).withComponents(components).build();
        add(entity, update, UpdateTo.ALL);
    }

    public void detach(int entity, Integer sEntity) {
        add(entity, EntityUpdateBuilder.delete(sEntity), UpdateTo.ALL);
        world.delete(sEntity); // TODO unregister in worldManager?
    }
}
