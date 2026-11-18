package servidor.backend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorSocket  implements Runnable{
    private final int PORT = 6969;
    private ServerSocket serverSocket;
    private boolean running;
    private final Map<String, ClientManager> clients = new ConcurrentHashMap<>();

    public ServidorSocket() {
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;
        new Thread(this).start();
        System.out.println("Servidor escuchando");
    }

    @Override
    public void run() {
        
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                ClientManager manager = new ClientManager(socket, this);
                new Thread(manager).start();
            } catch (IOException e) {
                if (running) {
                    System.out.println("Error de conexion:" + e.getMessage());
                }
            }
        }
    }

    boolean register(String nickname, ClientManager manager) {
        if (nickname == null || nickname.isBlank()
                || nickname.contains("|") || clients.containsKey(nickname)) {
            manager.send("X|Nickname invalid or already in use");
            return false;
        }
        clients.put(nickname, manager);
        sendToAll("S|* " + nickname + " joined", null);
        return true;
    }

    void unregister(ClientManager manager) {
        String nickname = manager.getNickname();
        if (nickname != null && clients.remove(nickname, manager)) {
            sendToAll("S|* " + nickname + " left", null);
        }
    }

    void sendToAll(String line, ClientManager except) {
        for (ClientManager manager : clients.values()) {
            if (manager != except) manager.send(line);
        }
    }

    void route(ClientManager from, String line) {
        if (line.startsWith("/msg ")) {
            String[] parts = line.substring(5).split(" ", 2); 
            if (parts.length < 2) {
                from.send("S|Usage: /msg Nickname message");
                return;
            }
            ClientManager to = clients.get(parts[0]);
            if (to == null) {
                from.send("S|User not connected: " + parts[0]);
            } else {
                to.send("P|" + from.getNickname() + "|" + parts[1]);
                from.send("P|" + parts[0] + "|" + parts[1]);
            }
        } else {
            sendToAll("G|" + from.getNickname() + "|" + line, from);
        }
    }
}
