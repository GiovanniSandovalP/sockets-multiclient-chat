package servidor;

import java.io.IOException;
import javax.swing.SwingUtilities;

import servidor.backend.ServidorSocket;
import servidor.frontend.ServerBackendActions;
import servidor.frontend.ServidorMain;

public class Main {
    public static void main(String args[]) throws IOException {
        if (args.length > 0 && args[0].equalsIgnoreCase("cli")) {
            runCli();
        } else {
            runGui();
        }
    }

    private static void runCli() throws IOException {
        ServidorSocket servidorSocket = new ServidorSocket();
        servidorSocket.start();
    }

    private static void runGui() {
        SwingUtilities.invokeLater(() -> {
            ServerBackendActions actions = new ServerBackendActions();
            ServidorMain window = new ServidorMain(actions);
            actions.setWindow(window);
        });
    }
}
