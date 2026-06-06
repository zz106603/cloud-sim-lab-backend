# Cloud Sim Lab Backend

Cloud Sim Lab은 백엔드 운영 상황을 시나리오로 학습하는 MVP 프로젝트입니다. 사용자는 학습 문서를 읽고, 운영 문제 상황에서 아키텍처 선택지를 고른 뒤, 규칙 기반 시뮬레이션 결과와 아키텍처 변화를 확인합니다.

이 프로젝트는 AI 추천이 아니라 결정적인 rule-based 평가로 동작합니다. 같은 입력은 항상 같은 결과를 반환하므로, 학습자가 선택지의 trade-off를 반복해서 비교하기 쉽습니다.

## 프로젝트 소개

Cloud Sim Lab은 다음 질문을 다룹니다.

- 단일 Spring Boot 배포는 왜 장애에 취약한가?
- EC2, RDS, Redis, ALB, Auto Scaling 같은 컴포넌트는 어떤 운영 문제를 해결하는가?
- 성능, 가용성, 비용, 복잡도, 보안, 일관성은 어떤 trade-off를 만드는가?
- 아키텍처 선택 결과를 텍스트만이 아니라 다이어그램으로 보면 무엇이 더 명확해지는가?

## 핵심 목표

- 백엔드 운영 상황을 짧은 시나리오로 학습한다.
- 선택지별 trade-off를 명확히 보여준다.
- 시뮬레이션 결과를 deterministic하게 평가한다.
- `initialArchitecture`/`finalArchitecture`와 graph 응답을 제공해 아키텍처 시각화를 가능하게 한다.
- MVP 범위 안에서 콘텐츠와 API 흐름을 단순하게 유지한다.

## 주요 기능

- 학습 문서 목록/상세 조회
- 시나리오 목록/상세 조회
- 선택지 기반 시뮬레이션 실행
- 결과 유형 반환: `GOOD`, `PARTIAL`, `RISKY`, `WRONG`
- 관련 학습 문서 추천
- 초기/최종 아키텍처 컴포넌트 배열과 최소 graph 응답 반환
- local profile 기준 초기 seed 데이터 제공

## 기술 스택

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- H2 Test Database
- Gradle
- Docker Compose

프론트엔드는 별도 Vite/React 앱에서 API를 호출하고 Mermaid로 아키텍처 배열을 렌더링하는 구조입니다.

## 실행 방법

### 1. PostgreSQL 실행

```bash
docker compose up -d
```

### 2. 백엔드 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

기본 API 주소:

```text
http://localhost:8080/api
```

local profile에서는 DB가 비어 있을 때 학습 문서와 시나리오 seed가 저장됩니다.

### 로컬 DB 초기화

seed 데이터가 변경된 경우 기존 PostgreSQL 볼륨에는 변경 내용이 자동 반영되지 않습니다. 로컬 데이터를 모두 삭제하고 최신 seed를 다시 저장하려면 다음 명령을 실행한 뒤 백엔드를 재시작합니다.

