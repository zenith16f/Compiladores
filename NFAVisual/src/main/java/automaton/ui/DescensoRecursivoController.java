package automaton.ui;

import automaton.AlmacenAutomatas;
import automaton.AnalizadorLexico;
import automaton.Lexico;
import automaton.descensoRecursivo.DescRecCalculadora;
import automaton.descensoRecursivo.DescRecGramatica;
import automaton.descensoRecursivo.LexicoCalculadora;
import automaton.descensoRecursivo.PasoDescenso;
import automaton.descensoRecursivo.ResultadoCalculadora;
import automaton.descensoRecursivo.TokensCalculadora;
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
import java.util.Map;
import java.util.Set;

/**
 * Controlador de la pantalla de Descenso Recursivo:
 *  1. Permite ingresar cualquier gramatica G libre de recursion por la izquierda.
 *  2. Calcula las reglas, conjuntos First de decision y genera el codigo/funciones del descenso recursivo.
 *  3. Permite probar una cadena σ en crudo o sincronizada con un AFD (con edicion de tokens en la tabla).
 *  4. Incluye tambien la pestaña de la Calculadora Aritmetica por Descenso Recursivo (DescRecCalculadora)
 *     con sintesis de valor numerico y notacion postfija.
 */
public class DescensoRecursivoController implements VistaConContexto {

    public static final String GRAMATICA_EJEMPLO = """
            E  -> T E' ;
            E' -> + T E' | - T E' | epsilon ;
            T  -> F T' ;
            T' -> * F T' | / F T' | epsilon ;
            F  -> ( E ) | num ;
            """;

    public record FilaReglaFirst(String id, String funcion, String der, String first) {
    }

    public record FilaPasoGram(String num, String funcion, String regla, String token, String cadena, String accion) {
    }

    public record FilaPasoCalc(String num, String funcion, String detalle, String token, String atributos) {
    }

    // Panel izquierdo: Gramatica
    @FXML private TextArea areaGramatica;
    @FXML private Label etiquetaEstadoAnalizador;
    @FXML private TabPane pestanas;

    // Tab 1: Reglas y First
    @FXML private TableView<FilaReglaFirst> tablaReglas;
    @FXML private TableColumn<FilaReglaFirst, String> colReglaId;
    @FXML private TableColumn<FilaReglaFirst, String> colReglaIzq;
    @FXML private TableColumn<FilaReglaFirst, String> colReglaDer;
    @FXML private TableColumn<FilaReglaFirst, String> colReglaFirst;

    // Tab 2: Funciones Recursivas (Pseudocodigo)
    @FXML private TextArea areaPseudocodigo;

    // Tab 3: Analizar σ con la gramatica
    @FXML private TableView<Integer> tablaTerminales;
    @FXML private TableColumn<Integer, String> colTermSimbolo;
    @FXML private TableColumn<Integer, String> colTermToken;
    @FXML private Label etiquetaLexico;
    @FXML private TextField campoSigma;
    @FXML private Label etiquetaResultado;
    @FXML private TableView<FilaPasoGram> tablaPasos;
    @FXML private TableColumn<FilaPasoGram, String> colPasoNum;
    @FXML private TableColumn<FilaPasoGram, String> colPasoFuncion;
    @FXML private TableColumn<FilaPasoGram, String> colPasoRegla;
    @FXML private TableColumn<FilaPasoGram, String> colPasoToken;
    @FXML private TableColumn<FilaPasoGram, String> colPasoCadena;
    @FXML private TableColumn<FilaPasoGram, String> colPasoAccion;
    @FXML private TextArea areaArbolGramatica;

    // Tab 4: Calculadora Aritmetica Directa
    @FXML private TextField campoExpresionCalc;
    @FXML private Label etiquetaLexicoCalc;
    @FXML private Label etiquetaResultadoCalc;
    @FXML private Label etiquetaValorCalc;
    @FXML private Label etiquetaPostfijoCalc;
    @FXML private TableView<FilaPasoCalc> tablaPasosCalc;
    @FXML private TableColumn<FilaPasoCalc, String> colCalcPaso;
    @FXML private TableColumn<FilaPasoCalc, String> colCalcFuncion;
    @FXML private TableColumn<FilaPasoCalc, String> colCalcDetalle;
    @FXML private TableColumn<FilaPasoCalc, String> colCalcToken;
    @FXML private TableColumn<FilaPasoCalc, String> colCalcAtributos;
    @FXML private TextArea areaArbolCalc;

