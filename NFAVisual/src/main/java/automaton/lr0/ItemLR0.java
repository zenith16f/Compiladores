package automaton.lr0;

import automaton.ll1.ElemArrReglas;
import automaton.ll1.SimbGram;

import java.util.List;
import java.util.Objects;

public class ItemLR0 {

    private final int numRegla;
    private final ElemArrReglas regla;
    private final int posPunto;

    public ItemLR0(int numRegla, ElemArrReglas regla, int posPunto) {
        this.numRegla = numRegla;
        this.regla = regla;
        this.posPunto = esReglaEpsilon(regla) ? regla.getL().size() : posPunto;
    }

    private static boolean esReglaEpsilon(ElemArrReglas r) {
        List<SimbGram> l = r.getL();
        return l.size() == 1 && l.get(0).esEpsilon();
    }

    public int getNumRegla() {
        return numRegla;
    }

    public ElemArrReglas getRegla() {
        return regla;
    }

    public int getPosPunto() {
        return posPunto;
    }

    public boolean puntoAlFinal() {
        return posPunto >= regla.getL().size();
    }

    public SimbGram simboloDespuesDelPunto() {
        if (puntoAlFinal()) {
            return null;
        }
        return regla.getL().get(posPunto);
    }

    public ItemLR0 avanzarPunto() {
        if (puntoAlFinal()) {
            throw new IllegalStateException("El punto ya está al final del ítem.");
        }
        return new ItemLR0(numRegla, regla, posPunto + 1);
    }

    public int longitudRealLadoDerecho() {
        return esReglaEpsilon(regla) ? 0 : regla.getL().size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemLR0 otro)) return false;
        return numRegla == otro.numRegla && posPunto == otro.posPunto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numRegla, posPunto);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(regla.getSimbIzqGram()).append(" → ");
        List<SimbGram> l = regla.getL();
        if (esReglaEpsilon(regla)) {
            sb.append("•");
            return sb.toString();
        }
        for (int i = 0; i < l.size(); i++) {
            if (i == posPunto) {
                sb.append("• ");
            }
            sb.append(l.get(i));
            if (i < l.size() - 1) {
                sb.append(' ');
            }
        }
        if (posPunto >= l.size()) {
            sb.append(" •");
        }
        return sb.toString();
    }
}
