#!/bin/bash

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
# App Home
cd "$SCRIPT_DIR"
APP_HOME=${APP_HOME-$(pwd -P)}

DAEMON="collector"
DESC="ETrace Collector"
LOG_PATH="$APP_HOME/logs"
EXE_FILE="$APP_HOME/bin/collector.sh"
LOG_FILE="collector.out"
PID=0
TIMEOUT=3

collector_run() {
  echo "Starting ${DESC} ....."
  PID=$(sh ${EXE_FILE} check_pid)
  if [ "${PID}" != "" ]; then
    echo "WARN: ${DESC} already started! (pid=${PID})"
  else
    sh ${EXE_FILE} start
    echo "${DESC} started!"
  fi
}

collector_start() {
  echo "Starting ${DESC} ....."
  PID=$(sh ${EXE_FILE} check_pid)
  if [ "${PID}" != "" ]; then
    echo "WARN: ${DESC} already started! (pid=${PID})"
  else
    if [ ! -d "${LOG_PATH}" ]; then
      mkdir "${LOG_PATH}"
    fi
#    nohup sh ${EXE_FILE} start >"${LOG_PATH}/${LOG_FILE}" 2>&1 &
    sh ${EXE_FILE} start
    echo "${DESC} started!"
  fi
}

collector_stop() {
  PID=$(sh ${EXE_FILE} check_pid)
  if [ "${PID}" != "" ]; then
    echo "Stopping ${DESC} ....."
    kill ${PID}
    if [ $? -eq 0 ]; then
      echo "[OK]"
    else
      echo "[Failed]"
    fi
  else
    echo "WARN: ${DESC} is stopped."
  fi
}

collector_status() {
  echo "Checking ${DESC} ....."
  PID=$(sh ${EXE_FILE} check_pid)
  if [ "${PID}" != "" ]; then
    echo "${DESC} is running! (pid=${PID})"
  else
    echo "${DESC} is stopped!"
  fi
}

collector_info() {
  echo "Collector information:"
  sh ${EXE_FILE} info
}

collector_force_stop() {
  PID=$(sh ${EXE_FILE} check_pid)
  if [ "${PID}" != "" ]; then
    echo "Stopping ${DESC} ....."
    kill -9 ${PID}
    if [ $? -eq 0 ]; then
      echo "[OK]"
    else
      echo "[Failed]"
    fi
  else
    echo "WARN: ${DESC} is not running."
  fi
}

collector_restart() {
  thrift_stop
  [ -n "${TIMEOUT}" ] && sleep ${TIMEOUT}
  thrift_start
}

case "$1" in
run)
  collector_run
  ;;
start)
  collector_start
  ;;
stop)
  collector_stop
  ;;
force-stop)
  collector_force_stop
  ;;
restart)
  collector_restart
  ;;
status)
  collector_status
  ;;
info)
  collector_info
  ;;
*)
  echo "Usage: $0 {start|stop|forcestop|restart|status|info}"
  exit 1
  ;;
esac

exit 0
