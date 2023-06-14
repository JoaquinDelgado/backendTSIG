package com.example.tsig.models.ide;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloDireccionIde {
    private Direccion direccion;
    private Integer codigoPostal;
    private Integer codigoPostalAmpliado;
    private Float puntoX;
    private Float puntoY;
    private Integer idPunto;
    private Integer srid;
    private Integer idTipoClasificacion;
    private String error;
    
    public ModeloDireccionIde() {
    }

    

}
