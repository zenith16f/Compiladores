package automaton.ui;

import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnirController implements VistaConContexto {

    @FXML private ToggleButton toggleUnirDos;
    @FXML private ToggleButton toggleUnirVarios;
    @FXML private VBox panelUnirDos;
    @FXML private VBox panelUnirVarios;

    @FXML private ComboBox<EntradaAFN> comboAfnUno;
    @FXML private ComboBox<EntradaAFN> comboAfnDos;
    @FXML private TextField campoNombreResultadoDos;
    @FXML private TextField campoTokenDos;

    @FXML private TableView<FilaUnion> tablaAfnUnir;
    @FXML private TableColumn<FilaUnion, Boolean> columnaSeleccion;
    @FXML private TableColumn<FilaUnion, String> columnaNombre;
    @FXML private TableColumn<FilaUnion, String> columnaToken;
    @FXML private TextField campoNombreResultadoVarios;

    private GestorAFN gestor;
    // Filas propias (no la lista del gestor) para guardar la marca y el token de cada AFN
    private final ObservableList<FilaUnion> filas = FXCollections.observableArrayList();

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        comboAfnUno.setItems(gestor.getEntradas());
        comboAfnDos.setItems(gestor.getEntradas());
        configurarTablaUnion();
        gestor.getEntradas().addListener((ListChangeListener<EntradaAFN>) cambio -> sincronizarFilas());
        sincronizarFilas();
        actualizarModoVisible();
    }

    private void configurarTablaUnion() {
        tablaAfnUnir.setItems(filas);
        columnaSeleccion.setCellValueFactory(datos -> datos.getValue().seleccionada);
        columnaSeleccion.setCellFactory(CheckBoxTableCell.forTableColumn(columnaSeleccion));
        columnaNombre.setCellValueFactory(datos -> new ReadOnlyStringWrapper(datos.getValue().entrada.getNombre()));
        columnaToken.setCellValueFactory(datos -> datos.getValue().token);
        columnaToken.setCellFactory(columna -> new CeldaToken());
    }

    @FXML
    private void onCambiarModo() {
        actualizarModoVisible();
    }

    private void actualizarModoVisible() {
        boolean modoDos = toggleUnirDos.isSelected();
        panelUnirDos.setVisible(modoDos);
        panelUnirDos.setManaged(modoDos);
        panelUnirVarios.setVisible(!modoDos);
        panelUnirVarios.setManaged(!modoDos);
    }

    @FXML
    private void onUnirDos() {
        EntradaAFN uno = comboAfnUno.getValue();
        EntradaAFN dos = comboAfnDos.getValue();
        String nombreResultado = campoNombreResultadoDos.getText();

        if (uno == null || dos == null) {
            mostrarAviso("Selecciona dos AFN.");
            return;
        }

        Integer token;
        try {
            token = CampoToken.leer(campoTokenDos.getText());
        } catch (IllegalArgumentException e) {
            mostrarAviso(e.getMessage());
            return;
        }

        String nombreUno = uno.getNombre();
        String nombreDos = dos.getNombre();
        int id = gestor.Union(uno.getId(), dos.getId(), nombreResultado, token);
        Avisos.info("Unión realizada", "Se unieron '" + nombreUno + "' y '" + nombreDos
                + "'. Resultado: '" + gestor.ObtenerEntrada(id).getNombre() + "'.");
        comboAfnUno.setValue(null);
        comboAfnDos.setValue(null);
        campoNombreResultadoDos.clear();
        campoTokenDos.clear();
    }

    /**
     * Union especial: un estado inicial nuevo con epsilon hacia cada AFN marcado.
     * Cada AFN conserva sus estados de aceptacion, marcados con el token de su fila.
     */
    @FXML
    private void onUnirVarios() {
        List<FilaUnion> seleccionadas = filas.stream()
                .filter(fila -> fila.seleccionada.get())
                .toList();
        String nombreResultado = campoNombreResultadoVarios.getText();

        if (seleccionadas.size() < 2) {
            mostrarAviso("Marca al menos dos AFN para unir.");
            return;
        }

        List<Integer> ids = new ArrayList<>();
        List<Integer> tokens = new ArrayList<>();
        for (FilaUnion fila : seleccionadas) {
            Integer token = leerToken(fila);
            if (token == null) {
                return;
            }
            ids.add(fila.entrada.getId());
            tokens.add(token);
        }

        int id = gestor.UnionEspecial(ids, tokens, nombreResultado);

        campoNombreResultadoVarios.clear();
        Avisos.info("Unión especial realizada", "Se unieron " + seleccionadas.size() + " AFN. Resultado: '"
                + gestor.ObtenerEntrada(id).getNombre() + "'.");
    }

    // En la union especial cada AFN marcado necesita su token
    private Integer leerToken(FilaUnion fila) {
        try {
            Integer token = CampoToken.leer(fila.token.get());
            if (token == null) {
                mostrarAviso("Escribe un token para '" + fila.entrada.getNombre() + "'.");
            }
            return token;
        } catch (IllegalArgumentException e) {
            mostrarAviso("'" + fila.entrada.getNombre() + "': " + e.getMessage());
            return null;
        }
    }

    // Reconstruye las filas cuando cambia el catalogo, conservando lo que el usuario ya marco o escribio
    private void sincronizarFilas() {
        Map<Integer, FilaUnion> anteriores = new HashMap<>();
        for (FilaUnion fila : filas) {
            anteriores.put(fila.entrada.getId(), fila);
        }
        List<FilaUnion> nuevas = new ArrayList<>();
        for (EntradaAFN entrada : gestor.getEntradas()) {
            FilaUnion anterior = anteriores.get(entrada.getId());
            nuevas.add(anterior != null && anterior.entrada == entrada ? anterior : crearFila(entrada));
        }
        filas.setAll(nuevas);
    }

    private FilaUnion crearFila(EntradaAFN entrada) {
        FilaUnion fila = new FilaUnion(entrada);
        // Al marcar un AFN sin token se le propone el siguiente multiplo de 10 (10, 20, 30...)
        fila.seleccionada.addListener((obs, antes, marcada) -> {
            if (marcada && fila.token.get().isBlank()) {
                fila.token.set(String.valueOf(siguienteTokenSugerido()));
            }
        });
        return fila;
    }

    private int siguienteTokenSugerido() {
        int mayor = 0;
        for (FilaUnion fila : filas) {
            if (!fila.seleccionada.get()) {
                continue;
            }
            try {
                mayor = Math.max(mayor, Integer.parseInt(fila.token.get().trim()));
            } catch (NumberFormatException ignorado) {
                // token vacio o invalido: no cuenta para la sugerencia
            }
        }
        return (mayor / 10 + 1) * 10;
    }

    private void mostrarAviso(String mensaje) {
        Avisos.aviso(mensaje);
    }

    // Celda con un TextField siempre visible (sin tener que dar Enter para confirmar)
    private static class CeldaToken extends TableCell<FilaUnion, String> {
        private final TextField campo = new TextField();
        private FilaUnion filaEnlazada;

        CeldaToken() {
            campo.setPromptText("token");
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(String valor, boolean vacia) {
            super.updateItem(valor, vacia);
            if (filaEnlazada != null) {
                campo.textProperty().unbindBidirectional(filaEnlazada.token);
                campo.disableProperty().unbind();
                filaEnlazada = null;
            }
            if (vacia || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
                return;
            }
            filaEnlazada = getTableView().getItems().get(getIndex());
            campo.textProperty().bindBidirectional(filaEnlazada.token);
            campo.disableProperty().bind(filaEnlazada.seleccionada.not());
            setGraphic(campo);
        }
    }

    private static class FilaUnion {
        private final EntradaAFN entrada;
        private final BooleanProperty seleccionada = new SimpleBooleanProperty(false);
        private final StringProperty token = new SimpleStringProperty("");

        FilaUnion(EntradaAFN entrada) {
            this.entrada = entrada;
        }
    }
}
