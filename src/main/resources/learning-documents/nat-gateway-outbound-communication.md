# NAT Gateway와 아웃바운드 통신

## 왜 필요한가

NAT Gateway는 Private subnet의 리소스가 인터넷으로 아웃바운드 연결을 시작할 수 있게 하는 관리형 네트워크 리소스입니다. 외부 사용자가 Private subnet의 EC2로 직접 연결하는 인바운드 경로는 만들지 않으면서 외부 API 호출, 패키지 다운로드, 보안 업데이트를 가능하게 합니다.

## 요청 흐름

```text
Private subnet EC2 -> Private 라우팅 테이블 -> Public subnet NAT Gateway
-> Internet Gateway -> 외부 서비스
```

NAT Gateway는 Public subnet에 위치하고 Elastic IP를 사용합니다. Private subnet 라우팅 테이블은 인터넷 대상 트래픽을 NAT Gateway로 보내야 합니다.

## 운영 예시

Private subnet의 Spring Boot 서버가 결제사 API를 호출해야 한다면 NAT Gateway를 통해 외부로 연결할 수 있습니다. 서버는 외부 API에 요청을 시작할 수 있지만 외부 사용자는 NAT Gateway를 통해 서버에 직접 접속할 수 없습니다.

## 가용성과 비용 판단

- NAT Gateway는 시간당 비용과 처리 데이터 비용이 발생합니다.
- 가용 영역별 NAT Gateway를 사용하면 한 영역 장애의 영향을 줄이고 영역 간 데이터 전송을 피할 수 있지만 비용이 늘어납니다.
- S3 같은 AWS 서비스 접근은 Gateway VPC Endpoint를 사용하면 NAT 의존성과 처리 비용을 줄일 수 있습니다.
- 외부 통신이 전혀 필요하지 않다면 NAT Gateway를 두지 않는 것이 더 단순하고 저렴합니다.

## 장애 상황

- Private 라우팅 테이블에 NAT 경로가 없으면 외부 API와 패키지 저장소에 연결할 수 없습니다.
- NAT Gateway가 있는 Public subnet에 인터넷 게이트웨이 경로가 없으면 아웃바운드가 실패합니다.
- 하나의 NAT Gateway를 여러 가용 영역에서 공유하면 해당 NAT 또는 가용 영역 장애가 넓게 영향을 줍니다.
- 외부 호출 타임아웃이 길면 NAT 또는 외부 서비스 장애가 애플리케이션 스레드 고갈로 이어질 수 있습니다.

## Trade-off

NAT Gateway는 관리 부담을 줄이고 Private subnet의 아웃바운드를 제공하지만 고정 비용과 데이터 처리 비용이 있습니다. 보안 경계를 유지한다는 이유만으로 모든 트래픽을 NAT에 의존하지 말고, 실제 목적에 따라 VPC Endpoint나 프록시 같은 대안을 검토해야 합니다.

## 실무 주의점

- 어떤 서비스가 어떤 외부 목적지로 통신하는지 목록화합니다.
- NAT Gateway 처리량, 오류, 포트 할당 지표와 외부 호출 실패율을 관찰합니다.
- 가용 영역별 라우팅과 장애 범위를 확인합니다.
- 외부 연동에는 연결 및 읽기 타임아웃, 제한된 재시도, 회로 차단을 적용합니다.

## 면접 질문

1. NAT Gateway가 Private subnet의 인바운드 접근을 허용하지 않는 이유는 무엇인가요?
2. 가용 영역별 NAT Gateway를 구성하는 이유는 무엇인가요?
3. NAT Gateway 비용과 의존성을 줄일 수 있는 방법은 무엇인가요?

## 추가 학습

- [NAT Gateway](https://docs.aws.amazon.com/vpc/latest/userguide/vpc-nat-gateway.html)
- [AWS PrivateLink와 VPC Endpoint](https://docs.aws.amazon.com/vpc/latest/privatelink/concepts.html)
