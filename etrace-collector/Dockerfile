FROM nimmis/java-centos:oracle-8-jdk as jdk8
EXPOSE 2890
COPY target/etrace-collector.tar.gz /etrace-collector/
WORKDIR /etrace-collector
RUN tar -xf etrace-collector.tar.gz
RUN ls

ENTRYPOINT ["sh","run.sh", "start"]


