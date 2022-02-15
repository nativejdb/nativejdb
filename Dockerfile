FROM ubuntu:22.04
LABEL maintainer="Ansu Varghese <avarghese@us.ibm.com>"

#Dockerfile input is the address to start the JDWP server on
ARG ADDRESS_ARG="0.0.0.0:8080"
#Dockerfile input is a knob to re-build native image executable (takes a few mins)
ARG REBUILD_EXEC="no"
#Dockerfile input is the relative path of the debuggee directory
ARG DEBUGEE_ARG="Hello"
#TODO: add input args to support class or jarfile option for native image exec

EXPOSE 8080
HEALTHCHECK CMD wget -q -O /dev/null http://localhost:8080/healthy || exit 1

WORKDIR /jdwp

COPY startProcesses.sh .
COPY target/NativeJDB-1.0-SNAPSHOT.jar .
COPY $DEBUGEE_ARG ./$DEBUGEE_ARG

RUN export DEBIAN_FRONTEND=noninteractive \
&& apt-get -qqy update \
&& apt-get -qqy install \
  build-essential \
  emacs \
  valgrind \
  vim \
  zlib1g-dev

ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64
ENV PATH $PATH:$JAVA_HOME/bin

#Graalvm exec jar in current dir downloaded from https://github.com/graalvm/graalvm-ce-builds/releases
COPY graalvm ./graalvm
ENV GRAALVM_HOME /jdwp/graalvm
ENV PATH $PATH:$GRAALVM_HOME/bin
RUN $GRAALVM_HOME/bin/gu install native-image

ENV SOCKET_ADDRESS=$ADDRESS_ARG
ENV REBUILD_NATIVE=$REBUILD_EXEC
ENV DEBUGEE_PATH=$DEBUGEE_ARG

WORKDIR /jdwp

ENTRYPOINT ./startProcesses.sh -s $SOCKET_ADDRESS -b $REBUILD_NATIVE -d $DEBUGEE_PATH