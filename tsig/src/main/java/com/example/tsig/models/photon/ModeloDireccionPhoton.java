package com.example.tsig.models.photon;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloDireccionPhoton {

    private Feature [] features;
    
}
