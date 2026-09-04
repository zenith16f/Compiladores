package automaton;

public class Transicion {
    private char simboloInferior;
    private char simboloSuperior;
    private Estado estadoDestino;

    // Constructors
    public Transicion(char simbolo, Estado estadoDestino) {
        this(simbolo, simbolo, estadoDestino);
    }

    public Transicion(char simboloInferior, char simboloSuperior, Estado estadoDestino) {
        this.simboloInferior = simboloInferior;
        this.simboloSuperior = simboloSuperior;
        this.estadoDestino = estadoDestino;
    }

    public boolean IsEpsilon() {
        return simboloInferior == Simbolo.EPSILON;
    }

    // Getters & Setters
    public char getSimboloInferior() {
        return simboloInferior;
    }

    public void setSimboloInferior(char simboloInferior) {
        this.simboloInferior = simboloInferior;
    }

    public char getSimboloSuperior() {
        return simboloSuperior;
    }

    public void setSimboloSuperior(char simboloSuperior) {
        this.simboloSuperior = simboloSuperior;
    }

    public Estado getEstadoDestino() {
        return estadoDestino;
    }

    public void setEstadoDestino(Estado estadoDestino) {
        this.estadoDestino = estadoDestino;
    }
}
