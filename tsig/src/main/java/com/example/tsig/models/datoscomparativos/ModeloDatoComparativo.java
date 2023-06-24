package com.example.tsig.models.datoscomparativos;

import lombok.Data;

@Data
public class ModeloDatoComparativo {
    private String direccionIM;
    private Double latitudIM;
    private Double longitudIM;
    private String geocoder;
    private String direccionGeocoder;
    private Double latitudGeocoder;
    private Double longitudGeocoder;
    private Double distanciaMetros;
}
