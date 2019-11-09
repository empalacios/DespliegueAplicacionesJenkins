CREATE ROLE app LOGIN ENCRYPTED PASSWORD 'app-production-pass';
CREATE DATABASE app OWNER postgres ENCODING 'utf-8';
\c app
CREATE TABLE producto (id serial NOT NULL PRIMARY KEY, nombre TEXT, descripcion TEXT);
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE producto TO app;
GRANT SELECT, USAGE ON SEQUENCE producto_id_seq TO app;
INSERT INTO producto (nombre, descripcion) VALUES ('T-600', 'Exterminador Serie 600');
INSERT INTO producto (nombre, descripcion) VALUES ('T-800 101', 'Exterminador Serie 800 Modelo 101');
INSERT INTO producto (nombre, descripcion) VALUES ('T-800 102', 'Exterminador Serie 800 Modelo 101');
INSERT INTO producto (nombre, descripcion) VALUES ('T-1000', 'Exterminador Serie 1000 (el más avanzado a su época)');
