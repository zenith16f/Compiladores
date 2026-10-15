package automaton;

import java.util.*;

public class AFN {
    private ArrayList<Character> alfabeto;
    private Estado estadoInicial;
    private ArrayList<Estado> estadosAccept;
    private ArrayList<Estado> estadosAFN;
    // true cuando los tokens de los estados de aceptacion los eligio el usuario
    // (Union especial); entonces el AFD hereda esos tokens en vez de generarlos.
    private boolean tokensAsignados;

    // Constructor
    public AFN() {
        this.alfabeto = new ArrayList<>();
        this.estadosAccept = new ArrayList<>();
        this.estadosAFN = new ArrayList<>();
    }

    // Method's
    public static AFN CreateAFN(char character) {
        return CreateAFN(character, character);
    }

    public static AFN CreateAFN(char simboloInferior, char simboloSuperior) {

        AFN afn = new AFN();
        Estado estadoUno = new Estado();
        Estado estadoDos = new Estado();

        Transicion transicion = new Transicion(simboloInferior, simboloSuperior, estadoDos);
        estadoUno.getTransiciones().add(transicion);
        estadoDos.setEstadoAccept(true);

        afn.estadoInicial = estadoUno;
        afn.estadosAccept.add(estadoDos);
        afn.estadosAFN.add(estadoUno);
        afn.estadosAFN.add(estadoDos);

        for (char a = simboloInferior; a <= simboloSuperior; a++) {
            afn.alfabeto.add(a);
        }

        return afn;
    }

