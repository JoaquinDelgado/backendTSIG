CREATE TABLE json_response (
  id SERIAL PRIMARY KEY,
  json jsonb,
  geocoder character varying COLLATE pg_catalog."default",
  servicio character varying COLLATE pg_catalog."default"
)