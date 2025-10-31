#!/bin/bash

MAIN_CLASS="ar.edu.itba.pod.client.Query4Client"

java $JAVA_OPTS -cp 'lib/jars/*' $MAIN_CLASS "$@"
