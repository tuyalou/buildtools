FROM centos:7

LABEL Fuchi Corp <fuchicorpsolution@gmail.com>

## Version of the Docker
ENV DOCKERVERSION=20.10.22

## Installing wget command
RUN yum install wget -y && yum clean all

##Installing helm 
RUN curl https://get.helm.sh/helm-v3.11.0-linux-amd64.tar.gz  > ./helm.tar.gz \
    && tar -xvf ./helm.tar.gz \
    && mv ./linux-amd64/*  /usr/local/bin

## Installing terraform
RUN wget https://releases.hashicorp.com/terraform/1.3.0/terraform_1.3.0_linux_amd64.zip \
    && yum install unzip -y \
    && unzip terraform_1.3.0_linux_amd64.zip \
    && rm -rf  terraform_1.3.0_linux_amd64.zip \
    && mv terraform /bin

## Download docker cli
RUN curl -fsSLO https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKERVERSION}.tgz \
  && tar xzvf docker-${DOCKERVERSION}.tgz --strip 1 -C /usr/local/bin docker/docker \
  && rm docker-${DOCKERVERSION}.tgz

## Install kubectl
RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.13.0/bin/linux/amd64/kubectl \
    && chmod +x ./kubectl \
    && mv ./kubectl /usr/local/bin/kubectl \
    && mkdir ~/.kube

## Install git command inside docker container 
RUN yum install git -y

## Coping the script to set kube config
COPY . /scripts/Dockerfile/

## Running the script to set ~/.kube/config file
CMD su - root -c "sh /scripts/Dockerfile/set-config.sh"
