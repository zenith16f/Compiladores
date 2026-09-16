package automaton.ui;

import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ConcatenarController implements VistaConContexto {

    @FXML private ComboBox<EntradaAFN> comboAfnUno;
    @FXML private ComboBox<EntradaAFN> comboAfnDos;
    @FXML private TextField campoNombreResultado;

    private GestorAFN gestor;

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        comboAfnUno.setItems(gestor.getEntradas());
        comboAfnDos.setItems(gestor.getEntradas());
    }

    @FXML
    private void onConcatenar() {
        EntradaAFN uno = comboAfnUno.getValue();
        EntradaAFN dos = comboAfnDos.getValue();
        String nombreResultado = campoNombreResultado.getText();

        if (uno == null || dos == null) {
            mostrarAviso("Selecciona dos AFN.");
            return;
        }

        gestor.Concatenacion(uno.getId(), dos.getId(), nombreResultado);
        comboAfnUno.setValue(null);
        comboAfnDos.setValue(null);
        campoNombreResultado.clear();
    }

    private void mostrarAviso(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).showAndWait();
    }
}
