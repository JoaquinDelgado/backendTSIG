package com.example.tsig.models.resultadocombinado;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DireccionCombinada {

    private String [] geoCoders;
    private String nombreNormalizado;
    private String departamento;

    private DireccionGeoCodificador IDE;
    private DireccionGeoCodificador Photon;
    private DireccionGeoCodificador Nominatim;
    
}
