FROM payara/server-full

# payara configuration
RUN sh /opt/payara/appserver/glassfish/bin/asadmin start-domain \
  && sh /opt/payara/appserver/glassfish/bin/asadmin --user=admin --passwordfile=/opt/payara/passwordFile create-system-properties bd_password=app-production-pass
