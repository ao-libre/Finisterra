package shared.network.interfaces;

import shared.network.interaction.DropItem;
import shared.network.inventory.InventoryUpdate;
import shared.network.lobby.JoinRoomNotification;
import shared.network.lobby.NewRoomNotification;
import shared.network.lobby.player.ChangeHeroNotification;
import shared.network.lobby.player.ChangeTeamNotification;
import shared.network.lobby.player.ReadyNotification;
import shared.network.movement.MovementNotification;
import shared.network.notifications.EntityUpdate;
import shared.network.notifications.FXNotification;
import shared.network.notifications.RemoveEntity;

public class DefaultNotificationProcessor implements INotificationProcessor {

    @Override
    public void defaultProcess(INotification notification) {

    }

    @Override
    public void processNotification(EntityUpdate notification) {

    }

    @Override
    public void processNotification(RemoveEntity removeEntity) {

    }

    @Override
    public void processNotification(InventoryUpdate inventoryUpdate) {

    }

    @Override
    public void processNotification(DropItem dropItem) {

    }

    @Override
    public void processNotification(MovementNotification movementNotification) {

    }

    @Override
    public void processNotification(FXNotification fxNotification) {

    }

    @Override
    public void processNotification(JoinRoomNotification joinRoomNotification) {

    }

    @Override
    public void processNotification(NewRoomNotification newRoomNotification) {

    }

    @Override
    public void processNotification(ChangeTeamNotification changeTeamNotification) {

    }

    @Override
    public void processNotification(ChangeHeroNotification changeHeroNotification) {

    }

    @Override
    public void processNotification(ReadyNotification readyNotification) {

    }
}
