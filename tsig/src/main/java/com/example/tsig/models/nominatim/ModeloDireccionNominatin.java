package com.example.tsig.models.nominatim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloDireccionNominatin {

    private Integer place_id;
    private Double lat;
    private Double lon;
    private String display_name;
    
    public ModeloDireccionNominatin() {
    }
 
}
