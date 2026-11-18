package servidor.frontend;

import java.awt.FlowLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Barra superior: puerto, iniciar/detener y estado del servidor.
 */
public class PanelConexion extends JPanel {
    private final JTextField campoPuerto;
    private final JButton btnIniciar;
    private final JLabel lblEstado;
    private final Consumer<Integer> alIniciar;
    private final Runnable alDetener;
    private boolean activo;

    public PanelConexion(Consumer<Integer> alIniciar, Runnable alDetener) {
        this.alIniciar = alIniciar;
        this.alDetener = alDetener;

        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        setBorder(BorderFactory.createTitledBorder("Conexión"));

        campoPuerto = new JTextField("6969", 6);
        btnIniciar = new JButton("Iniciar");
        lblEstado = new JLabel();
        setBackground(Tema.FONDO_CAMPO);

        add(new JLabel("Puerto:"));
        add(campoPuerto);
        add(btnIniciar);
        add(lblEstado);

        btnIniciar.addActionListener(e -> alPresionarBoton());
        setEstado(false, 0);
    }

    private void alPresionarBoton() {
        if (activo) {
            alDetener.run();
            return;
        }

        int puerto;
        try {
            puerto = Integer.parseInt(campoPuerto.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Escribe un puerto numérico.",
                    "Puerto inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (puerto < 1 || puerto > 65535) {
            JOptionPane.showMessageDialog(this, "El puerto debe estar entre 1 y 65535.",
                    "Puerto inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        alIniciar.accept(puerto);
    }

    public void setEstado(boolean activo, int puerto) {
        this.activo = activo;
        btnIniciar.setText(activo ? "Detener" : "Iniciar");
        btnIniciar.setBackground(activo ? Tema.COLOR_BOTON_DETENER : Tema.COLOR_BOTON);
        campoPuerto.setEnabled(!activo);

        if (activo) {
            lblEstado.setText("● Escuchando en " + puerto);
            lblEstado.setForeground(Tema.COLOR_ACTIVO);
        } else {
            lblEstado.setText("● Detenido");
            lblEstado.setForeground(Tema.COLOR_DETENIDO);
        }
    }
}
