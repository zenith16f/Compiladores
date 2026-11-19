package automaton.lr0;

import automaton.ll1.ElemArrReglas;
import automaton.ll1.Gramatica;
import automaton.ll1.SimbGram;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColeccionCanonicaLR0 {

    private final Gramatica gramaticaOriginal;
    private final SimbGram simboloInicialAumentado;
    private final List<ElemArrReglas> reglasAumentadas;
    private final List<Set<ItemLR0>> estados = new ArrayList<>();
    private final List<Map<SimbGram, Integer>> transiciones = new ArrayList<>();

    public ColeccionCanonicaLR0(Gramatica gramatica) {
        this.gramaticaOriginal = gramatica;
        this.simboloInicialAumentado = crearSimboloAumentado(gramatica);
        this.reglasAumentadas = new ArrayList<>();

        ElemArrReglas reglaCero = new ElemArrReglas(
                simboloInicialAumentado,
                List.of(gramatica.getSimboloInicial())
        );
        reglasAumentadas.add(reglaCero);
        reglasAumentadas.addAll(gramatica.getArrReglas());

        construirColeccion();
    }

    private static SimbGram crearSimboloAumentado(Gramatica g) {
        String base = g.getSimboloInicial().getSimbolo() + "'";
        Set<SimbGram> todos = new LinkedHashSet<>(g.getConjNoTerm());
        todos.addAll(g.getConjTerm());
        while (todos.contains(new SimbGram(base, 0, false))) {
            base = base + "'";
        }
        return new SimbGram(base, 0, false);
    }

    public Set<ItemLR0> cerradura(Set<ItemLR0> nucleo) {
        Set<ItemLR0> J = new LinkedHashSet<>(nucleo);
        Deque<ItemLR0> pendientes = new ArrayDeque<>(nucleo);

        while (!pendientes.isEmpty()) {
            ItemLR0 item = pendientes.poll();
            SimbGram B = item.simboloDespuesDelPunto();
            if (B != null && !B.isEsTerminal()) {
                for (int r = 0; r < reglasAumentadas.size(); r++) {
                    ElemArrReglas regla = reglasAumentadas.get(r);
                    if (regla.getSimbIzqGram().equals(B)) {
                        ItemLR0 nuevo = new ItemLR0(r, regla, 0);
                        if (J.add(nuevo)) {
                            pendientes.add(nuevo);
                        }
                    }
                }
            }
        }
        return J;
    }

    public Set<ItemLR0> irA(Set<ItemLR0> I, SimbGram X) {
        Set<ItemLR0> desplazados = new LinkedHashSet<>();
        for (ItemLR0 item : I) {
            SimbGram despues = item.simboloDespuesDelPunto();
            if (X.equals(despues)) {
                desplazados.add(item.avanzarPunto());
            }
        }
        if (desplazados.isEmpty()) {
            return Collections.emptySet();
        }
        return cerradura(desplazados);
    }

    private void construirColeccion() {
        ItemLR0 itemInicial = new ItemLR0(0, reglasAumentadas.get(0), 0);
        Set<ItemLR0> i0 = cerradura(Set.of(itemInicial));
        estados.add(i0);
        transiciones.add(new LinkedHashMap<>());

        List<SimbGram> simbolosGram = new ArrayList<>();
        simbolosGram.addAll(gramaticaOriginal.getConjNoTerm());
        simbolosGram.addAll(gramaticaOriginal.getConjTerm());

        int i = 0;
        while (i < estados.size()) {
            Set<ItemLR0> estadoActual = estados.get(i);
            Map<SimbGram, Integer> transEstado = transiciones.get(i);

            Set<SimbGram> candidatos = new LinkedHashSet<>();
            for (ItemLR0 item : estadoActual) {
                SimbGram x = item.simboloDespuesDelPunto();
                if (x != null && !x.esEpsilon()) {
                    candidatos.add(x);
                }
            }
            for (SimbGram x : simbolosGram) {
                if (candidatos.contains(x)) {
                    procesarTransicion(estadoActual, transEstado, x);
                }
            }
            for (SimbGram x : candidatos) {
                if (!transEstado.containsKey(x)) {
                    procesarTransicion(estadoActual, transEstado, x);
                }
            }
            i++;
        }
    }

    private void procesarTransicion(Set<ItemLR0> estadoActual, Map<SimbGram, Integer> transEstado, SimbGram x) {
        Set<ItemLR0> destino = irA(estadoActual, x);
        if (destino.isEmpty()) {
            return;
        }
        int indiceDestino = estados.indexOf(destino);
        if (indiceDestino == -1) {
            indiceDestino = estados.size();
            estados.add(destino);
            transiciones.add(new LinkedHashMap<>());
        }
        transEstado.put(x, indiceDestino);
    }

    public Gramatica getGramaticaOriginal() {
        return gramaticaOriginal;
    }

    public SimbGram getSimboloInicialAumentado() {
        return simboloInicialAumentado;
    }

    public List<ElemArrReglas> getReglasAumentadas() {
        return reglasAumentadas;
    }

    public List<Set<ItemLR0>> getEstados() {
        return estados;
    }

    public List<Map<SimbGram, Integer>> getTransiciones() {
        return transiciones;
    }
}
