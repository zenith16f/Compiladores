package automaton.descensoRecursivo;

import automaton.AFN;
import automaton.Lexico;

/**
 * Analizador Sintactico por Descenso Recursivo para Expresiones Regulares (Clase 3-6).
 * Sintetiza un objeto AFN aplicando la construccion de Thompson.
 *
 *   E  -> T E'
 *   E' -> '|' T E' | ε
 *   T  -> C T'
 *   T' -> '&' C T' | C T' | ε
 *   C  -> F C'
 *   C' -> '+' C' | '*' C' | '?' C' | ε
 *   F  -> '(' E ')' | '[' S '-' S ']' | SIMBOLO
 */
public class DescRecER {

    private final Lexico Lexic;
    private String error;

    public DescRecER(String er) {
        this.Lexic = new LexicoER(er);
    }

    public DescRecER(Lexico lexico) {
        this.Lexic = lexico;
    }

    public static AFN analizar(String er) {
        if (er == null || er.trim().isEmpty()) {
            throw new IllegalArgumentException("La expresion regular no puede estar vacia.");
        }
        DescRecER analizador = new DescRecER(er);
        AFN[] res = new AFN[1];
        if (analizador.IniSintac(res) && res[0] != null) {
            return res[0];
        }
        throw new IllegalArgumentException(analizador.error != null ? analizador.error
                : "Expresion regular mal formada.");
    }

    public boolean IniSintac(AFN[] f) {
        if (E(f)) {
            int token = Lexic.yylex();
            if (token == LexicoER.FIN) {
                return true;
            }
            marcarError("Sobra texto al final de la expresion regular");
        }
        return false;
    }

    // E -> T E'
    public boolean E(AFN[] f) {
        if (T(f)) {
            if (Ep(f)) {
                return true;
            }
        }
        return false;
    }

    // E' -> '|' T E' | ε
    public boolean Ep(AFN[] f) {
        int token = Lexic.yylex();
        if (token == LexicoER.OR) {
            AFN[] f2 = new AFN[1];
            if (T(f2)) {
                f[0].Unir(f2[0]);
                if (Ep(f)) {
                    return true;
                }
            }
            return false;
        }
        Lexic.undoToken();
        return true;
    }

    // T -> C T'
    public boolean T(AFN[] f) {
        if (C(f)) {
            if (Tp(f)) {
                return true;
            }
        }
        return false;
    }

    // T' -> '&' C T' | C T' (concatenacion implicita) | ε
    public boolean Tp(AFN[] f) {
        int token = Lexic.yylex();
        if (token == LexicoER.CONCAT) {
            AFN[] f2 = new AFN[1];
            if (C(f2)) {
                f[0].Concatenar(f2[0]);
                if (Tp(f)) {
                    return true;
                }
            }
            return false;
        }

        // Concatenacion implicita (ej. "ab" o "a(b|c)")
        if (token == LexicoER.SIMBOLO || token == LexicoER.PAR_I || token == LexicoER.CORCH_I) {
            Lexic.undoToken();
            AFN[] f2 = new AFN[1];
            if (C(f2)) {
                f[0].Concatenar(f2[0]);
                if (Tp(f)) {
                    return true;
                }
            }
            return false;
        }

        Lexic.undoToken();
        return true;
    }

    // C -> F C'
    public boolean C(AFN[] f) {
        if (F(f)) {
            if (Cp(f)) {
                return true;
            }
        }
        return false;
    }

    // C' -> '+' C' | '*' C' | '?' C' | ε
    public boolean Cp(AFN[] f) {
        int token = Lexic.yylex();
        switch (token) {
            case LexicoER.POSITIVA:
                f[0].CerraduraPositiva();
                return Cp(f);
            case LexicoER.KLEENE:
                f[0].CerraduraKleene();
                return Cp(f);
            case LexicoER.OPCIONAL:
                f[0].OpOpcional();
                return Cp(f);
            default:
                Lexic.undoToken();
                return true;
        }
    }

    // F -> '(' E ')' | '[' SIMBOLO '-' SIMBOLO ']' | SIMBOLO
    public boolean F(AFN[] f) {
        int token = Lexic.yylex();
        switch (token) {
            case LexicoER.PAR_I:
                if (E(f)) {
                    token = Lexic.yylex();
                    if (token == LexicoER.PAR_D) {
                        return true;
                    }
                    marcarError("Falta parentesis de cierre ')'");
                }
                return false;

            case LexicoER.CORCH_I:
                int tok1 = Lexic.yylex();
                char c1 = Lexic.getYytext().isEmpty() ? ' ' : Lexic.getYytext().charAt(0);
                int tokGuion = Lexic.yylex();
                int tok2 = Lexic.yylex();
                char c2 = Lexic.getYytext().isEmpty() ? ' ' : Lexic.getYytext().charAt(0);
                int tokCierre = Lexic.yylex();

                if (tokGuion == LexicoER.GUION && tokCierre == LexicoER.CORCH_D) {
                    if (c2 < c1) {
                        marcarError("Rango invalido: [" + c1 + "-" + c2 + "]");
                        return false;
                    }
                    f[0] = AFN.CreateAFN(c1, c2);
                    return true;
                }
                marcarError("Se esperaba un rango de la forma [a-z]");
                return false;

            case LexicoER.SIMBOLO:
                char c = Lexic.getYytext().charAt(0);
                f[0] = AFN.CreateAFN(c);
                return true;

            default:
                marcarError("Simbolo inesperado: '" + Lexic.getYytext() + "'");
                return false;
        }
    }

    private void marcarError(String mensaje) {
        if (error == null) {
            int pos = Lexic.getIndexInicioLex();
            error = mensaje + " (posicion " + pos + ").";
        }
    }

    public String getError() {
        return error;
    }
}
