package cliente.backend;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ClientConfig {
    public static final String HOST;
    public static final int PORT;

    static {
        String host;
        int port;

        try (InputStream in = new FileInputStream("cliente.properties")) {
            Properties properties = new Properties();
            properties.load(in);

            host = properties.getProperty("server.host");
            port = Integer.parseInt(properties.getProperty("server.port", "6969"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError("No se pudo leer cliente.properties: " + e.getMessage());
        }

        if (host == null || host.isBlank()) {
            throw new ExceptionInInitializerError("Falta server.host en cliente.properties");
        }

        HOST = host;
        PORT = port;
    }

    private ClientConfig() {
    }
}
