FROM debian:10

RUN apt-get update

RUN apt-get install -y wget unzip

# java
RUN apt-get -y install openjdk-11-jdk

# maven
RUN wget https://www-us.apache.org/dist/maven/maven-3/3.6.2/binaries/apache-maven-3.6.2-bin.zip -O /opt/apache-maven-3.6.2-bin.zip
RUN unzip /opt/apache-maven-3.6.2-bin.zip -d /opt
ENV PATH="${PATH}:/opt/apache-maven-3.6.2/bin"
