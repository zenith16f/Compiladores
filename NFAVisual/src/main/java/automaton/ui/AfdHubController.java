package automaton.ui;

import javafx.fxml.FXML;


public class AfdHubController implements VistaConContexto {

    @FXML private ListadoAFDController listadoController;
    @FXML private VerAfdController verAfdController;
    @FXML private ConvertirAFDController convertirController;

    @Override
    public void setContext(AppContext contexto) {
        listadoController.setContext(contexto);
        verAfdController.setContext(contexto);
        convertirController.setContext(contexto);
    }
}
