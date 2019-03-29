package server.network;

import server.manager.*;
import server.map.Maps;
import server.utils.WorldUtils;
import shared.interfaces.Constants;
import shared.interfaces.FXs;
import shared.model.Spell;
import shared.network.combat.AttackRequest;
import shared.network.combat.SpellCastRequest;
import shared.network.interaction.MeditateRequest;
import shared.network.interaction.TakeItemRequest;
import shared.network.interaction.TalkRequest;
import shared.network.interfaces.IRequestProcessor;
import shared.network.inventory.InventoryUpdate;
import shared.network.inventory.ItemActionRequest;
import shared.network.login.LoginOK;
import shared.network.login.LoginRequest;
import shared.network.movement.MovementNotification;
import shared.network.movement.MovementRequest;
import shared.network.movement.MovementResponse;
import shared.network.notifications.EntityUpdate;
import shared.network.notifications.FXNotification;
import shared.util.MapUtils;
import com.artemis.Component;
import com.artemis.E;
import com.artemis.Entity;
import com.artemis.World;
import com.esotericsoftware.minlog.Log;
import entity.*;
import entity.Object;
import entity.character.info.Inventory;
import entity.character.states.Meditating;
import graphics.FX;
import movement.Destination;
import physics.AttackAnimation;
import position.WorldPos;

import java.util.*;

import static com.artemis.E.E;

/**
 * Every packet received from users will be processed here
 */
public class ServerRequestProcessor implements IRequestProcessor {

    /**
     * @param request LoginRequest
     * @param connectionId connection id
     */
    @Override
    public void processRequest(LoginRequest request, int connectionId) {
        final Entity entity = WorldManager.createEntity(request.username, request.heroId);
        NetworkComunicator.sendTo(connectionId, new EntityUpdate(Maps.mapEntity, WorldUtils.getComponents(Maps.mapEntity), new Class[0]));
        NetworkComunicator.sendTo(connectionId, new EntityUpdate(entity.getId(), WorldUtils.getComponents(entity.getId()), new Class[0]));
        NetworkComunicator.sendTo(connectionId, new LoginOK(entity.getId()));
        WorldManager.registerEntity(connectionId, entity.getId());
    }

    /**
     * Process {@link MovementRequest}. If it is valid, move player and notify.
     * @param request
     * @param connectionId
     * @see MovementRequest
     */
    @Override
    public void processRequest(MovementRequest request, int connectionId) {
        // TODO check map changed

        // validate if valid
        int playerId = NetworkComunicator.getPlayerByConnection(connectionId);

        // update server entity
        E player = E(playerId);

        player.headingCurrent(WorldUtils.getHeading(request.movement));

        WorldPos worldPos = player.getWorldPos();
        WorldPos oldPos = new WorldPos(worldPos);
        WorldPos nextPos = WorldUtils.getNextPos(worldPos, request.movement);
        boolean blocked = false; //MapUtils.isBlocked(MapManager.get(nextPos.map), nextPos);
        boolean occupied = MapUtils.hasEntity(MapManager.getNearEntities(playerId), nextPos);
        if (!(player.hasImmobile() || blocked || occupied)) {
            Log.info("Player: " + playerId + ". Moved from: " + oldPos + " to: " + nextPos);
            player.worldPosMap(nextPos.map);
            player.worldPosX(nextPos.x);
            player.worldPosY(nextPos.y);
        } else {
            Log.info("Player: " + playerId + ". Wants to move to: " + nextPos + ", but stay at: " + oldPos);
            nextPos = oldPos;
        }

        MapManager.movePlayer(playerId, Optional.of(oldPos));

        // notify near users
        if (!nextPos.equals(oldPos)) {
            WorldManager.notifyToNearEntities(playerId, new MovementNotification(playerId, new Destination(nextPos, request.movement)));
        } else {
            WorldManager.notifyToNearEntities(playerId, new EntityUpdate(playerId, new Component[]{player.getHeading()}, new Class[0])); // is necessary?
        }

        // notify user
        NetworkComunicator.sendTo(connectionId, new MovementResponse(request.requestNumber, nextPos));
    }

    /**
     * Attack and notify, if it was effective or not, to near users
     * @param attackRequest attack type
     * @param connectionId user connection id
     */
    @Override
    public void processRequest(AttackRequest attackRequest, int connectionId) {
        int playerId = NetworkComunicator.getPlayerByConnection(connectionId);
        E player = E(playerId);

        WorldPos worldPos = player.getWorldPos();
        Heading heading = player.getHeading();
        WorldPos facingPos = WorldUtils.getFacingPos(worldPos, heading);

        Optional<Integer> victim = MapManager.getNearEntities(playerId)
                .stream()
                .filter(near -> E(near).hasWorldPos() && E(near).getWorldPos().equals(facingPos))
                .findFirst();
        if (victim.isPresent() && E(victim.get()).hasCharHero()) {
            Optional<Integer> damage = CombatManager.attack(playerId, victim.get());
            if (damage.isPresent()) {
                CombatManager.notify(victim.get(), new CombatMessage("-" + Integer.toString(damage.get())));
                // TODO fix fxgrh
                WorldManager.notifyUpdate(victim.get(), new FXNotification(victim.get(), FXs.FX_BLOOD));
            } else {
                CombatManager.notify(playerId, new CombatMessage(CombatManager.MISS));
            }
        } else {
            CombatManager.notify(playerId, new CombatMessage(CombatManager.MISS));
        }
        WorldManager.notifyUpdate(playerId, new EntityUpdate(playerId, new Component[]{new AttackAnimation()}, new Class[0]));
    }