```bash
docker compose down -v
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

`docker compose down -v`는 로컬 PostgreSQL 데이터를 모두 삭제합니다.

### 학습 콘텐츠 저장 구조

학습 문서 본문은 `src/main/resources/learning-documents/*.md` Markdown 파일로 관리합니다. `LearningDocumentSeed`는 제목, 카테고리, 난이도, 요약, 본문 파일명만 가지고 있고, local profile seed 실행 시 resource 파일을 읽어 기존 `content` 필드에 저장합니다.

본문 파일을 추가하거나 이름을 바꿀 때는 seed 메타데이터의 파일명도 함께 수정해야 합니다. resource 파일을 읽지 못하면 seed 단계에서 진단 가능한 예외가 발생합니다.

### 3. 테스트

```bash
./gradlew test
```

## API 예시

### 학습 문서 목록

```bash
curl http://localhost:8080/api/docs
```

응답 예시:

```json
[
  {
    "id": 1,
    "title": "EC2와 컴퓨팅 용량",
    "category": "COMPUTE",
    "level": "BEGINNER",
    "summary": "Spring Boot 같은 애플리케이션을 EC2에서 실행할 때 용량 선택이 왜 중요한지 이해합니다."
  }
]
```

### 학습 문서 상세

```bash
curl http://localhost:8080/api/docs/1
```

응답에는 `content`와 명시적으로 연결된 `relatedScenarios`가 포함됩니다. 관련 시나리오는 category 일치가 아니라 안정적인 문서/시나리오 키 관계로 결정되며, 각 연결은 학습 이유를 함께 반환합니다.

### 문서와 시나리오 연결 기준

학습 문서는 resource 파일명과 대응하는 안정적인 `documentKey`를 사용하고, 시나리오는 기존 `graphKey`를 사용합니다. 두 키의 명시적 관계에는 시나리오 실행 전 확인할 학습 이유와 결과 후 복습할 초점이 정의됩니다.

- 문서 상세, 시나리오 상세, 시뮬레이션 결과는 같은 관계 데이터를 사용합니다.
- 같은 category여도 명시적 관계가 없으면 관련 항목으로 반환하지 않습니다.
- 관계에 정의된 문서나 시나리오가 없으면 해당 연결만 제외하고 안전하게 응답합니다.

### 시나리오 목록

```bash
curl http://localhost:8080/api/scenarios
```

필터링:

```bash
curl 'http://localhost:8080/api/scenarios?category=STORAGE&level=INTERMEDIATE'
```

### 시나리오 상세

```bash
curl http://localhost:8080/api/scenarios/1
```

응답 예시:

```json
{
  "id": 1,
  "title": "단일 Spring Boot 배포",
  "category": "COMPUTE",
  "level": "BEGINNER",
  "summary": "단일 EC2에서 실행 중인 Spring Boot 서비스를 안정적으로 운영합니다.",
  "description": "현재 Spring Boot API가 EC2 한 대에서 실행되고 있습니다. 배포나 장애 때 전체 서비스가 멈출 수 있어 기본 운영 구조를 개선해야 합니다.",
  "problem": "현재 Spring Boot API가 EC2 한 대에서 실행되고 있습니다. 배포나 장애 때 전체 서비스가 멈출 수 있어 기본 운영 구조를 개선해야 합니다.",
  "initialArchitecture": ["Client", "EC2", "RDS"],
  "initialArchitectureGraph": {
    "nodes": [
      { "id": "client", "label": "Client", "type": "CLIENT", "description": "사용자 요청이 시작되는 외부 클라이언트입니다." },
      { "id": "ec2", "label": "EC2", "type": "EC2", "description": "애플리케이션 요청을 처리하는 컴퓨팅 리소스입니다." },
      { "id": "rds", "label": "RDS", "type": "RDS", "description": "애플리케이션의 주요 영속 데이터를 저장하는 데이터베이스입니다." }
    ],
    "edges": [
      { "source": "client", "target": "ec2", "label": "요청" },
      { "source": "ec2", "target": "rds", "label": "DB 접근" }
    ]
  },
  "options": [
    {
      "id": 1,
      "name": "ALB와 Auto Scaling 추가",
      "description": "비용과 설정 복잡도는 늘지만 트래픽 분산과 장애 우회가 가능해집니다."
    }
  ]
}
```

## 시뮬레이션 예시

```bash
curl -X POST http://localhost:8080/api/scenarios/1/simulate \
  -H 'Content-Type: application/json' \
  -d '{"selectedOptionIds":[2]}'
```

응답 예시:

```json
{
  "scenarioId": 1,
  "resultType": "GOOD",
  "score": 3,
  "riskScore": 0,
  "summary": "선택한 구성이 시나리오 요구를 잘 해결합니다.",
  "detail": "시나리오 목표인 '단일 EC2 장애 지점과 배포 중단 위험을 줄이는 기본 운영 구조를 선택합니다.'에 맞는 핵심 선택지가 포함되어 현재 문제의 주요 원인을 직접 줄입니다. 선택지별 판단: ALB와 Auto Scaling 추가는 비용과 설정 복잡도는 늘지만 정상 인스턴스로 요청을 분산하고 장애 인스턴스를 우회할 수 있습니다. 핵심 선택지는 성능, 가용성, 보안 중 현재 시나리오의 주요 목표를 직접 개선합니다. 다음으로 비용, 확장 지연, 장애 시 우회 흐름을 함께 확인하세요.",
  "selectedOptions": [
    {
      "id": 2,
      "name": "ALB와 Auto Scaling 추가",
      "description": "비용과 설정 복잡도는 늘지만 트래픽 분산과 장애 우회가 가능해집니다.",
      "effects": {
        "performance": 3,
        "availability": 3,
        "cost": -2,
        "complexity": -2,
        "consistency": 0,
        "security": 1
      }
    }
  ],
  "tradeOffSummary": {
    "performance": 3,
    "availability": 3,
    "cost": -2,
    "complexity": -2,
    "consistency": 0,
    "security": 1
  },
  "finalArchitecture": ["Client", "EC2", "RDS", "ALB", "Auto Scaling"],
  "finalArchitectureGraph": {
    "nodes": [
      { "id": "client", "label": "Client", "type": "CLIENT", "description": "사용자 요청이 시작되는 외부 클라이언트입니다." },
      { "id": "alb", "label": "ALB", "type": "ALB", "description": "요청을 정상 target으로 분산하고 장애 인스턴스를 우회하는 진입점입니다." },
      { "id": "auto-scaling", "label": "Auto Scaling", "type": "AUTO_SCALING", "description": "부하나 장애 상황에 맞춰 애플리케이션 인스턴스 수를 조정합니다." }
    ],
    "edges": [
      { "source": "client", "target": "alb", "label": "HTTP 요청" },
      { "source": "alb", "target": "auto-scaling", "label": "정상 target 분산" }
    ]
  },
  "relatedLearningDocuments": [
    {
      "id": 1,
      "title": "EC2와 컴퓨팅 용량",
      "category": "COMPUTE",
      "level": "BEGINNER",
      "summary": "Spring Boot 같은 애플리케이션을 EC2에서 실행할 때 용량 선택이 왜 중요한지 이해합니다."
    }
  ]
}
```

현재 MVP 평가는 단순한 deterministic 규칙으로 동작합니다.

- `score`는 선택지가 현재 시나리오 해결에 기여하는 정도입니다. `0`이면 일반적으로 유효한 기술이어도 현재 원인을 직접 줄이지 못한다는 뜻입니다.
- `riskScore`는 선택으로 생기는 운영 위험이나 부작용의 정도입니다. 합산 위험 점수가 높으면 `RISKY`가 `GOOD`보다 우선됩니다.
- `core=true`는 시나리오의 핵심 병목이나 장애 원인을 직접 해결하는 선택지라는 뜻입니다.
- 핵심 선택지가 있는 시나리오에서는 핵심 선택지 중 하나를 고르면 `GOOD` 후보가 되고, 유효하지만 핵심이 아니면 `PARTIAL`로 판단합니다.
- 핵심 선택지가 없는 시나리오는 유효한 선택지만으로 `GOOD`이 될 수 있습니다.
- `effects`는 `performance`, `availability`, `cost`, `complexity`, `consistency`, `security` 관점의 고정 효과입니다. 값은 `-3`부터 `3`이며 양수는 이점, 음수는 부담을 뜻합니다. `cost`와 `complexity`의 양수는 비용과 복잡도가 줄어드는 효과입니다.
- `tradeOffSummary`는 중복을 제거한 선택지들의 효과를 차원별로 단순 합산합니다. 이 요약은 비교와 설명에만 사용하며 기존 결과 타입 판정에는 영향을 주지 않습니다.

### 장애 영향 흐름 응답

RDS 장애, Redis 장애, ALB Health Check 실패처럼 요청 경로의 어느 지점이 실패하는지 학습해야 하는 시나리오는 상세 응답에 `initialFailureImpact`를 포함합니다. 정상 구조 개선 시나리오는 이 값이 `null`입니다.

```json
{
  "initialFailureImpact": {
    "failureSourceNodeId": "redis",
    "affectedNodeIds": ["redis", "rds", "ec2"],
    "affectedEdges": [
      { "source": "ec2", "target": "redis", "label": "연결" },
      { "source": "redis", "target": "rds", "label": "DB 접근" }
    ],
    "userSymptoms": [
      "캐시 조회 실패가 증가하고 조회 요청이 RDS로 한꺼번에 우회됩니다."
    ],
    "remainingRisks": [
      "fallback 제한이 없으면 Redis 장애가 RDS 포화로 확산될 수 있습니다."
    ]
  }
}
```

시뮬레이션 결과의 `failureImpactResult`는 선택한 대응으로 복구된 경로와 아직 남은 장애 영향 또는 주의점을 구분합니다.

```json
{
  "failureImpactResult": {
    "recoveredEdges": [
      { "source": "ec2", "target": "rds-fallback-guard", "label": "제한된 fallback" },
      { "source": "rds-fallback-guard", "target": "rds", "label": "보호된 조회" }
    ],
    "remainingImpact": {
      "failureSourceNodeId": "redis",
      "affectedNodeIds": [],
      "affectedEdges": [],
      "userSymptoms": [],
      "remainingRisks": [
        "Redis 자체 복구 전까지 캐시 hit율 저하와 일부 응답 지연은 남습니다."
      ]
    },
    "postActionNotes": [
      "짧은 timeout, 제한된 fallback, TTL 분산으로 RDS 포화를 막는 경로를 복구합니다."
    ]
  }
}
```

`failureSourceNodeId`, `affectedNodeIds`, `affectedEdges`, `recoveredEdges`는 같은 응답의 `initialArchitectureGraph` 또는 `finalArchitectureGraph`에 있는 node id와 edge source/target을 참조합니다. 프론트는 이 값을 사용해 장애 시작점, 영향 경로, 복구 경로를 시각적으로 강조할 수 있습니다.

## 아키텍처 시각화 예시

백엔드는 기존 호환성을 위한 컴포넌트 배열과 React Flow 같은 시각화에 사용할 수 있는 최소 graph 응답을 함께 반환합니다.

```json
{
  "initialArchitecture": ["Client", "ALB", "EC2", "RDS"],
  "initialArchitectureGraph": {
    "nodes": [
      { "id": "client", "label": "Client", "type": "CLIENT", "description": "사용자 요청이 시작되는 외부 클라이언트입니다." }
    ],
    "edges": []
  },
  "finalArchitecture": ["Client", "ALB", "EC2", "RDS", "Redis"],
  "finalArchitectureGraph": {
    "nodes": [
      { "id": "redis", "label": "Redis", "type": "REDIS", "description": "반복 조회를 빠르게 처리하고 DB 부하를 줄이는 캐시 계층입니다." }
    ],
    "edges": [
      { "source": "ec2", "target": "redis", "label": "캐시 조회" }
    ]
  }
}
```

프론트엔드는 전환 중에는 기존 배열을 Mermaid `flowchart LR` 형태로 사용할 수 있고, graph 응답이 있으면 node/edge 기반으로 렌더링할 수 있습니다. graph 응답은 범용 네트워크 모델이 아니라 학습용 시각화를 위한 최소 구조입니다.

스크린샷이나 GIF가 준비되면 아래 위치에 추가하는 것을 권장합니다.

```text
docs/images/scenario-detail.png
docs/images/simulation-result.gif
```

예시 마크다운:

```markdown
![시나리오 상세 화면](docs/images/scenario-detail.png)
![시뮬레이션 결과](docs/images/simulation-result.gif)
```

## 프로젝트 구조

```text
src/main/java/com/yunhwan/cloudsimlab
├── common
│   ├── config        # CORS 등 공통 설정
│   └── error         # 공통 예외 응답
├── learningdocument
│   ├── adapter       # Web / Persistence adapter
│   ├── application   # Use case service
│   └── domain        # LearningDocument domain
└── scenario
    ├── adapter       # Web / Persistence adapter
    ├── application   # Scenario 조회/시뮬레이션 service
    └── domain        # Scenario, Option, SimulationResult
```

## MVP 범위

구현된 범위:

- 학습 문서 10개 seed
- 시나리오 5개 seed
- 시나리오별 초기 아키텍처 배열
- 선택지 기반 결과 평가
- 최종 아키텍처 배열 생성
- 관련 학습 문서 추천
- 로컬 개발용 CORS 설정

의도적으로 제외한 범위:

- AI 기반 추천
- 사용자 진도 저장
- 고급 rule engine
- 대안 그룹/필수 그룹 모델링
- 그래프 형태의 노드/엣지 아키텍처 모델
- 클라우드 리소스 실배포

## 학습 포인트

- 단일 EC2 배포의 한계
- ALB와 Auto Scaling의 역할
- Private subnet과 NAT Gateway의 운영상 의미
- RDS 장애 대응과 Multi-AZ
- Read Replica와 Redis Cache의 차이
- 보안, 비용, 성능, 일관성 사이의 trade-off
- API 응답을 프론트엔드 시각화 데이터로 연결하는 방식

## 포트폴리오 관점 설명

이 프로젝트는 클라우드 아키텍처를 “정답 암기”가 아니라 운영 상황에서의 선택 문제로 다룹니다. 예를 들어 조회 부하 문제에서 Redis Cache와 Read Replica는 모두 유효할 수 있지만, 각각 일관성, 비용, 구현 복잡도에서 다른 trade-off를 가집니다.

시뮬레이션은 AI가 아니라 명시적인 점수와 핵심 선택지 기준으로 평가합니다. 이는 MVP에서 결과를 예측 가능하게 만들고, 학습자가 선택과 결과의 관계를 쉽게 추적하게 하기 위한 결정입니다.

아키텍처 시각화는 선택 결과를 설명하기 위한 보조 수단입니다. 복잡한 graph modeling 대신 `initialArchitecture`와 `finalArchitecture` 배열을 제공해, 프론트엔드에서 Mermaid로 빠르게 렌더링할 수 있게 했습니다.

## 향후 확장 방향

- 프론트엔드/백엔드 응답 DTO 계약 정리
- 시나리오별 관련 문서 ID 명시화
- 아키텍처 노드/엣지 모델 도입 검토
- 사용자별 풀이 이력 저장
