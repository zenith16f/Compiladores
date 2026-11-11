package automaton.ll1;

import automaton.AnalizadorLexico;
import automaton.Lexico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Clase de la tabla LL(1) (como en el pizarron):
 *
 *   SimbGram[] SimbTerm;     columnas: los terminales (cada uno con su token) y al final '$'
 *   SimbGram[] SimbNoTerm;   renglones: los no terminales
 *   int[][]    Tabla;        Tabla[noTerminal][terminal] = numero de regla a aplicar, -1 = error
 *   Gramatica  GramLL1;      la gramatica con el arreglo de reglas
 *
 * Llenado: para cada regla i  A -> α
 *   - por cada terminal a en First(α):            Tabla[A][a] = i
 *   - si ε esta en First(α), por cada b en Follow(A): Tabla[A][b] = i
 * Si una celda ya tenia otra regla, la gramatica NO es LL(1) (se reporta el conflicto).
 */
public class TablaLL1 {

    public static final int SIN_REGLA = -1;

    private final SimbGram[] SimbTerm;
    private final SimbGram[] SimbNoTerm;
    private final int[][] Tabla;
    private final Gramatica GramLL1;
    private final List<String> conflictos = new ArrayList<>();

    public TablaLL1(Gramatica gramatica) {
        this.GramLL1 = gramatica;

        // Columnas: terminales con un token propuesto 10, 20, 30... y al final $ con token 0 (FIN)
        List<SimbGram> terminales = new ArrayList<>();
        int token = 10;
        for (SimbGram t : gramatica.getConjTerm()) {
            terminales.add(new SimbGram(t.getSimbolo(), token, true));
            token += 10;
        }
        terminales.add(new SimbGram(SimbGram.FIN_CADENA, Lexico.FIN, true));
        SimbTerm = terminales.toArray(new SimbGram[0]);
        SimbNoTerm = gramatica.getConjNoTerm().toArray(new SimbGram[0]);

        Tabla = new int[SimbNoTerm.length][SimbTerm.length];
        for (int[] renglon : Tabla) {
            Arrays.fill(renglon, SIN_REGLA);
        }
        llenar();
    }

    private void llenar() {
        for (int i = 0; i < GramLL1.getNumReglas(); i++) {
            ElemArrReglas regla = GramLL1.getArrReglas().get(i);
            int renglon = indiceNoTerminal(regla.getSimbIzqGram());

            Set<SimbGram> first = GramLL1.First(regla.getL());
            for (SimbGram a : first) {
                if (!a.esEpsilon()) {
                    asignar(renglon, indiceTerminal(a), i);
                }
            }
            if (Gramatica.contieneEpsilon(first)) {
                for (SimbGram b : GramLL1.Follow(regla.getSimbIzqGram())) {
                    asignar(renglon, indiceTerminal(b), i);
                }
            }
        }
    }

    private void asignar(int renglon, int columna, int numRegla) {
        int anterior = Tabla[renglon][columna];
        if (anterior != SIN_REGLA && anterior != numRegla) {
            conflictos.add("Conflicto en [" + SimbNoTerm[renglon] + ", " + SimbTerm[columna] + "]: reglas "
                    + anterior + " y " + numRegla);
            return; // se queda la primera regla
        }
        Tabla[renglon][columna] = numRegla;
    }

    // =====================================================================================
    //  Analisis de una cadena σ con la tabla
    // =====================================================================================

    /** Un renglon de la tabla de analisis: como esta la pila, que falta de la cadena y que se hizo. */
    public record Paso(String pila, String cadena, String accion) {
    }

    public record ResultadoAnalisis(boolean aceptada, List<Paso> pasos, String mensaje) {
    }

