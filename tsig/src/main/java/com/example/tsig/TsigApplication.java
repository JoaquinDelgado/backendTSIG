package com.example.tsig;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.HashMap;
import java.util.Map;


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


	@GetMapping("/obtenerGeoCoders")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<Map<Integer,String>> obtenerGeoCoders() throws JsonProcessingException {

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
		// Agregar otros encabezados si es necesario

		Map<Integer,String>  geoCoders= new HashMap<Integer,String>();
		geoCoders.put(1, "AGESIC");
		ResponseEntity<Map<Integer,String>> response = new ResponseEntity<Map<Integer,String>>(geoCoders, headers, HttpStatus.OK);
		return response;
	}

	@GetMapping("/{idGeoCoder}/formasCanonicas")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<Map<Integer,String>> formasCanonicas(@PathVariable("idGeoCoder") Integer idGeoCoder) throws JsonProcessingException {

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
		// Agregar otros encabezados si es necesario

		Map<Integer,String> formasCanonicas = new HashMap<Integer,String>();
		switch (idGeoCoder) {
			case 1:		
				formasCanonicas.put(1, "calle,numero,localidad,departamento");
				formasCanonicas.put(2, "calle,numero,calle2,localidad,departamento");
				formasCanonicas.put(3, "calle,manzana,solar,localidad,departamento");
				formasCanonicas.put(4, "nombreInmueble,localidad,departamento");
				formasCanonicas.put(5, "numeroRuta,kilometro");

			}


		ResponseEntity<Map<Integer,String>> response = new ResponseEntity<Map<Integer,String>>(formasCanonicas, headers, HttpStatus.OK);


		return response;
	}

	@GetMapping("/busquedaDireccionStructurada/{idGeoCoder}/{formaCanonica}")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> busquedaDireccion(
			@PathVariable("idGeoCoder") Integer idGeoCoder,
			@PathVariable("formaCanonica") Integer formaCanonica,
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

		RestTemplate restTemplate = new RestTemplate();

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Access-Control-Allow-Origin", "*");
		// Agregar otros encabezados si es necesario

		// Construir los parámetros
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		
		
		if(idGeoCoder==1){
			String url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";;	
			String calleGeoCoder;
			UriComponentsBuilder builder;
			String fullUrl;
			HttpEntity<String> entity;
			ResponseEntity<String> response;

			switch (formaCanonica){
				case 1 :
					if (calle==null){
						return new ResponseEntity<String>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					calleGeoCoder = calle + (numero!=null ? " "+numero : "");
					// Construir la URL con los parámetros
					params.add("calle", calleGeoCoder);
					params.add("localidad",localidad);
					params.add("departamento", departamento);
					
					builder = UriComponentsBuilder.fromHttpUrl(url)
							.queryParams(params);
					fullUrl = builder.toUriString();
			
					// Crear una entidad HttpEntity con los encabezados
					entity = new HttpEntity<>(headers);
			
					// Hacer la solicitud GET al servicio externo
			
					// Obtener la respuesta del servicio externo
					response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
					return response;
				case 2:
					if (calle==null){
						return new ResponseEntity<String>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					if (calle2==null){
						return new ResponseEntity<String>("Calle2 no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					calleGeoCoder = calle + (numero!=null ? " "+numero : "") +" esquina "+calle2;
					// Construir la URL con los parámetros
					params.add("calle", calleGeoCoder);
					params.add("localidad",localidad);
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
					return response;
				case 3:
					if (departamento==null){
						return new ResponseEntity<String>("Departamento no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					if (localidad==null){
						return new ResponseEntity<String>("Localidad no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					if (manzana==null){
						return new ResponseEntity<String>("Manzana no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					if (solar==null){
						return new ResponseEntity<String>("Solar no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					calleGeoCoder = calle!=null ? calle+" " : "" + "manzana "+manzana+" solar "+solar;
					System.out.println(calleGeoCoder);

					params.add("calle", calleGeoCoder);
					params.add("localidad",localidad);
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
					return response;
				case 4: 
					if (nombreInmueble==null){
						return new ResponseEntity<String>("nombreInmueble no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					params.add("calle", nombreInmueble);
					params.add("localidad",localidad);
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
					return response;
				case 5:
					if (numeroRuta == null){
						return new ResponseEntity<String>("numeroRuta no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					if (kilometro == null){
						return new ResponseEntity<String>("kilometro no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
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
			
					//insertarEnBase(response, "RUTAKM");
			
					return response;
					
			}
			
	
		}

		return null;

		//insertarEnBase(response, "BUSQUEDADIRECCION");

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
