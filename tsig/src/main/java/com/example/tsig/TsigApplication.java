package com.example.tsig;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.lang.reflect.InvocationTargetException;


@SpringBootApplication
public class TsigApplication {

	public static void main(String[] args) {
		SpringApplication.run(TsigApplication.class, args);
	}

}


@RestController
@RequestMapping("/api")
class DireccionesController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	void insertarEnBase(ResponseEntity<String> response, String servicio) throws JsonProcessingException {
		String sql = "INSERT INTO json_response (json, geocoder, servicio) VALUES (to_jsonb(?::text), 'AGESIC', ?)";
		ObjectMapper objectMapper = new ObjectMapper();
		String responseBody = objectMapper.writeValueAsString(response.getBody());
		int rows = jdbcTemplate.update(sql, responseBody, servicio);
		if (rows > 0) {
			System.out.println("A new row has been inserted.");
		}
	}

	@GetMapping("/busquedaDireccion/{calle}")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> busquedaDireccion(@PathVariable("calle") String calle, @RequestParam(value = "departamento", required = false) String departamento, @RequestParam(value = "localidad", required = false) String localidad) throws JsonProcessingException {
		RestTemplate restTemplate = new RestTemplate();

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
		// Agregar otros encabezados si es necesario

		// Construir los parámetros
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("calle", calle);
		if (departamento != null) {
			params.add("departamento", departamento);
		}
		if (localidad != null) {
			params.add("localidad", localidad);
		}

		// Construir la URL con los parámetros
		String url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParams(params);
		String fullUrl = builder.toUriString();

		// Crear una entidad HttpEntity con los encabezados
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Hacer la solicitud GET al servicio externo

		// Obtener la respuesta del servicio externo
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

		insertarEnBase(response, "BUSQUEDADIRECCION");

		return response;
	}

	@GetMapping("/localidades/{departamento}")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> localidades(@PathVariable("departamento") String departamento, @RequestParam(value = "alias", required = false) Boolean alias) throws JsonProcessingException {
		RestTemplate restTemplate = new RestTemplate();

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
		// Agregar otros encabezados si es necesario

		// Construir los parámetros
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("departamento", departamento);
		if (alias != null) {
			params.add("alias", alias.toString());
		}

		// Construir la URL con los parámetros
		String url = "https://direcciones.ide.uy/api/v0/geocode/localidades";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParams(params);
		String fullUrl = builder.toUriString();

		// Crear una entidad HttpEntity con los encabezados
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Obtener la respuesta del servicio externo
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

		insertarEnBase(response, "LOCALIDADES");

		return response;
	}

	@GetMapping("/reverse")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> reverse(@RequestParam(value = "latitud", required = true) Double latitud, @RequestParam(value = "longitud", required = true) Double longitud, @RequestParam(value = "limit", required = false) Integer limit) throws JsonProcessingException {
		RestTemplate restTemplate = new RestTemplate();

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
		// Agregar otros encabezados si es necesario

		// Construir los parámetros
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("latitud", latitud.toString());
		params.add("longitud", longitud.toString());
		if (limit != null) {
			params.add("limit", limit.toString());
		}

		// Construir la URL con los parámetros
		String url = "https://direcciones.ide.uy/api/v1/geocode/reverse";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParams(params);
		String fullUrl = builder.toUriString();

		// Crear una entidad HttpEntity con los encabezados
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Obtener la respuesta del servicio externo
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

		insertarEnBase(response, "REVERSE");

		return response;

	}

	@GetMapping("/rutakm")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> rutakm(@RequestParam(value = "km", required = true) Double km, @RequestParam(value = "ruta", required = true) String ruta) throws JsonProcessingException {
		RestTemplate restTemplate = new RestTemplate();

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
		// Agregar otros encabezados si es necesario

		// Construir los parámetros
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("km", km.toString());
		params.add("ruta", ruta.toString());

		// Construir la URL con los parámetros
		String url = "https://direcciones.ide.uy/api/v1/geocode/rutakm";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParams(params);
		String fullUrl = builder.toUriString();

		// Crear una entidad HttpEntity con los encabezados
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Obtener la respuesta del servicio externo
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

		insertarEnBase(response, "RUTAKM");

		return response;

	}

	@GetMapping("/sugerenciaCalleCompleta")
	@CrossOrigin(origins = "*") // Permitir todas las IPs

	public ResponseEntity<String> sugerenciaCalleCompleta(@RequestParam(value = "entrada", required = true) String entrada, @RequestParam(value = "todos", required = false) Boolean todos) throws JsonProcessingException {
		RestTemplate restTemplate = new RestTemplate();

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
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
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

		insertarEnBase(response, "SUGERENCIACALLECOMPLETA");

		return response;

	}

	@GetMapping("/find")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> find(@RequestParam(value = "type", required = true) String type,
									   @RequestParam(value = "departamento", required = false) String departamento,
									   @RequestParam(value = "idcalle", required = false) String idcalle,
									   @RequestParam(value = "idcalleEsq", required = false) String idcalleEsq,
									   @RequestParam(value = "inmueble", required = false) String inmueble,
									   @RequestParam(value = "km", required = false) String km,
									   @RequestParam(value = "letra", required = false) String letra,
									   @RequestParam(value = "localidad", required = false) String localidad,
									   @RequestParam(value = "manzana", required = false) String manzana,
									   @RequestParam(value = "portal", required = false) String portal,
									   @RequestParam(value = "ruta", required = false) String ruta,
									   @RequestParam(value = "solar", required = false) String solar) throws JsonProcessingException {

		RestTemplate restTemplate = new RestTemplate();

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
		// Agregar otros encabezados si es necesario

		// Construir los parámetros
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("type", type);
		if (departamento != null) {
			params.add("departamento", departamento);
		}
		if (idcalle != null) {
			params.add("idcalle", idcalle);
		}
		if (idcalleEsq != null) {
			params.add("idcalleEsq", idcalleEsq);
		}
		if (inmueble != null) {
			params.add("inmueble", inmueble);
		}
		if (km != null) {
			params.add("km", km);
		}
		if (letra != null) {
			params.add("letra", letra);
		}
		if (localidad != null) {
			params.add("localidad", localidad);
		}
		if (manzana != null) {
			params.add("manzana", manzana);
		}
		if (portal != null) {
			params.add("portal", portal);
		}
		if (ruta != null) {
			params.add("ruta", ruta);
		}
		if (solar != null) {
			params.add("solar", solar);
		}
		// Construir la URL con los parámetros
		String url = "https://direcciones.ide.uy/api/v1/geocode/find";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParams(params);
		String fullUrl = builder.toUriString();

		// Crear una entidad HttpEntity con los encabezados
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Obtener la respuesta del servicio externo
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

		insertarEnBase(response, "FIND");

		return response;

	}

	@GetMapping("/direcEnPoligono")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> direcEnPoligono(@RequestParam(value = "limit", required = false) Integer limit, @RequestParam(value = "poligono", required = true) String poligono, @RequestParam(value = "tipoDirec", required = false) String tipoDirec) throws JsonProcessingException {
		RestTemplate restTemplate = new RestTemplate();

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
		// Agregar otros encabezados si es necesario

		// Construir los parámetros
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("poligono", poligono);
		if (limit != null) {
			params.add("limit", limit.toString());
		}
		if (tipoDirec != null) {
			params.add("tipoDirec", tipoDirec);
		}

		// Construir la URL con los parámetros
		String url = "https://direcciones.ide.uy/api/v1/geocode/direcEnPoligono";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParams(params);
		String fullUrl = builder.toUriString();

		// Crear una entidad HttpEntity con los encabezados
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Obtener la respuesta del servicio externo
		try {
			System.out.println(poligono);
			ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
			insertarEnBase(response, "DIRECENPOLIGONO");
			return response;
		} catch (RestClientException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
