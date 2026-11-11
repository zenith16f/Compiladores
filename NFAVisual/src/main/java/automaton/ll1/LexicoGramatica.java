package automaton.ll1;

import automaton.AnalizadorLexico;
import automaton.Lexico;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Analizador lexico para la "gramatica de gramaticas" (el texto donde el usuario escribe sus reglas).
 *
 *   Terminal   Clase lexica   e.r.                                          Token
 *   ;          PuntoComa      ';'                                             10
 *   ->         FLECHA         '-' & '>'                                       20
 *   SIMBOLO    SIMBOLO        ([a-z]|[A-Z]) & ([a-z]|[A-Z]|[0-9]|'|_)*        30
 *                             o un solo caracter como + - * / ( )
 *   |          OR             '|'                                             40
 *
 * Los espacios, tabuladores y saltos de linea se saltan. Para usar ';' '|' o '\' como terminal
 * de la gramatica se escriben con '\' antes: \;  \|  \\
 */
public class LexicoGramatica implements Lexico {

    public static final int PC = 10;
    public static final int FLECHA = 20;
    public static final int SIMBOLO = 30;
    public static final int OR = 40;
    public static final int ERROR = AnalizadorLexico.TOKEN_ERROR;

    private String sigma = "";
    private int indexCharAct = 0;
    private int indexInicioLex = 0;
    private String yytext = "";
    private final Deque<Integer> historial = new ArrayDeque<>();

    public LexicoGramatica(String sigma) {
        setSigma(sigma);
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

        // Los espacios no son tokens
        while (indexCharAct < sigma.length() && Character.isWhitespace(sigma.charAt(indexCharAct))) {
            indexCharAct++;
        }
        indexInicioLex = indexCharAct;
        if (indexCharAct >= sigma.length()) {
            yytext = "";
            return FIN;
        }

        char c = sigma.charAt(indexCharAct);
        int token;
        if (c == ';') {
            indexCharAct++;
            token = PC;
        } else if (c == '|') {
            indexCharAct++;
            token = OR;
        } else if (c == '-' && indexCharAct + 1 < sigma.length() && sigma.charAt(indexCharAct + 1) == '>') {
            indexCharAct += 2;
            token = FLECHA;
        } else if (c == '→') { // tambien se acepta la flecha unicode
            indexCharAct++;
            token = FLECHA;
        } else if (Character.isLetter(c) || c == '_') {
            // Identificador: E, E', T', num, SecSimbolos...
            indexCharAct++;
            while (indexCharAct < sigma.length()) {
                char d = sigma.charAt(indexCharAct);
                if (Character.isLetterOrDigit(d) || d == '\'' || d == '_') {
                    indexCharAct++;
                } else {
                    break;
                }
            }
            token = SIMBOLO;
        } else if (c == '\\' && indexCharAct + 1 < sigma.length()
                && !Character.isWhitespace(sigma.charAt(indexCharAct + 1))) {
            // Caracter escapado: \; \| \\ ...
            indexInicioLex = indexCharAct + 1;
            indexCharAct += 2;
            token = SIMBOLO;
        } else {
            // Cualquier otro caracter suelto es un simbolo: + - * / ( ) = < ...
            indexCharAct++;
            token = SIMBOLO;
        }
        yytext = sigma.substring(indexInicioLex, indexCharAct);
        return token;
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
