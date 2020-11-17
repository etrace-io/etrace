#!/bin/sh

#JAVA_OPTS="-Xms1G \
#            -Xmx2G \
#            -Xmn1G \
#            -XX:PermSize=128m \
#            -XX:MaxPermSize=128m \
#            -XX:-DisableExplicitGC \
#            -XX:+UseConcMarkSweepGC \
#            -XX:CMSInitiatingOccupancyFraction=70 \
#            -XX:+UseCMSCompactAtFullCollection \
#            -Djava.awt.headless=true \
#            -Dcom.sun.management.jmxremote.port=9003 \
#            -Dcom.sun.management.jmxremote.authenticate=false \
#            -Dcom.sun.management.jmxremote.ssl=false \
#            -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
#            -Dfile.encoding=utf-8 \
#            -XX:+PrintGC \
#            -XX:+PrintGCDetails \
#            -XX:+PrintGCDateStamps \
#            -Xloggc:/data/log/monitor.api/gc/monitor.api.gc.log \
#            -XX:-OmitStackTraceInFastThrow \
#            -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/log/monitor.api/dump \
#            "
export JAVA_OPTS=${JAVA_OPTS}