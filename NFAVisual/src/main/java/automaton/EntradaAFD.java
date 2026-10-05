package automaton;

public class EntradaAFD {
    private final int id;
    private final String nombre;
    private final AFD automata;

    // Constructor
    public EntradaAFD(int id, String nombre, AFD automata) {
        this.id = id;
        this.nombre = nombre;
        this.automata = automata;
    }


    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public AFD getAutomata() {
        return automata;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
