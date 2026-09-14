package automaton.ui;

import automaton.AFN;
import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class PrincipalController {

    @FXML private ComboBox<EntradaAFN> comboAfnUno;
    @FXML private ComboBox<EntradaAFN> comboAfnDos;
    @FXML private ListView<EntradaAFN> listaAfn;
    @FXML private TextField campoNuevoNombre;
    @FXML private TextField campoNombreNuevo;
    @FXML private TextField campoSimboloInferior;
    @FXML private TextField campoSimboloSuperior;

    private final GestorAFN gestor = new GestorAFN();

    @FXML
    public void initialize() {
        // Las tres vistas (2 combobox + la lista) comparten la misma
        // ObservableList del gestor: registrar/eliminar en el gestor
        // refresca las tres solas.
        comboAfnUno.setItems(gestor.getEntradas());
        comboAfnDos.setItems(gestor.getEntradas());
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
    private void onUnir() {
        operarBinaria(gestor::Union);
    }

    @FXML
    private void onConcatenar() {
        operarBinaria(gestor::Concatenacion);
    }

    @FXML
    private void onCerraduraKleene() {
        operarUnaria(gestor::CerraduraKleene);
    }

    @FXML
    private void onCerraduraPositiva() {
        operarUnaria(gestor::Positiva);
    }

    @FXML
    private void onOpOpcional() {
        operarUnaria(gestor::Optional);
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

    // ---------- Helpers para no repetir la validación de selección ----------

    private interface OperacionBinaria {
        int aplicar(int id1, int id2, String nombre);
    }

    private interface OperacionUnaria {
        int aplicar(int id, String nombre);
    }

    private void operarBinaria(OperacionBinaria operacion) {
        EntradaAFN uno = comboAfnUno.getValue();
        EntradaAFN dos = comboAfnDos.getValue();

        if (uno == null || dos == null) {
            mostrarAviso("Selecciona dos AFN.");
            return;
        }
        operacion.aplicar(uno.getId(), dos.getId(), null);
        comboAfnUno.setValue(null);
        comboAfnDos.setValue(null);
    }

    private void operarUnaria(OperacionUnaria operacion) {
        EntradaAFN seleccion = listaAfn.getSelectionModel().getSelectedItem();

        if (seleccion == null) {
            mostrarAviso("Selecciona un AFN de la lista.");
            return;
        }
        operacion.aplicar(seleccion.getId(), null);
    }

    private void mostrarAviso(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).showAndWait();
    }
}
