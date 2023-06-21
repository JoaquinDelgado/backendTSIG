package com.example.tsig.models.ide;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReverseIde {
    private String address;
    private String postalCode;
    private String departamento;
    private String localidad;
    private Double lat;
    private Double lng;
}
