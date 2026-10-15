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
    @FXML private TextField campoToken;

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

        Integer token;
        try {
            token = CampoToken.leer(campoToken.getText());
        } catch (IllegalArgumentException e) {
            mostrarAviso(e.getMessage());
            return;
        }

        int id = seleccion.getId();
        String nombreOriginal = seleccion.getNombre();
        int idResultado = switch (operacion) {
            case KLEENE -> gestor.CerraduraKleene(id, nombreResultado, token);
            case POSITIVA -> gestor.Positiva(id, nombreResultado, token);
            default -> throw new IllegalStateException("Operación desconocida: " + operacion);
        };
        Avisos.info("Operación realizada", "Se aplicó '" + operacion + "' a '" + nombreOriginal
                + "'. Resultado: '" + gestor.ObtenerEntrada(idResultado).getNombre() + "'.");

        campoNombreResultado.clear();
        campoToken.clear();
    }

    private void mostrarAviso(String mensaje) {
        Avisos.aviso(mensaje);
    }
}
