package automaton;

//agregar el ID a EdoAFD

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class AnalizadorLexico {
    AFD AFD_lexic; //este es el afd sobre el que va a trabajar nuestro algoritmo
    String sigma; // la cadena que será analizada por el léxico
    boolean pasoPorEdoAcep; //bandera para saber si el automata paso por el edoacep
    int indexCharAct; //este int va a llevar la cuenta de en que caracter del string vamos
    //lexema: subdcadena que sea reconocida
    int index_begin_lex;
    int index_last_lex;
    String yytext; // es la cadenita que ya hayamos reconocido como un lexema

    public AnalizadorLexico(){
        sigma="";
        AFD_lexic=null;
        pasoPorEdoAcep=false;
        indexCharAct = -1;
        index_begin_lex=-1;
        index_last_lex = -1;
    }

    public AnalizadorLexico(String rutaArchivo) throws FileNotFoundException {
        File afdFile = new File(rutaArchivo);
        Scanner afdReader = new Scanner(afdFile);

        int n = Integer.parseInt(afdReader.next());

        int[] infAlfabeto = new int[256];
        for (int i = 0; i < 256; i++) {
            infAlfabeto[i] = Integer.parseInt(afdReader.next());
        }

        //lo que hacemos aquí es definir el afd que vamos a leer a partir de nuestra clase AFD
        AFD_lexic = new AFD();
        AFD_lexic.setNumEdos(n);
        for(int i = 0; i<=255;i++){
            if (infAlfabeto[i] != -1) { //significa que ese carácter si está definido en nuestro afd
                AFD_lexic.getAlfabeto().add((char) i);
            }
        }
        AFD_lexic.setEstadosAFD(new ArrayList<Estado>());
        for (int i = 0; i < n; i++) { //n representa el numero de estados
            int edoInicial = afdReader.next().charAt(1) - '0';
            char sm1 = afdReader.next().charAt(0);
            char sm2 = afdReader.next().charAt(0);
            int edoDestino = afdReader.next().charAt(1) - '0';
            if (AFD_lexic.getEstadosAFD().get(edoInicial) == null)
                AFD_lexic.getEstadosAFD().add(new Estado());

            if (AFD_lexic.getEstadosAFD().get(edoDestino) == null)
                AFD_lexic.getEstadosAFD().add(new Estado());

            Estado destino = AFD_lexic.getEstadosAFD().get(edoDestino);
            AFD_lexic.getEstadosAFD().get(edoInicial).setTransicion(new Transicion(sm1,sm2,destino));

        }
        sigma="";
        pasoPorEdoAcep=false;
        indexCharAct=-1;
        index_begin_lex=-1;
        index_last_lex=-1;

    }

    public AnalizadorLexico(String rutaArchivo, String cadenaAnalizar) throws FileNotFoundException {
        File afdFile = new File(rutaArchivo);
        Scanner afdReader = new Scanner(afdFile);

        int n = Integer.parseInt(afdReader.next());

        int[] infAlfabeto = new int[256];
        for (int i = 0; i < 256; i++) {
            infAlfabeto[i] = Integer.parseInt(afdReader.next());
        }

        //lo que hacemos aquí es definir el afd que vamos a leer a partir de nuestra clase AFD
        AFD_lexic = new AFD();
        AFD_lexic.setNumEdos(n);
        for(int i = 0; i<=255;i++){
            if (infAlfabeto[i] != -1) { //significa que ese carácter si está definido en nuestro afd
                AFD_lexic.getAlfabeto().add((char) i);
            }
        }
        AFD_lexic.setEstadosAFD(new ArrayList<Estado>());
        for (int i = 0; i < n; i++) { //n representa el numero de estados
            int edoInicial = afdReader.next().charAt(1) - '0';
            char sm1 = afdReader.next().charAt(0);
            char sm2 = afdReader.next().charAt(0);
            int edoDestino = afdReader.next().charAt(1) - '0';
            if (AFD_lexic.getEstadosAFD().get(edoInicial) == null)
                AFD_lexic.getEstadosAFD().add(new Estado());

            if (AFD_lexic.getEstadosAFD().get(edoDestino) == null)
                AFD_lexic.getEstadosAFD().add(new Estado());

            Estado destino = AFD_lexic.getEstadosAFD().get(edoDestino);
            AFD_lexic.getEstadosAFD().get(edoInicial).setTransicion(new Transicion(sm1,sm2,destino));

        }
        sigma=cadenaAnalizar;
        indexCharAct=0;
    }

    //el analizador lexico siempre debe regresar 0 cuando ya no hay nada por revisar

    int yylex() //asi se llama en todos los compiladores, para llamarlo hay que tener ya cargado el automata y la cadena que quier revisar
    {
        pasoPorEdoAcep = false;
        index_begin_lex = indexCharAct;
        int token;
        int tokenError=1400;
        int edoAct=0;
        int lenSigma = sigma.length();
        if(indexCharAct > lenSigma){ //para verficar
            return 0;
        }

        while(indexCharAct < lenSigma){
            edoAct = AFD_lexic.getEstadosAFD().get(edoAct).
                    getTransiciones().get(0).isTransicion(sigma.charAt(indexCharAct)).getIdEstado();

            if(edoAct != -1) {//lo que significa que no es de aceptacion
                if(AFD_lexic.EdoAFD[edoAct]).Transiciones[256] != -1 ){ //es de aceptacion?
                    index_last_lex = indexCharAct;// aqui recordamos donde se dio el estado de acpetacion
                    pasoPorEdoAcep = true;
                    token = AFD_lexic.EdoAFD[edoAct].Transiciones[256];
                }
                indexCharAct++;

                if (pasoPorEdoAcep){
                    yytext = sigma.substring(index_begin_lex,index_last_lex);
                    indexCharAct = index_last_lex+1;
                    return token;
                }

                indexCharAct++; //esto lo hacemos por que en caso de que no hayamos pasado por edo de acpt y hay error pues nadamas saltar ese caracter que da problemas
                return tokenError;
            }
            return 0;
        }
    }




}
