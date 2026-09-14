package Scr_java;
/**
 * Nodo de la lista circular de oleadas. Solo necesita "siguiente": la
 * circularidad la garantiza ListaCircularOleadas enlazando el último con el primero.
 */
public class NodoOleada {

    private Oleada oleada;
    private NodoOleada siguiente;

    public NodoOleada(Oleada oleada) {
        this.oleada = oleada;
    }

    public Oleada getOleada() { return oleada; }
    public NodoOleada getSiguiente() { return siguiente; }
    public void setSiguiente(NodoOleada siguiente) { this.siguiente = siguiente; }
}
