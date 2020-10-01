#!/bin/bash
SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
SCRIPT_DIR=$(cd ${SCRIPT_DIR} && pwd -P)

BASE_DIR=$(cd $SCRIPT_DIR  && pwd -P)

CONF_DIR="${BASE_DIR}/conf"
JVM_CONF="${BASE_DIR}/conf/jvm_conf.sh"

if [ ! -f "${JVM_CONF}" ]; then
    echo "${JVM_CONF} doesn't exist!!!"
    sleep 5
    exit 0
fi

# App Home
cd "$SCRIPT_DIR"
APP_HOME=${APP_HOME-$(pwd -P)}
source ${JVM_CONF}
#source "${APP_HOME}/bin/jvm_conf.sh"
DAEMON="container"
DESC="Stream Server"
CONTAINER_PORT_END=$(expr $CONTAINER_PORT_START + $CONTAINER_PROCESS_COUNT - 1)
EXE_FILE="$APP_HOME/bin/container.sh"
LOG_FILE="container.out"
PID=0
TIMEOUT=3

server_start(){
    echo "Starting ${DESC} ....."
    for PORT in $(seq $CONTAINER_PORT_START $CONTAINER_PORT_END)
    do
        PID=$(sh ${EXE_FILE} check_pid $PORT)
        if [ "${PID}" != "" ];  then
            echo "WARN: ${DESC} port=${PORT} already started! (pid=${PID})"
        else
            LOG_PATH="${BASE_DIR}/logs/${PORT}"
            if [ ! -d "${LOG_PATH}" ]; then
                mkdir -p "${LOG_PATH}"
            fi
#            nohup sh ${EXE_FILE} start $PORT >> "${LOG_PATH}/${LOG_FILE}" 2>&1 &
            sh ${EXE_FILE} start $PORT
            echo "${DESC} ${PORT} started!"
            if [ $PORT == $CONTAINER_PORT_START ]; then
                #if first, sleep one second to wait masterElect
                sleep 1s
            fi
        fi
    done
}

get_container_port(){
    pid_info=$(ps -p "$1" -f | grep "Dstream.http.port")
    pid_info=${pid_info#*Dstream.http.port=}
    echo $pid_info | awk '{print $1}'
}

server_stop(){
    if [[ "X$1" == "Xforce_restart" ]]; then
        echo "Force Stopping ${DESC}  ....."
    else
        echo "Stopping ${DESC}  ....."
    fi
    PIDS=$(ps gaux | grep java | grep "${BASE_DIR}/logs" | grep -v grep | awk '{print $2}')
    if [ "${PIDS}" == "" ];then
        echo "WARN: ${DESC} is stopped."
    else
        for PID in $PIDS
        do
            if [[ "$PID" =~ ^[0-9]+$ ]];then
                port=$(get_container_port $PID)
                if [[ "X$1" == "Xforce_restart" ]]; then
                    kill -9 ${PID}
                else
                    kill ${PID}    
                fi
                if [ $? -eq 0 ]; then
                    echo "stop  (pid:${PID}  port:${port}) [OK]"
                else
                    echo "stop  (pid:${PID}  port:${port}) [Failed]"
                fi
            fi
        done
    fi
}

server_status(){
    echo "Checking ${DESC} ....."
    PIDS=$(ps gaux | grep java | grep "${BASE_DIR}/logs" | grep -v grep | awk '{print $2}')
    if [ "${PIDS}" == "" ];then
        echo "${DESC} is stopped!"
    else
        for PID in $PIDS
        do
            if [[ "$PID" =~ ^[0-9]+$ ]];then
                port=$(get_container_port $PID)
                echo "${DESC} is running! (pid:${PID}  port:${port})"
            fi
        done
    fi
}

server_info(){
    echo "Stream Server Information:"
    sh ${EXE_FILE} info
}

server_restart(){
    server_stop
    stopedStr="stopped"
    i=0
    loop_max=12
    while ((i<${loop_max}))
    do
        RESP=$(sh ${SCRIPT_DIR}/run.sh status)
        if [[ $RESP =~ $stopedStr ]]
        then
            echo ""
            echo "sleep 5s before begin to start"
            sleep 5
            server_start
            break;
        fi
        i=$[$i+1];
        echo -n "."
        sleep 5
        if [ $i -gt ${loop_max} ]
        then
           echo "can not stoped the host,please stop it yourself"
        fi
    done
}

force_restart(){
    server_stop "force_restart"
    echo "check server status"
    server_status
    # avoid kafka consumer rebalance
    sleep 5
    echo "Force restart phase, will start server"
    server_start
}

case "$1" in
    start)
        server_start
        ;;
    stop)
        server_stop
        ;;
    restart)
        server_restart
        ;;
    force_restart)
        force_restart
        ;;
    status)
        server_status
        ;;
    info)
        server_info
        ;;
    *)
        echo "Usage: $0 {start|stop|force_restart|restart|status|info}"
        exit 1;
        ;;
esac

exit 0