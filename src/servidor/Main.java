package servidor;

import java.io.IOException;
import servidor.backend.ServidorSocket;

public class Main {
    public static void main(String args[]) throws IOException {
        ServidorSocket servidorSocket = new ServidorSocket();
        servidorSocket.start();
    }
}
