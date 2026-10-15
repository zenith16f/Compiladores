package automaton.ui;

import automaton.AlmacenAutomatas;
import automaton.AnalizadorLexico;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class AnalizadorLexicoController implements VistaConContexto {

    /** Una fila de la tabla de resultados. */
    public record Fila(String token, String lexema) {
    }

    @FXML private Label etiquetaEstadoAfd;
    @FXML private TextArea areaCadena;
    @FXML private TableView<Fila> tablaResultado;
    @FXML private TableColumn<Fila, String> columnaToken;
    @FXML private TableColumn<Fila, String> columnaLexema;
    @FXML private Button botonAnalizarTodo;
    @FXML private Button botonSiguiente;
    @FXML private Button botonUndo;

    private AlmacenAutomatas almacen;
    private AnalizadorLexico analizador;

    // true cuando la tabla ya no corresponde al texto (se edito la cadena o no se ha empezado)
    private boolean cadenaCambiada = true;
    private boolean terminado = false; // yylex() ya regreso FIN

    @FXML
    public void initialize() {
        columnaToken.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().token()));
        columnaLexema.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().lexema()));
        areaCadena.textProperty().addListener((obs, anterior, nuevo) -> {
            cadenaCambiada = true;
            actualizarBotones();
        });
        actualizarBotones();
    }

    @Override
    public void setContext(AppContext contexto) {
        this.almacen = contexto.getAlmacen();
    }

    @FXML
    private void onCargarAfd() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Cargar AFD exportado");
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tabla de AFD (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        if (almacen != null && almacen.getCarpetaLexico().toFile().isDirectory()) {
            selector.setInitialDirectory(almacen.getCarpetaLexico().toFile());
        }
        File archivo = selector.showOpenDialog(areaCadena.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            analizador = AnalizadorLexico.desdeArchivo(archivo.toPath());
        } catch (IOException | RuntimeException e) {
            Avisos.error("No se pudo cargar '" + archivo.getName() + "': " + e.getMessage());
            return;
        }
        etiquetaEstadoAfd.setText("AFD listo (" + analizador.getNumEstados() + " estados) — " + archivo.getName());
        etiquetaEstadoAfd.getStyleClass().setAll("estado-afd-listo");
        limpiarResultado();
        Avisos.info("AFD cargado", "Se cargó '" + archivo.getName() + "' con "
                + analizador.getNumEstados() + " estados.");
    }

    @FXML
    private void onAnalizarTodo() {
        if (!hayAfd()) {
            return;
        }
        reiniciarAnalisis();
        while (!terminado) {
            siguienteToken();
        }
        actualizarBotones();
    }

    @FXML
    private void onSiguienteToken() {
        if (!hayAfd()) {
            return;
        }
        if (cadenaCambiada) {
            reiniciarAnalisis();
        }
        if (!terminado) {
            siguienteToken();
            resaltarLexema();
        }
        actualizarBotones();
    }

    @FXML
    private void onUndoToken() {
        if (analizador == null || cadenaCambiada || tablaResultado.getItems().isEmpty()) {
            Avisos.aviso("No hay tokens que deshacer.");
            return;
        }
        analizador.undoToken();
        tablaResultado.getItems().remove(tablaResultado.getItems().size() - 1);
        terminado = false;
        areaCadena.positionCaret(analizador.getIndexCharAct());
        actualizarBotones();
    }

    @FXML
    private void onReiniciar() {
        areaCadena.clear();
        limpiarResultado();
    }

    // ---------- Auxiliares ----------

    private boolean hayAfd() {
        if (analizador == null) {
            Avisos.aviso("Primero carga un AFD exportado con «Cargar AFD de archivo…».");
            return false;
        }
        return true;
    }

    private void reiniciarAnalisis() {
        analizador.setSigma(areaCadena.getText());
        tablaResultado.getItems().clear();
        cadenaCambiada = false;
        terminado = false;
    }

    private void limpiarResultado() {
        tablaResultado.getItems().clear();
        cadenaCambiada = true;
        terminado = false;
        actualizarBotones();
    }

    private void siguienteToken() {
        int token = analizador.yylex();
        String textoToken;
        String lexema;
        if (token == AnalizadorLexico.FIN) {
            textoToken = token + " (FIN)";
            lexema = "EOF";
            terminado = true;
        } else if (token == AnalizadorLexico.TOKEN_ERROR) {
            textoToken = token + " (ERROR)";
            lexema = visible(analizador.getYytext());
        } else {
            textoToken = String.valueOf(token);
            lexema = visible(analizador.getYytext());
        }
        tablaResultado.getItems().add(new Fila(textoToken, lexema));
        tablaResultado.scrollTo(tablaResultado.getItems().size() - 1);
    }

    private void resaltarLexema() {
        areaCadena.selectRange(analizador.getIndexInicioLex(), analizador.getIndexCharAct());
    }

    // Los espacios y saltos de linea no se verian en la tabla
    private static String visible(String lexema) {
        return lexema.replace(" ", "␣").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n");
    }

    private void actualizarBotones() {
        boolean sinAfd = analizador == null;
        botonAnalizarTodo.setDisable(sinAfd);
        botonSiguiente.setDisable(sinAfd || (terminado && !cadenaCambiada));
        botonUndo.setDisable(sinAfd || cadenaCambiada || tablaResultado.getItems().isEmpty());
    }
}
