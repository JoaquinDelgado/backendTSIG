package com.example.tsig.cache;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.tsig.repositories.Repository;

@Component
public class TsigCache {

    @Autowired
    private Repository repository;

    public void insertarEnCache(Map<String,String> params) {
        repository.insertarBusquedaEnCache(params);
    }

    public String obtenerDeCache(Map<String,String> params) {
        return repository.obtenerBusquedaDeCache(params);
    }

    public void insertarEnCacheSugerencia(String entrada, Boolean todos, String respuesta) {
        repository.insertarEnCacheSugerencia(entrada, todos, respuesta);
    }

    public String obtenerDeCacheSugerencia(String entrada, Boolean todos) {
        return repository.obtenerSugernciaDeCache(entrada, todos);
    }

    
}