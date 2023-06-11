package com.example.tsig.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Repository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void insertarCoordenadas(String input, String geoCoder, Double lat, Double lon){
        String sql = "INSERT INTO audits (input,geocoder,latitud,longitud) VALUES (?,?,?,?)";
        int rows = jdbcTemplate.update(sql, input, geoCoder, lat, lon);
        if (rows > 0) {
            System.out.println("A new row has been inserted.");
        }
    }

    public Map<Integer, String> obtenerFormasCanonicas() {
        Map<Integer, String> formasCanonicas = new HashMap<>();
        String sql = "SELECT * FROM canonic_forms";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        // Procesar los resultados
        for (Map<String, Object> row : rows) {
            formasCanonicas.put((Integer) row.get("id"), (String) row.get("canonic_form"));
        }
        return formasCanonicas;
    }

    public Map<Integer, String> obtenerGeoCoders(Integer idFormaCanonica) {
        Map<Integer, String> geoCoders = new HashMap<>();
        String sql = "SELECT g.ID, g.GEOCODER FROM GEOCODERS g INNER JOIN geocoders_canonic_forms gcf ON gcf.id_geocoder = g.id WHERE gcf.id_canonic_form = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, idFormaCanonica);

        // Procesar los resultados
        for (Map<String, Object> row : rows) {
            geoCoders.put((Integer) row.get("id"), (String) row.get("geocoder"));
        }
        return geoCoders;
    }

    public void insertarBusquedaEnCahe(Map<String,String> params) {

        String queryInsertar = 
        "INSERT INTO public.cache_busqueda "+
            "(id_geocoder, id_canonic_form, calle, numero, localidad, departamento, calle2, manzana, solar, nombre_inmueble, numeroruta, kilometro, response, fecha_creado) "+
            "VALUES "+
            "(:id_geocoder, :id_canonic_form, :calle, :numero, :localidad, :departamento, :calle2, :manzana, :solar, :nombre_inmueble, :numeroruta, :kilometro, :response, CURRENT_TIMESTAMP) ";
        String sql = reemplarParametrosQueryCacheBusqueda(queryInsertar, params);
        System.out.println("sql: "+sql);
        int rows = jdbcTemplate.update(sql);
		if (rows > 0) {
			System.out.println("A new row has been inserted.");
		}
    }


    private String reemplarParametrosQueryCacheBusqueda(String query, Map<String,String> params){

        String [] listaParametos= {"calle", "numero", "localidad", "departamento", "calle2", "manzana", "solar", "nombre_inmueble", "numeroruta", "kilometro", "response", "fecha_creado"};
        query = query.replace(":id_geocoder",(String) params.get("id_geocoder"));
        query = query.replace(":id_canonic_form",(String) params.get("id_canonic_form"));
        for(int i=0;i<listaParametos.length;i++) {
            String parametro = listaParametos[i];
            String valor = (String) params.get(parametro);
            if(valor==null) {
                valor = "";
            }   
            query = query.replace(":"+parametro+",", "'"+valor.toUpperCase()+"',");
        }
        return query;


    }
}
