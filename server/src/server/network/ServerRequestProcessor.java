package server.network;

import com.artemis.E;
import com.artemis.World;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.minlog.Log;
import entity.character.info.Inventory;
import entity.world.Dialog;
import entity.world.Object;
import movement.Destination;
import position.WorldPos;
import server.combat.CombatSystem;
import server.core.Server;
import server.systems.manager.*;
import server.systems.MeditateSystem;
import server.utils.WorldUtils;
import shared.model.AttackType;
import shared.model.lobby.Player;
import shared.model.map.Map;
import shared.network.combat.AttackRequest;
import shared.network.combat.SpellCastRequest;
import shared.network.interaction.MeditateRequest;
import shared.network.interaction.TakeItemRequest;
import shared.network.interaction.TalkRequest;
import shared.network.interfaces.DefaultRequestProcessor;
import shared.network.inventory.InventoryUpdate;
import shared.network.inventory.ItemActionRequest;
import shared.network.lobby.player.PlayerLoginRequest;
import shared.network.movement.MovementNotification;
import shared.network.movement.MovementRequest;
import shared.network.movement.MovementResponse;
import shared.network.notifications.EntityUpdate.EntityUpdateBuilder;
import shared.network.time.TimeSyncRequest;
import shared.network.time.TimeSyncResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.artemis.E.E;
import static server.utils.WorldUtils.WorldUtils;

/**
 * Every packet received from users will be processed here
 */
public class ServerRequestProcessor extends DefaultRequestProcessor {

    private Server server;

    public ServerRequestProcessor(Server server) {
        this.server = server;
    }

    private World getWorld() {
        return server.getWorld();
    }

    public Server getServer() {
        return server;
    }

    private NetworkManager getNetworkManager() {
        return getServer().getNetworkManager();
    }

    private MapManager getMapManager() {
        return getServer().getMapManager();
    }

    private WorldManager getWorldManager() {
        return getServer().getWorldManager();
    }

    private CombatSystem getCombatSystem(AttackType type) {
        if (type.equals(AttackType.PHYSICAL)) {
            return getServer().getCombatManager();
        }
        // TODO
        return getServer().getCombatManager();
    }

    private ItemManager getItemManager() {
        return getServer().getItemManager();
    }

    private SpellManager getSpellManager() {
        return getServer().getSpellManager();
    }

    private List<WorldPos> getArea(WorldPos worldPos, int range /*impar*/) {
        List<WorldPos> positions = new ArrayList<>();
        int i = range / 2;
        for (int x = worldPos.x - i; x <= worldPos.x + i; x++) {
            for (int y = worldPos.y - i; y <= worldPos.y + i; y++) {
                positions.add(new WorldPos(x, y, worldPos.map));
            }
        }
        return positions;
    }

    @Override
    public void processRequest(PlayerLoginRequest playerLoginRequest, int connectionId) {
        Player player = playerLoginRequest.getPlayer();
        getServer().getWorldManager().login(connectionId, player);
        getServer().getWorldManager().addNPC();
    }

    /**
     * Process {@link MovementRequest}. If it is valid, move player and notify.
     *
     * @param request
     * @param connectionId
     * @see MovementRequest
     */
    @Override
    public void processRequest(MovementRequest request, int connectionId) {
        NetworkManager networkManager = getWorld().getSystem(NetworkManager.class);
        int playerId = networkManager.getPlayerByConnection(connectionId);
        E player = E(playerId);
        WorldUtils worldUtils = WorldUtils(getServer().getWorld());
        player.headingCurrent(worldUtils.getHeading(request.movement));

        WorldPos worldPos = player.getWorldPos();
        WorldPos oldPos = new WorldPos(worldPos);
        WorldPos nextPos = worldUtils.getNextPos(worldPos, request.movement);
        MapManager mapManager = getServer().getMapManager();
        Map map = mapManager.get(nextPos.map);
        boolean blocked = mapManager.getHelper().isBlocked(map, nextPos);
        boolean occupied = mapManager.getHelper().hasEntity(getMapManager().getNearEntities(playerId), nextPos);
        if (!(player.hasImmobile() || blocked || occupied)) {
            player.worldPosMap(nextPos.map);
            player.worldPosX(nextPos.x);
            player.worldPosY(nextPos.y);
        } else {
            nextPos = oldPos;
        }

        getMapManager().movePlayer(playerId, Optional.of(oldPos));

        // notify near users
        if (!nextPos.equals(oldPos)) {
            getWorldManager().notifyToNearEntities(playerId, new MovementNotification(playerId, new Destination(nextPos, request.movement)));
        } else {
            getWorldManager().notifyToNearEntities(playerId, EntityUpdateBuilder.of(playerId).withComponents(player.getHeading()).build());
        }

        // notify user
        networkManager.sendTo(connectionId, new MovementResponse(request.requestNumber, nextPos));
    }

