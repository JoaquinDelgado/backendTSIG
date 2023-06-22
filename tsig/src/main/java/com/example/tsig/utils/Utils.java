package com.example.tsig.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.tsig.models.general.ModeloGeneral;
import com.example.tsig.models.ide.ModeloDireccionIde;
import com.example.tsig.models.ide.ModeloRutaKmIde;
import com.example.tsig.models.ide.ReverseIde;
import com.example.tsig.models.nominatim.ModeloDireccionNominatin;
import com.example.tsig.models.photon.ModeloDireccionPhoton;
import com.example.tsig.models.resultadocombinado.DireccionCombinada;
import com.example.tsig.models.resultadocombinado.DireccionGeoCodificador;

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

    public static ModeloGeneral direccionIdeToModeloGeneral(ModeloDireccionIde dirIde) {
        String numero = dirIde.getDireccion().getNumero() != null ? " " + dirIde.getDireccion().getNumero().getNro_puerta().toString() : "";
        String calleTemp = dirIde.getDireccion().getCalle() != null ? dirIde.getDireccion().getCalle().getNombre_normalizado() + numero : dirIde.getDireccion().getDepartamento().getNombre_normalizado() +" "+dirIde.getDireccion().getLocalidad().getNombre_normalizado();
        String calleDefinitivo = dirIde.getDireccion().getInmueble() != null ? dirIde.getDireccion().getInmueble().getNombre() + ", " + calleTemp : calleTemp;
        return new ModeloGeneral("IDE",
                calleDefinitivo,
                dirIde.getDireccion().getDepartamento().getNombre_normalizado(),
                dirIde.getDireccion().getLocalidad().getNombre_normalizado(),
                dirIde.getCodigoPostal(),
                dirIde.getPuntoY(),
                dirIde.getPuntoX());
    }

    public static ModeloGeneral rutaKmIdeToModeloGeneral(ModeloRutaKmIde rutaKmIde) {
        return new ModeloGeneral("IDE",
                rutaKmIde.getAddress(),
                rutaKmIde.getDepartamento(),
                rutaKmIde.getLocalidad(),
                rutaKmIde.getPostalCode(),
                rutaKmIde.getLat(),
                rutaKmIde.getLng());
    }

    public static ModeloGeneral direccionNominatimToModeloGeneral(ModeloDireccionNominatin dirNominatin) {
        ModeloGeneral mg = new ModeloGeneral("NOMINATIM",
                dirNominatin.getDisplay_name(),
                null,
                null,
                null,
                dirNominatin.getLat(),
                dirNominatin.getLon());
        return mg;
    }

    public static ModeloGeneral direccionPhotonToModeloGeneral(ModeloDireccionPhoton dirPhoton, int i) {
        String direccion = dirPhoton.getFeatures()[i].getProperties().getName() != null ? dirPhoton.getFeatures()[i].getProperties().getName() : dirPhoton.getFeatures()[i].getProperties().getStreet() + " "
                + dirPhoton.getFeatures()[i].getProperties().getHousenumber();

        ModeloGeneral mg = new ModeloGeneral("PHOTON",
                direccion,
                dirPhoton.getFeatures()[i].getProperties().getCity(),
                dirPhoton.getFeatures()[i].getProperties().getDistrict(),
                Integer.valueOf(dirPhoton.getFeatures()[i].getProperties().getPostcode()),
                dirPhoton.getFeatures()[i].getGeometry().getCoordinates()[1],
                dirPhoton.getFeatures()[i].getGeometry().getCoordinates()[0]);
        return mg;

    }

     public static int levenshtein(String str1, String str2, int cost_ins, int cost_rep, int cost_del) {
        int m = str1.length()-1;
        int n = str2.length()-1;

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i * cost_del;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j * cost_ins;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : cost_rep;
                dp[i][j] = Math.min(dp[i - 1][j - 1] + cost, Math.min(dp[i][j - 1] + cost_ins, dp[i - 1][j] + cost_del));
            }
        }

        return dp[m][n];
    }

    public static boolean isEqualsDirModeloGeneral(ModeloGeneral mg1, DireccionCombinada mg2) {
        
        if(mg1.getGeoCoder().equalsIgnoreCase("NOMINATIM")){
          String departamento = "";  
          String[] matches = mg1.getNombreNormalizado().split(",\\s*");
            if (matches.length >= 3) {
                departamento = matches[matches.length - 3].trim();
            }
            if(!departamento.equalsIgnoreCase(mg2.getDepartamento())){
                return false;
            } 
        }

        if(mg1.getDepartamento()!=null && !mg1.getDepartamento().equalsIgnoreCase(mg2.getDepartamento())){
            return false;
        }


        boolean distance0 = false;
        String direccion1 ="";
        if(mg1.getGeoCoder().equalsIgnoreCase("PHOTON"))
            direccion1 = RemplazarTildesYEspacios(mg1.getNombreNormalizado()).toUpperCase().replace(".", "");
        if(mg1.getGeoCoder().equalsIgnoreCase("NOMINATIM")){
            direccion1 = RemplazarTildesYEspacios(mg1.getNombreNormalizado()).toUpperCase().replace(".", "");
            // Definir la expresión regular
            String regex = ".*(?=,\\s*[^,]*$)";
            // Crear un objeto Pattern
            Pattern pattern = Pattern.compile(regex);

            // Crear un objeto Matcher y realizar la búsqueda
            Matcher matcher = pattern.matcher(direccion1);

            // Obtener el resultado
            String resultado = "";
            if (matcher.find()) {
                resultado = matcher.group();
                matcher = pattern.matcher(resultado);
            }
            if (matcher.find()) {
                resultado = matcher.group();
                matcher = pattern.matcher(resultado);
            }
            if (matcher.find()) {
                resultado = matcher.group();
                matcher = pattern.matcher(resultado);
            }
            if (matcher.find()) {
                resultado = matcher.group();
                matcher = pattern.matcher(resultado);
            }
            String calle = resultado.substring(resultado.lastIndexOf(",") + 2);
            String numeros="";
            if (matcher.find()) {
                numeros = matcher.group();
            }

            // Imprimir el resultado
            direccion1 = calle + "+" + numeros;

            if(numeros==null || numeros.isEmpty()){
                return false;
            }
            String regex2 = "^[0-9,]+$";
            if(!Pattern.matches(regex2, numeros)){
                return false;
            }
        }    
        
        String direccion2 = RemplazarTildesYEspacios(mg2.getNombreNormalizado()).toUpperCase().replace(".", "");

        if(direccion1.length()<direccion2.length()){
            distance0 = Utils.levenshtein(direccion1, direccion2, 0,1,1)==0;
        } else{
            distance0 = Utils.levenshtein(direccion2, direccion1, 0,1,1)==0;
        }

        return distance0;

    }

    public static DireccionCombinada reverseIdeToModeloGeneral(ReverseIde reverseIde) {

        DireccionGeoCodificador dirIde = new DireccionGeoCodificador();
        dirIde.setCodPostal(reverseIde.getPostalCode());
        dirIde.setLatitud(reverseIde.getLat());
        dirIde.setLongitud(reverseIde.getLng());
        dirIde.setNombreNormalizado(reverseIde.getAddress());
        dirIde.setLocalidad(reverseIde.getLocalidad());

        DireccionCombinada res =  new DireccionCombinada();
        res.setNombreNormalizado(reverseIde.getAddress());
        res.setDepartamento(reverseIde.getDepartamento());
        res.setGeoCoders(new String[]{"ide"});
        res.setIDE(dirIde);
        res.setNominatim(null);
        res.setPhoton(null);

        return res;
    }

}

