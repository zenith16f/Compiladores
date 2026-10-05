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
import java.util.Collections;
import java.util.Deque;
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
                afn.getEstadosAFN(), afn.getEstadosAccept());
        return destino;
    }

    public AFN cargarAfn(String nombre) throws IOException {
        Datos datos = leer(rutaDe(nombre, EXT_AFN), TIPO_AFN, true);
        AFN afn = new AFN();
        afn.setAlfabeto(datos.alfabeto);
        afn.setEstadoInicial(datos.inicial);
        afn.setEstadosAFN(datos.estados);
        afn.setEstadosAccept(datos.aceptacion);
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
                afd.getEstadosAFD(), afd.getEstadosAccept());
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

    public Path exportarAfdParaLexico(AFD afd, String nombre) throws IOException {
        if (afd == null) {
            throw new IllegalArgumentException("No hay AFD que exportar.");
        }
        Path destino = rutaDe(nombre, EXT_LEXICO);
        Files.createDirectories(destino.getParent());

        try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            List<Character> alfabeto = afd.getAlfabeto();

            StringBuilder cabecera = new StringBuilder();
            for (char c : alfabeto) {
                if (cabecera.length() > 0) {
                    cabecera.append(' ');
                }
                cabecera.append(c);
            }
            w.write(cabecera.toString());
            w.newLine();

            for (Estado estado : afd.getEstadosAFD()) {
                StringBuilder fila = new StringBuilder();
                for (char c : alfabeto) {
                    if (fila.length() > 0) {
                        fila.append(' ');
                    }
                    fila.append(destinoPara(estado, c));
                }
                w.write(fila.toString());
                w.newLine();
            }
        }
        return destino;
    }

    private int destinoPara(Estado estado, char simbolo) {
        for (Transicion t : estado.getTransiciones()) {
            if (!t.IsEpsilon() && simbolo >= t.getSimboloInferior() && simbolo <= t.getSimboloSuperior()) {
                return t.getEstadoDestino().getIdEstado();
            }
        }
        return -1;
    }

    // ---------- Nucleo comun de persistencia ----------

    private static class Datos {
        String tipo;
        ArrayList<Character> alfabeto = new ArrayList<>();
        ArrayList<Estado> estados = new ArrayList<>();
        Estado inicial;
        ArrayList<Estado> aceptacion = new ArrayList<>();
    }

    private void escribir(Path destino, String tipo, List<Character> alfabeto, Estado inicial,
                          List<Estado> estados, List<Estado> aceptacion) throws IOException {
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

    // Automatas/AFN/*.afn, Automatas/AFD/*.afd; los .txt del analizador quedan en Automatas/
    private Path carpetaDe(String extension) {
        if (EXT_AFN.equals(extension)) {
            return carpeta.resolve("AFN");
        }
        if (EXT_AFD.equals(extension)) {
            return carpeta.resolve("AFD");
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
