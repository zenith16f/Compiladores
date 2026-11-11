package automaton.ll1;

import automaton.Lexico;

import java.util.ArrayList;
import java.util.List;

/**
 * Descenso recursivo sobre la "gramatica de gramaticas": lee el texto con las reglas que escribe el usuario
 * y va llenando el arreglo de reglas (ArrReglas) de un objeto Gramatica.
 *
 * Gramatica de gramaticas, ya sin recursion por la izquierda (tal como en el pizarron):
 *
 *   G               -> Reglas
 *   Reglas          -> Regla PC Reglas'
 *   Reglas'         -> Regla PC Reglas' | ε
 *   Regla           -> LadoIzq FLECHA LadosDerechos
 *   LadoIzq         -> SIMBOLO
 *   LadosDerechos   -> LadoDerecho LadosDerechos'
 *   LadosDerechos'  -> OR LadoDerecho LadosDerechos' | ε
 *   LadoDerecho     -> SecSimbolos
 *   SecSimbolos     -> SIMBOLO SecSimbolos'
 *   SecSimbolos'    -> SIMBOLO SecSimbolos' | ε
 *
 * Hay una funcion bool por cada no terminal. Las que tienen ε piden un token y, si no es el que
 * esperaban, lo regresan con UndoToken() y terminan con true.
 */
public class DescRecGramGram {

    private final Lexico Lexic;
    private final Gramatica GramLL1 = new Gramatica();
    private String error; // primer lugar donde no se cumplio la gramatica (para avisar al usuario)

    public DescRecGramGram(String textoGramatica) {
        this.Lexic = new LexicoGramatica(textoGramatica);
    }

    /**
     * Analiza el texto y regresa la gramatica con su arreglo de reglas.
     *
     * @throws IllegalArgumentException si el texto no esta bien escrito
     */
    public static Gramatica analizar(String textoGramatica) {
        DescRecGramGram analizador = new DescRecGramGram(textoGramatica);
        if (!analizador.InicSintac()) {
            throw new IllegalArgumentException(analizador.error != null ? analizador.error
                    : "La gramática no está bien escrita.");
        }
        analizador.GramLL1.identificarTerminales();
        return analizador.GramLL1;
    }

    /** Para iniciar el analisis se llama a la funcion del simbolo inicial, y despues debe venir el FIN. */
    public boolean InicSintac() {
        int token;
        if (G()) {
            token = Lexic.yylex();
            if (token == Lexico.FIN) {
                return true;
            }
            marcarError("Sobra texto después de la última regla");
        }
        return false;
    }

    // G -> Reglas
    boolean G() {
        if (Reglas()) {
            return true;
        }
        return false;
    }

    // Reglas -> Regla PC Reglas'
    boolean Reglas() {
        int token;
        if (Regla()) {
            token = Lexic.yylex();
            if (token == LexicoGramatica.PC) {
                if (ReglasP()) {
                    return true;
                }
            } else {
                marcarError("Falta ';' al final de la regla");
            }
        }
        return false;
    }

    // Reglas' -> Regla PC Reglas' | ε
    boolean ReglasP() {
        int token;
        token = Lexic.yylex();
        if (token == LexicoGramatica.SIMBOLO) { // empieza otra regla
            Lexic.undoToken();
            if (Regla()) {
                token = Lexic.yylex();
                if (token == LexicoGramatica.PC) {
                    if (ReglasP()) {
                        return true;
                    }
                } else {
                    marcarError("Falta ';' al final de la regla");
                }
            }
            return false;
        }
        Lexic.undoToken();
        return true;
    }

    // Regla -> LadoIzq FLECHA LadosDerechos
    boolean Regla() {
        int token;
        String[] NombLadoIzq = new String[1]; // equivale al "ref string" del pizarron
        if (LadoIzq(NombLadoIzq)) {
            SimbGram s = new SimbGram(NombLadoIzq[0], 0, false);
            GramLL1.conjNoTerm.add(s);
            token = Lexic.yylex();
            if (token == LexicoGramatica.FLECHA) {
                if (LadosDerechos(NombLadoIzq[0])) {
                    return true;
                }
            } else {
                marcarError("Se esperaba '->' después de '" + NombLadoIzq[0] + "'");
            }
        }
        return false;
    }

