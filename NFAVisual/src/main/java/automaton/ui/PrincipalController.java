package automaton.ui;

import automaton.AFN;
import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
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

        if (nombre != null && !nombre.isBlank() && gestor.ExisteNombre(nombre)) {
            mostrarAviso("Ya existe un AFN llamado '" + nombre.trim() + "'. Escribe otro nombre.");
            return;
        }

        int id = gestor.Registrar(nuevo, nombre);
        Avisos.info("AFN creado", "Se creó el AFN '" + gestor.ObtenerEntrada(id).getNombre() + "'.");
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
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            mostrarAviso("Escribe el nuevo nombre.");
            return;
        }
        if (gestor.ExisteNombre(nuevoNombre) && !seleccion.getNombre().equalsIgnoreCase(nuevoNombre.trim())) {
            mostrarAviso("Ya existe un AFN llamado '" + nuevoNombre.trim() + "'. Escribe otro nombre.");
            return;
        }
        String anterior = seleccion.getNombre();
        gestor.Renombrar(seleccion.getId(), nuevoNombre.trim());
        campoNuevoNombre.clear();
        Avisos.info("AFN renombrado", "'" + anterior + "' ahora se llama '" + nuevoNombre.trim() + "'.");
    }

    @FXML
    private void onEliminar() {
        EntradaAFN seleccion = listaAfn.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            mostrarAviso("Selecciona un AFN de la lista para eliminar.");
            return;
        }

        boolean confirmado = Avisos.confirmar(
                "Eliminar AFN",
                "¿Eliminar el AFN '" + seleccion.getNombre() + "'?",
                "Se quitará del catálogo y se borrará su archivo guardado.\nEsta acción no se puede deshacer.",
                "Eliminar");
        if (!confirmado) {
            return;
        }
        // El gestor borra tambien el archivo .afn solo (autoguardado)
        gestor.Eliminar(seleccion.getId());
        Avisos.info("AFN eliminado", "Se eliminó el AFN '" + seleccion.getNombre() + "'.");
    }

    private void mostrarAviso(String mensaje) {
        Avisos.aviso(mensaje);
    }
}
