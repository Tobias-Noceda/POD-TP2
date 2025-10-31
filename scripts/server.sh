#!/bin/bash

MAIN_CLASS="ar.edu.itba.pod.server.Server"

java $JAVA_OPTS -cp 'lib/jars/*' $MAIN_CLASS "$@"
