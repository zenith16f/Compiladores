package automaton.descensoRecursivo;

import automaton.AnalizadorLexico;
import automaton.Lexico;
import automaton.ll1.ElemArrReglas;
import automaton.ll1.Gramatica;
import automaton.ll1.SimbGram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Analizador Sintactico por Descenso Recursivo General sobre una Gramatica libre de recursion
 * por la izquierda.
 *
 * Para cada no terminal A con producciones A -> α_1 | α_2 | ... | α_k:
 *   - Crea conceptualmente una funcion recursiva A().
 *   - Pide un token de anticipacion (lookahead) y verifica a cual produccion α_i pertenece
 *     mediante First(α_i).
 *   - Si ninguna produccion no-epsilon coincide, y existe una produccion que deriva en ε,
 *     devuelve el token con undoToken() y toma la produccion ε.
 *   - Si coincide con α_i, devuelve el token con undoToken() y consume uno por uno los
 *     simbolos X_1 X_2 ... X_m de α_i (llamando recursivamente a X_j() si es no terminal,
 *     o haciendo match con yylex() si es terminal).
 */
public class DescRecGramatica {

    public record PasoTraza(int num, int nivel, String funcion, String reglaAplicada, String tokenActual, String cadenaRestante, String accion) {
    }

    public record ResultadoAnalisis(
            boolean aceptada,
            String mensaje,
            List<PasoTraza> pasos,
            String arbolLlamadas,
            String pseudocodigo
    ) {
    }

    private final Gramatica gramatica;
    private final SimbGram[] simbTerm;
    private final SimbGram[] simbNoTerm;
    private final List<String> advertencias = new ArrayList<>();

    // Estado durante la ejecucion de analizar()
    private Lexico lexicoActivo;
    private String sigmaActiva;
    private final List<PasoTraza> pasos = new ArrayList<>();
    private final StringBuilder arbolTexto = new StringBuilder();
    private int contadorPasos = 0;
    private int nivelActual = 0;
    private String errorDetectado = null;

    public DescRecGramatica(Gramatica gramatica) {
        this.gramatica = gramatica;

        List<SimbGram> terminales = new ArrayList<>();
        int token = 10;
        for (SimbGram t : gramatica.getConjTerm()) {
            terminales.add(new SimbGram(t.getSimbolo(), token, true));
            token += 10;
        }
        terminales.add(new SimbGram(SimbGram.FIN_CADENA, Lexico.FIN, true));
        this.simbTerm = terminales.toArray(new SimbGram[0]);
        this.simbNoTerm = gramatica.getConjNoTerm().toArray(new SimbGram[0]);

        verificarGramatica();
    }

    private void verificarGramatica() {
        // Verifica si hay recursion por la izquierda o ambiguedad en los conjuntos First de un mismo no terminal
        for (SimbGram nt : simbNoTerm) {
            List<ElemArrReglas> reglasNt = reglasDe(nt);
            int reglasConEpsilon = 0;
            for (int i = 0; i < reglasNt.size(); i++) {
                Set<SimbGram> f1 = gramatica.First(reglasNt.get(i).getL());
                if (Gramatica.contieneEpsilon(f1)) {
                    reglasConEpsilon++;
                }
                for (int j = i + 1; j < reglasNt.size(); j++) {
                    Set<SimbGram> f2 = gramatica.First(reglasNt.get(j).getL());
                    for (SimbGram s : f1) {
                        if (!s.esEpsilon() && f2.contains(s)) {
                            advertencias.add("Ambigüedad en First de '" + nt + "' para el terminal '" + s + "'.");
                        }
                    }
                }
            }
            if (reglasConEpsilon > 1) {
                advertencias.add("El no terminal '" + nt + "' tiene múltiples reglas que derivan en ε.");
            }
        }
    }

    public List<ElemArrReglas> reglasDe(SimbGram noTerminal) {
        List<ElemArrReglas> lista = new ArrayList<>();
        for (ElemArrReglas r : gramatica.getArrReglas()) {
            if (r.getSimbIzqGram().equals(noTerminal)) {
                lista.add(r);
            }
        }
        return lista;
    }

