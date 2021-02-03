FROM nimmis/java-centos:oracle-8-jdk as jdk8
EXPOSE 2890
ADD target/etrace-api.tar.gz /etrace-api/
WORKDIR /etrace-api
RUN ls

ENTRYPOINT ["sh","run.sh", "start"]
