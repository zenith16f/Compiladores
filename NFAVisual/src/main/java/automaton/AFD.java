package automaton;

import java.util.ArrayList;

public class AFD {
    private ArrayList<Character> alfabeto;
    private Estado estadoInicial;
    private ArrayList<Estado> estadosAccept;
    private ArrayList<Estado> estadosAFD;
    private int numEdos;


    public AFD (){
        this.alfabeto = new ArrayList<>();
        this.estadosAccept = new ArrayList<>();
        this.estadosAFD= new ArrayList<>();
    }

    public AFD(ArrayList<Character> alfabeto, Estado estadoInicial,ArrayList<Estado> estadosAccept,ArrayList<Estado> estadosAFD ){
        this.alfabeto = alfabeto;
        this.estadoInicial = estadoInicial;
        this.estadosAccept = estadosAccept;
        this.estadosAFD =  estadosAFD;
        this.numEdos = estadosAFD.size();
    }


    public ArrayList<Character> getAlfabeto() {
        return alfabeto;
    }

    public void setAlfabeto(ArrayList<Character> alfabeto) {
        this.alfabeto = alfabeto;
    }

    public Estado getEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(Estado estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    public ArrayList<Estado> getEstadosAccept() {
        return estadosAccept;
    }

    public void setEstadosAccept(ArrayList<Estado> estadosAccept) {
        this.estadosAccept = estadosAccept;
    }

    public ArrayList<Estado> getEstadosAFD() {
        return estadosAFD;
    }

    public void setEstadosAFD(ArrayList<Estado> estadosAFD) {
        this.estadosAFD = estadosAFD;
    }

    public int getNumEdos() {
        return numEdos;
    }

    public void setNumEdos(int numEdos) {
        this.numEdos = numEdos;
    }
}
