# ☁️ 클라우드 아키텍처 설계 & 배포 과제

---

## 📌 프로젝트 소개
Spring Boot 기반의 팀원 정보 관리 API입니다.
AWS 클라우드 환경에서 VPC 네트워크 구성, RDS DB 연동, S3 파일 저장, ALB + ASG 구성,
GitHub Actions를 통한 CI/CD 자동화까지 단계별로 구축했습니다.

---

## 🛠 기술 스택
| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.3 |
| ORM | Spring Data JPA / Hibernate |
| DB | MySQL (AWS RDS), H2 (local) |
| Infra | AWS VPC, EC2, RDS, S3, ALB, ASG, CloudFront |
| CI/CD | GitHub Actions, Docker, Docker Hub |
| Config | AWS Parameter Store |
| Monitoring | Spring Actuator |
| Build | Gradle |
 
---

## 🗂️ 프로젝트 구조

```
src/
├── main/
│   ├── java/com/cloud/
│   │   ├── CloudApplication.java
│   │   └── member/
│   │       ├── controller/    MemberController.java
│   │       ├── service/       MemberService.java, S3Service.java
│   │       ├── repository/    MemberRepository.java
│   │       ├── entity/        Member.java
│   │       ├── dto/           MemberRequest.java, MemberResponse.java
│   │       └── exception/     GlobalExceptionHandler.java
│   └── resources/
│       ├── application.properties         # 공통 설정
│       ├── application-local.properties   # 로컬 (H2)
│       └── application-prod.properties    # 운영 (MySQL + Parameter Store)
├── Dockerfile
└── .github/workflows/deploy.yml
```

---

## ✅ 구현 단계별 기능

### LV 0 - AWS Budget 설정 💰

> 요금 폭탄 방지를 위한 비용 알림 설정

- AWS Budgets에서 **월 예산 $100** 설정
- 예산의 **80% 도달 시 이메일 알림** 수신 설정

📸 **Budget 설정 화면**
<img width="1506" height="736" alt="스크린샷 2026-03-13 120803" src="https://github.com/user-attachments/assets/c6984db4-0313-4cd3-8b0e-c00c0445a63f" />

---

### LV 1 - 네트워크 구축 및 핵심 기능 배포 🌐

> VPC 설계부터 EC2 배포, 헬스체크까지

**인프라**
- VPC 생성 및 Public / Private Subnet 분리
- Public Subnet에 EC2 인스턴스 생성

**API 기능**

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/members` | 팀원 정보(이름, 나이, MBTI) 저장 |
| GET | `/api/members/{id}` | 팀원 정보 조회 |

**운영 설정**
- Spring Profile 분리: `local`(H2) / `prod`(MySQL)
- INFO 레벨 API 요청 로그: `[API - LOG] ...`
- ERROR 레벨 예외 처리 + 스택트레이스 로깅
- Spring Actuator 헬스체크 노출

```json
GET /actuator/health
{"status": "UP"}
```

🖥️ **EC2 퍼블릭 IP**: `13.125.129.84`

---

### LV 2 - DB 분리 및 보안 연결 🔐

> RDS 분리 + 보안 그룹 체이닝 + Parameter Store

**인프라**
- Public Subnet에 MySQL RDS 생성 (로컬 접속 테스트 가능)
- **보안 그룹 체이닝**: RDS Inbound에 IP 직접 허용 대신 EC2 보안 그룹 ID만 허용

**설정**
- AWS Parameter Store에 DB 접속 정보 저장 (`url`, `username`, `password`, `team-name`)
- 앱 실행 시 Parameter Store 값 자동 주입

**Actuator Info 확장**
```json
GET /actuator/info
{"app": {"team-name": "Team2"}}
```

📍 **Actuator Info URL**: `https://pposong.kr/actuator/info`

📸 **RDS 보안 그룹 인바운드 규칙 화면**
<img width="1650" height="481" alt="스크린샷 2026-03-13 122014" src="https://github.com/user-attachments/assets/33fa2510-2047-452e-a779-6cbd07c6fc8e" />


---

### LV 3 - 프로필 사진 기능 추가 & 권한 관리 🖼️

> S3 + IAM Role + Presigned URL

