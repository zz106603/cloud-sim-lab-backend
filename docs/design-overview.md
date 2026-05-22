# 실무형 클라우드 아키텍처 시뮬레이터 설계서

# 1. 프로젝트 정의

## 프로젝트 목적

AWS 개념을 단순히 설명하는 것이 아니라,

백엔드 운영 상황에서 어떤 아키텍처를 선택해야 하는지 판단하고,

그 선택이 성능 / 비용 / 안정성 / 운영 복잡도에 어떤 영향을 주는지 학습하는 플랫폼.

---

# 2. 핵심 방향

이 프로젝트는 AI 챗봇 서비스가 아니다.

핵심은 다음이다.

- 실무 상황 기반 학습
- AWS 아키텍처 선택 훈련
- 장애 흐름 시뮬레이션
- 선택에 따른 결과 비교
- 단계별 학습 문서 연결
- 운영 관점의 trade-off 학습

AI 기능은 MVP 이후 확장 기능으로 둔다.

---

# 3. 서비스 한 줄 설명

백엔드 운영 상황에 따라 AWS 아키텍처를 선택하고,

성능·비용·장애 흐름 변화를 시뮬레이션하며 학습하는 실무형 클라우드 아키텍처 학습 플랫폼.

---

# 4. 핵심 사용자

## 대상 사용자

- AWS를 처음 실무 관점으로 학습하는 백엔드 개발자
- EC2, VPC, ALB, RDS 등을 개념은 알지만 실제 구성 이유가 약한 개발자
- 면접 대비용으로 인프라 구조를 정리하려는 주니어 개발자
- 장애 상황과 운영 흐름을 이해하고 싶은 개발자

---

# 5. MVP 범위

## MVP 포함 기능

- 학습 문서 목록
- 학습 문서 상세
- 시나리오 목록
- 시나리오 상세
- 현재 아키텍처 표시
- 사용자 선택지 제공
- 선택 결과 시뮬레이션
- 결과 피드백
- 관련 학습 문서 연결

## MVP 제외 기능

- AI 랜덤 시나리오 생성
- AI 답변 평가
- 실제 AWS 리소스 생성
- 실시간 비용 계산
- 실제 트래픽 테스트
- 사용자별 고도화된 학습 분석
- 팀 / 조직 기능

---

# 6. 핵심 기능

# 6.1 학습 문서 기능

AWS 개념을 단계별로 정리한 문서를 제공한다.

## 문서 카테고리

- Cloud 기본
- EC2
- VPC
- Subnet
- Security Group
- NAT Gateway
- ALB
- Auto Scaling
- RDS
- Read Replica
- Redis
- 데이터 정합성
- 동시성
- 장애 대응
- CI/CD

## 문서 구조

```markdown
# 주제명

## 왜 필요한가

## 핵심 개념

## 실제 구조

## 요청 흐름

## 장애 상황

## 실무 주의점

## 면접 질문
```

---

# 6.2 시나리오 기반 학습 기능

사용자는 정해진 실무 상황을 선택한다.

## 시나리오 예시

1. Spring Boot 단일 서버 배포
2. Private Subnet 서버 구성
3. 트래픽 급증 대응
4. RDS 장애 대응
5. 조회 성능 개선
6. Redis 장애 상황
7. Connection Pool 고갈
8. ALB Health Check 실패
9. NAT Gateway 미구성
10. Security Group 오설정

---

# 6.3 아키텍처 시각화

각 시나리오는 현재 구조를 시각적으로 보여준다.

## 예시

```
Client
  ↓
EC2
  ↓
RDS
```

사용자가 ALB와 Auto Scaling을 선택하면:

```
Client
  ↓
ALB
  ↓
EC2 x 2
  ↓
RDS
```

---

# 6.4 선택형 시뮬레이션

사용자는 주어진 상황에서 해결 방법을 선택한다.

## 예시 상황

