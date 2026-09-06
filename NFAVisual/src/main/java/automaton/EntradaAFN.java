package automaton;

public class EntradaAFN {
    private final int id;
    private final String nombre;
    private final AFN afn;
    
    // Constructor
    public EntradaAFN(int id, String nombre, AFN afn){
        this.id = id;
        this.nombre = (nombre == null || nombre.isBlank()) ? ("AFN" + id) : nombre;
        this.afn = afn;
    }
    
    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public AFN getAfn() {
        return afn;
    }
}
