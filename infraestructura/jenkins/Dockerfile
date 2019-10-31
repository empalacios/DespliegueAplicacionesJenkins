FROM debian:10

RUN apt-get update

RUN apt-get install -y wget gnupg openssh-client

RUN apt-get install -y git

# jdk
RUN apt-get -y install openjdk-11-jre

RUN wget -q -O - https://pkg.jenkins.io/debian/jenkins.io.key | apt-key add -
RUN sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
RUN apt-get update
RUN apt-get install -y jenkins


################################################
################################################
############                        ############
############ app build dependencies ############
############                        ############
################################################
################################################
RUN apt-get install -y unzip

# java
RUN apt-get -y install openjdk-11-jdk

# maven
RUN wget https://www-us.apache.org/dist/maven/maven-3/3.6.2/binaries/apache-maven-3.6.2-bin.zip -O /opt/apache-maven-3.6.2-bin.zip
RUN unzip /opt/apache-maven-3.6.2-bin.zip -d /opt
ENV PATH="${PATH}:/opt/apache-maven-3.6.2/bin"


