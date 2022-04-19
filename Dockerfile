FROM ubuntu:22.04
LABEL maintainer="Ansu Varghese <avarghese@us.ibm.com>"

#Dockerfile input is the address to start the JDWP server on
ARG ADDRESS_ARG="0.0.0.0:8080"
#Dockerfile input is the relative path of the debuggee directory
ARG CLASS_NAME="Hello"
#TODO: add input args to support jarfile option for native image exec

EXPOSE 8080
HEALTHCHECK CMD wget -q -O /dev/null http://localhost:8080/healthy || exit 1

WORKDIR /jdwp

COPY startProcesses.sh .
COPY target/NativeJDB-1.0-SNAPSHOT.jar .

RUN export DEBIAN_FRONTEND=noninteractive \
&& apt-get -qqy update \
&& apt-get -qqy install \
  build-essential \
  emacs \
  valgrind \
  vim \
  libunwind-dev \
  zlib1g-dev

#Graalvm exec jar in current dir downloaded from https://github.com/graalvm/graalvm-ce-builds/releases
COPY graalvm ./graalvm
ENV GRAALVM_HOME /jdwp/graalvm
ENV PATH $PATH:$GRAALVM_HOME/bin
RUN $GRAALVM_HOME/bin/gu install native-image

ENV ADDRESS_ARG=$ADDRESS_ARG
ENV CLASS_NAME=$CLASS_NAME

ENTRYPOINT ./startProcesses.sh -a $ADDRESS_ARG -c $CLASS_NAME