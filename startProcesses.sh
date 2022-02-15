#!/bin/bash

#Wrapper running gdb on native image executable &
#starting JDWP server listening for JDWP request packets
while getopts n:p:s:b:d: flag
do
    case "${flag}" in
        n) NATIVE_EXEC=${OPTARG};;
        p) PROCESS_ID=${OPTARG};;
        s) SOCKET_ADDRESS=${OPTARG};;
        b) REBUILD_NATIVE=${OPTARG};;
        d) DEBUGEE_PATH=${OPTARG};;
        *) ;;
    esac
done

#Insert info of program to debug below (TODO: remove hardcoded class/jarfile)
#***************Start***************
javac Hello/Hello.java
java -cp Hello Hello &
PROCESS_ID=$(pgrep -nf "java -cp Hello Hello")
IMAGE_NAME=debugeeImg

if [ "$REBUILD_NATIVE" = "yes" ]
then
#Usage: native-image [options] class [imagename] [options]
# or  native-image [options] -jar jarfile [imagename] [options]
 cd $DEBUGEE_PATH && $GRAALVM_HOME/bin/native-image -g Hello $IMAGE_NAME && cd ..
fi
#***************End***************

NATIVE_EXEC=$PWD'/'$DEBUGEE_PATH'/'$IMAGE_NAME
NATIVE_SRC=$PWD'/'$DEBUGEE_PATH'/sources'

echo "PROCESS_ID: $PROCESS_ID";
echo "SOCKET_ADDRESS: $SOCKET_ADDRESS";
echo "SYSTEM PROPERTY native.exec=$NATIVE_EXEC";
echo "SYSTEM PROPERTY native.src=$NATIVE_SRC";
java -Dnative.exec=$NATIVE_EXEC -Dnative.src=$NATIVE_SRC -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081 --add-modules=jdk.hotspot.agent --add-exports jdk.hotspot.agent/sun.jvm.hotspot.oops=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.tools=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.runtime=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.classfile=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.debugger=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.utilities=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.tools.jcore=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.memory=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.jdi=ALL-UNNAMED --add-exports jdk.hotspot.agent/com.sun.jdi.connect=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.classfile=ALL-UNNAMED --add-opens jdk.hotspot.agent/sun.jvm.hotspot.oops=ALL-UNNAMED -cp NativeJDB-1.0-SNAPSHOT.jar jdwp.JDWPServer "$PROCESS_ID" "$SOCKET_ADDRESS" &

#Wait for process to exit
wait -n

#Exit with status of process
exit $?