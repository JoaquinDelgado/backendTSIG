package com.example.tsig.services;

import com.example.tsig.models.datoscomparativos.ModeloDatoComparativo;
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
    public ResponseEntity<?> HaversineDistanceCalculator() throws JsonProcessingException {
        Map<Integer, DireccionIM> direccionesIM = repository.obtenerDireccionesIM();
        for (Map.Entry<Integer, DireccionIM> direccionIMEntry : direccionesIM.entrySet()) {
            DireccionIM value = direccionIMEntry.getValue();
            //IDE
            ModeloDireccionIde dirIde = PegarleAIDE(value.getCalle(), value.getNumero());
            String numero = dirIde.getDireccion().getNumero() != null ? " " + dirIde.getDireccion().getNumero().getNro_puerta().toString() : "";
            String calleTemp = dirIde.getDireccion().getCalle() != null ? dirIde.getDireccion().getCalle().getNombre_normalizado() + numero : dirIde.getDireccion().getDepartamento().getNombre_normalizado() +" "+dirIde.getDireccion().getLocalidad().getNombre_normalizado();
            String calleDefinitivo = dirIde.getDireccion().getInmueble() != null ? dirIde.getDireccion().getInmueble().getNombre() + ", " + calleTemp : calleTemp;
            Double distancia = CalcularDistancia(value.getLatitud(), value.getLongitud(), dirIde.getPuntoY(), dirIde.getPuntoX());
            repository.insertarDistancia(value.getId(), 1, calleDefinitivo,dirIde.getPuntoY(), dirIde.getPuntoX(), distancia.floatValue());
          
            //NOMINATIM
            ModeloDireccionNominatin dirNom = PegarleANominatim(value.getCalle(), value.getNumero());
            distancia = CalcularDistancia(value.getLatitud(), value.getLongitud(), dirNom.getLat(), dirNom.getLon());
            repository.insertarDistancia(value.getId(), 2, dirNom.getDisplay_name(), dirNom.getLat(), dirNom.getLon(), distancia.floatValue());
            //PHOTON
            ModeloDireccionPhoton dirPhoton = PegarleAPhoton(value.getCalle(), value.getNumero());
            String direccion = dirPhoton.getFeatures()[0].getProperties().getName() != null ? dirPhoton.getFeatures()[0].getProperties().getName() : dirPhoton.getFeatures()[0].getProperties().getStreet() + " "
                + dirPhoton.getFeatures()[0].getProperties().getHousenumber();
            distancia = CalcularDistancia(value.getLatitud(), value.getLongitud(), dirPhoton.getFeatures()[0].getGeometry().getCoordinates()[1] , dirPhoton.getFeatures()[0].getGeometry().getCoordinates()[0]);
            repository.insertarDistancia(value.getId(), 3, direccion,dirPhoton.getFeatures()[0].getGeometry().getCoordinates()[1], dirPhoton.getFeatures()[0].getGeometry().getCoordinates()[0], distancia.floatValue());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>("Datos Insertados", headers, HttpStatus.OK);
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

    private ModeloDireccionIde PegarleAIDE(String calle, Integer numero) throws JsonProcessingException {
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
        List<ModeloDireccionIde> res = objectMapper.readValue(jsonString, new TypeReference<>() {
        });
        for(ModeloDireccionIde ide : res){
            if(ide.getError()!=null && ide.getError().isEmpty()){
                return ide;
            }
        }
        return null;

    }

    private ModeloDireccionNominatin PegarleANominatim(String calle, Integer numero) throws JsonProcessingException {
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

        ObjectMapper objectMapper = new ObjectMapper();
        List<ModeloDireccionNominatin> res = objectMapper.readValue(jsonString,new TypeReference<List<ModeloDireccionNominatin>>() {});
        if(res!=null && !res.isEmpty())
            return res.get(0);
        return null;

    }

    private ModeloDireccionPhoton PegarleAPhoton(String calle, Integer numero) throws JsonProcessingException {
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
        ModeloDireccionPhoton res = objectMapper.readValue(jsonString,new TypeReference<ModeloDireccionPhoton>() {});
        return res;
    }
}

