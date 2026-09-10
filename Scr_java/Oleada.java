package Scr_java;
/**
 * Plantilla de una oleada de enemigos: cuántos enemigos genera y con qué
 * atributos base. Es el dato que envuelve cada NodoOleada de ListaCircularOleadas.
 */
public class Oleada {

    private int idOleada;
    private int cantidadEnemigos;
    private String tipoEnemigo;
    private int vidaBase;
    private int velocidadBase;

    public Oleada(int idOleada, int cantidadEnemigos, String tipoEnemigo, int vidaBase, int velocidadBase) {
        this.idOleada = idOleada;
        this.cantidadEnemigos = cantidadEnemigos;
        this.tipoEnemigo = tipoEnemigo;
        this.vidaBase = vidaBase;
        this.velocidadBase = velocidadBase;
    }

    public int getIdOleada() { return idOleada; }
    public int getCantidadEnemigos() { return cantidadEnemigos; }
    public String getTipoEnemigo() { return tipoEnemigo; }
    public int getVidaBase() { return vidaBase; }
    public int getVelocidadBase() { return velocidadBase; }

    @Override
    public String toString() {
        return "Oleada #" + idOleada + " [" + tipoEnemigo + "] enemigos=" + cantidadEnemigos
                + " vidaBase=" + vidaBase + " velBase=" + velocidadBase;
    }
}
