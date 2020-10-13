#!/bin/bash

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
# App Home
cd "$SCRIPT_DIR"
APP_HOME=${APP_HOME-$(pwd -P)}

DAEMON="pushgateway"
DESC="ETrace pushgateway"
LOG_PATH="$APP_HOME/logs"
EXE_FILE="$APP_HOME/bin/pushgateway.sh"
LOG_FILE="pushgateway.out"
PID=0
TIMEOUT=3

pushgateway_run(){
    echo "Starting ${DESC} ....."
    PID=$(sh ${EXE_FILE} check_pid)
    if [ "${PID}" != "" ];  then
       echo "WARN: ${DESC} already started! (pid=${PID})"
    else
        sh ${EXE_FILE} start
        echo "${DESC} started!"
    fi
}

pushgateway_start(){
    echo "Starting ${DESC} ....."
    PID=$(sh ${EXE_FILE} check_pid)
    if [ "${PID}" != "" ];  then
       echo "WARN: ${DESC} already started! (pid=${PID})"
    else
        if [ ! -d "${LOG_PATH}" ]; then
            mkdir "${LOG_PATH}"
        fi
#        nohup sh ${EXE_FILE} start > "${LOG_PATH}/${LOG_FILE}" 2>&1 &
        sh ${EXE_FILE} start
        echo "${DESC} started!"
    fi
}

pushgateway_stop(){
   PID=$(sh ${EXE_FILE} check_pid)
   if [ "${PID}" != "" ]; then
      echo "Stopping ${DESC} ....."
      if ! kill -9 ${PID}
      then
         echo "[OK]"
      else
         echo "[Failed]"
      fi
   else
      echo "WARN: ${DESC} is stopped."
   fi
}

pushgateway_status(){
    echo "Checking ${DESC} ....."
    PID=$(sh ${EXE_FILE} check_pid)
    if [ "${PID}" != "" ];  then
       echo "${DESC} is running! (pid=${PID})"
    else
       echo "${DESC} is stopped!"
    fi
}

pushgateway_info(){
	echo "Consumer information:"
    sh ${EXE_FILE} infoconsumer.sh:
}

pushgateway_force_stop(){
   PID=$(sh ${EXE_FILE} check_pid)
   if [ "${PID}" != "" ]; then
      echo "Stopping ${DESC} ....."
      if ! kill -9 ${PID}
      then
         echo "[OK]"
      else
         echo "[Failed]"
      fi
   else
      echo "WARN: ${DESC} is not running."
   fi
}

pushgateway_restart(){
    thrift_stop
    [ -n "${TIMEOUT}" ] && sleep ${TIMEOUT}
    thrift_start
}

case "$1" in
    run)
	pushgateway_run
	;;
    start)
	pushgateway_start
	;;
    stop)
	pushgateway_stop
	;;
    force-stop)
	pushgateway_force_stop
	;;
    restart)
	pushgateway_restart
	;;
    status)
	pushgateway_status
	;;
    info)
	pushgateway_info
	;;
    *)
        echo "Usage: $0 {start|stop|forcestop|restart|status|info}" 
   	exit 1;	
	;;
esac

exit 0

