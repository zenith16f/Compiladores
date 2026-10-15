package automaton;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Analizador lexico que trabaja sobre la tabla de transiciones de un AFD exportado.
 *
 * El archivo lo genera AlmacenAutomatas.exportarAfdParaLexico().
 * Formato del archivo (todo separado por espacios / saltos de linea):
 *   linea 1: numero de estados
 *   linea 2: 256 enteros, uno por codigo ASCII: el mismo codigo si el simbolo esta en el alfabeto, -1 si no
 *   una linea por estado (la fila 0 es el estado inicial): 256 destinos (-1 = sin transicion) y al final el token
 *   (-1 si el estado no es de aceptacion)
 */
public class AnalizadorLexico {

    public static final int FIN = 0;            // ya no hay nada que analizar
    public static final int TOKEN_ERROR = 1400; // caracter que no forma parte de ningun lexema
    public static final int NUM_COLUMNAS = 256;

    private final int[][] tabla; // tabla[estado][codigoAscii] = estado destino o -1
    private final int[] tokens;  // tokens[estado] = token del estado o -1

    private String sigma = "";      // la cadena que sera analizada por el lexico
    private int indexCharAct = 0;   // en que caracter de sigma vamos
    private int indexInicioLex = 0; // donde empezo el ultimo lexema regresado
    private String yytext = "";     // el ultimo lexema reconocido
    // Posicion antes de cada llamada a yylex(), para poder deshacer tokens
    private final Deque<Integer> historial = new ArrayDeque<>();

    private AnalizadorLexico(int[][] tabla, int[] tokens) {
        this.tabla = tabla;
        this.tokens = tokens;
    }

    // ---------- Construccion ----------

    public static AnalizadorLexico desdeArchivo(Path archivo) throws IOException {
        String[] numeros = Files.readString(archivo, StandardCharsets.UTF_8).trim().split("\\s+");
        int[] pos = {0};
        try {
            int totalEstados = siguiente(numeros, pos);
            if (totalEstados <= 0) {
                throw new IOException("El número de estados debe ser mayor que cero.");
            }
            for (int col = 0; col < NUM_COLUMNAS; col++) {
                siguiente(numeros, pos); // fila del alfabeto: solo informativa
            }

            int[][] tabla = new int[totalEstados][NUM_COLUMNAS];
            int[] tokens = new int[totalEstados];
            for (int edo = 0; edo < totalEstados; edo++) {
                for (int col = 0; col < NUM_COLUMNAS; col++) {
                    int destino = siguiente(numeros, pos);
                    if (destino < -1 || destino >= totalEstados) {
                        throw new IOException("El estado " + edo + " tiene una transición al estado inexistente "
                                + destino + ".");
                    }
                    tabla[edo][col] = destino;
                }
                tokens[edo] = siguiente(numeros, pos);
            }
            if (pos[0] < numeros.length) {
                throw new IOException("Sobran datos al final: el archivo dice tener " + totalEstados + " estados.");
            }
            return new AnalizadorLexico(tabla, tokens);
        } catch (NumberFormatException e) {
            throw new IOException("El archivo no es una tabla de AFD válida (valor no numérico: "
                    + e.getMessage() + ").", e);
        }
    }

    private static int siguiente(String[] numeros, int[] pos) throws IOException {
        if (pos[0] >= numeros.length || numeros[pos[0]].isEmpty()) {
            throw new IOException("El archivo está incompleto: no es una tabla de AFD exportada.");
        }
        return Integer.parseInt(numeros[pos[0]++]);
    }

    // ---------- Analisis ----------

    /**
     * Regresa el token del siguiente lexema (el mas largo posible), TOKEN_ERROR si el caracter
     * actual no inicia ningun lexema, o FIN cuando ya no queda cadena. El lexema queda en yytext.
     */
    public int yylex() {
        historial.push(indexCharAct);
        indexInicioLex = indexCharAct;
        if (indexCharAct >= sigma.length()) {
            yytext = "";
            return FIN;
        }

        int edoAct = 0;
        int token = -1;
        int finUltimoLexema = -1; // posicion justo despues del ultimo estado de aceptacion visitado
        int i = indexCharAct;
        while (i < sigma.length()) {
            char c = sigma.charAt(i);
            int destino = c < NUM_COLUMNAS ? tabla[edoAct][c] : -1;
            if (destino == -1) {
                break;
            }
            edoAct = destino;
            i++;
            if (tokens[edoAct] != -1) {
                token = tokens[edoAct];
                finUltimoLexema = i;
            }
        }

        if (finUltimoLexema == -1) {
            // Nunca se paso por un estado de aceptacion: solo se salta el caracter que da problemas
            indexCharAct = indexInicioLex + 1;
            yytext = sigma.substring(indexInicioLex, indexCharAct);
            return TOKEN_ERROR;
        }
        // Se regresa al ultimo estado de aceptacion: lo leido despues se vuelve a analizar
        indexCharAct = finUltimoLexema;
        yytext = sigma.substring(indexInicioLex, indexCharAct);
        return token;
    }

    /** Deshace la ultima llamada a yylex(). @return false si no habia nada que deshacer */
    public boolean undoToken() {
        if (historial.isEmpty()) {
            return false;
        }
        indexCharAct = historial.pop();
        indexInicioLex = indexCharAct;
        yytext = "";
        return true;
    }

    // ---------- Getters & Setters ----------

    public int getNumEstados() {
        return tabla.length;
    }

    public String getSigma() {
        return sigma;
    }

    /** Cambia la cadena y reinicia el analisis desde el principio. */
    public void setSigma(String sigma) {
        this.sigma = sigma == null ? "" : sigma;
        this.indexCharAct = 0;
        this.indexInicioLex = 0;
        this.yytext = "";
        this.historial.clear();
    }

    public int getIndexCharAct() {
        return indexCharAct;
    }

    public int getIndexInicioLex() {
        return indexInicioLex;
    }

    public String getYytext() {
        return yytext;
    }
}
