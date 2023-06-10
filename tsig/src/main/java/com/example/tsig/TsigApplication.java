package com.example.tsig;

import com.example.tsig.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
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

	public void insertarCoordenadas(String input, String geoCoder, Double lat, Double lon){
		String sql = "INSERT INTO audits (input,geocoder,latitud,longitud) VALUES (?,?,?,?)";
		int rows = jdbcTemplate.update(sql, input, geoCoder, lat, lon);
		if (rows > 0) {
			System.out.println("A new row has been inserted.");
		}

	}

	@GetMapping("/formasCanonicas")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<Map<Integer, String>> formasCanonicas() throws JsonProcessingException {

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		// Agregar otros encabezados si es necesario

		Map<Integer, String> formasCanonicas = new HashMap<>();

		String sql = "SELECT * FROM canonic_forms";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

		// Procesar los resultados
		for (Map<String, Object> row : rows) {
			formasCanonicas.put((Integer) row.get("id"), (String) row.get("canonic_form"));
		}

		return new ResponseEntity<>(formasCanonicas, headers, HttpStatus.OK);
	}

	@GetMapping("/obtenerGeoCoders")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<Map<Integer, String>> obtenerGeoCoders(
			@RequestParam(value = "idFormaCanonica", required = true) Integer idFormaCanonica)
			throws JsonProcessingException {

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		// Agregar otros encabezados si es necesario

		Map<Integer, String> geoCoders = new HashMap<>();

		String sql = "SELECT g.ID, g.GEOCODER FROM GEOCODERS g INNER JOIN geocoders_canonic_forms gcf ON gcf.id_geocoder = g.id WHERE gcf.id_canonic_form = ?";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, idFormaCanonica);

		// Procesar los resultados
		for (Map<String, Object> row : rows) {
			geoCoders.put((Integer) row.get("id"), (String) row.get("geocoder"));
		}

		return new ResponseEntity<>(geoCoders, headers, HttpStatus.OK);
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

		RestTemplate restTemplate = new RestTemplate();

		// Configurar los encabezados de la solicitud
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		// Agregar otros encabezados si es necesario

		// Construir los parámetros
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		if (idGeoCoder == 1) {
			String url = "https://direcciones.ide.uy/api/v0/geocode/BusquedaDireccion";
			String calleGeoCoder;
			UriComponentsBuilder builder;
			String fullUrl;
			HttpEntity<String> entity;
			ResponseEntity<String> response;

			switch (idFormaCanonica) {
				case 1:
					if (calle == null) {
						return new ResponseEntity<String>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					calleGeoCoder = calle + (numero != null ? " " + numero : "");
					// Construir la URL con los parámetros
					params.add("calle", calleGeoCoder);
					params.add("localidad", localidad);
					params.add("departamento", departamento);

					builder = UriComponentsBuilder.fromHttpUrl(url)
							.queryParams(params);
					fullUrl = builder.toUriString();

					System.out.println(fullUrl);
					// Crear una entidad HttpEntity con los encabezados
					entity = new HttpEntity<>(headers);

					// Hacer la solicitud GET al servicio externo

					// Obtener la respuesta del servicio externo
					response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);

					String input = "calleGeoCoder:"+calleGeoCoder + (localidad!="" ? ";localidad:"+localidad : "");
					input = input + (departamento!="" ? ";departamento:"+departamento : "");
					// Obtener la respuesta de ResponseEntity
					String jsonString = response.getBody();
					// Crear un ObjectMapper de Jackson
					ObjectMapper objectMapper = new ObjectMapper();
					// Analizar la cadena de texto JSON en una lista de objetos Java
					List<Map<String, Object>> listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
					// Recorrer la lista de objetos
					for (Map<String, Object> objeto : listaObjetos) {
						// Acceder a los atributos de cada objeto
						String error = (String)objeto.get("error");
						Double lon = (Double) objeto.get("puntoX");
						Double lat = (Double) objeto.get("puntoY");
						if(error.isEmpty()){
							System.out.println("Long: " + lon);
							System.out.println("lat: " + lat);
							insertarCoordenadas(input, "IDE", lat, lon);
						}
					}

					return response;
				case 2:
					if (calle == null) {
						return new ResponseEntity<String>("Calle no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					if (calle2 == null) {
						return new ResponseEntity<String>("Calle2 no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
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

					input = "calleGeoCoder:"+calleGeoCoder + (localidad!="" ? ";localidad:"+localidad : "");
					input = input + (departamento!="" ? ";departamento:"+departamento : "");
					// Obtener la respuesta de ResponseEntity
					jsonString = response.getBody();
					// Crear un ObjectMapper de Jackson
					objectMapper = new ObjectMapper();
					// Analizar la cadena de texto JSON en una lista de objetos Java
					listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
					// Recorrer la lista de objetos
					for (Map<String, Object> objeto : listaObjetos) {
						// Acceder a los atributos de cada objeto
						String error = (String)objeto.get("error");
						Double lon = (Double) objeto.get("puntoX");
						Double lat = (Double) objeto.get("puntoY");
						if(error.isEmpty()){
							System.out.println("Long: " + lon);
							System.out.println("lat: " + lat);
							insertarCoordenadas(input, "IDE", lat, lon);
						}
					}

					return response;
				case 3:
					if (departamento == null) {
						return new ResponseEntity<String>("Departamento no puede ser vacio", headers,
								HttpStatus.BAD_REQUEST);
					}
					if (localidad == null) {
						return new ResponseEntity<String>("Localidad no puede ser vacio", headers,
								HttpStatus.BAD_REQUEST);
					}
					if (manzana == null) {
						return new ResponseEntity<String>("Manzana no puede ser vacio", headers,
								HttpStatus.BAD_REQUEST);
					}
					if (solar == null) {
						return new ResponseEntity<String>("Solar no puede ser vacio", headers, HttpStatus.BAD_REQUEST);
					}
					calleGeoCoder = calle != null ? calle + " " : "" + "manzana " + manzana + " solar " + solar;
					System.out.println(calleGeoCoder);

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

					input = "calleGeoCoder:"+calleGeoCoder + (localidad!="" ? ";localidad:"+localidad : "");
					input = input + (departamento!="" ? ";departamento:"+departamento : "");
					// Obtener la respuesta de ResponseEntity
					jsonString = response.getBody();
					// Crear un ObjectMapper de Jackson
					objectMapper = new ObjectMapper();
					// Analizar la cadena de texto JSON en una lista de objetos Java
					listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
					// Recorrer la lista de objetos
					for (Map<String, Object> objeto : listaObjetos) {
						// Acceder a los atributos de cada objeto
						String error = (String)objeto.get("error");
						Double lon = (Double) objeto.get("puntoX");
						Double lat = (Double) objeto.get("puntoY");
						if(error.isEmpty()){
							System.out.println("Long: " + lon);
							System.out.println("lat: " + lat);
							insertarCoordenadas(input, "IDE", lat, lon);
						}
					}


					return response;
				case 4:
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

					input = "nombreInmueble:"+nombreInmueble + (localidad!="" ? ";localidad:"+localidad : "");
					input = input + (departamento!="" ? ";departamento:"+departamento : "");
					// Obtener la respuesta de ResponseEntity
					jsonString = response.getBody();
					// Crear un ObjectMapper de Jackson
					objectMapper = new ObjectMapper();
					// Analizar la cadena de texto JSON en una lista de objetos Java
					listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
					// Recorrer la lista de objetos
					for (Map<String, Object> objeto : listaObjetos) {
						// Acceder a los atributos de cada objeto
						String error = (String)objeto.get("error");
						Double lon = (Double) objeto.get("puntoX");
						Double lat = (Double) objeto.get("puntoY");
						if(error.isEmpty()){
							System.out.println("Long: " + lon);
							System.out.println("lat: " + lat);
							insertarCoordenadas(input, "IDE", lat, lon);
						}
					}

					return response;
				case 5:
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

					input = "numeroRuta:"+numeroRuta.toString()+";kilometro:"+kilometro;
					// Obtener la respuesta de ResponseEntity
					jsonString = response.getBody();
					// Crear un ObjectMapper de Jackson
					objectMapper = new ObjectMapper();
					// Analizar la cadena de texto JSON en una lista de objetos Java
					listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
					// Recorrer la lista de objetos
					for (Map<String, Object> objeto : listaObjetos) {
						// Acceder a los atributos de cada objeto
						Double lon = (Double) objeto.get("lng");
						Double lat = (Double) objeto.get("lat");
						System.out.println("Long: " + lon);
						System.out.println("lat: " + lat);
						insertarCoordenadas(input, "IDE", lat, lon);
					}


					//insertarEnBase(response, "RUTAKM");
					return response;
				default:
					return new ResponseEntity<>("IDFORMACANONICA INVALIDO", headers, HttpStatus.BAD_REQUEST);
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

					String input = "calle:"+calle + ";numero:"+numero;
					// Obtener la respuesta de ResponseEntity
					String jsonString = response.getBody();
					// Crear un ObjectMapper de Jackson
					ObjectMapper objectMapper = new ObjectMapper();
					// Analizar la cadena de texto JSON en una lista de objetos Java
					List<Map<String, Object>> listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
					// Recorrer la lista de objetos
					for (Map<String, Object> objeto : listaObjetos) {
						// Acceder a los atributos de cada objeto
						Double lon = Double.parseDouble((String)objeto.get("lon"));
						Double lat = Double.parseDouble((String)objeto.get("lat"));
						System.out.println("log: " + lon);
						System.out.println("lat: " + lat);
						insertarCoordenadas(input, "NOMINATIM", lat, lon);
						
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

					String input = "nombreInmueble:"+nombreInmueble + (localidad!="" ? ";localidad:"+localidad : "");
					input = input + (departamento!="" ? ";departamento:"+departamento : "");
					// Obtener la respuesta de ResponseEntity
					String jsonString = response.getBody();
					// Crear un ObjectMapper de Jackson
					ObjectMapper objectMapper = new ObjectMapper();
					// Analizar la cadena de texto JSON en una lista de objetos Java
					List<Map<String, Object>> listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
					// Recorrer la lista de objetos
					for (Map<String, Object> objeto : listaObjetos) {
						// Acceder a los atributos de cada objeto
						Double lon = Double.parseDouble((String)objeto.get("lon"));
						Double lat = Double.parseDouble((String)objeto.get("lat"));
						insertarCoordenadas(input, "NOMINATIM", lat, lon);
						
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
				switch (idFormaCanonica) {
					case 6 -> {
						// Construir la URL con los parámetros
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

						String input = "calle:"+calle + ";numero:"+numero;
						// Obtener la respuesta de ResponseEntity
						String jsonString = response.getBody();
						// Crear un ObjectMapper de Jackson
						ObjectMapper objectMapper = new ObjectMapper();
						// Analizar la cadena de texto JSON en un objeto Java
						Map<String, Object> objeto = objectMapper.readValue(jsonString, new TypeReference<>() {
						});

						// Acceder a los valores del objeto
						List<Map<String, Object>> listaObjetos = (List<Map<String, Object>>) objeto.get("features");
						Map<String,Object> primerObjeto = listaObjetos.get(0);
						Map<String, Object> geometry = (Map<String, Object>) primerObjeto.get("geometry");
						List<Double> coordenadas = (List<Double>) geometry.get("coordinates");
						Double latitud = coordenadas.get(1);
						Double longitud = coordenadas.get(0);
						insertarCoordenadas(input, "PHOTON", latitud, longitud);
						return response;
					}
					default -> {
						return new ResponseEntity<>(
								"El idFormaCanonica " + idFormaCanonica + " no es valido para el geocoder seleccionado.",
								headers, HttpStatus.BAD_REQUEST);
					}
				}

			}
		}
		return new ResponseEntity<>("IDGEOCODER INVALIDO", headers, HttpStatus.BAD_REQUEST);
	}

	@GetMapping("/sugerenciaCalleCompleta")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> sugerenciaCalleCompleta(
			@RequestParam(value = "entrada", required = true) String entrada,
			@RequestParam(value = "todos", required = false) Boolean todos) throws JsonProcessingException {
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
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
		return response;

	}

	@GetMapping("/direcEnPoligono")
	@CrossOrigin(origins = "*")
	public ResponseEntity<String> direcEnPoligono(@RequestParam(value = "limit", required = false) Integer limit,
												  @RequestParam(value = "poligono") String poligono,
												  @RequestParam(value = "tipoDirec", required = false) String tipoDirec) {
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

	@GetMapping("/busquedaSimple")
	@CrossOrigin(origins = "*") // Permitir todas las IPs
	public ResponseEntity<String> busquedaSimple(
			@RequestParam(value = "entrada", required = true) String entrada) throws JsonProcessingException {
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
		List<Map<String, Object>> listaObjetos = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
		// Recorrer la lista de objetos
		for (Map<String, Object> objeto : listaObjetos) {
			// Acceder a los atributos de cada objeto
			Double lon = Double.parseDouble((String)objeto.get("lon"));
			Double lat = Double.parseDouble((String)objeto.get("lat"));
			insertarCoordenadas(input, "NOMINATIM", lat, lon);
		}
		return response;
	}
}