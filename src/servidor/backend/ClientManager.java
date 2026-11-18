package servidor.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientManager implements Runnable {
    private final Socket socket;
    private final ServidorSocket server;
    private BufferedReader input;
    private PrintWriter output;
    private String nickname;

    public ClientManager(Socket socket, ServidorSocket server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            output = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            nickname = input.readLine();
            if (!server.register(nickname, this)) {
                socket.close();
                return;
            }

            String line;
            while ((line = input.readLine()) != null) {
                server.route(this, line);
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        } finally {
            server.unregister(this);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        }
    }

    synchronized void send(String line) {
        output.println(line);
    }

    String getNickname() {
        return nickname;
    }
}
