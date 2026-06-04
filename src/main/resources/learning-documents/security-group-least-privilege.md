# Security Group 최소 허용

## 왜 필요한가

Security Group은 ALB, EC2, RDS 같은 리소스의 인바운드와 아웃바운드 트래픽을 제어하는 상태 저장 방식의 가상 방화벽입니다. 최소 권한 원칙에 따라 실제 요청 흐름에 필요한 출발지와 포트만 허용하면 공격 표면과 설정 실수의 영향을 줄일 수 있습니다.

## 요청 흐름

```text
Client -> ALB: 443
ALB Security Group -> EC2 Security Group: 애플리케이션 포트
EC2 Security Group -> RDS Security Group: DB 포트
```

IP 주소를 넓게 허용하기보다 다른 Security Group을 출발지로 참조하면 리소스가 교체되어도 역할 기반 접근 관계를 유지할 수 있습니다.

## 운영 예시

RDS의 DB 포트를 `0.0.0.0/0`에 열면 연결 테스트는 쉬워지지만 인터넷 전체에서 접근을 시도할 수 있습니다. RDS 인바운드를 애플리케이션 EC2의 Security Group으로 제한하면 정상 서비스 흐름은 유지하면서 직접 접근 위험을 줄일 수 있습니다.

## 장애 상황

- ALB에서 EC2 애플리케이션 포트가 허용되지 않으면 Health Check와 사용자 요청이 실패합니다.
- EC2에서 RDS 포트가 허용되지 않으면 애플리케이션 시작이나 쿼리가 실패합니다.
- 필요한 아웃바운드를 제거하면 외부 API, DNS, 패키지 저장소 접근이 실패할 수 있습니다.
- 임시 점검을 위해 연 규칙을 제거하지 않으면 장기적인 보안 취약점이 됩니다.

## Trade-off

규칙을 넓게 열면 초기 운영은 편하지만 침해 가능성과 영향 범위가 커집니다. 규칙을 지나치게 세분화하면 관리 복잡도와 장애 가능성이 증가합니다. 최소 허용은 필요한 흐름을 명확히 이해하고 지속해서 검증할 때 효과적입니다.

## 실무 주의점

- 규칙마다 허용 이유와 소유 서비스를 기록합니다.
- 관리 포트를 인터넷에 직접 열지 않고 제한된 운영 경로를 사용합니다.
- 변경 후 ALB Health Check, DB 연결, 외부 연동을 실제로 검증합니다.
- 사용하지 않는 규칙과 임시 규칙을 정기적으로 제거합니다.

## 면접 질문

1. Security Group이 상태 저장 방식이라는 의미는 무엇인가요?
2. IP 대신 Security Group을 참조하는 장점은 무엇인가요?
3. 최소 권한 규칙이 운영 장애를 만들지 않도록 어떻게 검증하나요?

## 추가 학습

- [Security Group으로 AWS 리소스 트래픽 제어](https://docs.aws.amazon.com/vpc/latest/userguide/vpc-security-groups.html)
- [Security Group 규칙](https://docs.aws.amazon.com/vpc/latest/userguide/security-group-rules.html)
