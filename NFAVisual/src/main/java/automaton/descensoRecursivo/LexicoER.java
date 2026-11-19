package automaton.descensoRecursivo;

import automaton.Lexico;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Analizador lexico para el Descenso Recursivo de Expresiones Regulares (Clase 3-6).
 */
public class LexicoER implements Lexico {

    public static final int FIN = 0;
    public static final int OR = 10;          // |
    public static final int CONCAT = 20;      // &
    public static final int POSITIVA = 30;    // +
    public static final int KLEENE = 40;      // *
    public static final int OPCIONAL = 50;    // ?
    public static final int PAR_I = 60;       // (
    public static final int PAR_D = 70;       // )
    public static final int CORCH_I = 80;     // [
    public static final int CORCH_D = 90;     // ]
    public static final int GUION = 100;      // -
    public static final int SIMBOLO = 110;    // caracter
    public static final int ERROR = -1;

    private String sigma = "";
    private int indexCharAct = 0;
    private int indexInicioLex = 0;
    private String yytext = "";
    private final Deque<Integer> historial = new ArrayDeque<>();

    public LexicoER(String sigma) {
        setSigma(sigma);
    }

    @Override
    public void setSigma(String sigma) {
        this.sigma = sigma == null ? "" : sigma;
        this.indexCharAct = 0;
        this.indexInicioLex = 0;
        this.yytext = "";
        this.historial.clear();
    }

    @Override
    public int yylex() {
        historial.push(indexCharAct);

        // Omitir espacios solo si no estan escapados
        while (indexCharAct < sigma.length() && (sigma.charAt(indexCharAct) == ' ' || sigma.charAt(indexCharAct) == '\t')) {
            indexCharAct++;
        }

        indexInicioLex = indexCharAct;
        if (indexCharAct >= sigma.length()) {
            yytext = "";
            return FIN;
        }

        char c = sigma.charAt(indexCharAct);

        // Caracteres escapados con \
        if (c == '\\') {
            indexCharAct++;
            if (indexCharAct < sigma.length()) {
                char esc = sigma.charAt(indexCharAct);
                indexCharAct++;
                yytext = switch (esc) {
                    case 'n' -> "\n";
                    case 't' -> "\t";
                    case 's' -> " ";
                    default -> String.valueOf(esc);
                };
                return SIMBOLO;
            }
            yytext = "\\";
            return SIMBOLO;
        }

        indexCharAct++;
        yytext = String.valueOf(c);

        return switch (c) {
            case '|' -> OR;
            case '&' -> CONCAT;
            case '+' -> POSITIVA;
            case '*' -> KLEENE;
            case '?' -> OPCIONAL;
            case '(' -> PAR_I;
            case ')' -> PAR_D;
            case '[' -> CORCH_I;
            case ']' -> CORCH_D;
            case '-' -> GUION;
            default -> SIMBOLO;
        };
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
