package automaton.ui;

import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckListView;

import java.util.List;
import java.util.Objects;

public class UnirController implements VistaConContexto {

    @FXML private ToggleButton toggleUnirDos;
    @FXML private ToggleButton toggleUnirVarios;
    @FXML private VBox panelUnirDos;
    @FXML private VBox panelUnirVarios;

    @FXML private ComboBox<EntradaAFN> comboAfnUno;
    @FXML private ComboBox<EntradaAFN> comboAfnDos;
    @FXML private TextField campoNombreResultadoDos;

    @FXML private CheckListView<EntradaAFN> listaAfnUnir;
    @FXML private TextField campoNombreResultadoVarios;

    private GestorAFN gestor;

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        comboAfnUno.setItems(gestor.getEntradas());
        comboAfnDos.setItems(gestor.getEntradas());
        listaAfnUnir.setItems(gestor.getEntradas());
        actualizarModoVisible();
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

        String nombreUno = uno.getNombre();
        String nombreDos = dos.getNombre();
        int id = gestor.Union(uno.getId(), dos.getId(), nombreResultado);
        Avisos.info("Unión realizada", "Se unieron '" + nombreUno + "' y '" + nombreDos
                + "'. Resultado: '" + gestor.ObtenerEntrada(id).getNombre() + "'.");
        comboAfnUno.setValue(null);
        comboAfnDos.setValue(null);
        campoNombreResultadoDos.clear();
    }

    @FXML
    private void onUnirVarios() {
        List<EntradaAFN> seleccionados = listaAfnUnir.getCheckModel().getCheckedItems()
                .stream()
                .filter(Objects::nonNull)
                .toList();
        String nombreResultado = campoNombreResultadoVarios.getText();

        if (seleccionados.size() < 2) {
            mostrarAviso("Marca al menos dos AFN para unir.");
            return;
        }

        // Se limpian los checks ANTES de mutar la lista compartida.
        // gestor.Union() elimina entradas de gestor.getEntradas() (la misma
        // lista que usa este CheckListView), y si el check model todavia
        // tiene posiciones marcadas cuando eso ocurre, su listener interno
        // truena con IndexOutOfBoundsException al intentar reubicar esos
        // indices en la lista ya mas corta.
        listaAfnUnir.getCheckModel().clearChecks();

        int idAcumulado = seleccionados.get(0).getId();
        for (int i = 1; i < seleccionados.size(); i++) {
            idAcumulado = gestor.Union(idAcumulado, seleccionados.get(i).getId(), null);
        }

        if (nombreResultado != null && !nombreResultado.isBlank()) {
            gestor.Renombrar(idAcumulado, nombreResultado);
        }

        campoNombreResultadoVarios.clear();
        Avisos.info("Unión realizada", "Se unieron " + seleccionados.size() + " AFN. Resultado: '"
                + gestor.ObtenerEntrada(idAcumulado).getNombre() + "'.");
    }

    private void mostrarAviso(String mensaje) {
        Avisos.aviso(mensaje);
    }
}
