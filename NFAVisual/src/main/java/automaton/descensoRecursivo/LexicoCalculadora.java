package automaton.descensoRecursivo;

import automaton.Lexico;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Analizador lexico para la calculadora aritmetica por descenso recursivo.
 * Implementa la interfaz Lexico compartida del proyecto.
 */
public class LexicoCalculadora implements Lexico {

    private String sigma = "";
    private int indexCharAct = 0;
    private int indexInicioLex = 0;
    private String yytext = "";
    private final Deque<Integer> historial = new ArrayDeque<>();

    public LexicoCalculadora(String sigma) {
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

        // Omitir espacios en blanco
        while (indexCharAct < sigma.length() && Character.isWhitespace(sigma.charAt(indexCharAct))) {
            indexCharAct++;
        }

        indexInicioLex = indexCharAct;
        if (indexCharAct >= sigma.length()) {
            yytext = "";
            return TokensCalculadora.FIN;
        }

        char c = sigma.charAt(indexCharAct);

        // Operadores de 1 solo caracter
        switch (c) {
            case '+':
                indexCharAct++;
                yytext = "+";
                return TokensCalculadora.SUMA;
            case '-':
                indexCharAct++;
                yytext = "-";
                return TokensCalculadora.RESTA;
            case '*':
                indexCharAct++;
                yytext = "*";
                return TokensCalculadora.PRODUCTO;
            case '/':
                indexCharAct++;
                yytext = "/";
                return TokensCalculadora.DIVISION;
            case '(':
                indexCharAct++;
                yytext = "(";
                return TokensCalculadora.PAR_I;
            case ')':
                indexCharAct++;
                yytext = ")";
                return TokensCalculadora.PAR_D;
            default:
                break;
        }

        // Numeros: [0-9]+(\.[0-9]+)?
        if (Character.isDigit(c)) {
            int ini = indexCharAct;
            while (indexCharAct < sigma.length() && Character.isDigit(sigma.charAt(indexCharAct))) {
                indexCharAct++;
            }
            if (indexCharAct < sigma.length() && sigma.charAt(indexCharAct) == '.') {
                if (indexCharAct + 1 < sigma.length() && Character.isDigit(sigma.charAt(indexCharAct + 1))) {
                    indexCharAct++; // punto
                    while (indexCharAct < sigma.length() && Character.isDigit(sigma.charAt(indexCharAct))) {
                        indexCharAct++;
                    }
                }
            }
            yytext = sigma.substring(ini, indexCharAct);
            return TokensCalculadora.NUM;
        }

        // Caracter desconocido
        indexCharAct++;
        yytext = String.valueOf(c);
        return TokensCalculadora.ERROR;
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

    public String getSigma() {
        return sigma;
    }
}
