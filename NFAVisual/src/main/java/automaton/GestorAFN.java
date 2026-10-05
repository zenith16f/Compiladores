package automaton;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class GestorAFN {
    private final ObservableList<EntradaAFN> entradas = FXCollections.observableArrayList();
    private int siguienteId = 0;

    
    private AlmacenAutomatas almacen;
    private Consumer<String> manejadorErrores = mensaje -> System.err.println(mensaje);


    public void conectarAlmacen(AlmacenAutomatas almacen) {
        this.almacen = almacen;
        try {
            for (String nombre : almacen.listarAfn()) {
                try {
                    entradas.add(new EntradaAFN(siguienteId++, nombre, almacen.cargarAfn(nombre)));
                } catch (IOException | RuntimeException e) {
                    manejadorErrores.accept("No se pudo cargar el AFN '" + nombre + "': " + e.getMessage());
                }
            }
        } catch (IOException e) {
            manejadorErrores.accept("No se pudo leer la carpeta de AFN: " + e.getMessage());
        }
        // El listener se agrega despues de cargar para no volver a escribir lo que se acaba de leer
        entradas.addListener(this::alCambiarEntradas);
    }

    public void setManejadorErrores(Consumer<String> manejadorErrores) {
        this.manejadorErrores = manejadorErrores;
    }

    private void alCambiarEntradas(ListChangeListener.Change<? extends EntradaAFN> cambio) {
        while (cambio.next()) {
            // Primero se borra lo quitado (o renombrado) y luego se escribe lo nuevo
            for (EntradaAFN quitada : cambio.getRemoved()) {
                try {
                    almacen.eliminarAfn(quitada.getNombre());
                } catch (IOException | RuntimeException e) {
                    manejadorErrores.accept("No se pudo borrar el archivo de '" + quitada.getNombre() + "': " + e.getMessage());
                }
            }
            for (EntradaAFN agregada : cambio.getAddedSubList()) {
                try {
                    almacen.guardarAfn(agregada.getAfn(), agregada.getNombre());
                } catch (IOException | RuntimeException e) {
                    manejadorErrores.accept("No se pudo guardar '" + agregada.getNombre() + "': " + e.getMessage());
                }
            }
        }
    }

    // Getters and setters
    public ObservableList<EntradaAFN> getEntradas() {
        return entradas;
    }

    // Methods
    public int Registrar(AFN afn, String nombre) {
        int id = siguienteId++;
        entradas.add(new EntradaAFN(id, nombre, afn));
        return id;
    }

    public AFN Obtener(int id) {
        return BuscarEntrada(id).map(EntradaAFN::getAfn).orElse(null);
    }

    public EntradaAFN ObtenerEntrada(int id) {
        return BuscarEntrada(id).orElse(null);
    }

    public boolean ExisteNombre(String nombre) {
        return entradas.stream().anyMatch(entrada -> entrada.getNombre().equalsIgnoreCase(nombre.trim()));
    }

    public AFN ObtenerPorNombre(String nombre) {
        return entradas.stream()
                .filter(entrada -> entrada.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .map(EntradaAFN::getAfn)
                .orElse(null);
    }

    public boolean Eliminar(int id) {
        return entradas.removeIf(entrada -> entrada.getId() == id);
    }

    public void Renombrar(int id, String nuevoNombre) {
        EntradaAFN old = ExigirEntrada(id);
        int indice = entradas.indexOf(old);
        entradas.set(indice, new EntradaAFN(id, nuevoNombre, old.getAfn()));
    }

    public int total() {
        return entradas.size();
    }

    private Optional<EntradaAFN> BuscarEntrada(int id) {
        return entradas.stream().filter(entrada -> entrada.getId() == id).findFirst();
    }

    private EntradaAFN ExigirEntrada(int id) {
        return BuscarEntrada(id).orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada"));
    }

    // AFN Methods
    public int Union(int firstId, int secondId, String nombreResultado) {
        AFN f1 = ExigirEntrada(firstId).getAfn();
        AFN f2 = ExigirEntrada(secondId).getAfn();
        AFN resultado = f1.Unir(f2);
        Eliminar(firstId);
        Eliminar(secondId);
        return Registrar(resultado, nombreResultado);
    }

    public int Concatenacion(int firstId, int secondId, String nombreResultado) {
        AFN f1 = ExigirEntrada(firstId).getAfn();
        AFN f2 = ExigirEntrada(secondId).getAfn();
        AFN resultado = f1.Concatenar(f2);
        Eliminar(firstId);
        Eliminar(secondId);
        return Registrar(resultado, nombreResultado);
    }

    public int Positiva(int id, String nombreResultado) {
        AFN resultado = ExigirEntrada(id).getAfn().CerraduraPositiva();
        Eliminar(id);
        return Registrar(resultado, nombreResultado);
    }

    public int CerraduraKleene(int id, String nombreResultado) {
        AFN resultado = ExigirEntrada(id).getAfn().CerraduraKleene();
        Eliminar(id);
        return Registrar(resultado, nombreResultado);
    }

    public int Optional(int id, String nombreResultado) {
        AFN resultado = ExigirEntrada(id).getAfn().OpOpcional();
        Eliminar(id);
        return Registrar(resultado, nombreResultado);
    }
}
