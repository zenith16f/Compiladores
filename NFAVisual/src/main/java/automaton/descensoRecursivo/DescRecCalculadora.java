package automaton.descensoRecursivo;

import automaton.Lexico;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador Sintactico por Descenso Recursivo para Expresiones Aritmeticas.
 *
 * Gramatica G' sin recursion por la izquierda (del apunte y pizarron):
 *
 *   E  -> T E'
 *   E' -> + T E' | - T E' | ε
 *   T  -> F T'
 *   T' -> * F T' | / F T' | ε
 *   F  -> ( E ) | Num
 *
 * Atributos semanticos:
 *   - v: Atributo sintetizado/acumulado para evaluar el resultado numerico.
 *   - post: Atributo sintetizado/acumulado para la cadena en notacion postfija.
 */
public class DescRecCalculadora {

    private final Lexico Lexic;
    private String error;
    private final List<PasoDescenso> pasos = new ArrayList<>();
    private final StringBuilder arbolTexto = new StringBuilder();
    private int contadorPasos = 0;
    private int nivelActual = 0;

    public DescRecCalculadora(String expresion) {
        this.Lexic = new LexicoCalculadora(expresion);
    }

    public DescRecCalculadora(Lexico lexico) {
        this.Lexic = lexico;
    }

    /**
     * Analiza y evalua la expresion aritmetica dada.
     */
    public static ResultadoCalculadora analizar(String expresion) {
        if (expresion == null || expresion.trim().isEmpty()) {
            return ResultadoCalculadora.error("La expresion esta vacia.", List.of(), "");
        }
        DescRecCalculadora analizador = new DescRecCalculadora(expresion);
        double[] v = new double[1];
        String[] post = new String[1];

        boolean exito = analizador.IniSintac(v, post);
        if (exito && analizador.error == null) {
            return ResultadoCalculadora.exito(v[0], post[0], analizador.pasos, analizador.arbolTexto.toString());
        } else {
            String msg = analizador.error != null ? analizador.error : "Sintaxis invalida en la expresion.";
            return ResultadoCalculadora.error(msg, analizador.pasos, analizador.arbolTexto.toString());
        }
    }

    // ==========================================
    // Metodos del Descenso Recursivo
    // ==========================================

    /**
     * Para iniciar el proceso de analisis sintactico se hace el llamado
     * a la funcion que corresponde al simbolo inicial de G' (IniSintac).
     */
    public boolean IniSintac(double[] v, String[] post) {
        registrarPaso("IniSintac", "Inicia analisis sintactico", null, null);
        entrar("IniSintac");

        if (E(v, post)) {
            int token = Lexic.yylex();
            registrarPaso("IniSintac", "Verifica fin de cadena", TokensCalculadora.nombreToken(token),
                    "v=" + v[0] + ", post='" + post[0] + "'");
            if (token == TokensCalculadora.FIN) {
                salir("IniSintac", true, "v=" + v[0] + ", post='" + post[0] + "'");
                return true;
            } else {
                marcarError("Sobra texto o caracteres no reconocidos despues de la expresion: '" + Lexic.getYytext() + "'");
            }
        } else {
            if (error == null) {
                marcarError("Expresion aritmetica mal formada");
            }
        }

        salir("IniSintac", false, null);
        return false;
    }

    /**
     * E -> T E'
     */
    public boolean E(double[] v, String[] post) {
        registrarPaso("E", "Llama a T()", null, null);
        entrar("E");

        if (T(v, post)) {
            registrarPaso("E", "T() fue exitoso, llama a Ep()", null, "v=" + v[0] + ", post='" + post[0] + "'");
            if (Ep(v, post)) {
                salir("E", true, "v=" + v[0] + ", post='" + post[0] + "'");
                return true;
            }
        }

        salir("E", false, null);
        return false;
    }

    /**
     * E' -> + T E' | - T E' | ε
     */
    public boolean Ep(double[] v, String[] post) {
        registrarPaso("Ep", "Lee token para +/- o ε", null, "v=" + v[0]);
        entrar("Ep");

        int token = Lexic.yylex();
        registrarPaso("Ep", "Token obtenido", TokensCalculadora.nombreToken(token), "yytext='" + Lexic.getYytext() + "'");

        if (token == TokensCalculadora.SUMA || token == TokensCalculadora.RESTA) {
            double[] v2 = new double[1];
            String[] post2 = new String[1];

            if (T(v2, post2)) {
                // Acciones semanticas heredadas/sintetizadas
                if (token == TokensCalculadora.SUMA) {
                    v[0] = v[0] + v2[0];
                    post[0] = post[0] + " " + post2[0] + " +";
                } else {
                    v[0] = v[0] - v2[0];
                    post[0] = post[0] + " " + post2[0] + " -";
                }

                registrarPaso("Ep", "Calculo operador " + (token == TokensCalculadora.SUMA ? "+" : "-"),
                        null, "nuevo v=" + v[0] + ", post='" + post[0] + "'");

                if (Ep(v, post)) {
                    salir("Ep", true, "v=" + v[0] + ", post='" + post[0] + "'");
                    return true;
                }
            }
            salir("Ep", false, null);
            return false;
        }

        // Caso ε: no es '+' ni '-', deshace la lectura del token y termina exitoso
        Lexic.undoToken();
        registrarPaso("Ep", "UndoToken (caso ε)", TokensCalculadora.nombreToken(token), "v=" + v[0]);
        salir("Ep", true, "ε (v=" + v[0] + ", post='" + post[0] + "')");
        return true;
    }

