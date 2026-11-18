package servidor.frontend;

import java.util.Arrays;
import java.util.Collections;

/**
 * Implementación temporal para probar la GUI sin backend.
 * No abre sockets ni modifica el servidor.
 */
public class AccionesDemo implements AccionesServidor {
    private final ServidorMain ventana;
    private boolean activo;

    public AccionesDemo(ServidorMain ventana) {
        this.ventana = ventana;
    }

    @Override
    public void iniciar(int puerto) {
        if (activo) {
            return;
        }
        activo = true;
        ventana.setEstado(true, puerto);
        ventana.agregarLog("[DEMO] Servidor simulado en el puerto " + puerto);
        // Clientes falsos solo para ver la lista y probar los botones.
        ventana.setClientes(Arrays.asList("ana (demo)", "luis (demo)"));
    }

    @Override
    public void detener() {
        if (!activo) {
            return;
        }
        activo = false;
        ventana.setEstado(false, 0);
        ventana.setClientes(Collections.emptyList());
        ventana.agregarLog("[DEMO] Servidor simulado detenido");
    }

    @Override
    public void enviarATodos(String texto) {
        if (!activo) {
            ventana.mostrarError("Primero inicia el servidor.");
            return;
        }
        ventana.agregarLog("[DEMO] Mensaje para todos: " + texto);
    }

    @Override
    public void enviarPrivado(String destino, String texto) {
        if (!activo) {
            ventana.mostrarError("Primero inicia el servidor.");
            return;
        }
        ventana.agregarLog("[DEMO] Privado para " + destino + ": " + texto);
    }

    @Override
    public void expulsar(String nickname) {
        if (!activo) {
            ventana.mostrarError("Primero inicia el servidor.");
            return;
        }
        ventana.quitarCliente(nickname);
        ventana.agregarLog("[DEMO] Cliente expulsado: " + nickname);
    }
}
