package automaton;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

public class GestorAFD {

    private final ObservableList<EntradaAFD> entradas = FXCollections.observableArrayList();

    private int siguienteId = 0;


    private AlmacenAutomatas almacen;


    public void conectarAlmacen(AlmacenAutomatas almacen) {
        this.almacen = almacen;
        try {
            for (String nombre : almacen.listarAfd()) {
                try {
                    entradas.add(new EntradaAFD(siguienteId++, nombre, almacen.cargarAfd(nombre)));
                } catch (IOException | RuntimeException e) {
                    System.err.println("No se pudo cargar el AFD '" + nombre + "': " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer la carpeta de AFD: " + e.getMessage());
        }
    }

    public boolean ExisteNombre(String nombre) {
        return entradas.stream().anyMatch(entrada -> entrada.getNombre().equalsIgnoreCase(nombre.trim()));
    }

    /**
     * Cambia el nombre en la lista y renombra tambien el archivo .afd (si existe).
     */
    public void RenombrarConArchivo(int id, String nuevoNombre) throws IOException {
        EntradaAFD actual = ExigirEntrada(id);
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio.");
        }
        String limpio = nuevoNombre.trim();
        boolean ocupado = entradas.stream()
                .anyMatch(e -> e.getId() != id && e.getNombre().equalsIgnoreCase(limpio));
        if (ocupado) {
            throw new IllegalArgumentException("Ya existe un AFD llamado '" + limpio + "'.");
        }
        if (almacen != null) {
            // Primero el archivo: si falla (nombre invalido, disco), la lista queda como estaba
            almacen.renombrarAfd(actual.getNombre(), limpio);
        }
        Renombrar(id, limpio);
    }

    
    public boolean EliminarConArchivo(int id) throws IOException {
        EntradaAFD entrada = ExigirEntrada(id);
        if (almacen != null) {
            almacen.eliminarAfd(entrada.getNombre());
        }
        return Eliminar(id);
    }


    public void guardarEnArchivo(int id) throws IOException {
        if (almacen == null) {
            throw new IllegalStateException("El gestor no tiene almacen conectado.");
        }
        EntradaAFD entrada = ExigirEntrada(id);
        almacen.guardarAfd(entrada.getAutomata(), entrada.getNombre());
    }

    public ObservableList<EntradaAFD> getEntradas() {
        return entradas;
    }

    // Methods
    public int Registrar(AFD afd, String nombre) {
        int id = siguienteId++;
        entradas.add(new EntradaAFD(id, nombre, afd));
        return id;
    }

    public AFD Obtener(int id) {
        return BuscarEntrada(id).map(EntradaAFD::getAutomata).orElse(null);
    }

    public AFD ObtenerPorNombre(String nombre) {
        return entradas.stream()
                .filter(entradaAFD -> entradaAFD.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .map(EntradaAFD::getAutomata)
                .orElse(null);
    }

    public boolean Eliminar(int id) {
        return entradas.removeIf(entradaAFD -> entradaAFD.getId() == id);
    }

    public void Renombrar(int id, String nuevoNombre) {
        EntradaAFD old = ExigirEntrada(id);
        int indice = entradas.indexOf(old);
        entradas.set(indice, new EntradaAFD(id, nuevoNombre, old.getAutomata()));
    }

    public int total() {
        return entradas.size();
    }

    private Optional<EntradaAFD> BuscarEntrada(int id) {
        return entradas.stream().filter(entradaAFD -> entradaAFD.getId() == id).findFirst();
    }

    private EntradaAFD ExigirEntrada(int id) {
        return BuscarEntrada(id).orElseThrow(() -> new IllegalArgumentException("No existe entrada con id " + id));
    }

}
