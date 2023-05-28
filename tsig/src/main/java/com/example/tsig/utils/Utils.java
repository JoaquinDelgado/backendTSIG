package com.example.tsig.utils;

public class Utils {

    public static String RemplazarTildesYEspacios(String palabra) {
        palabra = palabra.replaceAll(" ", "+");
        palabra = palabra.replaceAll("á|Á", "a");
        palabra = palabra.replaceAll("é|É", "e");
        palabra = palabra.replaceAll("í|Í", "i");
        palabra = palabra.replaceAll("ó|Ó", "o");
        palabra = palabra.replaceAll("ú|Ú", "u");
        palabra = palabra.replaceAll("ñ|Ñ", "n");
        return palabra;
    }

}