    /**
     * Analiza σ con el algoritmo predictivo de la tabla LL(1):
     *
     *   pila = [$, S]          a = yylex()
     *   repetir:
     *     X = tope de la pila
     *     si X = $ y a = $                 -> se acepta la cadena
     *     si X es terminal y su token = a  -> pop y a = yylex()   (se consume el simbolo)
     *     si X es terminal y no coincide   -> error
     *     si X es no terminal:  r = Tabla[X][a]
     *         r = -1  -> error
     *         si no   -> pop y push del lado derecho de la regla r al reves (ε no se mete)
     */
    public ResultadoAnalisis analizar(Lexico lexico, String sigma) {
        List<Paso> pasos = new ArrayList<>();
        lexico.setSigma(sigma);

        Deque<SimbGram> pila = new ArrayDeque<>();
        pila.push(SimbTerm[SimbTerm.length - 1]); // $
        pila.push(GramLL1.getSimboloInicial());

        int token = siguienteToken(lexico);
        int inicioLexema = lexico.getIndexInicioLex();

        for (int iteracion = 0; iteracion < 100_000; iteracion++) {
            String textoPila = textoPila(pila);
            String textoCadena = textoRestante(sigma, inicioLexema, token);
            SimbGram X = pila.peek();
            int columna = columnaDeToken(token);

            if (columna == -1) {
                String lexema = lexico.getYytext();
                String msg = "Error léxico: '" + lexema + "' (token " + token + ") no es ningún terminal de la gramática";
                pasos.add(new Paso(textoPila, textoCadena, msg));
                return new ResultadoAnalisis(false, pasos, msg + ".");
            }

            if (X.getSimbolo().equals(SimbGram.FIN_CADENA) && X.isEsTerminal()) {
                if (token == Lexico.FIN) {
                    pasos.add(new Paso(textoPila, textoCadena, "Aceptar"));
                    return new ResultadoAnalisis(true, pasos, "La cadena es válida: pertenece al lenguaje de la gramática.");
                }
                String msg = "Error: la pila se vació pero sobra '" + lexico.getYytext() + "'";
                pasos.add(new Paso(textoPila, textoCadena, msg));
                return new ResultadoAnalisis(false, pasos, msg + ".");
            }

            if (X.isEsTerminal()) {
                int filaTerminal = indiceTerminal(X);
                if (SimbTerm[filaTerminal].getToken() == token) {
                    pasos.add(new Paso(textoPila, textoCadena, "pop  (se reconoce '" + lexico.getYytext() + "')"));
                    pila.pop();
                    token = siguienteToken(lexico);
                    inicioLexema = lexico.getIndexInicioLex();
                    continue;
                }
                String msg = "Error: se esperaba '" + X + "' y llegó '" + SimbTerm[columna] + "'";
                pasos.add(new Paso(textoPila, textoCadena, msg));
                return new ResultadoAnalisis(false, pasos, msg + ".");
            }

            int numRegla = Tabla[indiceNoTerminal(X)][columna];
            if (numRegla == SIN_REGLA) {
                String msg = "Error: Tabla[" + X + ", " + SimbTerm[columna] + "] está vacía";
                pasos.add(new Paso(textoPila, textoCadena, msg));
                return new ResultadoAnalisis(false, pasos, msg + ".");
            }
            ElemArrReglas regla = GramLL1.getArrReglas().get(numRegla);
            pasos.add(new Paso(textoPila, textoCadena, "Regla " + numRegla + ":  " + regla));
            pila.pop();
            List<SimbGram> ladoDerecho = regla.getL();
            for (int k = ladoDerecho.size() - 1; k >= 0; k--) {
                if (!ladoDerecho.get(k).esEpsilon()) {
                    pila.push(ladoDerecho.get(k));
                }
            }
        }
        return new ResultadoAnalisis(false, pasos, "El análisis se detuvo: demasiados pasos.");
    }

    /**
     * Pide el siguiente token. Si el AFD regresa espacios en blanco que no son ningun terminal,
     * se saltan (asi la cadena puede llevar espacios aunque el AFD no los ignore).
     */
    private int siguienteToken(Lexico lexico) {
        int token = lexico.yylex();
        while (token != Lexico.FIN && columnaDeToken(token) == -1 && lexico.getYytext().isBlank()) {
            token = lexico.yylex();
        }
        return token;
    }

    private int columnaDeToken(int token) {
        for (int j = 0; j < SimbTerm.length; j++) {
            if (SimbTerm[j].getToken() == token) {
                return j;
            }
        }
        return -1;
    }

    private static String textoPila(Deque<SimbGram> pila) {
        // El fondo ($) a la izquierda y el tope a la derecha, como se escribe en el pizarron
        List<String> simbolos = new ArrayList<>();
        Iterator<SimbGram> it = pila.descendingIterator();
        while (it.hasNext()) {
            simbolos.add(it.next().toString());
        }
        return String.join(" ", simbolos);
    }

    private static String textoRestante(String sigma, int desde, int token) {
        if (token == Lexico.FIN || desde >= sigma.length()) {
            return "$";
        }
        return sigma.substring(desde).strip() + " $";
    }

    // =====================================================================================
    //  Consultas para la interfaz
    // =====================================================================================

    /** Cambia el token de un terminal (el que regresa el AFD para esa clase lexica). */
    public void setTokenTerminal(int indice, int token) {
        if (indice < 0 || indice >= SimbTerm.length - 1) {
            throw new IllegalArgumentException("Índice de terminal inválido.");
        }
        if (token == Lexico.FIN || token == AnalizadorLexico.TOKEN_ERROR) {
            throw new IllegalArgumentException("El token " + token + " está reservado.");
        }
        for (int j = 0; j < SimbTerm.length - 1; j++) {
            if (j != indice && SimbTerm[j].getToken() == token) {
                throw new IllegalArgumentException("El token " + token + " ya es de '" + SimbTerm[j] + "'.");
            }
        }
        SimbTerm[indice].setToken(token);
    }

    public int indiceTerminal(SimbGram s) {
        for (int j = 0; j < SimbTerm.length; j++) {
            if (SimbTerm[j].equals(s)) {
                return j;
            }
        }
        throw new IllegalStateException("'" + s + "' no es terminal.");
    }

    public int indiceNoTerminal(SimbGram s) {
        for (int i = 0; i < SimbNoTerm.length; i++) {
            if (SimbNoTerm[i].equals(s)) {
                return i;
            }
        }
        throw new IllegalStateException("'" + s + "' no es no terminal.");
    }

    public static String textoConjunto(Set<SimbGram> conjunto) {
        return "{ " + conjunto.stream().map(SimbGram::toString).collect(Collectors.joining(", ")) + " }";
    }

    public SimbGram[] getSimbTerm() {
        return SimbTerm;
    }

    public SimbGram[] getSimbNoTerm() {
        return SimbNoTerm;
    }

    public int[][] getTabla() {
        return Tabla;
    }

    public Gramatica getGramLL1() {
        return GramLL1;
    }

    public boolean esLL1() {
        return conflictos.isEmpty();
    }

    public List<String> getConflictos() {
        return conflictos;
    }
}
