package com.example.tsig.utils;

import com.example.tsig.models.general.ModeloGeneral;
import com.example.tsig.models.ide.ModeloDireccionIde;
import com.example.tsig.models.ide.ModeloRutaKmIde;
import com.example.tsig.models.nominatim.ModeloDireccionNominatin;
import com.example.tsig.models.photon.ModeloDireccionPhoton;

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

    public static ModeloGeneral direccionPhotonToModeloGeneral(ModeloDireccionPhoton dirPhoton) {
        ModeloGeneral mg = new ModeloGeneral("PHOTON",
                dirPhoton.getFeatures()[0].getProperties().getStreet() + " "
                        + dirPhoton.getFeatures()[0].getProperties().getHousenumber(),
                dirPhoton.getFeatures()[0].getProperties().getCity(),
                dirPhoton.getFeatures()[0].getProperties().getDistrict(),
                Integer.valueOf(dirPhoton.getFeatures()[0].getProperties().getPostcode()),
                dirPhoton.getFeatures()[0].getGeometry().getCoordinates()[1],
                dirPhoton.getFeatures()[0].getGeometry().getCoordinates()[0]);
        return mg;

    }

}
