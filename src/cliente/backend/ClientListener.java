package cliente.backend;

public interface ClientListener {
    void message(String type, String from, String text); // G, P, S, X
    void disconnected();
}
