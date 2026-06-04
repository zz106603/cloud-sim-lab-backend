# Redis Cache

## 왜 필요한가

Redis는 자주 읽는 데이터를 메모리에 저장해 응답 시간을 줄이고 RDS 조회 부하를 낮추는 캐시 계층으로 사용할 수 있습니다. 반복 조회가 많고 데이터 최신성을 잠시 늦춰도 되는 기능에서 효과적입니다.

## Cache-aside 요청 흐름

```text
1. 애플리케이션 -> Redis 조회
2. Cache hit: Redis 데이터 반환
3. Cache miss: RDS 조회 -> Redis 저장 -> 데이터 반환
```

애플리케이션이 캐시 조회와 DB fallback을 직접 관리합니다. Redis에 데이터가 없거나 장애가 나도 RDS에서 읽을 수 있지만, 동시에 많은 요청이 RDS로 향하지 않도록 보호 장치가 필요합니다.

## 운영 예시

상품 목록처럼 같은 결과를 반복해서 조회하는 API는 TTL을 둔 캐시로 응답 시간과 RDS 부하를 줄일 수 있습니다. 반면 재고나 권한처럼 최신성이 중요한 값은 오래된 캐시가 잘못된 판단을 만들 수 있으므로 무효화 시점과 허용 가능한 지연을 명확히 해야 합니다.

## TTL과 무효화

- TTL은 오래된 데이터가 남는 최대 시간을 제한하지만 너무 짧으면 cache miss가 늘어납니다.
- 데이터 변경 시 관련 키를 삭제하거나 갱신해야 합니다.
- 여러 키가 같은 데이터를 표현하면 일부 키만 무효화되는 문제가 생길 수 있습니다.
- TTL이 동시에 만료되면 많은 요청이 RDS로 몰리는 cache stampede가 발생할 수 있습니다.

## 장애 상황

- Redis 장애 시 모든 요청이 RDS로 우회해 DB가 포화될 수 있습니다.
- 캐시 무효화 실패로 사용자가 오래된 데이터를 볼 수 있습니다.
- DB 조회 실패 결과까지 캐시하면 장애가 복구된 뒤에도 오류가 유지될 수 있습니다.
- 캐시를 영구 저장소처럼 사용하면 eviction이나 장애 시 데이터를 잃을 수 있습니다.

## Trade-off

Redis Cache는 매우 빠른 읽기와 DB 부하 감소를 제공하지만 비용, 운영 복잡도, 정합성 위험을 추가합니다. Read Replica는 읽기 가능한 원본 데이터 복제본을 제공하지만 복제 지연과 읽기 라우팅이 필요합니다. 데이터 특성과 최신성 요구에 따라 선택해야 합니다.

## 실무 주의점

- 캐시 없이도 기능이 동작하는 fallback과 RDS 보호 정책을 설계합니다.
- 키 규칙, TTL, 무효화 책임을 명확히 정의합니다.
- hit ratio, 응답 시간, eviction, 메모리, 오류율을 모니터링합니다.
- 장애 시 무제한 재시도보다 빠른 실패와 제한된 우회를 사용합니다.

## 면접 질문

1. Cache-aside 패턴의 장점과 단점은 무엇인가요?
2. cache stampede는 왜 발생하며 어떻게 완화할 수 있나요?
3. Redis 장애가 RDS 장애로 확산될 수 있는 이유는 무엇인가요?

## 추가 학습

- [ElastiCache 캐싱 전략](https://docs.aws.amazon.com/AmazonElastiCache/latest/dg/Strategies.html)
- [ElastiCache 장애 완화](https://docs.aws.amazon.com/AmazonElastiCache/latest/dg/FaultTolerance.html)
