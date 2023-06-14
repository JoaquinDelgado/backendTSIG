package com.example.tsig.utils;

import com.example.tsig.models.general.ModeloGeneral;
import com.example.tsig.models.ide.ModeloDireccionIde;
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
        ModeloGeneral mg = new ModeloGeneral("IDE",
                dirIde.getDireccion().getCalle()!=null ?  dirIde.getDireccion().getCalle().getNombre_normalizado() :   dirIde.getDireccion().getDepartamento().getNombre_normalizado() +" "+dirIde.getDireccion().getLocalidad().getNombre_normalizado(),
                dirIde.getDireccion().getDepartamento().getNombre_normalizado(),
                dirIde.getDireccion().getLocalidad().getNombre_normalizado(),
                dirIde.getCodigoPostal(),
                dirIde.getPuntoY(),
                dirIde.getPuntoX());
        return mg;
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
