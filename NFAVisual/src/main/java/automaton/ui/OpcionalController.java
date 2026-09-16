package automaton.ui;

import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class OpcionalController implements VistaConContexto {

    @FXML private ComboBox<EntradaAFN> comboAfn;
    @FXML private TextField campoNombreResultado;

    private GestorAFN gestor;

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        comboAfn.setItems(gestor.getEntradas());
    }

    @FXML
    private void onAplicarOpcional() {
        EntradaAFN seleccion = comboAfn.getValue();
        String nombreResultado = campoNombreResultado.getText();

        if (seleccion == null) {
            mostrarAviso("Selecciona un AFN.");
            return;
        }

        gestor.Optional(seleccion.getId(), nombreResultado);
        comboAfn.setValue(null);
        campoNombreResultado.clear();
    }

    private void mostrarAviso(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).showAndWait();
    }
}
