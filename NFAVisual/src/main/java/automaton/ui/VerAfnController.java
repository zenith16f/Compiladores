package automaton.ui;

import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class VerAfnController implements VistaConContexto {

    @FXML private ComboBox<EntradaAFN> comboAfnVer;
    @FXML private LienzoAfn lienzo;
    @FXML private Label etiquetaVacio;

    private GestorAFN gestor;

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        comboAfnVer.setItems(gestor.getEntradas());
        comboAfnVer.valueProperty().addListener((obs, anterior, nuevo) -> actualizarGrafico(nuevo));
        actualizarGrafico(comboAfnVer.getValue());
    }

    private void actualizarGrafico(EntradaAFN seleccion) {
        if (seleccion == null) {
            lienzo.dibujar(null);
            etiquetaVacio.setVisible(true);
        } else {
            lienzo.dibujar(seleccion.getAfn());
            etiquetaVacio.setVisible(false);
        }
    }
}
