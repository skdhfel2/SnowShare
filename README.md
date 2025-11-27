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

### 다른 사람의 저장소에 참여하기

다른 사람이 만든 저장소에 참여하는 방법은 [COLLABORATION_GUIDE.md](./COLLABORATION_GUIDE.md)를 참고하세요.

**빠른 시작:**

```bash
# 1. 저장소 Fork (GitHub에서 Fork 버튼 클릭)

# 2. Fork한 저장소 클론
git clone https://github.com/내계정/저장소명.git
cd 저장소명

# 3. 원본 저장소를 upstream으로 추가
git remote add upstream https://github.com/원본소유자/저장소명.git

# 4. 작업 브랜치 생성
git checkout -b feature/새기능명

# 5. 작업 후 커밋 및 푸시
git add .
git commit -m "feat: 새 기능 추가"
git push origin feature/새기능명

# 6. GitHub에서 Pull Request 생성
```

### 저장소 소유자: 협업자 초대하기

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



```
SnowShare
├─ api
│  ├─ .eslintrc.js
│  ├─ .prettierrc
│  ├─ app.js
│  ├─ controllers
│  │  └─ testController.js
│  ├─ lib
│  │  ├─ db.js
│  │  └─ logger.js
│  ├─ middleware
│  │  └─ errorHandler.js
│  ├─ models
│  │  └─ README.md
│  ├─ package.json
│  ├─ routes
│  │  └─ test.js
│  └─ utils
│     └─ README.md
├─ client
│  ├─ App.java
│  ├─ components
│  │  ├─ auth
│  │  │  ├─ LoginPanel.java
│  │  │  └─ RegisterPanel.java
│  │  ├─ common
│  │  │  └─ HeaderNav.java
│  │  ├─ community
│  │  │  └─ CommunityPanel.java
│  │  ├─ guide
│  │  │  └─ GuidePanel.java
│  │  ├─ main
│  │  │  └─ HomePanel.java
│  │  ├─ map
│  │  │  ├─ CoordinateConverter.java
│  │  │  ├─ lib
│  │  │  ├─ MapPanel.java
│  │  │  ├─ SnowRemovalMap.java
│  │  │  └─ SnowRemovalMap1.java
│  │  ├─ news
│  │  │  └─ NewsPanel.java
│  │  └─ README.md
│  ├─ core
│  │  ├─ BaseFrame.java
│  │  ├─ BasePanel.java
│  │  ├─ Navigator.java
│  │  └─ Session.java
│  ├─ hooks
│  │  └─ README.md
│  ├─ Main.java
│  ├─ public
│  │  └─ data
│  │     └─ seoul_snowbox_location.json
│  ├─ README.md
│  └─ utils
│     └─ README.md
└─ README.md

```