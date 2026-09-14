package Scr_java;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;

/**
 * Dibuja el camino, las torres, los enemigos activos y los efectos del
 * turno; también resuelve en qué posición del camino (o sobre qué torre)
 * cae un clic del mouse. No contiene ninguna regla del juego: solo lee el
 * estado que expone TowerDefenseGUI y, ante un clic, se lo delega a ella.
 *
 * Las torres se pueden dibujar encima o debajo de la línea del camino según
 * el lado donde se hizo clic para colocarlas (ver {@link TowerDefenseGUI#esLadoAbajo}).
 */
public class TableroPanel extends JPanel {

    private static final int MARGEN = 60;
    private static final int PATH_Y = 230;

    private final TowerDefenseGUI gui;
    private int mouseX = -1, mouseY = -1;

    public TableroPanel(TowerDefenseGUI gui) {
        this.gui = gui;
        setPreferredSize(new Dimension(920, 460));
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Torre torreHit = torreEnPixel(e.getX(), e.getY());
                int posicion = posicionEnPixel(e.getX());
                boolean abajo = e.getY() > PATH_Y;
                gui.alHacerClicEnTablero(posicion, torreHit, abajo);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                mouseX = -1; mouseY = -1;
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX(); mouseY = e.getY();
                repaint();
            }
        });
    }

    private double unidad() {
        return (getWidth() - 2.0 * MARGEN) / TowerDefenseGUI.LONGITUD_RUTA;
    }

    private int xDePosicion(double posicion) {
        return (int) Math.round(MARGEN + posicion * unidad());
    }

    /** Posición entera del camino más cercana a una coordenada X en pantalla. */
    int posicionEnPixel(int x) {
        double posicion = (x - MARGEN) / unidad();
        int redondeada = (int) Math.round(posicion);
        if (redondeada < 0) redondeada = 0;
        if (redondeada > TowerDefenseGUI.LONGITUD_RUTA) redondeada = TowerDefenseGUI.LONGITUD_RUTA;
        return redondeada;
    }

    /** Tope (borde pegado al camino) del bloque de una torre, según su lado. */
    private int topeBloque(boolean abajo) {
        return abajo ? PATH_Y + 46 : PATH_Y - 72;
    }

    /** Torre cuyo bloque (icono + etiquetas) contiene el punto (x, y), o null si no hay ninguna ahí. */
    Torre torreEnPixel(int x, int y) {
        for (Torre t : gui.getTorres().getTorres()) {
            int tx = xDePosicion(t.getPosicion());
            boolean abajo = gui.esLadoAbajo(t.getId());
            int tope = topeBloque(abajo);
            // Arriba: el bloque va de la primera línea de texto (tope-16) al icono (hasta
            // tope+28). Abajo: del icono (tope) a la segunda línea de texto (tope+62).
            int y1 = abajo ? tope - 6 : tope - 16;
            int y2 = abajo ? tope + 62 : tope + 46;
            if (x >= tx - 18 && x <= tx + 18 && y >= y1 && y <= y2) {
                return t;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        dibujarFondo(g2);
        dibujarCamino(g2);
        dibujarRangosTorres(g2);
        dibujarGhostColocacion(g2);
        dibujarTorres(g2);
        dibujarEnemigos(g2);
        dibujarBase(g2);
        dibujarEfectos(g2);
        dibujarOverlayFinDePartida(g2);
    }

    private void dibujarFondo(Graphics2D g2) {
        GradientPaint cielo = new GradientPaint(0, 0, new Color(200, 230, 250), 0, PATH_Y, new Color(225, 245, 220));
        g2.setPaint(cielo);
        g2.fillRect(0, 0, getWidth(), PATH_Y + 10);
        g2.setPaint(new Color(190, 225, 165));
        g2.fillRect(0, PATH_Y + 10, getWidth(), Math.max(0, getHeight() - PATH_Y - 10));
    }

    private void dibujarCamino(Graphics2D g2) {
        g2.setColor(new Color(90, 60, 30));
        g2.setStroke(new BasicStroke(24));
        g2.drawLine(MARGEN, PATH_Y, xDePosicion(TowerDefenseGUI.LONGITUD_RUTA), PATH_Y);
        g2.setColor(new Color(210, 180, 140));
        g2.setStroke(new BasicStroke(18));
        g2.drawLine(MARGEN, PATH_Y, xDePosicion(TowerDefenseGUI.LONGITUD_RUTA), PATH_Y);

        // Regla de posiciones: una franja clara pegada al camino para que los números
        // resalten sobre el pasto en vez de perderse como texto suelto.
        int inicioRegla = MARGEN - 12;
        int finRegla = xDePosicion(TowerDefenseGUI.LONGITUD_RUTA) + 12;
        int reglaY = PATH_Y + 13;
        g2.setColor(new Color(255, 255, 255, 210));
        g2.fillRoundRect(inicioRegla, reglaY, finRegla - inicioRegla, 20, 10, 10);
        g2.setColor(new Color(150, 170, 140));
        g2.drawRoundRect(inicioRegla, reglaY, finRegla - inicioRegla, 20, 10, 10);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        for (int p = 0; p <= TowerDefenseGUI.LONGITUD_RUTA; p += 2) {
            int x = xDePosicion(p);
            g2.setColor(new Color(120, 95, 60));
            g2.drawLine(x, PATH_Y + 9, x, reglaY + 1);
            String texto = String.valueOf(p);
            int ancho = g2.getFontMetrics().stringWidth(texto);
            g2.setColor(new Color(45, 35, 20));
            g2.drawString(texto, x - ancho / 2, reglaY + 15);
        }
    }

    private void dibujarRangosTorres(Graphics2D g2) {
        for (Torre t : gui.getTorres().getTorres()) {
            int x = xDePosicion(t.getPosicion());
            double radioPx = t.getRango() * unidad();
            g2.setColor(new Color(70, 130, 210, 35));
            g2.fillOval((int) (x - radioPx), (int) (PATH_Y - radioPx), (int) (2 * radioPx), (int) (2 * radioPx));
        }
    }

    private void dibujarGhostColocacion(Graphics2D g2) {
        TowerDefenseGUI.PresetTorre preset = gui.getPresetSeleccionado();
        if (preset == null || mouseX < 0 || gui.isJuegoTerminado()) return;
        int posicion = posicionEnPixel(mouseX);
        int x = xDePosicion(posicion);
        boolean abajo = mouseY > PATH_Y;

        double radioPx = preset.rango * unidad();
        g2.setColor(new Color(70, 130, 210, 60));
        g2.fillOval((int) (x - radioPx), (int) (PATH_Y - radioPx), (int) (2 * radioPx), (int) (2 * radioPx));

        Color c = preset.color;
        dibujarIconoTorre(g2, x, topeBloque(abajo), new Color(c.getRed(), c.getGreen(), c.getBlue(), 140), preset.tipo);
    }

    private void dibujarTorres(Graphics2D g2) {
        for (Torre t : gui.getTorres().getTorres()) {
            int x = xDePosicion(t.getPosicion());
            boolean abajo = gui.esLadoAbajo(t.getId());
            int tope = topeBloque(abajo);
            Color color = colorPorTipo(t.getTipo());
            dibujarIconoTorre(g2, x, tope, color, t.getTipo());

            String texto1 = "#" + t.getId() + " " + t.getNombre();
            String texto2 = "dmg " + t.getDanio() + " / rng " + t.getRango();
            g2.setColor(Color.BLACK);
            if (abajo) {
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g2.drawString(texto1, x - 26, tope + 42);
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                g2.drawString(texto2, x - 28, tope + 56);
            } else {
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g2.drawString(texto1, x - 26, tope - 6);
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                g2.drawString(texto2, x - 28, tope + 40);
            }
        }
    }

    /**
     * Cada tipo de torre tiene una silueta distinta para que se note a simple vista cuál
     * ataca a un único objetivo (Arquero: triángulo, Cañón: cuadrado) y cuál ataca en área
     * (Mago: rombo). Es puramente decorativo — no afecta ninguna regla del juego.
     */
    private void dibujarIconoTorre(Graphics2D g2, int x, int yTope, Color color, String tipo) {
        g2.setColor(color);
        if ("Cañón".equals(tipo)) {
            g2.fillRect(x - 12, yTope, 24, 24);
            g2.setColor(Color.BLACK);
            g2.drawRect(x - 12, yTope, 24, 24);
        } else if ("Mago".equals(tipo)) {
            int[] xs = {x, x + 13, x, x - 13};
            int[] ys = {yTope - 4, yTope + 12, yTope + 28, yTope + 12};
            g2.fillPolygon(xs, ys, 4);
            g2.setColor(Color.BLACK);
            g2.drawPolygon(xs, ys, 4);
        } else {
            int[] xs = {x, x - 13, x + 13};
            int[] ys = {yTope - 2, yTope + 24, yTope + 24};
            g2.fillPolygon(xs, ys, 3);
            g2.setColor(Color.BLACK);
            g2.drawPolygon(xs, ys, 3);
        }
    }

    private Color colorPorTipo(String tipo) {
        if (tipo == null) return new Color(90, 90, 90);
        switch (tipo) {
            case "Arquero": return new Color(60, 140, 70);
            case "Cañón": return new Color(70, 90, 160);
            case "Mago": return new Color(150, 60, 150);
            default: return new Color(120, 100, 80);
        }
    }

    private void dibujarEnemigos(Graphics2D g2) {
        // Varios enemigos de la misma oleada comparten posición y velocidad exactas, así que
        // sin esto se dibujarían perfectamente superpuestos. El desfase es solo visual (se
        // calcula a partir del id) y no toca la posición real que usa la lógica del juego.
        for (TowerDefenseGUI.EstadoAnimEnemigoView vista : gui.getAnimacionesEnemigos()) {
            NodoEnemigo nodo = gui.getEnemigos().buscarEnemigoPorId(vista.id);
            if (nodo == null) continue;
            Enemigo e = nodo.getEnemigo();
            int desfaseX = (vista.id * 23) % 33 - 16;
            int desfaseY = (vista.id * 13) % 21 - 10;
            int x = xDePosicion(vista.posicionVisual) + desfaseX;
            int y = PATH_Y + desfaseY;

            g2.setColor(new Color(210, 50, 50));
            g2.fillOval(x - 6, y - 6, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawOval(x - 6, y - 6, 12, 12);

            double fraccion = vista.vidaMax > 0 ? Math.max(0, e.getVida()) / (double) vista.vidaMax : 1;
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(x - 9, y - 15, 18, 3);
            g2.setColor(fraccion > 0.5 ? new Color(60, 170, 60) : fraccion > 0.25 ? new Color(220, 170, 30) : new Color(200, 40, 40));
            g2.fillRect(x - 9, y - 15, (int) (18 * fraccion), 3);

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
            g2.setColor(Color.BLACK);
            g2.drawString("#" + e.getId(), x - 6, y + 19);
        }
    }

    private void dibujarBase(Graphics2D g2) {
        int x = xDePosicion(TowerDefenseGUI.LONGITUD_RUTA);
        g2.setColor(new Color(120, 70, 40));
        g2.fillRect(x + 8, PATH_Y - 30, 22, 30);
        g2.setColor(new Color(180, 30, 30));
        int[] xs = {x + 8, x + 19, x + 30};
        int[] ys = {PATH_Y - 30, PATH_Y - 46, PATH_Y - 30};
        g2.fillPolygon(xs, ys, 3);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g2.drawString("Base", x, PATH_Y - 50);
    }

    private void dibujarEfectos(Graphics2D g2) {
        for (TowerDefenseGUI.EfectoView ef : gui.getEfectos()) {
            int alpha = (int) Math.round(220 * ef.vidaRestante);
            if (alpha <= 0) continue;
            switch (ef.tipo) {
                case 0: { // línea de ataque torre -> enemigo
                    int x1 = xDePosicion(ef.x1);
                    int x2 = xDePosicion(ef.x2);
                    g2.setColor(new Color(255, 220, 60, alpha));
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawLine(x1, PATH_Y - 60, x2, PATH_Y);
                    break;
                }
                case 1: { // explosión al destruir un enemigo
                    int x = xDePosicion(ef.x1);
                    int radio = (int) (6 + 14 * (1 - ef.vidaRestante));
                    g2.setColor(new Color(255, 140, 0, alpha));
                    g2.fillOval(x - radio, PATH_Y - radio, radio * 2, radio * 2);
                    break;
                }
                case 2: { // golpe a la base
                    int x = xDePosicion(ef.x1);
                    g2.setColor(new Color(220, 30, 30, alpha / 2));
                    g2.fillOval(x - 30, PATH_Y - 55, 60, 60);
                    break;
                }
                default:
                    break;
            }
        }
    }

    private void dibujarOverlayFinDePartida(Graphics2D g2) {
        if (!gui.isJuegoTerminado()) return;
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(gui.isVictoria() ? new Color(60, 200, 90) : new Color(220, 60, 60));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
        String texto = gui.isVictoria() ? "¡VICTORIA!" : "DERROTA";
        int ancho = g2.getFontMetrics().stringWidth(texto);
        g2.drawString(texto, (getWidth() - ancho) / 2, getHeight() / 2 - 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        String sub = "Usa \"Reiniciar partida\" para jugar de nuevo";
        int anchoSub = g2.getFontMetrics().stringWidth(sub);
        g2.drawString(sub, (getWidth() - anchoSub) / 2, getHeight() / 2 + 20);
    }
}
