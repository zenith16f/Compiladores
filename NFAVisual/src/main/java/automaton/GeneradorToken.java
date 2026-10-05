package automaton;

public class GeneradorToken {
    private static int siguienteTokenEstado = 0;

    private GeneradorToken(){}
    
    public static int NuevoTokenEstado(){
        siguienteTokenEstado = siguienteTokenEstado +10;
        return siguienteTokenEstado;
    }
    
    public static void ReiniciarToken(){
        siguienteTokenEstado = 0;
    }
}
