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

echo "== AWS에서 OpenAI API Key 가져오는 중 =="
# SSM에서 키를 가져와서 환경변수에 저장
export OPENAI_API_KEY=$(aws ssm get-parameter --name "/bookapp/prod/OPENAI_API_KEY" --with-decryption --query "Parameter.Value" --output text --region us-east-1)

echo "== Spring Boot 애플리케이션 시작 =="

cd $APP_DIR
# -Dopenai.api-key 옵션으로 키를 명시적으로 주입합니다.
nohup java -jar -Dopenai.api-key=$OPENAI_API_KEY $JAR_FILE > nohup.out 2>&1 &

sleep 10

echo "== 실행 상태 확인 =="
ps -ef | grep java
 