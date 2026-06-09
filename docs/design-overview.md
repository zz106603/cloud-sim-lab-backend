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

각 선택지의 효과 값은 `-3`부터 `3`까지다.

- 양수는 해당 관점에서 이점이 커지는 것을 뜻한다. `cost`, `complexity`의 양수는 비용이나 운영 복잡도가 줄어드는 효과다.
- 음수는 해당 관점에서 부담이나 위험이 커지는 것을 뜻한다.
- `0`은 직접적인 영향이 없거나 현재 학습 범위에서 중립임을 뜻한다.
- 여러 선택지를 고르면 각 차원 값을 단순 합산해 `tradeOffSummary`로 제공한다. 선택지 중복은 제거하므로 같은 입력은 항상 같은 요약을 반환한다.
- trade-off 효과는 비교와 설명을 위한 데이터다. `GOOD/PARTIAL/RISKY/WRONG` 판정은 기존 `score`, `riskScore`, `core` 규칙으로 결정한다.

---

# 7.1 사용자 작성 아키텍처 도메인 모델

사용자가 직접 작성하는 아키텍처는 고정 시나리오의 `ArchitectureGraph`와 분리된 도메인 모델로 다룬다.

`ArchitectureGraph`는 시나리오의 초기 구조와 선택지 결과를 화면에 보여주기 위한 서버 생성 응답 모델이다. 반면 사용자 작성 아키텍처는 사용자가 편집한 학습 구조 자체를 표현하는 모델이며, 제목, 설명, 생성/수정 시각, 리소스 노드, 연결 관계를 가진다.

## 최소 구성

- 아키텍처 기본 정보: 안정적인 아키텍처 ID, 제목, 설명, 생성 시각, 수정 시각
- 리소스 노드: 안정적인 노드 ID, 리소스 타입, 표시 이름
- 연결 관계: 안정적인 연결 ID, source 노드 ID, target 노드 ID, 연결 타입

## 의미 유지 기준

노드와 연결은 입력 순서가 아니라 안정적인 ID로 식별한다. 같은 리소스 타입의 노드가 여러 개 있어도 서로 다른 ID를 가지면 별개의 리소스로 취급한다.

도메인 모델은 노드와 연결을 ID 기준으로 보관해 같은 구성의 아키텍처가 입력 순서 때문에 다른 의미로 해석되지 않도록 한다.

# 7.2 아키텍처 빌더 리소스 카탈로그

아키텍처 빌더는 사용자가 임의 문자열로 리소스 타입과 연결 타입을 입력하게 하지 않고, 백엔드가 제공하는 작은 카탈로그를 기준으로 선택지를 구성한다.

카탈로그의 목적은 AWS 전체 서비스 목록을 모델링하는 것이 아니라 현재 CloudSimLab 학습 범위에서 반복적으로 다루는 구성 요소를 안정적인 키로 맞추는 것이다. 저장 API와 검증 기능은 같은 enum 키를 사용하므로, 빌더가 카탈로그에서 받은 `key`를 저장 요청의 `resourceType`, `connectionType`으로 그대로 사용할 수 있어야 한다.

## 리소스 타입 지원 범위

현재 지원 리소스 타입은 다음 기준으로 제한한다.

- 시나리오 초기/최종 그래프에 반복 등장하는 핵심 컴포넌트
- 학습 문서에서 독립적인 판단 대상으로 설명하는 컴포넌트
- 프론트 시각화에서 노드로 구분해야 학습 흐름이 명확해지는 컴포넌트

최소 지원 범위는 `CLIENT`, `VPC`, `SUBNET`, `EC2`, `ALB`, `TARGET_GROUP`, `AUTO_SCALING_GROUP`, `RDS`, `RDS_STANDBY`, `READ_REPLICA`, `REDIS`, `CONNECTION_POOL`, `HEALTH_CHECK`, `NAT_GATEWAY`, `INTERNET_GATEWAY`, `SECURITY_GROUP`, `EXTERNAL_SERVICE`다.

각 리소스 타입은 다음 정보를 가진다.

- 안정적인 `key`
- 학습자가 이해할 수 있는 `displayName`
- 역할을 설명하는 `description`
- 프론트 시각화 분류인 `visualizationCategory`
- 어떤 학습 판단에 쓰는지 설명하는 `learningPurpose`

## 연결 타입 지원 범위

현재 지원 연결 타입은 요청 흐름, 네트워크 라우팅, 의존 관계, 복제 관계, 보안 허용 관계로 제한한다.

