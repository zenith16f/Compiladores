package automaton.ui;

import automaton.AFD;
import automaton.AFN;
import automaton.Estado;
import automaton.Transicion;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class TablaTransicionesAfn {

    private static final String SIN_TRANSICION = "—";

    private TablaTransicionesAfn() {
    }

    public static class Fila {
        private final String estado;
        private final Map<String, String> celdas;

        Fila(String estado, Map<String, String> celdas) {
            this.estado = estado;
            this.celdas = celdas;
        }

        public String getEstado() {
            return estado;
        }

        public String obtener(String columna) {
            return celdas.getOrDefault(columna, SIN_TRANSICION);
        }
    }

    public static void poblar(TableView<Fila> tabla, AFN afn) {
        poblar(tabla, afn == null ? null : afn.getEstadoInicial(), afn == null ? null : afn.getEstadosAFN());
    }

    /**
     * Misma tabla, pero para un AFD (reutiliza el mismo motor: un AFD
     * tambien es, para estos efectos, un estado inicial + una lista de
     * Estado con sus transiciones).
     */
    public static void poblarAfd(TableView<Fila> tabla, AFD afd) {
        poblar(tabla, afd == null ? null : afd.getEstadoInicial(), afd == null ? null : afd.getEstadosAFD());
    }

    public static void poblar(TableView<Fila> tabla, Estado estadoInicial, List<Estado> estados) {
        tabla.getColumns().clear();
        tabla.getItems().clear();

        if (estadoInicial == null || estados == null) {
            return;
        }

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Fila, String> columnaEstado = new TableColumn<>("Estado");
        columnaEstado.setCellValueFactory(datos -> new ReadOnlyStringWrapper(datos.getValue().getEstado()));

        columnaEstado.setMinWidth(52);
        columnaEstado.setPrefWidth(58);
        columnaEstado.setMaxWidth(64);
        columnaEstado.setSortable(false);
        columnaEstado.setReorderable(false);
        columnaEstado.getStyleClass().add("columna-estado");
        columnaEstado.setCellFactory(col -> celdaEstilizada(false));
        tabla.getColumns().add(columnaEstado);

        TreeSet<String> simbolos = new TreeSet<>();
        boolean hayEpsilon = false;
        for (Estado estado : estados) {
            for (Transicion transicion : estado.getTransiciones()) {
                if (transicion.IsEpsilon()) {
                    hayEpsilon = true;
                } else {
                    simbolos.add(etiquetaSimbolo(transicion));
                }
            }
        }

        for (String simbolo : simbolos) {
            tabla.getColumns().add(crearColumnaSimbolo(simbolo));
        }
        if (hayEpsilon) {
            tabla.getColumns().add(crearColumnaSimbolo("ε"));
        }

        List<Fila> filas = new ArrayList<>();
        for (Estado estado : estados) {
            Map<String, List<String>> destinosPorSimbolo = new HashMap<>();
            for (Transicion transicion : estado.getTransiciones()) {
                String clave = transicion.IsEpsilon() ? "ε" : etiquetaSimbolo(transicion);
                destinosPorSimbolo
                        .computeIfAbsent(clave, k -> new ArrayList<>())
                        .add("q" + transicion.getEstadoDestino().getIdEstado());
            }

            Map<String, String> celdas = new HashMap<>();
            for (Map.Entry<String, List<String>> entrada : destinosPorSimbolo.entrySet()) {
                celdas.put(entrada.getKey(), String.join(", ", entrada.getValue()));
            }

            String etiquetaEstado = (estado == estadoInicial ? "→" : "")
                    + "q" + estado.getIdEstado()
                    + (estado.isEstadoAccept() ? "*" : "");
            filas.add(new Fila(etiquetaEstado, celdas));
        }

        tabla.getItems().setAll(filas);
    }

    private static TableColumn<Fila, String> crearColumnaSimbolo(String simbolo) {
        TableColumn<Fila, String> columna = new TableColumn<>(simbolo);
        columna.setCellValueFactory(datos -> new ReadOnlyStringWrapper(datos.getValue().obtener(simbolo)));
        columna.setMinWidth(60);
        columna.setSortable(false);
        columna.setReorderable(false);
        columna.setCellFactory(col -> celdaEstilizada(true));
        return columna;
    }

    
    private static TableCell<Fila, String> celdaEstilizada(boolean puedeEstarVacia) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                if (vacia || valor == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(valor);
                if (puedeEstarVacia && SIN_TRANSICION.equals(valor)) {
                    setStyle("-fx-text-fill: #c7c7cc;");
                } else {
                    setStyle("-fx-text-fill: #1c1c1e; -fx-font-weight: 600;");
                }
            }
        };
    }

    private static String etiquetaSimbolo(Transicion transicion) {
        char inferior = transicion.getSimboloInferior();
        char superior = transicion.getSimboloSuperior();
        return inferior == superior ? String.valueOf(inferior) : (inferior + "-" + superior);
    }
}
