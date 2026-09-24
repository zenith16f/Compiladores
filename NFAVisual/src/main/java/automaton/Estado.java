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

    public Estado(int i){
        this.idEstado = i;
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
    public void setTransicion(Transicion transicion) {this.transiciones.add(transicion);}

    // Override Methods
    @Override
    public String toString(){
        return "q: " + idEstado + (estadoAccept? "*": "");
    }
}