최소 지원 범위는 `REQUEST_FLOW`, `NETWORK_ROUTE`, `DEPENDS_ON`, `REPLICATION`, `SECURITY_RULE`다.

각 연결 타입은 안정적인 `key`, 표시 이름, 의미 설명을 가진다.

## 신규 타입 추가 기준

새 리소스 타입이나 연결 타입은 다음 조건을 만족할 때 추가한다.

- 기존 타입의 표시 이름 변경만으로 표현할 수 없는 별도 학습 의미가 있다.
- 최소 하나 이상의 시나리오, 학습 문서, 검증 규칙에서 실제로 사용된다.
- 프론트 시각화에서 별도 분류나 노드/엣지 의미가 필요하다.
- 카탈로그 무결성 테스트에 중복 키와 필수 설명 누락이 잡히도록 메타데이터를 함께 추가한다.

단순한 인스턴스 사양, 가격, 리전별 서비스 차이, AWS 서비스 전체 목록 확장은 현재 MVP 범위에서 제외한다.

# 7.3 사용자 작성 아키텍처 검증

사용자 작성 아키텍처 검증은 런타임 입력에 대한 결정적 학습 피드백이다. 시나리오 결과의 `GOOD/PARTIAL/RISKY/WRONG` 판정을 재사용하지 않고, 저장 전 또는 저장된 사용자 아키텍처를 대상으로 구조 오류와 최소 운영 위험을 설명한다.

검증 결과는 다음 세 단계로 구분한다.

- `ERROR`: 저장 또는 시각화 의미를 신뢰하기 어려운 구조 오류
- `WARNING`: 현재 학습 범위에서 명백히 주의해야 하는 운영 위험
- `GUIDANCE`: 정답 판정은 아니지만 다음 학습 단계에서 검토할 설계 힌트

각 결과는 안정적인 코드, 대상 유형, 대상 ID, 사용자에게 보여줄 메시지, 판단 이유를 가진다. 대상은 아키텍처 전체, 노드, 연결 중 하나다.

## 구조 오류 검증

초기 구조 오류는 다음을 확인한다.

- 노드 ID와 연결 ID 누락
- 노드 ID와 연결 ID 중복
- 지원하지 않는 리소스 타입과 연결 타입
- 존재하지 않는 source/target 노드 참조
- self-loop 연결
- 같은 source, target, 연결 타입을 가진 중복 연결

카탈로그 기준으로 명백히 의미가 맞지 않는 연결도 오류로 다룬다. 예를 들어 `REPLICATION`은 데이터 저장소 사이에서만 사용하고, `SECURITY_RULE`은 Security Group을 포함해야 하며, `NETWORK_ROUTE`은 VPC, subnet, gateway, 외부 서비스 같은 네트워크 경계 리소스 사이에서 사용한다.

## 최소 운영 위험 검증

구조 오류가 없을 때 다음 위험을 경고로 제공한다.

- 외부 `CLIENT`가 `RDS`에 직접 `REQUEST_FLOW`로 연결됨
- `Private subnet`으로 표시된 subnet에 필요한 NAT Gateway 아웃바운드 경로가 보이지 않음
- Private subnet 안의 애플리케이션에 ALB 진입 경로가 보이지 않음
- ALB 뒤에 EC2, Auto Scaling Group, Target Group 같은 요청 처리 대상이 없음
- 애플리케이션 계층에서 데이터 저장소로 이어지는 경로가 없음

추가로 애플리케이션이나 데이터 저장소가 있는데 Security Group 경계가 표현되지 않은 경우 학습 안내를 제공한다.

## 검증 한계

이 검증은 전체 AWS Well-Architected Framework 평가, 실제 보안 취약점 스캔, 비용/성능 예측, 자동 수정 기능이 아니다. 현재 카탈로그와 학습 시나리오 안에서 설명 가능한 최소 규칙만 다루며, 같은 입력은 항상 같은 결과를 반환해야 한다.

# 7.4 사용자 작성 아키텍처 비교

사용자 작성 아키텍처 비교는 두 구조 사이의 추가, 제거, 변경, 유지 항목을 결정적으로 반환한다. 이 기능은 어느 아키텍처가 더 좋은지 자동 판정하지 않고, 구조 차이를 학습자가 해석할 수 있는 형태로 제공한다.

## 사용자 아키텍처 간 비교

