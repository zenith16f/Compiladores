package automaton;

import java.util.ArrayList;

public class Estado {
    private final int idEstado;
    private boolean estadoAccept;
    private ArrayList<Transicion> transiciones;
    private int token;

    public Estado(){
        this.idEstado = GeneradorId.NuevoIdEstado();
        this.estadoAccept = false;
        this.transiciones = new ArrayList<>();
        this.token = -1;
    }

    public Estado(int i){
        this.idEstado = i;
        this.estadoAccept = false;
        this.transiciones = new ArrayList<>();
        this.token = -1;
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
        this.token = GeneradorToken.NuevoTokenEstado();
    }
    public int getToken() {return token;}
    public void setToken(int token) {this.token = token;}

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
