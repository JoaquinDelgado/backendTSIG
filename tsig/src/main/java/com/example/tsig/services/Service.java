package com.example.tsig.services;

import com.example.tsig.cache.TsigCache;
import com.example.tsig.models.general.ModeloGeneral;
import com.example.tsig.models.ide.ModeloDireccionIde;
import com.example.tsig.models.ide.ModeloRutaKmIde;
import com.example.tsig.models.nominatim.ModeloDireccionNominatin;
import com.example.tsig.models.photon.ModeloDireccionPhoton;
import com.example.tsig.repositories.Repository;
import com.example.tsig.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class Service {

    @Autowired
    Repository repository;

    @Autowired
    TsigCache tsigCache;

    public Map<Integer, String> formasCanonicas() {
        return repository.obtenerFormasCanonicas();
    }

    public Map<Integer, String> obtenerGeoCoders(Integer idFormaCanonica) {
        return repository.obtenerGeoCoders(idFormaCanonica);
    }

    public ResponseEntity<?> busquedaDireccionEstructurada(Integer idGeoCoder,
            Integer idFormaCanonica,
            String calle, String numero,
            String localidad,
            String departamento,
            String calle2, String manzana,
            String solar, String nombreInmueble,
            Integer numeroRuta, Double kilometro) throws JsonProcessingException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        // Construir los parámetros
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String jsonString;
        String nominatimJsonString = "";
        String photonJsonString = "";
        String input;
        List<Map<String, Object>> listaObjetos;
        ObjectMapper objectMapper = new ObjectMapper();
        String calleGeoCoder;
        UriComponentsBuilder builder;
        String fullUrl;
        HttpEntity<String> entity;
        ResponseEntity<String> response;
        String url;
        if (idGeoCoder == 0) {
            switch (idFormaCanonica) {
                case 1 -> {
                    if (calle == null) {
                        return new ResponseEntity<>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    calleGeoCoder = calle + (numero != null ? " " + numero : "");

                    params.add("calle", calleGeoCoder);
                    params.add("localidad", localidad);
                    params.add("departamento", departamento);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "1");
                    datos.put("calle", calle);
                    datos.put("numero", numero);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {});
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity<>(resultado, headers, HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    entity = new HttpEntity<>(headers);
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                    input = "calleGeoCoder:" + calleGeoCoder
                            + (!Objects.equals(localidad, "") ? ";localidad:" + localidad : "");
                    input = input + (!Objects.equals(departamento, "") ? ";departamento:" + departamento : "");
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Agrego response para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionIde>>() {});
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        String error = (String) objeto.get("error");
                        Double lon = (Double) objeto.get("puntoX");
                        Double lat = (Double) objeto.get("puntoY");
                        if (error.isEmpty()) {
                            repository.insertarCoordenadas(input, "IDE", lat, lon);
                        }
                    }
                    List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                    for(ModeloDireccionIde ide : res){
                        resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                    }
                    return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                }
                case 2 -> {
                    if (calle == null) {
                        return new ResponseEntity<>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    if (calle2 == null) {
                        return new ResponseEntity<>("Calle2 no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    calleGeoCoder = calle + (numero != null ? " " + numero : "") + " esquina " + calle2;
                    // Construir la URL con los parámetros
                    params.add("calle", calleGeoCoder);
                    params.add("localidad", localidad);
                    params.add("departamento", departamento);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "2");
                    datos.put("calle", calle);
                    datos.put("numero", numero);
                    datos.put("calle2", calle2);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {});
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);

                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                    input = "calleGeoCoder:" + calleGeoCoder
                            + (!Objects.equals(localidad, "") ? ";localidad:" + localidad : "");
                    input = input + (!Objects.equals(departamento, "") ? ";departamento:" + departamento : "");
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Agrego response para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionIde>>() {});
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        String error = (String) objeto.get("error");
                        Double lon = (Double) objeto.get("puntoX");
                        Double lat = (Double) objeto.get("puntoY");
                        if (error.isEmpty()) {
                            repository.insertarCoordenadas(input, "IDE", lat, lon);
                        }
                    }
                    List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                    for(ModeloDireccionIde ide : res){
                        resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                    }
                    return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                }
                case 3 -> {
                    if (departamento == null) {
                        return new ResponseEntity<String>("Departamento no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    if (localidad == null) {
                        return new ResponseEntity<String>("Localidad no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    if (manzana == null) {
                        return new ResponseEntity<>("Manzana no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    if (solar == null) {
                        return new ResponseEntity<>("Solar no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    calleGeoCoder = manzana + " " + solar;
                    params.add("calle", calleGeoCoder);
                    params.add("localidad", localidad);
                    params.add("departamento", departamento);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "3");
                    datos.put("manzana", manzana);
                    datos.put("solar", solar);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {});
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);

                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                    input = "calleGeoCoder:" + calleGeoCoder + (!localidad.equals("") ? ";localidad:" + localidad : "");
                    input = input + (!departamento.equals("") ? ";departamento:" + departamento : "");
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Agrego response para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionIde>>() {});
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        String error = (String) objeto.get("error");
                        Double lon = (Double) objeto.get("puntoX");
                        Double lat = (Double) objeto.get("puntoY");
                        if (error.isEmpty()) {
                            repository.insertarCoordenadas(input, "IDE", lat, lon);
                        }
                    }
                    List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                    for(ModeloDireccionIde ide : res){
                        resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                    }
                    return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                }
                case 4 -> {
                    // IDE
                    if (nombreInmueble == null) {
                        return new ResponseEntity<>("nombreInmueble no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    params.add("calle", nombreInmueble);
                    params.add("departamento", departamento);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "4");
                    datos.put("nombre_inmueble", nombreInmueble);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    List<ModeloGeneral> ideResultado = new ArrayList<>();
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {});
                        for(ModeloDireccionIde ide : res){
                            ideResultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                    } else {
                        System.out.println("Cache No");
                        url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
                        builder = UriComponentsBuilder.fromHttpUrl(url)
                                .queryParams(params);
                        fullUrl = builder.toUriString();

                        // Crear una entidad HttpEntity con los encabezados
                        entity = new HttpEntity<>(headers);
                        // Obtener la respuesta del servicio externo
                        response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                        input = "nombreInmueble:" + nombreInmueble + (localidad != "" ? ";localidad:" + localidad : "");
                        input = input + (departamento != "" ? ";departamento:" + departamento : "");
                        // Obtener la respuesta de ResponseEntity
                        jsonString = response.getBody();

                        // Agrego response para guardar en cache
                        datos.put("response", jsonString);
                        tsigCache.insertarEnCache(datos);

                        // Crear un ObjectMapper de Jackson
                        objectMapper = new ObjectMapper();
                        List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<>() {
                        });
                        // Analizar la cadena de texto JSON en una lista de objetos Java
                        listaObjetos = objectMapper.readValue(jsonString,
                                new TypeReference<List<Map<String, Object>>>() {
                                });
                        // Recorrer la lista de objetos
                        for (Map<String, Object> objeto : listaObjetos) {
                            // Acceder a los atributos de cada objeto
                            String error = (String) objeto.get("error");
                            Double lon = (Double) objeto.get("puntoX");
                            Double lat = (Double) objeto.get("puntoY");
                            if (error.isEmpty()) {
                                repository.insertarCoordenadas(input, "IDE", lat, lon);
                            }
                        }
                        for(ModeloDireccionIde ide : res){
                            ideResultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                    }

                    // NOMINATIM
                    // Construir la URL con los parámetros
                    url = "https://nominatim.openstreetmap.org/search";
                    nombreInmueble = Utils.RemplazarTildesYEspacios(nombreInmueble);
                    departamento = Utils.RemplazarTildesYEspacios(departamento);
                    params.add("q", nombreInmueble + "+" + departamento);
                    params.add("format", "json");

                    datos = new HashMap<>();
                    datos.put("id_geocoder", "2");
                    datos.put("id_canonic_form", "4");
                    datos.put("nombre_inmueble", nombreInmueble);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    cache = tsigCache.obtenerDeCache(datos);
                    List<ModeloGeneral> nominatimResultado = new ArrayList<ModeloGeneral>();
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionNominatin> myObjects = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionNominatin>>() {});
                        for(ModeloDireccionNominatin  nom : myObjects){
                            nominatimResultado.add(Utils.direccionNominatimToModeloGeneral(nom));
                        }
                    } else {
                        System.out.println("Cache No");
                        builder = UriComponentsBuilder.fromHttpUrl(url)
                                .queryParams(params);
                        fullUrl = builder.toUriString();

                        // Crear una entidad HttpEntity con los encabezados
                        entity = new HttpEntity<>(headers);
                        // Hacer la solicitud GET al servicio externo

                        // Obtener la respuesta del servicio externo
                        ResponseEntity<String> responseNominatim = restTemplate.exchange(fullUrl, HttpMethod.GET,
                                entity,
                                String.class);

                        input = "nombreInmueble:" + nombreInmueble;
                        input = input + (departamento != "" ? ";departamento:" + departamento : "");
                        // Obtener la respuesta de ResponseEntity
                        nominatimJsonString = responseNominatim.getBody();

                        // Agrego response para guardar en cache
                        datos.put("response", nominatimJsonString);
                        tsigCache.insertarEnCache(datos);

                        // Crear un ObjectMapper de Jackson
                        objectMapper = new ObjectMapper();
                        // Analizar la cadena de texto JSON en una lista de objetos Java
                        listaObjetos = objectMapper.readValue(nominatimJsonString, new TypeReference<>() {
                        });
                        // Recorrer la lista de objetos
                        for (Map<String, Object> objeto : listaObjetos) {
                            // Acceder a los atributos de cada objeto
                            Double lon = Double.parseDouble((String) objeto.get("lon"));
                            Double lat = Double.parseDouble((String) objeto.get("lat"));
                            repository.insertarCoordenadas(input, "NOMINATIN", lat, lon);
                        }
                        List<ModeloDireccionNominatin> myObjects = objectMapper.readValue(nominatimJsonString, new TypeReference<>() {
                        });
                        for(ModeloDireccionNominatin  nom : myObjects){
                            nominatimResultado.add(Utils.direccionNominatimToModeloGeneral(nom));
                        }
                    }

                    // RESPONSE FINAL
                    // Convertir JSON1 a objeto Java
                    List<ModeloGeneral> concatenatedList = new ArrayList<>();
                    concatenatedList.addAll(ideResultado);
                    concatenatedList.addAll(nominatimResultado);
                    return new ResponseEntity<>(concatenatedList, headers, HttpStatus.OK);
                }
                case 5 -> {
                    if (numeroRuta == null) {
                        return new ResponseEntity<String>("numeroRuta no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    if (kilometro == null) {
                        return new ResponseEntity<String>("kilometro no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    params.add("km", kilometro.toString());
                    params.add("ruta", numeroRuta.toString());

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "5");
                    datos.put("numeroruta", numeroRuta.toString());
                    datos.put("kilometro", kilometro.toString());

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloRutaKmIde> res = objectMapper.readValue(cache, new TypeReference<>() {
                        });
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloRutaKmIde ide : res){
                            resultado.add(Utils.rutaKmIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity<>(resultado, headers, HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    // Construir la URL con los parámetros
                    url = "https://direcciones.ide.uy/api/v1/geocode/rutakm";
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                    input = "numeroRuta:" + numeroRuta + ";kilometro:" + kilometro;
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Agrego response para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    List<ModeloRutaKmIde> res = objectMapper.readValue(jsonString, new TypeReference<>() {
                    });
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        Double lon = (Double) objeto.get("lon");
                        Double lat = (Double) objeto.get("lat");
                        repository.insertarCoordenadas(input, "IDE", lat, lon);
                    }
                    List<ModeloGeneral> resultado = new ArrayList<>();
                    for(ModeloRutaKmIde ide : res){
                        resultado.add(Utils.rutaKmIdeToModeloGeneral(ide));
                    }
                    return new ResponseEntity<>(resultado, headers, HttpStatus.OK);
                }
                case 6 -> {
                    // IDE
                    if (calle == null) {
                        return new ResponseEntity<>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    calleGeoCoder = calle + (numero != null ? " " + numero : "");
                    // Construir la URL con los parámetros
                    params.add("calle", calleGeoCoder);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "6");
                    datos.put("calle", calle);
                    datos.put("numero", numero);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    List<ModeloGeneral> ideResultado = new ArrayList<>();
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {});
                        for(ModeloDireccionIde ide : res){
                            ideResultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                    } else {
                        System.out.println("Cache No");
                        url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
                        builder = UriComponentsBuilder.fromHttpUrl(url)
                                .queryParams(params);
                        fullUrl = builder.toUriString();
                        // Crear una entidad HttpEntity con los encabezados
                        entity = new HttpEntity<>(headers);

                        // Hacer la solicitud GET al servicio externo

                        // Obtener la respuesta del servicio externo
                        response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                        input = "calleGeoCoder:" + calleGeoCoder;
                        // Obtener la respuesta de ResponseEntity
                        jsonString = response.getBody();

                        // Datos para guardar en cache
                        datos.put("response", jsonString);
                        tsigCache.insertarEnCache(datos);

                        // Crear un ObjectMapper de Jackson
                        objectMapper = new ObjectMapper();
                        List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionIde>>() {});
                        // Analizar la cadena de texto JSON en una lista de objetos Java
                        listaObjetos = objectMapper.readValue(jsonString, new TypeReference<>() {
                        });
                        // Recorrer la lista de objetos
                        for (Map<String, Object> objeto : listaObjetos) {
                            // Acceder a los atributos de cada objeto
                            String error = (String) objeto.get("error");
                            Double lon = (Double) objeto.get("puntoX");
                            Double lat = (Double) objeto.get("puntoY");
                            if (error.isEmpty()) {
                                repository.insertarCoordenadas(input, "IDE", lat, lon);
                            }
                        }
                        for(ModeloDireccionIde ide : res){
                            ideResultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }

                    }

                    // NOMINATIM
                    calle = Utils.RemplazarTildesYEspacios(calle);
                    params.add("street", calle + "+" + numero);
                    params.add("format", "json");
                    url = "https://nominatim.openstreetmap.org/search";

                    // Datos para buscar en cache
                    datos = new HashMap<>();
                    datos.put("id_geocoder", "2");
                    datos.put("id_canonic_form", "6");
                    datos.put("calle", calle);
                    datos.put("numero", numero);

                    // Busco en cache
                    cache = tsigCache.obtenerDeCache(datos);
                    List<ModeloGeneral> nominatimResultado = new ArrayList<ModeloGeneral>();
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionNominatin> myObjects = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionNominatin>>() {});
                        for(ModeloDireccionNominatin  nom : myObjects){
                            nominatimResultado.add(Utils.direccionNominatimToModeloGeneral(nom));
                        }
                    } else {
                        System.out.println("Cache No");
                        builder = UriComponentsBuilder.fromHttpUrl(url)
                                .queryParams(params);
                        fullUrl = builder.toUriString();

                        // Crear una entidad HttpEntity con los encabezados
                        entity = new HttpEntity<>(headers);
                        // Hacer la solicitud GET al servicio externo

                        // Obtener la respuesta del servicio externo
                        response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

                        input = "calle:" + calle + ";numero:" + numero;
                        // Obtener la respuesta de ResponseEntity
                        nominatimJsonString = response.getBody();

                        // Datos para guardar en cache
                        datos.put("response", nominatimJsonString);
                        tsigCache.insertarEnCache(datos);

                        // Crear un ObjectMapper de Jackson
                        objectMapper = new ObjectMapper();

                        // Analizar la cadena de texto JSON en una lista de objetos Java
                        listaObjetos = objectMapper.readValue(nominatimJsonString, new TypeReference<>() {
                        });
                        // Recorrer la lista de objetos
                        for (Map<String, Object> objeto : listaObjetos) {
                            // Acceder a los atributos de cada objeto
                            Double lon = Double.parseDouble((String) objeto.get("lon"));
                            Double lat = Double.parseDouble((String) objeto.get("lat"));
                            repository.insertarCoordenadas(input, "NOMINATIN", lat, lon);
                        }
                        List<ModeloDireccionNominatin> myObjects = objectMapper.readValue(nominatimJsonString, new TypeReference<>() {
                        });
                        for(ModeloDireccionNominatin  nom : myObjects){
                            nominatimResultado.add(Utils.direccionNominatimToModeloGeneral(nom));
                        }

                    }

                    // PHOTON
                    MultiValueMap<String, String> paramsPhoton = new LinkedMultiValueMap<>();
                    paramsPhoton.add("q", calle + "+" + numero);

                    // Datos para buscar en cache
                    datos = new HashMap<>();
                    datos.put("id_geocoder", "3");
                    datos.put("id_canonic_form", "6");
                    datos.put("calle", calle);
                    datos.put("numero", numero);

                    // Busco en cache
                    cache = tsigCache.obtenerDeCache(datos);
                    List<ModeloGeneral> photonResultado = new ArrayList<ModeloGeneral>();
                    if (cache != null) {
                        System.out.println("Cache Si");
                        ModeloDireccionPhoton photonObj = objectMapper.readValue(cache, new TypeReference<>() {
                        });
                        photonResultado.add(Utils.direccionPhotonToModeloGeneral(photonObj));
                    } else {
                        System.out.println("Cache No");
                        url = "https://photon.komoot.io/api/";
                        builder = UriComponentsBuilder.fromHttpUrl(url)
                                .queryParams(paramsPhoton);
                        fullUrl = builder.toUriString();

                        // Crear una entidad HttpEntity con los encabezados
                        entity = new HttpEntity<>(headers);

                        // Obtener la respuesta del servicio externo
                        response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

                        input = "calle:" + calle + ";numero:" + numero;
                        // Obtener la respuesta de ResponseEntity
                        photonJsonString = response.getBody();

                        // Datos para guardar en cache
                        datos.put("response", photonJsonString);
                        tsigCache.insertarEnCache(datos);

                        // Crear un ObjectMapper de Jackson
                        objectMapper = new ObjectMapper();
                        // Analizar la cadena de texto JSON en un objeto Java
                        Map<String, Object> objeto = objectMapper.readValue(photonJsonString, new TypeReference<>() {
                        });

                        // Acceder a los valores del objeto
                        listaObjetos = (List<Map<String, Object>>) objeto.get("features");
                        Map<String, Object> primerObjeto = listaObjetos.get(0);
                        Map<String, Object> geometry = (Map<String, Object>) primerObjeto.get("geometry");
                        List<Double> coordenadas = (List<Double>) geometry.get("coordinates");
                        Double latitud = coordenadas.get(1);
                        Double longitud = coordenadas.get(0);
                        repository.insertarCoordenadas(input, "PHOTON", latitud, longitud);

                        ModeloDireccionPhoton photonObj = objectMapper.readValue(photonJsonString, new TypeReference<>() {
                        });
                        photonResultado.add(Utils.direccionPhotonToModeloGeneral(photonObj));
                    }

                    // RESPONSE FINAL
                    // Convertir JSON1 a objeto Java
                    List<ModeloGeneral> concatenatedList = new ArrayList<>();
                    concatenatedList.addAll(ideResultado);
                    concatenatedList.addAll(nominatimResultado);
                    concatenatedList.addAll(photonResultado);
                    return new ResponseEntity<>(concatenatedList, headers, HttpStatus.OK);

                }
                default -> {
                    return new ResponseEntity<>("IDFORMACANONICA INVALIDO", headers, HttpStatus.BAD_REQUEST);
                }
            }
        }
        if (idGeoCoder == 1) {
            url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
            switch (idFormaCanonica) {
                case 1 -> {
                    if (calle == null) {
                        return new ResponseEntity<>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    calleGeoCoder = calle + (numero != null ? " " + numero : "");
                    // Construir la URL con los parámetros
                    params.add("calle", calleGeoCoder);
                    params.add("localidad", localidad);
                    params.add("departamento", departamento);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "1");
                    datos.put("calle", calle);
                    datos.put("numero", numero);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {}); 
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();
                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);

                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

                    input = "calleGeoCoder:" + calleGeoCoder + (localidad != "" ? ";localidad:" + localidad : "");
                    input = input + (departamento != "" ? ";departamento:" + departamento : "");
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();
                    
                    objectMapper = new ObjectMapper();
                    List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionIde>>() {});    

                    // Agrego response para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        String error = (String) objeto.get("error");
                        Double lon = (Double) objeto.get("puntoX");
                        Double lat = (Double) objeto.get("puntoY");
                        if (error.isEmpty()) {
                            repository.insertarCoordenadas(input, "IDE", lat, lon);
                        }
                    }
                    
                    List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                    for(ModeloDireccionIde ide : res){
                        resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                    }
                    return new ResponseEntity<>(resultado, headers, HttpStatus.OK);
                }
                case 2 -> {
                    if (calle == null) {
                        return new ResponseEntity<>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    if (calle2 == null) {
                        return new ResponseEntity<>("Calle2 no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    calleGeoCoder = calle + (numero != null ? " " + numero : "") + " esquina " + calle2;
                    // Construir la URL con los parámetros
                    params.add("calle", calleGeoCoder);
                    params.add("localidad", localidad);
                    params.add("departamento", departamento);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "2");
                    datos.put("calle", calle);
                    datos.put("numero", numero);
                    datos.put("calle2", calle2);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {}); 
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);

                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                    input = "calleGeoCoder:" + calleGeoCoder + (localidad != "" ? ";localidad:" + localidad : "");
                    input = input + (departamento != "" ? ";departamento:" + departamento : "");
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Agrego response para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        String error = (String) objeto.get("error");
                        Double lon = (Double) objeto.get("puntoX");
                        Double lat = (Double) objeto.get("puntoY");
                        if (error.isEmpty()) {
                            repository.insertarCoordenadas(input, "IDE", lat, lon);
                        }
                    }
                        List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionIde>>() {}); 
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);

                }
                case 3 -> {
                    if (departamento == null) {
                        return new ResponseEntity<String>("Departamento no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    if (localidad == null) {
                        return new ResponseEntity<String>("Localidad no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    if (manzana == null) {
                        return new ResponseEntity<>("Manzana no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    if (solar == null) {
                        return new ResponseEntity<>("Solar no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    calleGeoCoder = manzana + " " + solar;
                    params.add("calle", calleGeoCoder);
                    params.add("localidad", localidad);
                    params.add("departamento", departamento);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "3");
                    datos.put("manzana", manzana);
                    datos.put("solar", solar);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {}); 
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);

                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                    input = "calleGeoCoder:" + calleGeoCoder + (localidad != "" ? ";localidad:" + localidad : "");
                    input = input + (departamento != "" ? ";departamento:" + departamento : "");
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Agrego response para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        String error = (String) objeto.get("error");
                        Double lon = (Double) objeto.get("puntoX");
                        Double lat = (Double) objeto.get("puntoY");
                        if (error.isEmpty()) {
                            repository.insertarCoordenadas(input, "IDE", lat, lon);
                        }
                    }
                        List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionIde>>() {}); 
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                }
                case 4 -> {
                    if (nombreInmueble == null) {
                        return new ResponseEntity<String>("nombreInmueble no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    params.add("calle", nombreInmueble);
                    params.add("localidad", localidad);
                    params.add("departamento", departamento);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "4");
                    datos.put("nombre_inmueble", nombreInmueble);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {});
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);
                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                    input = "nombreInmueble:" + nombreInmueble + (localidad != "" ? ";localidad:" + localidad : "");
                    input = input + (departamento != "" ? ";departamento:" + departamento : "");
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Agrego response para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        String error = (String) objeto.get("error");
                        Double lon = (Double) objeto.get("puntoX");
                        Double lat = (Double) objeto.get("puntoY");
                        if (error.isEmpty()) {
                            repository.insertarCoordenadas(input, "IDE", lat, lon);
                        }
                    }
                    List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionIde>>() {});
                    List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                    for(ModeloDireccionIde ide : res){
                        resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                    }
                    return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                }
                case 5 -> {
                    if (numeroRuta == null) {
                        return new ResponseEntity<>("numeroRuta no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    if (kilometro == null) {
                        return new ResponseEntity<>("kilometro no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    params.add("km", kilometro.toString());
                    params.add("ruta", numeroRuta.toString());

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "5");
                    datos.put("numeroruta", numeroRuta.toString());
                    datos.put("kilometro", kilometro.toString());

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloRutaKmIde> res = objectMapper.readValue(cache, new TypeReference<>() {
                        });
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloRutaKmIde ide : res){
                            resultado.add(Utils.rutaKmIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity<>(resultado, headers, HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    // Construir la URL con los parámetros
                    url = "https://direcciones.ide.uy/api/v1/geocode/rutakm";
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                    input = "numeroRuta:" + numeroRuta.toString() + ";kilometro:" + kilometro;
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    datos.put("response", jsonString);

                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        Double lon = (Double) objeto.get("lng");
                        Double lat = (Double) objeto.get("lat");
                        repository.insertarCoordenadas(input, "IDE", lat, lon);
                    }
                    List<ModeloRutaKmIde> res = objectMapper.readValue(jsonString, new TypeReference<>() {
                    });
                    List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                    for(ModeloRutaKmIde ide : res){
                        resultado.add(Utils.rutaKmIdeToModeloGeneral(ide));
                    }
                    return new ResponseEntity<>(resultado, headers, HttpStatus.OK);
                }
                case 6 -> {
                    if (calle == null) {
                        return new ResponseEntity<>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
                    }
                    calleGeoCoder = calle + (numero != null ? " " + numero : "");
                    // Construir la URL con los parámetros
                    params.add("calle", calleGeoCoder);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
                    datos.put("id_canonic_form", "6");
                    datos.put("calle", calle);
                    datos.put("numero", numero);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionIde> res = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionIde>>() {});
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionIde ide : res){
                            resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();
                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);

                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                    input = "calleGeoCoder:" + calleGeoCoder;
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Datos para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        String error = (String) objeto.get("error");
                        Double lon = (Double) objeto.get("puntoX");
                        Double lat = (Double) objeto.get("puntoY");
                        if (error.isEmpty()) {
                            repository.insertarCoordenadas(input, "IDE", lat, lon);
                        }
                    }
                    List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionIde>>() {});
                    List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                    for(ModeloDireccionIde ide : res){
                        resultado.add(Utils.direccionIdeToModeloGeneral(ide));
                    }
                    return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);
                }
                default -> {
                    return new ResponseEntity<>("IDFORMACANONICA INVALIDO", headers, HttpStatus.BAD_REQUEST);
                }
            }
        }
        if (idGeoCoder == 2) {

            url = "https://nominatim.openstreetmap.org/search";
            switch (idFormaCanonica) {
                case 6 -> {
                    // Construir la URL con los parámetros
                    calle = Utils.RemplazarTildesYEspacios(calle);
                    params.add("street", calle + "+" + numero);
                    params.add("format", "json");

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "2");
                    datos.put("id_canonic_form", "6");
                    datos.put("calle", calle);
                    datos.put("numero", numero);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionNominatin> myObjects = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionNominatin>>() {});
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionNominatin  nom : myObjects){
                            resultado.add(Utils.direccionNominatimToModeloGeneral(nom));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);

                        //return new ResponseEntity<>(cache, headers, HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);
                    System.out.println(fullUrl);
                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

                    input = "calle:" + calle + ";numero:" + numero;
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Datos para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        Double lon = Double.parseDouble((String) objeto.get("lon"));
                        Double lat = Double.parseDouble((String) objeto.get("lat"));
                        repository.insertarCoordenadas(input, "NOMINATIN", lat, lon);

                    }
                    List<ModeloDireccionNominatin> myObjects = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionNominatin>>() {});
                    List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                    for(ModeloDireccionNominatin  nom : myObjects){
                        resultado.add(Utils.direccionNominatimToModeloGeneral(nom));
                    }
                    return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);

                    //return response;
                }
                case 4 -> {
                    // Construir la URL con los parámetros
                    nombreInmueble = Utils.RemplazarTildesYEspacios(nombreInmueble);
                    departamento = Utils.RemplazarTildesYEspacios(departamento);
                    params.add("q", nombreInmueble + "+" + departamento);
                    params.add("format", "json");

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "2");
                    datos.put("id_canonic_form", "4");
                    datos.put("nombre_inmueble", nombreInmueble);
                    datos.put("localidad", localidad);
                    datos.put("departamento", departamento);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        List<ModeloDireccionNominatin> myObjects = objectMapper.readValue(cache, new TypeReference<List<ModeloDireccionNominatin>>() {});
                        List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                        for(ModeloDireccionNominatin  nom : myObjects){
                            resultado.add(Utils.direccionNominatimToModeloGeneral(nom));
                        }
                        return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);

                        //return new ResponseEntity<>(cache, headers, HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);
                    System.out.println(fullUrl);
                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

                    input = "nombreInmueble:" + nombreInmueble + (localidad != "" ? ";localidad:" + localidad : "");
                    input = input + (departamento != "" ? ";departamento:" + departamento : "");
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        Double lon = Double.parseDouble((String) objeto.get("lon"));
                        Double lat = Double.parseDouble((String) objeto.get("lat"));
                        repository.insertarCoordenadas(input, "NOMINATIN", lat, lon);
                    }

                    List<ModeloDireccionNominatin> myObjects = objectMapper.readValue(jsonString, new TypeReference<List<ModeloDireccionNominatin>>() {});
                    List<ModeloGeneral> resultado = new ArrayList<ModeloGeneral>();
                    for(ModeloDireccionNominatin  nom : myObjects){
                        resultado.add(Utils.direccionNominatimToModeloGeneral(nom));
                    }
                    return new ResponseEntity< List<ModeloGeneral>>(resultado, headers,HttpStatus.OK);

                }
                default -> {
                    return new ResponseEntity<>(
                            "El idFormaCanonica " + idFormaCanonica + " no es valido para el geocoder seleccionado.",
                            headers, HttpStatus.BAD_REQUEST);
                }
            }

        }
        if (idGeoCoder == 3) {
            {
                url = "https://photon.komoot.io/api/";
                if (idFormaCanonica == 6) {// Construir la URL con los parámetros
                    calle = Utils.RemplazarTildesYEspacios(calle);
                    params.add("q", calle + "+" + numero);

                    // Datos para buscar en cache
                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "3");
                    datos.put("id_canonic_form", "6");
                    datos.put("calle", calle);
                    datos.put("numero", numero);

                    // Busco en cache
                    String cache = tsigCache.obtenerDeCache(datos);
                    if (cache != null) {
                        System.out.println("Cache Si");
                        ModeloDireccionPhoton myObjects = objectMapper.readValue(cache, new TypeReference<ModeloDireccionPhoton>() {});
                        return new ResponseEntity< ModeloGeneral>( Utils.direccionPhotonToModeloGeneral(myObjects), headers,HttpStatus.OK);
                        //return new ResponseEntity<>(cache, headers, HttpStatus.OK);
                    } else {
                        System.out.println("Cache No");
                    }

                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);
                    System.out.println(fullUrl);
                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

                    input = "calle:" + calle + ";numero:" + numero;
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    // Datos para guardar en cache
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCache(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en un objeto Java
                    Map<String, Object> objeto = objectMapper.readValue(jsonString, new TypeReference<>() {
                    });

                    // Acceder a los valores del objeto
                    listaObjetos = (List<Map<String, Object>>) objeto.get("features");
                    Map<String, Object> primerObjeto = listaObjetos.get(0);
                    Map<String, Object> geometry = (Map<String, Object>) primerObjeto.get("geometry");
                    List<Double> coordenadas = (List<Double>) geometry.get("coordinates");
                    Double latitud = coordenadas.get(1);
                    Double longitud = coordenadas.get(0);
                    repository.insertarCoordenadas(input, "PHOTON", latitud, longitud);

                    ModeloDireccionPhoton myObjects = objectMapper.readValue(jsonString, new TypeReference<ModeloDireccionPhoton>() {});
                    return new ResponseEntity< ModeloGeneral>( Utils.direccionPhotonToModeloGeneral(myObjects), headers,HttpStatus.OK);
                    //return response;
                }
                return new ResponseEntity<>(
                        "El idFormaCanonica " + idFormaCanonica + " no es valido para el geocoder seleccionado.",
                        headers, HttpStatus.BAD_REQUEST);

            }
        }
        return new ResponseEntity<>("IDGEOCODER INVALIDO", headers, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> sugerenciaCalleCompleta(String entrada, Boolean todos) {
        RestTemplate restTemplate = new RestTemplate();

        // Configurar los encabezados de la solicitud
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Agregar otros encabezados si es necesario

        // Construir los parámetros
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("entrada", entrada);
        params.add("todos", todos != null ? todos.toString() : "false");
        String cache = tsigCache.obtenerDeCacheSugerencia(entrada, (todos != null) ? todos : false);
        if (cache != null) {
            System.out.println("Cache Si");
            return new ResponseEntity<>(cache, headers, HttpStatus.OK);
        } else {
            System.out.println("Cache No");
        }

        // Construir la URL con los parámetros
        String url = "https://direcciones.ide.uy/api/v0/geocode/SugerenciaCalleCompleta";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParams(params);
        String fullUrl = builder.toUriString();

        // Crear una entidad HttpEntity con los encabezados
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Obtener la respuesta del servicio externo
        ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

        String jsonString = response.getBody();

        tsigCache.insertarEnCacheSugerencia(entrada, (todos != null) ? todos : false, jsonString);

        return response;
    }

    public ResponseEntity<String> direcEnPoligono(Integer limit, String poligono, String tipoDirec) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://direcciones.ide.uy/api/v1/geocode")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .build();

        UriBuilder uriBuilder = UriComponentsBuilder.fromPath("/direcEnPoligono")
                .queryParam("poligono", poligono);

        if (limit != null) {
            uriBuilder.queryParam("limit", limit);
        }
        if (tipoDirec != null) {
            uriBuilder.queryParam("tipoDirec", tipoDirec);
        }

        String uri = uriBuilder.build().toString();

        Mono<String> responseMono = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class);

        String response = responseMono.block(); // Bloquea y espera la respuesta

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<String> busquedaSimple(String entrada) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        // Configurar los encabezados de la solicitud
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Agregar otros encabezados si es necesario

        // Construir los parámetros
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String input = entrada;
        entrada = Utils.RemplazarTildesYEspacios(entrada);
        params.add("q", entrada);
        params.add("format", "json");
        // Construir la URL con los parámetros
        String url = "https://nominatim.openstreetmap.org/search";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParams(params);
        String fullUrl = builder.toUriString();

        // Crear una entidad HttpEntity con los encabezados
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Obtener la respuesta del servicio externo
        ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
        String jsonString = response.getBody();
        // Crear un ObjectMapper de Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        // Analizar la cadena de texto JSON en una lista de objetos Java
        List<Map<String, Object>> listaObjetos = objectMapper.readValue(jsonString, new TypeReference<>() {
        });
        // Recorrer la lista de objetos
        for (Map<String, Object> objeto : listaObjetos) {
            // Acceder a los atributos de cada objeto
            Double lon = Double.parseDouble((String) objeto.get("lon"));
            Double lat = Double.parseDouble((String) objeto.get("lat"));
            repository.insertarCoordenadas(input, "NOMINATIM", lat, lon);
        }
        return response;
    }
}
