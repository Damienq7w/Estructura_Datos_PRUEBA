package Scr_java;

/**
 * Reglas de la partida que comparten la versión de consola (TowerDefenseApp) y
 * la versión con ventana (TowerDefenseGUI).
 *
 * Antes cada pantalla llevaba su propia copia de estas decisiones y ya se habían
 * desincronizado: en la consola TODAS las torres golpeaban en área y la recompensa
 * del enemigo se calculaba distinto. Al vivir en un solo lugar, corregir la regla
 * una vez la corrige en las dos pantallas.
 */
public final class ReglasJuego {

    /** Clase de utilidades: no se instancia. */
    private ReglasJuego() { }

    /** Solo el Mago ataca en área; Arquero y Cañón son de objetivo único. */
    public static boolean esAreaDeEfecto(String tipoTorre) {
        return "Mago".equalsIgnoreCase(tipoTorre);
    }

    /** Oro que deja al morir un enemigo nacido de esta oleada. */
    public static int recompensaPorEnemigo(Oleada oleada) {
        return Math.max(5, oleada.getVidaBase() / 4);
    }

    /** Derrota: al jugador no le quedan vidas. */
    public static boolean esDerrota(Jugador jugador) {
        return jugador.derrotado();
    }

    /** Victoria: se lanzaron todas las oleadas registradas y el camino quedó vacío. */
    public static boolean esVictoria(ListaCircularOleadas oleadas, int oleadasIniciadas, ListaDobleEnemigos enemigos) {
        return oleadas.tamanio() > 0
                && oleadasIniciadas >= oleadas.tamanio()
                && enemigos.estaVacia();
    }
}
