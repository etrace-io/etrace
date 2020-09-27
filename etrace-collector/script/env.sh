#!/bin/sh
# set jvm startup argument
#JAVA_OPTS="-Xms3g \
#            -Xmx3g \
#            -Xmn1g \
#            -XX:PermSize=128m \
#            -XX:MaxPermSize=128m \
#            -XX:+ExplicitGCInvokesConcurrent \
#            -XX:+UseConcMarkSweepGC \
#            -XX:CMSInitiatingOccupancyFraction=40 \
#            -XX:+UseCMSCompactAtFullCollection \
#            -Djava.awt.headless=true \
#            -Dcom.sun.management.jmxremote.port=2899 \
#            -Dcom.sun.management.jmxremote.authenticate=false \
#            -Dcom.sun.management.jmxremote.ssl=false \
#            -Dfile.encoding=utf-8 \
#            -XX:+PrintGC \
#            -XX:+PrintGCDetails \
#            -XX:+PrintGCDateStamps \
#            -Xloggc:collector.gc.log \
#            -XX:-OmitStackTraceInFastThrow \
#            -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump \
#            -Dio.netty.recycler.maxCapacity.default=0 \
#            "
export JAVA_OPTS=${JAVA_OPTS}
