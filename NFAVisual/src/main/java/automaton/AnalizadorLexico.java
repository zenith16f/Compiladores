package automaton;

//agregar el ID a EdoAFD

public class AnalizadorLexico {
    AFD AFD_lexic; //este es el afd sobre el que va a trabajar nuestro algoritmo
    String sigma; // la cadena que sera analizada por el lexico
    boolean pasoPorEdoAcep; //bandera ara saber si el automata paso por el edoacep
    int indexCharAct; //este int va a llevar la cuenta de en que caracter del string vamos
    //lexema: subdcadena que sea reconocida
    int index_begin_lex;
    int index_last_lex;
    String yytext; // es la cadenita que ya hayamos reconocido como un lexema

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
        indexCharAct = -1;
        index_begin_lex=-1;
        index_last_lex = -1;
    }



    //cargamos el automata desde un archivo
    public AnalizadorLexico(String rutaArchivo){
        //vamos a leer  el afd de archivo y de ahi sacaraemos numero de estados
        //revisar fotos
        int n;
        int infAlfabeto[] = new int[256];

        //lo que hacemos aqui es definir el afd que vamos a leer apartir de nuestra clase AFD
        AFD_lexic = new AFD();
        for(int i = 0; i<=255;i++){
            if (infAlfabeto[i] != -1) { //significa que ese caracter si esta definido en nuestro afd
                AFD_lexic.Alfabeto.add(char[i]);
            }
        }
        AFD_lexic.numEdos = n;
        AFD_lexic.EdosAFD = new EdoAFD[n];

        for (int i = 0; i < n; i++) { //n representa el numero de estados
            AFD_lexic.EdosAFD[i].idEDO = i;
            AFD_lexic.EdosAFD[i].Transiciones = 0; //aqui hay que leer cada renglon de nuestro archivo para vaciar las transiciones aqui
        }
        sigma="";
        pasoPorEdoAcep=false;
        indexCharAct=-1;
        index_begin_lex=-1;
        index_last_lex=-1;

    }

    public AnalizadorLexico(String rutaArchivo, String cadenaAnalizar){
        //vamos a leer  el afd de archivo y de ahi sacaraemos numero de estados
        //revisar fotos
        int n;
        int infAlfabeto[] = new int[256];

        //lo que hacemos aqui es definir el afd que vamos a leer apartir de nuestra clase AFD
        AFD_lexic = new AFD();
        for(int i = 0; i<=255;i++){
            if (infAlfabeto[i] != -1) { //significa que ese caracter si esta definido en nuestro afd
                AFD_lexic.Alfabeto.add();//
            }
        }
        AFD_lexic.numEdos = n;
        AFD_lexic.EdoAFD = new EdoAFD[n];

        for (int i = 0; i < n; i++) { //n representa el numero de estados
            AFD_lexic.EdoAFD[i].idEDO = i;
            AFD_lexic.EdoAFD[i].Transiciones = 0; //aqui hay que leer cada renglon de nuestro archivo para vaciar las transiciones aqui
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
            edoAct = AFD_lexic.EdoAFD[edoAct].
            Transiciones[sigma.charAt(indexCharAct)];

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
