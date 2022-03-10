#!/bin/bash

while getopts n:p:s:b:d: flag
do
    case "${flag}" in
        n) NATIVE_EXEC=${OPTARG};;
        *) ;;
    esac
done

IMAGE_NAME=debugeeImg

# Usage: native-image [options] class [imagename] [options]
#    or  native-image [options] -jar jarfile [imagename] [options]
cd /jdwp/apps && $GRAALVM_HOME/bin/native-image -g -O0 $NATIVE_EXEC $IMAGE_NAME

#Exit with status of process
exit $?