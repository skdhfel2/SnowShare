# SnowShare

Java Swing 프론트엔드와 Node.js (Express) + MySQL 백엔드 구조의 애플리케이션 프로젝트입니다.

## 프로젝트 구조
ㄴ
```
SnowShare/
├── api/                    # Node.js + Express + MySQL 백엔드
│   ├── routes/            # API 라우터
│   ├── controllers/       # 컨트롤러
│   ├── models/            # 데이터 모델
│   ├── middleware/        # 미들웨어
│   ├── utils/             # 유틸리티 함수
│   ├── lib/               # 라이브러리 (DB, Logger 등)
│   ├── app.js             # Express 앱 진입점
│   ├── package.json       # Node.js 의존성
│   ├── .eslintrc.js       # ESLint 설정
│   ├── .prettierrc        # Prettier 설정
│   └── .env.example       # 환경 변수 템플릿
├── client/                 # Java Swing 클라이언트
│   ├── components/        # Swing 컴포넌트
│   ├── utils/             # 유틸리티 클래스
│   ├── hooks/             # 이벤트 핸들링
│   ├── Main.java          # 메인 진입점
│   └── README.md          # 클라이언트 실행 가이드
└── README.md              # 프로젝트 전체 가이드
```

## 시작하기

### 백엔드 (API) 설정

1. **의존성 설치**

```bash
cd api
npm install
```

2. **환경 변수 설정**

`.env.example` 파일을 복사하여 `.env` 파일을 생성하고 데이터베이스 정보를 입력하세요:

```bash
cp .env.example .env
```

`.env` 파일 내용:

```
PORT=3000
NODE_ENV=development

DB_HOST=localhost
DB_USER=root
DB_PASS=your_password
DB_NAME=snowshare

LOG_LEVEL=info
```

3. **데이터베이스 생성**

MySQL에서 데이터베이스를 생성하세요:

```sql
CREATE DATABASE snowshare;
```

4. **서버 실행**

```bash
npm run dev
```

서버가 `http://localhost:3000`에서 실행됩니다.

### API 엔드포인트

- `GET /health` - 서버 상태 확인
- `GET /api/test` - 데이터베이스 연결 테스트

### 클라이언트 (Java Swing) 실행

1. **컴파일**

```bash
cd client
javac Main.java
```

2. **실행**

```bash
java Main
```

## 개발 도구

### ESLint & Prettier

코드 스타일을 자동으로 검사하고 포맷팅합니다.

```bash
# ESLint 검사
npm run lint

# ESLint 자동 수정
npm run lint:fix

# Prettier 포맷팅
npm run format
```

### 코드 스타일

- 세미콜론 사용
- 싱글 따옴표 사용
- 탭 2칸
- Trailing comma 항상 사용

## 기술 스택

### 백엔드
- Node.js
- Express
- MySQL (mysql2)
- Winston (로깅)
- ESLint + Prettier

### 프론트엔드
- Java Swing

## 협업하기

다른 개발자들과 함께 작업하고 싶으시다면 [CONTRIBUTING.md](./CONTRIBUTING.md) 문서를 참고하세요.

### GitHub에서 협업자 초대하기

1. GitHub 저장소 페이지로 이동: `https://github.com/skdhfel2/SnowShare`
2. **Settings** → **Collaborators** → **Add people** 클릭
3. 협업자의 GitHub 사용자명 또는 이메일 입력
4. 권한 설정 (Read, Write, Admin 중 선택)
5. 초대 전송

협업자가 초대를 수락하면 저장소에 접근할 수 있습니다.

### 협업 워크플로우 요약

```bash
# 1. 최신 코드 가져오기
git pull origin main

# 2. 새 브랜치 생성
git checkout -b feature/새기능명

# 3. 작업 후 커밋
git add .
git commit -m "feat: 새 기능 추가"

# 4. 원격에 푸시
git push origin feature/새기능명

# 5. GitHub에서 Pull Request 생성
```

자세한 내용은 [CONTRIBUTING.md](./CONTRIBUTING.md)를 참고하세요.

## 라이선스

ISC
