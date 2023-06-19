package com.example.tsig.models.resultadocombinado;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DireccionGeoCodificador {

    private String nombreNormalizado;
    String localidad;
    String codPostal;
    Double latitud;
    Double longitud;
    
}
