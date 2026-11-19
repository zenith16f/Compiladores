package automaton.lr0;

import automaton.AnalizadorLexico;
import automaton.Lexico;
import automaton.ll1.ElemArrReglas;
import automaton.ll1.Gramatica;
import automaton.ll1.SimbGram;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TablaLR0 {

    public enum TipoAccion {
        ERROR,
        SHIFT,
        REDUCE,
        ACCEPT
    }

    public record Accion(TipoAccion tipo, int valor) {
        public static final Accion ERROR = new Accion(TipoAccion.ERROR, -1);
        public static final Accion ACCEPT = new Accion(TipoAccion.ACCEPT, 0);

        public static Accion shift(int estado) {
            return new Accion(TipoAccion.SHIFT, estado);
        }

        public static Accion reduce(int numRegla) {
            return new Accion(TipoAccion.REDUCE, numRegla);
        }

        @Override
        public String toString() {
            return switch (tipo) {
                case ERROR -> "";
                case SHIFT -> "d" + valor;
                case REDUCE -> "r" + valor;
                case ACCEPT -> "acc";
            };
        }
    }

    public record Paso(String pila, String cadena, String accion) {
    }

    public record ResultadoAnalisis(boolean aceptada, List<Paso> pasos, String mensaje) {
    }

    public static final int SIN_ESTADO = -1;

    private final ColeccionCanonicaLR0 coleccion;
    private final SimbGram[] simbTerm;
    private final SimbGram[] simbNoTerm;
    private final Accion[][] tablaAction;
    private final int[][] tablaGoto;
    private final List<String> conflictos = new ArrayList<>();

    public TablaLR0(Gramatica gramatica) {
        this(new ColeccionCanonicaLR0(gramatica));
    }

    public TablaLR0(ColeccionCanonicaLR0 coleccion) {
        this.coleccion = coleccion;
        Gramatica g = coleccion.getGramaticaOriginal();

        List<SimbGram> terminales = new ArrayList<>();
        int token = 10;
        for (SimbGram t : g.getConjTerm()) {
            terminales.add(new SimbGram(t.getSimbolo(), token, true));
            token += 10;
        }
        terminales.add(new SimbGram(SimbGram.FIN_CADENA, Lexico.FIN, true));
        this.simbTerm = terminales.toArray(new SimbGram[0]);
        this.simbNoTerm = g.getConjNoTerm().toArray(new SimbGram[0]);

        int numEstados = coleccion.getEstados().size();
        this.tablaAction = new Accion[numEstados][simbTerm.length];
        this.tablaGoto = new int[numEstados][simbNoTerm.length];

        for (int i = 0; i < numEstados; i++) {
            Arrays.fill(tablaAction[i], Accion.ERROR);
            Arrays.fill(tablaGoto[i], SIN_ESTADO);
        }

        llenarTablas();
    }

    private void llenarTablas() {
        List<Set<ItemLR0>> estados = coleccion.getEstados();
        List<Map<SimbGram, Integer>> transiciones = coleccion.getTransiciones();
        int colFin = simbTerm.length - 1;

        for (int i = 0; i < estados.size(); i++) {
            Map<SimbGram, Integer> trans = transiciones.get(i);

            for (Map.Entry<SimbGram, Integer> entry : trans.entrySet()) {
                SimbGram x = entry.getKey();
                int destino = entry.getValue();
                if (x.isEsTerminal()) {
                    int col = indiceTerminal(x);
                    asignarAction(i, col, Accion.shift(destino));
                } else {
                    int col = indiceNoTerminal(x);
                    tablaGoto[i][col] = destino;
                }
            }

            for (ItemLR0 item : estados.get(i)) {
                if (item.puntoAlFinal()) {
                    if (item.getNumRegla() == 0) {
                        asignarAction(i, colFin, Accion.ACCEPT);
                    } else {
                        Accion red = Accion.reduce(item.getNumRegla());
                        for (int col = 0; col < simbTerm.length; col++) {
                            asignarAction(i, col, red);
                        }
                    }
                }
            }
        }
    }

    private void asignarAction(int estado, int colTerm, Accion nueva) {
        Accion actual = tablaAction[estado][colTerm];
        if (actual.tipo() == TipoAccion.ERROR) {
            tablaAction[estado][colTerm] = nueva;
            return;
        }
        if (actual.equals(nueva)) {
            return;
        }
        String tipoConflicto = (actual.tipo() == TipoAccion.REDUCE && nueva.tipo() == TipoAccion.REDUCE)
                ? "Reduce/Reduce"
                : "Shift/Reduce";
        conflictos.add("Conflicto " + tipoConflicto + " en [I" + estado + ", " + simbTerm[colTerm]
                + "]: " + actual + " vs " + nueva);
    }

    public ResultadoAnalisis analizar(Lexico lexico, String sigma) {
        List<Paso> pasos = new ArrayList<>();
        lexico.setSigma(sigma);

        Deque<Integer> pilaEstados = new ArrayDeque<>();
        Deque<String> pilaSimbolos = new ArrayDeque<>();
        pilaEstados.push(0);

        int token = siguienteToken(lexico);
        int inicioLexema = lexico.getIndexInicioLex();
        List<ElemArrReglas> reglas = coleccion.getReglasAumentadas();

        for (int iteracion = 0; iteracion < 100_000; iteracion++) {
            String textoPila = formatearPila(pilaEstados, pilaSimbolos);
            String textoCadena = textoRestante(sigma, inicioLexema, token);
            int estadoActual = pilaEstados.peek();
            int col = columnaDeToken(token);

            if (col == -1) {
                String lexema = lexico.getYytext();
                String msg = "Error léxico: '" + lexema + "' (token " + token + ") no es ningún terminal de la gramática";
                pasos.add(new Paso(textoPila, textoCadena, msg));
                return new ResultadoAnalisis(false, pasos, msg + ".");
            }

            Accion acc = tablaAction[estadoActual][col];
            switch (acc.tipo()) {
                case SHIFT -> {
                    String lexema = lexico.getYytext();
                    pasos.add(new Paso(textoPila, textoCadena,
                            "Shift " + acc.valor() + " ('" + lexema + "')"));
                    pilaSimbolos.push(simbTerm[col].toString());
                    pilaEstados.push(acc.valor());
                    token = siguienteToken(lexico);
                    inicioLexema = lexico.getIndexInicioLex();
                }
                case REDUCE -> {
                    int numRegla = acc.valor();
                    ElemArrReglas regla = reglas.get(numRegla);
                    ItemLR0 aux = new ItemLR0(numRegla, regla, 0);
                    int k = aux.longitudRealLadoDerecho();
                    for (int m = 0; m < k; m++) {
                        pilaEstados.pop();
                        pilaSimbolos.pop();
                    }
                    int estadoTope = pilaEstados.peek();
                    SimbGram A = regla.getSimbIzqGram();
                    int colNoTerm = indiceNoTerminal(A);
                    int destinoGoto = tablaGoto[estadoTope][colNoTerm];
                    if (destinoGoto == SIN_ESTADO) {
                        String msg = "Error: GOTO[I" + estadoTope + ", " + A + "] está vacío";
                        pasos.add(new Paso(textoPila, textoCadena, msg));
                        return new ResultadoAnalisis(false, pasos, msg + ".");
                    }
                    pasos.add(new Paso(textoPila, textoCadena,
                            "Reduce r" + numRegla + " (" + regla + "), GOTO[I" + estadoTope + ", " + A + "] = " + destinoGoto));
                    pilaSimbolos.push(A.toString());
                    pilaEstados.push(destinoGoto);
                }
                case ACCEPT -> {
                    pasos.add(new Paso(textoPila, textoCadena, "Aceptar"));
                    return new ResultadoAnalisis(true, pasos, "La cadena es válida: pertenece al lenguaje de la gramática.");
                }
                case ERROR -> {
                    String msg = "Error sintáctico: ACTION[I" + estadoActual + ", " + simbTerm[col] + "] está vacía";
                    pasos.add(new Paso(textoPila, textoCadena, msg));
                    return new ResultadoAnalisis(false, pasos, msg + ".");
                }
            }
        }
        return new ResultadoAnalisis(false, pasos, "El análisis se detuvo: demasiados pasos.");
    }

    private int siguienteToken(Lexico lexico) {
        int token = lexico.yylex();
        while (token != Lexico.FIN && columnaDeToken(token) == -1 && lexico.getYytext().isBlank()) {
            token = lexico.yylex();
        }
        return token;
    }

    private int columnaDeToken(int token) {
        for (int j = 0; j < simbTerm.length; j++) {
            if (simbTerm[j].getToken() == token) {
                return j;
            }
        }
        return -1;
    }

    private static String formatearPila(Deque<Integer> estados, Deque<String> simbolos) {
        List<String> elems = new ArrayList<>();
        Iterator<Integer> itEdo = estados.descendingIterator();
        Iterator<String> itSimb = simbolos.descendingIterator();
        if (itEdo.hasNext()) {
            elems.add(String.valueOf(itEdo.next()));
        }
        while (itSimb.hasNext() && itEdo.hasNext()) {
            elems.add(itSimb.next());
            elems.add(String.valueOf(itEdo.next()));
        }
        return String.join(" ", elems);
    }

    private static String textoRestante(String sigma, int desde, int token) {
        if (token == Lexico.FIN || desde >= sigma.length()) {
            return "$";
        }
        return sigma.substring(desde).strip() + " $";
    }

    public void setTokenTerminal(int indice, int token) {
        if (indice < 0 || indice >= simbTerm.length - 1) {
            throw new IllegalArgumentException("Índice de terminal inválido.");
        }
        if (token == Lexico.FIN || token == AnalizadorLexico.TOKEN_ERROR) {
            throw new IllegalArgumentException("El token " + token + " está reservado.");
        }
        for (int j = 0; j < simbTerm.length - 1; j++) {
            if (j != indice && simbTerm[j].getToken() == token) {
                throw new IllegalArgumentException("El token " + token + " ya es de '" + simbTerm[j] + "'.");
            }
        }
        simbTerm[indice].setToken(token);
    }

    public int indiceTerminal(SimbGram s) {
        for (int j = 0; j < simbTerm.length; j++) {
            if (simbTerm[j].equals(s)) {
                return j;
            }
        }
        throw new IllegalStateException("'" + s + "' no es terminal.");
    }

    public int indiceNoTerminal(SimbGram s) {
        for (int i = 0; i < simbNoTerm.length; i++) {
            if (simbNoTerm[i].equals(s)) {
                return i;
            }
        }
        throw new IllegalStateException("'" + s + "' no es no terminal.");
    }

    public ColeccionCanonicaLR0 getColeccion() {
        return coleccion;
    }

    public SimbGram[] getSimbTerm() {
        return simbTerm;
    }

    public SimbGram[] getSimbNoTerm() {
        return simbNoTerm;
    }

    public Accion[][] getTablaAction() {
        return tablaAction;
    }

    public int[][] getTablaGoto() {
        return tablaGoto;
    }

    public boolean esLR0() {
        return conflictos.isEmpty();
    }

    public List<String> getConflictos() {
        return conflictos;
    }
}
