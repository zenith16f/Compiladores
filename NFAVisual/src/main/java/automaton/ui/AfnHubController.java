package automaton.ui;

import javafx.fxml.FXML;

/**
 * Controlador "cascaron" de la pestaña grande AFN: no tiene logica
 * propia, solo reparte el AppContext compartido a los controladores
 * de cada sub-pestaña (Catalogo, Ver AFN, Unir, Concatenar,
 * Cerraduras, Opcional).
 */
public class AfnHubController implements VistaConContexto {

    @FXML private PrincipalController catalogoController;
    @FXML private VerAfnController verAfnController;
    @FXML private UnirController unirController;
    @FXML private ConcatenarController concatenarController;
    @FXML private CerradurasController cerradurasController;
    @FXML private OpcionalController opcionalController;

    @Override
    public void setContext(AppContext contexto) {
        catalogoController.setContext(contexto);
        verAfnController.setContext(contexto);
        unirController.setContext(contexto);
        concatenarController.setContext(contexto);
        cerradurasController.setContext(contexto);
        opcionalController.setContext(contexto);
    }
}
