/**
 * Nodo de la lista doblemente enlazada de enemigos activos.
 * Guarda referencias a anterior y siguiente para permitir recorrido bidireccional.
 */
public class NodoEnemigo {

    private Enemigo enemigo;
    private NodoEnemigo anterior;
    private NodoEnemigo siguiente;

    public NodoEnemigo(Enemigo enemigo) {
        this.enemigo = enemigo;
    }

    public Enemigo getEnemigo() { return enemigo; }
    public NodoEnemigo getAnterior() { return anterior; }
    public void setAnterior(NodoEnemigo anterior) { this.anterior = anterior; }
    public NodoEnemigo getSiguiente() { return siguiente; }
    public void setSiguiente(NodoEnemigo siguiente) { this.siguiente = siguiente; }
}
