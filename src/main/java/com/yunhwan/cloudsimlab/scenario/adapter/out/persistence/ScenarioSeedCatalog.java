package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

public final class ScenarioSeedCatalog {

	private static final List<Scenario> SCENARIOS = List.of(
				Scenario.newScenarioWithGraphKey(
						"single-spring-boot",
						"단일 Spring Boot 배포",
						ScenarioCategory.COMPUTE,
						ScenarioLevel.BEGINNER,
						"단일 EC2 장애 지점과 배포 중단 위험을 줄이는 기본 운영 구조를 선택합니다.",
						"현재 Spring Boot API가 EC2 한 대에서 실행되고 있고 Client 요청이 이 서버로 직접 들어옵니다. 배포 중 재시작이 발생하면 전체 요청이 실패하고, 인스턴스 장애가 나면 RDS는 정상이어도 서비스가 중단됩니다. 비용을 크게 늘리지 않으면서 장애 우회와 점진적 확장이 가능한 구조를 선택해야 합니다.",
						List.of("Client", "EC2", "RDS"),
						List.of(
								ScenarioOption.newOption("EC2 한 대 유지", "비용과 운영 복잡도는 가장 낮지만 배포, 장애, 트래픽 증가가 모두 단일 서버에 집중됩니다.", 1, false, 1,
										effects(0, -3, 3, 3, 0, 0)),
								ScenarioOption.newOptionWithGraphKey("add-alb-auto-scaling", "ALB와 Auto Scaling 추가", "비용과 설정 복잡도는 늘지만 정상 인스턴스로 요청을 분산하고 장애 인스턴스를 우회할 수 있습니다.", 3, true, 0,
										effects(3, 3, -2, -2, 0, 1)),
								ScenarioOption.newOption("EC2 사양만 크게 변경", "CPU와 메모리 여유는 생기지만 단일 장애 지점과 배포 중단 문제는 해결하지 못합니다.", 2, false, 0,
										effects(2, -2, -1, 1, 0, 0))
						)
				),
				Scenario.newScenarioWithGraphKey(
						"private-subnet-app",
						"Private subnet 애플리케이션 서버",
						ScenarioCategory.NETWORK,
						ScenarioLevel.BEGINNER,
						"외부 진입점은 유지하면서 애플리케이션 서버의 직접 노출을 줄입니다.",
						"EC2 애플리케이션 서버가 Public subnet에 있고 Security Group 실수 시 서버 포트가 인터넷에 직접 노출될 수 있습니다. Client 요청은 계속 받아야 하지만 EC2와 RDS는 외부에서 직접 접근하지 못하게 분리해야 합니다. 동시에 애플리케이션 서버가 외부 API 호출이나 패키지 업데이트를 수행할 수 있는지도 고려해야 합니다.",
						List.of("Client", "Internet Gateway", "Public subnet", "EC2", "RDS"),
						List.of(
								ScenarioOption.newOption("EC2를 Public subnet에 유지", "구성은 단순하지만 관리 포트나 애플리케이션 포트가 잘못 열리면 보안 위험이 커집니다.", 1, false, 2,
										effects(0, -1, 2, 3, 0, -3)),
								ScenarioOption.newOptionWithGraphKey("add-alb-private-ec2", "ALB 앞단과 Private subnet EC2로 분리", "Client 진입점은 ALB로 유지하고 EC2 인바운드는 ALB에서 오는 요청으로 제한할 수 있습니다.", 3, true, 0,
										effects(1, 2, -2, -2, 0, 3)),
								ScenarioOption.newOptionWithGraphKey("add-private-ec2-nat", "Private subnet EC2와 NAT Gateway 구성", "서버 직접 노출은 줄이고 아웃바운드 통신은 확보하지만 NAT Gateway 비용과 라우팅 복잡도가 추가됩니다.", 2, false, 0,
										effects(0, 1, -3, -2, 0, 3))
						)
				),
				Scenario.newScenarioWithGraphKey(
						"traffic-spike-compute",
						"트래픽 급증 대응",
						ScenarioCategory.COMPUTE,
						ScenarioLevel.INTERMEDIATE,
						"EC2 계층의 요청 증가를 흡수하면서 비용과 확장 지연을 함께 판단합니다.",
						"이벤트 시작 후 Client 요청이 평소보다 크게 늘었고 ALB target response time과 EC2 CPU 사용률이 동시에 상승했습니다. RDS CPU는 아직 여유가 있지만 API 응답 시간이 길어지고 있습니다. 현재 병목이 애플리케이션 서버 계층에 있는지 판단하고, 피크를 따라갈 수 있는 확장 전략을 선택해야 합니다.",
						List.of("Client", "ALB", "EC2", "RDS"),
						List.of(
								ScenarioOption.newOptionWithGraphKey("add-auto-scaling", "Auto Scaling 추가", "요청 증가에 맞춰 EC2 수를 늘려 처리량과 가용성을 높이지만 시작 시간과 추가 비용을 고려해야 합니다.", 3, true, 0,
										effects(3, 3, -2, -2, 0, 0)),
								ScenarioOption.newOption("EC2 사양만 크게 변경", "즉시 CPU 여유를 만들 수 있지만 피크가 더 커지면 다시 한계가 오고 장애 우회 능력도 제한적입니다.", 2, false, 0,
										effects(2, -1, -2, 1, 0, 0)),
								ScenarioOption.newOption("RDS만 증설", "현재 병목이 DB가 아니라면 비용만 늘고 API 서버의 CPU와 응답 시간 문제는 남습니다.", 0, false, 1,
										effects(0, 0, -3, 0, 0, 0))
						)
				),
				Scenario.newScenarioWithGraphKey(
						"rds-failure",
						"RDS 장애 대응",
						ScenarioCategory.STORAGE,
						ScenarioLevel.INTERMEDIATE,
						"RDS 단일 장애가 서비스 전체 중단으로 이어지는 시간을 줄입니다.",
						"운영 중인 RDS 인스턴스에 장애가 발생하면 EC2와 ALB는 정상이어도 쓰기와 조회가 모두 실패합니다. 최근 장애 대응 목표는 데이터 유실보다 서비스 중단 시간을 줄이는 쪽에 가깝습니다. 장애 조치 자동화, 복구 시간, 비용 증가, 애플리케이션 재연결 흐름을 함께 고려해야 합니다.",
						List.of("Client", "ALB", "EC2", "RDS"),
						List.of(
								ScenarioOption.newOptionWithGraphKey("enable-multi-az", "Multi-AZ 활성화", "비용은 증가하지만 자동 장애 조치로 RDS 인스턴스 장애 시 복구 시간을 줄일 수 있습니다.", 3, true, 0,
										effects(0, 3, -3, -1, 2, 0)),
								ScenarioOption.newOptionWithGraphKey("add-read-replica", "Read Replica만 추가", "읽기 부하 분산에는 도움이 되지만 쓰기 DB 장애에 대한 자동 장애 조치 대책으로는 부족합니다.", 1, false, 1,
										effects(2, 1, -2, -2, -2, 0)),
								ScenarioOption.newOption("백업 주기만 늘리기", "데이터 복구 가능성은 높이지만 장애가 발생한 순간의 서비스 중단 시간은 줄이지 못합니다.", 0, false, 1,
										effects(0, 0, -1, -1, 3, 0))
						)
				),
				Scenario.newScenarioWithGraphKey(
						"read-heavy-performance",
						"조회 중심 성능 문제",
						ScenarioCategory.STORAGE,
						ScenarioLevel.INTERMEDIATE,
						"읽기 중심 트래픽에서 RDS 부하를 줄이면서 일관성 위험을 판단합니다.",
						"상품 목록과 상세 조회가 급증하면서 RDS CPU와 쿼리 시간이 상승했습니다. 쓰기 요청은 많지 않고 대부분 반복 조회지만, 일부 화면은 방금 변경한 상품 정보가 바로 보여야 합니다. 조회 경로를 캐시하거나 읽기 복제본으로 분리할 때 성능, 비용, 복잡도, 데이터 최신성 trade-off를 함께 판단해야 합니다.",
						List.of("Client", "ALB", "EC2", "RDS"),
						List.of(
								ScenarioOption.newOptionWithGraphKey("add-redis-cache", "Redis Cache 추가", "반복 조회 응답 시간을 줄이고 RDS 부하를 낮추지만 TTL, 무효화, 장애 시 우회 전략이 필요합니다.", 2, true, 0,
										effects(3, 1, -2, -3, -3, 0)),
								ScenarioOption.newOptionWithGraphKey("add-read-replica", "Read Replica 추가", "읽기 부하를 분산할 수 있지만 복제 지연과 읽기/쓰기 분리 복잡도를 감수해야 합니다.", 2, true, 0,
										effects(2, 2, -3, -2, -2, 0)),
								ScenarioOption.newOption("EC2만 증설", "애플리케이션 처리 여유는 늘지만 RDS CPU와 쿼리 시간 병목은 그대로 남을 수 있습니다.", 1, false, 1,
										effects(0, 1, -2, 1, 0, 0))
						)
				),
				Scenario.newScenarioWithGraphKey(
						"redis-failure-fallback",
						"Redis 장애와 RDS fallback 부하 급증",
						ScenarioCategory.STORAGE,
						ScenarioLevel.INTERMEDIATE,
						"Redis 장애 시 RDS fallback 부하가 서비스 장애로 확산되지 않게 대응합니다.",
						"Redis 연결 실패가 증가하면서 캐시 hit율이 급락했고 모든 조회가 RDS로 우회되고 있습니다. RDS CPU와 connection 수가 함께 상승하며 API 응답 시간이 길어집니다. 캐시 장애를 우회하되 RDS를 보호할 제한, TTL 분산, 재시도 제어를 함께 판단해야 합니다.",
						List.of("Client", "ALB", "EC2", "Redis", "RDS"),
						List.of(
								ScenarioOption.newOptionWithGraphKey("add-cache-fallback-guard", "Redis 장애 우회와 RDS 보호 장치 적용", "캐시 실패 시 제한된 RDS fallback, 짧은 타임아웃, TTL 분산으로 기능은 유지하면서 RDS 포화를 막습니다.", 3, true, 0,
										effects(1, 3, -1, -2, 1, 0)),
								ScenarioOption.newOption("Redis 재시작만 수행", "캐시 복구에는 도움이 될 수 있지만 장애 중 몰린 RDS fallback과 재시도 폭증을 제어하지 못합니다.", 1, false, 1,
										effects(1, 0, 1, 1, 0, 0)),
								ScenarioOption.newOption("EC2 인스턴스만 증설", "애플리케이션 처리량은 늘 수 있지만 캐시 장애로 RDS에 몰리는 조회 부하는 줄이지 못합니다.", 0, false, 1,
										effects(1, 0, -2, 1, 0, 0))
						)
				),
				Scenario.newScenarioWithGraphKey(
						"rds-connection-pool-exhaustion",
						"RDS Connection Pool 고갈",
						ScenarioCategory.STORAGE,
						ScenarioLevel.INTERMEDIATE,
						"연결 대기와 DB 포화 지표를 함께 보고 커넥션 풀 고갈을 완화합니다.",
						"API timeout이 늘고 Hikari active connection이 최대치에 붙어 있으며 요청 대기 시간이 증가합니다. RDS CPU는 높지만 연결 수와 느린 쿼리도 함께 증가했습니다. 풀 크기를 무작정 키우기보다 쿼리 시간, 트랜잭션 범위, RDS 보호 한계를 함께 판단해야 합니다.",
						List.of("Client", "ALB", "EC2", "Connection Pool", "RDS"),
						List.of(
								ScenarioOption.newOptionWithGraphKey("tune-connection-pool-limits", "Connection Pool 한계와 쿼리 시간을 함께 조정", "풀 크기, timeout, 느린 쿼리, 트랜잭션 범위를 함께 조정해 대기 시간을 줄이면서 RDS 연결 폭주를 막습니다.", 3, true, 0,
										effects(2, 2, 1, -2, 1, 0)),
								ScenarioOption.newOption("RDS 인스턴스 사양 증설", "DB 처리 여유는 늘릴 수 있지만 애플리케이션 풀 대기와 느린 쿼리 원인이 남으면 재발할 수 있습니다.", 1, false, 1,
										effects(1, 1, -3, 1, 0, 0)),
								ScenarioOption.newOption("Connection Pool 최대값만 크게 증가", "짧은 대기는 줄어도 RDS 동시 연결과 쿼리 부하가 폭증해 장애를 키울 수 있습니다.", 1, false, 2,
										effects(1, -2, 1, 1, 0, 0))
						)
				),
				Scenario.newScenarioWithGraphKey(
						"alb-health-check-failure",
						"ALB Health Check 실패",
						ScenarioCategory.NETWORK,
						ScenarioLevel.INTERMEDIATE,
						"ALB가 정상 인스턴스를 제외하는 원인을 Health Check와 요청 경로에서 좁힙니다.",
						"배포 후 ALB UnHealthyHostCount가 증가했고 Target 5xx 없이도 사용자 요청이 503으로 실패합니다. EC2 프로세스는 실행 중이지만 Health Check 경로가 외부 의존성을 검사하거나 Security Group 포트가 맞지 않을 수 있습니다. 정상 target 판정 조건을 서비스 준비 상태에 맞게 조정해야 합니다.",
						List.of("Client", "ALB", "Target Group", "EC2", "RDS"),
						List.of(
								ScenarioOption.newOptionWithGraphKey("fix-health-check-path", "Health Check 경로와 EC2 인바운드 규칙 수정", "가벼운 readiness 경로와 ALB에서 EC2로 가는 포트를 맞춰 정상 인스턴스가 target에 남도록 합니다.", 3, true, 0,
										effects(1, 3, 1, -1, 0, 1)),
								ScenarioOption.newOption("EC2 인스턴스 추가", "정상 판정 조건이 틀린 상태라면 새 인스턴스도 비정상 target이 되어 503을 해결하지 못할 수 있습니다.", 1, false, 1,
										effects(1, 0, -2, 1, 0, 0)),
								ScenarioOption.newOption("Health Check 비활성화", "요청은 일부 전달될 수 있지만 비정상 인스턴스에도 트래픽이 가서 장애를 숨기고 확대할 수 있습니다.", 1, false, 3,
										effects(1, -3, 1, 2, 0, -1))
						)
				),
				Scenario.newScenarioWithGraphKey(
						"private-subnet-nat-missing",
						"Private subnet NAT Gateway 또는 라우팅 누락",
						ScenarioCategory.NETWORK,
						ScenarioLevel.INTERMEDIATE,
						"Private subnet 서버의 외부 API 호출 실패를 NAT Gateway와 라우팅 관점에서 복구합니다.",
						"Private subnet의 EC2가 결제사 API와 패키지 저장소에 연결하지 못하고 connection timeout이 발생합니다. ALB를 통한 사용자 요청은 들어오지만 아웃바운드 경로가 없거나 NAT Gateway가 Public subnet 라우팅과 연결되지 않았을 수 있습니다. 보안 경계를 유지하면서 필요한 외부 통신만 열어야 합니다.",
						List.of("Client", "ALB", "Private subnet", "EC2", "RDS"),
						List.of(
								ScenarioOption.newOptionWithGraphKey("add-nat-gateway-route", "NAT Gateway와 Private 라우팅 경로 추가", "Public subnet의 NAT Gateway와 Private 라우팅 테이블 경로를 구성해 서버 직접 노출 없이 외부 호출을 복구합니다.", 3, true, 0,
										effects(1, 2, -3, -2, 0, 2)),
								ScenarioOption.newOption("VPC Endpoint만 추가", "S3 같은 AWS 서비스 접근에는 도움이 되지만 결제사 API 같은 인터넷 외부 서비스 호출은 해결하지 못할 수 있습니다.", 1, false, 0,
										effects(1, 1, 1, -1, 0, 2)),
								ScenarioOption.newOption("EC2를 Public subnet으로 이동", "외부 통신은 쉬워질 수 있지만 애플리케이션 서버 직접 노출 위험이 커져 보안 목표를 훼손합니다.", 1, false, 2,
										effects(1, -1, 2, 1, 0, -3))
						)
				),
				Scenario.newScenarioWithGraphKey(
						"security-group-misconfiguration",
						"Security Group 오설정",
						ScenarioCategory.SECURITY,
						ScenarioLevel.INTERMEDIATE,
						"요청 경로 차단과 과도한 노출을 Security Group 참조 관계로 바로잡습니다.",
						"ALB 502와 EC2 DB 연결 실패가 함께 발생했고 일부 포트는 임시로 0.0.0.0/0에 열려 있습니다. Client는 ALB로만 들어오고 ALB는 EC2 애플리케이션 포트로, EC2는 RDS 포트로만 접근해야 합니다. 막힌 요청 경로와 과도한 노출을 동시에 줄여야 합니다.",
						List.of("Client", "ALB", "Security Group", "EC2", "RDS"),
						List.of(
								ScenarioOption.newOptionWithGraphKey("fix-security-group-references", "Security Group 참조 관계로 최소 허용 재구성", "ALB, EC2, RDS 사이 필요한 포트만 Security Group 참조로 허용해 요청 경로를 복구하고 직접 노출을 줄입니다.", 3, true, 0,
										effects(1, 2, 1, -1, 0, 3)),
								ScenarioOption.newOption("RDS Multi-AZ 활성화", "DB 인프라 가용성에는 도움이 되지만 Security Group이 막은 연결 경로나 과도한 노출은 해결하지 못합니다.", 1, false, 0,
										effects(0, 1, -3, -1, 2, 0)),
								ScenarioOption.newOption("문제 포트를 0.0.0.0/0에 개방", "연결은 빠르게 살아날 수 있지만 인터넷 전체에 관리 포트나 DB 포트를 노출할 수 있어 위험합니다.", 1, false, 3,
										effects(1, -1, 2, 2, 0, -3))
						)
				)
		);

	private static final Set<String> SCENARIO_GRAPH_KEYS = SCENARIOS.stream()
			.map(Scenario::getGraphKey)
			.collect(Collectors.toUnmodifiableSet());

	private ScenarioSeedCatalog() {
	}

	public static List<Scenario> scenarios() {
		return SCENARIOS;
	}

	public static Set<String> scenarioGraphKeys() {
		return SCENARIO_GRAPH_KEYS;
	}

	private static TradeOffEffects effects(
			int performance,
			int availability,
			int cost,
			int complexity,
			int consistency,
			int security
	) {
		return new TradeOffEffects(performance, availability, cost, complexity, consistency, security);
	}
}
