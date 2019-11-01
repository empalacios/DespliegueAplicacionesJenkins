FROM payara/server-full

USER root
RUN apt-get update
RUN apt-get install -y sudo openssh-server

# usuario jenkins
RUN useradd -d/home/jenkins jenkins \
  && mkdir /home/jenkins \
  && chown jenkins:jenkins /home/jenkins \
  && echo jenkins:holamundo-jenkins | chpasswd

# Configuracion de sudo
RUN echo 'payara ALL=(ALL) NOPASSWD: /etc/init.d/ssh start, /etc/init.d/ssh stop' > /etc/sudoers.d/payara

USER payara
COPY start-ssh.sh $HOME_DIR/scripts/init.d
# payara configuration
RUN sh $PAYARA_DIR/glassfish/bin/asadmin start-domain \
  && sh $PAYARA_DIR/glassfish/bin/asadmin --user=admin --passwordfile=$HOME_DIR/passwordFile create-system-properties bd_password=app-production-pass
