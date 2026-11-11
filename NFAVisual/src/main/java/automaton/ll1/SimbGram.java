package automaton.ll1;

import java.util.Objects;

/**
 * Un simbolo de la gramatica (clase SimbGram del pizarron).
 *
 *   EsTerminal: true para terminales (y para epsilon), false para no terminales
 *   Token:      el token que el analizador lexico regresa para este terminal
 *   Simbolo:    el nombre del simbolo tal como se escribio en la gramatica ("E'", "+", "num"...)
 *
 * Dos simbolos son iguales si tienen el mismo nombre, asi los conjuntos First/Follow no los repiten.
 */
public class SimbGram {

    public static final String EPSILON = "epsilon";
    public static final String FIN_CADENA = "$";

    private boolean esTerminal;
    private int token;
    private final String simbolo;

    public SimbGram(String simbolo, int token, boolean esTerminal) {
        this.simbolo = simbolo;
        this.token = token;
        this.esTerminal = esTerminal;
    }

    /** Se acepta "epsilon", "Epsilon" o "ε" para la cadena vacia. */
    public static boolean esNombreEpsilon(String nombre) {
        return nombre.equalsIgnoreCase(EPSILON) || nombre.equals("ε");
    }

    public boolean esEpsilon() {
        return simbolo.equals(EPSILON);
    }

    public boolean isEsTerminal() {
        return esTerminal;
    }

    public void setEsTerminal(boolean esTerminal) {
        this.esTerminal = esTerminal;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public String getSimbolo() {
        return simbolo;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SimbGram otro && simbolo.equals(otro.simbolo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simbolo);
    }

    @Override
    public String toString() {
        return esEpsilon() ? "ε" : simbolo;
    }
}
