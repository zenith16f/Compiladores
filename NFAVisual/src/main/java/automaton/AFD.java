package automaton;

import java.io.*;
import java.util.*;

public class AFD {
    private ArrayList<Character> alfabeto;
    private ArrayList<Estado> estadosAccept;
    private ArrayList<Estado> estadosAFD;
    private Estado estadoInicial;
    private int numEdos;


    public AFD() {
        this.alfabeto = new ArrayList<>();
        this.estadosAccept = new ArrayList<>();
        this.estadosAFD = new ArrayList<>();
    }

    public AFD(ArrayList<Character> alfabeto,ArrayList<Estado> estadosAccept, ArrayList<Estado> estadosAFD) {
        this.alfabeto = alfabeto;
        this.estadosAccept = estadosAccept;
        this.estadosAFD = estadosAFD;
        this.numEdos = estadosAFD.size();
    }


    public void imprimirAFD() throws IOException {
       File file = new File("AFD.txt");
        if(!file.createNewFile())
            System.out.println("El archivo ya existe");

        try(PrintWriter writer = new PrintWriter(file)){
            for(char c : getAlfabeto()) {
                writer.print(c + " ");
            }
            writer.println();
            for (Estado estado : getEstadosAFD()){
                Map<Character, Integer> map = new HashMap<>();
                for (Transicion t : estado.getTransiciones())
                    map.put(t.getSimboloInferior(),t.getEstadoDestino().getIdEstado());

                for (char c : getAlfabeto())
                    writer.print(map.get(c));

                writer.println();
            }

        }
    }


    public ArrayList<Character> getAlfabeto() {
        return alfabeto;
    }

    public void setAlfabeto(ArrayList<Character> alfabeto) {
        this.alfabeto = alfabeto;
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

    public Estado getEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(Estado estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

}