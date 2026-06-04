# Auto Scaling 기본

## 왜 필요한가

Auto Scaling은 트래픽 변화에 맞춰 EC2 수를 조절해 처리량과 가용성을 유지하는 장치입니다. 트래픽 피크에 맞춰 항상 많은 인스턴스를 실행하는 대신 필요한 시점에 확장하고, 트래픽이 줄면 축소해 비용을 조절할 수 있습니다.

## 확장 흐름

```text
CloudWatch 지표 상승 -> Scaling 정책 실행 -> 새 EC2 시작
-> 애플리케이션 준비 -> ALB Health Check 통과 -> 요청 처리
```

CPU 사용률, ALB 요청 수, target response time 같은 지표를 사용할 수 있습니다. 어떤 지표를 선택하든 실제 사용자 부하와 충분히 연관되어 있어야 합니다.

## 운영 예시

이벤트 시작 후 ALB 응답 시간과 EC2 CPU가 함께 상승하고 RDS에는 여유가 있다면 EC2 계층 확장이 적합할 수 있습니다. 다만 새 인스턴스가 시작되고 애플리케이션이 준비될 때까지 시간이 걸리므로, 예측 가능한 이벤트에는 예약 확장이나 충분한 최소 용량을 검토합니다.

## 장애 상황

- 최대 인스턴스 수가 너무 낮으면 확장 정책이 실행돼도 피크를 흡수하지 못합니다.
- 시작 스크립트나 외부 의존성 장애로 새 인스턴스가 Health Check를 통과하지 못할 수 있습니다.
- DB 커넥션 풀을 인스턴스마다 크게 잡으면 EC2 확장이 RDS 연결 고갈을 유발할 수 있습니다.
- 로컬 세션이나 파일에 의존하면 요청이 다른 인스턴스로 분산될 때 기능이 깨질 수 있습니다.

## Trade-off

공격적인 확장 정책은 장애 위험을 낮추지만 비용과 빈번한 인스턴스 교체를 늘릴 수 있습니다. 보수적인 정책은 비용을 줄이지만 급격한 피크에 늦게 대응할 수 있습니다. Auto Scaling은 애플리케이션 계층 병목에 효과적이며, RDS나 외부 API 병목에는 다른 대응이 필요합니다.

## 실무 주의점

- 애플리케이션을 상태 비저장으로 만들고 시작 과정을 자동화합니다.
- 최소, 최대, 희망 용량과 scale-in 보호 기준을 함께 정의합니다.
- 배포 직후와 확장 직후의 Health Check 유예 시간을 조정합니다.
- EC2 수 증가에 따라 DB 연결, 외부 API 호출량도 증가하는지 확인합니다.

## 면접 질문

1. Auto Scaling이 트래픽 급증에 즉시 대응하지 못하는 이유는 무엇인가요?
2. CPU 사용률 외에 어떤 확장 지표를 사용할 수 있나요?
3. EC2 수평 확장이 RDS 장애를 만들 수 있는 경우는 무엇인가요?

## 추가 학습

- [Amazon EC2 Auto Scaling](https://docs.aws.amazon.com/autoscaling/ec2/userguide/what-is-amazon-ec2-auto-scaling.html)
- [Target tracking scaling 정책](https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-scaling-target-tracking.html)
