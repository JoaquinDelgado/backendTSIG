package com.example.tsig.models.ide;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Direccion {
        private Departamento departamento;
        private Localidad localidad;
        private Calle calle;
        private Numero numero;
        private Integer manzana;
        private Integer solar;
        private Inmueble inmueble;
    }