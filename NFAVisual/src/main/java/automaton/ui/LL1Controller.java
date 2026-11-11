package automaton.ui;

import automaton.AlmacenAutomatas;
import automaton.AnalizadorLexico;
import automaton.Lexico;
import automaton.ll1.DescRecGramGram;
import automaton.ll1.ElemArrReglas;
import automaton.ll1.Gramatica;
import automaton.ll1.LexicoTerminales;
import automaton.ll1.SimbGram;
import automaton.ll1.TablaLL1;
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

/**
 * Pantalla de la tabla LL(1):
 *   1. Lee la gramatica con descenso recursivo (DescRecGramGram) y llena el arreglo de reglas.
 *   2. Calcula First y Follow y construye la tabla (TablaLL1).
 *   3. Analiza una cadena σ con la tabla, usando un AFD exportado como analizador lexico
 *      (o, si no hay AFD, reconociendo directamente los nombres de los terminales).
 */
public class LL1Controller implements VistaConContexto {

    public static final String GRAMATICA_EJEMPLO = """
            E  -> T E' ;
            E' -> + T E' | - T E' | epsilon ;
            T  -> F T' ;
            T' -> * F T' | / F T' | epsilon ;
            F  -> ( E ) | num ;
            """;

    public record FilaRegla(String id, String izq, String der) {
    }

    public record FilaFirstFollow(String simbolo, String first, String follow) {
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

    @FXML private TableView<FilaFirstFollow> tablaFirstFollow;
    @FXML private TableColumn<FilaFirstFollow, String> colFfSimbolo;
    @FXML private TableColumn<FilaFirstFollow, String> colFfFirst;
    @FXML private TableColumn<FilaFirstFollow, String> colFfFollow;

    @FXML private TableView<Integer> tablaLL1; // cada renglon es el indice de un no terminal
    @FXML private Label etiquetaConflictos;

    @FXML private TableView<Integer> tablaTerminales; // cada renglon es el indice de un terminal
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
    private TablaLL1 tabla;
    private AnalizadorLexico lexicoAfd; // null = se usa LexicoTerminales
    private String nombreAfd;

