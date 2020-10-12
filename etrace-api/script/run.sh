#!/bin/bash

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
# set collector home path
# cd "$SCRIPT_DIR/.."

API_HOME=${SCRIPT_DIR-$(pwd -P)}

ENV_FILE="$API_HOME/conf/env.sh"
source "${ENV_FILE}"

PID_FILE=${API_HOME}/ETraceApi.pid

# set main collector driver class full name
MonitorApi_DRIVER=io.etrace.api.ApiApplication

# set conf file into classpath
CLASSPATH=${API_HOME}:${API_HOME}/conf

CLASSPATH="${CLASSPATH}":"${API_HOME}/lib/*":"${API_HOME}/*"
for i in "${API_HOME}"/*.jar; do
  CLASSPATH="${CLASSPATH}":"${i}"
done


# add exec to enable java process shutdown
start() {
#  nohup java ${YOUR_OPTS} -classpath ${CLASSPATH} ${MonitorApi_DRIVER} >/home/admin/${APP_NAME}/logs/start.log &
  java ${YOUR_OPTS} -classpath ${CLASSPATH} ${MonitorApi_DRIVER}
}

case "$1" in
start)
  start
  ;;
*)
  echo "Usage: $0 {start}"
  exit 1
  ;;
esac
exit 0
