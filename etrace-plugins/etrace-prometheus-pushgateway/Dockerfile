FROM nimmis/java-centos:oracle-8-jdk as jdk8
EXPOSE 19091
ADD target/prometheus-pushgateway.tar.gz /etrace-prometheus-pushgateway/
WORKDIR /etrace-prometheus-pushgateway
RUN ls
ENTRYPOINT ["sh","run.sh", "start"]