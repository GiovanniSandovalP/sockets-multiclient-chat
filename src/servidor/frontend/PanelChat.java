package servidor.frontend;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Registro de eventos y envío de mensajes para todos.
 * Mantiene el estilo del cliente: área verde y botón verde.
 */
public class PanelChat extends JPanel {
    private final JTextArea areaLog;
    private final JTextField campoMensaje;

    public PanelChat(Consumer<String> alEnviar) {
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createTitledBorder("Registro del servidor"));

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(Tema.FONDO_CHAT);
        areaLog.setFont(Tema.FUENTE_MONO);
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);
        add(new JScrollPane(areaLog), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout(4, 0));
        campoMensaje = new JTextField();
        campoMensaje.setBackground(Tema.FONDO_CAMPO);
        campoMensaje.setFont(Tema.FUENTE_MONO);

        JButton btnEnviar = new JButton("enviar mensaje");
        btnEnviar.setBackground(Tema.COLOR_BOTON);
        btnEnviar.setFont(new Font("SansSerif", Font.BOLD, 12));

        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);
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

        btnEnviar.addActionListener(e -> enviar.run());
        campoMensaje.addActionListener(e -> enviar.run());
    }

    public void agregarLinea(String linea) {
        areaLog.append(linea + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    public void limpiar() {
        areaLog.setText("");
    }
}
