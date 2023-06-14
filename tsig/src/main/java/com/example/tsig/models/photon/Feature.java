package com.example.tsig.models.photon;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {

    private Geometry geometry;
    private Properties properties;
    
}
