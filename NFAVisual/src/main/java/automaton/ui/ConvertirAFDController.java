package automaton.ui;

import automaton.AFD;
import automaton.EntradaAFN;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

/**
 * Controlador de "AFN -> AFD": toma un AFN ya registrado en el catalogo,
 * lo convierte con AFN.convertir_AFN() (construccion de subconjuntos) y
 * dibuja el AFD resultante reutilizando el mismo LienzoAfn/TablaTransicionesAfn
 * que ya se usa en "Ver AFN".
 */
public class ConvertirAFDController implements VistaConContexto {

    @FXML private ComboBox<EntradaAFN> comboAfnOrigen;
    @FXML private LienzoAfn lienzo;
    @FXML private Label etiquetaVacio;
    @FXML private TableView<TablaTransicionesAfn.Fila> tablaTransiciones;

    private GestorAFN gestor;

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        comboAfnOrigen.setItems(gestor.getEntradas());
        comboAfnOrigen.valueProperty().addListener((obs, anterior, nuevo) -> actualizarVista(nuevo));
        actualizarVista(comboAfnOrigen.getValue());
    }

    private void actualizarVista(EntradaAFN seleccion) {
        AFD afd = seleccion == null ? null : seleccion.getAfn().convertir_AFN();

        lienzo.dibujarAfd(afd);
        etiquetaVacio.setVisible(afd == null);
        TablaTransicionesAfn.poblarAfd(tablaTransiciones, afd);
    }
}
