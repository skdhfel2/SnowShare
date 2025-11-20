# SnowShare Client

Java Swing 기반 GUI 클라이언트 애플리케이션입니다.

## 프로젝트 구조

```
client/
├── Main.java          # 메인 진입점
├── components/        # Swing 컴포넌트 클래스들
├── utils/            # 유틸리티 클래스
└── hooks/            # 이벤트 핸들링 관련
```

## 빌드 및 실행

### 컴파일

```bash
javac -d bin src/**/*.java
```

또는 Main.java가 루트에 있는 경우:

```bash
javac Main.java
```

### 실행

```bash
java Main
```

또는 클래스패스 지정:

```bash
java -cp bin Main
```

## 개발 환경

- Java 8 이상
- Swing 라이브러리 (JDK 포함)

## 주요 기능

- API 서버 연결 테스트
- (추가 기능 구현 예정)

