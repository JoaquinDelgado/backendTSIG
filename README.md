# backendTSIG

Pasos para poder establecer conexion con base de datos

1) Descargarse el PostgresQL 15 del siguiente link: https://sbp.enterprisedb.com/getfile.jsp?fileid=1258478 e instalarlo
2) Abrir con el pgAdmin4 y crear una tabla ejecutando el siguiente script: 
   1) CREATE TABLE IF NOT EXISTS public.json_response
      (
      id integer NOT NULL DEFAULT nextval('json_response_id_seq'::regclass),
      json jsonb,
      geocoder character varying COLLATE pg_catalog."default",
      servicio character varying COLLATE pg_catalog."default",
      CONSTRAINT json_response_pkey PRIMARY KEY (id)
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
