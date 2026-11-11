package automaton.ll1;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Un elemento del arreglo de reglas (clase ElemArrGram / ReglasGram del pizarron):
 * el simbolo del lado izquierdo y la lista (ligada) de simbolos del lado derecho.
 *
 * Ejemplo: la regla 1 "E' -> + T E'" se guarda como  SimbIzqGram = E'   l = [+] -> [T] -> [E']
 */
public class ElemArrReglas {

    private final SimbGram simbIzqGram;
    private final List<SimbGram> l;

    public ElemArrReglas(SimbGram simbIzqGram, List<SimbGram> l) {
        this.simbIzqGram = simbIzqGram;
        this.l = l;
    }

    public SimbGram getSimbIzqGram() {
        return simbIzqGram;
    }

    public List<SimbGram> getL() {
        return l;
    }

    /** El lado derecho como texto, ej. "+ T E'". */
    public String ladoDerechoTexto() {
        return l.stream().map(SimbGram::toString).collect(Collectors.joining(" "));
    }

    @Override
    public String toString() {
        return simbIzqGram + " → " + ladoDerechoTexto();
    }
}
