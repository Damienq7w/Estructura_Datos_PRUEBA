package Scr_java;

/**
 * Enemigo que avanza por el camino hacia la base del jugador.
 * Es el dato que envuelve cada NodoEnemigo de ListaDobleEnemigos.
 */
public class Enemigo {

    private int id;
    private String tipo;
    private int vida;
    private int velocidad;
    private int posicion;
    private int recompensa;

    public Enemigo(int id, String tipo, int vida, int velocidad, int posicion, int recompensa) {
        this.id = id;
        this.tipo = tipo;
        this.vida = vida;
        this.velocidad = velocidad;
        this.posicion = posicion;
        this.recompensa = recompensa;
    }

    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public int getVida() { return vida; }
    public int getVelocidad() { return velocidad; }
    public int getPosicion() { return posicion; }
    public int getRecompensa() { return recompensa; }

    /** Avanza el enemigo según su velocidad (se invoca en cada turno). */
    public void avanzar() {
        posicion += velocidad;
    }

    /** Aplica el daño recibido de una torre, sin dejar la vida en negativo. */
    public void recibirDanio(int danio) {
        vida -= danio;
        if (vida < 0) vida = 0;
    }

    public boolean estaDestruido() {
        return vida <= 0;
    }

    @Override
    public String toString() {
        return "Enemigo #" + id + " [" + tipo + "] vida=" + vida
                + " vel=" + velocidad + " pos=" + posicion + " recompensa=" + recompensa;
    }
}
