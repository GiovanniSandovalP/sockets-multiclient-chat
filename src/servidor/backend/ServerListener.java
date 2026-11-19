package servidor.backend;

public interface ServerListener {
    void started(int port);

    void stopped();

    void log(String line);

    void clientJoined(String nickname);

    void clientLeft(String nickname);
}