```
이벤트 시작 후 API 응답 시간이 3초 이상으로 증가했다.
RDS CPU 사용률이 90% 이상이다.
조회 API 트래픽이 대부분이다.
```

## 선택지 예시

- EC2 인스턴스 타입 증가
- ALB 추가
- Auto Scaling 추가
- Read Replica 추가
- Redis Cache 추가
- RDS Multi-AZ 추가

---

# 6.5 결과 피드백

사용자 선택에 따라 결과를 제공한다.

## 결과 항목

- 성능 영향
- 비용 영향
- 안정성 영향
- 운영 복잡도 영향
- 정합성 이슈 가능성
- 추천 여부
- 왜 적절한지
- 왜 부족한지

## 예시

```
Redis Cache 추가는 조회 부하를 줄이는 데 효과적이다.
하지만 데이터 변경이 잦은 API라면 캐시 무효화 전략이 필요하다.

Read Replica 추가도 조회 트래픽 분산에 도움이 된다.
다만 복제 지연으로 인해 최신 데이터 조회가 필요한 기능에는 주의가 필요하다.
```

---

# 7. 시뮬레이션 평가 기준

각 선택지는 고정된 평가 기준을 가진다.

| 지표 | 설명 |
| --- | --- |
| performance | 응답 속도와 처리량 개선 |
| availability | 장애 대응 능력 |
| cost | 비용 증가 또는 절감 |
| complexity | 운영 복잡도 |
| consistency | 데이터 정합성 영향 |
| security | 보안 구조 개선 |

---

# 8. 시나리오 데이터 구조

```json
{
  "id": "traffic-spike-read-heavy",
  "title": "조회 트래픽 급증",
  "level": "INTERMEDIATE",
  "category": "PERFORMANCE",
  "problem": "이벤트 시작 후 조회 API 트래픽이 급증했고 RDS CPU가 90% 이상으로 증가했다.",
  "initialArchitecture": [
    "Client",
    "EC2",
    "RDS"
  ],
  "options": [
    {
      "id": "add-redis",
      "label": "Redis Cache 추가",
      "effects": {
        "performance": 4,
        "availability": 1,
        "cost": -2,
        "complexity": -2,
        "consistency": -2,
        "security": 0
      },
      "feedback": "조회 부하 감소에는 효과적이지만 캐시 무효화 전략이 필요하다."
    }
  ],
  "recommendedOptions": [
    "add-redis"
  ],
  "relatedDocs": [
    "redis-cache",
    "cache-aside-pattern"
  ]
}
```

---

# 9. 점수 규칙

## 점수 범위

```
-5 ~ +5
```

## 의미

| 점수 | 의미 |
| --- | --- |
| +5 | 매우 긍정적 |
| +3 | 긍정적 |
| 0 | 영향 없음 |
| -3 | 부정적 |
| -5 | 매우 부정적 |

## 주의

비용과 복잡도는 점수가 낮을수록 부담 증가를 의미한다.

예시:

```
cost: -3
```

→ 비용 증가

```
complexity: -2
```

→ 운영 복잡도 증가

---

# 10. 추천 결과 판단

| 결과 | 조건 |
| --- | --- |
| GOOD | 핵심 문제를 직접 해결 |
| PARTIAL | 일부 문제만 해결 |
| RISKY | 해결은 가능하지만 부작용 큼 |
| WRONG | 문제 원인과 맞지 않음 |

---

# 11. 예시 시뮬레이션 흐름

## 시나리오

```
조회 트래픽 급증으로 RDS CPU가 90% 이상 증가했다.
```

## 초기 구조

```
Client → EC2 → RDS
```

## 사용자 선택

```
Redis Cache 추가
Read Replica 추가
```

## 결과

```
분류: GOOD

조회 트래픽을 Redis와 Read Replica로 분산할 수 있다.
RDS Primary에 집중되던 부하가 줄어든다.
다만 캐시 무효화와 복제 지연을 함께 고려해야 한다.
```

## 변경 후 구조

```
Client
  ↓
EC2
  ├── Redis
  └── RDS Read Replica
        ↓
      RDS Primary
```

