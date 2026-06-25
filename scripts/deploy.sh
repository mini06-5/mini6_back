#!/bin/bash

APP_DIR=/home/ec2-user/app/step1
JAR_FILE=$APP_DIR/app.jar

echo "== 기존 Spring Boot 애플리케이션 종료 =="

PID=$(pgrep -f "java -jar")

if [ -n "$PID" ]; then
  echo "종료할 PID: $PID"
  kill -15 $PID
  sleep 5
else
  echo "실행 중인 애플리케이션이 없습니다."
fi

echo "== Spring Boot 애플리케이션 시작 =="

cd $APP_DIR
nohup java -jar $JAR_FILE > nohup.out 2>&1 &

sleep 10

echo "== 실행 상태 확인 =="
ps -ef | grep java
 