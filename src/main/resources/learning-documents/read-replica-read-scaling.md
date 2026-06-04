# Read Replica와 읽기 확장

## 왜 필요한가

Read Replica는 원본 RDS의 데이터를 비동기적으로 복제해 읽기 요청을 분산하는 읽기 전용 복제본입니다. 조회 비율이 높아 원본 DB의 CPU와 쿼리 시간이 상승할 때 읽기 처리량을 늘릴 수 있습니다.

## 읽기/쓰기 흐름

```text
쓰기 요청 -> 애플리케이션 -> Primary RDS
읽기 요청 -> 애플리케이션 -> Read Replica
Primary RDS -> 비동기 복제 -> Read Replica
```

애플리케이션이 읽기와 쓰기 경로를 구분해야 하며, 어떤 조회를 Replica로 보낼지 데이터 최신성 요구에 따라 결정해야 합니다.

## 운영 예시

상품 목록과 리포트 조회가 급증해 원본 RDS CPU가 높아졌다면 Read Replica로 조회를 분산할 수 있습니다. 하지만 상품 가격을 수정한 직후 상세 화면에서 최신 가격을 반드시 보여줘야 한다면 해당 조회는 Primary를 사용하거나 복제 지연을 고려한 별도 정책이 필요합니다.

## 복제 지연과 정합성

복제는 비동기이므로 Primary에 저장한 데이터가 Replica에 즉시 보이지 않을 수 있습니다. 이 현상을 허용할 수 있는 목록, 검색, 통계성 조회와 허용할 수 없는 결제, 권한, 저장 직후 조회를 구분해야 합니다.

## 장애 상황

- Replica lag가 커지면 사용자가 오래된 데이터를 보게 됩니다.
- 애플리케이션의 읽기/쓰기 라우팅이 잘못되면 쓰기 요청이 실패하거나 최신성이 깨집니다.
- Replica 장애 시 읽기 요청을 Primary로 모두 우회하면 원본 DB가 급격히 포화될 수 있습니다.
- Read Replica만 추가해도 Primary 쓰기 장애가 자동으로 해결되는 것은 아닙니다.

## Trade-off

Read Replica는 읽기 부하를 분산하지만 인스턴스 비용, 복제 지연, 라우팅 복잡도를 추가합니다. 반복 조회가 많고 약간의 오래된 데이터를 허용할 수 있다면 Redis Cache도 대안이지만 TTL과 무효화 정책이 필요합니다.

## 실무 주의점

- 조회별 최신성 요구를 명시하고 Replica 사용 범위를 정합니다.
- replica lag를 모니터링하고 임계값 초과 시 처리 정책을 준비합니다.
- Replica 장애 시 fallback이 Primary를 압도하지 않도록 부하를 제한합니다.
- Read Replica를 재해 복구 또는 Multi-AZ와 같은 기능으로 오해하지 않습니다.

## 면접 질문

1. Read Replica에서 방금 저장한 데이터가 보이지 않을 수 있는 이유는 무엇인가요?
2. 어떤 조회를 Primary와 Replica에 각각 보내야 하나요?
3. Replica 장애 시 Primary fallback의 위험은 무엇인가요?

## 추가 학습

- [Amazon RDS Read Replica](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_ReadRepl.html)
- [Read Replica 모니터링](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_ReadRepl.Monitoring.html)