    public int idDeRegla(ElemArrReglas regla) {
        return gramatica.getArrReglas().indexOf(regla);
    }

    /**
     * Genera el codigo / pseudocodigo de las funciones recursivas para cada no terminal de la gramatica.
     */
    public String generarPseudocodigo() {
        StringBuilder sb = new StringBuilder();
        SimbGram inicial = gramatica.getSimboloInicial();
        sb.append("// =========================================================\n");
        sb.append("// Funciones del Descenso Recursivo generadas para G\n");
        sb.append("// Símbolo inicial: ").append(inicial).append("\n");
        sb.append("// =========================================================\n\n");

        sb.append("boolean IniSintac() {\n");
        sb.append("    if (").append(nombreFuncion(inicial)).append("()) {\n");
        sb.append("        int token = Lexic.yylex();\n");
        sb.append("        if (token == FIN)\n");
        sb.append("            return true;\n");
        sb.append("    }\n");
        sb.append("    return false;\n");
        sb.append("}\n\n");

        for (SimbGram nt : simbNoTerm) {
            List<ElemArrReglas> reglas = reglasDe(nt);
            sb.append("// Producciones de ").append(nt).append("\n");
            sb.append("boolean ").append(nombreFuncion(nt)).append("() {\n");
            sb.append("    int token = Lexic.yylex();\n");

            ElemArrReglas reglaEpsilon = null;
            for (ElemArrReglas r : reglas) {
                Set<SimbGram> first = gramatica.First(r.getL());
                List<SimbGram> terminalesFirst = first.stream().filter(s -> !s.esEpsilon()).toList();
                if (Gramatica.contieneEpsilon(first)) {
                    reglaEpsilon = r;
                }
                if (!terminalesFirst.isEmpty()) {
                    String cond = String.join(" || ",
                            terminalesFirst.stream().map(t -> "token == TOKEN_" + t.getSimbolo() + " /* " + obtenerTokenActual(t) + " */").toList());
                    sb.append("    // Regla ").append(idDeRegla(r)).append(": ").append(r).append("\n");
                    sb.append("    if (").append(cond).append(") {\n");
                    sb.append("        Lexic.undoToken();\n");
                    for (SimbGram s : r.getL()) {
                        if (s.esEpsilon()) {
                            continue;
                        }
                        if (s.isEsTerminal()) {
                            sb.append("        if (Lexic.yylex() != TOKEN_").append(s.getSimbolo()).append(") return false;\n");
                        } else {
                            sb.append("        if (!").append(nombreFuncion(s)).append("()) return false;\n");
                        }
                    }
                    sb.append("        return true;\n");
                    sb.append("    }\n");
                }
            }

            if (reglaEpsilon != null) {
                sb.append("    // Caso ε (Regla ").append(idDeRegla(reglaEpsilon)).append(": ").append(reglaEpsilon).append(")\n");
                sb.append("    Lexic.undoToken();\n");
                sb.append("    return true;\n");
            } else {
                sb.append("    return false;\n");
            }
            sb.append("}\n\n");
        }
        return sb.toString();
    }

    private String nombreFuncion(SimbGram nt) {
        return nt.getSimbolo().replace("'", "p");
    }

    private int obtenerTokenActual(SimbGram terminal) {
        for (SimbGram t : simbTerm) {
            if (t.equals(terminal)) {
                return t.getToken();
            }
        }
        return -1;
    }

    // =====================================================================================
    //  Ejecucion del Descenso Recursivo sobre una cadena σ
    // =====================================================================================

