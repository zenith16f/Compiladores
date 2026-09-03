package automaton;

import java.util.ArrayList;

public class ListUtils {
    
    private ListUtils()
    {

    }
    
    public static <T> void Union (ArrayList<T> listaUno, ArrayList<T> listaDos){
        for (T elemento : listaDos) {
            if (!listaUno.contains(elemento)) {
                listaUno.add(elemento);
            }
        }
    }
}
