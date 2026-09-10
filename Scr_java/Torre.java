package Scr_java;

/**
 * Estructura defensiva colocada por el jugador.
 * Es el elemento que almacena ListaSecuencialTorres.
 */
public class Torre {

    private int id;
    private String nombre;
    private String tipo;
    private int posicion;
    private int danio;
    private int rango;
    private int costo;

    public Torre(int id, String nombre, String tipo, int posicion, int danio, int rango, int costo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.posicion = posicion;
        this.danio = danio;
        this.rango = rango;
        this.costo = costo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public int getPosicion() { return posicion; }
    public int getDanio() { return danio; }
    public int getRango() { return rango; }
    public int getCosto() { return costo; }

    /** Indica si una posición del camino está dentro del rango de ataque de la torre. */
    public boolean enRango(int posicionEnemigo) {
        return Math.abs(posicionEnemigo - posicion) <= rango;
    }

    @Override
    public String toString() {
        return "Torre #" + id + " [" + nombre + " - " + tipo + "] pos=" + posicion
                + " danio=" + danio + " rango=" + rango + " costo=" + costo;
    }
}

