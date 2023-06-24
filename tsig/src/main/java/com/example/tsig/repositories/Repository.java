package com.example.tsig.repositories;

import com.example.tsig.models.datoscomparativos.ModeloDatoComparativo;
import com.example.tsig.models.im.DireccionIM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Repository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void insertarCoordenadas(String input, String geoCoder, Double lat, Double lon) {
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

    public void insertarBusquedaEnCache(Map<String, String> params) {

        String queryInsertar = "INSERT INTO public.cache_busqueda " +
                "(id_geocoder, id_canonic_form, calle, numero, localidad, departamento, calle2, manzana, solar, nombre_inmueble, numeroruta, kilometro, response, fecha_creado) "
                +
                "VALUES " +
                "(:id_geocoder, :id_canonic_form, :calle, :numero, :localidad, :departamento, :calle2, :manzana, :solar, :nombre_inmueble, :numeroruta, :kilometro, :response, CURRENT_TIMESTAMP) ";
        String sql = reemplarParametrosQueryCacheBusqueda(queryInsertar, params, true);
        int rows = jdbcTemplate.update(sql);
        if (rows > 0) {
            System.out.println("A new row has been inserted.");
        }
    }

    public String obtenerBusquedaDeCache(Map<String, String> params) {
        String queryObtener = "SELECT response FROM public.cache_busqueda " +
                "WHERE id_geocoder = :id_geocoder " +
                "AND id_canonic_form = :id_canonic_form " +
                "AND calle = :calle " +
                "AND numero = :numero " +
                "AND localidad = :localidad " +
                "AND departamento = :departamento " +
                "AND calle2 = :calle2 " +
                "AND manzana = :manzana " +
                "AND solar = :solar " +
                "AND nombre_inmueble = :nombre_inmueble " +
                "AND numeroruta = :numeroruta " +
                "AND kilometro = :kilometro ";
        String sql = reemplarParametrosQueryCacheBusqueda(queryObtener, params, false);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        if (rows.size() > 0) {
            return (String) rows.get(0).get("response");
        } else {
            return null;
        }
    }

    private String reemplarParametrosQueryCacheBusqueda(String query, Map<String, String> params, boolean esInsertar) {

        String caracterSeparador = esInsertar ? "," : " ";
        String[] listaParametos = { "calle", "numero", "localidad", "departamento", "calle2", "manzana", "solar",
                "nombre_inmueble", "numeroruta", "kilometro", "response", "fecha_creado" };
        query = query.replace(":id_geocoder", (String) params.get("id_geocoder"));
        query = query.replace(":id_canonic_form", (String) params.get("id_canonic_form"));
        for (int i = 0; i < listaParametos.length; i++) {
            String parametro = listaParametos[i];
            String valor = (String) params.get(parametro);
            if (valor == null) {
                valor = "";
            }
            if(!parametro.equals("response")){
                valor = valor.toUpperCase();
            }
            query = query.replace(":" + parametro + caracterSeparador,
                    "'" + valor + "'" + caracterSeparador);
        }
        return query;
    }

    public void insertarEnCacheSugerencia(String entrada, Boolean todos, String response){
        String queryInsertar = "INSERT INTO public.cache_sugerencias " +
                "(entrada, todos, response,fecha_creado) " +
                "VALUES " +
                "(:entrada, :todos, :response, CURRENT_TIMESTAMP) ";
        queryInsertar=queryInsertar.replace(":entrada", "'"+entrada.toUpperCase()+"'");  
        String todosString = todos ? "t" : "f"; 
        queryInsertar=queryInsertar.replace(":todos", "'"+todosString+"'");  
        queryInsertar=queryInsertar.replace(":response", "'"+response+"'");   
        int rows = jdbcTemplate.update(queryInsertar);
        if (rows > 0) {
            System.out.println("A new row has been inserted.");
        }
    }

    public String obtenerSugerenciaDeCache(String entrada, Boolean todos){
        String queryObtener = "SELECT response FROM public.cache_sugerencias " +
                "WHERE entrada = :entrada " +
                "AND todos = :todos ";
        queryObtener=queryObtener.replace(":entrada", "'"+entrada.toUpperCase()+"'");  
        String todosString = todos ? "t" : "f"; 
        queryObtener=queryObtener.replace(":todos", "'"+todosString+"'");  
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(queryObtener);
        if (rows.size() > 0) {
            return (String) rows.get(0).get("response");
        } else {
            return null;
        }
    }

    public Map<Integer, DireccionIM> obtenerDireccionesIM() {
        Map<Integer, DireccionIM> direccionesIM = new HashMap<>();
        String sql = "SELECT * FROM DIRECCIONES_IM";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        // Procesar los resultados
        for (Map<String, Object> row : rows) {
            DireccionIM direccionIM = new DireccionIM();
            direccionIM.setId((Integer) row.get("id"));
            direccionIM.setCalle((String) row.get("calle"));
            direccionIM.setNumero((Integer) row.get("numero"));
            direccionIM.setLatitud((Double) row.get("latitud"));
            direccionIM.setLongitud((Double) row.get("longitud"));
            direccionesIM.put((Integer) row.get("id"), direccionIM);
        }
        return direccionesIM;
    }

    public void insertarDistancia(Integer idElemento, Integer idGeocoder, String nombreNormalizado,Double lat, Double lon, Float distancia) {
        String sql = "INSERT INTO distancias_im_geocoders (id_direcciones_im,id_geocoder,dir_geocoder,latitud_geocoder,longitud_geocoder, distancia_metros) VALUES (?,?,?,?,?,?)";
        int rows = jdbcTemplate.update(sql, idElemento, idGeocoder,nombreNormalizado, lat, lon, distancia);
        if (rows > 0) {
            System.out.println("A new row has been inserted.");
        }
    }

    public List<ModeloDatoComparativo> obtenerDatosComparativos(){
        List<ModeloDatoComparativo> datos = new ArrayList<>();
        String sql = "SELECT * FROM vista_datos_comparativos";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : rows) {
            ModeloDatoComparativo dato = new ModeloDatoComparativo();
            dato.setDireccionGeocoder((String) row.get("direccion_geocoder"));
            dato.setDireccionIM((String) row.get("direccion_im"));
            dato.setDistanciaMetros((Double) row.get("distancia_metros"));
            dato.setLatitudGeocoder((Double) row.get("latitud_geocoder"));
            dato.setLongitudGeocoder((Double) row.get("longitud_geocoder"));
            dato.setLatitudIM((Double) row.get("latitud_im"));
            dato.setLongitudIM((Double) row.get("longitud_im"));
            dato.setGeocoder((String) row.get("geocoder"));
            datos.add(dato);
        }
        return datos;
    }



}
