# 협업 가이드 (Contributing Guide)

SnowShare 프로젝트에 기여해주셔서 감사합니다! 이 문서는 프로젝트에 참여하는 방법을 안내합니다.

## 시작하기

### 1. 저장소 클론

```bash
git clone https://github.com/skdhfel2/SnowShare.git
cd SnowShare
```

### 2. 원격 저장소 확인

```bash
git remote -v
```

## 협업 워크플로우

### 브랜치 전략

- `main`: 프로덕션 준비 코드 (안정적인 버전)
- `develop`: 개발 브랜치 (기능 개발 통합)
- `feature/기능명`: 새로운 기능 개발
- `fix/버그명`: 버그 수정
- `hotfix/긴급수정명`: 긴급 수정

### 작업 흐름

1. **최신 코드 가져오기**
   ```bash
   git fetch origin
   git pull origin main
   ```

2. **새 브랜치 생성**
   ```bash
   git checkout -b feature/새기능명
   # 또는
   git checkout -b fix/버그수정명
   ```

3. **작업 및 커밋**
   ```bash
   # 파일 수정 후
   git add .
   git commit -m "feat: 새 기능 추가"
   ```

4. **원격 저장소에 푸시**
   ```bash
   git push origin feature/새기능명
   ```

5. **Pull Request 생성**
   - GitHub에서 Pull Request를 생성합니다
   - 리뷰를 요청하고 피드백을 받습니다
   - 승인 후 `main` 브랜치에 병합됩니다

## 커밋 메시지 규칙

커밋 메시지는 다음 형식을 따릅니다:

```
타입: 간단한 설명

상세 설명 (선택사항)
```

### 타입 종류

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅, 세미콜론 누락 등
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가/수정
- `chore`: 빌드 업무 수정, 패키지 매니저 설정 등

### 예시

```bash
git commit -m "feat: RSS 뉴스 피드 연동 기능 추가"
git commit -m "fix: 뉴스 필터링 로직 오류 수정"
git commit -m "docs: 협업 가이드 문서 추가"
```

## 코드 스타일

### Java
- 들여쓰기: 2칸 스페이스
- 클래스명: PascalCase
- 메서드/변수명: camelCase

### JavaScript/Node.js
- 들여쓰기: 2칸 스페이스
- 세미콜론 사용
- 싱글 따옴표 사용
- Trailing comma 사용

```bash
# ESLint 검사
cd api
npm run lint

# 자동 수정
npm run lint:fix
```

## Pull Request 체크리스트

PR을 생성하기 전에 다음을 확인하세요:

- [ ] 코드가 정상적으로 작동하는가?
- [ ] 테스트를 통과하는가?
- [ ] 코드 스타일 가이드를 따르는가?
- [ ] 불필요한 코드나 주석을 제거했는가?
- [ ] 커밋 메시지가 명확한가?
- [ ] 관련 문서를 업데이트했는가?

## 이슈 리포트

버그를 발견하거나 기능 제안이 있으시면 GitHub Issues에 등록해주세요.

### 버그 리포트
- 버그 설명
- 재현 단계
- 예상 동작
- 실제 동작
- 환경 정보 (OS, Java 버전 등)

### 기능 제안
- 기능 설명
- 사용 사례
- 구현 아이디어 (선택사항)

## 문의

질문이나 도움이 필요하시면:
- GitHub Issues에 등록
- 프로젝트 관리자에게 직접 연락

## 라이선스

이 프로젝트는 ISC 라이선스를 따릅니다.

