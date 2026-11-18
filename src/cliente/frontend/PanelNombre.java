package cliente.frontend;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Pantalla inicial donde el usuario escribe su nombre.
 * El servidor espera ese nombre como primera línea de la conexión.
 */
public class PanelNombre extends JPanel {
    private final JTextField campoNombre;
    private final JLabel lblError;
    private final Consumer<String> alEntrar;

    public PanelNombre(Consumer<String> alEntrar) {
        this.alEntrar = alEntrar;

        setLayout(new GridBagLayout());
        setBackground(Tema.FONDO_PANTALLA_NOMBRE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel titulo = new JLabel("Elige tu nombre de usuario");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Tema.COLOR_TEXTO);

        JLabel ayuda = new JLabel("El servidor usará este nombre para identificarte.");
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ayuda.setForeground(Tema.COLOR_TEXTO);

        campoNombre = new JTextField(16);
        campoNombre.setBackground(Tema.FONDO_INTERMEDIO_CAMPO);
        campoNombre.setFont(Tema.FUENTE_MONO);
        campoNombre.setHorizontalAlignment(JTextField.CENTER);

        JButton btnEntrar = new JButton("Entrar");
        btnEntrar.setBackground(Tema.COLOR_BOTON);
        btnEntrar.setFont(new Font("SansSerif", Font.BOLD, 13));

        lblError = new JLabel(" ");
        lblError.setForeground(Tema.COLOR_ERROR);
        lblError.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);

        gbc.gridy = 0;
        add(titulo, gbc);
        gbc.gridy = 1;
        add(ayuda, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 40, 10, 40);
        add(campoNombre, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 40, 10, 40);
        add(btnEntrar, gbc);
        gbc.gridy = 4;
        add(lblError, gbc);

        btnEntrar.addActionListener(e -> intentarEntrar());
        campoNombre.addActionListener(e -> intentarEntrar());
    }

    private void intentarEntrar() {
        String nombre = campoNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarError("Escribe un nombre de usuario.");
            return;
        }
        if (nombre.contains("|")) {
            mostrarError("El nombre no puede contener el carácter |.");
            return;
        }
        if (nombre.length() > 20) {
            mostrarError("El nombre debe tener máximo 20 caracteres.");
            return;
        }

        lblError.setText(" ");
        alEntrar.accept(nombre);
    }

    public void mostrarError(String mensaje) {
        lblError.setText(mensaje == null || mensaje.isBlank() ? " " : mensaje);
        campoNombre.requestFocusInWindow();
    }

    public void limpiar() {
        campoNombre.setText("");
        lblError.setText(" ");
    }

    public void enfocar() {
        campoNombre.requestFocusInWindow();
    }
}
