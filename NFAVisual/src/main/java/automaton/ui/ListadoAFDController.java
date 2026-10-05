package automaton.ui;

import automaton.EntradaAFD;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class ListadoAFDController implements VistaConContexto {

    @FXML private ListView<EntradaAFD> listaAfd;

    @Override
    public void setContext(AppContext contexto) {
        // La lista se llena sola: registrar un AFD en el gestor refresca la ObservableList compartida.
        listaAfd.setItems(contexto.getGestorAfd().getEntradas());
    }
}
