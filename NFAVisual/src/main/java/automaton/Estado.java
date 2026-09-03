package automaton;

import java.util.ArrayList;

public class Estado {
    private final int idEstado;
    private boolean estadoAccept;
    private ArrayList<Transicion> transiciones;
    
    public Estado(){
        this.idEstado = GeneradorId.NuevoIdEstado();
        this.estadoAccept = false;
        this.transiciones = new ArrayList<>();
    }
    
    // Getters and Setters

    public int getIdEstado() {
        return idEstado;
    }

    public boolean isEstadoAccept() {
        return estadoAccept;
    }

    public void setEstadoAccept(boolean estadoAccept) {
        this.estadoAccept = estadoAccept;
    }

    public ArrayList<Transicion> getTransiciones() {
        return transiciones;
    }

    public void setTransiciones(ArrayList<Transicion> transiciones) {
        this.transiciones = transiciones;
    }
    
    // Override Methods
    @Override
    public String toString(){
        return "q: " + idEstado + (estadoAccept? "*": "");
    }
}
