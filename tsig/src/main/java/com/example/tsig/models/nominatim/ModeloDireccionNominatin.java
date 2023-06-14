package com.example.tsig.models.nominatim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloDireccionNominatin {

    private Integer place_id;
    private Float lat;
    private Float lon;
    private String display_name;
    
    public ModeloDireccionNominatin() {
    }
 
}
