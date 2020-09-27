#!/bin/bash

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
# App Home
cd "$SCRIPT_DIR"
APP_HOME=${APP_HOME-`pwd -P`}

DAEMON="consumer"
DESC="ETrace Consumer"
LOG_PATH="$APP_HOME/logs"
EXE_FILE="$APP_HOME/bin/consumer.sh"
LOG_FILE="consumer.out"
PID=0
TIMEOUT=3

consumer_run(){
    echo "Starting ${DESC} ....."
    PID=`sh ${EXE_FILE} check_pid`
    if [ "${PID}" != "" ];  then
       echo "WARN: ${DESC} already started! (pid=${PID})"
    else
        sh ${EXE_FILE} start
        echo "${DESC} started!"
    fi
}

consumer_start(){
    echo "Starting ${DESC} ....."
    PID=`sh ${EXE_FILE} check_pid`
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

consumer_stop(){
   PID=`sh ${EXE_FILE} check_pid`
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

consumer_status(){
    echo "Checking ${DESC} ....."
    PID=`sh ${EXE_FILE} check_pid`
    if [ "${PID}" != "" ];  then
       echo "${DESC} is running! (pid=${PID})"
    else
       echo "${DESC} is stopped!"
    fi
}

consumer_info(){
	echo "Consumer information:"
    sh ${EXE_FILE} info
}

consumer_force_stop(){
   PID=`sh ${EXE_FILE} check_pid`
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

consumer_restart(){
    thrift_stop
    [ -n "${TIMEOUT}" ] && sleep ${TIMEOUT}
    thrift_start
}

case "$1" in
    run)
	consumer_run
	;;
    start)
	consumer_start
	;;
    stop)
	consumer_stop
	;;
    force-stop)
	consumer_force_stop
	;;
    restart)
	consumer_restart
	;;
    status)
	consumer_status
	;;
    info)
	consumer_info
	;;
    *)
        echo "Usage: $0 {start|stop|forcestop|restart|status|info}" 
   	exit 1;	
	;;
esac

exit 0

