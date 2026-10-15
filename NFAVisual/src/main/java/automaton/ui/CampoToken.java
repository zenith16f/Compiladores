package automaton.ui;

/**
 * Lectura y validacion del token que el usuario escribe para los estados finales de un AFN.
 */
final class CampoToken {

    // Token que regresa yylex() cuando no reconoce nada; no se puede asignar a un AFN
    static final int TOKEN_ERROR = 1400;

    private CampoToken() {
    }

    /**
     * @return el token escrito, o null si el campo esta vacio (se usan tokens genericos)
     * @throws IllegalArgumentException con el mensaje para el usuario si el token no es valido
     */
    static Integer leer(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        int token;
        try {
            token = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El token debe ser un número entero.");
        }
        if (token <= 0 || token == TOKEN_ERROR) {
            throw new IllegalArgumentException("El token debe ser un entero positivo distinto de "
                    + TOKEN_ERROR + " (token de error).");
        }
        return token;
    }
}
