FROM postgres

ENV POSTGRES_PASSWORD 'dbpass'
ENV POSTGRES_USER     'postgres'
COPY db-creation.sql /docker-entrypoint-initdb.d
