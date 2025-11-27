#!/bin/bash

cd "$(dirname "$0")"

# 컴파일
echo "컴파일 중..."
javac -cp ".:lib/gson-2.10.1.jar" -d . *.java

if [ $? -eq 0 ]; then
    echo "컴파일 성공!"
    echo "실행 중..."
    java -cp ".:lib/gson-2.10.1.jar" Main
else
    echo "컴파일 실패!"
    exit 1
fi

