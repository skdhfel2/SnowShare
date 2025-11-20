# SnowShare Client 설정 가이드

## 프로젝트 구조

```
client/
├── lib/                 # 외부 라이브러리 (json-20231013.jar)
├── bin/                 # 컴파일된 클래스 파일 (자동 생성)
├── Main.java            # 프로그램 시작점
├── App.java             # 화면 전환 관리
├── core/                # 공통 프레임워크
├── components/          # UI 컴포넌트
├── models/              # 데이터 모델
├── utils/               # 유틸리티 (ApiClient 등)
├── compile.bat          # 컴파일 스크립트
└── run.bat              # 실행 스크립트
```

## 빠른 시작

### 방법 1: 배치 파일 사용 (Windows)

```bash
cd client
compile.bat    # 컴파일
run.bat        # 실행
```

### 방법 2: 수동 컴파일/실행

```bash
cd client

# 컴파일
javac -encoding UTF-8 -cp "lib/json-20231013.jar" -d bin Main.java App.java core/*.java components/**/*.java models/*.java utils/*.java

# 실행
java -cp "lib/json-20231013.jar;bin" Main
```

### 방법 3: VS Code에서 실행

1. `Main.java` 파일 열기
2. `F5` 키 누르기 (디버그 모드)
3. 또는 `Ctrl+Shift+P` → "Java: Run Java"

## 필수 요구사항

- **Java JDK 11 이상** 설치 필요
- **lib/json-20231013.jar** 파일이 있어야 함

## JSON 라이브러리 오류 해결

`The import org.json cannot be resolved` 오류가 발생하면:

1. **lib 폴더 확인**
   - `client/lib/json-20231013.jar` 파일이 있는지 확인

2. **JAR 파일이 없으면 다운로드**
   - https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar
   - 다운로드 후 `client/lib/` 폴더에 저장

3. **VS Code 설정 확인**
   - `Ctrl+Shift+P` → "Java: Reload Projects" 실행








