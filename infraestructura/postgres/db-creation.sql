CREATE ROLE app LOGIN ENCRYPTED PASSWORD 'app-production-pass';
CREATE DATABASE app OWNER postgres ENCODING 'utf-8';
\c app
CREATE TABLE producto (id serial NOT NULL PRIMARY KEY, nombre TEXT, descripcion TEXT);
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE producto TO app;
GRANT SELECT, USAGE ON SEQUENCE producto_id_seq TO app;
