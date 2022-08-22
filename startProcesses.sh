#!/bin/bash

#Wrapper running gdb on native image executable &
#starting JDWP server listening for JDWP request packets
while getopts a:c:e:s:k:m: flag
do
    case "${flag}" in
        a) ADDRESS_ARG=${OPTARG};;
        c) CLASS_NAME=${OPTARG};;
        e) NATIVE_EXEC=${OPTARG};;
        s) NATIVE_SRC=${OPTARG};;
        k) IS_QUARKUS=${OPTARG};;
        m) ASM_LINE=${OPTARG};;
        *) ;;
    esac
done

#check ASMLINE input is valid
if [[ $ASM_LINE =~ [^[:digit:]] ]]
then
  ASM_LINE=10
else
  ASM_LINE=$ASM_LINE
fi

#java -cp apps/$CLASS_NAME.jar $CLASS_NAME.$CLASS_NAME &
#PROCESS_ID=$(pgrep -nf "java -cp apps/$CLASS_NAME.jar $CLASS_NAME.$CLASS_NAME")
if [ "$IS_QUARKUS" == "true" ];
then
  echo "Starting Quarkus' $CLASS_NAME process..."

  java -Dquarkus.http.port=8888 -cp apps/$CLASS_NAME.jar:apps/lib io.quarkus.runner.GeneratedMain &
  PROCESS_ID=$(pgrep -nf "java -Dquarkus.http.port=8888 -cp apps/$CLASS_NAME.jar:apps/lib io.quarkus.runner.GeneratedMain")

  sleep 5
  curl -w "\n" http://localhost:8888/hello
  curl -w "\n" http://localhost:8888/hello/greeting/quarkus
else
  echo "Starting $CLASS_NAME process..."

  java -jar apps/$CLASS_NAME.jar &
  PROCESS_ID=$(pgrep -nf "java -jar apps/$CLASS_NAME.jar")
fi

IMAGE_NAME=$CLASS_NAME

echo "PROCESS_ID: $PROCESS_ID";
echo "ADDRESS_ARG: $ADDRESS_ARG";
echo "SYSTEM PROPERTY program.class=$CLASS_NAME";
echo "SYSTEM PROPERTY native.exec=$NATIVE_EXEC";
echo "SYSTEM PROPERTY native.src=$NATIVE_SRC";
java -Dprogram.class=$CLASS_NAME -Dnative.exec=$NATIVE_EXEC -Dnative.src=$NATIVE_SRC -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081 --add-modules=jdk.hotspot.agent --add-exports jdk.hotspot.agent/sun.jvm.hotspot.oops=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.tools=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.runtime=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.classfile=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.debugger=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.utilities=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.tools.jcore=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.memory=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.jdi=ALL-UNNAMED --add-exports jdk.hotspot.agent/com.sun.jdi.connect=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.classfile=ALL-UNNAMED --add-opens jdk.hotspot.agent/sun.jvm.hotspot.oops=ALL-UNNAMED -cp NativeJDB-1.0-SNAPSHOT.jar jdwp.JDWPServer "$PROCESS_ID" "$ADDRESS_ARG" &

#Wait for process to exit
wait -n

#Exit with status of process
exit $?