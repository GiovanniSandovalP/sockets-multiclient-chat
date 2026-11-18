package cliente.frontend;

/**
 * Implementación temporal para probar la GUI sin backend.
 * No abre sockets ni envía datos al servidor.
 */
public class AccionesDemoCliente implements AccionesCliente {
    private final ClientePrueba ventana;
    private String nickname;

    public AccionesDemoCliente(ClientePrueba ventana) {
        this.ventana = ventana;
    }

    @Override
    public void conectar(String nickname) {
        this.nickname = nickname;
        ventana.mostrarChat(nickname);
        ventana.agregarSistema("Entraste como " + nickname + " (modo demo).");
    }

    @Override
    public void enviarMensaje(String texto) {
        if (nickname == null) {
            ventana.mostrarError("Primero ingresa tu nombre.");
            return;
        }
        ventana.agregarMensaje("Tú: " + texto);
    }

    @Override
    public void desconectar() {
        nickname = null;
    }
}
