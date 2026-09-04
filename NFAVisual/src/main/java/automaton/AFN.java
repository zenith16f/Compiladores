package automaton;

import java.util.ArrayList;

public class AFN {
    private ArrayList<Character> alfabeto;
    private Estado estadoInicial;
    private ArrayList<Estado> estadosAccept;
    private ArrayList<Estado> estadosAFN;

    // Constructor
    public AFN() {
        this.alfabeto = new ArrayList<>();
        this.estadosAccept = new ArrayList<>();
        this.estadosAFN = new ArrayList<>();
    }

    // Method's
    public static AFN CreateAFN(char character) {
        return CreateAFN(character, character);
    }

    public static AFN CreateAFN(char simboloInferior, char simboloSuperior) {

        AFN afn = new AFN();
        Estado estadoUno = new Estado();
        Estado estadoDos = new Estado();

        Transicion transicion = new Transicion(simboloInferior, simboloSuperior, estadoDos);
        estadoUno.getTransiciones().add(transicion);
        estadoDos.setEstadoAccept(true);

        afn.estadoInicial = estadoUno;
        afn.estadosAccept.add(estadoDos);
        afn.estadosAFN.add(estadoUno);
        afn.estadosAFN.add(estadoDos);

        for (char a = simboloInferior; a <= simboloSuperior; a++) {
            afn.alfabeto.add(a);
        }

        return afn;
    }

    // Operations
    public AFN Unir(AFN afn2) {
        Estado estadoUno = new Estado();
        Estado estadoDos = new Estado();

        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, this.estadoInicial));
        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, afn2.estadoInicial));

        for (Estado estado : this.estadosAccept) {
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
            estado.setEstadoAccept(false);
        }

        for (Estado estado : afn2.estadosAccept) {
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
            estado.setEstadoAccept(false);
        }

        estadoDos.setEstadoAccept(true);
        ListUtils.Union(this.estadosAFN, afn2.estadosAFN);
        this.estadosAFN.add(estadoUno);
        this.estadosAFN.add(estadoDos);
        this.estadosAccept.clear();
        this.estadosAccept.add(estadoDos);
        this.estadoInicial = estadoUno;
        ListUtils.Union(this.alfabeto, afn2.alfabeto);

        return this;
    }

    public AFN Concatenar(AFN afn2) {
        for (Estado estado : this.estadosAccept) {
            for (Transicion transicion : afn2.estadoInicial.getTransiciones()) {
                estado.getTransiciones().add(transicion);
            }
            estado.setEstadoAccept(false);
        }

        ListUtils.Union(this.estadosAFN, afn2.estadosAFN);
        this.estadosAFN.remove(afn2.estadoInicial);
        this.estadosAccept.clear();
        ListUtils.Union(this.estadosAccept, afn2.estadosAccept);
        ListUtils.Union(this.alfabeto, afn2.alfabeto);

        return this;
    }

    public AFN CerraduraPositiva() {
        Estado estadoUno = new Estado();
        Estado estadoDos = new Estado();
        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, this.estadoInicial));

        for (Estado estado : this.estadosAccept) {
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, this.estadoInicial));
            estado.setEstadoAccept(false);
        }

        this.estadosAFN.add(estadoUno);
        this.estadosAFN.add(estadoDos);
        this.setEstadoInicial(estadoUno);
        this.estadosAccept.clear();
        this.estadosAccept.add(estadoDos);

        return this;
    }

    public AFN CerraduraKleene() {
        this.CerraduraPositiva();

        for (Estado estado : this.estadosAccept) {
            this.estadoInicial.getTransiciones().add(new Transicion(Simbolo.EPSILON, estado));
        }

        return this;
    }

    public AFN OpOpcional() {
        Estado estadoUno = new Estado();
        Estado estadoDos = new Estado();

        estadoDos.setEstadoAccept(true);
        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, this.estadoInicial));

        for (Estado estado : this.estadosAccept) {
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
            estado.setEstadoAccept(false);
        }

        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
        this.estadosAFN.add(estadoUno);
        this.estadosAFN.add(estadoDos);
        this.setEstadoInicial(estadoUno);
        this.estadosAccept.clear();
        this.estadosAccept.add(estadoDos);

        return this;
    }

    // Getters and Setters
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

    public ArrayList<Estado> getEstadosAFN() {
        return estadosAFN;
    }

    public void setEstadosAFN(ArrayList<Estado> estadosAFN) {
        this.estadosAFN = estadosAFN;
    }
}
