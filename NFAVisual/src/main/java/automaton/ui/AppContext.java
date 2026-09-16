package automaton.ui;

import automaton.GestorAFN;


public class AppContext {
    private final GestorAFN gestor = new GestorAFN();

    public GestorAFN getGestor() {
        return gestor;
    }
}