    // Operations
    public AFN Unir(AFN afn2) {
        Estado estadoUno = new Estado();
        Estado estadoDos = new Estado();

        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, this.estadoInicial));
        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, afn2.estadoInicial));

        for (Estado estado : this.estadosAccept) {
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
            estado.setEstadoAccept(false);
        }

        for (Estado estado : afn2.estadosAccept) {
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
            estado.setEstadoAccept(false);
        }

        estadoDos.setEstadoAccept(true);
        ListUtils.Union(this.estadosAFN, afn2.estadosAFN);
        this.estadosAFN.add(estadoUno);
        this.estadosAFN.add(estadoDos);
        this.estadosAccept.clear();
        this.estadosAccept.add(estadoDos);
        this.estadoInicial = estadoUno;
        ListUtils.Union(this.alfabeto, afn2.alfabeto);
        this.tokensAsignados = false;

        return this;
    }

    /**
     * Pone el mismo token a todos los estados de aceptacion. Con null se quedan
     * los tokens genericos (el AFD numera sus estados de aceptacion 10, 20, 30...).
     */
    public AFN AsignarToken(Integer token) {
        if (token == null) {
            this.tokensAsignados = false;
            return this;
        }
        for (Estado estado : this.estadosAccept) {
            estado.setToken(token);
        }
        this.tokensAsignados = true;
        return this;
    }

    /**
     * Union especial (para el analizador lexico): un estado inicial nuevo con
     * transiciones epsilon al estado inicial de cada AFN. A diferencia de Unir(),
     * NO se crea un estado final comun: cada AFN conserva sus estados de
     * aceptacion y estos quedan marcados con el token que le toca a ese AFN.
     *
     * El orden de la lista es la prioridad: si al convertir a AFD un estado
     * contiene aceptaciones de varios AFN, gana el que aparece primero.
     */
    public static AFN UnionEspecial(List<AFN> afns, List<Integer> tokens) {
        if (afns.size() != tokens.size()) {
            throw new IllegalArgumentException("Cada AFN necesita su token.");
        }

        AFN resultado = new AFN();
        Estado nuevoInicial = new Estado();
        resultado.estadoInicial = nuevoInicial;
        resultado.estadosAFN.add(nuevoInicial);

        for (int i = 0; i < afns.size(); i++) {
            AFN afn = afns.get(i);
            int token = tokens.get(i);

            nuevoInicial.getTransiciones().add(new Transicion(Simbolo.EPSILON, afn.estadoInicial));

            for (Estado estado : afn.estadosAccept) {
                estado.setEstadoAccept(true); // setEstadoAccept pone un token generico,
                estado.setToken(token);       // por eso el token del usuario va despues
            }

            ListUtils.Union(resultado.estadosAFN, afn.estadosAFN);
            ListUtils.Union(resultado.estadosAccept, afn.estadosAccept);
            ListUtils.Union(resultado.alfabeto, afn.alfabeto);
        }

        resultado.tokensAsignados = true;
        return resultado;
    }

    public AFN Concatenar(AFN afn2) {
        for (Estado estado : this.estadosAccept) {
            for (Transicion transicion : afn2.estadoInicial.getTransiciones()) {
                estado.getTransiciones().add(transicion);
            }
            estado.setEstadoAccept(false);
        }

        ListUtils.Union(this.estadosAFN, afn2.estadosAFN);
        this.estadosAFN.remove(afn2.estadoInicial);
        this.estadosAccept.clear();
        ListUtils.Union(this.estadosAccept, afn2.estadosAccept);
        ListUtils.Union(this.alfabeto, afn2.alfabeto);
        this.tokensAsignados = false;

        return this;
    }

    public AFN CerraduraPositiva() {
        Estado estadoUno = new Estado();
        Estado estadoDos = new Estado();
        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, this.estadoInicial));

        for (Estado estado : this.estadosAccept) {
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, this.estadoInicial));
            estado.setEstadoAccept(false);
        }

        estadoDos.setEstadoAccept(true);
        this.estadosAFN.add(estadoUno);
        this.estadosAFN.add(estadoDos);
        this.setEstadoInicial(estadoUno);
        this.estadosAccept.clear();
        this.estadosAccept.add(estadoDos);
        this.tokensAsignados = false;

        return this;
    }

    public AFN CerraduraKleene() {
        this.CerraduraPositiva();

        for (Estado estado : this.estadosAccept) {
            this.estadoInicial.getTransiciones().add(new Transicion(Simbolo.EPSILON, estado));
        }

        return this;
    }

    public AFN OpOpcional() {
        Estado estadoUno = new Estado();
        Estado estadoDos = new Estado();

        estadoDos.setEstadoAccept(true);
        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, this.estadoInicial));

        for (Estado estado : this.estadosAccept) {
            estado.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
            estado.setEstadoAccept(false);
        }

        estadoUno.getTransiciones().add(new Transicion(Simbolo.EPSILON, estadoDos));
        this.estadosAFN.add(estadoUno);
        this.estadosAFN.add(estadoDos);
        this.setEstadoInicial(estadoUno);
        this.estadosAccept.clear();
        this.estadosAccept.add(estadoDos);
        this.tokensAsignados = false;

        return this;
    }

    public boolean esEstadoDeAceptacion(Set<Estado> conjuntoSj, ArrayList<Estado> estadosFinalesAFN) {
        for (Estado e : conjuntoSj) {
            if (estadosFinalesAFN.contains(e)) {
                return true;
            }
        }
        return false;
    }

    // Marca un estado del AFD como de aceptacion. Si el AFN viene de una Union
    // especial, el token es el del primer estado de aceptacion del AFN (en orden
    // de prioridad) contenido en Sj; si no, se queda el token generico.
    private void marcarAceptacionAfd(Estado edoAFD, Set<Estado> conjuntoSj) {
        edoAFD.setEstadoAccept(true);
        if (!tokensAsignados) {
            return;
        }
        for (Estado e : this.estadosAccept) {
            if (conjuntoSj.contains(e)) {
                edoAFD.setToken(e.getToken());
                return;
            }
        }
    }

    public AFD convertir_AFN() {
        GeneradorToken.ReiniciarToken();
        int numConjSj = 0;
        Queue<Sj> conjSjSinAnalizar = new ArrayDeque<>();
        List<Sj> conjTodosSj = new ArrayList<>();
        ArrayList<Estado> edosAFD = new ArrayList<>();
        ArrayList<Estado> edosAcept = new ArrayList<>();

        // Construcción de S0
        Set<Estado> s0Edos = cerradura_e(this.estadoInicial);
        Estado s0 = new Estado(numConjSj);
        if (esEstadoDeAceptacion(s0Edos, this.estadosAccept)) {
            // El AFN acepta la cadena vacia (la cerradura-e del estado
            // inicial ya toca un estado de aceptacion), asi que S0 en
            // el AFD tambien debe quedar marcado como de aceptacion.
            marcarAceptacionAfd(s0, s0Edos);
            edosAcept.add(s0);
        }
        Sj sjAux = new Sj(numConjSj++, s0Edos,s0);

        conjSjSinAnalizar.add(sjAux);
        conjTodosSj.add(sjAux);

        while (!conjSjSinAnalizar.isEmpty()) {
            sjAux = conjSjSinAnalizar.poll();

            // Reutilizamos el Estado que ya quedo guardado en el Sj (al que
            // ya apuntan las transiciones de otros estados) en vez de crear
            // uno nuevo: si se crea uno nuevo aqui, sus transiciones salientes
            // se agregan a un objeto distinto del que el resto del AFD ya
            // tiene como destino, y el grafo queda desconectado.
            Estado edojAFD = sjAux.edoAFD;

            for (char simb : this.alfabeto) {
                Set<Estado> sjTempEdos = ir_a(sjAux.edos, simb);
                if (sjTempEdos.isEmpty()) {
                    continue;
                }
                Sj edoExist = buscarSj(conjTodosSj, sjTempEdos);

                if (edoExist == null) { // Es nuevo
                    Estado edoAFDnuevo = new Estado(numConjSj);
                    if (esEstadoDeAceptacion(sjTempEdos, this.estadosAccept)) {
                        marcarAceptacionAfd(edoAFDnuevo, sjTempEdos);
                        edosAcept.add(edoAFDnuevo);
                    }

                    Sj sjTemp = new Sj(numConjSj++, sjTempEdos,edoAFDnuevo);
                    conjTodosSj.add(sjTemp);
                    conjSjSinAnalizar.add(sjTemp);
                    edojAFD.setTransicion(new Transicion(simb, edoAFDnuevo));
                } else { // Ya existe
                    edojAFD.setTransicion(new Transicion(simb,edoExist.edoAFD));
                }

            }

            edosAFD.add(edojAFD);

        }

        AFD afd = new AFD(this.alfabeto, edosAcept,edosAFD);
        afd.setEstadoInicial(s0);
        return afd;
    }

    public Set<Estado> cerradura_e(Estado estadoInicial){
        Set<Estado> r = new LinkedHashSet<>();
        Deque<Estado> s = new ArrayDeque<Estado>();
        s.push(estadoInicial);
        while(!s.isEmpty()){
            Estado aux = s.pop();
            r.add(aux);
            for(Transicion t : aux.getTransiciones()){
                if(t.IsEpsilon()){
                    if (!r.contains(t.getEstadoDestino()))
                        s.push(t.getEstadoDestino());
                }
            }
        }

        return r;
    }
    public Set<Estado> mover_a(char simbolo, Estado estadoInicial){
        Set<Estado> r = new LinkedHashSet<Estado>();
        for(Transicion t : estadoInicial.getTransiciones())
            if ( simbolo >= t.getSimboloInferior() && simbolo <= t.getSimboloSuperior() )
                r.add(t.getEstadoDestino());

        return r;
    }

    public Set<Estado> ir_a(Set<Estado> c, char simbolo){
        Set<Estado> r = new LinkedHashSet<>();
        for (Estado e : c ) {
            for (Estado ef : mover_a(simbolo,e)) {
                r.addAll(cerradura_e(ef));
            }
        }
        return r;
    }

    public Sj buscarSj(List<Sj> conjTodosSj, Set<Estado> sjTempEdos) {
        for (Sj sj : conjTodosSj) {
            if (sj.edos.equals(sjTempEdos)) {
                return sj;
            }
        }
        return null;
    }


    // Getters and Setters
    public ArrayList<Character> getAlfabeto() {
        return alfabeto;
    }

    public void setAlfabeto(ArrayList<Character> alfabeto) {
        this.alfabeto = alfabeto;
    }

    public Estado getEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(Estado estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    public ArrayList<Estado> getEstadosAccept() {
        return estadosAccept;
    }

    public void setEstadosAccept(ArrayList<Estado> estadosAccept) {
        this.estadosAccept = estadosAccept;
    }

    public ArrayList<Estado> getEstadosAFN() {
        return estadosAFN;
    }

    public void setEstadosAFN(ArrayList<Estado> estadosAFN) {
        this.estadosAFN = estadosAFN;
    }

    public boolean isTokensAsignados() {
        return tokensAsignados;
    }

    public void setTokensAsignados(boolean tokensAsignados) {
        this.tokensAsignados = tokensAsignados;
    }
}
