package com.example.tsig.cache;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.example.tsig.repositories.Repository;

@Component
public class TsigCache {

    @Autowired
    private Repository repository;

    public void insertarEnCahe(Map<String,String> params) {
        repository.insertarBusquedaEnCahe(params);
    }
    
}