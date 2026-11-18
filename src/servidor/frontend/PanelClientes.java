package servidor.frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

/**
 * Lista de clientes conectados y acciones de expulsar o mensaje privado.
 */
public class PanelClientes extends JPanel {
    private final DefaultListModel<String> modelo;
    private final JList<String> lista;
    private final TitledBorder borde;

    public PanelClientes(Consumer<String> alExpulsar,
            BiConsumer<String, String> alMensajePrivado) {
        modelo = new DefaultListModel<>();
        lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(Tema.FONDO_INTERMEDIO_CAMPO);

        setLayout(new BorderLayout(4, 4));
        borde = BorderFactory.createTitledBorder("Conectados (0)");
        setBorder(borde);
        setPreferredSize(new Dimension(230, 0));
        setBackground(Tema.FONDO_CAMPO);
        add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(2, 1, 0, 4));
        JButton btnExpulsar = new JButton("Expulsar");
        JButton btnPrivado = new JButton("Mensaje privado");
        botones.add(btnExpulsar);
        botones.add(btnPrivado);
        

        add(botones, BorderLayout.SOUTH);
        
        btnExpulsar.setBackground(Tema.COLOR_BOTON_EXPULSAR); 
        btnPrivado.setBackground(Tema.COLOR_BOTON_PRIVADO);       

        btnExpulsar.addActionListener(e -> {
            String nick = lista.getSelectedValue();
            if (nick == null) {
                avisarSeleccion();
                return;
            }
            alExpulsar.accept(nick);
        });

        btnPrivado.addActionListener(e -> {
            String nick = lista.getSelectedValue();
            if (nick == null) {
                avisarSeleccion();
                return;
            }
            String texto = JOptionPane.showInputDialog(this,
                    "Mensaje para " + nick + ":", "Mensaje privado",
                    JOptionPane.PLAIN_MESSAGE);
            if (texto != null && !texto.trim().isEmpty()) {
                alMensajePrivado.accept(nick, texto.trim());
            }
        });
    }

    public void setClientes(List<String> clientes) {
        modelo.clear();
        for (String cliente : clientes) {
            modelo.addElement(cliente);
        }
        actualizarTitulo();
    }

    public void agregarCliente(String nickname) {
        if (!modelo.contains(nickname)) {
            modelo.addElement(nickname);
            actualizarTitulo();
        }
    }

    public void quitarCliente(String nickname) {
        modelo.removeElement(nickname);
        actualizarTitulo();
    }

    private void avisarSeleccion() {
        JOptionPane.showMessageDialog(this, "Selecciona un cliente de la lista.",
                "Clientes", JOptionPane.WARNING_MESSAGE);
    }

    private void actualizarTitulo() {
        borde.setTitle("Conectados (" + modelo.size() + ")");
        repaint();
    }
}
