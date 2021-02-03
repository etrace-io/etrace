FROM nimmis/java-centos:oracle-8-jdk as jdk8
EXPOSE 8082
ADD target/etrace-consumer.tar.gz /etrace-consumer/
WORKDIR /etrace-consumer
RUN ls
ENTRYPOINT ["sh","run.sh", "start"]