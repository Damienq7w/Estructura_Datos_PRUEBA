package Scr_java;
/**
 * Lista doblemente enlazada, con referencia a primero y último, para los
 * enemigos activos. Permite recorrido en ambos sentidos y eliminación en O(1)
 * una vez localizado el nodo, sin desplazar elementos como en un arreglo.
 */
public class ListaDobleEnemigos {

    private NodoEnemigo primero;
    private NodoEnemigo ultimo;
    private int tamanio;

    public boolean estaVacia() { return primero == null; }
    public int tamanio() { return tamanio; }
    public NodoEnemigo getPrimero() { return primero; }

    /** Inserta un enemigo nuevo al final de la lista (aparece en la posición inicial del camino). */
    public void insertarEnemigoAlFinal(Enemigo e) {
        NodoEnemigo nuevo = new NodoEnemigo(e);
        if (estaVacia()) {
            primero = ultimo = nuevo;
        } else {
            nuevo.setAnterior(ultimo);
            ultimo.setSiguiente(nuevo);
            ultimo = nuevo;
        }
        tamanio++;
    }

    public NodoEnemigo buscarEnemigoPorId(int id) {
        NodoEnemigo actual = primero;
        while (actual != null) {
            if (actual.getEnemigo().getId() == id) return actual;
            actual = actual.getSiguiente();
        }
        return null;
    }

    /**
     * Desengancha un nodo que el llamador ya tiene localizado. Esta es la operación
     * O(1) que justifica usar una lista doblemente enlazada: como el nodo conoce a
     * su anterior y a su siguiente, no hay que recorrer la lista ni desplazar
     * elementos como habría que hacer en un arreglo.
     */
    public boolean eliminarNodo(NodoEnemigo nodo) {
        if (nodo == null) return false;
        NodoEnemigo ant = nodo.getAnterior();
        NodoEnemigo sig = nodo.getSiguiente();
        if (ant != null) ant.setSiguiente(sig); else primero = sig;
        if (sig != null) sig.setAnterior(ant); else ultimo = ant;
        tamanio--;
        return true;
    }

    /**
     * Elimina al enemigo destruido (o que llegó al final del camino) con ese id.
     * Su costo O(n) viene de la búsqueda, no del borrado: quien ya tenga el nodo
     * en la mano debe llamar a eliminarNodo() y la eliminación queda en O(1).
     */
    public boolean eliminarEnemigoDestruido(int id) {
        return eliminarNodo(buscarEnemigoPorId(id));
    }

    public void recorrerHaciaAdelante() {
        if (estaVacia()) { System.out.println("No hay enemigos activos."); return; }
        NodoEnemigo actual = primero;
        while (actual != null) {
            System.out.println(actual.getEnemigo());
            actual = actual.getSiguiente();
        }
    }

    public void recorrerHaciaAtras() {
        if (estaVacia()) { System.out.println("No hay enemigos activos."); return; }
        NodoEnemigo actual = ultimo;
        while (actual != null) {
            System.out.println(actual.getEnemigo());
            actual = actual.getAnterior();
        }
    }

    /** Mueve a todos los enemigos según su velocidad (paso 1 de "avanzar turno"). */
    public void actualizarPosicionEnCadaTurno() {
        NodoEnemigo actual = primero;
        while (actual != null) {
            actual.getEnemigo().avanzar();
            actual = actual.getSiguiente();
        }
    }
}
