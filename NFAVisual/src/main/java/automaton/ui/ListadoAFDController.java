package automaton.ui;

import automaton.EntradaAFD;
import automaton.GestorAFD;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ListadoAFDController implements VistaConContexto {

    @FXML private ListView<EntradaAFD> listaAfd;
    @FXML private TextField campoNuevoNombre;

    private GestorAFD gestorAfd;

    @Override
    public void setContext(AppContext contexto) {
        this.gestorAfd = contexto.getGestorAfd();
        // La lista se llena sola: registrar un AFD en el gestor refresca la ObservableList compartida.
        listaAfd.setItems(gestorAfd.getEntradas());
    }

    @FXML
    private void onRenombrar() {
        EntradaAFD seleccion = listaAfd.getSelectionModel().getSelectedItem();
        String nuevoNombre = campoNuevoNombre.getText();

        if (seleccion == null) {
            Avisos.aviso("Selecciona un AFD de la lista para renombrar.");
            return;
        }
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            Avisos.aviso("Escribe el nuevo nombre.");
            return;
        }

        String anterior = seleccion.getNombre();
        try {
            gestorAfd.RenombrarConArchivo(seleccion.getId(), nuevoNombre);
        } catch (IllegalArgumentException e) {
            // Nombre repetido o con caracteres no permitidos
            Avisos.aviso(e.getMessage());
            return;
        } catch (IOException e) {
            Avisos.error("No se pudo renombrar el archivo: " + e.getMessage());
            return;
        }
        campoNuevoNombre.clear();
        Avisos.info("AFD renombrado", "'" + anterior + "' ahora se llama '" + nuevoNombre.trim() + "'.");
    }

    @FXML
    private void onEliminar() {
        EntradaAFD seleccion = listaAfd.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            Avisos.aviso("Selecciona un AFD de la lista para eliminar.");
            return;
        }

        boolean confirmado = Avisos.confirmar(
                "Eliminar AFD",
                "¿Eliminar el AFD '" + seleccion.getNombre() + "'?",
                "Se quitará del listado y se borrará su archivo guardado.\nEsta acción no se puede deshacer.",
                "Eliminar");
        if (!confirmado) {
            return;
        }

        try {
            gestorAfd.EliminarConArchivo(seleccion.getId());
            Avisos.info("AFD eliminado", "Se eliminó el AFD '" + seleccion.getNombre() + "'.");
        } catch (IOException | RuntimeException e) {
            Avisos.error("No se pudo eliminar el archivo: " + e.getMessage());
        }
    }
}
