# backendTSIG

Para acceder al swagger:
http://localhost:8080/swagger-ui/index.html

Pasos para poder establecer conexion con base de datos

1) Descargarse el PostgresQL 15 del siguiente link: https://sbp.enterprisedb.com/getfile.jsp?fileid=1258478 e instalarlo
2) Abrir con el pgAdmin4 y crear una tabla ejecutando el siguiente script: 
   1) CREATE TABLE IF NOT EXISTS public.audits
      (
      id serial PRIMARY KEY,
      input character varying,
      geocoder character varying,
      latitud double precision,
      longitud double precision
      )


3) En el application.properties deben colocar lo siguiente:
   spring.datasource.url=jdbc:postgresql://localhost:5432/tsig

   spring.datasource.username=postgres

   spring.datasource.password=1234

Donde dice "tsig" colocar el nombre de la base

Donde dice "postgres" el nombre de su usuario

Donde dice "password" su contraseña

Levantar backend utilizando DOCKER
   1) Se necesita docker instalado
   2) Dentro de la rais del proyecto ejecutar docker-compose up o docker-compose up -d (para ejecutar en segundo plano)
      2.1) Se crea contenedor para base de datos postgres, puerto 5432, crea bd: tsig_2023 user:tsig pass: tsig y agrega tablas
      2.2) Se crea contenedor para pgadmin web, http://localhost:5050/, usuario:pgadmin@tsig.com pass: tsig. Para agregar conexion Host name/Address: postgress
      2.2) Se crea contendor para backend, http://localhost:8080/api/
   3) Para bajar contenedores docker-compose down, para bajar y borrar docker-compose down --rmi all
   4) Posibles errores
      4.1) Puertos bloqueados, se pueden cambiar en el archivo docker-compose.yml
      4.2) Borrar directorio data

CREATE TABLE IF NOT EXISTS public.geocoders
(
id integer NOT NULL DEFAULT nextval('geocoders_id_seq'::regclass),
geocoder character varying COLLATE pg_catalog."default",
CONSTRAINT geocoders_pkey PRIMARY KEY (id)
)

CREATE TABLE IF NOT EXISTS public.canonic_forms
(
id integer NOT NULL DEFAULT nextval('canonic_forms_id_seq'::regclass),
canonic_form character varying COLLATE pg_catalog."default",
CONSTRAINT canonic_forms_pkey PRIMARY KEY (id)
)

INSERT INTO public.geocoders(
geocoder)
VALUES ('IDE'),
('NOMINATIM');

INSERT INTO public.canonic_forms(
canonic_form)
VALUES ('calle,numero,localidad,departamento'),
('calle,numero,calle2,localidad,departamento'),
('calle,manzana,solar,localidad,departamento'),
('nombreInmueble,departamento'),
('numeroRuta,kilometro'),
('calle,numero');

CREATE TABLE IF NOT EXISTS public.geocoders_canonic_forms
(
id integer NOT NULL DEFAULT nextval('geocoders_canonic_forms_id_seq'::regclass),
id_geocoder integer,
id_canonic_form integer,
CONSTRAINT geocoders_canonic_forms_pkey PRIMARY KEY (id)
)

INSERT INTO public.geocoders_canonic_forms(
id_geocoder, id_canonic_form)
VALUES (1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(2, 6),
(1, 6),
(2, 4);
