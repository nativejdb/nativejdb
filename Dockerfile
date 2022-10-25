FROM ubuntu:22.04
LABEL maintainer="Ansu Varghese <avarghese@us.ibm.com>"

#Dockerfile input is the address to start the JDWP server on
ARG ADDRESS_ARG="0.0.0.0:8082"
#Dockerfile input is the relative path of the debuggee directory
ARG CLASS_NAME="Hello"
ARG NATIVE_EXEC="apps/${CLASS_NAME}"
ARG NATIVE_SRC="apps/${CLASS_NAME}sources"
ARG IS_QUARKUS="false"
ARG ASM_LINE="10"
#TODO: add input args to support jarfile option for native image exec

EXPOSE 8082
HEALTHCHECK CMD wget -q -O /dev/null http://localhost:8080/healthy || exit 1

WORKDIR /jdwp

COPY startProcesses.sh .
COPY target/NativeJDB-1.0-SNAPSHOT-uber.jar .

RUN export DEBIAN_FRONTEND=noninteractive \
&& apt-get -qqy update \
&& apt-get -qqy install \
  build-essential \
  curl \
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
ENV NATIVE_EXEC=$NATIVE_EXEC
ENV NATIVE_SRC=$NATIVE_SRC
ENV IS_QUARKUS=$IS_QUARKUS
ENV ASM_LINE=$ASM_LINE

ENTRYPOINT ./startProcesses.sh -a $ADDRESS_ARG -c $CLASS_NAME -e $NATIVE_EXEC -s $NATIVE_SRC -k $IS_QUARKUS -m $ASM_LINE