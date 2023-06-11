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