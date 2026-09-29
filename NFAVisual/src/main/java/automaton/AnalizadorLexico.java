package automaton;

//agregar el ID a EdoAFD

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class AnalizadorLexico {
    AFD AFD_lexic; //este es el afd sobre el que va a trabajar nuestro algoritmo
    String sigma; // la cadena que será analizada por el léxico
    boolean pasoPorEdoAcep; //bandera para saber si él automata paso por el edoacep
    int indexCharAct; //este int va a llevar la cuenta de en que caracter del string vamos
    //lexema: subdcadena que sea reconocida
    int index_begin_lex;
    int index_last_lex;
    String yytext; // es la cadenita que ya hayamos reconocido como un lexema

    public AnalizadorLexico() {
        sigma = "";
        AFD_lexic = null;
        pasoPorEdoAcep = false;
        indexCharAct = -1;
        index_begin_lex = -1;
        index_last_lex = -1;
    }

    public AnalizadorLexico(String rutaArchivo) throws FileNotFoundException {
        File afdFile = new File(rutaArchivo);

        try (Scanner afdReader = new Scanner(afdFile)) {
            int totalEstados = Integer.parseInt(afdReader.next());
            int[] infAlfabeto = new int[256];
            AFD_lexic = new AFD();
            AFD_lexic.setNumEdos(totalEstados);
            ArrayList<Estado> estadosAfd = new ArrayList<>();

            for (int i = 0; i < 256; i++)
                infAlfabeto[i] = Integer.parseInt(afdReader.next());

            for (int i = 0; i < 256; i++) {
                if (infAlfabeto[i] != -1)
                    AFD_lexic.getAlfabeto().add((char) i);
            }
            for (int t = 0; t < totalEstados; t++)
                estadosAfd.add(new Estado(t));

            for (Estado e : estadosAfd) {
                for (int col = 0; col < 256; col++) {
                    int edoDestino = Integer.parseInt(afdReader.next());
                    if (edoDestino == -1) continue;
                    e.setTransicion(new Transicion((char) infAlfabeto[col], estadosAfd.get(edoDestino)));
                }
            }
            AFD_lexic.setEstadosAFD(estadosAfd);
        }

        sigma = "";
        pasoPorEdoAcep = false;
        indexCharAct = -1;
        index_begin_lex = -1;
        index_last_lex = -1;
    }


    public AnalizadorLexico(String rutaArchivo, String cadenaAnalizar) throws FileNotFoundException {
        File afdFile = new File(rutaArchivo);

        try (Scanner afdReader = new Scanner(afdFile)) {
            int totalEstados = Integer.parseInt(afdReader.next());
            int[] infAlfabeto = new int[256];
            AFD_lexic = new AFD();
            AFD_lexic.setNumEdos(totalEstados);
            ArrayList<Estado> estadosAfd = new ArrayList<>();

            for (int i = 0; i < 256; i++)
                infAlfabeto[i] = Integer.parseInt(afdReader.next());

            for (int i = 0; i < 256; i++) {
                if (infAlfabeto[i] != -1)
                    AFD_lexic.getAlfabeto().add((char) i);
            }
            for (int t = 0; t < totalEstados; t++)
                estadosAfd.add(new Estado(t));

            for (Estado e : estadosAfd) {
                for (int col = 0; col < 256; col++) {
                    int edoDestino = Integer.parseInt(afdReader.next());
                    if (edoDestino == -1) continue;
                    e.setTransicion(new Transicion((char) infAlfabeto[col], estadosAfd.get(edoDestino)));
                }
            }
            AFD_lexic.setEstadosAFD(estadosAfd);
        }
        sigma = cadenaAnalizar;
        indexCharAct = 0;
    }

    //el analizador lexico siempre debe regresar 0 cuando ya no hay nada por revisar
    int yylex() //asi se llama en todos los compiladores, para llamarlo hay que tener ya cargado él automata y la cadena que quier revisar
    {
        pasoPorEdoAcep = false;
        index_begin_lex = indexCharAct;
        int token = -1;
        int tokenError = 1400;
        int edoAct = 0;
        int lenSigma = sigma.length();
        if (indexCharAct > lenSigma) { //para verficar
            return 0;
        }

        while (indexCharAct < lenSigma) {
            for (Transicion t : AFD_lexic.getEstadosAFD().get(edoAct).getTransiciones()) {
                if (sigma.charAt(indexCharAct) >= t.getSimboloInferior() &&
                        sigma.charAt(indexCharAct) <= t.getSimboloSuperior()) {
                    edoAct = t.getEstadoDestino().getIdEstado();
                }
            }

            if (edoAct != -1) {//lo que significa que no es de aceptacion
                if (AFD_lexic.getEstadosAFD().get(edoAct)
                        .getTransiciones().get(256).getEstadoDestino().getIdEstado() != -1) {
                    index_last_lex = indexCharAct;// aquí recordamos donde se dio el estado de aceptación
                    pasoPorEdoAcep = true;
                    token = AFD_lexic.getEstadosAFD().get(edoAct).getTransiciones().get(256).getEstadoDestino().getIdEstado();
                }
                indexCharAct++;

                if (pasoPorEdoAcep) {
                    yytext = sigma.substring(index_begin_lex, index_last_lex);
                    indexCharAct = index_last_lex + 1;
                    return token;
                }

                indexCharAct++; //Esto lo hacemos por qué en caso de que no hayamos pasado por edo. De acpt y hay error, pues, nadamas saltar ese caracter que da problemas
                return tokenError;
            }
            return 0;
        }

        return token;
    }

}