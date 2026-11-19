package servidor.backend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorSocket implements Runnable {
    private static final int DEFAULT_PORT = 6969;
    private final ServerListener listener;
    private final Map<String, ClientManager> clients = new ConcurrentHashMap<>();
    private volatile ServerSocket serverSocket;
    private volatile boolean running;

    public ServidorSocket() {
        this(null);
    }

    public ServidorSocket(ServerListener listener) {
        this.listener = listener;
    }

    public void start() throws IOException {
        start(DEFAULT_PORT);
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        new Thread(this).start();
        log("Servidor escuchando");
        if (listener != null) {
            listener.started(port);
        }
    }

    public void stop() {
        running = false;
        closeServerSocket();
        for (ClientManager manager : clients.values()) {
            manager.closeSocket();
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    log("Client connected: " + socket.getRemoteSocketAddress());
                    ClientManager manager = new ClientManager(socket, this);
                    new Thread(manager).start();
                } catch (IOException e) {
                    if (running) {
                        log("Error de conexion:" + e.getMessage());
                    }
                }
            }
        } finally {
            closeServerSocket();
            log("Servidor detenido");
            if (listener != null) {
                listener.stopped();
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
        if (listener != null) {
            listener.clientJoined(nickname);
        }
        sendToAll("S|* " + nickname + " joined", null);
        return true;
    }

    void unregister(ClientManager manager) {
        String nickname = manager.getNickname();
        if (nickname != null && clients.remove(nickname, manager)) {
            if (listener != null) {
                listener.clientLeft(nickname);
            }
            sendToAll("S|* " + nickname + " left", null);
        }
    }

    void sendToAll(String line, ClientManager except) {
        for (ClientManager manager : clients.values()) {
            if (manager != except) {
                manager.send(line);
            }
        }
    }

    public void broadcastAdmin(String text) {
        sendToAll("G|ADMIN|" + text, null);
    }

    public void sendAdminPrivate(String nickname, String text) {
        ClientManager to = clients.get(nickname);
        if (to != null) {
            to.send("P|ADMIN|" + text);
        }
    }

    public void kick(String nickname) {
        ClientManager manager = clients.get(nickname);
        if (manager != null) {
            manager.closeSocket();
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

    boolean isRunning() {
        return running;
    }

    void log(String line) {
        System.out.println(line);
        if (listener != null) {
            listener.log(line);
        }
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
