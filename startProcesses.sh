#!/bin/bash

#Wrapper running gdb on native image executable &
#starting JDWP server listening for JDWP request packets
while getopts a:c: flag
do
    case "${flag}" in
        a) ADDRESS_ARG=${OPTARG};;
        c) CLASS_NAME=${OPTARG};;
        *) ;;
    esac
done

#Insert info of program to debug below (TODO: remove hardcoded class/jarfile)
#***************Start***************
javac apps/*.java
java -cp apps $CLASS_NAME &
PROCESS_ID=$(pgrep -nf "java -cp apps $CLASS_NAME")

IMAGE_NAME=debugeeImg
NATIVE_EXEC=$PWD'/apps/'$IMAGE_NAME
NATIVE_SRC=$PWD'/apps/sources'

echo "PROCESS_ID: $PROCESS_ID";
echo "ADDRESS_ARG: $ADDRESS_ARG";
echo "SYSTEM PROPERTY native.exec=$NATIVE_EXEC";
echo "SYSTEM PROPERTY native.src=$NATIVE_SRC";
java -Dnative.exec=$NATIVE_EXEC -Dnative.src=$NATIVE_SRC -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081 --add-modules=jdk.hotspot.agent --add-exports jdk.hotspot.agent/sun.jvm.hotspot.oops=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.tools=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.runtime=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.classfile=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.debugger=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.utilities=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.tools.jcore=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.memory=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.jdi=ALL-UNNAMED --add-exports jdk.hotspot.agent/com.sun.jdi.connect=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.classfile=ALL-UNNAMED --add-opens jdk.hotspot.agent/sun.jvm.hotspot.oops=ALL-UNNAMED -cp NativeJDB-1.0-SNAPSHOT.jar jdwp.JDWPServer "$PROCESS_ID" "$ADDRESS_ARG" &

#Wait for process to exit
wait -n

#Exit with status of process
exit $?