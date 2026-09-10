package Scr_java;

import java.util.Scanner;

/**
 * Menú principal y lógica de simulación del Tower Defense.
 * Integra las tres estructuras obligatorias: ListaSecuencialTorres,
 * ListaDobleEnemigos y ListaCircularOleadas.
 */
public class TowerDefenseApp {

    private static final int LONGITUD_RUTA = 20;
    private static final int CAPACIDAD_TORRES = 50;

    private final Scanner sc = new Scanner(System.in);
    private final ListaSecuencialTorres torres = new ListaSecuencialTorres(CAPACIDAD_TORRES);
    private final ListaDobleEnemigos enemigos = new ListaDobleEnemigos();
    private final ListaCircularOleadas oleadas = new ListaCircularOleadas();
    private final Jugador jugador = new Jugador(3);

    private int siguienteIdTorre = 1;
    private int siguienteIdEnemigo = 1;
    private int siguienteIdOleada = 1;
    private int oleadasIniciadas = 0;
    private boolean juegoTerminado = false;
    private boolean victoria = false;

    public static void main(String[] args) {
        new TowerDefenseApp().ejecutar();
    }

    private void ejecutar() {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1: registrarTorre(); break;
                case 2: torres.mostrarTodasLasTorres(); break;
                case 3: eliminarTorre(); break;
                case 4: registrarOleada(); break;
                case 5: oleadas.mostrarOleadas(); break;
                case 6: iniciarSiguienteOleada(); break;
                case 7: avanzarTurno(); break;
                case 8: mostrarEnemigosActivos(); break;
                case 9: mostrarEstadoGeneral(); break;
                case 10: System.out.println("Saliendo..."); break;
                default: System.out.println("Opción inválida.");
            }
        } while (opcion != 10);
        sc.close();
    }

    private void mostrarMenu() {
        System.out.println("\n===== TOWER DEFENSE =====");
        System.out.println("1. Registrar torre defensiva");
        System.out.println("2. Mostrar torres registradas");
        System.out.println("3. Eliminar torre");
        System.out.println("4. Registrar oleada");
        System.out.println("5. Mostrar oleadas");
        System.out.println("6. Iniciar siguiente oleada");
        System.out.println("7. Avanzar turno");
        System.out.println("8. Mostrar enemigos activos");
        System.out.println("9. Mostrar estado general del juego");
        System.out.println("10. Salir");
    }

    // ---------- Opciones del menú ----------

    private void registrarTorre() {
        String nombre = leerTexto("Nombre de la torre: ");
        String tipo = leerTexto("Tipo (ej. Arquero, Cañón): ");
        int posicion = leerEnteroEnRango("Posición en el camino (0-" + LONGITUD_RUTA + "): ", 0, LONGITUD_RUTA);
        int danio = leerEnteroPositivo("Daño: ");
        int rango = leerEnteroNoNegativo("Rango: ");
        int costo = leerEnteroPositivo("Costo: ");

        Torre t = new Torre(siguienteIdTorre, nombre, tipo, posicion, danio, rango, costo);
        if (torres.insertarTorre(t)) {
            System.out.println("Torre registrada con id " + siguienteIdTorre + ".");
            siguienteIdTorre++;
        } else {
            System.out.println("No se pudo registrar la torre (capacidad llena).");
        }
    }

    private void eliminarTorre() {
        int id = leerEntero("Id de la torre a eliminar: ");
        if (torres.eliminarTorrePorId(id)) {
            System.out.println("Torre eliminada.");
        } else {
            System.out.println("No existe una torre con ese id.");
        }
    }

    private void registrarOleada() {
        String tipoEnemigo = leerTexto("Tipo de enemigo de la oleada: ");
        int cantidad = leerEnteroPositivo("Cantidad de enemigos: ");
        int vidaBase = leerEnteroPositivo("Vida base: ");
        int velocidadBase = leerEnteroPositivo("Velocidad base: ");

        Oleada o = new Oleada(siguienteIdOleada, cantidad, tipoEnemigo, vidaBase, velocidadBase);
        oleadas.registrarOleada(o);
        System.out.println("Oleada registrada con id " + siguienteIdOleada + ".");
        siguienteIdOleada++;
    }

    private void iniciarSiguienteOleada() {
        if (juegoTerminado) {
            System.out.println("La partida ya finalizó.");
            return;
        }
        if (oleadas.estaVacia()) {
            System.out.println("No hay oleadas registradas.");
            return;
        }

        // La lista es circular: al pasar la última oleada, avanzarSiguienteOleada()
        // vuelve sola a la primera sin necesitar lógica adicional.
        Oleada o = oleadas.avanzarSiguienteOleada();
        oleadasIniciadas++;
        for (int i = 0; i < o.getCantidadEnemigos(); i++) {
            Enemigo e = new Enemigo(siguienteIdEnemigo++, o.getTipoEnemigo(),
                    o.getVidaBase(), o.getVelocidadBase(), 0, o.getVidaBase());
            enemigos.insertarEnemigoAlFinal(e);
        }
        System.out.println("Oleada iniciada: " + o);
        System.out.println(o.getCantidadEnemigos() + " enemigos añadidos al camino.");
    }

    private void avanzarTurno() {
        if (juegoTerminado) {
            System.out.println("La partida ya finalizó.");
            return;
        }
        if (enemigos.estaVacia()) {
            System.out.println("No hay enemigos activos para avanzar el turno.");
            return;
        }

        StringBuilder resumen = new StringBuilder();

        // 1. Mover todos los enemigos según su velocidad.
        enemigos.actualizarPosicionEnCadaTurno();

        // 2 y 3. Verificar rango de cada torre y aplicar el daño correspondiente.
        int ataques = 0;
        for (Torre t : torres.getTorres()) {
            NodoEnemigo actual = enemigos.getPrimero();
            while (actual != null) {
                Enemigo e = actual.getEnemigo();
                if (!e.estaDestruido() && t.enRango(e.getPosicion())) {
                    e.recibirDanio(t.getDanio());
                    ataques++;
                    resumen.append("  Torre #").append(t.getId()).append(" (").append(t.getNombre())
                            .append(") ataca a enemigo #").append(e.getId())
                            .append(" -> vida restante: ").append(e.getVida()).append("\n");
                }
                actual = actual.getSiguiente();
            }
        }

        // 4. Eliminar de la lista los enemigos cuya vida llegó a 0.
        int destruidos = 0;
        NodoEnemigo nodo = enemigos.getPrimero();
        while (nodo != null) {
            NodoEnemigo siguiente = nodo.getSiguiente();
            if (nodo.getEnemigo().estaDestruido()) {
                resumen.append("  Enemigo #").append(nodo.getEnemigo().getId()).append(" destruido.\n");
                enemigos.eliminarEnemigoDestruido(nodo.getEnemigo().getId());
                destruidos++;
            }
            nodo = siguiente;
        }

        // 5. Descontar vidas al jugador si un enemigo alcanzó el final del camino.
        int vidasPerdidas = 0;
        nodo = enemigos.getPrimero();
        while (nodo != null) {
            NodoEnemigo siguiente = nodo.getSiguiente();
            if (nodo.getEnemigo().getPosicion() >= LONGITUD_RUTA) {
                jugador.perderVida();
                vidasPerdidas++;
                resumen.append("  Enemigo #").append(nodo.getEnemigo().getId())
                        .append(" llegó a la base. Vidas restantes: ").append(jugador.getVidas()).append("\n");
                enemigos.eliminarEnemigoDestruido(nodo.getEnemigo().getId());
            }
            nodo = siguiente;
        }

        // 6. Mostrar un resumen del turno ejecutado.
        System.out.println("\n--- RESUMEN DEL TURNO ---");
        if (resumen.length() == 0) {
            System.out.println("  Los enemigos avanzaron, sin ataques ni bajas este turno.");
        } else {
            System.out.print(resumen);
        }
        System.out.println("Ataques: " + ataques + " | Enemigos destruidos: " + destruidos
                + " | Vidas perdidas: " + vidasPerdidas);

        verificarFinDePartida();
    }

    private void mostrarEnemigosActivos() {
        if (enemigos.estaVacia()) {
            System.out.println("No hay enemigos activos.");
            return;
        }
        int direccion = leerEntero("Mostrar (1) hacia adelante o (2) hacia atrás: ");
        if (direccion == 2) {
            enemigos.recorrerHaciaAtras();
        } else {
            enemigos.recorrerHaciaAdelante();
        }
    }

    private void mostrarEstadoGeneral() {
        System.out.println("\n--- ESTADO GENERAL DEL JUEGO ---");
        System.out.println("Vidas del jugador: " + jugador.getVidas());
        System.out.println("Torres activas: " + torres.contarTorresActivas());
        System.out.println("Enemigos activos: " + enemigos.tamanio());
        System.out.println("Oleadas registradas: " + oleadas.tamanio() + " | Oleadas iniciadas: " + oleadasIniciadas);
        if (juegoTerminado) {
            System.out.println("Resultado: " + (victoria ? "VICTORIA" : "DERROTA"));
        } else {
            System.out.println("Estado: en curso");
        }
    }

    // ---------- Lógica de fin de partida ----------

    private void verificarFinDePartida() {
        if (jugador.derrotado()) {
            juegoTerminado = true;
            victoria = false;
            System.out.println("\n*** El jugador perdió todas sus vidas. Fin del juego. ***");
        } else if (oleadas.tamanio() > 0 && oleadasIniciadas >= oleadas.tamanio() && enemigos.estaVacia()) {
            juegoTerminado = true;
            victoria = true;
            System.out.println("\n*** ¡Todas las oleadas fueron completadas! Victoria. ***");
        }
    }

    // ---------- Utilidades de entrada ----------

    private int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = sc.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException ex) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    private int leerEnteroEnRango(String mensaje, int min, int max) {
        while (true) {
            int valor = leerEntero(mensaje);
            if (valor < min || valor > max) {
                System.out.println("Debe estar entre " + min + " y " + max + ".");
                continue;
            }
            return valor;
        }
    }

    private int leerEnteroPositivo(String mensaje) {
        while (true) {
            int valor = leerEntero(mensaje);
            if (valor <= 0) {
                System.out.println("Debe ser un número mayor que 0.");
                continue;
            }
            return valor;
        }
    }

    private int leerEnteroNoNegativo(String mensaje) {
        while (true) {
            int valor = leerEntero(mensaje);
            if (valor < 0) {
                System.out.println("Debe ser un número mayor o igual que 0.");
                continue;
            }
            return valor;
        }
    }
}

