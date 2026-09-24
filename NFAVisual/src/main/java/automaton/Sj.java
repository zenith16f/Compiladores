package automaton;

import java.util.Objects;
import java.util.Set;

public class Sj {
    public int id;
    public Set<Estado> edos;
    public Estado edoAFD;

    public Sj(int id, Set<Estado> edos, Estado edoAFD) {
        this.id = id;
        this.edos = edos;
        this.edoAFD = edoAFD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sj)) return false;
        Sj sj = (Sj) o;
        return Objects.equals(edos, sj.edos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edos);
    }

}
