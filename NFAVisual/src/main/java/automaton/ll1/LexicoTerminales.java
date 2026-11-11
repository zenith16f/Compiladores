package automaton.ll1;

import automaton.AnalizadorLexico;
import automaton.Lexico;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Lexico sencillo para probar la tabla LL(1) sin cargar un AFD: en la cadena σ se escriben los
 * terminales tal cual se llaman en la gramatica (ej. "num + num * ( num )" o "num+num*(num)").
 *
 * En cada posicion reconoce el nombre de terminal mas largo que coincida (igual que un AFD que
 * siempre se queda con el lexema mas largo) y regresa el token que ese terminal tiene en la tabla.
 * Los espacios se saltan.
 */
public class LexicoTerminales implements Lexico {

    private final SimbGram[] terminales; // sin el $ del final

    private String sigma = "";
    private int indexCharAct = 0;
    private int indexInicioLex = 0;
    private String yytext = "";
    private final Deque<Integer> historial = new ArrayDeque<>();

    public LexicoTerminales(TablaLL1 tabla) {
        SimbGram[] todos = tabla.getSimbTerm();
        terminales = java.util.Arrays.copyOf(todos, todos.length - 1);
    }

    @Override
    public void setSigma(String sigma) {
        this.sigma = sigma == null ? "" : sigma;
        indexCharAct = 0;
        indexInicioLex = 0;
        yytext = "";
        historial.clear();
    }

    @Override
    public int yylex() {
        historial.push(indexCharAct);
        while (indexCharAct < sigma.length() && Character.isWhitespace(sigma.charAt(indexCharAct))) {
            indexCharAct++;
        }
        indexInicioLex = indexCharAct;
        if (indexCharAct >= sigma.length()) {
            yytext = "";
            return FIN;
        }

        SimbGram mejor = null;
        for (SimbGram t : terminales) {
            if (sigma.startsWith(t.getSimbolo(), indexCharAct)
                    && (mejor == null || t.getSimbolo().length() > mejor.getSimbolo().length())) {
                mejor = t;
            }
        }
        if (mejor == null) {
            indexCharAct++;
            yytext = sigma.substring(indexInicioLex, indexCharAct);
            return AnalizadorLexico.TOKEN_ERROR;
        }
        indexCharAct += mejor.getSimbolo().length();
        yytext = mejor.getSimbolo();
        return mejor.getToken();
    }

    @Override
    public String getYytext() {
        return yytext;
    }

    @Override
    public boolean undoToken() {
        if (historial.isEmpty()) {
            return false;
        }
        indexCharAct = historial.pop();
        indexInicioLex = indexCharAct;
        yytext = "";
        return true;
    }

    @Override
    public int getIndexCharAct() {
        return indexCharAct;
    }

    @Override
    public int getIndexInicioLex() {
        return indexInicioLex;
    }
}
