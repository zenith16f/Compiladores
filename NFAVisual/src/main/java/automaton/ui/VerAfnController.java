package automaton.ui;

import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class VerAfnController implements VistaConContexto {

    @FXML private ComboBox<EntradaAFN> comboAfnVer;
    @FXML private LienzoAfn lienzo;
    @FXML private Label etiquetaVacio;
    @FXML private TableView<TablaTransicionesAfn.Fila> tablaTransiciones;

    private GestorAFN gestor;

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        comboAfnVer.setItems(gestor.getEntradas());
        comboAfnVer.valueProperty().addListener((obs, anterior, nuevo) -> actualizarVista(nuevo));
        actualizarVista(comboAfnVer.getValue());
    }

    private void actualizarVista(EntradaAFN seleccion) {
        boolean hayAfn = seleccion != null;

        lienzo.dibujar(hayAfn ? seleccion.getAfn() : null);
        etiquetaVacio.setVisible(!hayAfn);
        TablaTransicionesAfn.poblar(tablaTransiciones, hayAfn ? seleccion.getAfn() : null);
    }
}