    public ResultadoAnalisis analizar(Lexico lexico, String sigma) {
        this.lexicoActivo = lexico;
        this.sigmaActiva = sigma == null ? "" : sigma;
        this.lexicoActivo.setSigma(this.sigmaActiva);
        this.pasos.clear();
        this.arbolTexto.setLength(0);
        this.contadorPasos = 0;
        this.nivelActual = 0;
        this.errorDetectado = null;

        SimbGram inicial = gramatica.getSimboloInicial();
        registrarPaso("IniSintac", "Inicio", "-", textoRestanteActual(), "Llama a " + inicial + "()");
        entrarArbol("IniSintac");

        boolean exito = expandirNoTerminal(inicial);

        if (exito) {
            int token = siguienteToken();
            String infoTok = descripcionToken(token);
            if (token == Lexico.FIN) {
                registrarPaso("IniSintac", "Fin de cadena", infoTok, "$", "Aceptar (token == FIN)");
                salirArbol("IniSintac", true, "Cadena aceptada");
                return new ResultadoAnalisis(true,
                        "La cadena es válida: aceptada por descenso recursivo.",
                        new ArrayList<>(pasos), arbolTexto.toString(), generarPseudocodigo());
            } else {
                String msg = "Error sintáctico: terminó " + inicial + "() pero sobra texto '" + lexicoActivo.getYytext() + "'";
                marcarError(msg);
                registrarPaso("IniSintac", "Error fin", infoTok, textoRestanteActual(), msg);
            }
        }

        salirArbol("IniSintac", false, errorDetectado != null ? errorDetectado : "Fallo");
        String msgFinal = errorDetectado != null ? errorDetectado : "La cadena fue rechazada por el descenso recursivo.";
        return new ResultadoAnalisis(false, msgFinal, new ArrayList<>(pasos), arbolTexto.toString(), generarPseudocodigo());
    }

    private boolean expandirNoTerminal(SimbGram A) {
        if (contadorPasos > 50_000) {
            marcarError("Se excedió el límite de pasos (posible ciclo infinito).");
            return false;
        }

        entrarArbol(A.toString());
        int token = siguienteToken();
        String infoTok = descripcionToken(token);
        String restante = textoRestanteActual();

        if (token != Lexico.FIN && buscarTerminalPorToken(token) == null) {
            String msg = "Error léxico: '" + lexicoActivo.getYytext() + "' (token " + token + ") no corresponde a ningún terminal";
            registrarPaso(A.toString(), "-", infoTok, restante, msg);
            marcarError(msg);
            salirArbol(A.toString(), false, msg);
            return false;
        }

        List<ElemArrReglas> reglas = reglasDe(A);
        ElemArrReglas reglaElegida = null;
        ElemArrReglas reglaEpsilon = null;

        for (ElemArrReglas r : reglas) {
            Set<SimbGram> first = gramatica.First(r.getL());
            if (Gramatica.contieneEpsilon(first)) {
                reglaEpsilon = r;
            }
            for (SimbGram f : first) {
                if (!f.esEpsilon() && obtenerTokenActual(f) == token) {
                    reglaElegida = r;
                    break;
                }
            }
            if (reglaElegida != null) {
                break;
            }
        }

        if (reglaElegida != null) {
            // Devolvemos el token de anticipacion para que la secuencia del lado derecho lo consuma en orden
            lexicoActivo.undoToken();
            int numRegla = idDeRegla(reglaElegida);
            registrarPaso(A.toString(), "Regla " + numRegla + ": " + reglaElegida, infoTok, restante,
                    "Lookahead coincide con First -> expande " + reglaElegida.ladoDerechoTexto());

            for (SimbGram X : reglaElegida.getL()) {
                if (X.esEpsilon()) {
                    continue;
                }
                if (X.isEsTerminal()) {
                    int tokConsumido = siguienteToken();
                    String infoCons = descripcionToken(tokConsumido);
                    int tokEsperado = obtenerTokenActual(X);
                    if (tokConsumido == tokEsperado) {
                        registrarPaso(A.toString(), "Match '" + X + "'", infoCons, textoRestanteActual(),
                                "Consume terminal '" + lexicoActivo.getYytext() + "' (token " + tokConsumido + ")");
                    } else {
                        String msg = "Error en " + A + "(): se esperaba '" + X + "' (token " + tokEsperado
                                + ") pero llegó " + infoCons;
                        registrarPaso(A.toString(), "Error Match", infoCons, textoRestanteActual(), msg);
                        marcarError(msg);
                        salirArbol(A.toString(), false, "Fallo match " + X);
                        return false;
                    }
                } else {
                    if (!expandirNoTerminal(X)) {
                        salirArbol(A.toString(), false, "Fallo en " + X);
                        return false;
                    }
                }
            }
            salirArbol(A.toString(), true, reglaElegida.toString());
            return true;
        }

        if (reglaEpsilon != null) {
            lexicoActivo.undoToken();
            int numRegla = idDeRegla(reglaEpsilon);
            registrarPaso(A.toString(), "Regla " + numRegla + ": " + reglaEpsilon, infoTok, restante,
                    "undoToken() -> aplica transición ε");
            salirArbol(A.toString(), true, "ε");
            return true;
        }

        String msg = "Error en " + A + "(): token inesperado " + infoTok;
        registrarPaso(A.toString(), "Sin regla", infoTok, restante, msg);
        marcarError(msg);
        salirArbol(A.toString(), false, "Token inesperado");
        return false;
    }

