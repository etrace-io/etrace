FROM nimmis/java-centos:oracle-8-jdk as jdk8
EXPOSE 8082
COPY target/etrace-consumer.tar.gz /etrace-consumer/
WORKDIR /etrace-consumer
RUN tar -xf etrace-consumer.tar.gz
RUN ls
ENTRYPOINT ["sh","run.sh", "start"]