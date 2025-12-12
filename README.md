# SnowShare

> 시민 참여형 제설함 정보 공유 플랫폼

![Java](https://img.shields.io/badge/Java-Swing-007396?style=flat-square&logo=java)
![Node.js](https://img.shields.io/badge/Node.js-Express-339933?style=flat-square&logo=node.js)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)

---

## 📌 프로젝트 소개

### 프로젝트 주제

**SnowShare**는 시민들이 실시간으로 제설함 위치와 상태를 확인하고, 제설 활동 정보를 공유할 수 있는 시민 참여형 플랫폼입니다.

### 프로젝트 목적

최근 국내 주요 도시에서 폭설로 인한 제설 대응의 한계가 뚜렷이 드러나고 있습니다. 기존 행정기관의 제설 시스템은 제설함 위치만 제공할 뿐, 실시간 상태 정보와 시민 참여 기능이 부족합니다.

SnowShare는 다음과 같은 목적으로 개발되었습니다:

- **실시간 정보 공유**: 제설함의 위치, 제설 도구 보유 상태를 실시간으로 확인
- **시민 참여 활성화**: 제설 활동 후기 및 현장 상황을 커뮤니티를 통해 공유
- **효율적 자원 배치**: 행정기관이 시민 데이터를 기반으로 제설 취약 지역을 파악하고 효율적으로 대응
- **정보 접근성 향상**: AI 요약 기능으로 폭설 관련 뉴스를 빠르게 파악

---

## 주요 기능

### 1. 제설함 지도

- 서울시 전역의 제설함 위치를 지도에서 확인
- 제설함 클릭 시 상세 정보(주소, 관리기관) 표시
- 총 10,000개 이상의 제설함 데이터 제공

### 2. 커뮤니티

- **자유게시판**: 제설 관련 자유로운 정보 공유
- **후기게시판**: 제설함 이용 후기 및 별점(1~5점) 평가
- 댓글, 검색, 정렬 기능 지원

### 3. 관련 뉴스

- RSS 피드를 통한 폭설/한파 관련 최신 뉴스 제공
- **Google Gemini AI 요약**: 뉴스 내용을 3~5문장으로 자동 요약
- 원문 링크 제공

### 4. 대응 안내

- 폭설 시 행동요령 안내
- 제설 방법 가이드
- 안전한 보행법 소개

### 5. 회원 시스템

- 회원가입 및 로그인
- 세션 기반 인증
- 게시글/후기 작성 권한 관리

---

## 기술 스택

### Frontend (Client)

- **Java Swing**: Desktop GUI 애플리케이션
- **Gson 2.10.1**: JSON 데이터 파싱
- **Proj4j 1.2.2**: 지도 좌표 변환

### Backend (API)

- **Node.js + Express**: REST API 서버
- **MySQL 8.0**: 데이터베이스
- **bcrypt**: 비밀번호 암호화
- **Winston**: 로깅
- **Google Gemini API**: AI 뉴스 요약

### 배포

- **Railway**: 백엔드 API 및 MySQL 호스팅
- Production URL: `https://snowshare-production.up.railway.app`

---

## 코드 설치 및 실행 방법

### 사전 요구사항

- **Java JDK 8 이상**
- **Node.js 14 이상** (백엔드 로컬 실행 시)
- **MySQL 8.0** (백엔드 로컬 실행 시)

### 1️ 저장소 클론

```bash
git clone https://github.com/skdhfel2/SnowShare.git
cd SnowShare
```

### 2️ 클라이언트 실행 (Java Swing)

#### Windows에서 실행

```bash
cd client
run.bat
```

#### macOS/Linux에서 실행

```bash
cd client
chmod +x run.sh
./run.sh
```

#### 수동 컴파일 및 실행

```bash
cd client

# 컴파일
javac -encoding UTF-8 -cp "lib/*:components/map/lib/*" -d bin Main.java components/**/*.java core/*.java utils/*.java models/*.java

# 실행 (Windows)
java -cp "bin;lib/*;components/map/lib/*" Main

# 실행 (macOS/Linux)
java -cp "bin:lib/*:components/map/lib/*" Main
```

### 3️ 백엔드 API 서버 (선택사항)

기본적으로 클라이언트는 **Railway 프로덕션 서버**에 연결됩니다.  
로컬에서 백엔드를 실행하려면:

```bash
cd api

# 의존성 설치
npm install

# 환경 변수 설정 (.env 파일 생성)
# DB_HOST, DB_USER, DB_PASSWORD, DB_NAME, GEMINI_API_KEY 설정 필요

# 데이터베이스 초기화
node models/initDatabase.js

# 서버 실행
npm start
```

**클라이언트 설정 변경** (`client/config.properties`):

```properties
# 로컬 서버 사용
api.base.url=http://localhost:3000/api
```

---

## 프로젝트 구조

```
SnowShare/
├── client/                          # Java Swing 클라이언트
│   ├── Main.java                    # 메인 진입점
│   ├── core/                        # 핵심 프레임워크
│   │   ├── BaseFrame.java          # 메인 프레임
│   │   ├── BasePanel.java          # 기본 패널
│   │   ├── Navigator.java          # 화면 전환
│   │   └── Session.java            # 세션 관리
│   ├── components/                  # UI 컴포넌트
│   │   ├── auth/                   # 로그인/회원가입
│   │   ├── main/                   # 메인 홈
│   │   ├── news/                   # 뉴스
│   │   ├── guide/                  # 대응안내
│   │   ├── map/                    # 제설함 지도
│   │   ├── community/              # 커뮤니티 (자유/후기 게시판)
│   │   └── common/                 # 공통 컴포넌트
│   ├── utils/                       # 유틸리티 (API 클라이언트 등)
│   ├── models/                      # 데이터 모델
│   ├── lib/                         # JAR 라이브러리
│   ├── public/                      # 리소스 파일
│   │   ├── images/                 # 이미지 (PNG)
│   │   └── data/                   # 제설함 데이터 (JSON)
│   ├── config.properties            # API 서버 설정
│   ├── run.bat                      # Windows 실행 스크립트
│   └── run.sh                       # macOS/Linux 실행 스크립트
│
├── api/                             # Node.js + Express 백엔드
│   ├── app.js                       # Express 앱 진입점
│   ├── routes/                      # API 라우터
│   ├── controllers/                 # 컨트롤러
│   ├── models/                      # 데이터 모델
│   ├── middleware/                  # 미들웨어
│   ├── services/                    # 비즈니스 로직
│   ├── lib/                         # DB, Logger
│   └── package.json                 # Node.js 의존성
│
└── README.md                        # 프로젝트 문서
```

---

## 주요 화면

### 메인 화면

- 시스템 소개 및 주요 기능 안내

### 제설함 지도

- 서울시 전역 제설함 위치 표시
- 마커 클릭 시 상세 정보 팝업

### 커뮤니티

- 자유게시판: 제설 관련 자유로운 소통
- 후기게시판: 제설함 이용 후기 및 별점 평가

### 뉴스

- 폭설 관련 최신 뉴스
- AI 요약 기능으로 핵심 내용 빠르게 파악

---

## 데이터베이스 스키마

### users (사용자)

- `id`, `username`, `password`, `created_at`

### posts (자유게시판)

- `id`, `title`, `content`, `author_id`, `view_count`, `created_at`, `updated_at`

### reviews (후기게시판)

- `id`, `snowbox_name`, `rating`, `title`, `content`, `author_id`, `view_count`, `created_at`, `updated_at`

### comments (댓글)

- `id`, `post_id`, `post_type`, `author_id`, `content`, `created_at`, `updated_at`

---

## 필수 리소스 파일

프로젝트 실행에 필요한 리소스:

### 이미지 파일

- `client/public/images/grit_bin.png`
- `client/public/images/penguin_walk.png`
- `client/public/images/snow_clearing.png`

### 데이터 파일

- `client/public/data/seoul_snowbox_location.json`

### JAR 라이브러리

- `client/lib/gson-2.10.1.jar`
- `client/lib/json-20231013.jar`
- `client/components/map/lib/proj4j-1.2.2.jar`

---

## 배포 정보

### 프로덕션 API 서버

- **URL**: `https://snowshare-production.up.railway.app/api`
- **상태 확인**: `https://snowshare-production.up.railway.app/api/test/db`

### GitHub 저장소

- **Repository**: [https://github.com/skdhfel2/SnowShare](https://github.com/skdhfel2/SnowShare)
- **Public** 접근 가능

---

## 문의 및 개발자 정보

### 개발팀

- **프로젝트명**: SnowShare
- **개발 기간**: 2025년 11월 ~ 12월

### 코드 문의처

프로젝트 관련 문의사항은 아래로 연락해주세요:

- **이메일**: [skdhfel8@gmail.com]

---

## 라이선스

ISC License

---

## 감사의 말

서울시 공공데이터를 제공해주신 서울열린데이터광장에 감사드립니다.
