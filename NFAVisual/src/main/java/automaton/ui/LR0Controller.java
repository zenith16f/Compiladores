package automaton.ui;

import automaton.AlmacenAutomatas;
import automaton.AnalizadorLexico;
import automaton.Lexico;
import automaton.ll1.DescRecGramGram;
import automaton.ll1.ElemArrReglas;
import automaton.ll1.Gramatica;
import automaton.ll1.LexicoTerminales;
import automaton.ll1.SimbGram;
import automaton.lr0.ColeccionCanonicaLR0;
import automaton.lr0.ItemLR0;
import automaton.lr0.TablaLR0;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LR0Controller implements VistaConContexto {

    public static final String GRAMATICA_EJEMPLO = """
            S -> a S b | a b ;
            """;

    public record FilaRegla(String id, String izq, String der) {
    }

    public record FilaEstado(String estado, String items, String transiciones) {
    }

    public record FilaPaso(String num, String pila, String cadena, String accion) {
    }

    @FXML private TextArea areaGramatica;
    @FXML private Label etiquetaEstadoTabla;
    @FXML private TabPane pestanas;

    @FXML private TableView<FilaRegla> tablaReglas;
    @FXML private TableColumn<FilaRegla, String> colReglaId;
    @FXML private TableColumn<FilaRegla, String> colReglaIzq;
    @FXML private TableColumn<FilaRegla, String> colReglaDer;

    @FXML private TableView<FilaEstado> tablaEstados;
    @FXML private TableColumn<FilaEstado, String> colEdoId;
    @FXML private TableColumn<FilaEstado, String> colEdoItems;
    @FXML private TableColumn<FilaEstado, String> colEdoTrans;

    @FXML private TableView<Integer> tablaLR0;
    @FXML private Label etiquetaConflictos;

    @FXML private TableView<Integer> tablaTerminales;
    @FXML private TableColumn<Integer, String> colTermSimbolo;
    @FXML private TableColumn<Integer, String> colTermToken;
    @FXML private Label etiquetaLexico;
    @FXML private TextField campoSigma;
    @FXML private Label etiquetaResultado;
    @FXML private TableView<FilaPaso> tablaPasos;
    @FXML private TableColumn<FilaPaso, String> colPasoNum;
    @FXML private TableColumn<FilaPaso, String> colPasoPila;
    @FXML private TableColumn<FilaPaso, String> colPasoCadena;
    @FXML private TableColumn<FilaPaso, String> colPasoAccion;

    private AlmacenAutomatas almacen;
    private TablaLR0 tabla;
    private AnalizadorLexico lexicoAfd;
    private String nombreAfd;

    @FXML
    public void initialize() {
        colReglaId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().id()));
        colReglaIzq.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().izq()));
        colReglaDer.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().der()));

        colEdoId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().estado()));
        colEdoItems.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().items()));
        colEdoTrans.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().transiciones()));

        colPasoNum.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().num()));
        colPasoPila.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().pila()));
        colPasoCadena.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().cadena()));
        colPasoAccion.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().accion()));

        colTermSimbolo.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(tabla.getSimbTerm()[c.getValue()].toString()));
        colTermToken.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(String.valueOf(tabla.getSimbTerm()[c.getValue()].getToken())));
        colTermToken.setCellFactory(TextFieldTableCell.forTableColumn());
        colTermToken.setOnEditCommit(evento -> onEditarToken(evento.getRowValue(), evento.getNewValue()));

        areaGramatica.setText(GRAMATICA_EJEMPLO);
    }

    @Override
    public void setContext(AppContext contexto) {
        this.almacen = contexto.getAlmacen();
    }

    @FXML
    private void onGenerar() {
        Gramatica gramatica;
        TablaLR0 nueva;
        try {
            gramatica = DescRecGramGram.analizar(areaGramatica.getText());
            nueva = new TablaLR0(gramatica);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Avisos.aviso(e.getMessage());
            etiquetaEstadoTabla.setText("La gramática tiene errores: " + e.getMessage());
            etiquetaEstadoTabla.getStyleClass().setAll("texto-error");
            return;
        }
        tabla = nueva;

        mostrarReglas(tabla.getColeccion());
        mostrarEstados(tabla.getColeccion());
        mostrarTablaLR0();
        mostrarTerminales();
        tablaPasos.getItems().clear();
        etiquetaResultado.setText("");

        int numEstados = tabla.getColeccion().getEstados().size();
        if (tabla.esLR0()) {
            etiquetaEstadoTabla.setText("Tabla lista: " + numEstados + " estados, "
                    + tabla.getColeccion().getReglasAumentadas().size() + " reglas, "
                    + tabla.getSimbNoTerm().length + " no terminales, "
                    + (tabla.getSimbTerm().length - 1) + " terminales.");
            etiquetaEstadoTabla.getStyleClass().setAll("estado-afd-listo");
            Avisos.info("Tabla LR(0) generada", "La gramática es LR(0) (" + numEstados + " estados).");
        } else {
            etiquetaEstadoTabla.setText("La gramática NO es LR(0): hay " + tabla.getConflictos().size()
                    + " conflicto(s). Revisa la pestaña Tabla LR(0).");
            etiquetaEstadoTabla.getStyleClass().setAll("texto-error");
            Avisos.aviso("La gramática no es LR(0): se detectaron conflictos.");
        }
        pestanas.getSelectionModel().select(2);
    }

    @FXML
    private void onEjemplo() {
        areaGramatica.setText(GRAMATICA_EJEMPLO);
    }

    @FXML
    private void onAbrirArchivo() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Abrir gramática");
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Texto (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        File archivo = selector.showOpenDialog(areaGramatica.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        try {
            areaGramatica.setText(Files.readString(archivo.toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            Avisos.error("No se pudo leer '" + archivo.getName() + "': " + e.getMessage());
        }
    }

    private void mostrarReglas(ColeccionCanonicaLR0 coleccion) {
        tablaReglas.getItems().clear();
        List<ElemArrReglas> reglas = coleccion.getReglasAumentadas();
        for (int i = 0; i < reglas.size(); i++) {
            tablaReglas.getItems().add(new FilaRegla(String.valueOf(i),
                    reglas.get(i).getSimbIzqGram().toString(), reglas.get(i).ladoDerechoTexto()));
        }
    }

    private void mostrarEstados(ColeccionCanonicaLR0 coleccion) {
        tablaEstados.getItems().clear();
        List<Set<ItemLR0>> estados = coleccion.getEstados();
        List<Map<SimbGram, Integer>> transiciones = coleccion.getTransiciones();
        for (int i = 0; i < estados.size(); i++) {
            final int numEdo = i;
            String itemsTexto = estados.get(i).stream()
                    .map(ItemLR0::toString)
                    .collect(Collectors.joining("\n"));
            String transTexto = transiciones.get(i).entrySet().stream()
                    .map(e -> "Ir_a(I" + numEdo + ", " + e.getKey() + ") = I" + e.getValue())
                    .collect(Collectors.joining("   |   "));
            tablaEstados.getItems().add(new FilaEstado("I" + i, itemsTexto, transTexto));
        }
    }

    private void mostrarTablaLR0() {
        tablaLR0.getColumns().clear();
        tablaLR0.getItems().clear();

        TableColumn<Integer, String> colEstado = new TableColumn<>("Estado");
        colEstado.setSortable(false);
        colEstado.setPrefWidth(70);
        colEstado.getStyleClass().add("columna-estado");
        colEstado.setCellValueFactory(c -> new ReadOnlyStringWrapper("I" + c.getValue()));
        tablaLR0.getColumns().add(colEstado);

        SimbGram[] terminales = tabla.getSimbTerm();
        for (int j = 0; j < terminales.length; j++) {
            final int colTerm = j;
            TableColumn<Integer, String> col = new TableColumn<>(encabezadoTerminal(colTerm));
            col.setSortable(false);
            col.setPrefWidth(68);
            col.setCellValueFactory(c -> {
                TablaLR0.Accion acc = tabla.getTablaAction()[c.getValue()][colTerm];
                return new ReadOnlyStringWrapper(acc.toString());
            });
            tablaLR0.getColumns().add(col);
        }

        SimbGram[] noTerminales = tabla.getSimbNoTerm();
        for (int j = 0; j < noTerminales.length; j++) {
            final int colNoTerm = j;
            TableColumn<Integer, String> col = new TableColumn<>("GOTO\n" + noTerminales[colNoTerm]);
            col.setSortable(false);
            col.setPrefWidth(68);
            col.setCellValueFactory(c -> {
                int destino = tabla.getTablaGoto()[c.getValue()][colNoTerm];
                return new ReadOnlyStringWrapper(destino == TablaLR0.SIN_ESTADO ? "" : String.valueOf(destino));
            });
            tablaLR0.getColumns().add(col);
        }

        int numEstados = tabla.getColeccion().getEstados().size();
        for (int i = 0; i < numEstados; i++) {
            tablaLR0.getItems().add(i);
        }

        etiquetaConflictos.setText(tabla.esLR0() ? "" : String.join("\n", tabla.getConflictos()));
    }

    private String encabezadoTerminal(int columna) {
        SimbGram t = tabla.getSimbTerm()[columna];
        return t + "\n" + t.getToken();
    }

    private void mostrarTerminales() {
        tablaTerminales.getItems().clear();
        for (int j = 0; j < tabla.getSimbTerm().length - 1; j++) {
            tablaTerminales.getItems().add(j);
        }
    }

    private void onEditarToken(int indice, String texto) {
        try {
            tabla.setTokenTerminal(indice, Integer.parseInt(texto.trim()));
        } catch (NumberFormatException e) {
            Avisos.aviso("El token debe ser un número entero.");
        } catch (IllegalArgumentException e) {
            Avisos.aviso(e.getMessage());
        }
        tablaTerminales.refresh();
        for (int j = 0; j < tabla.getSimbTerm().length; j++) {
            tablaLR0.getColumns().get(j + 1).setText(encabezadoTerminal(j));
        }
    }

    @FXML
    private void onCargarAfd() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Cargar AFD exportado para σ");
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tabla de AFD (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        if (almacen != null && almacen.getCarpetaLexico().toFile().isDirectory()) {
            selector.setInitialDirectory(almacen.getCarpetaLexico().toFile());
        }
        File archivo = selector.showOpenDialog(campoSigma.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        try {
            lexicoAfd = AnalizadorLexico.desdeArchivo(archivo.toPath());
        } catch (IOException | RuntimeException e) {
            Avisos.error("No se pudo cargar '" + archivo.getName() + "': " + e.getMessage());
            return;
        }
        nombreAfd = archivo.getName();
        etiquetaLexico.setText("AFD: " + nombreAfd + " (" + lexicoAfd.getNumEstados()
                + " estados). Los tokens de los terminales deben ser los del AFD.");
        etiquetaLexico.getStyleClass().setAll("estado-afd-listo");
        Avisos.info("AFD cargado", "σ se analizará con '" + nombreAfd + "'.");
    }

    @FXML
    private void onQuitarAfd() {
        lexicoAfd = null;
        nombreAfd = null;
        etiquetaLexico.setText("Sin AFD: σ se escribe con los nombres de los terminales (ej. a a b b)");
        etiquetaLexico.getStyleClass().setAll("estado-afd-pendiente");
    }

    @FXML
    private void onAnalizar() {
        if (tabla == null) {
            Avisos.aviso("Primero genera la tabla LR(0).");
            return;
        }
        String sigma = campoSigma.getText() == null ? "" : campoSigma.getText();
        Lexico lexico = lexicoAfd != null ? lexicoAfd : new LexicoTerminales(tabla.getSimbTerm());

        TablaLR0.ResultadoAnalisis resultado = tabla.analizar(lexico, sigma);

        tablaPasos.getItems().clear();
        List<TablaLR0.Paso> pasos = resultado.pasos();
        for (int i = 0; i < pasos.size(); i++) {
            TablaLR0.Paso p = pasos.get(i);
            tablaPasos.getItems().add(new FilaPaso(String.valueOf(i + 1), p.pila(), p.cadena(), p.accion()));
        }
        etiquetaResultado.setText((resultado.aceptada() ? "✔ " : "✘ ") + resultado.mensaje());
        etiquetaResultado.getStyleClass().setAll(resultado.aceptada() ? "estado-afd-listo" : "texto-error");
        if (resultado.aceptada()) {
            Avisos.info("Cadena aceptada", "σ pertenece al lenguaje de la gramática.");
        } else {
            Avisos.aviso("Cadena rechazada: " + resultado.mensaje());
        }
    }
}
