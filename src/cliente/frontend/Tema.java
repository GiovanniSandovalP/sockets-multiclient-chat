package cliente.frontend;

import java.awt.Color;
import java.awt.Font;

/**
 * Colores y fuentes de la GUI del cliente.
 * Cambia estos valores para personalizar toda la interfaz.
 */
public final class Tema {
    public static final Color FONDO_CHAT = new Color(245, 194, 107);
    public static final Color FONDO_CAMPO = new Color(245, 194, 107);
    public static final Color FONDO_INTERMEDIO_CAMPO = new Color(226, 226, 197);
    public static final Color FONDO_CABECERA = new Color(226, 226, 197);
    public static final Color FONDO_PANTALLA_NOMBRE = new Color(250, 235, 200);
    public static final Color COLOR_BOTON = new Color(103, 162, 15);
    public static final Color COLOR_BOTON_EXPULSAR = new Color(229, 126, 113);
    public static final Color COLOR_BOTON_PRIVADO = new Color(103, 162, 15);
    public static final Color COLOR_BOTON_DETENER = new Color(103, 162, 15);
    public static final Color COLOR_ACTIVO = new Color(0, 128, 0);
    public static final Color COLOR_DETENIDO = Color.RED;
    public static final Color COLOR_TEXTO = Color.BLACK;
    public static final Color COLOR_ERROR = new Color(180, 0, 0);
    public static final Font FUENTE_MONO = new Font("Monospaced", Font.PLAIN, 13);

    private Tema() {
    }
}
