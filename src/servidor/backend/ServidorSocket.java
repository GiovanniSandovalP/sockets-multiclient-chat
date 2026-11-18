package servidor.backend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorSocket  implements Runnable{
    private final int PORT = 6969;
    private ServerSocket serverSocket;
    private boolean running;

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
                // Manager
            } catch (IOException e) {
                if (running) {
                    System.out.println("Error de conexion:" + e.getMessage());
                }
            }
        }
    }
}
