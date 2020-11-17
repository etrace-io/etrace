#!/bin/bash
SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})

BASE_DIR=$(cd ${SCRIPT_DIR} && pwd -P)
JVM_CONF="${BASE_DIR}/../conf/jvm_conf.sh"

if [ ! -f "${JVM_CONF}" ]; then
    echo "${JVM_CONF} doesn't exist!!! exit"
    sleep 5
    exit 0
fi

cd "$SCRIPT_DIR/.."

source "${JVM_CONF}"
JVM_LOGS_DIR=${BASE_DIR}/../logs/${STREAM_HTTP_PORT}
GC_LOGS_DIR=${JVM_LOGS_DIR}/gc
DUMP_LOGS_DIR=${JVM_LOGS_DIR}/dump
if [ ! -d "${GC_LOGS_DIR}" ]; then
    mkdir -p "${GC_LOGS_DIR}"
fi
if [ ! -d "${DUMP_LOGS_DIR}" ]; then
    mkdir -p "${DUMP_LOGS_DIR}"
fi
# set jvm startup argument
JAVA_OPTS="-Xms${CONTAINER_JVM_XMS} \
            -Xmx${CONTAINER_JVM_XMX} \
            -XX:+UseCompressedOops \
            -XX:+ExplicitGCInvokesConcurrent \
            -XX:+UseG1GC \
            -Djava.awt.headless=true \
            -Dstream.http.port=${STREAM_HTTP_PORT}
            -Dstream.logs.path=${JVM_LOGS_DIR}
            -Dfile.encoding=utf-8 \
            -XX:+PrintGC \
            -XX:+PrintGCDetails \
            -XX:+PrintGCDateStamps \
            -Xloggc:${GC_LOGS_DIR}/server.gc.$(date +%Y%m%d-%H%M%S).log \
            -XX:-OmitStackTraceInFastThrow \
            -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${DUMP_LOGS_DIR} \
            "
export JAVA_OPTS=${JAVA_OPTS}