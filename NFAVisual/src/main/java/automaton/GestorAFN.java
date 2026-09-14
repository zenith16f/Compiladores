package automaton;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;

public class GestorAFN {
    private final ObservableList<EntradaAFN> entradas = FXCollections.observableArrayList();
    private int siguienteId = 0;
    
    // Getters and setters
    public ObservableList<EntradaAFN> getEntradas() {
        return entradas;
    }
    
    // Methods
    public int Registrar(AFN afn, String nombre){
        int id = siguienteId++;
        entradas.add(new EntradaAFN(id, nombre, afn));
        return id;
    }
    
    public AFN Obtener(int id){
        return BuscarEntrada(id).map(EntradaAFN::getAfn).orElse(null);
    }
    
    public AFN ObtenerPorNombre(String nombre){
        return entradas.stream()
                .filter(entrada -> entrada.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .map(EntradaAFN::getAfn)
                .orElse(null);
    }
    
    public boolean Eliminar(int id){
        return entradas.removeIf(entrada -> entrada.getId() == id);
    }
    
    public void Renombrar(int id , String nuevoNombre){
        EntradaAFN old = ExigirEntrada(id);
        int indice = entradas.indexOf(old);
        entradas.set(indice, new EntradaAFN(id, nuevoNombre, old.getAfn()));
    }
    
    public int total(){
        return entradas.size();
    }
    
    private Optional<EntradaAFN> BuscarEntrada(int id){
        return entradas.stream().filter(entrada -> entrada.getId() == id).findFirst();
    }
    
    private EntradaAFN ExigirEntrada(int id){
        return BuscarEntrada(id).orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada"));
    }
    
    // AFN Methods
    public int Union(int firstId, int secondId, String nombreResultado){
        AFN f1 = ExigirEntrada(firstId).getAfn();
        AFN f2 = ExigirEntrada(secondId).getAfn();
        AFN resultado = f1.Unir(f2);
        Eliminar(firstId);
        Eliminar(secondId);
        return Registrar(resultado, nombreResultado);
    }
    
    public int Concatenacion(int firstId, int secondId, String nombreResultado){
        AFN f1 = ExigirEntrada(firstId).getAfn();
        AFN f2 = ExigirEntrada(secondId).getAfn();
        AFN resultado = f1.Concatenar(f2);
        Eliminar(firstId);
        Eliminar(secondId);
        return Registrar(resultado, nombreResultado);
    }
    
    public int Positiva(int id, String nombreResultado){
        AFN resultado = ExigirEntrada(id).getAfn().CerraduraPositiva();
        Eliminar(id);
        return Registrar(resultado, nombreResultado);
    }
    
    public int CerraduraKleene(int id, String nombreResultado){
        AFN resultado = ExigirEntrada(id).getAfn().CerraduraKleene();
        Eliminar(id);
        return Registrar(resultado, nombreResultado);
    }
    
    public int Optional(int id, String nombreResultado){
        AFN resultado = ExigirEntrada(id).getAfn().OpOpcional();
        Eliminar(id);
        return Registrar(resultado, nombreResultado);
    }
}