    private AlmacenAutomatas almacen;
    private DescRecGramatica analizadorGramatica;
    private AnalizadorLexico lexicoAfd;
    private String nombreAfd;

    private AnalizadorLexico lexicoAfdCalc;
    private String nombreAfdCalc;

    @FXML
    public void initialize() {
        colReglaId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().id()));
        colReglaIzq.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().funcion()));
        colReglaDer.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().der()));
        colReglaFirst.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().first()));

        colPasoNum.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().num()));
        colPasoFuncion.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().funcion()));
        colPasoRegla.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().regla()));
        colPasoToken.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().token()));
        colPasoCadena.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().cadena()));
        colPasoAccion.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().accion()));

        colTermSimbolo.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(analizadorGramatica.getSimbTerm()[c.getValue()].toString()));
        colTermToken.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(String.valueOf(analizadorGramatica.getSimbTerm()[c.getValue()].getToken())));
        colTermToken.setCellFactory(TextFieldTableCell.forTableColumn());
        colTermToken.setOnEditCommit(evento -> onEditarToken(evento.getRowValue(), evento.getNewValue()));

        colCalcPaso.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().num()));
        colCalcFuncion.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().funcion()));
        colCalcDetalle.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().detalle()));
        colCalcToken.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().token()));
        colCalcAtributos.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().atributos()));

        areaGramatica.setText(GRAMATICA_EJEMPLO);
        campoExpresionCalc.setText("(2 + 3) * 4 - 10 / 2");
    }

    @Override
    public void setContext(AppContext contexto) {
        this.almacen = contexto.getAlmacen();
    }

    // =====================================================================================
    //  Gramatica y generacion del Descenso Recursivo
    // =====================================================================================

    @FXML
    private void onGenerar() {
        Gramatica gramatica;
        DescRecGramatica nuevo;
        try {
            gramatica = DescRecGramGram.analizar(areaGramatica.getText());
            nuevo = new DescRecGramatica(gramatica);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Avisos.aviso(e.getMessage());
            etiquetaEstadoAnalizador.setText("Error en la gramática: " + e.getMessage());
            etiquetaEstadoAnalizador.getStyleClass().setAll("texto-error");
            return;
        }
        analizadorGramatica = nuevo;

        mostrarReglasYFirst(gramatica);
        areaPseudocodigo.setText(analizadorGramatica.generarPseudocodigo());
        mostrarTerminales();
        tablaPasos.getItems().clear();
        areaArbolGramatica.clear();
        etiquetaResultado.setText("");

        if (analizadorGramatica.getAdvertencias().isEmpty()) {
            etiquetaEstadoAnalizador.setText("Descenso Recursivo listo: " + gramatica.getNumReglas() + " reglas, "
                    + analizadorGramatica.getSimbNoTerm().length + " funciones recursivas, "
                    + (analizadorGramatica.getSimbTerm().length - 1) + " terminales.");
            etiquetaEstadoAnalizador.getStyleClass().setAll("estado-afd-listo");
            Avisos.info("Descenso Recursivo generado", "Funciones recursivas listas para analizar σ.");
        } else {
            etiquetaEstadoAnalizador.setText("Advertencia (ambigüedad en First): "
                    + String.join(" ", analizadorGramatica.getAdvertencias()));
            etiquetaEstadoAnalizador.getStyleClass().setAll("texto-error");
            Avisos.aviso("La gramática presenta conjuntos First solapados en un mismo no terminal.");
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

    private void mostrarReglasYFirst(Gramatica gramatica) {
        tablaReglas.getItems().clear();
        List<ElemArrReglas> reglas = gramatica.getArrReglas();
        for (int i = 0; i < reglas.size(); i++) {
            ElemArrReglas r = reglas.get(i);
            Set<SimbGram> first = gramatica.First(r.getL());
            String textoFirst = TablaLL1.textoConjunto(first);
            if (Gramatica.contieneEpsilon(first)) {
                textoFirst += "  (si no coincide otro → undoToken)";
            }
            tablaReglas.getItems().add(new FilaReglaFirst(
                    String.valueOf(i),
                    r.getSimbIzqGram() + "()",
                    r.ladoDerechoTexto(),
                    textoFirst
            ));
        }
    }

    private void mostrarTerminales() {
        tablaTerminales.getItems().clear();
        for (int j = 0; j < analizadorGramatica.getSimbTerm().length - 1; j++) {
            tablaTerminales.getItems().add(j);
        }
    }

    private void onEditarToken(int indice, String texto) {
        try {
            analizadorGramatica.setTokenTerminal(indice, Integer.parseInt(texto.trim()));
            areaPseudocodigo.setText(analizadorGramatica.generarPseudocodigo());
        } catch (NumberFormatException e) {
            Avisos.aviso("El token debe ser un número entero.");
        } catch (IllegalArgumentException e) {
            Avisos.aviso(e.getMessage());
        }
        tablaTerminales.refresh();
    }

    // =====================================================================================
    //  Sincronizacion con AFD y Analisis de σ
    // =====================================================================================

    @FXML
    private void onCargarAfd() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Cargar AFD exportado para sincronizar con el Descenso Recursivo");
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
        etiquetaLexico.setText("AFD sincronizado: " + nombreAfd + " (" + lexicoAfd.getNumEstados()
                + " estados). Verifica en la tabla izquierda que los tokens coincidan con tu AFD.");
        etiquetaLexico.getStyleClass().setAll("estado-afd-listo");
        Avisos.info("AFD cargado", "σ se analizará usando el AFD '" + nombreAfd + "'.");
    }

    @FXML
    private void onSincronizarTokens() {
        if (analizadorGramatica == null) {
            Avisos.aviso("Primero genera el descenso recursivo.");
            return;
        }
        // Asigna tokens estandar de calculadora si los simbolos coinciden (+=10, -=20, *=30, /=40, (=50, )=60, num=70)
        // o numera secuencialmente 10, 20, 30...
        Map<String, Integer> mapaCalculadora = Map.of(
                "+", TokensCalculadora.SUMA,
                "-", TokensCalculadora.RESTA,
                "*", TokensCalculadora.PRODUCTO,
                "/", TokensCalculadora.DIVISION,
                "(", TokensCalculadora.PAR_I,
                ")", TokensCalculadora.PAR_D,
                "num", TokensCalculadora.NUM,
                "NUM", TokensCalculadora.NUM
        );

        SimbGram[] terminales = analizadorGramatica.getSimbTerm();
        // Primero asignamos valores temporales negativos para evitar colisiones durante el reacomodo
        for (int i = 0; i < terminales.length - 1; i++) {
            terminales[i].setToken(-100 - i);
        }
        int siguienteLibre = 10;
        for (int i = 0; i < terminales.length - 1; i++) {
            String nombre = terminales[i].getSimbolo();
            if (mapaCalculadora.containsKey(nombre)) {
                terminales[i].setToken(mapaCalculadora.get(nombre));
            } else {
                while (mapaCalculadora.containsValue(siguienteLibre)) {
                    siguienteLibre += 10;
                }
                terminales[i].setToken(siguienteLibre);
                siguienteLibre += 10;
            }
        }
        tablaTerminales.refresh();
        areaPseudocodigo.setText(analizadorGramatica.generarPseudocodigo());
        Avisos.info("Tokens sincronizados", "Se sincronizaron los códigos de token de los terminales.");
    }

    @FXML
    private void onQuitarAfd() {
        lexicoAfd = null;
        nombreAfd = null;
        etiquetaLexico.setText("Sin AFD (Modo crudo): σ se escribe directamente con los terminales (ej. num + num * ( num ))");
        etiquetaLexico.getStyleClass().setAll("estado-afd-pendiente");
    }

    @FXML
    private void onAnalizar() {
        if (analizadorGramatica == null) {
            onGenerar();
            if (analizadorGramatica == null) {
                return;
            }
        }
        String sigma = campoSigma.getText() == null ? "" : campoSigma.getText();
        Lexico lexico = lexicoAfd != null ? lexicoAfd : new LexicoTerminales(analizadorGramatica.getSimbTerm());

        DescRecGramatica.ResultadoAnalisis resultado = analizadorGramatica.analizar(lexico, sigma);

        tablaPasos.getItems().clear();
        for (DescRecGramatica.PasoTraza p : resultado.pasos()) {
            tablaPasos.getItems().add(new FilaPasoGram(
                    String.valueOf(p.num()),
                    p.funcion(),
                    p.reglaAplicada(),
                    p.tokenActual(),
                    p.cadenaRestante(),
                    p.accion()
            ));
        }
        areaArbolGramatica.setText(resultado.arbolLlamadas());

        etiquetaResultado.setText((resultado.aceptada() ? "✔ " : "✘ ") + resultado.mensaje());
        etiquetaResultado.getStyleClass().setAll(resultado.aceptada() ? "estado-afd-listo" : "texto-error");
        if (resultado.aceptada()) {
            Avisos.info("Cadena aceptada", "σ pertenece al lenguaje de la gramática.");
        } else {
            Avisos.aviso("Cadena rechazada: " + resultado.mensaje());
        }
    }

    // =====================================================================================
    //  Pestaña: Calculadora Aritmetica Directa (DescRecCalculadora)
    // =====================================================================================

    @FXML
    private void onCargarAfdCalculadora() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Cargar AFD para la Calculadora (Tokens: +=10, -=20, *=30, /=40, (=50, )=60, NUM=70)");
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tabla de AFD (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        if (almacen != null && almacen.getCarpetaLexico().toFile().isDirectory()) {
            selector.setInitialDirectory(almacen.getCarpetaLexico().toFile());
        }
        File archivo = selector.showOpenDialog(campoExpresionCalc.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        try {
            lexicoAfdCalc = AnalizadorLexico.desdeArchivo(archivo.toPath());
        } catch (IOException | RuntimeException e) {
            Avisos.error("No se pudo cargar '" + archivo.getName() + "': " + e.getMessage());
            return;
        }
        nombreAfdCalc = archivo.getName();
        etiquetaLexicoCalc.setText("AFD sincronizado en Calculadora: " + nombreAfdCalc + " ("
                + lexicoAfdCalc.getNumEstados() + " estados). Tokens esperados: +=10, -=20, *=30, /=40, (=50, )=60, NUM=70");
        etiquetaLexicoCalc.getStyleClass().setAll("estado-afd-listo");
        Avisos.info("AFD cargado en Calculadora", "Se usará '" + nombreAfdCalc + "' para evaluar expresiones.");
    }

    @FXML
    private void onUsarLexicoInternoCalc() {
        lexicoAfdCalc = null;
        nombreAfdCalc = null;
        etiquetaLexicoCalc.setText("Léxico activo: LexicoCalculadora interno (Tokens: +=10, -=20, *=30, /=40, (=50, )=60, NUM=70)");
        etiquetaLexicoCalc.getStyleClass().setAll("estado-afd-pendiente");
    }

    @FXML
    private void onEvaluarCalculadora() {
        String expr = campoExpresionCalc.getText() == null ? "" : campoExpresionCalc.getText().trim();
        if (expr.isEmpty()) {
            Avisos.aviso("Escribe una expresión aritmética para evaluar.");
            return;
        }

        ResultadoCalculadora res;
        if (lexicoAfdCalc != null) {
            lexicoAfdCalc.setSigma(expr);
            DescRecCalculadora calc = new DescRecCalculadora(lexicoAfdCalc);
            double[] v = new double[1];
            String[] post = new String[1];
            boolean ok = calc.IniSintac(v, post);
            if (ok && calc.getError() == null) {
                res = ResultadoCalculadora.exito(v[0], post[0], calc.getPasos(), calc.getArbolTexto());
            } else {
                res = ResultadoCalculadora.error(
                        calc.getError() != null ? calc.getError() : "Sintaxis inválida.",
                        calc.getPasos(), calc.getArbolTexto());
            }
        } else {
            res = DescRecCalculadora.analizar(expr);
        }

        tablaPasosCalc.getItems().clear();
        for (PasoDescenso p : res.pasos()) {
            String indent = "  ".repeat(Math.max(0, p.nivel()));
            tablaPasosCalc.getItems().add(new FilaPasoCalc(
                    String.valueOf(p.paso()),
                    indent + p.funcion() + "()",
                    p.detalle(),
                    p.tokenInfo() != null ? p.tokenInfo() : "-",
                    p.estadoAtributos() != null ? p.estadoAtributos() : ""
            ));
        }
        areaArbolCalc.setText(res.arbolTexto());

        if (res.sintaxisValida()) {
            etiquetaResultadoCalc.setText("✔ Expresión válida");
            etiquetaResultadoCalc.getStyleClass().setAll("estado-afd-listo");
            etiquetaValorCalc.setText("Resultado: " + res.valor());
            etiquetaPostfijoCalc.setText("Postfijo: " + res.postfijo());
            Avisos.info("Expresión evaluada", "Valor = " + res.valor() + "  |  Postfijo: " + res.postfijo());
        } else {
            etiquetaResultadoCalc.setText("✘ " + res.error());
            etiquetaResultadoCalc.getStyleClass().setAll("texto-error");
            etiquetaValorCalc.setText("");
            etiquetaPostfijoCalc.setText("");
            Avisos.aviso(res.error());
        }
    }
}
