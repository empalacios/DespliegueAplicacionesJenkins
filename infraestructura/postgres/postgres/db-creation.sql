create role app login encrypted password 'app-production-pass';
create database app owner postgres encoding 'utf-8';
\c app
create table producto (id serial not null primary key, nombre text, descripcion text);
grant select, insert, update, delete on table producto to app;
grant select, usage on sequence producto_id_seq to app;
