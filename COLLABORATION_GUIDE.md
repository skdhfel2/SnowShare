# 다른 사람의 저장소에 참여하기 가이드

다른 사람이 만든 GitHub 저장소에 참여하는 방법을 안내합니다.

## 방법 1: Fork & Pull Request (오픈소스 기여)

저장소 소유자가 직접 초대하지 않아도 참여할 수 있는 방법입니다.

### 1단계: 저장소 Fork

1. GitHub에서 참여하고 싶은 저장소로 이동
2. 우측 상단의 **Fork** 버튼 클릭
3. Fork할 계정/조직 선택
4. Fork 완료 (내 계정에 복사본이 생성됨)

### 2단계: Fork한 저장소 클론

```bash
# 내가 Fork한 저장소를 클론
git clone https://github.com/내계정/저장소명.git
cd 저장소명
```

### 3단계: 원본 저장소를 upstream으로 추가

```bash
# 원본 저장소를 upstream으로 추가
git remote add upstream https://github.com/원본소유자/저장소명.git

# 원격 저장소 확인
git remote -v
# origin    https://github.com/내계정/저장소명.git (fetch)
# origin    https://github.com/내계정/저장소명.git (push)
# upstream  https://github.com/원본소유자/저장소명.git (fetch)
# upstream  https://github.com/원본소유자/저장소명.git (push)
```

### 4단계: 작업 브랜치 생성 및 작업

```bash
# 최신 코드 가져오기
git fetch upstream
git checkout main
git merge upstream/main

# 새 기능 브랜치 생성
git checkout -b feature/새기능명

# 작업 후 커밋
git add .
git commit -m "feat: 새 기능 추가"

# 내 Fork 저장소에 푸시
git push origin feature/새기능명
```

### 5단계: Pull Request 생성

1. GitHub에서 내 Fork 저장소로 이동
2. **Compare & pull request** 버튼 클릭
3. PR 제목과 설명 작성
4. **Create pull request** 클릭
5. 원본 저장소 소유자가 리뷰 후 병합

### 6단계: 원본 저장소와 동기화 유지

```bash
# 원본 저장소의 최신 변경사항 가져오기
git fetch upstream
git checkout main
git merge upstream/main

# 내 Fork에도 업데이트 반영
git push origin main
```

## 방법 2: 협업자로 초대받기

저장소 소유자가 직접 초대한 경우입니다.

### 1단계: 초대 수락

1. GitHub 이메일에서 초대 링크 클릭
2. 또는 저장소 페이지에서 초대 알림 확인
3. **Accept invitation** 클릭

### 2단계: 저장소 클론

```bash
# 원본 저장소를 직접 클론 (Fork 불필요)
git clone https://github.com/소유자/저장소명.git
cd 저장소명
```

### 3단계: 작업 브랜치 생성

```bash
# 최신 코드 가져오기
git pull origin main

# 새 브랜치 생성
git checkout -b feature/새기능명

# 작업 후 커밋
git add .
git commit -m "feat: 새 기능 추가"

# 원본 저장소에 푸시 (권한이 있으면)
git push origin feature/새기능명
```

### 4단계: Pull Request 생성

1. GitHub 저장소 페이지로 이동
2. **Pull requests** 탭 클릭
3. **New pull request** 클릭
4. base: `main` ← compare: `feature/새기능명` 선택
5. PR 제목과 설명 작성
6. **Create pull request** 클릭

## 방법 3: 로컬에서만 작업 (공식 기여 아님)

저장소를 클론해서 로컬에서만 수정하고 사용하는 경우입니다.

```bash
# 저장소 클론
git clone https://github.com/소유자/저장소명.git
cd 저장소명

# 로컬에서만 브랜치 생성 및 작업
git checkout -b my-changes
# ... 작업 ...
git add .
git commit -m "내 수정사항"

# 원본 저장소에는 푸시하지 않음 (권한이 없거나 원하지 않는 경우)
```

## Git 설정 확인

다른 사람의 저장소에 기여할 때는 본인의 Git 정보가 올바르게 설정되어 있어야 합니다.

```bash
# 현재 설정 확인
git config --global user.name
git config --global user.email

# 설정이 안 되어 있다면
git config --global user.name "본인이름"
git config --global user.email "본인이메일@example.com"
```

## 주의사항

1. **main 브랜치에 직접 푸시하지 않기**
   - 항상 새 브랜치를 만들어서 작업
   - Pull Request를 통해 코드 리뷰 받기

2. **커밋 메시지 규칙 따르기**
   - 저장소의 CONTRIBUTING.md 확인
   - 일반적인 형식: `타입: 설명` (예: `feat: 새 기능 추가`)

3. **코드 스타일 준수**
   - 저장소의 코드 스타일 가이드 확인
   - ESLint, Prettier 등 설정 확인

4. **최신 코드 유지**
   - 작업 전 항상 `git pull` 또는 `git fetch`로 최신 코드 가져오기
   - 충돌 방지

## 문제 해결

### 충돌 발생 시

```bash
# 최신 코드 가져오기
git fetch upstream  # 또는 origin
git checkout main
git pull upstream main  # 또는 origin main

# 내 브랜치에 최신 코드 병합
git checkout feature/내브랜치
git merge main
# 충돌 해결 후
git add .
git commit -m "merge: main 브랜치와 병합"
```

### 원격 브랜치 삭제 후 다시 푸시

```bash
# 원격 브랜치 삭제
git push origin --delete feature/브랜치명

# 다시 푸시
git push origin feature/브랜치명
```

## 참고 자료

- [GitHub Fork 가이드](https://docs.github.com/en/get-started/quickstart/fork-a-repo)
- [GitHub Pull Request 가이드](https://docs.github.com/en/pull-requests)
- [Git 브랜치 전략](https://www.atlassian.com/git/tutorials/comparing-workflows)