두 저장된 사용자 아키텍처는 안정적인 ID를 기준으로 비교한다.

- 같은 노드 ID가 양쪽에 있고 리소스 타입과 표시 이름이 같으면 `UNCHANGED`
- 같은 노드 ID가 양쪽에 있지만 리소스 타입 또는 표시 이름이 다르면 `CHANGED`
- 기준 아키텍처에만 있으면 `REMOVED`
- 비교 대상 아키텍처에만 있으면 `ADDED`

연결도 같은 기준을 따른다. 연결 ID가 같고 source, target, 연결 타입이 같으면 유지로 보고, 하나라도 다르면 변경으로 본다. 입력 배열 순서는 비교 결과에 영향을 주지 않는다.

요청 예시는 다음과 같다.

```http
POST /api/user-architectures/compare
Content-Type: application/json

{
  "baseArchitectureId": "base-architecture-id",
  "targetArchitectureId": "target-architecture-id"
}
```

## 시나리오 권장 구조와 비교

사용자 아키텍처와 시나리오 권장 구조 비교는 다음 API로 조회한다.

```http
GET /api/user-architectures/{architectureId}/comparison/scenarios/{scenarioId}
```

시나리오 권장 구조는 해당 시나리오의 핵심 선택지(`core=true`)를 적용한 최종 그래프를 기준으로 만든다. 핵심 선택지가 없으면 양수 점수 선택지를 권장 후보로 사용한다.

시나리오 권장 구조와 비교할 때는 사용자 노드 ID가 아니라 리소스 타입과 표시 이름을 정규화한 컴포넌트 signature를 기준으로 비교한다. 이 방식은 서버가 만든 시나리오 그래프의 node id와 사용자가 직접 만든 node id가 달라도 같은 컴포넌트를 같은 의미로 비교하기 위한 것이다.

결과에는 다음 항목이 포함된다.

- 기준 아키텍처와 비교 대상 아키텍처 요약
- 리소스 추가, 제거, 변경, 유지 목록
- 연결 추가, 제거, 변경, 유지 목록
- 시나리오 권장 구조 대비 누락된 컴포넌트
- 시나리오 권장 구조에는 없는 추가 컴포넌트
- 시나리오 학습 목표에 영향을 줄 수 있는 차이
- 권장 선택지에 정의된 trade-off 정보

응답 예시는 다음과 같다.

```json
{
  "base": {
    "comparisonType": "SCENARIO_RECOMMENDATION",
    "id": "1",
    "title": "단일 Spring Boot 배포",
    "resourceCount": 5,
    "connectionCount": 5
  },
  "target": {
    "comparisonType": "USER_ARCHITECTURE",
    "id": "user-architecture-id",
    "title": "사용자 구조",
    "resourceCount": 3,
    "connectionCount": 2
  },
  "scenarioComparison": {
    "scenarioId": 1,
    "scenarioTitle": "단일 Spring Boot 배포",
    "learningGoal": "단일 장애 지점을 줄입니다.",
    "missingRecommendedResources": [],
    "extraResources": [],
    "learningImpacts": []
  },
  "tradeOffReferences": [
    {
      "optionName": "ALB와 Auto Scaling 추가",
      "reason": "시나리오 권장 구조를 만드는 핵심 선택지의 trade-off입니다.",
      "effects": {
        "performance": 3,
        "availability": 3,
        "cost": -2,
        "complexity": -2,
        "consistency": 0,
        "security": 1
      }
    }
  ]
}
```

비교 기능은 그래프 레이아웃 위치, 비용/성능 점수, 아키텍처 우열, AI 요약을 다루지 않는다.

## 기본 방어 규칙

- 아키텍처 ID와 제목은 비어 있을 수 없다.
- 생성/수정 시각은 필수이며, 수정 시각은 생성 시각보다 이전일 수 없다.
- 노드 ID, 리소스 타입, 표시 이름은 필수다.
- 연결 ID, source, target, 연결 타입은 필수다.
- 노드 ID와 연결 ID는 각각 중복될 수 없다.
- 연결의 source와 target은 같은 아키텍처 안의 기존 노드 ID를 참조해야 한다.

DB 저장, CRUD API, 사용자 소유권, 리소스 연결 가능 여부 검증, 점수화, 범용 AWS 리소스 속성 모델은 이 모델의 초기 범위에 포함하지 않는다.

---

# 8. 콘텐츠 무결성 검증 규칙

