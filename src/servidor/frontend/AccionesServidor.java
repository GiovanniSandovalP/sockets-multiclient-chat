package servidor.frontend;

/**
 * Punto único de integración entre la GUI y el backend.
 * La GUI no conoce ServidorSocket; solo llama estos métodos.
 */
public interface AccionesServidor {
    void iniciar(int puerto);

    void detener();

    void enviarATodos(String texto);

    void enviarPrivado(String destino, String texto);

    void expulsar(String nickname);
}