**인프라**
- S3 버킷 생성 (퍼블릭 액세스 차단 ON)
- IAM Role 생성 → EC2에 연결 (Access Key 코드 미포함)

**API 기능**

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/members/{id}/profile-image` | 프로필 사진 S3 업로드 |
| GET | `/api/members/{id}/profile-image` | Presigned URL 반환 (유효기간 7일) |

🔗 **Presigned URL**:
```
https://camp-health-pposong-files.s3.ap-northeast-2.amazonaws.com/profiles/6aaf57ff-fa0d-412b-b99f-cfee8c0f966b_2.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ASIASSDNAZWDS5S4ADTL%2F20260313%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Date=20260313T014914Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Security-Token=IQoJb3JpZ2luX2VjELr%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDmFwLW5vcnRoZWFzdC0yIkYwRAIgGwyBGbQCpyRNZmG%2F3zN5E%2F6ysrLwoWCao5hkTFZOsN0CIACV7g%2B2oYt83sJudRzNTkz2rUafx0haVxQKKvU3g7JRKtQFCIP%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEQABoMMTc2MzIyMzAxMzE5Igzm17YtM4H69uBs0RIqqAWif5pNGmtroPNqqtTZyZ27YGSV9JFM8B%2BVPq2LzCv1BelPaCfBbyD18jTvjsPHB4ca3qRCnPJA5zo8mQKZ1Pb4rQf68HAZq%2BtmgvpvIoorWvB1XL5wXYHFq%2BOUehXb55Pdp8JFnHfLDz7YgsUJ5wS2LoamEDyGcsNFkEMRX62R%2FjBZVlnVT7eoRRal4qA5UD1f66DvvysZCs%2FCLO1CDBm3%2F24gRcW9gASDbY3CfwGOYr529vDGOoqrfZXV1VYl2XXhMCiT%2Fz2QoJpypKZQW9O7zbh9oDUA81aBPAPegoQrJNFZZ6N8M2ZErQAuanJpZnUoRrbHUrZ4RzixD5rHZeE5y1ApRJBgPDY9lFxT064T1UWYFvUUuF6mzqeuUoC%2BySyro9N0NNtAtKUlGR8DyqArGWbaRj2qxNneS%2BEcw95LXASUDRzZR%2F3WnXpArxGiSuoy119WP2jeW9PZL%2FZPNPmP0Cx1uLJm8nSs0zAZMuOUuaX7Cf4PV07sSTz4l7K%2FMFyFERok2OuyIFxNmTlpzSVgMEnvDyepTFs5zqhAS05%2BDhX9qc2%2FUpPm7McoZQJjFAn8qV5RcCEwG63xVRxGavwe10hecRu0EhpZBjzlAXC4KoHhEUmRGKA%2BGhvLIUjz1T1Khx8w9WlL7eBJivMEZqNo20Rd%2FEiolpeythQ8Kc6urJhbFNy6ZtBmjeKvJPXIQjmM4H52RahOWSAjP1lKoFSlhC2jQsTAJ%2FJ7hJ%2BeX%2FGFuD1Or45KnTexIG7WOnKnBqKGoHSaY8POsT9wCr4KF0ZXirp8acMi7f6VyK40o8lMpiFwQ8%2Bojn0Mt3ZIQzZRlo3%2B2mNDsTvHyOTyo44MC0sPUNd%2BQBgsAQlAtIhS6B3w5V1k3jz%2BUhGA5L2v22aKiCQHu6y1QzQ8SzD70c3NBjqyAVAfqvT6ZNdhSx3w85fGz%2BHw%2F%2BsdZn5xNf79Xclhq3ng0uOZYO2r7gAkTHJSXohoxQS%2FO8EnA%2F8i6%2FMe7EkK5ihFRDazPDEQLPi%2FL7jvZrvxCDXAmpy1mT6y%2BrpNXV91xY3l9MWA2iGy7RX5Fb3q7bzfrx9%2Bl4Z5h8sRCbES68vjo9MX2z%2FWnKIO42nA38VSuhs8ZPHrLQXt1U26N%2B1gM9JXdpR6%2BL5%2BaZDOaZNsPRtr9Dk%3D&X-Amz-Signature=9507ffffb33d68403a0826c4f35b870edfdf3d6be671879a4f18fd3e2ea85f62
```
⏰ **만료 시간**: 2026년 3월 20일
<img width="732" height="669" alt="스크린샷 2026-03-13 123909" src="https://github.com/user-attachments/assets/d8bda404-7063-4f76-93b3-0e0fd61ab828" />

---

### LV 4 - Docker & CI/CD 파이프라인 🐳

> Dockerfile 작성 + GitHub Actions 자동 배포

**Docker**
- `Dockerfile` 작성 (Amazon Corretto 17 기반)
- `app.jar` 빌드 후 이미지화

**GitHub Actions (`deploy.yml`)**
1. main 브랜치 Push 시 자동 트리거
2. Gradle 빌드 (`bootJar`)
3. Docker 이미지 빌드 → Docker Hub Push
4. AWS 자격증명 설정
5. ASG Instance Refresh로 EC2 자동 재배포

📸 **GitHub Actions 성공 화면**
<img width="1306" height="322" alt="스크린샷 2026-03-13 123005" src="https://github.com/user-attachments/assets/0dce5f61-5c7f-4455-b4b6-6578e0aba5eb" />

📸 **EC2 `sudo docker ps` 실행 화면**
<img width="1311" height="79" alt="스크린샷 2026-03-13 124311" src="https://github.com/user-attachments/assets/c99ae45b-9e28-432a-819b-05de7b702ae6" />


---

### LV 5 - 고가용성 아키텍처 & HTTPS 도메인 연결 🏗️

> NAT Gateway + ALB + ASG + ACM

**인프라 변경**
- NAT Gateway 생성 (Public Subnet) → Private Subnet 라우팅 설정
- RDS & EC2 → Private Subnet으로 이전 (로컬 직접 접속 불가)

**도메인 & 보안**
- Cloudflare에서 도메인 연결
- ACM에서 SSL 인증서 발급

**로드 밸런서 & 오토 스케일링**
- ALB 생성: HTTPS(443) 리스너 + 인증서 적용, HTTP → HTTPS 리다이렉트
- Launch Template 작성 → Auto Scaling Group 생성
- Cloudflare CNAME → ALB DNS 연결

🌍 **HTTPS 도메인**: `https://pposong.kr`

