package automaton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class AnalizadorLexico {
    AFD AFD_lexic; //este es el afd sobre el que va a trabajar nuestro algoritmo
    String sigma; // la cadena que sera analizada por el lexico
    boolean pasoPorEdoAcep; //bandera ara saber si el automata paso por el edoacep
    int indexCaracterAct; //este int va a llevar la cuenta de en que caracter del string vamos
    //lexema: subdcadena que sea reconocida
    int index_begin_lex;
    int index_last_lex;

    /*
    boolean PasoPorEdoAcep; //bandera ara saber si el automata paso por el edoacep
    int indexCaracterAct; //este int va a llevar la cuenta de en que caracter del string vamos
    //lexema: subdcadena que sea reconocida
    int Index_begin_lex;
    int Index_last_lex;
    Estas variabels son las del status del analizador lexico
    * */

    public AnalizadorLexico(){
        sigma="";
        AFD_lexic=null;
        pasoPorEdoAcep=false;
        indexCaracterAct = -1;
        index_begin_lex=0;
        index_last_lex = 0;
    }
    //cargamos el automata desde un archivo
    public AnalizadorLexico(String rutaArchivo){
        try {
            //poner la lectura del archivo

        } catch (Exception e) {

        }
    }



}
