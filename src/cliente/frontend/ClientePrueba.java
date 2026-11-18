package cliente.frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Ventana principal del cliente.
 * Primero pide el nombre de usuario y luego habilita el chat.
 * Aún no está conectada al servidor: usa AccionesDemoCliente.
 *
 * Para integrarla más adelante:
 * 1. Crea un adaptador que implemente AccionesCliente.
 * 2. Pásalo con new ClientePrueba(acciones).
 * 3. Desde el lector del socket llama a mostrarChat, volverAlNombre,
 *    agregarMensaje y agregarSistema.
 */
public class ClientePrueba extends JFrame {
    private static final String TARJETA_NOMBRE = "nombre";
    private static final String TARJETA_CHAT = "chat";

    private final JLabel lblUsuario;
    private final PanelNombre panelNombre;
    private final PanelChatCliente panelChat;
    private final JPanel contenedor;
    private final java.awt.CardLayout tarjetas;
    private AccionesCliente acciones;

    public ClientePrueba() {
        this(null);
    }

    public ClientePrueba(AccionesCliente acciones) {
        super("Cliente");
        this.acciones = acciones;

        setSize(560, 560);
        setMinimumSize(new Dimension(420, 420));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Label superior: el nombre aparece alineado a la izquierda.
        lblUsuario = new JLabel("Usuario: —", SwingConstants.LEFT);
        lblUsuario.setOpaque(true);
        lblUsuario.setBackground(Tema.FONDO_CABECERA);
        lblUsuario.setForeground(Tema.COLOR_TEXTO);
        lblUsuario.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblUsuario.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(lblUsuario, BorderLayout.NORTH);

        tarjetas = new java.awt.CardLayout();
        contenedor = new JPanel(tarjetas);

        panelNombre = new PanelNombre(this::confirmarNombre);
        panelChat = new PanelChatCliente(this::enviarMensaje);
        contenedor.add(panelNombre, TARJETA_NOMBRE);
        contenedor.add(panelChat, TARJETA_CHAT);
        add(contenedor, BorderLayout.CENTER);

        if (this.acciones == null) {
            this.acciones = new AccionesDemoCliente(this);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientePrueba.this.acciones.desconectar();
            }
        });

        setVisible(true);
        SwingUtilities.invokeLater(panelNombre::enfocar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientePrueba::new);
    }

    private void confirmarNombre(String nombre) {
        acciones.conectar(nombre);
    }

    private void enviarMensaje(String texto) {
        acciones.enviarMensaje(texto);
    }

    // Métodos que usará el adaptador del socket.
    public void mostrarChat(String nombre) {
        SwingUtilities.invokeLater(() -> {
            lblUsuario.setText("Usuario: " + nombre);
            setTitle("Cliente - " + nombre);
            panelNombre.limpiar();
            tarjetas.show(contenedor, TARJETA_CHAT);
            panelChat.enfocarCampo();
        });
    }

    public void volverAlNombre(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            lblUsuario.setText("Usuario: —");
            setTitle("Cliente");
            panelChat.limpiar();
            tarjetas.show(contenedor, TARJETA_NOMBRE);
            panelNombre.mostrarError(mensaje);
            panelNombre.enfocar();
        });
    }

    public void agregarMensaje(String linea) {
        SwingUtilities.invokeLater(() -> panelChat.agregarLinea(linea));
    }

    public void agregarSistema(String texto) {
        SwingUtilities.invokeLater(() -> panelChat.agregarLinea("* " + texto));
    }

    public void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                this, mensaje, "Cliente", JOptionPane.WARNING_MESSAGE));
    }
}