    // LadoIzq -> SIMBOLO
    boolean LadoIzq(String[] NombSimb) {
        int token;
        token = Lexic.yylex();
        if (token == LexicoGramatica.SIMBOLO) {
            NombSimb[0] = Lexic.getYytext();
            if (SimbGram.esNombreEpsilon(NombSimb[0])) {
                marcarError("epsilon no puede ser lado izquierdo de una regla");
                return false;
            }
            return true;
        }
        marcarError("Se esperaba el símbolo del lado izquierdo de la regla");
        return false;
    }

    // LadosDerechos -> LadoDerecho LadosDerechos'
    boolean LadosDerechos(String NombLadoIzq) {
        if (LadoDerecho(NombLadoIzq)) {
            if (LadosDerechosP(NombLadoIzq)) {
                return true;
            }
        }
        return false;
    }

    // LadosDerechos' -> OR LadoDerecho LadosDerechos' | ε
    boolean LadosDerechosP(String NombLadoIzq) {
        int token;
        token = Lexic.yylex();
        if (token == LexicoGramatica.OR) {
            if (LadoDerecho(NombLadoIzq)) {
                if (LadosDerechosP(NombLadoIzq)) {
                    return true;
                }
            }
            return false;
        }
        Lexic.undoToken();
        return true;
    }

    // LadoDerecho -> SecSimbolos
    // Aqui se guarda la regla en el arreglo: ArrReglas[NumReglas] = (NombLadoIzq, l)
    boolean LadoDerecho(String NombLadoIzq) {
        List<SimbGram> l = new ArrayList<>();
        SimbGram s = new SimbGram(NombLadoIzq, 0, false);
        if (SecSimbolos(l)) {
            normalizarEpsilon(l);
            GramLL1.arrReglas.add(new ElemArrReglas(s, l));
            return true;
        }
        return false;
    }

    // SecSimbolos -> SIMBOLO SecSimbolos'
    boolean SecSimbolos(List<SimbGram> l) {
        int token;
        SimbGram Nodo;
        token = Lexic.yylex();
        if (token == LexicoGramatica.SIMBOLO) {
            Nodo = new SimbGram(Lexic.getYytext(), 0, false);
            if (SecSimbolosP(l)) {
                l.add(0, Nodo); // AddInicio: los simbolos que siguen ya se agregaron
                return true;
            }
        } else {
            marcarError("Se esperaba al menos un símbolo en el lado derecho (usa 'epsilon' para la cadena vacía)");
        }
        return false;
    }

    // SecSimbolos' -> SIMBOLO SecSimbolos' | ε
    boolean SecSimbolosP(List<SimbGram> l) {
        int token;
        SimbGram Nodo;
        token = Lexic.yylex();
        if (token == LexicoGramatica.SIMBOLO) {
            Nodo = new SimbGram(Lexic.getYytext(), 0, false);
            if (SecSimbolosP(l)) {
                l.add(0, Nodo);
                return true;
            }
            return false;
        }
        Lexic.undoToken();
        return true;
    }

    // ---------- Auxiliares ----------

    /** "epsilon" / "ε" se guardan como un solo simbolo EPSILON; si viene junto a otros simbolos sobra. */
    private static void normalizarEpsilon(List<SimbGram> l) {
        for (int i = 0; i < l.size(); i++) {
            if (SimbGram.esNombreEpsilon(l.get(i).getSimbolo())) {
                l.set(i, new SimbGram(SimbGram.EPSILON, 0, true));
            }
        }
        if (l.size() > 1) {
            l.removeIf(SimbGram::esEpsilon);
        }
    }

    private void marcarError(String mensaje) {
        if (error == null) {
            int pos = Lexic.getIndexInicioLex();
            String cerca = Lexic.getYytext().isEmpty() ? "cerca del final del texto"
                    : "cerca de '" + Lexic.getYytext() + "'";
            error = mensaje + " (" + cerca + ", posición " + pos + ").";
        }
    }
}
