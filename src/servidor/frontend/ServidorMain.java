package servidor.frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 * Ventana principal del servidor (Swing/AWT).
 * Todavía no está conectada al backend: usa AccionesDemo.
 *
 * Para integrarla más adelante:
 * 1. Crea un adaptador que implemente AccionesServidor.
 * 2. Pásalo con new ServidorMain(acciones).
 * 3. Desde los eventos del servidor llama a setEstado, agregarLog,
 *    setClientes, agregarCliente y quitarCliente.
 */
public class ServidorMain extends JFrame {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PanelConexion panelConexion;
    private final PanelChat panelChat;
    private final PanelClientes panelClientes;
    private AccionesServidor acciones;

    public ServidorMain() {
        this(null);
    }

    public ServidorMain(AccionesServidor acciones) {
        super("Servidor - Chat Multicliente");
        this.acciones = acciones;

        setSize(860, 600);
        setMinimumSize(new Dimension(700, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panelConexion = new PanelConexion(this::iniciarServidor, this::detenerServidor);
        panelChat = new PanelChat(this::enviarATodos);
        panelClientes = new PanelClientes(this::expulsarCliente, this::enviarPrivado);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                panelChat, panelClientes);
        division.setResizeWeight(1.0);
        division.setContinuousLayout(true);
        division.setDividerLocation(590);

        JPanel raiz = new JPanel(new BorderLayout(8, 8));
        raiz.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        raiz.add(panelConexion, BorderLayout.NORTH);
        raiz.add(division, BorderLayout.CENTER);
        setContentPane(raiz);

        if (this.acciones == null) {
            this.acciones = new AccionesDemo(this);
        }

        agregarLog("GUI lista.");
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServidorMain::new);
    }

    // Métodos que usará el adaptador del backend.
    public void setEstado(boolean activo, int puerto) {
        SwingUtilities.invokeLater(() -> panelConexion.setEstado(activo, puerto));
    }

    public void agregarLog(String linea) {
        SwingUtilities.invokeLater(() -> panelChat.agregarLinea(
                "[" + LocalTime.now().format(HORA) + "] " + linea));
    }

    public void setClientes(List<String> clientes) {
        SwingUtilities.invokeLater(() -> panelClientes.setClientes(clientes));
    }

    public void agregarCliente(String nickname) {
        SwingUtilities.invokeLater(() -> panelClientes.agregarCliente(nickname));
    }

    public void quitarCliente(String nickname) {
        SwingUtilities.invokeLater(() -> panelClientes.quitarCliente(nickname));
    }

    public void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                this, mensaje, "Servidor", JOptionPane.WARNING_MESSAGE));
    }

    private void iniciarServidor(int puerto) {
        acciones.iniciar(puerto);
    }

    private void detenerServidor() {
        acciones.detener();
    }

    private void enviarATodos(String texto) {
        acciones.enviarATodos(texto);
    }

    private void enviarPrivado(String destino, String texto) {
        acciones.enviarPrivado(destino, texto);
    }

    private void expulsarCliente(String nickname) {
        acciones.expulsar(nickname);
    }
}
