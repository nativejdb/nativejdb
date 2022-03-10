#!/bin/bash

while getopts c: flag
do
    case "${flag}" in
        c) CLASS_NAME=${OPTARG};;
        *) ;;
    esac
done

javac apps/*.java

IMAGE_NAME=debugeeImg

# Usage: native-image [options] class [imagename] [options]
#    or  native-image [options] -jar jarfile [imagename] [options]
cd /jdwp/apps && $GRAALVM_HOME/bin/native-image -g -O0 $CLASS_NAME $IMAGE_NAME

#Exit with status of process
exit $?