    @FXML
    public void initialize() {
        colReglaId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().id()));
        colReglaIzq.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().izq()));
        colReglaDer.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().der()));

        colFfSimbolo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().simbolo()));
        colFfFirst.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().first()));
        colFfFollow.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().follow()));

        colPasoNum.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().num()));
        colPasoPila.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().pila()));
        colPasoCadena.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().cadena()));
        colPasoAccion.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().accion()));

        // Tabla de terminales: el token se puede editar (doble clic) para que coincida con el AFD
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

    // =====================================================================================
    //  Gramatica y tabla
    // =====================================================================================

    @FXML
    private void onGenerar() {
        Gramatica gramatica;
        TablaLL1 nueva;
        try {
            gramatica = DescRecGramGram.analizar(areaGramatica.getText());
            nueva = new TablaLL1(gramatica);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Avisos.aviso(e.getMessage());
            etiquetaEstadoTabla.setText("La gramática tiene errores: " + e.getMessage());
            etiquetaEstadoTabla.getStyleClass().setAll("texto-error");
            return;
        }
        tabla = nueva;

        mostrarReglas(gramatica);
        mostrarFirstFollow(gramatica);
        mostrarTablaLL1();
        mostrarTerminales();
        tablaPasos.getItems().clear();
        etiquetaResultado.setText("");

        if (tabla.esLL1()) {
            etiquetaEstadoTabla.setText("Tabla lista: " + gramatica.getNumReglas() + " reglas, "
                    + tabla.getSimbNoTerm().length + " no terminales, "
                    + (tabla.getSimbTerm().length - 1) + " terminales.");
            etiquetaEstadoTabla.getStyleClass().setAll("estado-afd-listo");
            Avisos.info("Tabla LL(1) generada", "La gramática es LL(1).");
        } else {
            etiquetaEstadoTabla.setText("La gramática NO es LL(1): hay " + tabla.getConflictos().size()
                    + " conflicto(s). Revisa la pestaña Tabla LL(1).");
            etiquetaEstadoTabla.getStyleClass().setAll("texto-error");
            Avisos.aviso("La gramática no es LL(1): hay celdas con dos reglas.");
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

    private void mostrarReglas(Gramatica gramatica) {
        tablaReglas.getItems().clear();
        List<ElemArrReglas> reglas = gramatica.getArrReglas();
        for (int i = 0; i < reglas.size(); i++) {
            tablaReglas.getItems().add(new FilaRegla(String.valueOf(i),
                    reglas.get(i).getSimbIzqGram().toString(), reglas.get(i).ladoDerechoTexto()));
        }
    }

    private void mostrarFirstFollow(Gramatica gramatica) {
        tablaFirstFollow.getItems().clear();
        for (SimbGram noTerminal : gramatica.getConjNoTerm()) {
            tablaFirstFollow.getItems().add(new FilaFirstFollow(noTerminal.toString(),
                    TablaLL1.textoConjunto(gramatica.First(List.of(noTerminal))),
                    TablaLL1.textoConjunto(gramatica.Follow(noTerminal))));
        }
    }

    /** Columnas = terminales (con su token) y $, renglones = no terminales. */
    private void mostrarTablaLL1() {
        tablaLL1.getColumns().clear();
        tablaLL1.getItems().clear();

        TableColumn<Integer, String> colNoTerm = new TableColumn<>("");
        colNoTerm.setSortable(false);
        colNoTerm.setPrefWidth(70);
        colNoTerm.getStyleClass().add("columna-estado");
        colNoTerm.setCellValueFactory(c -> new ReadOnlyStringWrapper(tabla.getSimbNoTerm()[c.getValue()].toString()));
        tablaLL1.getColumns().add(colNoTerm);

        SimbGram[] terminales = tabla.getSimbTerm();
        for (int j = 0; j < terminales.length; j++) {
            final int columna = j;
            TableColumn<Integer, String> col = new TableColumn<>(encabezadoTerminal(columna));
            col.setSortable(false);
            col.setPrefWidth(64);
            col.setCellValueFactory(c -> {
                int regla = tabla.getTabla()[c.getValue()][columna];
                return new ReadOnlyStringWrapper(regla == TablaLL1.SIN_REGLA ? "" : String.valueOf(regla));
            });
            tablaLL1.getColumns().add(col);
        }
        for (int i = 0; i < tabla.getSimbNoTerm().length; i++) {
            tablaLL1.getItems().add(i);
        }

        etiquetaConflictos.setText(tabla.esLL1() ? "" : String.join("\n", tabla.getConflictos()));
    }

    private String encabezadoTerminal(int columna) {
        SimbGram t = tabla.getSimbTerm()[columna];
        return t + "\n" + t.getToken();
    }

    // =====================================================================================
    //  Analisis de σ
    // =====================================================================================

    private void mostrarTerminales() {
        tablaTerminales.getItems().clear();
        for (int j = 0; j < tabla.getSimbTerm().length - 1; j++) { // el $ no se edita (es FIN = 0)
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
        // Los encabezados de la tabla LL(1) muestran el token
        for (int j = 0; j < tabla.getSimbTerm().length; j++) {
            tablaLL1.getColumns().get(j + 1).setText(encabezadoTerminal(j));
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
        etiquetaLexico.setText("Sin AFD: σ se escribe con los nombres de los terminales (ej. num+num*(num))");
        etiquetaLexico.getStyleClass().setAll("estado-afd-pendiente");
    }

    @FXML
    private void onAnalizar() {
        if (tabla == null) {
            Avisos.aviso("Primero genera la tabla LL(1).");
            return;
        }
        String sigma = campoSigma.getText() == null ? "" : campoSigma.getText();
        Lexico lexico = lexicoAfd != null ? lexicoAfd : new LexicoTerminales(tabla);

        TablaLL1.ResultadoAnalisis resultado = tabla.analizar(lexico, sigma);

        tablaPasos.getItems().clear();
        List<TablaLL1.Paso> pasos = resultado.pasos();
        for (int i = 0; i < pasos.size(); i++) {
            TablaLL1.Paso p = pasos.get(i);
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
