#!/bin/sh

# set jvm startup argument
#JAVA_OPTS="-Xms3g \
#            -Xmx3g \
#            -Xmn2g \
#            -XX:MetaspaceSize=128m \
#            -XX:MaxMetaspaceSize=128m \
#            -XX:-DisableExplicitGC \
#            -XX:+UseConcMarkSweepGC \
#            -XX:CMSInitiatingOccupancyFraction=70 \
#            -XX:+UseCMSCompactAtFullCollection \
#            -Djava.awt.headless=true \
#            -Dcom.sun.management.jmxremote.port=2898 \
#            -Dcom.sun.management.jmxremote.authenticate=false \
#            -Dcom.sun.management.jmxremote.ssl=false \
#            -Dfile.encoding=utf-8 \
#            -XX:+PrintGC \
#            -XX:+PrintGCDetails \
#            -XX:+PrintGCDateStamps \
#            -Xloggc:consumer.gc.log \
#            -XX:-OmitStackTraceInFastThrow \
#            -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump \
#            "
export JAVA_OPTS=${JAVA_OPTS}