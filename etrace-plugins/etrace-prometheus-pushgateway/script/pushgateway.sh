#!/bin/bash

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
# set consumer home path
cd "$SCRIPT_DIR/.."
PUSHGATEWAY_HOME=${PUSHGATEWAY_HOME-$(pwd -P)}

ENV_FILE="$PUSHGATEWAY_HOME/conf/env.sh"
source "${ENV_FILE}"

PID_FILE=${PUSHGATEWAY_HOME}/CONSUMER.pid

# set main consumer driver class full name
PUSHGATEWAY_DRIVER=io.etrace.plugins.prometheus.pushgateway.Application

# initial pid
PID=0

# set conf file into classpath
CLASSPATH=${PUSHGATEWAY_HOME}:${PUSHGATEWAY_HOME}/conf

# set consumer classpath include all jar under lib and home path
CLASSPATH="${CLASSPATH}":"${PUSHGATEWAY_HOME}/lib/*":"${PUSHGATEWAY_HOME}/*"
for i in "${PUSHGATEWAY_HOME}"/*.jar; do
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
    java ${JAVA_OPTS} -classpath ${CLASSPATH} ${PUSHGATEWAY_DRIVER}
}

info() {
   echo "****************************"
   head -n 1 /etc/issue
   uname -a
   echo "PUSHGATEWAY_HOME=${PUSHGATEWAY_HOME}"
   echo "PUSHGATEWAY_DRIVER=${PUSHGATEWAY_DRIVER}"
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