## 관련 학습 문서

- Redis Cache
- Read Replica
- Cache Aside Pattern
- 데이터 정합성

---

# 12. 도메인 모델

# 12.1 LearningDocument

학습 문서

| 필드 | 설명 |
| --- | --- |
| id | 문서 ID |
| title | 제목 |
| category | 카테고리 |
| level | 난이도 |
| content | Markdown 본문 |
| orderIndex | 학습 순서 |
| relatedDocumentIds | 관련 문서 ID 목록 |

---

# 12.2 Scenario

시뮬레이션 시나리오

| 필드 | 설명 |
| --- | --- |
| id | 시나리오 ID |
| title | 제목 |
| category | 시나리오 유형 |
| level | 난이도 |
| problem | 문제 상황 |
| initialArchitecture | 초기 아키텍처 |
| options | 선택지 목록 |
| recommendedOptionIds | 추천 선택지 |
| relatedDocumentIds | 관련 문서 |

---

# 12.3 ScenarioOption

시나리오 선택지

| 필드 | 설명 |
| --- | --- |
| id | 선택지 ID |
| label | 선택지 이름 |
| description | 설명 |
| effects | 평가 지표 변화 |
| feedback | 선택 피드백 |

---

# 12.4 SimulationResult

시뮬레이션 결과

| 필드 | 설명 |
| --- | --- |
| scenarioId | 시나리오 ID |
| selectedOptionIds | 사용자 선택지 |
| resultType | GOOD / PARTIAL / RISKY / WRONG |
| summary | 결과 요약 |
| detailFeedback | 상세 피드백 |
| finalArchitecture | 변경 후 아키텍처 |
| relatedDocumentIds | 추천 학습 문서 |

---

# 13. API 설계

# 13.1 학습 문서 목록 조회

```
GET /api/docs
```

---

# 13.2 학습 문서 상세 조회

```
GET /api/docs/{documentId}
```

---

# 13.3 시나리오 목록 조회

```
GET /api/scenarios
```

## Query Parameter

| 이름 | 설명 |
| --- | --- |
| category | PERFORMANCE / SECURITY / AVAILABILITY |
| level | BASIC / INTERMEDIATE / ADVANCED |

---

# 13.4 시나리오 상세 조회

```
GET /api/scenarios/{scenarioId}
```

---

# 13.5 시뮬레이션 실행

```
POST /api/scenarios/{scenarioId}/simulate
```

## 요청 예시

```json
{
  "selectedOptionIds": [
    "add-redis",
    "add-read-replica"
  ]
}
```

---

# 14. 화면 설계

# 14.1 메인 화면

## 구성

- 프로젝트 소개
- 학습 시작 버튼
- 시나리오 시작 버튼
- 추천 학습 경로
- 인기 시나리오

---

# 14.2 학습 문서 목록 화면

## 구성

- 카테고리 필터
- 난이도 필터
- 문서 카드 목록

---

# 14.3 학습 문서 상세 화면

## 구성

- 문서 본문
- 관련 문서
- 관련 시나리오
- 면접 질문
- 다음 학습 문서

---

# 14.4 시나리오 목록 화면

## 구성

- 시나리오 카드 목록
- 카테고리 필터
- 난이도 필터

---

# 14.5 시나리오 상세 화면

## 구성

- 문제 상황
- 현재 아키텍처
- 선택지
- 시뮬레이션 실행 버튼

---

# 14.6 시뮬레이션 결과 화면

## 구성

- 결과 타입
- 점수 변화
- 변경 후 아키텍처
- 상세 피드백
- 관련 학습 문서
- 다시 시도 버튼

---

# 15. 초기 시나리오 목록

## 15.1 Spring Boot 단일 서버 배포

### 문제

Spring Boot 애플리케이션을 처음 외부에 배포하려고 한다.

### 추천 구성

- EC2
- Security Group
- Nginx
- Public IP

---

