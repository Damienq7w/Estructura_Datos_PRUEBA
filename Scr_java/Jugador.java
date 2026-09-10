package Scr_java;
/**
 * Estado del jugador dentro de la partida: únicamente las vidas, que es
 * lo que exigen las reglas del juego (regla 6 y 7 del documento guía).
 */
public class Jugador {

    private int vidas;

    public Jugador(int vidasIniciales) {
        this.vidas = vidasIniciales;
    }

    public int getVidas() { return vidas; }

    public void perderVida() {
        if (vidas > 0) vidas--;
    }

    public boolean derrotado() {
        return vidas <= 0;
    }
}
