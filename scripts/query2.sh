#!/bin/bash

MAIN_CLASS="ar.edu.itba.pod.client.Query2Client"

java $JAVA_OPTS -cp 'lib/jars/*' $MAIN_CLASS "$@"
