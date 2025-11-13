# SnowShare

Java Swing 프론트엔드와 Node.js (Express) + MySQL 백엔드 구조의 애플리케이션 프로젝트입니다.

## 프로젝트 구조

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

==============================================
# 아래 구조 참고해서 구현

SnowShare/
├── api/                        # Node.js 백엔드
│   ├── routes/
│   ├── controllers/
│   ├── models/
│   ├── middleware/
│   ├── utils/
│   ├── lib/
│   ├── app.js
│   └── ...
│
└── client/                     # Java Swing 애플리케이션
    ├── Main.java
    ├── App.java               # 화면 전환 관리자
    │
    ├── core/                  # 핵심 프레임워크 (공통 기능)
    │   ├── BasePanel.java     # 모든 화면의 기본 구조
    │   ├── BaseFrame.java     # 메인 프레임 공통
    │   ├── Navigator.java     # 화면 이동
    │   └── Session.java       # 로그인 세션 관리
    │
    ├── components/            # UI 화면 (기능별 그룹화)
    │   ├── common/            # 공통 컴포넌트
    │   │   ├── HeaderNav.java     # 상단 메뉴/탭(뉴스/대응안내/지도/커뮤니티)
    │   │   ├── CustomButton.java
    │   │   ├── CustomTable.java
    │   │   └── LoadingSpinner.java
    │   │
    │   ├── auth/              # 회원가입/로그인
    │   │   ├── LoginPanel.java
    │   │   ├── RegisterPanel.java
    │   │   └── ProfilePanel.java  # 사용자 정보(선택)
    │   │
    │   ├── main/              # 메인 홈 화면
    │   │   ├── HomePanel.java     # 시스템 소개 문구 포함
    │   │
    │   ├── news/              # 관련 뉴스 기능
    │   │   ├── NewsPanel.java
    │   │   ├── NewsListPanel.java
    │   │   └── NewsDetailPanel.java
    │   │
    │   ├── guide/             # 폭설 대응 안내
    │   │   ├── GuidePanel.java
    │   │   ├── EmergencyContactPanel.java
    │   │   └── EquipmentPanel.java
    │   │
    │   ├── map/               # 제설함 지도
    │   │   ├── MapPanel.java
    │   │   ├── SnowMarkerInfoPanel.java
    │   │   └── UserLocationFinder.java
    │   │
    │   ├── community/         # 커뮤니티 (자유게시판/후기게시판)
    │   │   ├── CommunityPanel.java
    │   │   ├── FreeBoard/
    │   │   │   ├── FreeBoardPanel.java
    │   │   │   ├── FreeBoardWritePanel.java
    │   │   │   ├── FreeBoardDetailPanel.java
    │   │   │   └── FreeBoardEditPanel.java
    │   │   ├── ReviewBoard/
    │   │   │   ├── ReviewPanel.java
    │   │   │   ├── ReviewWritePanel.java
    │   │   │   └── ReviewDetailPanel.java
    │   │   └── CommentPanel.java
    │
    ├── hooks/                 # 이벤트 핸들러 (기능별로 분리)
    │   ├── auth/
    │   │   ├── LoginHandler.java
    │   │   └── RegisterHandler.java
    │   │
    │   ├── news/
    │   │   └── RssLoadHandler.java
    │   │
    │   ├── guide/
    │   │   └── ContactLoadHandler.java
    │   │
    │   ├── map/
    │   │   ├── MapLoadHandler.java
    │   │   └── MarkerClickHandler.java
    │   │
    │   ├── community/
    │       ├── CreatePostHandler.java
    │       ├── EditPostHandler.java
    │       ├── DeletePostHandler.java
    │       ├── LoadPostListHandler.java
    │       └── CommentHandler.java
    │
    ├── utils/                 # 공통 기능
    │   ├── ApiClient.java     # 백엔드 서버 통신
    │   ├── JsonUtil.java
    │   ├── Validator.java
    │   ├── RssParser.java
    │   ├── GeoUtil.java       # 거리 계산, GPS 관련
    │   └── FileLoader.java    # JSON 불러오기 (긴급 연락처 등)
    │
    ├── models/                # DTO/데이터 객체
    │   ├── User.java
    │   ├── News.java
    │   ├── SnowBox.java
    │   ├── Post.java
    │   ├── Comment.java
    │   └── Review.java
    │
    └── assets/                # 이미지, JSON, 아이콘
        ├── icons/
        ├── images/
        ├── json/
        │   └── emergency_contacts.json
        └── posters/
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

## 라이선스

ISC
