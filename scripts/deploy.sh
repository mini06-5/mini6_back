#!/bin/bash

REPOSITORY=/home/ec2-user/app/step1
# 프로젝트 이름 대신 jar 파일명을 직접 찾도록 변경
JAR_NAME=$(ls -tr $REPOSITORY/build/libs/*-SNAPSHOT.jar | grep -v plain | tail -n 1)

echo "> 현재 구동 중인 애플리케이션 pid 확인"
# jar 파일명으로 실행 중인 프로세스를 찾습니다.
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 애플리케이션 배포"
echo "> JAR Name: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"
chmod +x $JAR_NAME

ENV_FILE=/home/ec2-user/app/backend.env

if [ -f "$ENV_FILE" ]; then
    echo "> 환경변수 파일 로드"
    set -a
    source "$ENV_FILE"
    set +a
else
    echo "> 환경변수 파일이 없습니다: $ENV_FILE"
fi

echo "> $JAR_NAME 실행"
nohup java -jar $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &
