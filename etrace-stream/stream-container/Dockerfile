FROM nimmis/java-centos:oracle-8-jdk as jdk8
EXPOSE 2890
COPY target/stream-container.tar.gz /stream-container/
WORKDIR /stream-container
RUN tar -xf stream-container.tar.gz
RUN ls

ENTRYPOINT ["sh","run.sh", "start"]
