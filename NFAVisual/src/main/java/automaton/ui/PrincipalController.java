package automaton.ui;

import automaton.AFN;
import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class PrincipalController implements VistaConContexto {

    @FXML private ListView<EntradaAFN> listaAfn;
    @FXML private TextField campoNuevoNombre;
    @FXML private TextField campoNombreNuevo;
    @FXML private TextField campoSimboloInferior;
    @FXML private TextField campoSimboloSuperior;

    private GestorAFN gestor;

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();

        // La lista se llena sola: registrar/eliminar/renombrar en el gestor
        // refresca la ObservableList compartida sin sincronizacion manual.
        listaAfn.setItems(gestor.getEntradas());
    }

    @FXML
    private void onCrearAfn() {
        String nombre = campoNombreNuevo.getText();
        String textoInferior = campoSimboloInferior.getText();
        String textoSuperior = campoSimboloSuperior.getText();

        if (textoInferior == null || textoInferior.isBlank()) {
            mostrarAviso("Escribe al menos un símbolo (ej. 'a').");
            return;
        }

        char inferior = textoInferior.trim().charAt(0);
        AFN nuevo;

        if (textoSuperior == null || textoSuperior.isBlank()) {
            nuevo = AFN.CreateAFN(inferior);
        } else {
            char superior = textoSuperior.trim().charAt(0);
            if (superior < inferior) {
                mostrarAviso("El símbolo 'hasta' debe ser mayor o igual al primero.");
                return;
            }
            nuevo = AFN.CreateAFN(inferior, superior);
        }

        gestor.Registrar(nuevo, nombre);
        campoNombreNuevo.clear();
        campoSimboloInferior.clear();
        campoSimboloSuperior.clear();
    }

    @FXML
    private void onRenombrar() {
        EntradaAFN seleccion = listaAfn.getSelectionModel().getSelectedItem();
        String nuevoNombre = campoNuevoNombre.getText();

        if (seleccion == null) {
            mostrarAviso("Selecciona un AFN de la lista para renombrar.");
            return;
        }
        gestor.Renombrar(seleccion.getId(), nuevoNombre);
        campoNuevoNombre.clear();
    }

    private void mostrarAviso(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).showAndWait();
    }
}
