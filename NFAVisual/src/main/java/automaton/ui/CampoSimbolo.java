package automaton.ui;

/**
 * Lectura de los simbolos que el usuario escribe al crear un AFN, y su forma visible en pantalla.
 * Los caracteres invisibles se escriben con un nombre: \s o "espacio", \t o "tab", \n o "salto".
 */
final class CampoSimbolo {

    static final String AYUDA = "Escribe un solo símbolo. Para espacio usa \\s o «espacio»; "
            + "para tabulador \\t; para salto de línea \\n.";

    private CampoSimbolo() {
    }

    /**
     * @return el simbolo escrito, o null si el campo esta vacio
     * @throws IllegalArgumentException con el mensaje para el usuario si no es un solo simbolo
     */
    static Character leer(String texto) {
        if (texto == null || texto.isEmpty()) {
            return null;
        }
        // Un espacio tecleado tal cual tambien vale
        if (texto.length() == 1) {
            return texto.charAt(0);
        }
        String limpio = texto.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException(AYUDA);
        }
        switch (limpio.toLowerCase()) {
            case "\\s", "espacio", "space", "␣" -> {
                return ' ';
            }
            case "\\t", "tab" -> {
                return '\t';
            }
            case "\\n", "salto" -> {
                return '\n';
            }
            default -> {
                if (limpio.length() == 1) {
                    return limpio.charAt(0);
                }
                throw new IllegalArgumentException(AYUDA);
            }
        }
    }

    /** Forma visible de un simbolo para la grafica y las tablas. */
    static String mostrar(char simbolo) {
        return switch (simbolo) {
            case ' ' -> "␣";
            case '\t' -> "\\t";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            default -> String.valueOf(simbolo);
        };
    }
}
