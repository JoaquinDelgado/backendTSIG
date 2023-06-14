package com.example.tsig.models.general;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModeloGeneral {

    private String geoCoder;
    private String nombreNormalizado;
    private String departamento;
    private String localidad;
    private Integer codigoPostal;
    private Double latitud;
    private Double longitud;
    
}
