package com.example.tsig.services;

import com.example.tsig.models.general.ModeloGeneral;
import com.example.tsig.models.ide.ModeloDireccionIde;
import com.example.tsig.models.im.DireccionIM;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Component
public class BackofficeService {

    @Autowired
    Repository repository;

    //por cada elemento del array -> para cada geocoder, busco la dir del array con dicho geocoder, obtengo las coordenadas y llamo al haveersineDistance
    // inserto en la tabla distancias_im_geocoders el id del elemento del array, el id del geocoder, las coordenadas del geocoder, y el resultado de haversineDistance
    public void HaversineDistanceCalculator() throws JsonProcessingException {
        Map<Integer, DireccionIM> direccionesIM = repository.obtenerDireccionesIM();
        for (Map.Entry<Integer, DireccionIM> direccionIMEntry : direccionesIM.entrySet()) {
            DireccionIM value = direccionIMEntry.getValue();
            //IDE
            Double[] coordenadas = PegarleAIDE(value.getCalle(), value.getNumero());
            Double distancia = CalcularDistancia(value.getLatitud(), value.getLongitud(), coordenadas[0], coordenadas[1]);
            repository.insertarDistancia(value.getId(), 1, coordenadas[0], coordenadas[1], distancia.floatValue());
            //NOMINATIM
            coordenadas = PegarleANominatim(value.getCalle(), value.getNumero());
            distancia = CalcularDistancia(value.getLatitud(), value.getLongitud(), coordenadas[0], coordenadas[1]);
            repository.insertarDistancia(value.getId(), 2, coordenadas[0], coordenadas[1], distancia.floatValue());
            //PHOTON
            coordenadas = PegarleAPhoton(value.getCalle(), value.getNumero());
            distancia = CalcularDistancia(value.getLatitud(), value.getLongitud(), coordenadas[0], coordenadas[1]);
            repository.insertarDistancia(value.getId(), 3, coordenadas[0], coordenadas[1], distancia.floatValue());
        }
    }

    private Double CalcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        Integer EARTH_RADIUS = 6371; // Radio de la Tierra en kilómetros

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 1000 * EARTH_RADIUS * c;
    }

    private Double[] PegarleAIDE(String calle, Integer numero) throws JsonProcessingException {
        // Crear una entidad HttpEntity con los encabezados
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String calleGeoCoder = calle + (numero != null ? " " + numero : "");
        // Construir la URL con los parámetros
        params.add("calle", calleGeoCoder);

        UriComponentsBuilder builder;
        builder = UriComponentsBuilder.fromHttpUrl("https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion")
                .queryParams(params);
        String fullUrl = builder.toUriString();

        // Hacer la solicitud GET al servicio externo

        // Obtener la respuesta del servicio externo
        ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
        String input = "calleGeoCoder:" + calleGeoCoder;
        // Obtener la respuesta de ResponseEntity
        String jsonString = response.getBody();

        // Crear un ObjectMapper de Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        // Analizar la cadena de texto JSON en una lista de objetos Java
        List<Map<String, Object>> listaObjetos = objectMapper.readValue(jsonString, new TypeReference<>() {
        });
        // Recorrer la lista de objetos
        Double longitud = 0D;
        Double latitud = 0D;
        for (Map<String, Object> objeto : listaObjetos) {
            // Acceder a los atributos de cada objeto
            String error = (String) objeto.get("error");
            if (Objects.equals(error, "")) {
                longitud = (Double) objeto.get("puntoX");
                latitud = (Double) objeto.get("puntoY");
            }
        }
        return new Double[]{latitud, longitud};
    }

    private Double[] PegarleANominatim(String calle, Integer numero) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        UriComponentsBuilder builder;
        calle = Utils.RemplazarTildesYEspacios(calle);
        params.add("street", calle + "+" + numero);
        params.add("country", "Uruguay");
        params.add("format", "json");

        builder = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search")
                .queryParams(params);
        String fullUrl = builder.toUriString();


        // Obtener la respuesta del servicio externo
        ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

        String input = "calle:" + calle + ";numero:" + numero;
        // Obtener la respuesta de ResponseEntity
        String jsonString = response.getBody();

        // Crear un ObjectMapper de Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        // Analizar la cadena de texto JSON en una lista de objetos Java
        List<Map<String, Object>> listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
        });
        // Recorrer la lista de objetos
        Double latitud = 0D;
        Double longitud = 0D;
        for (Map<String, Object> objeto : listaObjetos) {
            // Acceder a los atributos de cada objeto
            longitud = Double.parseDouble((String) objeto.get("lon"));
            latitud = Double.parseDouble((String) objeto.get("lat"));
        }
        return new Double[]{latitud, longitud};
    }

    private Double[] PegarleAPhoton(String calle, Integer numero) throws JsonProcessingException {
        // Crear un ObjectMapper de Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        UriComponentsBuilder builder;
        calle = Utils.RemplazarTildesYEspacios(calle);
        params.add("q", calle + "+" + numero + "+" + "Uruguay");
        builder = UriComponentsBuilder.fromHttpUrl("https://photon.komoot.io/api/")
                .queryParams(params);
        String fullUrl = builder.toUriString();

        // Obtener la respuesta del servicio externo
        ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

        String input = "calle:" + calle + ";numero:" + numero;
        // Obtener la respuesta de ResponseEntity
        String jsonString = response.getBody();
        // Analizar la cadena de texto JSON en un objeto Java
        Map<String, Object> objeto = objectMapper.readValue(jsonString, new TypeReference<>() {
        });

        // Acceder a los valores del objeto
        List<Map<String, Object>> listaObjetos = (List<Map<String, Object>>) objeto.get("features");
        Map<String, Object> primerObjeto = listaObjetos.get(0);
        Map<String, Object> geometry = (Map<String, Object>) primerObjeto.get("geometry");
        List<Double> coordenadas = (List<Double>) geometry.get("coordinates");
        Double latitud = coordenadas.get(1);
        Double longitud = coordenadas.get(0);
        return new Double[]{latitud,longitud};
    }
}

