#!/bin/bash

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
# set collector home path
cd "$SCRIPT_DIR/.." || exit
COLLECTOR_HOME=${COLLECTOR_HOME-$(pwd -P)}

ENV_FILE="conf/env.sh"
source "${ENV_FILE}"

PID_FILE=${COLLECTOR_HOME}/COLLECTOR.pid

# set main collector driver class full name
COLLECTOR_DRIVER=io.etrace.collector.CollectorApplication

# initial pid
PID=0

# set conf file into classpath
CLASSPATH=${COLLECTOR_HOME}:${COLLECTOR_HOME}/conf

# set collector classpath include all jar under lib and home path
CLASSPATH="${CLASSPATH}":"${COLLECTOR_HOME}/lib/*":"${COLLECTOR_HOME}/*"
for i in "${COLLECTOR_HOME}"/*.jar; do
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
  java ${JAVA_OPTS} -classpath ${CLASSPATH} ${COLLECTOR_DRIVER}
}

info() {
  echo "****************************"
  head -n 1 /etc/issue
  uname -a
  echo "COLLECTOR_HOME=${COLLECTOR_HOME}"
  echo "COLLECTOR_DRIVER=${COLLECTOR_DRIVER}"
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
  exit 1
  ;;
esac
exit 0
