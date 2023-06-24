# backendTSIG

Para acceder al swagger:
http://localhost:8080/swagger-ui/index.html

Pasos para poder establecer conexion con base de datos

1. Descargarse el PostgresQL 15 del siguiente link: https://sbp.enterprisedb.com/getfile.jsp?fileid=1258478 e instalarlo
2. Abrir con el pgAdmin4 y crear una tabla ejecutando el siguiente script:

   CREATE TABLE IF NOT EXISTS public.audits (
   id serial PRIMARY KEY,
   input character varying,
   geocoder character varying,
   latitud double precision,
   longitud double precision
   );

   CREATE TABLE IF NOT EXISTS public.geocoders (
   id SERIAL PRIMARY KEY,
   geocoder character varying COLLATE pg_catalog."default"
   );

   CREATE TABLE IF NOT EXISTS public.canonic_forms (
   id SERIAL PRIMARY KEY,
   canonic_form character varying COLLATE pg_catalog."default"
   );

   CREATE TABLE IF NOT EXISTS public.geocoders_canonic_forms (
   id SERIAL PRIMARY KEY,
   id_geocoder integer,
   id_canonic_form integer
   );

   INSERT INTO
   public.geocoders(geocoder)
   VALUES
   ('IDE'),
   ('NOMINATIM'),
   ('PHOTON');

   INSERT INTO
   public.canonic_forms(canonic_form)
   VALUES
   ('calle,numero,localidad,departamento'),
   ('calle,numero,calle2,localidad,departamento'),
   ('calle,manzana,solar,localidad,departamento'),
   ('nombreInmueble,departamento'),
   ('numeroRuta,kilometro'),
   ('calle,numero');

   INSERT INTO
   public.geocoders_canonic_forms(id_geocoder, id_canonic_form)
   VALUES
   (1, 1),
   (1, 2),
   (1, 3),
   (1, 4),
   (1, 5),
   (2, 6),
   (1, 6),
   (2, 4),
   (3, 6);

   CREATE TABLE IF NOT EXISTS public.cache_busqueda (
   id SERIAL PRIMARY KEY,
   id_geocoder integer,
   id_canonic_form integer,
   calle character varying COLLATE pg_catalog."default",
   numero character varying COLLATE pg_catalog."default",
   localidad character varying COLLATE pg_catalog."default",
   departamento character varying COLLATE pg_catalog."default",
   calle2 character varying COLLATE pg_catalog."default",
   manzana character varying COLLATE pg_catalog."default",
   solar character varying COLLATE pg_catalog."default",
   nombre_inmueble character varying COLLATE pg_catalog."default",
   numeroRuta character varying COLLATE pg_catalog."default",
   kilometro character varying COLLATE pg_catalog."default",
   response character varying COLLATE pg_catalog."default",
   fecha_creado timestamp
   );

   CREATE TABLE IF NOT EXISTS public.cache_sugerencias (
   id SERIAL PRIMARY KEY,
   entrada character varying COLLATE pg_catalog."default",
   todos char(1),
   response character varying COLLATE pg_catalog."default",
   fecha_creado timestamp
   );

   CREATE TABLE IF NOT EXISTS public.direcciones_im
   (
      id serial PRIMARY key,
      calle character varying COLLATE pg_catalog."default",
      numero integer,
      latitud double precision,
      longitud double precision
   );

   CREATE TABLE IF NOT EXISTS public.distancias_im_geocoders
   (
      id serial PRIMARY key,
      id_direcciones_im integer,
      id_geocoder integer,
      dir_geocoder character varying COLLATE pg_catalog."default",
      latitud_geocoder double precision,
      longitud_geocoder double precision,
      distancia_metros double precision
   );

   INSERT INTO public.direcciones_im(
      calle, numero, latitud, longitud)
      VALUES 
      ('Av. Gonzalo Ramírez', 1270, -34.911815424412055, -56.187992386335544),
      ('Av. Uruguay', 1936, -34.89861649270423, -56.17652060352443),
      ('Av. Santiago Rivas', 1500, -34.89793079168539, -56.13218848818313),
      ('Marsella', 2738, -34.87615263134674,-56.1838250881837),
      ('Av. Agraciada', 4241, -34.85671219459859,-56.221153130513486),
      ('Haití', 1606, -34.87180262558506,-56.250478601677884),
      ('Av. Carlos María Ramírez', 881, -34.86592307702279,-56.23571257711002),
      ('Soria', 1243, -34.83771136172423,-56.1991293016787),
      ('Camino Tomkinson', 2459, -34.8373196610002,-56.2731408350664),
      ('Hipólito Yrigoyen', 2069, -34.88062865081787,-56.111827816231106);

CREATE OR REPLACE VIEW public.vista_datos_comparativos as
select dir.calle||' '||dir.numero direccion_im,
		dir.latitud latitud_im,
		dir.longitud longitud_im,
		geo.geocoder geocoder,
		dis.dir_geocoder direccion_geocoder,
		dis.latitud_geocoder latitud_geocoder,
		dis.longitud_geocoder longitud_geocoder,
		dis.distancia_metros distancia_metros 
from geocoders geo, direcciones_im dir, distancias_im_geocoders dis
where dis.id_geocoder=geo.id and dis.id_direcciones_im=dir.id;  

3. En el application.properties deben colocar lo siguiente:
   spring.datasource.url=jdbc:postgresql://localhost:5432/tsig

   spring.datasource.username=postgres

   spring.datasource.password=1234

Donde dice "tsig" colocar el nombre de la base

Donde dice "postgres" el nombre de su usuario

Donde dice "password" su contraseña

Levantar backend utilizando DOCKER

1.  Se necesita docker instalado
2.  Dentro de la rais del proyecto ejecutar docker-compose up o docker-compose up -d (para ejecutar en segundo plano)
    2.1) Se crea contenedor para base de datos postgres, puerto 5432, crea bd: tsig_2023 user:tsig pass: tsig y agrega tablas
    2.2) Se crea contenedor para pgadmin web, http://localhost:5050/, usuario:pgadmin@tsig.com pass: tsig. Para agregar conexion Host name/Address: postgress
    2.2) Se crea contendor para backend, http://localhost:8080/api/
3.  Para bajar contenedores docker-compose down, para bajar y borrar docker-compose down --rmi all
4.  Posibles errores
    4.1) Puertos bloqueados, se pueden cambiar en el archivo docker-compose.yml
    4.2) Borrar directorio data

