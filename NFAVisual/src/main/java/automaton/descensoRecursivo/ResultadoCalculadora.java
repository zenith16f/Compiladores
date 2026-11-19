package automaton.descensoRecursivo;

import java.util.List;

public record ResultadoCalculadora(
        boolean sintaxisValida,
        double valor,
        String postfijo,
        String error,
        List<PasoDescenso> pasos,
        String arbolTexto
) {
    public static ResultadoCalculadora error(String mensaje, List<PasoDescenso> pasos, String arbolTexto) {
        return new ResultadoCalculadora(false, 0.0, "", mensaje, pasos, arbolTexto);
    }

    public static ResultadoCalculadora exito(double valor, String postfijo, List<PasoDescenso> pasos, String arbolTexto) {
        return new ResultadoCalculadora(true, valor, postfijo, null, pasos, arbolTexto);
    }
}
