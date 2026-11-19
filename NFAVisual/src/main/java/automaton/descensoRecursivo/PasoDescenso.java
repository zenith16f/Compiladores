package automaton.descensoRecursivo;

/**
 * Representa un paso en el proceso del analisis sintactico por descenso recursivo.
 */
public record PasoDescenso(
        int paso,
        int nivel,
        String funcion,
        String detalle,
        String tokenInfo,
        String estadoAtributos
) {
    public String formateado() {
        String indent = "  ".repeat(Math.max(0, nivel));
        return String.format("#%03d %s[%s] %s | Token: %s | %s",
                paso, indent, funcion, detalle,
                (tokenInfo != null && !tokenInfo.isBlank() ? tokenInfo : "-"),
                (estadoAtributos != null && !estadoAtributos.isBlank() ? estadoAtributos : ""));
    }
}
