# ALB와 트래픽 분산

## 왜 필요한가

Application Load Balancer(ALB)는 HTTP/HTTPS 요청을 받아 대상 그룹의 정상 타깃으로 전달합니다. 여러 EC2에 요청을 분산하고 비정상 서버를 우회하므로 단일 EC2 직접 연결보다 배포와 장애 대응에 유리합니다.

## 요청 흐름

```text
Client -> ALB Listener -> Listener Rule -> Target Group -> 정상 EC2
```

Listener는 요청을 받고 규칙에 따라 대상 그룹을 선택합니다. 대상 그룹은 Health Check에 통과한 EC2에만 요청을 전달합니다. 경로 또는 호스트 기반 규칙을 사용하면 하나의 ALB가 여러 서비스로 요청을 분기할 수 있습니다.

## 운영 예시

EC2 두 대 중 한 대의 Spring Boot 프로세스가 종료되면 Health Check가 실패합니다. ALB는 실패 임계값에 도달한 타깃을 요청 대상에서 제외하고 정상 EC2로 트래픽을 보냅니다. 새 버전을 배포할 때도 새 인스턴스가 정상 상태가 된 뒤 트래픽을 받게 할 수 있습니다.

## Health Check 설계

- 단순 프로세스 생존 여부와 실제 요청 처리 가능 여부를 구분합니다.
- Health Check 경로가 느리거나 외부 의존성을 과도하게 검사하면 정상 서버도 제외될 수 있습니다.
- 반대로 항상 `200 OK`만 반환하면 장애 서버가 계속 요청을 받을 수 있습니다.
- ALB 5xx, Target 5xx, target response time, unhealthy host 수를 함께 관찰합니다.

## 장애 상황

- 모든 타깃이 비정상이면 ALB가 요청을 전달할 정상 서버가 없습니다.
- Security Group이 ALB에서 EC2로 가는 포트를 막으면 Health Check가 실패합니다.
- 애플리케이션 시작 시간이 긴데 Health Check가 너무 빨리 시작되면 배포 중 타깃이 반복 교체될 수 있습니다.

## Trade-off

ALB는 가용성, 분산, TLS 종료, 배포 유연성을 높이지만 고정 비용과 설정 복잡도를 추가합니다. ALB를 추가해도 EC2가 한 대뿐이면 단일 장애 지점은 그대로이며, 애플리케이션 병목이나 DB 병목을 직접 해결하지는 않습니다.

## 실무 주의점

- ALB와 대상 그룹의 타임아웃을 애플리케이션 타임아웃과 함께 설계합니다.
- 무중단 배포를 위해 deregistration delay와 종료 중 요청 처리를 확인합니다.
- EC2 인바운드는 인터넷 전체가 아니라 ALB Security Group으로 제한합니다.

## 면접 질문

1. ALB Health Check가 잘못 설계되면 어떤 장애가 발생할 수 있나요?
2. ALB를 추가하는 것만으로 단일 EC2 장애 문제가 해결되나요?
3. ALB 5xx와 Target 5xx의 차이는 무엇인가요?

## 추가 학습

- [Application Load Balancer 소개](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/introduction.html)
- [ALB 대상 그룹 Health Check](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/target-group-health-checks.html)
