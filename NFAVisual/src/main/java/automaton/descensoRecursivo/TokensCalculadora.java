package automaton.descensoRecursivo;

/**
 * Codigos de token para el Analizador Sintactico por Descenso Recursivo
 * de Expresiones Aritmeticas
 *
 *   Clase Lexica   E.R.                         Token
 *   SUMA           '+'                           10
 *   RESTA          '-'                           20
 *   PRODUCTO       '*'                           30
 *   DIVISION       '/'                           40
 *   PAR_I          '('                           50
 *   PAR_D          ')'                           60
 *   NUM            [0-9]+(\.[0-9]+)?             70
 *   FIN            fin de cadena                  0
 */
public final class TokensCalculadora {

    public static final int FIN = 0;
    public static final int SUMA = 10;
    public static final int RESTA = 20;
    public static final int PRODUCTO = 30;
    public static final int DIVISION = 40;
    public static final int PAR_I = 50;
    public static final int PAR_D = 60;
    public static final int NUM = 70;
    public static final int ERROR = -1;

    private TokensCalculadora() {
    }

    public static String nombreToken(int token) {
        return switch (token) {
            case FIN -> "FIN (0)";
            case SUMA -> "SUMA (10)";
            case RESTA -> "RESTA (20)";
            case PRODUCTO -> "PRODUCTO (30)";
            case DIVISION -> "DIVISION (40)";
            case PAR_I -> "PAR_I (50)";
            case PAR_D -> "PAR_D (60)";
            case NUM -> "NUM (70)";
            default -> "ERROR (" + token + ")";
        };
    }
}