    /**
     * User wants to use or act over an item, do action and notify.
     * @param itemAction user slot number
     * @param connectionId user connection id
     */
    @Override
    public void processRequest(ItemActionRequest itemAction, int connectionId) {
        int playerId = NetworkComunicator.getPlayerByConnection(connectionId);
        E player = E(playerId);
        Inventory.Item[] userItems = player.getInventory().items;
        int itemIndex = itemAction.getSlot();
        if (itemIndex < userItems.length) {
            // if equipable
            Inventory.Item item = userItems[itemIndex];
            if (item == null) {
                return;
            }
            if (ItemManager.isEquippable(item)) {
                // modify user equipment
                ItemManager.equip(playerId, itemIndex, item);
            } else if (ItemManager.isUsable(item)) {
                ItemManager.use(playerId, itemIndex, item);
            }
        }
    }

    /**
     * User wants to meditate
     * @param meditateRequest request (no data)
     * @param connectionId user connection id
     */
    @Override
    public void processRequest(MeditateRequest meditateRequest, int connectionId) {
        int playerId = NetworkComunicator.getPlayerByConnection(connectionId);
        E player = E(playerId);
        boolean meditating = player.isMeditating();
        if (meditating) {
            player.removeFX();
            player.removeMeditating();
            WorldManager.notifyUpdate(playerId, new EntityUpdate(playerId, new Component[0], new Class[]{FX.class, Meditating.class}));
        } else {
            player.fXAddParticleEffect(Constants.MEDITATE_NW_FX);
            player.meditating();
            WorldManager.notifyUpdate(playerId, new EntityUpdate(playerId, new Component[]{player.getMeditating(), player.getFX()}, new Class[0]));
        }
    }

    /**
     * Notify near users that user talked
     * @param talkRequest talk request with message
     * @param connectionId user connection id
     */
    @Override
    public void processRequest(TalkRequest talkRequest, int connectionId) {
        int playerId = NetworkComunicator.getPlayerByConnection(connectionId);
        WorldManager.notifyUpdate(playerId, new EntityUpdate(playerId, new Component[]{new Dialog(talkRequest.getMessage())}, new Class[0]));
    }

    /**
     * User wants to take something from ground
     * @param takeItemRequest request (no data)
     * @param connectionId user connection id
     */
    @Override
    public void processRequest(TakeItemRequest takeItemRequest, int connectionId) {
        int playerId = NetworkComunicator.getPlayerByConnection(connectionId);
        E player = E(playerId);
        WorldPos playerPos = player.getWorldPos();
        MapManager.getNearEntities(playerId)
                .stream()
                .filter(entityId -> {
                    WorldPos entityPos = E(entityId).getWorldPos();
                    return E(entityId).hasObject() && entityPos.x == playerPos.x && entityPos.y == playerPos.y;
                })
                .findFirst()
                .ifPresent(objectEntityId -> {
                    Object object = E(objectEntityId).getObject();
                    int index = player.getInventory().add(object.index, object.count);
                    if (index >= 0) {
                        Log.info("Adding item to index: " + index);
                        InventoryUpdate update = new InventoryUpdate();
                        update.add(index, player.getInventory().items[index]);
                        NetworkComunicator.sendTo(connectionId, update);
                        WorldManager.unregisterEntity(objectEntityId);
                        MapManager.removeEntity(objectEntityId);
                    } else {
                        Log.info("Could not put item in inventory (FULL?)");
                    }
                });
    }

    @Override
    public void processRequest(SpellCastRequest spellCastRequest, int connectionId) {
        int playerId = NetworkComunicator.getPlayerByConnection(connectionId);
        Spell spell = spellCastRequest.getSpell();
        WorldPos worldPos = spellCastRequest.getWorldPos();
        Log.info("Processing spell cast pos: " + spellCastRequest.getWorldPos());
        Set<Integer> entities = new HashSet<>(MapManager.getNearEntities(playerId));
        entities.add(playerId);
        Optional<Integer> target = entities
                .stream()
                .filter(entity -> E(entity).getWorldPos().equals(worldPos))
                .findFirst();
        if (target.isPresent()) {
            SpellManager.castSpell(playerId, target.get(), spell);
            AttackAnimation attackAnimation = new AttackAnimation();
            WorldManager.notifyUpdate(playerId, new EntityUpdate(playerId, new Component[]{attackAnimation}, new Class[0]));
        } else {
            List<WorldPos> area = getArea(worldPos, 3);
            int fxGrh = spell.getFxGrh();
            if (fxGrh > 0) {
                area.forEach(pos -> {
                    World world = WorldManager.getWorld();
                    int entity = world.create();
                    // TODO notify all near users instead of playerid
                    WorldManager.notifyUpdate(playerId, new EntityUpdate(entity, new Component[]{pos, new Ground()}, new Class[0]));
                    WorldManager.notifyUpdate(playerId, new FXNotification(entity, fxGrh - 1));
                    world.delete(entity);
                });
            }
        }

    }

    private List<WorldPos> getArea(WorldPos worldPos, int range /*impar*/) {
        List<WorldPos> positions = new ArrayList<>();
        int i = range / 2;
        for (int x = worldPos.x - i; x <= worldPos.x + i ; x++) {
            for (int y = worldPos.y - i; y <= worldPos.y + i ; y++) {
                positions.add(new WorldPos(x, y, worldPos.map));
            }
        }
        return positions;
    }

}