    /**
     * T -> F T'
     */
    public boolean T(double[] v, String[] post) {
        registrarPaso("T", "Llama a F()", null, null);
        entrar("T");

        if (F(v, post)) {
            registrarPaso("T", "F() fue exitoso, llama a Tp()", null, "v=" + v[0] + ", post='" + post[0] + "'");
            if (Tp(v, post)) {
                salir("T", true, "v=" + v[0] + ", post='" + post[0] + "'");
                return true;
            }
        }

        salir("T", false, null);
        return false;
    }

    /**
     * T' -> * F T' | / F T' | ε
     */
    public boolean Tp(double[] v, String[] post) {
        registrarPaso("Tp", "Lee token para * / o ε", null, "v=" + v[0]);
        entrar("Tp");

        int token = Lexic.yylex();
        registrarPaso("Tp", "Token obtenido", TokensCalculadora.nombreToken(token), "yytext='" + Lexic.getYytext() + "'");

        if (token == TokensCalculadora.PRODUCTO || token == TokensCalculadora.DIVISION) {
            double[] v2 = new double[1];
            String[] post2 = new String[1];

            if (F(v2, post2)) {
                if (token == TokensCalculadora.PRODUCTO) {
                    v[0] = v[0] * v2[0];
                    post[0] = post[0] + " " + post2[0] + " *";
                } else {
                    if (v2[0] == 0.0) {
                        marcarError("Error aritmetico: division entre cero.");
                        salir("Tp", false, "Division entre cero");
                        return false;
                    }
                    v[0] = v[0] / v2[0];
                    post[0] = post[0] + " " + post2[0] + " /";
                }

                registrarPaso("Tp", "Calculo operador " + (token == TokensCalculadora.PRODUCTO ? "*" : "/"),
                        null, "nuevo v=" + v[0] + ", post='" + post[0] + "'");

                if (Tp(v, post)) {
                    salir("Tp", true, "v=" + v[0] + ", post='" + post[0] + "'");
                    return true;
                }
            }
            salir("Tp", false, null);
            return false;
        }

        // Caso ε: no es '*' ni '/', se deshace el token
        Lexic.undoToken();
        registrarPaso("Tp", "UndoToken (caso ε)", TokensCalculadora.nombreToken(token), "v=" + v[0]);
        salir("Tp", true, "ε (v=" + v[0] + ", post='" + post[0] + "')");
        return true;
    }

    /**
     * F -> ( E ) | Num
     */
    public boolean F(double[] v, String[] post) {
        registrarPaso("F", "Lee token para '(' o NUM", null, null);
        entrar("F");

        int token = Lexic.yylex();
        registrarPaso("F", "Token obtenido", TokensCalculadora.nombreToken(token), "yytext='" + Lexic.getYytext() + "'");

        switch (token) {
            case TokensCalculadora.PAR_I: // '('
                if (E(v, post)) {
                    token = Lexic.yylex();
                    registrarPaso("F", "Espera ')'", TokensCalculadora.nombreToken(token), "yytext='" + Lexic.getYytext() + "'");
                    if (token == TokensCalculadora.PAR_D) { // ')'
                        salir("F", true, "(E) -> v=" + v[0] + ", post='" + post[0] + "'");
                        return true;
                    } else {
                        marcarError("Se esperaba parentesis de cierre ')' pero se encontro '" + Lexic.getYytext() + "'");
                    }
                }
                break;

            case TokensCalculadora.NUM: // Num
                try {
                    v[0] = Double.parseDouble(Lexic.getYytext());
                    post[0] = Lexic.getYytext();
                    registrarPaso("F", "Sintetiza numero", TokensCalculadora.nombreToken(token),
                            "v=" + v[0] + ", post='" + post[0] + "'");
                    salir("F", true, "NUM=" + v[0]);
                    return true;
                } catch (NumberFormatException e) {
                    marcarError("Numero invalido: '" + Lexic.getYytext() + "'");
                }
                break;

            default:
                marcarError("Se esperaba un numero o '(' pero se encontro '"
                        + (Lexic.getYytext().isEmpty() ? "fin de texto" : Lexic.getYytext()) + "'");
                break;
        }

        salir("F", false, null);
        return false;
    }

    // ==========================================
    // Auxiliares para Traza y Errores
    // ==========================================

    private void registrarPaso(String funcion, String detalle, String tokenInfo, String estado) {
        contadorPasos++;
        PasoDescenso p = new PasoDescenso(contadorPasos, nivelActual, funcion, detalle, tokenInfo, estado);
        pasos.add(p);
    }

    private void entrar(String funcion) {
        String indent = "  ".repeat(nivelActual);
        arbolTexto.append(indent).append("→ ").append(funcion).append("\n");
        nivelActual++;
    }

    private void salir(String funcion, boolean exito, String res) {
        nivelActual = Math.max(0, nivelActual - 1);
        String indent = "  ".repeat(nivelActual);
        arbolTexto.append(indent).append(exito ? "✔ " : "✘ ").append(funcion)
                .append(res != null ? " [" + res + "]" : "")
                .append("\n");
    }

    private void marcarError(String mensaje) {
        if (error == null) {
            int pos = Lexic.getIndexInicioLex();
            String cerca = Lexic.getYytext().isEmpty() ? "al final de la expresion"
                    : "cerca de '" + Lexic.getYytext() + "'";
            error = mensaje + " (" + cerca + ", posicion " + pos + ").";
        }
    }

    public String getError() {
        return error;
    }

    public List<PasoDescenso> getPasos() {
        return pasos;
    }

    public String getArbolTexto() {
        return arbolTexto.toString();
    }
}
