package com.example.tsig.models.ide;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloRutaKmIde {
    private String address;
    private Integer postalCode;
    private String localidad;
    private String departamento;
    private Double lat;
    private Double lng;
}
