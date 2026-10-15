package automaton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class AlmacenAutomatas {

    public static final String EXT_AFN = ".afn";
    public static final String EXT_AFD = ".afd";
    public static final String EXT_LEXICO = ".txt";

    private static final String TIPO_AFN = "AFN";
    private static final String TIPO_AFD = "AFD";

    private final Path carpeta;

    public AlmacenAutomatas() {
        this(Paths.get("Automatas"));
    }

    public AlmacenAutomatas(Path carpeta) {
        this.carpeta = carpeta;
    }

    public Path getCarpeta() {
        return carpeta;
    }

    // ---------- AFN ----------

    public Path guardarAfn(AFN afn, String nombre) throws IOException {
        if (afn == null) {
            throw new IllegalArgumentException("No hay AFN que guardar.");
        }
        Path destino = rutaDe(nombre, EXT_AFN);
        escribir(destino, TIPO_AFN, afn.getAlfabeto(), afn.getEstadoInicial(),
                afn.getEstadosAFN(), afn.getEstadosAccept(), afn.isTokensAsignados());
        return destino;
    }

    public AFN cargarAfn(String nombre) throws IOException {
        Datos datos = leer(rutaDe(nombre, EXT_AFN), TIPO_AFN, true);
        AFN afn = new AFN();
        afn.setAlfabeto(datos.alfabeto);
        afn.setEstadoInicial(datos.inicial);
        afn.setEstadosAFN(datos.estados);
        afn.setEstadosAccept(datos.aceptacion);
        afn.setTokensAsignados(datos.hayTokens);
        return afn;
    }

    public List<String> listarAfn() throws IOException {
        return listar(EXT_AFN);
    }

    public boolean eliminarAfn(String nombre) throws IOException {
        return Files.deleteIfExists(rutaDe(nombre, EXT_AFN));
    }

    // ---------- AFD (guardado general) ----------
    
    public void renombrarAfd(String nombreActual, String nombreNuevo) throws IOException {
        Path origen = rutaDe(nombreActual, EXT_AFD);
        Path destino = rutaDe(nombreNuevo, EXT_AFD);
        if (!Files.exists(origen)) {
            return;
        }
        boolean soloCambiaMayusculas = nombreActual.trim().equalsIgnoreCase(nombreNuevo.trim());
        if (!soloCambiaMayusculas && Files.exists(destino)) {
            throw new IOException("Ya existe un archivo llamado " + destino.getFileName());
        }
        if (soloCambiaMayusculas) {
            // En Windows "ab" y "AB" son el mismo archivo: se pasa por un nombre temporal
            Path temporal = origen.resolveSibling(nombreActual.trim() + ".renombrando");
            Files.move(origen, temporal);
            Files.move(temporal, destino);
        } else {
            Files.move(origen, destino);
        }
    }

    public Path guardarAfd(AFD afd, String nombre) throws IOException {
        if (afd == null) {
            throw new IllegalArgumentException("No hay AFD que guardar.");
        }
        Path destino = rutaDe(nombre, EXT_AFD);
        escribir(destino, TIPO_AFD, afd.getAlfabeto(), afd.getEstadoInicial(),
                afd.getEstadosAFD(), afd.getEstadosAccept(), true);
        return destino;
    }

    public AFD cargarAfd(String nombre) throws IOException {
        Datos datos = leer(rutaDe(nombre, EXT_AFD), TIPO_AFD, false);
        AFD afd = new AFD();
        afd.setAlfabeto(datos.alfabeto);
        afd.setEstadoInicial(datos.inicial);
        afd.setEstadosAFD(datos.estados);
        afd.setEstadosAccept(datos.aceptacion);
        afd.setNumEdos(datos.estados.size());
        return afd;
    }

    public List<String> listarAfd() throws IOException {
        return listar(EXT_AFD);
    }

    public boolean eliminarAfd(String nombre) throws IOException {
        return Files.deleteIfExists(rutaDe(nombre, EXT_AFD));
    }

    // ---------- AFD -> analizador lexico ----------

    /** Carpeta sugerida para las tablas que lee el analizador lexico (Automatas/Lexico). */
    public Path getCarpetaLexico() {
        return carpetaDe(EXT_LEXICO);
    }

    /** Escribe el AFD como tabla para {@link AnalizadorLexico#desdeArchivo(Path)}; el formato esta descrito ahi. */
    public Path exportarAfdParaLexico(AFD afd, Path destino) throws IOException {
        if (afd == null) {
            throw new IllegalArgumentException("No hay AFD que exportar.");
        }
        List<Estado> estados = ordenarEstados(afd);
        Map<Estado, Integer> fila = new IdentityHashMap<>();
        for (int i = 0; i < estados.size(); i++) {
            fila.put(estados.get(i), i);
        }

        int[][] tabla = new int[estados.size()][AnalizadorLexico.NUM_COLUMNAS];
        boolean[] enAlfabeto = new boolean[AnalizadorLexico.NUM_COLUMNAS];
        for (int i = 0; i < estados.size(); i++) {
            Arrays.fill(tabla[i], -1);
            for (Transicion t : estados.get(i).getTransiciones()) {
                if (t.IsEpsilon()) {
                    continue;
                }
                if (t.getSimboloSuperior() >= AnalizadorLexico.NUM_COLUMNAS) {
                    throw new IOException("El símbolo '" + t.getSimboloSuperior()
                            + "' no cabe en la tabla ASCII (0-255); no se puede exportar.");
                }
                for (int c = t.getSimboloInferior(); c <= t.getSimboloSuperior(); c++) {
                    tabla[i][c] = fila.get(t.getEstadoDestino());
                    enAlfabeto[c] = true;
                }
            }
        }

        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            w.write(String.valueOf(estados.size()));
            w.newLine();

            StringBuilder linea = new StringBuilder();
            for (int c = 0; c < AnalizadorLexico.NUM_COLUMNAS; c++) {
                linea.append(c == 0 ? "" : " ").append(enAlfabeto[c] ? c : -1);
            }
            w.write(linea.toString());
            w.newLine();

            for (int i = 0; i < estados.size(); i++) {
                Estado e = estados.get(i);
                linea = new StringBuilder();
                for (int c = 0; c < AnalizadorLexico.NUM_COLUMNAS; c++) {
                    linea.append(tabla[i][c]).append(' ');
                }
                boolean acepta = e.isEstadoAccept() || afd.getEstadosAccept().contains(e);
                linea.append(acepta ? e.getToken() : -1);
                w.write(linea.toString());
                w.newLine();
            }
        }
        return destino;
    }

    // El estado inicial va en la fila 0; despues los demas alcanzables en orden de busqueda
    private List<Estado> ordenarEstados(AFD afd) {
        Estado inicial = afd.getEstadoInicial();
        if (inicial == null) {
            if (afd.getEstadosAFD().isEmpty()) {
                throw new IllegalArgumentException("El AFD no tiene estados.");
            }
            inicial = afd.getEstadosAFD().get(0);
        }
        List<Estado> orden = new ArrayList<>();
        Map<Estado, Boolean> vistos = new IdentityHashMap<>();
        Deque<Estado> pendientes = new ArrayDeque<>();
        pendientes.add(inicial);
        vistos.put(inicial, true);
        while (!pendientes.isEmpty()) {
            Estado actual = pendientes.poll();
            orden.add(actual);
            for (Transicion t : actual.getTransiciones()) {
                if (vistos.putIfAbsent(t.getEstadoDestino(), true) == null) {
                    pendientes.add(t.getEstadoDestino());
                }
            }
        }
        return orden;
    }

    // ---------- Nucleo comun de persistencia ----------

    private static class Datos {
        String tipo;
        ArrayList<Character> alfabeto = new ArrayList<>();
        ArrayList<Estado> estados = new ArrayList<>();
        Estado inicial;
        ArrayList<Estado> aceptacion = new ArrayList<>();
        boolean hayTokens;
    }

    private void escribir(Path destino, String tipo, List<Character> alfabeto, Estado inicial,
                          List<Estado> estados, List<Estado> aceptacion, boolean conTokens) throws IOException {
        if (inicial == null) {
            throw new IllegalArgumentException("El automata no tiene estado inicial.");
        }

        List<Estado> todos = estadosAlcanzables(inicial, estados);
        Set<Integer> idsUsados = new java.util.HashSet<>();
        for (Estado e : todos) {
            if (!idsUsados.add(e.getIdEstado())) {
                // Mejor no escribir que dejar un archivo que no se pueda volver a leer
                throw new IOException("Hay dos estados distintos con el mismo id (" + e.getIdEstado() + "); no se guarda.");
            }
        }
        Files.createDirectories(destino.getParent());

        try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            w.write("TIPO " + tipo);
            w.newLine();

            StringBuilder linea = new StringBuilder("ALFABETO");
            for (char c : alfabeto) {
                linea.append(' ').append(codificar(c));
            }
            w.write(linea.toString());
            w.newLine();

            linea = new StringBuilder("ESTADOS");
            for (Estado e : todos) {
                linea.append(' ').append(e.getIdEstado());
            }
            w.write(linea.toString());
            w.newLine();

            w.write("INICIAL " + inicial.getIdEstado());
            w.newLine();

            linea = new StringBuilder("ACEPTACION");
            for (Estado e : todos) {
                if (e.isEstadoAccept() || (aceptacion != null && aceptacion.contains(e))) {
                    linea.append(' ').append(e.getIdEstado());
                }
            }
            w.write(linea.toString());
            w.newLine();

            // Va despues de ACEPTACION: al leer, marcar aceptacion genera un token generico
            // y esta linea lo reemplaza por el que tenia el estado.
            if (conTokens) {
                for (Estado e : todos) {
                    if (e.isEstadoAccept() || (aceptacion != null && aceptacion.contains(e))) {
                        w.write("TOKEN " + e.getIdEstado() + " " + e.getToken());
                        w.newLine();
                    }
                }
            }

            for (Estado origen : todos) {
                for (Transicion t : origen.getTransiciones()) {
                    w.write("TRANSICION " + origen.getIdEstado()
                            + " " + codificar(t.getSimboloInferior())
                            + " " + codificar(t.getSimboloSuperior())
                            + " " + t.getEstadoDestino().getIdEstado());
                    w.newLine();
                }
            }
        }
    }


    private List<Estado> estadosAlcanzables(Estado inicial, List<Estado> estados) {
        Set<Estado> vistos = new LinkedHashSet<>(estados);
        vistos.add(inicial);

        Deque<Estado> pendientes = new ArrayDeque<>(vistos);
        while (!pendientes.isEmpty()) {
            Estado actual = pendientes.poll();
            for (Transicion t : actual.getTransiciones()) {
                if (vistos.add(t.getEstadoDestino())) {
                    pendientes.add(t.getEstadoDestino());
                }
            }
        }
        return new ArrayList<>(vistos);
    }

    private Datos leer(Path origen, String tipoEsperado, boolean idsNuevos) throws IOException {
        if (!Files.exists(origen)) {
            throw new IOException("No existe el archivo: " + origen);
        }

        Datos datos = new Datos();
        Map<Integer, Estado> porId = null;
        int numeroLinea = 0;

        try (BufferedReader r = Files.newBufferedReader(origen, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = r.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                String[] partes = linea.split("\\s+");
                try {
                    switch (partes[0]) {
                        case "TIPO" -> {
                            datos.tipo = partes[1];
                            if (!tipoEsperado.equals(datos.tipo)) {
                                throw new IOException("El archivo es de tipo " + datos.tipo
                                        + " y se esperaba " + tipoEsperado + ".");
                            }
                        }
                        case "ALFABETO" -> {
                            for (int i = 1; i < partes.length; i++) {
                                datos.alfabeto.add(decodificar(partes[i]));
                            }
                        }
                        case "ESTADOS" -> {
                            porId = new HashMap<>();
                            for (int i = 1; i < partes.length; i++) {
                                int id = Integer.parseInt(partes[i]);
                                if (porId.containsKey(id)) {
                                    throw new IOException("Estado repetido: " + id);
                                }
                                Estado estado = idsNuevos ? new Estado() : new Estado(id);
                                porId.put(id, estado);
                                datos.estados.add(estado);
                            }
                        }
                        case "INICIAL" -> datos.inicial = estadoPorId(porId, partes[1]);
                        case "ACEPTACION" -> {
                            for (int i = 1; i < partes.length; i++) {
                                Estado estado = estadoPorId(porId, partes[i]);
                                estado.setEstadoAccept(true);
                                datos.aceptacion.add(estado);
                            }
                        }
                        case "TOKEN" -> {
                            estadoPorId(porId, partes[1]).setToken(Integer.parseInt(partes[2]));
                            datos.hayTokens = true;
                        }
                        case "TRANSICION" -> {
                            Estado origenT = estadoPorId(porId, partes[1]);
                            char inferior = decodificar(partes[2]);
                            char superior = decodificar(partes[3]);
                            Estado destino = estadoPorId(porId, partes[4]);
                            origenT.setTransicion(new Transicion(inferior, superior, destino));
                        }
                        default -> throw new IOException("Linea no reconocida: " + partes[0]);
                    }
                } catch (IOException e) {
                    throw new IOException("Archivo invalido (linea " + numeroLinea + "): " + e.getMessage(), e);
                } catch (RuntimeException e) {
                    throw new IOException("Archivo invalido (linea " + numeroLinea + "): " + linea, e);
                }
            }
        }

        if (datos.tipo == null) {
            throw new IOException("Archivo invalido: falta la linea TIPO.");
        }
        if (datos.inicial == null) {
            throw new IOException("Archivo invalido: falta el estado INICIAL.");
        }
        return datos;
    }

    private Estado estadoPorId(Map<Integer, Estado> porId, String texto) throws IOException {
        if (porId == null) {
            throw new IOException("La linea ESTADOS debe ir antes que las demas.");
        }
        Estado estado = porId.get(Integer.parseInt(texto));
        if (estado == null) {
            throw new IOException("Estado inexistente: " + texto);
        }
        return estado;
    }

    private String codificar(char c) {
        if (Character.isWhitespace(c) || Character.isISOControl(c) || c == '\\') {
            return String.format("\\u%04x", (int) c);
        }
        return String.valueOf(c);
    }

    private char decodificar(String texto) throws IOException {
        if (texto.length() == 6 && texto.startsWith("\\u")) {
            return (char) Integer.parseInt(texto.substring(2), 16);
        }
        if (texto.length() == 1) {
            return texto.charAt(0);
        }
        throw new IOException("Simbolo invalido: " + texto);
    }

    // ---------- Rutas ----------

    // Automatas/AFN/*.afn, Automatas/AFD/*.afd, Automatas/Lexico/*.txt
    private Path carpetaDe(String extension) {
        if (EXT_AFN.equals(extension)) {
            return carpeta.resolve("AFN");
        }
        if (EXT_AFD.equals(extension)) {
            return carpeta.resolve("AFD");
        }
        if (EXT_LEXICO.equals(extension)) {
            return carpeta.resolve("Lexico");
        }
        return carpeta;
    }

    private List<String> listar(String extension) throws IOException {
        Path dir = carpetaDe(extension);
        if (!Files.isDirectory(dir)) {
            return new ArrayList<>();
        }
        try (Stream<Path> archivos = Files.list(dir)) {
            List<String> nombres = new ArrayList<>();
            archivos.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(extension))
                    .forEach(n -> nombres.add(n.substring(0, n.length() - extension.length())));
            Collections.sort(nombres, String.CASE_INSENSITIVE_ORDER);
            return nombres;
        }
    }

    private Path rutaDe(String nombre, String extension) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacio.");
        }
        String limpio = nombre.trim();
        if (limpio.matches(".*[\\\\/:*?\"<>|].*") || limpio.contains("..")) {
            throw new IllegalArgumentException("El nombre tiene caracteres no permitidos: " + nombre);
        }
        return carpetaDe(extension).resolve(limpio + extension);
    }
}
