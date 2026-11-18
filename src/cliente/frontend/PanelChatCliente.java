package cliente.frontend;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Área de chat del cliente y envío de mensajes.
 * Mantiene el estilo original: área verde y botón de envío.
 */
public class PanelChatCliente extends JPanel {
    private final JTextArea areaChat;
    private final JTextField campoMensaje;

    public PanelChatCliente(Consumer<String> alEnviar) {
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO_CHAT);

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setBackground(Tema.FONDO_CHAT);
        areaChat.setForeground(Tema.COLOR_TEXTO);
        areaChat.setFont(Tema.FUENTE_MONO);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        campoMensaje.setBackground(Tema.FONDO_INTERMEDIO_CAMPO);
        campoMensaje.setFont(Tema.FUENTE_MONO);

        JButton btnEnviarMensaje = new JButton("enviar mensaje");
        btnEnviarMensaje.setBackground(Tema.COLOR_ACTIVO);
        btnEnviarMensaje.setFont(new Font("SansSerif", Font.BOLD, 12));

        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviarMensaje, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        Runnable enviar = () -> {
            String texto = campoMensaje.getText().trim();
            if (texto.isEmpty()) {
                return;
            }
            alEnviar.accept(texto);
            campoMensaje.setText("");
            campoMensaje.requestFocusInWindow();
        };

        btnEnviarMensaje.addActionListener(e -> enviar.run());
        campoMensaje.addActionListener(e -> enviar.run());
    }

    public void agregarLinea(String linea) {
        areaChat.append(linea + "\n");
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }

    public void limpiar() {
        areaChat.setText("");
    }

    public void enfocarCampo() {
        campoMensaje.requestFocusInWindow();
    }
}
