package com.example.tsig.models.ide;

import lombok.Getter;
import lombok.Setter;

    @Getter
    @Setter
    public class Direccion {
        private Departamento departamento;
        private Localidad localidad;
        private Calle calle;
        private Numero numero;
        private Integer manzana;
        private Integer solar;

        public Direccion() {
        }

    }