package automaton.ui;

import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class CerradurasController implements VistaConContexto {

    private static final String KLEENE = "Cerradura * (Kleene)";
    private static final String POSITIVA = "Cerradura + (Positiva)";

    @FXML private ComboBox<EntradaAFN> comboAfn;
    @FXML private ComboBox<String> comboOperacion;
    @FXML private TextField campoNombreResultado;

    private GestorAFN gestor;

    @FXML
    public void initialize() {
        comboOperacion.getItems().setAll(KLEENE, POSITIVA);
        comboOperacion.getSelectionModel().selectFirst();
    }

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        comboAfn.setItems(gestor.getEntradas());
    }

    @FXML
    private void onRealizarOperacion() {
        EntradaAFN seleccion = comboAfn.getValue();
        String operacion = comboOperacion.getValue();
        String nombreResultado = campoNombreResultado.getText();

        if (seleccion == null || operacion == null) {
            mostrarAviso("Selecciona un AFN y una operación.");
            return;
        }

        int id = seleccion.getId();
        String nombreOriginal = seleccion.getNombre();
        int idResultado = switch (operacion) {
            case KLEENE -> gestor.CerraduraKleene(id, nombreResultado);
            case POSITIVA -> gestor.Positiva(id, nombreResultado);
            default -> throw new IllegalStateException("Operación desconocida: " + operacion);
        };
        Avisos.info("Operación realizada", "Se aplicó '" + operacion + "' a '" + nombreOriginal
                + "'. Resultado: '" + gestor.ObtenerEntrada(idResultado).getNombre() + "'.");

        campoNombreResultado.clear();
    }

    private void mostrarAviso(String mensaje) {
        Avisos.aviso(mensaje);
    }
}
