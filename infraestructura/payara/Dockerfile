FROM debian:10

RUN apt-get update

RUN apt-get install -y wget unzip

# ssh
RUN apt-get install -y openssh-server

# jdk
RUN apt-get install -y openjdk-11-jdk

# payara
RUN wget https://search.maven.org/remotecontent?filepath=fish/payara/distributions/payara/5.193.1/payara-5.193.1.zip -O /opt/payara-5.193.1.zip
RUN unzip /opt/payara-5.193.1.zip -d /opt && rm /opt/payara-5.193.1.zip

# payara configuration
COPY payara/payara-change-password-file /tmp/payara-change-password
RUN ./opt/payara5/glassfish/bin/asadmin --user=admin --passwordfile=/tmp/payara-change-password change-admin-password --domain_name domain1
COPY payara/payara-password-file /tmp/payara-password-file
RUN ./opt/payara5/glassfish/bin/asadmin start-domain \
  && ./opt/payara5/glassfish/bin/asadmin --user=admin --passwordfile=/tmp/payara-password-file enable-secure-admin \
  && ./opt/payara5/glassfish/bin/asadmin stop-domain \
  && ./opt/payara5/glassfish/bin/asadmin start-domain \
  && ./opt/payara5/glassfish/bin/asadmin --user=admin --passwordfile=/tmp/payara-password-file create-system-properties bd_password=app-production-pass

# jenkins user
RUN useradd -d/home/jenkins jenkins \
  && mkdir /home/jenkins \
  && chown jenkins:jenkins /home/jenkins \
  && chown -R jenkins:jenkins /opt/payara5
RUN echo jenkins:holamundo-jenkins | chpasswd
COPY payara/payara-password-file /home/jenkins/payara-password-file
RUN chown jenkins:jenkins /home/jenkins/payara-password-file && chmod go-rwx /home/jenkins/payara-password-file
