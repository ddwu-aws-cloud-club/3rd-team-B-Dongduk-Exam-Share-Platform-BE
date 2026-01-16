# 3rd-team-B-Dongduk-Exam-Share-Platform-BE

동덕여자대학교 재학생만을 위한
**학교 이메일 인증 기반 족보(시험 자료) 공유 및 학술 커뮤니티 플랫폼 – Backend**

---

## 환경 설정

### 1. 환경변수 설정

백엔드를 실행하기 전에 `.env` 파일을 생성해야 합니다.

```bash
# .env.example 파일을 복사하여 .env 파일 생성
cp .env.example .env
```

`.env` 파일에 실제 값을 입력하세요:

```bash
# ===============================
# 공통 AWS 설정
# ===============================
AWS_REGION=ap-northeast-2

# ===============================
# AWS SES (이메일 전송)
# ===============================
AWS_SES_ACCESS_KEY=실제_SES_Access_Key
AWS_SES_SECRET_KEY=실제_SES_Secret_Key
SES_FROM_EMAIL=인증된_이메일@gmail.com

# ===============================
# AWS S3 (PDF 저장) - 선택사항
# ===============================
AWS_S3_ACCESS_KEY=실제_S3_Access_Key
AWS_S3_SECRET_KEY=실제_S3_Secret_Key
S3_BUCKET=somshare-pdf-bucket

# ===============================
# Database (PostgreSQL)
# ===============================
DB_URL=jdbc:postgresql://localhost:5432/somshare
DB_USERNAME=somshare_user
DB_PASSWORD=실제_비밀번호
```

**주의사항:**
- `.env` 파일은 `.gitignore`에 포함되어 있어 Git에 커밋되지 않습니다
- AWS Access Key는 절대 공개하지 마세요
- SES와 S3는 서로 다른 IAM 사용자 또는 역할로 분리하는 것을 권장합니다

### 2. 프로필 선택

- **dev 프로필**: AWS SES로 실제 이메일 전송 (`.env` 파일 필요)
- **simple 프로필**: 콘솔 로그만 출력 (AWS 설정 불필요)

```bash
# dev 프로필로 실행 (실제 이메일 전송)
./gradlew bootRun --args='--spring.profiles.active=dev'

# simple 프로필로 실행 (콘솔 로그만)
./gradlew bootRun --args='--spring.profiles.active=simple'
```

---

## ERD (Entity Relationship Diagram)

<img width="940" height="446" alt="image" src="https://github.com/user-attachments/assets/f936d6c4-1700-4e72-8118-25a25a6cd9fd" />

---

## System Architecture

<img width="684" height="699" alt="aws" src="https://github.com/user-attachments/assets/d3aa794f-f098-4d52-af21-1db2496524dc" />


