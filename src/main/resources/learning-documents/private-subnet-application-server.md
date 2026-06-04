# Private subnet과 애플리케이션 서버

## 왜 필요한가

Private subnet은 인터넷 게이트웨이로 직접 나가는 경로가 없는 서브넷입니다. 외부 사용자가 직접 접근할 필요가 없는 애플리케이션 서버와 데이터베이스를 Private subnet에 배치하면 잘못된 보안 설정이 즉시 인터넷 노출로 이어질 위험을 줄일 수 있습니다.

## 요청 흐름

```text
Client -> Internet Gateway -> Public subnet의 ALB
       -> Private subnet의 EC2 -> Private subnet의 RDS
```

Client의 진입점은 ALB로 제한하고, EC2는 ALB의 Security Group에서 오는 애플리케이션 요청만 허용합니다. RDS는 EC2의 Security Group에서 오는 DB 연결만 허용합니다.

## 운영 예시

Spring Boot 서버가 Public subnet에서 공인 IP를 가지고 있으면 Security Group 실수로 애플리케이션 포트나 관리 포트가 외부에 노출될 수 있습니다. 서버를 Private subnet으로 이동하고 ALB를 진입점으로 사용하면 직접 노출을 줄이면서 사용자 요청은 계속 받을 수 있습니다.

## 라우팅 판단

- Public subnet의 라우팅 테이블은 인터넷 게이트웨이 경로를 가질 수 있습니다.
- Private subnet의 인터넷 아웃바운드가 필요하면 NAT Gateway 경로를 별도로 구성합니다.
- AWS 서비스 호출만 필요하다면 비용과 보안 요구에 따라 VPC Endpoint도 검토합니다.

## 장애 상황

- ALB에서 EC2로 가는 Security Group 규칙이 없으면 모든 대상이 비정상으로 판단될 수 있습니다.
- Private subnet 라우팅에 NAT 경로가 없으면 외부 API 호출, 패키지 다운로드, 배포가 실패합니다.
- 서로 다른 가용 영역을 사용하지 않으면 한 가용 영역 장애가 전체 애플리케이션 중단으로 이어질 수 있습니다.

## Trade-off

Private subnet은 보안 경계를 강화하지만 ALB, NAT Gateway, 라우팅 테이블 관리가 추가됩니다. 단순히 서버를 Private subnet으로 이동하는 것만으로 보안이 완성되지는 않으며, 인바운드와 아웃바운드 흐름을 모두 설계해야 합니다.

## 실무 주의점

- 서브넷이 public인지 여부는 이름이 아니라 라우팅 테이블로 판단합니다.
- 운영 접근은 SSH 포트를 인터넷에 열기보다 Systems Manager 같은 관리 경로를 검토합니다.
- ALB, EC2, RDS의 Security Group 참조 관계를 요청 흐름과 함께 문서화합니다.
- 가용성을 위해 여러 가용 영역에 서브넷과 애플리케이션 서버를 배치합니다.

## 면접 질문

1. Public subnet과 Private subnet을 구분하는 기준은 무엇인가요?
2. Private subnet의 EC2는 어떻게 인터넷의 외부 API를 호출할 수 있나요?
3. ALB는 Public subnet에 두고 EC2는 Private subnet에 두는 이유는 무엇인가요?

## 추가 학습

- [VPC와 서브넷](https://docs.aws.amazon.com/vpc/latest/userguide/configure-subnets.html)
- [라우팅 테이블](https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html)
