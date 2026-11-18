import javax.swing.*;

import java.awt.*;

public class ClientePrueba extends JFrame{
    private JTextArea areaChateo;
    /*private JTextField espacioMensajes;
    private JButton btnEviarMensaje;*/

    public ClientePrueba(){
        setTitle("Cliente");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        areaChateo = new JTextArea();
        areaChateo.setEditable(false);
        areaChateo.setBackground(Color.GREEN);
        add(new JScrollPane(areaChateo), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        JTextField campoMensaje = new JTextField();
        JButton btnEnviarMensaje = new JButton("enviar mensaje");
        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviarMensaje, BorderLayout.EAST);
        
        campoMensaje.setBackground(Color.lightGray);
        btnEnviarMensaje.setBackground(Color.GREEN);
        
        add(panelInferior, BorderLayout.SOUTH);

        setVisible(true);
    }
    public static void main( String[] args){
            SwingUtilities.invokeLater(ClientePrueba::new);
    }
}