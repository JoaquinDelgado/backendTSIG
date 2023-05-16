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