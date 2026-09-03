package automaton;

public class GeneradorId {
    private static int siguienteIdEstado = 0;
    
    private GeneradorId(){}
    
    public static int NuevoIdEstado(){
        return siguienteIdEstado++;
    }
    
    public static void ReiniciarEstado(){
        siguienteIdEstado = 0;
    }
}
