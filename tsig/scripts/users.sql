CREATE TABLE json_response (
  id SERIAL PRIMARY KEY,
  json jsonb,
  geocoder character varying COLLATE pg_catalog."default",
  servicio character varying COLLATE pg_catalog."default",
  puntoX DECIMAL(12,8),
  puntoY DECIMAL(12,8)

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
  ('NOMINATIM');

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
  (2, 4);