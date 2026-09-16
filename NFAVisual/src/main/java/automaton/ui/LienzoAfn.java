package automaton.ui;

import automaton.AFN;
import automaton.Estado;
import automaton.Transicion;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class LienzoAfn extends Pane {

    private static final double RADIO = 22;
    private static final double ESPACIO_X = 140;
    private static final double ESPACIO_Y = 95;
    private static final double MARGEN_X = 70;
    private static final double MARGEN_Y = 60;

    private static final Color COLOR_LINEA = Color.web("#8a8a90");
    private static final Color COLOR_ACENTO = Color.web("#ff2f7e");
    private static final Color COLOR_TEXTO = Color.web("#1c1c1e");
    private static final Color COLOR_NODO = Color.web("#fbfbfa");
    private static final Color COLOR_ETIQUETA_FONDO = Color.web("#fbfbfa");
    private static final Color COLOR_ETIQUETA_BORDE = Color.web("#e2e2e7");

    /**
     * Dibuja el AFN dado. Pasar null limpia el lienzo (pantalla vacia).
     */
    public void dibujar(AFN afn) {
        getChildren().clear();

        if (afn == null || afn.getEstadoInicial() == null || afn.getEstadosAFN().isEmpty()) {
            setPrefSize(0, 0);
            return;
        }

        Map<Estado, Integer> capas = calcularCapas(afn);
        Map<Integer, List<Estado>> columnas = agruparPorColumna(afn, capas);
        Map<Estado, double[]> posiciones = calcularPosiciones(columnas);

        double[] posInicial = posiciones.get(afn.getEstadoInicial());
        dibujarEntradaInicial(posInicial[0], posInicial[1]);

        // Las transiciones se dibujan antes que los estados para que
        // los circulos de los estados queden siempre encima de las lineas.
        Map<String, Integer> contadorParejas = new HashMap<>();
        for (Estado origen : afn.getEstadosAFN()) {
            double[] posOrigen = posiciones.get(origen);
            if (posOrigen == null) {
                continue;
            }
            for (Transicion transicion : origen.getTransiciones()) {
                Estado destino = transicion.getEstadoDestino();
                double[] posDestino = posiciones.get(destino);
                if (posDestino == null) {
                    continue;
                }
                String etiqueta = transicion.IsEpsilon() ? "ε" : etiquetaSimbolo(transicion);

                if (origen == destino) {
                    dibujarBucle(posOrigen[0], posOrigen[1], etiqueta);
                } else {
                    String clave = System.identityHashCode(origen) + "->" + System.identityHashCode(destino);
                    int indice = contadorParejas.merge(clave, 1, Integer::sum) - 1;
                    dibujarTransicion(posOrigen[0], posOrigen[1], posDestino[0], posDestino[1], etiqueta, indice);
                }
            }
        }

        for (Estado estado : afn.getEstadosAFN()) {
            double[] pos = posiciones.get(estado);
            if (pos != null) {
                dibujarEstado(pos[0], pos[1], estado, estado == afn.getEstadoInicial());
            }
        }

        int maxFilas = columnas.values().stream().mapToInt(List::size).max().orElse(1);
        double ancho = MARGEN_X * 2 + columnas.size() * ESPACIO_X;
        double alto = MARGEN_Y * 2 + maxFilas * ESPACIO_Y;
        setPrefSize(Math.max(ancho, 260), Math.max(alto, 200));
    }

    // ---------- Layout ----------

    private Map<Estado, Integer> calcularCapas(AFN afn) {
        Map<Estado, Integer> capas = new HashMap<>();
        Deque<Estado> cola = new ArrayDeque<>();

        capas.put(afn.getEstadoInicial(), 0);
        cola.add(afn.getEstadoInicial());

        while (!cola.isEmpty()) {
            Estado actual = cola.poll();
            int capaActual = capas.get(actual);
            for (Transicion transicion : actual.getTransiciones()) {
                Estado destino = transicion.getEstadoDestino();
                if (!capas.containsKey(destino)) {
                    capas.put(destino, capaActual + 1);
                    cola.add(destino);
                }
            }
        }

        // Por si algun estado quedara inalcanzable desde el inicial
        // (no deberia pasar con Thompson, pero evita que se pierda del dibujo).
        int capaExtra = capas.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        for (Estado estado : afn.getEstadosAFN()) {
            capas.putIfAbsent(estado, capaExtra);
        }

        return capas;
    }

    private Map<Integer, List<Estado>> agruparPorColumna(AFN afn, Map<Estado, Integer> capas) {
        Map<Integer, List<Estado>> columnas = new TreeMap<>();
        for (Estado estado : afn.getEstadosAFN()) {
            columnas.computeIfAbsent(capas.get(estado), k -> new ArrayList<>()).add(estado);
        }
        return columnas;
    }

    private Map<Estado, double[]> calcularPosiciones(Map<Integer, List<Estado>> columnas) {
        Map<Estado, double[]> posiciones = new HashMap<>();
        int maxFilas = columnas.values().stream().mapToInt(List::size).max().orElse(1);
        double alturaTotal = maxFilas * ESPACIO_Y;

        for (Map.Entry<Integer, List<Estado>> columna : columnas.entrySet()) {
            List<Estado> estadosColumna = columna.getValue();
            double x = MARGEN_X + columna.getKey() * ESPACIO_X;
            double alturaColumna = estadosColumna.size() * ESPACIO_Y;
            double offsetY = MARGEN_Y + Math.max(0, (alturaTotal - alturaColumna) / 2.0);

            for (int i = 0; i < estadosColumna.size(); i++) {
                posiciones.put(estadosColumna.get(i), new double[]{x, offsetY + i * ESPACIO_Y});
            }
        }
        return posiciones;
    }

    private String etiquetaSimbolo(Transicion transicion) {
        char inferior = transicion.getSimboloInferior();
        char superior = transicion.getSimboloSuperior();
        return inferior == superior ? String.valueOf(inferior) : (inferior + "-" + superior);
    }

    // ---------- Dibujo ----------

    private void dibujarEstado(double x, double y, Estado estado, boolean esInicial) {
        Circle circulo = new Circle(x, y, RADIO);
        circulo.setFill(COLOR_NODO);
        circulo.setStroke(esInicial ? COLOR_ACENTO : COLOR_TEXTO);
        circulo.setStrokeWidth(esInicial ? 2.6 : 1.8);
        getChildren().add(circulo);

        if (estado.isEstadoAccept()) {
            Circle anilloAceptacion = new Circle(x, y, RADIO - 5);
            anilloAceptacion.setFill(Color.TRANSPARENT);
            anilloAceptacion.setStroke(COLOR_TEXTO);
            anilloAceptacion.setStrokeWidth(1.3);
            getChildren().add(anilloAceptacion);
        }

        agregarTexto(x, y, "q" + estado.getIdEstado(), COLOR_TEXTO, false);
    }

    private void dibujarEntradaInicial(double x, double y) {
        Line linea = new Line(x - RADIO - 28, y, x - RADIO - 4, y);
        linea.setStroke(COLOR_ACENTO);
        linea.setStrokeWidth(2);
        getChildren().add(linea);
        getChildren().add(puntaFlecha(x - RADIO - 4, y, 0, COLOR_ACENTO));
    }

    private void dibujarBucle(double x, double y, String etiqueta) {
        double inicioX = x - RADIO * 0.5;
        double inicioY = y - RADIO * 0.88;
        double finX = x + RADIO * 0.5;
        double finY = y - RADIO * 0.88;
        double ctrl1X = x - RADIO * 1.3;
        double ctrl1Y = y - RADIO * 2.7;
        double ctrl2X = x + RADIO * 1.3;
        double ctrl2Y = y - RADIO * 2.7;

        CubicCurve curva = new CubicCurve(inicioX, inicioY, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, finX, finY);
        curva.setFill(Color.TRANSPARENT);
        curva.setStroke(COLOR_LINEA);
        curva.setStrokeWidth(1.6);
        getChildren().add(curva);

        double anguloLlegada = Math.toDegrees(Math.atan2(finY - ctrl2Y, finX - ctrl2X));
        getChildren().add(puntaFlecha(finX, finY, anguloLlegada, COLOR_LINEA));

        agregarEtiqueta(x, y - RADIO * 2.95, etiqueta);
    }

    private void dibujarTransicion(double x1, double y1, double x2, double y2, String etiqueta, int indiceParalelo) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double distancia = Math.max(Math.sqrt(dx * dx + dy * dy), 1);
        double ux = dx / distancia;
        double uy = dy / distancia;

        double inicioX = x1 + ux * RADIO;
        double inicioY = y1 + uy * RADIO;
        double finX = x2 - ux * RADIO;
        double finY = y2 - uy * RADIO;

        boolean haciaAtras = x2 <= x1;
        double curvatura = (haciaAtras ? 46 : 26) + indiceParalelo * 22;
        double nx = -uy;
        double ny = ux;
        double signo = haciaAtras ? -1 : (indiceParalelo % 2 == 0 ? 1 : -1);

        double medioX = (inicioX + finX) / 2.0 + nx * curvatura * signo;
        double medioY = (inicioY + finY) / 2.0 + ny * curvatura * signo;

        QuadCurve curva = new QuadCurve(inicioX, inicioY, medioX, medioY, finX, finY);
        curva.setFill(Color.TRANSPARENT);
        curva.setStroke(COLOR_LINEA);
        curva.setStrokeWidth(1.6);
        getChildren().add(curva);

        double anguloLlegada = Math.toDegrees(Math.atan2(finY - medioY, finX - medioX));
        getChildren().add(puntaFlecha(finX, finY, anguloLlegada, COLOR_LINEA));

        agregarEtiqueta(medioX, medioY, etiqueta);
    }

    private Polygon puntaFlecha(double x, double y, double anguloGrados, Color color) {
        double rad = Math.toRadians(anguloGrados);
        double anguloAla = Math.toRadians(24);
        double largo = 10;

        double x1 = x - largo * Math.cos(rad - anguloAla);
        double y1 = y - largo * Math.sin(rad - anguloAla);
        double x2 = x - largo * Math.cos(rad + anguloAla);
        double y2 = y - largo * Math.sin(rad + anguloAla);

        Polygon punta = new Polygon(x, y, x1, y1, x2, y2);
        punta.setFill(color);
        return punta;
    }

    private void agregarEtiqueta(double x, double y, String texto) {
        Text etiqueta = new Text(texto);
        etiqueta.setStyle("-fx-font-size: 11px; -fx-font-weight: 700;");
        etiqueta.setFill(COLOR_ACENTO);
        etiqueta.applyCss();

        double anchoTexto = etiqueta.getLayoutBounds().getWidth();
        double altoTexto = etiqueta.getLayoutBounds().getHeight();

        Rectangle fondo = new Rectangle(x - anchoTexto / 2.0 - 4, y - altoTexto / 2.0 - 2, anchoTexto + 8, altoTexto + 4);
        fondo.setFill(COLOR_ETIQUETA_FONDO);
        fondo.setArcWidth(6);
        fondo.setArcHeight(6);
        fondo.setStroke(COLOR_ETIQUETA_BORDE);
        fondo.setStrokeWidth(1);
        getChildren().add(fondo);

        etiqueta.setX(x - anchoTexto / 2.0);
        etiqueta.setY(y + altoTexto / 4.0);
        getChildren().add(etiqueta);
    }

    private void agregarTexto(double x, double y, String texto, Color color, boolean negrita) {
        Text nodoTexto = new Text(texto);
        nodoTexto.setStyle("-fx-font-size: 11px; -fx-font-weight: " + (negrita ? "700" : "600") + ";");
        nodoTexto.setFill(color);
        nodoTexto.applyCss();

        double anchoTexto = nodoTexto.getLayoutBounds().getWidth();
        double altoTexto = nodoTexto.getLayoutBounds().getHeight();
        nodoTexto.setX(x - anchoTexto / 2.0);
        nodoTexto.setY(y + altoTexto / 4.0);
        getChildren().add(nodoTexto);
    }
}