📸 **Target Group Healthy 상태 화면**
<img width="1636" height="281" alt="스크린샷 2026-03-13 124422" src="https://github.com/user-attachments/assets/37fdbfef-8831-44ee-9f23-695566b69582" />

---

### LV 6 - CloudFront CDN 적용 🌏

> S3 + CloudFront로 글로벌 이미지 배포

**구성**
- S3 버킷을 Origin으로 CloudFront 배포 생성
- Presigned URL → CloudFront 도메인 URL 방식으로 변경

🔗 **CloudFront 이미지 URL**:
`https://d1v1uch0vgh0vj.cloudfront.net/profiles/6aaf57ff-fa0d-412b-b99f-cfee8c0f966b_2.jpg`

---

## 🚀 API 명세

| Method | URL | 설명 | 상태코드 |
|--------|-----|------|---------|
| POST | `/api/members` | 팀원 등록 | 201 |
| GET | `/api/members/{id}` | 팀원 조회 | 200 |
| POST | `/api/members/{id}/profile-image` | 프로필 사진 업로드 | 200 |
| GET | `/api/members/{id}/profile-image` | 프로필 이미지 URL 조회 | 200 |
| GET | `/actuator/health` | 헬스체크 | 200 |
| GET | `/actuator/info` | 팀 정보 조회 | 200 |

---

## ⚙️ 환경 설정

### Profile 분리

| Profile | DB | 용도 |
|---------|-----|------|
| `local` | H2 (In-Memory) | 로컬 개발 |
| `prod` | MySQL (RDS) | 운영 배포 |

### Parameter Store 경로
```
/camp-health-app/prod/
  ├── spring.datasource.url
  ├── spring.datasource.username
  ├── spring.datasource.password
  └── TEAM-NAME
```

---

## 🐛 트러블슈팅

_(트러블슈팅 내용 및 TIL 블로그 링크 첨부)_
