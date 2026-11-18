package cliente;

import cliente.backend.ClientBackend;
import cliente.backend.ClientListener;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner keyboard = new Scanner(System.in);

        System.out.print("Nickname: ");
        String nickname = keyboard.nextLine();

        ClientBackend backend = new ClientBackend(new ClientListener() {
            @Override
            public void message(String type, String from, String text) {
                if (from.isEmpty()) {
                    System.out.println("<< " + type + "| " + text);
                } else {
                    System.out.println("<< " + type + "|" + from + "| " + text);
                }
            }

            @Override
            public void disconnected() {
                System.out.println("Servidor desconectado");
            }
        });

        backend.connect(nickname);
        startFrontend(backend); 

        while (keyboard.hasNextLine()) {
            String line = keyboard.nextLine();
            if (line.equals("exit")) {
                backend.close();
                break;
            }
            backend.send(line); 
        }
    }

    private static void startFrontend(ClientBackend backend) {
    }
}
