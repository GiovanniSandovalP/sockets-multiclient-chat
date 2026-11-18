package servidor.backend.models;

public class Cliente {
    private final String ip;
    private String nombre;

    public Cliente(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
