package com.example.tsig.controllers;

import com.example.tsig.models.general.Poligono;
import com.example.tsig.models.general.Punto;
import com.example.tsig.services.BackofficeService;
import com.example.tsig.services.Service;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DireccionesController {

	@Autowired
	Service service;

	@Autowired
	BackofficeService backofficeService;

	@GetMapping("/formasCanonicas")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<Map<Integer, String>> formasCanonicas() throws JsonProcessingException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(service.formasCanonicas(), headers, HttpStatus.OK);
	}

	@GetMapping("/obtenerGeoCoders")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<Map<Integer, String>> obtenerGeoCoders(
			@RequestParam(value = "idFormaCanonica") Integer idFormaCanonica) {

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(service.obtenerGeoCoders(idFormaCanonica), headers, HttpStatus.OK);
	}

	@GetMapping("/busquedaDireccionEstructurada/{idGeoCoder}/{idFormaCanonica}")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<?> busquedaDireccionEstructurada(
			@PathVariable("idGeoCoder") Integer idGeoCoder,
			@PathVariable("idFormaCanonica") Integer idFormaCanonica,
			@RequestParam(value = "calle", required = false) String calle,
			@RequestParam(value = "numero", required = false) String numero,
			@RequestParam(value = "localidad", required = false) String localidad,
			@RequestParam(value = "departamento", required = false) String departamento,
			@RequestParam(value = "calle2", required = false) String calle2,
			@RequestParam(value = "manzana", required = false) String manzana,
			@RequestParam(value = "solar", required = false) String solar,
			@RequestParam(value = "nombreInmueble", required = false) String nombreInmueble,
			@RequestParam(value = "numeroRuta", required = false) Integer numeroRuta,
			@RequestParam(value = "kilometro", required = false) Double kilometro

	) throws JsonProcessingException {
		return service.busquedaDireccionEstructurada(idGeoCoder,
				idFormaCanonica, calle, numero, localidad, departamento, calle2, manzana, solar, nombreInmueble, numeroRuta, kilometro);

	}

	@GetMapping("/sugerenciaCalleCompleta")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> sugerenciaCalleCompleta(
			@RequestParam(value = "entrada") String entrada,
			@RequestParam(value = "todos", required = false) Boolean todos) {

		return service.sugerenciaCalleCompleta(entrada, todos);

	}

	@PostMapping("/direcEnPoligono")
	@CrossOrigin(origins = "*")
	public ResponseEntity<?> direcEnPoligono(@RequestParam(value = "limit", required = false) Integer limit,
												  @RequestBody Poligono poligono,
												  @RequestParam(value = "tipoDirec", required = false) String tipoDirec) throws JsonProcessingException {
	  return service.direcEnPoligono(limit, poligono, tipoDirec);
	}

	@GetMapping("/busquedaSimple")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<?> busquedaSimple(
			@RequestParam(value = "entrada") String entrada) throws JsonProcessingException {
		return service.busquedaSimple(entrada);
	}

	@GetMapping("/reverse")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<?> reverse(
			@RequestParam(value = "latitud") Double latitud,
			@RequestParam(value = "longitud") Double longitud,
			@RequestParam(value = "limit", required = false) Integer limit) throws JsonProcessingException {
		return service.reverse(latitud, longitud, limit);
	}

	/////////////////// BACKOFFICE ////////////////////
	@GetMapping("/backoffice/distancias")
	public ResponseEntity<?> distancias() throws JsonProcessingException {
		return backofficeService.HaversineDistanceCalculator();
	}
}