## 15.2 Private Subnet 서버 구성

### 문제

애플리케이션 서버를 외부에서 직접 접근하지 못하게 하고 싶다.

### 추천 구성

- VPC
- Public Subnet
- Private Subnet
- ALB
- NAT Gateway
- Private EC2

---

## 15.3 트래픽 급증 대응

### 문제

이벤트 이후 API 요청이 급증해 EC2 한 대로 감당하기 어렵다.

### 추천 구성

- ALB
- Auto Scaling Group
- Multi-AZ
- Target Group

---

## 15.4 RDS 장애 대응

### 문제

DB 장애 시 서비스 전체가 중단될 위험이 있다.

### 추천 구성

- RDS Multi-AZ
- Backup
- Failover

---

## 15.5 조회 성능 개선

### 문제

조회 API 트래픽이 많아 RDS Primary 부하가 크다.

### 추천 구성

- Read Replica
- Redis Cache
- Cache Aside Pattern
- Connection Pool 조정

---

# 16. 기술 스택 제안

# Backend

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Gradle
- REST API

# Frontend

- React
- TypeScript
- Vite
- Tailwind CSS
- Mermaid.js 또는 React Flow

# Infra

- Docker Compose
- PostgreSQL
- GitHub Actions

---

# 17. AI 확장 설계

MVP에서는 AI를 사용하지 않는다.

## 향후 AI 기능

- 랜덤 운영 시나리오 생성
- 사용자 답변 평가
- 아키텍처 리뷰
- 학습 경로 추천
- 면접 질문 생성
- 문서 기반 Q&A

## AI 적용 원칙

AI는 핵심 정답 판단을 대체하지 않는다.

AI는 다음 역할로 제한한다.

- 설명 보조
- 시나리오 생성 보조
- 피드백 문장 생성
- 학습 추천 보조

핵심 판단 기준은 서버의 고정 규칙으로 관리한다.

---

# 18. 구현 우선순위

## Step 1. 프로젝트 초기 세팅

- Spring Boot 프로젝트 생성
- React 프로젝트 생성
- Docker Compose PostgreSQL 구성
- 기본 CI 구성

---

## Step 2. 학습 문서 도메인 구현

- LearningDocument Entity
- 문서 목록 API
- 문서 상세 API
- Markdown 저장 방식 결정

---

## Step 3. 시나리오 도메인 구현

- Scenario Entity
- ScenarioOption Entity
- 시나리오 목록 API
- 시나리오 상세 API

---

## Step 4. 시뮬레이션 엔진 구현

- 선택지 평가 로직
- 점수 합산
- 결과 타입 판단
- 피드백 생성
- 관련 문서 연결

---

## Step 5. 프론트 기본 화면 구현

- 메인 화면
- 학습 문서 목록
- 문서 상세
- 시나리오 목록
- 시나리오 상세
- 결과 화면

---

## Step 6. 아키텍처 시각화

- Mermaid.js 또는 React Flow 적용
- 초기 구조 표시
- 변경 후 구조 표시

---

## Step 7. 초기 콘텐츠 작성

- 학습 문서 10개
- 시나리오 5개
- 선택지 / 피드백 데이터 작성

---

## Step 8. README / 포트폴리오 정리

- 프로젝트 목적
- 기능 소개
- 아키텍처
- 시뮬레이션 예시
- API 예시
- 실행 방법

---

# 19. 가장 중요한 설계 기준

```
AWS를 설명하는 서비스가 아니라,
운영 상황에서 어떤 AWS 구조를 선택해야 하는지 훈련하는 서비스
```

---

# 20. 최종 방향 요약

1. 정적 학습 문서
2. 고정 시나리오
3. 선택형 시뮬레이션
4. 결과 피드백
5. 아키텍처 시각화
6. 사용자 학습 기록
7. AI 시나리오 생성
8. AI 답변 평가

초기에는 AI 없이도 가치가 있어야 한다.

AI는 학습 경험을 강화하는 확장 기능으로 추가한다.