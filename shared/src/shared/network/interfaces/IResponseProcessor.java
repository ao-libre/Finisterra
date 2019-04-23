package shared.network.interfaces;

import shared.network.lobby.CreateRoomResponse;
import shared.network.lobby.JoinLobbyResponse;
import shared.network.lobby.JoinRoomResponse;
import shared.network.lobby.StartGameResponse;
import shared.network.movement.MovementResponse;

public interface IResponseProcessor {

    void processResponse(MovementResponse movementResponse);

    void processResponse(CreateRoomResponse createRoomResponse);

    void processResponse(JoinLobbyResponse joinLobbyResponse);

    void processResponse(JoinRoomResponse joinRoomResponse);

    void processResponse(StartGameResponse startGameResponse);
}
