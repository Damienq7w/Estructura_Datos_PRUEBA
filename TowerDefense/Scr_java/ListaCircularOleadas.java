package Scr_java;
/**
 * Lista simplemente enlazada circular, con referencia a último, para las
 * oleadas de enemigos. El siguiente del último nodo apunta siempre al
 * primero, lo que permite recorrer y avanzar el ciclo de oleadas sin límite.
 */
public class ListaCircularOleadas {

    private NodoOleada ultimo;
    private NodoOleada actual;
    private int tamanio;

    public boolean estaVacia() { return ultimo == null; }
    public int tamanio() { return tamanio; }

    /** Registra una nueva oleada al final del ciclo, manteniendo la circularidad. */
    public void registrarOleada(Oleada o) {
        NodoOleada nuevo = new NodoOleada(o);
        if (estaVacia()) {
            nuevo.setSiguiente(nuevo);
            ultimo = nuevo;
        } else {
            nuevo.setSiguiente(ultimo.getSiguiente());
            ultimo.setSiguiente(nuevo);
            ultimo = nuevo;
        }
        tamanio++;
    }

    /** Recorre el ciclo completo una sola vez a partir del primero (ultimo.siguiente). */
    public void mostrarOleadas() {
        if (estaVacia()) { System.out.println("No hay oleadas registradas."); return; }
        NodoOleada inicio = ultimo.getSiguiente();
        NodoOleada nodo = inicio;
        do {
            String marca = (nodo == actual) ? "  <- actual" : "";
            System.out.println(nodo.getOleada() + marca);
            nodo = nodo.getSiguiente();
        } while (nodo != inicio);
    }

    /** Avanza el puntero de oleada actual al siguiente nodo del ciclo (la primera vez, al primero). */
    public Oleada avanzarSiguienteOleada() {
        if (estaVacia()) return null;
        actual = (actual == null) ? ultimo.getSiguiente() : actual.getSiguiente();
        return actual.getOleada();
    }

    /** Reinicia el ciclo para que la siguiente oleada vuelva a ser la primera registrada. */
    public void reiniciarCiclo() {
        actual = null;
    }

    /** Oleada a la que apunta el ciclo ahora mismo, o null si todavía no se inició ninguna. */
    public Oleada getOleadaActual() {
        return (actual == null) ? null : actual.getOleada();
    }

    /**
     * Copia del ciclo como arreglo, en el orden en que se registraron las oleadas
     * (índice 0 = primera registrada). Recorre el ciclo una sola vuelta, así que
     * sirve para consultarlo sin mover el puntero actual ni exponer los nodos.
     */
    public Oleada[] getOleadasComoArreglo() {
        Oleada[] arreglo = new Oleada[tamanio];
        if (estaVacia()) return arreglo;
        NodoOleada inicio = ultimo.getSiguiente();
        NodoOleada nodo = inicio;
        int i = 0;
        do {
            arreglo[i++] = nodo.getOleada();
            nodo = nodo.getSiguiente();
        } while (nodo != inicio);
        return arreglo;
    }
}