    private int siguienteToken() {
        int token = lexicoActivo.yylex();
        while (token != Lexico.FIN && buscarTerminalPorToken(token) == null && lexicoActivo.getYytext().isBlank()) {
            token = lexicoActivo.yylex();
        }
        return token;
    }

    private SimbGram buscarTerminalPorToken(int token) {
        for (SimbGram t : simbTerm) {
            if (t.getToken() == token) {
                return t;
            }
        }
        return null;
    }

    private String descripcionToken(int token) {
        if (token == Lexico.FIN) {
            return "$ (FIN=0)";
        }
        SimbGram term = buscarTerminalPorToken(token);
        String lex = lexicoActivo.getYytext();
        if (term != null) {
            return "'" + lex + "' → " + term.getSimbolo() + " (" + token + ")";
        }
        return "'" + lex + "' (token " + token + ")";
    }

    private String textoRestanteActual() {
        int desde = lexicoActivo.getIndexInicioLex();
        if (sigmaActiva == null || desde >= sigmaActiva.length()) {
            return "$";
        }
        String sub = sigmaActiva.substring(desde).strip();
        return sub.isEmpty() ? "$" : sub + " $";
    }

    private void registrarPaso(String funcion, String regla, String tokenInfo, String restante, String accion) {
        contadorPasos++;
        String indent = "  ".repeat(Math.max(0, nivelActual));
        pasos.add(new PasoTraza(contadorPasos, nivelActual, indent + funcion + "()", regla, tokenInfo, restante, accion));
    }

    private void entrarArbol(String funcion) {
        String indent = "  ".repeat(Math.max(0, nivelActual));
        arbolTexto.append(indent).append("→ ").append(funcion).append("()\n");
        nivelActual++;
    }

    private void salirArbol(String funcion, boolean exito, String detalle) {
        nivelActual = Math.max(0, nivelActual - 1);
        String indent = "  ".repeat(Math.max(0, nivelActual));
        arbolTexto.append(indent)
                .append(exito ? "✔ " : "✘ ")
                .append(funcion).append("()")
                .append(detalle != null && !detalle.isBlank() ? "  [" + detalle + "]" : "")
                .append("\n");
    }

    private void marcarError(String msg) {
        if (errorDetectado == null) {
            errorDetectado = msg;
        }
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
                throw new IllegalArgumentException("El token " + token + " ya pertenece al terminal '" + simbTerm[j] + "'.");
            }
        }
        simbTerm[indice].setToken(token);
    }

    public Gramatica getGramatica() {
        return gramatica;
    }

    public SimbGram[] getSimbTerm() {
        return simbTerm;
    }

    public SimbGram[] getSimbNoTerm() {
        return simbNoTerm;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }
}
