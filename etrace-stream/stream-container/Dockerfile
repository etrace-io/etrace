FROM nimmis/java-centos:oracle-8-jdk as jdk8
EXPOSE 2890
ADD target/stream-container.tar.gz /stream-container/
WORKDIR /stream-container
RUN ls

ENTRYPOINT ["sh","run.sh", "start"]
