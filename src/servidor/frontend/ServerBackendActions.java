package servidor.frontend;

import java.io.IOException;

import servidor.backend.ServerListener;
import servidor.backend.ServidorSocket;

public class ServerBackendActions implements AccionesServidor {
    private ServidorMain window;
    private ServidorSocket server;

    public void setWindow(ServidorMain window) {
        this.window = window;
    }

    @Override
    public void iniciar(int port) {
        ServidorSocket newServer = new ServidorSocket(new ServerListener() {
            @Override
            public void started(int p) {
                window.setEstado(true, p);
                window.agregarLog("Server listening on port " + p);
            }

            @Override
            public void stopped() {
                window.setEstado(false, 0);
                window.agregarLog("Server stopped");
            }

            @Override
            public void log(String line) {
                window.agregarLog(line);
            }

            @Override
            public void clientJoined(String nickname) {
                window.agregarCliente(nickname);
            }

            @Override
            public void clientLeft(String nickname) {
                window.quitarCliente(nickname);
            }
        });

        server = newServer;

        try {
            newServer.start(port);
        } catch (IOException e) {
            window.mostrarError("Unable to start server: " + e.getMessage());
        }
    }

    @Override
    public void detener() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public void enviarATodos(String text) {
        if (server != null) {
            server.broadcastAdmin(text);
        }
    }

    @Override
    public void enviarPrivado(String nickname, String text) {
        if (server != null) {
            server.sendAdminPrivate(nickname, text);
        }
    }

    @Override
    public void expulsar(String nickname) {
        if (server != null) {
            server.kick(nickname);
        }
    }
}
