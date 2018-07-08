#!/bin/bash

DEVTOOLS_PORT=0

for var in "$@"
do
  if [[ $var == --remote-debugging-port* ]] ;
  then
    DEVTOOLS_PORT=${var#*=}
    echo "devtools_port=$DEVTOOLS_PORT"
  fi
done

openfin -l -c openfin_selenium.json -p $DEVTOOLS_PORT

