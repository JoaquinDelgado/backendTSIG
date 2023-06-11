package com.example.tsig.services;

import com.example.tsig.cache.TsigCache;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String input;
        List<Map<String, Object>> listaObjetos;
        ObjectMapper objectMapper;

        if (idGeoCoder == 1) {
            String url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
            String calleGeoCoder;
            UriComponentsBuilder builder;
            String fullUrl;
            HttpEntity<String> entity;
            ResponseEntity<String> response;

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

					//Preparo datos para guardar en cache
					Map<String, String> datos = new HashMap<>();
					datos.put("id_geocoder", "1");
					datos.put("id_canonic_form", "1");
					datos.put("calle", calle);
					datos.put("numero", numero);
					datos.put("localidad", localidad);
					datos.put("departamento", departamento);
					datos.put("response", jsonString);
					tsigCache.insertarEnCahe(datos);

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
                    return response;
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

                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
					datos.put("id_canonic_form", "2");
					datos.put("calle", calle);
					datos.put("numero", numero);
					datos.put("calle2", calle2);
					datos.put("localidad", localidad);
					datos.put("departamento", departamento);
					datos.put("response", jsonString);
					tsigCache.insertarEnCahe(datos);

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
                    return response;
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
                    calleGeoCoder = calle != null ? calle + " " : "" + "manzana " + manzana + " solar " + solar;
                    params.add("calle", calleGeoCoder);
                    params.add("localidad", localidad);
                    params.add("departamento", departamento);
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

                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
					datos.put("id_canonic_form", "3");
					datos.put("manzana", manzana);
					datos.put("solar", solar);
					datos.put("localidad", localidad);
					datos.put("departamento", departamento);
					datos.put("response", jsonString);
                    tsigCache.insertarEnCahe(datos);

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
                    return response;
                }
                case 4 -> {
                    if (nombreInmueble == null) {
                        return new ResponseEntity<String>("nombreInmueble no puede ser vacio", headers,
                                HttpStatus.BAD_REQUEST);
                    }
                    params.add("calle", nombreInmueble);
                    params.add("localidad", localidad);
                    params.add("departamento", departamento);
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

                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "1");
					datos.put("id_canonic_form", "4");
					datos.put("nombre_inmueble", nombreInmueble);
					datos.put("localidad", localidad);
					datos.put("departamento", departamento);
					datos.put("response", jsonString);
                    tsigCache.insertarEnCahe(datos);

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
                    return response;
                }
                case 5 -> {
                    System.out.println("Entro....");
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

                    Map<String, String> datos = new HashMap<>();
					datos.put("id_geocoder", "1");
					datos.put("id_canonic_form", "5");
					datos.put("numeroruta", numeroRuta.toString());
					datos.put("kilometro", kilometro.toString());
                    tsigCache.insertarEnCahe(datos);


                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                    });
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        Double lon = (Double) objeto.get("lng");
                        Double lat = (Double) objeto.get("lat");
                        repository.insertarCoordenadas(input, "IDE", lat, lon);
                    }
                    return response;
                }
                default -> {
                    return new ResponseEntity<>("IDFORMACANONICA INVALIDO", headers, HttpStatus.BAD_REQUEST);
                }
            }
        }
        if (idGeoCoder == 2) {

            String url = "https://nominatim.openstreetmap.org/search";
            UriComponentsBuilder builder;
            String fullUrl;
            HttpEntity<String> entity;
            ResponseEntity<String> response;
            switch (idFormaCanonica) {
                case 6 -> {
                    // Construir la URL con los parámetros
                    calle = Utils.RemplazarTildesYEspacios(calle);
                    params.add("street", calle + "+" + numero);
                    params.add("format", "json");
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);
                    System.out.println(fullUrl);
                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

                    input = "calle:"+calle + ";numero:"+numero;
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "2");
					datos.put("id_canonic_form", "6");
					datos.put("calle", calle);
					datos.put("numero", numero);
					datos.put("response", jsonString);
                    tsigCache.insertarEnCahe(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        Double lon = Double.parseDouble((String)objeto.get("lon"));
                        Double lat = Double.parseDouble((String)objeto.get("lat"));
                        repository.insertarCoordenadas(input, "NOMINATIN", lat, lon);

                    }
                    return response;
                }
                case 4 -> {
                    // Construir la URL con los parámetros
                    nombreInmueble = Utils.RemplazarTildesYEspacios(nombreInmueble);
                    departamento = Utils.RemplazarTildesYEspacios(departamento);
                    params.add("q", nombreInmueble + "+" + departamento);
                    params.add("format", "json");
                    builder = UriComponentsBuilder.fromHttpUrl(url)
                            .queryParams(params);
                    fullUrl = builder.toUriString();

                    // Crear una entidad HttpEntity con los encabezados
                    entity = new HttpEntity<>(headers);
                    System.out.println(fullUrl);
                    // Hacer la solicitud GET al servicio externo

                    // Obtener la respuesta del servicio externo
                    response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

                    input = "nombreInmueble:"+nombreInmueble + (localidad!="" ? ";localidad:"+localidad : "");
                    input = input + (departamento!="" ? ";departamento:"+departamento : "");
                    // Obtener la respuesta de ResponseEntity
                    jsonString = response.getBody();

                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "2");
					datos.put("id_canonic_form", "4");
					datos.put("nombre_inmueble", nombreInmueble);
					datos.put("localidad", localidad);
					datos.put("departamento", departamento);
					datos.put("response", jsonString);
                    tsigCache.insertarEnCahe(datos);

                    // Crear un ObjectMapper de Jackson
                    objectMapper = new ObjectMapper();
                    // Analizar la cadena de texto JSON en una lista de objetos Java
                    listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
                    // Recorrer la lista de objetos
                    for (Map<String, Object> objeto : listaObjetos) {
                        // Acceder a los atributos de cada objeto
                        Double lon = Double.parseDouble((String)objeto.get("lon"));
                        Double lat = Double.parseDouble((String)objeto.get("lat"));
                        repository.insertarCoordenadas(input, "NOMINATIN", lat, lon);
                    }
                    return response;
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
                String url = "https://photon.komoot.io/api/";
                UriComponentsBuilder builder;
                String fullUrl;
                HttpEntity<String> entity;
                ResponseEntity<String> response;
                if (idFormaCanonica == 6) {// Construir la URL con los parámetros
                    calle = Utils.RemplazarTildesYEspacios(calle);
                    params.add("q", calle + "+" + numero);
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

                    Map<String, String> datos = new HashMap<>();
                    datos.put("id_geocoder", "3");
                    datos.put("id_canonic_form", "6");
                    datos.put("calle", calle);
                    datos.put("numero", numero);
                    datos.put("response", jsonString);
                    tsigCache.insertarEnCahe(datos);

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
                    return response;
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
        if (todos != null) {
            params.add("todos", todos.toString());
        }
        // Construir la URL con los parámetros
        String url = "https://direcciones.ide.uy/api/v0/geocode/SugerenciaCalleCompleta";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParams(params);
        String fullUrl = builder.toUriString();

        // Crear una entidad HttpEntity con los encabezados
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Obtener la respuesta del servicio externo
        return restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
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
        params.add("format","json");
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
            Double lon = Double.parseDouble((String)objeto.get("lon"));
            Double lat = Double.parseDouble((String)objeto.get("lat"));
            repository.insertarCoordenadas(input, "NOMINATIM", lat, lon);
        }
        return response;
    }
}
