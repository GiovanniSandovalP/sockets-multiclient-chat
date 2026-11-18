package cliente.frontend;

/**
 * Punto único de integración entre la GUI del cliente y el backend.
 * La GUI no conoce Socket; solo llama estos métodos.
 */
public interface AccionesCliente {
    void conectar(String nickname);

    void enviarMensaje(String texto);

    void desconectar();
}
