FROM debian:10

RUN apt-get update

RUN apt-get install -y wget unzip curl

# ssh
RUN apt-get install -y openssh-server

# git
RUN apt-get install -y git

# git user
RUN useradd -d/home/git git \
  && mkdir /home/git \
  && chown git:git /home/git \
  && echo git:git | chpasswd

# jenkins user
RUN useradd jenkins \
  && mkdir /home/jenkins \
  && chown jenkins:jenkins /home/jenkins \
  && echo jenkins:jenkins-git | chpasswd

# app's repository
RUN mkdir /home/git/app \
  && cd /home/git/app \
  && git init --bare . \
  && chown -R git:git /home/git/app
