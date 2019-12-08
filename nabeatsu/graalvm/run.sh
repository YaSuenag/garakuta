#!/bin/bash

SHELLNAME=$0
MODE=$1
MAX=$2

function usage(){
  echo "$SHELLNAME [java|native] [iteration]"
}

if [ -z "$GRAALVM_HOME" ]; then
  echo '$GRAALVM_HOME must be set.'
  exit 3
fi

if [ -z "$MAX" ]; then
  usage
  exit 3
fi

echo $MAX | grep -qE '[^0-9]'
if [ $? != 1 ]; then
  usage
  exit 3
fi

case $MODE in
  java)
    cd java
    make
    $GRAALVM_HOME/bin/java Nabeatsu $MAX;;

  native)
    cd native
    make
    LD_LIBRARY_PATH="$LD_LIBRARY_PATH:lib/" ./nabeatsu $MAX;;

  *)
    usage;;
esac

