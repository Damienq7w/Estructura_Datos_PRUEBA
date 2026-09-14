package Scr_java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class TowerDefenseGUI extends JFrame {

    public static final int LONGITUD_RUTA = 20;
    private static final int CAPACIDAD_TORRES = 50;
    private static final int ORO_INICIAL = 180;
    private static final int NIVELES_INICIALES = 20;

    // ---------- Estado de la partida (mismas 3 estructuras que la consola) ----------
    private ListaSecuencialTorres torres = new ListaSecuencialTorres(CAPACIDAD_TORRES);
    private ListaDobleEnemigos enemigos = new ListaDobleEnemigos();
    private ListaCircularOleadas oleadas = new ListaCircularOleadas();
    private Jugador jugador = new Jugador(3);

    private int siguienteIdTorre = 1;
    private int siguienteIdEnemigo = 1;
    private int siguienteIdOleada = 1;
    private int oleadasIniciadas = 0;
    private boolean juegoTerminado = false;
    private boolean victoria = false;
    private int oro = ORO_INICIAL;

    // ---------- Catálogo de torres (para colocar con un clic) ----------
    static final class PresetTorre {
        final String nombre, tipo;
        final int danio, rango, costo;
        final Color color;
        PresetTorre(String nombre, String tipo, int danio, int rango, int costo, Color color) {
            this.nombre = nombre; this.tipo = tipo; this.danio = danio;
            this.rango = rango; this.costo = costo; this.color = color;
        }
    }

    // El Mago ataca en área (a todos los enemigos en rango) y por eso es el más caro;
    // Arquero y Cañón son de objetivo único, como corresponde a una torre "física".
    final PresetTorre[] presetsTorre = {
        new PresetTorre("Arquero", "Arquero", 18, 3, 40, new Color(60, 140, 70)),
        new PresetTorre("Cañón", "Cañón", 45, 2, 90, new Color(70, 90, 160)),
        new PresetTorre("Mago", "Mago", 9, 3, 130, new Color(150, 60, 150)),
    };

    private PresetTorre presetSeleccionado = null;
    private final JToggleButton[] botonesPreset = new JToggleButton[presetsTorre.length];

    // ---------- Lado del camino donde quedó cada torre (arreglo + contador, sin java.util) ----------
    // Torre no guarda esto (no le corresponde a la clase de datos); es puramente una
    // preferencia visual de esta pantalla, así que se lleva aparte, igual que la animación.
    private static final class LadoTorre {
        int idTorre;
        boolean abajo;
    }
    private final LadoTorre[] ladosTorres = new LadoTorre[CAPACIDAD_TORRES];
    private int ladosCantidad = 0;

    // ---------- Estructuras propias (arreglo + contador, sin java.util) para animar ----------
    private static final class EstadoAnimEnemigo {
        int id;
        double posicionVisual;
        int vidaMax;
    }
    private final EstadoAnimEnemigo[] animEnemigos = new EstadoAnimEnemigo[200];
    private int animCantidad = 0;

    private static final class Efecto {
        int tipo; // 0 = ataque (línea torre->enemigo), 1 = explosión, 2 = golpe a la base
        double x1, x2;
        long expiraEn;
        long duracionMs;
    }
    private final Efecto[] efectos = new Efecto[100];
    private int efectosCantidad = 0;

    // ---------- Componentes ----------
    private final TableroPanel tablero = new TableroPanel(this);
    private final JTextArea log = new JTextArea();
    private final JLabel lblNivelInfo = new JLabel();
    private final JLabel lblVidas = new JLabel();
    private final JLabel lblOro = new JLabel();
    private final JLabel lblNivel = new JLabel();
    private final JLabel lblEnemigos = new JLabel();
    private final JLabel lblEstado = new JLabel();
    private final JButton btnIniciarOleada = new JButton("Iniciar siguiente nivel");
    private final JButton btnAvanzar = new JButton("Avanzar turno");
    private final JToggleButton btnAuto = new JToggleButton("Auto-avanzar");
    private final JButton btnReiniciar = new JButton("Reiniciar partida");

    private final Timer timerTurno = new Timer(900, e -> avanzarTurno());
    private final Timer timerAnimacion = new Timer(25, e -> tick());

    public TowerDefenseGUI() {
        super("Tower Defense — Estructura de Datos (Tablero visual)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        generarCampanaInicial();

        JScrollPane scrollArsenal = new JScrollPane(construirArsenal());
        scrollArsenal.setBorder(BorderFactory.createEmptyBorder());
        scrollArsenal.getVerticalScrollBar().setUnitIncrement(16);
        // Ancho fijo, alto "preferido" (así pack() ya deja ver todo sin scroll); si de
        // todas formas la ventana termina más baja que el contenido, el JScrollPane
        // agrega una barra en vez de recortarlo, a diferencia de un JPanel suelto.
        scrollArsenal.setPreferredSize(new Dimension(270, 718));

        add(tablero, BorderLayout.CENTER);
        add(scrollArsenal, BorderLayout.WEST);
        add(construirPanelInferior(), BorderLayout.SOUTH);

        escribirLog("Bienvenido. Elige una torre a la izquierda y haz clic sobre el camino para colocarla.");
        escribirLog(NIVELES_INICIALES + " niveles cargados; cada uno es más difícil que el anterior.");
        actualizarEstado();
        pack();
        setLocationRelativeTo(null);
        timerAnimacion.start();
    }

    // ---------- Construcción de la interfaz ----------

    private JPanel construirArsenal() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setMaximumSize(new Dimension(255, Integer.MAX_VALUE));

        panel.add(construirPanelTorres());
        panel.add(Box.createVerticalStrut(10));
        panel.add(construirPanelNiveles());
        panel.add(Box.createVerticalStrut(10));
        panel.add(construirPanelTurno());
        panel.add(Box.createVerticalStrut(10));

        btnReiniciar.setAlignmentX(0f);
        btnReiniciar.setPreferredSize(new Dimension(235, 28));
        btnReiniciar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btnReiniciar.addActionListener(e -> reiniciarPartida());
        panel.add(btnReiniciar);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel construirPanelTorres() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Torres — clic en el camino"));

        for (int i = 0; i < presetsTorre.length; i++) {
            final int idx = i;
            PresetTorre p = presetsTorre[i];
            String modo = esAreaDeEfecto(p.tipo)
                    ? "Daño en área: golpea a la vez a todos los enemigos en su rango"
                    : "Un solo objetivo: ataca al enemigo más cercano a la base";
            JToggleButton b = new JToggleButton("<html><b>" + p.nombre + "</b><br>"
                    + "Costo " + p.costo + " &middot; Daño " + p.danio + " &middot; Rango " + p.rango
                    + "<br><i>" + modo + "</i></html>");
            b.setToolTipText(esAreaDeEfecto(p.tipo)
                    ? "Daña a TODOS los enemigos dentro de su rango cada turno."
                    : "De todos los enemigos en su rango, siempre le dispara al que va más adelantado en el camino (el más cerca de la base).");
            b.setAlignmentX(0f);
            b.setPreferredSize(new Dimension(225, 84));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
            b.setBackground(mezclarConBlanco(p.color));
            b.addActionListener(e -> seleccionarPreset(idx));
            botonesPreset[i] = b;
            panel.add(b);
            panel.add(Box.createVerticalStrut(4));
        }
        JLabel ayudaTorre = new JLabel("<html><i>Clic en una torre del camino la vende (mitad del costo).</i></html>");
        ayudaTorre.setFont(ayudaTorre.getFont().deriveFont(10f));
        ayudaTorre.setPreferredSize(new Dimension(225, 30));
        ayudaTorre.setAlignmentX(0f);
        panel.add(ayudaTorre);
        return panel;
    }

    private JPanel construirPanelNiveles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Oleadas por niveles"));

        JLabel info = new JLabel("<html>" + NIVELES_INICIALES + " niveles listos de entrada;<br>"
                + "cada uno trae más enemigos, con<br>más vida y más velocidad.</html>");
        info.setFont(info.getFont().deriveFont(10f));
        info.setPreferredSize(new Dimension(225, 45));
        info.setAlignmentX(0f);
        panel.add(info);
        panel.add(Box.createVerticalStrut(6));

        JButton btnNivelExtra = new JButton("+ Agregar nivel extra");
        btnNivelExtra.setToolTipText("Encola un nivel más, todavía más difícil que el último.");
        btnNivelExtra.setAlignmentX(0f);
        btnNivelExtra.setPreferredSize(new Dimension(225, 26));
        btnNivelExtra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        btnNivelExtra.addActionListener(e -> agregarNivelExtra());
        panel.add(btnNivelExtra);
        panel.add(Box.createVerticalStrut(4));

        btnIniciarOleada.setAlignmentX(0f);
        btnIniciarOleada.setPreferredSize(new Dimension(225, 26));
        btnIniciarOleada.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        btnIniciarOleada.addActionListener(e -> iniciarSiguienteOleada());
        panel.add(btnIniciarOleada);
        return panel;
    }

    private JPanel construirPanelTurno() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Turno"));

        btnAvanzar.setAlignmentX(0f);
        btnAvanzar.setPreferredSize(new Dimension(225, 26));
        btnAvanzar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        btnAvanzar.addActionListener(e -> avanzarTurno());
        panel.add(btnAvanzar);
        panel.add(Box.createVerticalStrut(4));

        btnAuto.setAlignmentX(0f);
        btnAuto.setPreferredSize(new Dimension(225, 26));
        btnAuto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        btnAuto.addActionListener(e -> {
            if (btnAuto.isSelected()) timerTurno.start(); else timerTurno.stop();
        });
        panel.add(btnAuto);
        return panel;
    }

    private JPanel construirPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));

        JPanel contenedorEstado = new JPanel();
        contenedorEstado.setLayout(new BoxLayout(contenedorEstado, BoxLayout.Y_AXIS));

        lblNivelInfo.setAlignmentX(0f);
        lblNivelInfo.setFont(lblNivelInfo.getFont().deriveFont(Font.BOLD, 13f));
        lblNivelInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        contenedorEstado.add(lblNivelInfo);

        JPanel filaEstado = new JPanel(new GridLayout(1, 5, 10, 0));
        Font f = lblVidas.getFont().deriveFont(Font.BOLD, 13f);
        for (JLabel l : new JLabel[]{lblVidas, lblOro, lblNivel, lblEnemigos, lblEstado}) {
            l.setFont(f);
        }
        filaEstado.add(lblVidas);
        filaEstado.add(lblOro);
        filaEstado.add(lblNivel);
        filaEstado.add(lblEnemigos);
        filaEstado.add(lblEstado);
        filaEstado.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        contenedorEstado.add(filaEstado);

        panel.add(contenedorEstado, BorderLayout.NORTH);

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        log.setRows(7);
        JScrollPane scroll = new JScrollPane(log);
        scroll.setPreferredSize(new Dimension(900, 130));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private Color mezclarConBlanco(Color c) {
        return new Color((c.getRed() + 255 * 2) / 3, (c.getGreen() + 255 * 2) / 3, (c.getBlue() + 255 * 2) / 3);
    }

    // ---------- Selección de torre a colocar ----------

    private void seleccionarPreset(int idx) {
        if (botonesPreset[idx].isSelected()) {
            presetSeleccionado = presetsTorre[idx];
            for (int i = 0; i < botonesPreset.length; i++) {
                if (i != idx) botonesPreset[i].setSelected(false);
            }
            tablero.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            presetSeleccionado = null;
            tablero.setCursor(Cursor.getDefaultCursor());
        }
        tablero.repaint();
    }

    // ---------- Interacción proveniente del tablero (clic del mouse) ----------

    void alHacerClicEnTablero(int posicion, Torre torreClickeada, boolean abajo) {
        if (juegoTerminado) return;

        if (torreClickeada != null) {
            int reembolso = torreClickeada.getCosto() / 2;
            if (torres.eliminarTorrePorId(torreClickeada.getId())) {
                removerLadoTorre(torreClickeada.getId());
                oro += reembolso;
                escribirLog("Torre #" + torreClickeada.getId() + " (" + torreClickeada.getNombre()
                        + ") vendida por " + reembolso + " de oro.");
            }
        } else if (presetSeleccionado != null) {
            if (posicion < 0 || posicion > LONGITUD_RUTA) {
                // clic fuera del camino: nada que hacer
            } else if (hayTorreEnPosicion(posicion, abajo)) {
                escribirLog("Ya hay una torre en ese lado de la posición " + posicion + ".");
            } else if (oro < presetSeleccionado.costo) {
                escribirLog("Oro insuficiente para " + presetSeleccionado.nombre + " (cuesta " + presetSeleccionado.costo + ", tienes " + oro + ").");
            } else {
                Torre t = new Torre(siguienteIdTorre, presetSeleccionado.nombre, presetSeleccionado.tipo,
                        posicion, presetSeleccionado.danio, presetSeleccionado.rango, presetSeleccionado.costo);
                if (torres.insertarTorre(t)) {
                    siguienteIdTorre++;
                    oro -= t.getCosto();
                    registrarLadoTorre(t.getId(), abajo);
                    escribirLog(t.getNombre() + " #" + t.getId() + " colocada en la posición " + posicion
                            + (abajo ? " (debajo del camino)." : " (encima del camino)."));
                } else {
                    escribirLog("No se pudo colocar la torre (capacidad llena).");
                }
            }
        } else {
            escribirLog("Selecciona primero un tipo de torre a la izquierda.");
        }
        refrescar();
    }

    /** Ya se puede colocar una torre encima Y otra debajo del camino en la misma posición. */
    private boolean hayTorreEnPosicion(int posicion, boolean abajo) {
        for (Torre t : torres.getTorres()) {
            if (t.getPosicion() == posicion && esLadoAbajo(t.getId()) == abajo) return true;
        }
        return false;
    }

    private void registrarLadoTorre(int idTorre, boolean abajo) {
        if (ladosCantidad >= ladosTorres.length) return;
        LadoTorre l = new LadoTorre();
        l.idTorre = idTorre;
        l.abajo = abajo;
        ladosTorres[ladosCantidad++] = l;
    }

    private void removerLadoTorre(int idTorre) {
        for (int i = 0; i < ladosCantidad; i++) {
            if (ladosTorres[i].idTorre == idTorre) {
                ladosTorres[i] = ladosTorres[ladosCantidad - 1];
                ladosCantidad--;
                return;
            }
        }
    }

    /** Lado del camino donde se dibuja la torre con ese id (false = encima, true = debajo). */
    boolean esLadoAbajo(int idTorre) {
        for (int i = 0; i < ladosCantidad; i++) {
            if (ladosTorres[i].idTorre == idTorre) return ladosTorres[i].abajo;
        }
        return false;
    }

    // ---------- Campaña de niveles ----------

    /** Solo el Mago ataca en área; el resto de torres son de objetivo único. */
    private static boolean esAreaDeEfecto(String tipo) {
        return "Mago".equalsIgnoreCase(tipo);
    }

    /**
     * Genera el siguiente nivel de la campaña (usa el próximo id de oleada como número de
     * nivel, así que la dificultad sube en línea recta mientras se van registrando).
     */
    private void generarNivel() {
        int nivel = siguienteIdOleada;
        int cantidad = Math.min(4 + nivel, 20);
        int vidaBase = 30 + nivel * 8;
        int velocidadBase = 1 + Math.min(nivel / 5, 3);
        String tipoEnemigo;
        if (nivel % 5 == 0) tipoEnemigo = "Jefe";
        else if (nivel % 3 == 0) tipoEnemigo = "Élite";
        else if (nivel % 2 == 0) tipoEnemigo = "Veloz";
        else tipoEnemigo = "Básico";

        Oleada o = new Oleada(siguienteIdOleada, cantidad, tipoEnemigo, vidaBase, velocidadBase);
        oleadas.registrarOleada(o);
        siguienteIdOleada++;
    }

    private void generarCampanaInicial() {
        for (int i = 0; i < NIVELES_INICIALES; i++) {
            generarNivel();
        }
    }

    private void agregarNivelExtra() {
        if (juegoTerminado) {
            escribirLog("La partida ya finalizó.");
            return;
        }
        int nivel = siguienteIdOleada;
        generarNivel();
        escribirLog("Nivel " + nivel + " agregado a la cola (" + oleadas.tamanio() + " niveles en total).");
        refrescar();
    }

    private void iniciarSiguienteOleada() {
        if (juegoTerminado) {
            escribirLog("La partida ya finalizó.");
            return;
        }
        if (oleadas.estaVacia()) {
            escribirLog("No hay niveles en cola.");
            return;
        }
        Oleada o = oleadas.avanzarSiguienteOleada();
        oleadasIniciadas++;
        for (int i = 0; i < o.getCantidadEnemigos(); i++) {
            int recompensa = Math.max(5, o.getVidaBase() / 4);
            Enemigo e = new Enemigo(siguienteIdEnemigo, o.getTipoEnemigo(), o.getVidaBase(), o.getVelocidadBase(), 0, recompensa);
            enemigos.insertarEnemigoAlFinal(e);
            siguienteIdEnemigo++;
        }
        escribirLog("Nivel " + o.getIdOleada() + " (" + o.getTipoEnemigo() + ") iniciado -- "
                + o.getCantidadEnemigos() + " enemigos añadidos al camino.");
        refrescar();
    }

    // ---------- Turno (misma lógica de 5 pasos que TowerDefenseApp) ----------

    private void avanzarTurno() {
        if (juegoTerminado) {
            escribirLog("La partida ya finalizó.");
            timerTurno.stop();
            btnAuto.setSelected(false);
            return;
        }
        if (enemigos.estaVacia()) {
            escribirLog("No hay enemigos activos para avanzar el turno.");
            return;
        }

        int ataques = 0, destruidos = 0, vidasPerdidas = 0;
        boolean huboEventos = false;

        // 1. Mover todos los enemigos según su velocidad.
        enemigos.actualizarPosicionEnCadaTurno();

        // 2 y 3. Cada torre ataca según su tipo: el Mago daña a todos los enemigos en su
        // rango (área); el resto solo al enemigo más avanzado dentro de su rango (objetivo
        // único), como se esperaría de un Arquero o un Cañón reales.
        for (Torre t : torres.getTorres()) {
            if (esAreaDeEfecto(t.getTipo())) {
                NodoEnemigo actual = enemigos.getPrimero();
                while (actual != null) {
                    Enemigo e = actual.getEnemigo();
                    if (!e.estaDestruido() && t.enRango(e.getPosicion())) {
                        aplicarDanioDeTorre(t, e);
                        ataques++;
                        huboEventos = true;
                    }
                    actual = actual.getSiguiente();
                }
            } else {
                Enemigo objetivo = enemigoMasAvanzadoEnRango(t);
                if (objetivo != null) {
                    aplicarDanioDeTorre(t, objetivo);
                    ataques++;
                    huboEventos = true;
                }
            }
        }

        // 4. Eliminar de la lista los enemigos cuya vida llegó a 0.
        NodoEnemigo nodo = enemigos.getPrimero();
        while (nodo != null) {
            NodoEnemigo siguiente = nodo.getSiguiente();
            if (nodo.getEnemigo().estaDestruido()) {
                Enemigo e = nodo.getEnemigo();
                agregarEfecto(1, e.getPosicion(), e.getPosicion(), 420);
                oro += e.getRecompensa();
                escribirLog("Enemigo #" + e.getId() + " destruido (+" + e.getRecompensa() + " de oro).");
                enemigos.eliminarEnemigoDestruido(e.getId());
                destruidos++;
                huboEventos = true;
            }
            nodo = siguiente;
        }

        // 5. Descontar vidas al jugador si un enemigo alcanzó el final del camino.
        nodo = enemigos.getPrimero();
        while (nodo != null) {
            NodoEnemigo siguiente = nodo.getSiguiente();
            if (nodo.getEnemigo().getPosicion() >= LONGITUD_RUTA) {
                jugador.perderVida();
                vidasPerdidas++;
                huboEventos = true;
                agregarEfecto(2, LONGITUD_RUTA, LONGITUD_RUTA, 500);
                escribirLog("Enemigo #" + nodo.getEnemigo().getId() + " llegó a la base. Vidas restantes: " + jugador.getVidas());
                enemigos.eliminarEnemigoDestruido(nodo.getEnemigo().getId());
            }
            nodo = siguiente;
        }

        if (!huboEventos) {
            escribirLog("Turno: los enemigos avanzaron, sin ataques ni bajas.");
        }
        escribirLog("Resumen — ataques: " + ataques + " | destruidos: " + destruidos + " | vidas perdidas: " + vidasPerdidas);

        verificarFinDePartida();
        if (juegoTerminado) {
            timerTurno.stop();
            btnAuto.setSelected(false);
            escribirLog(victoria
                    ? "*** ¡VICTORIA! Todos los niveles fueron completados. ***"
                    : "*** DERROTA. El jugador perdió todas sus vidas. ***");
        }
        refrescar();
    }

    /** Enemigo más avanzado (mayor posición) dentro del rango de la torre, o null si ninguno. */
    private Enemigo enemigoMasAvanzadoEnRango(Torre t) {
        Enemigo objetivo = null;
        NodoEnemigo actual = enemigos.getPrimero();
        while (actual != null) {
            Enemigo e = actual.getEnemigo();
            if (!e.estaDestruido() && t.enRango(e.getPosicion())) {
                if (objetivo == null || e.getPosicion() > objetivo.getPosicion()) {
                    objetivo = e;
                }
            }
            actual = actual.getSiguiente();
        }
        return objetivo;
    }

    private void aplicarDanioDeTorre(Torre t, Enemigo e) {
        e.recibirDanio(t.getDanio());
        agregarEfecto(0, t.getPosicion(), e.getPosicion(), 280);
        escribirLog("Torre #" + t.getId() + " (" + t.getNombre() + ") ataca a enemigo #" + e.getId()
                + " -> vida restante: " + e.getVida());
    }

    private void verificarFinDePartida() {
        if (jugador.derrotado()) {
            juegoTerminado = true;
            victoria = false;
        } else if (oleadas.tamanio() > 0 && oleadasIniciadas >= oleadas.tamanio() && enemigos.estaVacia()) {
            juegoTerminado = true;
            victoria = true;
        }
    }

    private void reiniciarPartida() {
        timerTurno.stop();
        btnAuto.setSelected(false);
        torres = new ListaSecuencialTorres(CAPACIDAD_TORRES);
        enemigos = new ListaDobleEnemigos();
        oleadas = new ListaCircularOleadas();
        jugador = new Jugador(3);
        siguienteIdTorre = 1;
        siguienteIdEnemigo = 1;
        siguienteIdOleada = 1;
        oleadasIniciadas = 0;
        juegoTerminado = false;
        victoria = false;
        oro = ORO_INICIAL;
        animCantidad = 0;
        efectosCantidad = 0;
        ladosCantidad = 0;
        presetSeleccionado = null;
        for (JToggleButton b : botonesPreset) b.setSelected(false);
        generarCampanaInicial();
        log.setText("");
        escribirLog("Partida reiniciada. Elige una torre y haz clic sobre el camino para colocarla.");
        escribirLog(NIVELES_INICIALES + " niveles cargados; cada uno es más difícil que el anterior.");
        refrescar();
    }

    // ---------- Animación ----------

    private void tick() {
        NodoEnemigo n = enemigos.getPrimero();
        while (n != null) {
            Enemigo e = n.getEnemigo();
            EstadoAnimEnemigo estado = obtenerOCrearAnim(e.getId(), e.getVida());
            double objetivo = Math.min(e.getPosicion(), LONGITUD_RUTA);
            estado.posicionVisual += (objetivo - estado.posicionVisual) * 0.25;
            if (Math.abs(objetivo - estado.posicionVisual) < 0.03) estado.posicionVisual = objetivo;
            n = n.getSiguiente();
        }
        limpiarAnimHuerfanos();
        limpiarEfectosExpirados();
        tablero.repaint();
    }

    private EstadoAnimEnemigo obtenerOCrearAnim(int id, int vidaActual) {
        for (int i = 0; i < animCantidad; i++) {
            if (animEnemigos[i].id == id) return animEnemigos[i];
        }
        EstadoAnimEnemigo nuevo = new EstadoAnimEnemigo();
        nuevo.id = id;
        nuevo.posicionVisual = 0;
        nuevo.vidaMax = vidaActual;
        if (animCantidad < animEnemigos.length) {
            animEnemigos[animCantidad++] = nuevo;
        }
        return nuevo;
    }

    private void limpiarAnimHuerfanos() {
        int i = 0;
        while (i < animCantidad) {
            if (enemigos.buscarEnemigoPorId(animEnemigos[i].id) == null) {
                animEnemigos[i] = animEnemigos[animCantidad - 1];
                animCantidad--;
            } else {
                i++;
            }
        }
    }

    private void agregarEfecto(int tipo, double x1, double x2, long duracionMs) {
        if (efectosCantidad >= efectos.length) return;
        Efecto ef = new Efecto();
        ef.tipo = tipo;
        ef.x1 = x1;
        ef.x2 = x2;
        ef.duracionMs = duracionMs;
        ef.expiraEn = System.currentTimeMillis() + duracionMs;
        efectos[efectosCantidad++] = ef;
    }

    private void limpiarEfectosExpirados() {
        long ahora = System.currentTimeMillis();
        int i = 0;
        while (i < efectosCantidad) {
            if (efectos[i].expiraEn <= ahora) {
                efectos[i] = efectos[efectosCantidad - 1];
                efectosCantidad--;
            } else {
                i++;
            }
        }
    }

    // ---------- Registro y estado ----------

    private void escribirLog(String linea) {
        log.append(linea + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    private void refrescar() {
        tablero.repaint();
        actualizarEstado();
    }

    private void actualizarEstado() {
        lblVidas.setText("Vidas: " + jugador.getVidas());
        lblOro.setText("Oro: " + oro);
        lblNivel.setText("Nivel: " + oleadasIniciadas + "/" + oleadas.tamanio());
        lblEnemigos.setText("Enemigos: " + enemigos.tamanio() + "   Torres: " + torres.contarTorresActivas());
        lblEstado.setText(juegoTerminado ? (victoria ? "VICTORIA" : "DERROTA") : "En curso");
        lblEstado.setForeground(juegoTerminado ? (victoria ? new Color(30, 130, 30) : new Color(170, 30, 30)) : Color.DARK_GRAY);
        lblNivelInfo.setText(describirNivelInfo());
    }

    private String describirNivelInfo() {
        if (juegoTerminado) {
            return victoria ? "¡Campaña completada!" : "Partida terminada.";
        }
        Oleada actual = oleadas.getOleadaActual();
        String enCurso = actual == null ? "ninguno todavía" : "Nivel " + actual.getIdOleada() + " (" + actual.getTipoEnemigo() + ")";
        return "Nivel en curso: " + enCurso + "   |   Próximo: " + describirProximoNivel();
    }

    private String describirProximoNivel() {
        if (oleadas.estaVacia() || oleadasIniciadas >= oleadas.tamanio()) return "-";
        Oleada[] arreglo = oleadas.getOleadasComoArreglo();
        Oleada siguiente = arreglo[oleadasIniciadas];
        return "Nivel " + siguiente.getIdOleada() + " (" + siguiente.getTipoEnemigo() + ", "
                + siguiente.getCantidadEnemigos() + " enemigos)";
    }

    // ---------- Accesores usados por TableroPanel (mismo paquete) ----------

    ListaSecuencialTorres getTorres() { return torres; }
    ListaDobleEnemigos getEnemigos() { return enemigos; }
    ListaCircularOleadas getOleadas() { return oleadas; }
    boolean isJuegoTerminado() { return juegoTerminado; }
    boolean isVictoria() { return victoria; }
    PresetTorre getPresetSeleccionado() { return presetSeleccionado; }
    EstadoAnimEnemigoView[] getAnimacionesEnemigos() {
        EstadoAnimEnemigoView[] vista = new EstadoAnimEnemigoView[animCantidad];
        for (int i = 0; i < animCantidad; i++) {
            vista[i] = new EstadoAnimEnemigoView(animEnemigos[i].id, animEnemigos[i].posicionVisual, animEnemigos[i].vidaMax);
        }
        return vista;
    }
    EfectoView[] getEfectos() {
        long ahora = System.currentTimeMillis();
        EfectoView[] vista = new EfectoView[efectosCantidad];
        for (int i = 0; i < efectosCantidad; i++) {
            Efecto ef = efectos[i];
            double restante = (ef.expiraEn - ahora) / (double) ef.duracionMs;
            vista[i] = new EfectoView(ef.tipo, ef.x1, ef.x2, Math.max(0, Math.min(1, restante)));
        }
        return vista;
    }

    /** Vista de solo lectura de la animación de un enemigo, para que TableroPanel no toque el estado interno. */
    static final class EstadoAnimEnemigoView {
        final int id; final double posicionVisual; final int vidaMax;
        EstadoAnimEnemigoView(int id, double posicionVisual, int vidaMax) {
            this.id = id; this.posicionVisual = posicionVisual; this.vidaMax = vidaMax;
        }
    }

    /** Vista de solo lectura de un efecto visual en curso. */
    static final class EfectoView {
        final int tipo; final double x1, x2; final double vidaRestante;
        EfectoView(int tipo, double x1, double x2, double vidaRestante) {
            this.tipo = tipo; this.x1 = x1; this.x2 = x2; this.vidaRestante = vidaRestante;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TowerDefenseGUI().setVisible(true));
    }
}