    /**
     * Attack and notify, if it was effective or not, to near users
     *
     * @param attackRequest attack type
     * @param connectionId  user connection id
     */
    @Override
    public void processRequest(AttackRequest attackRequest, int connectionId) {
        int playerId = getNetworkManager().getPlayerByConnection(connectionId);
        getCombatSystem(AttackType.PHYSICAL).entityAttack(playerId, Optional.empty());
    }

    /**
     * User wants to use or act over an item, do action and notify.
     *
     * @param itemAction   user slot number
     * @param connectionId user connection id
     */
    @Override
    public void processRequest(ItemActionRequest itemAction, int connectionId) {
        int playerId = getNetworkManager().getPlayerByConnection(connectionId);
        E player = E(playerId);
        Inventory.Item[] userItems = player.getInventory().items;
        int itemIndex = itemAction.getSlot();
        if (itemIndex < userItems.length) {
            // if equipable
            Inventory.Item item = userItems[itemIndex];
            if (item == null) {
                return;
            }
            if (getItemManager().isEquippable(item)) {
                // modify user equipment
                getItemManager().equip(playerId, itemIndex, item);
            } else if (getItemManager().isUsable(item)) {
                getItemManager().use(playerId, item);
            }
        }
    }

    /**
     * User wants to meditate
     *
     * @param meditateRequest request (no data)
     * @param connectionId    user connection id
     */
    @Override
    public void processRequest(MeditateRequest meditateRequest, int connectionId) {
        int playerId = getNetworkManager().getPlayerByConnection(connectionId);
        MeditateSystem meditateSystem = server.getWorld().getSystem(MeditateSystem.class);
        meditateSystem.toggle(playerId);
    }

    /**
     * Notify near users that user talked
     *
     * @param talkRequest  talk request with message
     * @param connectionId user connection id
     */
    @Override
    public void processRequest(TalkRequest talkRequest, int connectionId) {
        int playerId = getNetworkManager().getPlayerByConnection(connectionId);
        getWorldManager().notifyUpdate(playerId, EntityUpdateBuilder.of(playerId).withComponents(new Dialog(talkRequest.getMessage())).build());
    }

    /**
     * User wants to take something from ground
     *
     * @param takeItemRequest request (no data)
     * @param connectionId    user connection id
     */
    @Override
    public void processRequest(TakeItemRequest takeItemRequest, int connectionId) {
        int playerId = getNetworkManager().getPlayerByConnection(connectionId);
        E player = E(playerId);
        WorldPos playerPos = player.getWorldPos();
        getMapManager().getNearEntities(playerId)
                .stream()
                .filter(entityId -> {
                    WorldPos entityPos = E(entityId).getWorldPos();
                    return E(entityId).hasObject() && entityPos.x == playerPos.x && entityPos.y == playerPos.y;
                })
                .findFirst()
                .ifPresent(objectEntityId -> {
                    Object object = E(objectEntityId).getObject();
                    int index = player.getInventory().add(object.index, object.count, false);
                    if (index >= 0) {
                        Log.info("Adding item to index: " + index);
                        InventoryUpdate update = new InventoryUpdate();
                        update.add(index, player.getInventory().items[index]);
                        getNetworkManager().sendTo(connectionId, update);
                        getWorldManager().unregisterEntity(objectEntityId);
                    } else {
                        Log.info("Could not put item in inventory (FULL?)");
                    }
                });
    }

    @Override
    public void processRequest(SpellCastRequest spellCastRequest, int connectionId) {
        int playerId = getNetworkManager().getPlayerByConnection(connectionId);
        getServer().getMagicCombatManager().spell(playerId, spellCastRequest);
    }

    @Override
    public void processRequest(TimeSyncRequest request, int connectionId) {
        long receiveTime = TimeUtils.millis();
        TimeSyncResponse response = new TimeSyncResponse();
        response.receiveTime = receiveTime;
        response.requestId = request.requestId;
        response.sendTime = TimeUtils.millis();
        getNetworkManager().sendTo(connectionId, response);
    }

}
