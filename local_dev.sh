#!/bin/sh
#set -xv

Requirement="HBase/Hdfs/ZK/Kafka/Mysql/Prometheus/PushGateway"

echo "### Welcome! Your can use this script to build your local development environment.
Your should have cloned this git repository. Then your need:

1. Java 8+
2. Maven 3.5+
3. Docker

Run 'mvn clean install' first to compile all codes.
"

while true; do
echo "#### choose the profile you want to build: [Input the number]

[1]. All in One: whole etrace project (include front-end and back-end services) and related requirements ($Requirement)
[2]. Only front-end project
[3]. all back-end projects (collector, consumer, stream, api) and related requirements ($Requirement)
[4]. All related requirements ($Requirement)
[5]. Only Mysql

[7]. Prometheus and Grafana.
[8]. Debug Etrace back-end services.
[9]. Clean all orphan docker service (todo)

[0]. exit

Input number: "

  read -r num
  case $num in
  [1]*) echo 1 ;;
  [2]*)
    echo "todo: run yarn start"
     ;;
  [3]*)
    docker-compose  -f docker-compose-etrace.yml build
    docker-compose  -f docker-compose-etrace.yml -f docker-compose-mysql.yml -f docker-compose-kafka-hadoop.yml -f docker-compose-prometheus-grafana.yml up
    ;;
  [4]*)
    docker-compose -f docker-compose-mysql.yml -f docker-compose-kafka-hadoop.yml -f docker-compose-prometheus-grafana.yml up
    ;;
  [5]*)
    docker-compose -f docker-compose-mysql.yml up
    ;;

   [7]*)
    echo "docker-compose  -f docker-compose-prometheus-grafana.yml up"
    docker-compose -f docker-compose-prometheus-grafana.yml up
    ;;
  [8]*)
    docker-compose  -f docker-compose-etrace.yml build
    docker-compose -f docker-compose-etrace.yml  up
    ;;
  [9]*)
    docker-compose  -f docker-compose-etrace.yml -f docker-compose-mysql.yml -f docker-compose-kafka-hadoop.yml -f docker-compose-prometheus-grafana.yml down --remove-orphans
    docker image prune
    ;;
  [0]*) exit ;;
  [q]*) exit ;;
  *) echo "Only number accepted!" ;;
  esac
done
