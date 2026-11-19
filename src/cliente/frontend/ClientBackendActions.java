package cliente.frontend;

import java.io.IOException;

import cliente.backend.ClientBackend;
import cliente.backend.ClientListener;

public class ClientBackendActions implements AccionesCliente {
    private ClientePrueba window;
    private ClientBackend backend;
    private volatile boolean connected;
    private volatile boolean rejected;
    private volatile boolean closing;

    public void setWindow(ClientePrueba window) {
        this.window = window;
    }

    @Override
    public void conectar(String nickname) {
        connected = false;
        rejected = false;
        closing = false;

        ClientBackend newBackend = new ClientBackend(new ClientListener() {
            @Override
            public void message(String type, String from, String text) {
                if (type.equals("G")) {
                    window.agregarMensaje(from + ": " + text);
                } else if (type.equals("P")) {
                    window.agregarMensaje("[private] " + from + ": " + text);
                } else if (type.equals("S")) {
                    window.agregarSistema(text);
                } else if (type.equals("X")) {
                    rejected = true;
                    window.volverAlNombre(text);
                }
            }

            @Override
            public void disconnected() {
                connected = false;
                if (!closing && !rejected) {
                    window.volverAlNombre("Server disconnected");
                }
            }
        });

        backend = newBackend;

        new Thread(() -> {
            try {
                newBackend.connect(nickname);
                connected = true;
                window.mostrarChat(nickname);
            } catch (IOException e) {
                window.volverAlNombre("Unable to connect: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void enviarMensaje(String text) {
        if (connected && backend != null) {
            backend.send(text);
        }
    }

    @Override
    public void desconectar() {
        closing = true;
        if (connected && backend != null) {
            backend.close();
        }
        connected = false;
    }
}
