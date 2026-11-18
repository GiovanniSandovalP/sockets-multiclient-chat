package cliente.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientBackend {
    private final ClientListener listener;
    private Socket socket;
    private PrintWriter output;

    public ClientBackend(ClientListener listener) {
        this.listener = listener;
    }

    public void connect(String nickname) throws IOException {
        socket = new Socket(ClientConfig.HOST, ClientConfig.PORT);
        output = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

        new Thread(this::receive).start(); 
        output.println(nickname);          
    }

    private void receive() {
        try (BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = input.readLine()) != null) {
                String[] parts = line.split("\\|", 3);

                if (parts.length < 2) {
                    listener.message("S", "", line);
                    continue;
                }

                if ((parts[0].equals("G") || parts[0].equals("P")) && parts.length == 3) {
                    listener.message(parts[0], parts[1], parts[2]);
                } else {
                    listener.message(parts[0], "", parts[1]);
                }
            }
        } catch (IOException ignored) {
        }
        listener.disconnected();
    }

    public synchronized void send(String text) {
        output.println(text); 
    }

    public synchronized void sendPrivate(String nickname, String text) {
        output.println("/msg " + nickname + " " + text);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
