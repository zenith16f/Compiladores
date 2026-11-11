package automaton;

/**
 * Lo que necesita un analizador sintactico de su analizador lexico (el "Lexic" del pizarron):
 * pedir el siguiente token con yylex(), ver su lexema con yytext y regresarlo con UndoToken().
 *
 * La implementan AnalizadorLexico (el que trabaja con la tabla de un AFD exportado) y los lexicos
 * auxiliares de la parte LL(1).
 */
public interface Lexico {

    /** Token que se regresa cuando ya no queda cadena por analizar. */
    int FIN = 0;

    /** Cambia la cadena a analizar y empieza desde el principio. */
    void setSigma(String sigma);

    /** Regresa el token del siguiente lexema (FIN al terminar la cadena). */
    int yylex();

    /** El lexema que reconocio la ultima llamada a yylex(). */
    String getYytext();

    /** Deshace la ultima llamada a yylex(). */
    boolean undoToken();

    /** Posicion en la cadena donde empieza el siguiente lexema a analizar. */
    int getIndexCharAct();

    /** Posicion donde empezo el ultimo lexema regresado. */
    int getIndexInicioLex();
}
