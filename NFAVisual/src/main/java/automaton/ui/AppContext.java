package automaton.ui;

import automaton.GestorAFD;
import automaton.GestorAFN;


public class AppContext {
    private final GestorAFN gestor = new GestorAFN();
    private final GestorAFD gestorAfd = new GestorAFD();

    public GestorAFN getGestor() {
        return gestor;
    }

    public GestorAFD getGestorAfd() {
        return gestorAfd;
    }
}
