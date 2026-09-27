package automaton.ui;

import javafx.fxml.FXML;

/**
 * Controlador "cascaron" de la pestaña grande AFD: no tiene logica
 * propia, solo reparte el AppContext compartido al controlador de la
 * sub-pestaña "AFN -> AFD" (el "Listado" es un placeholder sin
 * controlador todavia, ver ListadoAFD-view.fxml).
 */
public class AfdHubController implements VistaConContexto {

    @FXML private ConvertirAFDController convertirController;

    @Override
    public void setContext(AppContext contexto) {
        convertirController.setContext(contexto);
    }
}
