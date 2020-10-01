#!/bin/bash

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
BASE_DIR=$(cd $SCRIPT_DIR && pwd -P)
# set stream home path
cd "$SCRIPT_DIR/.."
STREAM_HOME=${STREAM_HOME-$(pwd -P)}
STREAM_HTTP_PORT=$2
ENV_FILE="$STREAM_HOME/bin/env.sh"
source "${ENV_FILE}"

# set main stream driver class full name
STREAM_DRIVER=io.etrace.stream.container.StreamContainer

# initial pid
PID=0


# set stream server classpath include all jar under lib and home path
CLASSPATH="${BASE_DIR}/conf:${STREAM_HOME}/conf/*:${STREAM_HOME}/lib/*:${STREAM_HOME}/*"

check_pid() {
 PID=$(ps gaux | grep java | grep "${JVM_LOGS_DIR}" | grep -v grep | awk '{print $2}')
 if [[ "$PID" =~ ^[0-9]+$ ]];then
      echo $PID
 fi
}

start() {
    echo "java ${JAVA_OPTS} -classpath ${CLASSPATH} ${STREAM_DRIVER}"
    java ${JAVA_OPTS} -classpath ${CLASSPATH} ${STREAM_DRIVER}
}

info() {
   echo "****************************"
   echo $(head -n 1 /etc/issue)
   echo $(uname -a)
   echo "STREAM_HOME=${STREAM_HOME}"
   echo "STREAM_DRIVER=${STREAM_DRIVER}"
   echo "****************************"
}


if [[ ! "$STREAM_HTTP_PORT" =~ ^[0-9]+$ ]];then
    echo "Usage: $0 {start|check_pid|info} int"
    exit 1;
fi

case "$1" in
    start)
        start
	    ;;
	check_pid)
	    check_pid
	    ;;
	info)
	    info
	    ;;
    *)
        echo "Usage: $0 {start|check_pid|info} int"
   	    exit 1;
	    ;;
esac
exit 0
