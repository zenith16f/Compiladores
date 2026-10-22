package automaton.ui;

import automaton.AFD;
import automaton.AFN;
import automaton.Estado;
import automaton.Transicion;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        ScrollPane scrollPane = new ScrollPane();


        tabla.getColumns().clear();
        tabla.getItems().clear();
        scrollPane.setContent(tabla);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(false);

        if (estadoInicial == null || estados == null) {
            return;
        }

        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

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

        // Destino (por estado) de cada simbolo individual (p.ej. 'a', 'b', 'c'...)
        // que no viene ya con un rango explicito en la transicion.
        Map<Character, Map<Estado, String>> destinoPorCaracter = new HashMap<>();
        // Destino (por estado) de columnas que se muestran tal cual: epsilon y
        // transiciones que ya traian un rango explicito (p.ej. de CrearAFN(a, z)).
        Map<String, Map<Estado, String>> destinoPorEtiquetaFija = new HashMap<>();
        boolean hayEpsilon = false;

        for (Estado estado : estados) {
            Map<Character, List<String>> destinosCaracter = new HashMap<>();
            Map<String, List<String>> destinosFija = new HashMap<>();

            for (Transicion transicion : estado.getTransiciones()) {
                String destino = "q" + transicion.getEstadoDestino().getIdEstado();
                if (transicion.IsEpsilon()) {
                    hayEpsilon = true;
                    destinosFija.computeIfAbsent("ε", k -> new ArrayList<>()).add(destino);
                } else if (transicion.getSimboloInferior() == transicion.getSimboloSuperior()) {
                    destinosCaracter.computeIfAbsent(transicion.getSimboloInferior(), k -> new ArrayList<>()).add(destino);
                } else {
                    destinosFija.computeIfAbsent(etiquetaSimbolo(transicion), k -> new ArrayList<>()).add(destino);
                }
            }

            for (Map.Entry<Character, List<String>> entrada : destinosCaracter.entrySet()) {
                List<String> destinos = entrada.getValue();
                Collections.sort(destinos);
                destinoPorCaracter
                        .computeIfAbsent(entrada.getKey(), k -> new HashMap<>())
                        .put(estado, String.join(", ", destinos));
            }
            for (Map.Entry<String, List<String>> entrada : destinosFija.entrySet()) {
                List<String> destinos = entrada.getValue();
                Collections.sort(destinos);
                destinoPorEtiquetaFija
                        .computeIfAbsent(entrada.getKey(), k -> new HashMap<>())
                        .put(estado, String.join(", ", destinos));
            }
        }

        // Columnas finales, ordenadas alfabeticamente por etiqueta: empezamos con
        // las fijas (rangos explicitos) y les sumamos los caracteres sueltos,
        // fusionando en un rango ("a-z") los que sean consecutivos y tengan
        // exactamente el mismo destino en todos los estados.
        Map<String, Map<Estado, String>> columnas = new TreeMap<>(destinoPorEtiquetaFija);

        List<Character> caracteres = new ArrayList<>(destinoPorCaracter.keySet());
        Collections.sort(caracteres);
        int i = 0;
        while (i < caracteres.size()) {
            char inicio = caracteres.get(i);
            Map<Estado, String> destinoGrupo = destinoPorCaracter.get(inicio);
            int j = i + 1;
            while (j < caracteres.size()
                    && caracteres.get(j) == caracteres.get(j - 1) + 1
                    && destinoPorCaracter.get(caracteres.get(j)).equals(destinoGrupo)) {
                j++;
            }
            char fin = caracteres.get(j - 1);
            String etiqueta = inicio == fin
                    ? CampoSimbolo.mostrar(inicio)
                    : CampoSimbolo.mostrar(inicio) + "-" + CampoSimbolo.mostrar(fin);
            columnas.put(etiqueta, destinoGrupo);
            i = j;
        }

        for (String etiqueta : columnas.keySet()) {
            tabla.getColumns().add(crearColumnaSimbolo(etiqueta));
        }
        if (hayEpsilon) {
            tabla.getColumns().add(crearColumnaSimbolo("ε"));
        }

        List<Fila> filas = new ArrayList<>();
        for (Estado estado : estados) {
            Map<String, String> celdas = new HashMap<>();
            for (Map.Entry<String, Map<Estado, String>> columna : columnas.entrySet()) {
                String destino = columna.getValue().get(estado);
                if (destino != null) {
                    celdas.put(columna.getKey(), destino);
                }
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
        return inferior == superior ? CampoSimbolo.mostrar(inferior)
                : (CampoSimbolo.mostrar(inferior) + "-" + CampoSimbolo.mostrar(superior));
    }
}
