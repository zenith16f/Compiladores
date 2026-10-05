package automaton.ui;

import automaton.AFD;
import automaton.EntradaAFN;
import automaton.GestorAFD;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;


public class ConvertirAFDController implements VistaConContexto {

    @FXML
    private ComboBox<EntradaAFN> comboAfnOrigen;
    @FXML
    private TextField campoNombreAfd;
    @FXML
    private LienzoAfn lienzo;
    @FXML
    private Label etiquetaVacio;
    @FXML
    private TableView<TablaTransicionesAfn.Fila> tablaTransiciones;

    private GestorAFN gestor;
    private GestorAFD gestorAfd;

    // AFD que se esta mostrando ahora mismo (el que se guarda al presionar "Guardar AFD").
    private AFD afdActual;

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        this.gestorAfd = contexto.getGestorAfd();
        comboAfnOrigen.setItems(gestor.getEntradas());
        comboAfnOrigen.valueProperty().addListener((obs, anterior, nuevo) -> actualizarVista(nuevo));
        actualizarVista(comboAfnOrigen.getValue());
    }

    @FXML
    private void onGuardarAfd() {
        if (afdActual == null) {
            mostrarAviso("Selecciona un AFN para convertirlo antes de guardar.");
            return;
        }

        String nombre = campoNombreAfd.getText();
        if (nombre == null || nombre.isBlank()) {
            EntradaAFN origen = comboAfnOrigen.getValue();
            nombre = "AFD de " + origen.getNombre();
        }

        gestorAfd.Registrar(afdActual, nombre.trim());
        campoNombreAfd.clear();
    }

    private void actualizarVista(EntradaAFN seleccion) {
        afdActual = seleccion == null ? null : seleccion.getAfn().convertir_AFN();

        lienzo.dibujarAfd(afdActual);
        etiquetaVacio.setVisible(afdActual == null);
        TablaTransicionesAfn.poblarAfd(tablaTransiciones, afdActual);
    }

    private void mostrarAviso(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).showAndWait();
    }
}
