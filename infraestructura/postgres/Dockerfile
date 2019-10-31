FROM debian:10

RUN apt-get update

RUN apt-get install -y postgresql

# configuration
COPY postgres/db-creation.sql /root
RUN cp /etc/postgresql/11/main/pg_hba.conf /etc/postgresql/11/main/pg_hba.conf.back
COPY postgres/pg_hba-insecure.conf /etc/postgresql/11/main
RUN cat /etc/postgresql/11/main/pg_hba-insecure.conf > /etc/postgresql/11/main/pg_hba.conf
RUN service postgresql start && psql -Upostgres -hlocalhost -f /root/db-creation.sql
COPY postgres/pg_hba-production.conf /etc/postgresql/11/main
RUN cat /etc/postgresql/11/main/pg_hba-production.conf > /etc/postgresql/11/main/pg_hba.conf
RUN rm /etc/postgresql/11/main/pg_hba-insecure.conf
RUN rm /etc/postgresql/11/main/pg_hba-production.conf
COPY postgres/postgresql.conf /etc/postgresql/11/main/postgresql-production.conf
RUN cat /etc/postgresql/11/main/postgresql-production.conf > /etc/postgresql/11/main/postgresql.conf
RUN rm /etc/postgresql/11/main/postgresql-production.conf
