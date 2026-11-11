package automaton.ll1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Representacion de la gramatica para obtener la tabla LL(1) (clase Gramatica del pizarron).
 *
 * - Cada regla (cada alternativa separada por '|') es un ElemArrReglas: simbolo izquierdo + lista ligada.
 * - Todas las reglas se guardan en un arreglo (ArrReglas); el indice de la regla en el arreglo es su Id.
 * - El lado izquierdo de la regla 0 es el simbolo inicial.
 *
 *   E  -> T E' ;               ArrReglas[0] = E  : T E'
 *   E' -> + T E' | - T E' | ε  ArrReglas[1] = E' : + T E'
 *                              ArrReglas[2] = E' : - T E'
 *                              ArrReglas[3] = E' : ε
 *   ...
 */
public class Gramatica {

    final List<ElemArrReglas> arrReglas = new ArrayList<>();
    final Set<SimbGram> conjNoTerm = new LinkedHashSet<>(); // en orden de aparicion
    final Set<SimbGram> conjTerm = new LinkedHashSet<>();

    public int getNumReglas() {
        return arrReglas.size();
    }

    public List<ElemArrReglas> getArrReglas() {
        return arrReglas;
    }

    public Set<SimbGram> getConjNoTerm() {
        return conjNoTerm;
    }

    public Set<SimbGram> getConjTerm() {
        return conjTerm;
    }

    public SimbGram getSimboloInicial() {
        return arrReglas.get(0).getSimbIzqGram();
    }

    /**
     * Una vez leidas todas las reglas: los simbolos que aparecen en algun lado izquierdo son no terminales;
     * todos los demas del lado derecho son terminales (epsilon no cuenta como terminal de la tabla).
     */
    void identificarTerminales() {
        conjTerm.clear();
        for (ElemArrReglas regla : arrReglas) {
            for (SimbGram s : regla.getL()) {
                if (s.esEpsilon()) {
                    s.setEsTerminal(true);
                } else if (conjNoTerm.contains(s)) {
                    s.setEsTerminal(false);
                } else {
                    s.setEsTerminal(true);
                    conjTerm.add(s);
                }
            }
        }
    }

    // =====================================================================================
    //  FIRST
    // =====================================================================================

    /** First de una lista de simbolos (el lado derecho de una regla, o una parte de el). */
    public Set<SimbGram> First(List<SimbGram> lista) {
        return First(lista, new HashSet<>());
    }

    /**
     * Igual que en el pizarron. El conjunto "visitando" solo sirve para detectar recursion por la izquierda
     * (A -> A ...), que haria que First se llamara a si mismo sin fin; una gramatica asi no es LL(1).
     */
    private Set<SimbGram> First(List<SimbGram> lista, Set<SimbGram> visitando) {
        Set<SimbGram> R = new LinkedHashSet<>();

        if (lista.isEmpty()) {
            return R;
        }

        // Si el primer simbolo es terminal o epsilon, el First es ese simbolo
        if (lista.get(0).isEsTerminal()) {
            R.add(lista.get(0));
            return R;
        }

        // Es no terminal: se une el First de todas sus reglas
        SimbGram noTerminal = lista.get(0);
        if (!visitando.add(noTerminal)) {
            throw new IllegalStateException("La gramática tiene recursión por la izquierda en '" + noTerminal
                    + "'. Hay que eliminarla antes de construir la tabla LL(1).");
        }
        for (int i = 0; i < getNumReglas(); i++) {
            if (lista.get(0).equals(arrReglas.get(i).getSimbIzqGram())) {
                R.addAll(First(arrReglas.get(i).getL(), visitando));
            }
        }
        visitando.remove(noTerminal);

        // Si el no terminal puede ser epsilon, tambien cuenta lo que sigue en la lista
        if (contieneEpsilon(R) && lista.size() > 1) {
            quitarEpsilon(R);
            List<SimbGram> l2 = new ArrayList<>(lista.subList(1, lista.size()));
            R.addAll(First(l2, visitando));
        }
        return R;
    }

    // =====================================================================================
    //  FOLLOW
    // =====================================================================================

    public Set<SimbGram> Follow(SimbGram s) {
        return Follow(s, new HashSet<>());
    }

    /**
     * Igual que en el pizarron. "visitados" evita ciclos como A -> ... B y B -> ... A, donde
     * Follow(A) necesita Follow(B) y Follow(B) necesita Follow(A).
     */
    private Set<SimbGram> Follow(SimbGram s, Set<SimbGram> visitados) {
        Set<SimbGram> R = new LinkedHashSet<>();
        if (!visitados.add(s)) {
            return R;
        }

        // Al simbolo inicial siempre lo puede seguir el fin de cadena
        if (s.equals(arrReglas.get(0).getSimbIzqGram())) {
            R.add(new SimbGram(SimbGram.FIN_CADENA, 0, true));
        }

        for (int i = 0; i < getNumReglas(); i++) {
            List<SimbGram> l = arrReglas.get(i).getL();
            SimbGram izq = arrReglas.get(i).getSimbIzqGram();
            for (int j = 0; j < l.size(); j++) {
                if (!l.get(j).equals(s)) {
                    continue;
                }
                // Lo que esta a la derecha de s en la regla
                List<SimbGram> l2 = new ArrayList<>(l.subList(j + 1, l.size()));
                if (l2.isEmpty()) {
                    // s esta al final: A -> α s  =>  Follow(A) esta en Follow(s)
                    if (!izq.equals(s)) {
                        R.addAll(Follow(izq, visitados));
                    }
                } else {
                    Set<SimbGram> F = First(l2);
                    if (contieneEpsilon(F)) {
                        // A -> α s β  con β =>* ε  : First(β) - {ε}  U  Follow(A)
                        quitarEpsilon(F);
                        R.addAll(F);
                        if (!izq.equals(s)) {
                            R.addAll(Follow(izq, visitados));
                        }
                    } else {
                        R.addAll(F);
                    }
                }
            }
        }
        return R;
    }

    // ---------- Auxiliares ----------

    public static boolean contieneEpsilon(Set<SimbGram> conjunto) {
        return conjunto.stream().anyMatch(SimbGram::esEpsilon);
    }

    public static void quitarEpsilon(Set<SimbGram> conjunto) {
        conjunto.removeIf(SimbGram::esEpsilon);
    }
}
