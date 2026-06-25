#!/bin/bash

set -euo pipefail

APP_DIR=/home/ec2-user/app/step1
JAR_FILE=$APP_DIR/app.jar
ENV_FILE=/home/ec2-user/app/backend.env
PARAM_PREFIX=${PARAM_PREFIX:-/app/aivle05/backend}
AWS_REGION=${AWS_REGION:-us-east-1}

get_parameter() {
  local name=$1
  local decrypt_option=${2:-}

  aws ssm get-parameter \
    --name "$PARAM_PREFIX/$name" \
    $decrypt_option \
    --query "Parameter.Value" \
    --output text \
    --region "$AWS_REGION"
}

write_env_value() {
  local key=$1
  local value=$2

  printf '%s=%q\n' "$key" "$value" >> "$ENV_FILE"
}

echo "== 기존 Spring Boot 애플리케이션 종료 =="

PID=$(pgrep -f "java -jar" || true)

if [ -n "$PID" ]; then
  echo "종료할 PID: $PID"
  kill -15 $PID
  sleep 5
else
  echo "실행 중인 애플리케이션이 없습니다."
fi

echo "== Parameter Store에서 환경변수 파일 생성 =="

: > "$ENV_FILE"
chmod 600 "$ENV_FILE"

write_env_value "DB_DRIVER" "$(get_parameter DB_DRIVER)"
write_env_value "DB_URL" "$(get_parameter DB_URL)"
write_env_value "DB_USERNAME" "$(get_parameter DB_USERNAME)"
write_env_value "DB_PASSWORD" "$(get_parameter DB_PASSWORD --with-decryption)"
write_env_value "OPENAI_API_KEY" "$(get_parameter OPENAI_API_KEY --with-decryption)"

echo "== 환경변수 파일 로드 =="
set -a
source "$ENV_FILE"
set +a

echo "== Spring Boot 애플리케이션 시작 =="

cd "$APP_DIR"
nohup java -jar "$JAR_FILE" > nohup.out 2>&1 &

sleep 10

echo "== 실행 상태 확인 =="
ps -ef | grep java | grep -v grep
