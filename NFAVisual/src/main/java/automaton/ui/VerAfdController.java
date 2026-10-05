package automaton.ui;

import automaton.EntradaAFD;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class VerAfdController implements VistaConContexto {

    @FXML private ComboBox<EntradaAFD> comboAfdVer;
    @FXML private LienzoAfn lienzo;
    @FXML private Label etiquetaVacio;
    @FXML private TableView<TablaTransicionesAfn.Fila> tablaTransiciones;

    @Override
    public void setContext(AppContext contexto) {
        comboAfdVer.setItems(contexto.getGestorAfd().getEntradas());
        comboAfdVer.valueProperty().addListener((obs, anterior, nuevo) -> actualizarVista(nuevo));
        actualizarVista(comboAfdVer.getValue());
    }

    private void actualizarVista(EntradaAFD seleccion) {
        boolean hayAfd = seleccion != null;

        lienzo.dibujarAfd(hayAfd ? seleccion.getAutomata() : null);
        etiquetaVacio.setVisible(!hayAfd);
        TablaTransicionesAfn.poblarAfd(tablaTransiciones, hayAfd ? seleccion.getAutomata() : null);
    }
}
