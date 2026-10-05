package automaton;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.NoSuchElementException;
import java.util.Optional;

public class GestorAFD {

    private final ObservableList<EntradaAFD> entradas = FXCollections.observableArrayList();

    private int siguienteId = 0;

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
