package automaton.ui;

import automaton.AlmacenAutomatas;
import automaton.GestorAFD;
import automaton.GestorAFN;
import javafx.application.Platform;


public class AppContext {
    private final GestorAFN gestor = new GestorAFN();
    private final GestorAFD gestorAfd = new GestorAFD();

    public AppContext() {
        // Carpeta "automatas" junto a donde corre la app: AFN autoguardados, AFD guardados con boton
        AlmacenAutomatas almacen = new AlmacenAutomatas();
        // Los fallos de archivo (nombre invalido, disco, archivo danado) se avisan en pantalla
        gestor.setManejadorErrores(mensaje -> Platform.runLater(
                () -> Avisos.error(mensaje)));
        gestor.conectarAlmacen(almacen);
        gestorAfd.conectarAlmacen(almacen);
    }

    public GestorAFN getGestor() {
        return gestor;
    }

    public GestorAFD getGestorAfd() {
        return gestorAfd;
    }
}
