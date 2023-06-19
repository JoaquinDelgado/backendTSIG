package com.example.tsig.models.photon;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Properties {
    private String  country;
    private String city;
    private String postcode;
    private String housenumber;
    private String street;
    private String name;
    private String district;
    private String state;
}