@echo off
echo 컴파일 중...
javac -encoding UTF-8 -cp "lib/json-20231013.jar;lib/gson-2.10.1.jar" -d bin Main.java App.java core/*.java components/**/*.java models/*.java utils/*.java
if %errorlevel% == 0 (
    echo 컴파일 완료!
) else (
    echo 컴파일 실패!
    pause
)


