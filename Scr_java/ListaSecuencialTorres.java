package Scr_java;

/**
 * Lista secuencial (manual, basada en arreglo) para las torres defensivas.
 * Sigue el mismo patrón datos[] + cantidad visto en clase para ListaSecuencial:
 * las posiciones válidas son siempre 0 .. cantidad-1.
 */
public class ListaSecuencialTorres {

    private Torre[] datos;
    private int cantidad;

    public ListaSecuencialTorres(int capacidad) {
        if (capacidad <= 0) throw new IllegalArgumentException("Capacidad inválida");
        datos = new Torre[capacidad];
        cantidad = 0;
    }

    public boolean estaLlena() { return cantidad == datos.length; }
    public boolean estaVacia() { return cantidad == 0; }

    /** Inserta la torre al final del arreglo si hay espacio y el id no existe. */
    public boolean insertarTorre(Torre t) {
        if (estaLlena() || buscarIndice(t.getId()) != -1) return false;
        datos[cantidad] = t;
        cantidad++;
        return true;
    }

    /** Elimina la torre con el id indicado y desplaza los elementos siguientes. */
    public boolean eliminarTorrePorId(int id) {
        int pos = buscarIndice(id);
        if (pos == -1) return false;
        for (int i = pos; i < cantidad - 1; i++) {
            datos[i] = datos[i + 1];
        }
        datos[cantidad - 1] = null;
        cantidad--;
        return true;
    }

    public Torre buscarTorrePorId(int id) {
        int pos = buscarIndice(id);
        return pos == -1 ? null : datos[pos];
    }

    private int buscarIndice(int id) {
        for (int i = 0; i < cantidad; i++) {
            if (datos[i].getId() == id) return i;
        }
        return -1;
    }

    public void mostrarTodasLasTorres() {
        if (estaVacia()) {
            System.out.println("No hay torres registradas.");
            return;
        }
        for (int i = 0; i < cantidad; i++) {
            System.out.println(datos[i]);
        }
    }

    /** Cuenta las torres activas (todas las que están en la lista lo están). */
    public int contarTorresActivas() {
        return cantidad;
    }

    /** Copia defensiva usada por la lógica de turno para recorrer sin exponer el arreglo interno. */
    public Torre[] getTorres() {
        Torre[] copia = new Torre[cantidad];
        System.arraycopy(datos, 0, copia, 0, cantidad);
        return copia;
    }
}
