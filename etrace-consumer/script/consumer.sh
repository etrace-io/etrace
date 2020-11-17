#!/bin/bash

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
# set consumer home path
cd "$SCRIPT_DIR/.."
CONSUMER_HOME=${CONSUMER_HOME-$(pwd -P)}

ENV_FILE="$CONSUMER_HOME/conf/env.sh"
source "${ENV_FILE}"

PID_FILE=${CONSUMER_HOME}/CONSUMER.pid

# set main consumer driver class full name
CONSUMER_DRIVER=io.etrace.consumer.ConsumerApplication

# initial pid
PID=0

# set conf file into classpath
CLASSPATH=${CONSUMER_HOME}:${CONSUMER_HOME}/conf

# set consumer classpath include all jar under lib and home path
CLASSPATH="${CLASSPATH}":"${CONSUMER_HOME}/lib/*":"${CONSUMER_HOME}/*"
for i in "${CONSUMER_HOME}"/*.jar; do
   CLASSPATH="${CLASSPATH}":"${i}"
done

check_pid() {
 if [ -f "${PID_FILE}" ]; then
      PID=$(cat "${PID_FILE}")
      if [ -n "$PID" ]; then
          echo ${PID}
      fi
  fi
}

start() {
    java ${JAVA_OPTS} -classpath ${CLASSPATH} ${CONSUMER_DRIVER}
}

info() {
   echo "****************************"
   head -n 1 /etc/issue
   uname -a
   echo "CONSUMER_HOME=${CONSUMER_HOME}"
   echo "CONSUMER_DRIVER=${CONSUMER_DRIVER}"
   echo "****************************"
}

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
        echo "Usage: $0 {start|check_pid|info}"
   	    exit 1;
	    ;;
esac
exit 0
