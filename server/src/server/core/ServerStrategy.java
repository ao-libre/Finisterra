package server.core;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Server;
import net.mostlyoriginal.api.network.marshal.common.MarshalState;
import net.mostlyoriginal.api.network.marshal.kryonet.KryonetMarshalStrategy;

import java.io.IOException;

public class ServerStrategy extends KryonetMarshalStrategy {

    private final int tcpPort;
    private final int udpPort;

    public ServerStrategy(int tcpPort, int udpPort) {
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        endpoint = new Server();
    }

    @Override
    protected void connectEndpoint() {
        try {
            ((Server) endpoint).bind(tcpPort, udpPort);
            Gdx.app.log("Server initialization", "Listening connections in ports TCP: " + tcpPort + " and UDP: " + udpPort);
            state = MarshalState.STARTED;
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.log("Server initialization", "Server port binding has FAILED!");
            state = MarshalState.FAILED_TO_START;
        }
    }

    @Override
    public void sendToAll(Object o) {
        ((Server) endpoint).sendToAllTCP(o);
    }

    public void sendTo(int connectionId, Object o) {
        ((Server) endpoint).sendToTCP(connectionId, o);
    }

}