시나리오와 학습 문서 seed를 확장할 때는 테스트에서 다음 규칙을 검증한다.

- 시나리오 `graphKey`는 비어 있지 않고 소문자여야 하며 앞뒤 공백이 없어야 하고 전체 seed 안에서 중복될 수 없다.
- 그래프 변화를 선언하는 선택지 `graphKey`는 비어 있지 않고 소문자여야 하며 앞뒤 공백이 없어야 하고 같은 시나리오 안에서 중복될 수 없다.
- 선택지 `graphKey`가 있으면 `scenarioGraphKey::optionGraphKey` 그래프 매핑이 존재해야 하고, 적용 결과가 초기 그래프와 달라야 한다.
- 생성된 초기/최종 아키텍처 그래프의 모든 edge `source`, `target`은 존재하는 node id를 참조해야 한다.
- 장애 영향 흐름이 있는 시나리오의 `failureSourceNodeId`, `affectedNodeIds`, `affectedEdges`, `recoveredEdges`는 초기 또는 최종 아키텍처 그래프의 유효한 node/edge를 참조해야 한다.
- 명시적 문서-시나리오 연결은 존재하는 시나리오 `graphKey`와 문서 key만 참조해야 하며, 연결 이유와 검토 포인트가 비어 있으면 안 된다.
- 선택지 trade-off 효과는 `performance`, `availability`, `cost`, `complexity`, `consistency`, `security` 값을 모두 가져야 하며 각 값은 `-3`부터 `3` 사이다.
- 시나리오는 제목, 학습 목표 역할의 요약, 설명, 초기 아키텍처, 선택지를 가져야 한다.
- 각 시나리오는 최소 하나 이상의 핵심 선택지(`core=true`) 또는 양수 점수 선택지를 가져야 사용자가 판단 가능한 결과를 받을 수 있다.

---

# 9. 장애 영향 흐름 응답 구조

장애 시나리오는 초기 그래프의 어떤 node에서 장애가 시작되고 어떤 요청 경로가 영향을 받는지 `initialFailureImpact`로 제공한다. 정상 구조 개선 시나리오는 이 데이터를 갖지 않는다.

```json
{
  "initialFailureImpact": {
    "failureSourceNodeId": "target-group",
    "affectedNodeIds": ["target-group", "alb", "ec2"],
    "affectedEdges": [
      { "source": "alb", "target": "target-group", "label": "연결" },
      { "source": "target-group", "target": "ec2", "label": "연결" }
    ],
    "userSymptoms": [
      "정상 EC2가 target에서 제외되어 사용자 요청이 503으로 실패합니다."
    ],
    "remainingRisks": [
      "Health Check가 외부 의존성을 과도하게 검사하면 배포 중 정상 인스턴스도 제외될 수 있습니다."
    ]
  }
}
```

선택지 적용 후에는 `failureImpactResult`로 복구된 경로, 남은 영향, 대응 후 주의점을 제공한다.

```json
{
  "failureImpactResult": {
    "recoveredEdges": [
      { "source": "alb", "target": "health-check", "label": "readiness 확인" },
      { "source": "health-check", "target": "ec2", "label": "정상 target 판정" }
    ],
    "remainingImpact": {
      "failureSourceNodeId": "target-group",
      "affectedNodeIds": [],
      "affectedEdges": [],
      "userSymptoms": [],
      "remainingRisks": [
        "배포 직후 준비 시간과 Health Check 유예 시간은 계속 맞춰야 합니다."
      ]
    },
    "postActionNotes": [
      "가벼운 readiness 경로와 ALB-EC2 포트 허용으로 정상 target 판정 경로를 복구합니다."
    ]
  }
}
```

이 모델은 실제 장애 전파 엔진이 아니라 콘텐츠에 정의된 결정적 학습 결과다. 같은 시나리오와 같은 선택지는 항상 같은 장애 영향 결과를 반환한다.

---

# 10. 시나리오 데이터 구조

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
        "performance": 3,
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
| initialArchitectureGraph | 초기 아키텍처 node/edge 구조 |
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
| selectedOptions.effects | 선택한 선택지별 6개 trade-off 효과 |
| tradeOffSummary | 선택한 효과를 차원별로 합산한 요약 |
| summary | 결과 요약 |
| detailFeedback | 상세 피드백 |
| finalArchitecture | 변경 후 아키텍처 |
| finalArchitectureGraph | 변경 후 아키텍처 node/edge 구조 |
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
