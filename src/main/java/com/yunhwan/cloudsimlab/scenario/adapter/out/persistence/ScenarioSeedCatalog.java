package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioObservationPoint;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioPrerequisiteConcept;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

public final class ScenarioSeedCatalog {

	private static final List<Scenario> SCENARIOS = List.of(
				scenario(
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
				scenario(
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
				scenario(
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
				scenario(
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
				scenario(
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
				scenario(
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
				scenario(
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
				scenario(
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
				scenario(
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
				scenario(
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

	private static Scenario scenario(
			String graphKey,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<String> initialArchitecture,
			List<ScenarioOption> options
	) {
		return Scenario.newScenarioWithLearningContext(
				graphKey,
				title,
				category,
				level,
				summary,
				description,
				initialArchitecture,
				relatedModuleIds(graphKey),
				prerequisiteConcepts(graphKey),
				observationPoint(graphKey),
				judgmentPerspectives(graphKey),
				options
		);
	}

	private static List<String> relatedModuleIds(String graphKey) {
		return switch (graphKey) {
			case "single-spring-boot" -> List.of("single-server-deployment");
			case "private-subnet-app" -> List.of("network-boundary", "alb-private-subnet");
			case "traffic-spike-compute", "alb-health-check-failure" -> List.of("auto-scaling-health-check");
			case "rds-failure", "read-heavy-performance", "redis-failure-fallback", "rds-connection-pool-exhaustion" -> List.of("data-tier-scaling");
			case "private-subnet-nat-missing" -> List.of("user-architecture-practice");
			case "security-group-misconfiguration" -> List.of("network-boundary", "user-architecture-practice");
			default -> List.of();
		};
	}

	private static List<ScenarioPrerequisiteConcept> prerequisiteConcepts(String graphKey) {
		return switch (graphKey) {
			case "single-spring-boot" -> List.of(
					concept("ec2-capacity", "EC2 용량과 단일 장애 지점", "ec2-compute-capacity", "단일 EC2가 성능과 가용성 한계를 동시에 만드는 이유를 알아야 합니다."),
					concept("alb-routing", "ALB 요청 분산", "alb-traffic-distribution", "여러 인스턴스로 요청을 보내고 비정상 인스턴스를 우회하는 흐름을 판단해야 합니다."),
					concept("auto-scaling", "Auto Scaling 기본", "auto-scaling-basics", "수평 확장이 비용과 시작 지연을 동반한다는 점을 함께 봐야 합니다.")
			);
			case "private-subnet-app" -> List.of(
					concept("private-subnet", "Private subnet 애플리케이션 서버", "private-subnet-application-server", "서버 직접 노출을 줄이는 네트워크 경계 기준이 필요합니다."),
					concept("alb-routing", "ALB 진입점", "alb-traffic-distribution", "외부 요청은 ALB로 받고 내부 서버로 전달하는 구조를 이해해야 합니다."),
					concept("security-group", "Security Group 최소 허용", "security-group-least-privilege", "ALB, EC2, RDS 사이 필요한 통신만 허용해야 합니다.")
			);
			case "traffic-spike-compute" -> List.of(
					concept("ec2-capacity", "EC2 지표와 용량", "ec2-compute-capacity", "CPU와 응답 시간 지표로 애플리케이션 계층 병목을 판단해야 합니다."),
					concept("auto-scaling", "Auto Scaling 기본", "auto-scaling-basics", "확장 지표, 최대 용량, 시작 지연이 해결책의 한계를 결정합니다."),
					concept("alb-target", "ALB target 분산", "alb-traffic-distribution", "증설된 인스턴스가 정상 target으로 요청을 받는 흐름을 확인해야 합니다.")
			);
			case "rds-failure" -> List.of(
					concept("rds-multi-az", "RDS Multi-AZ", "rds-multi-az", "DB 인스턴스 장애 시 자동 장애 조치가 필요한지 판단해야 합니다."),
					concept("connection-recovery", "RDS 연결 회복", "rds-connection-management", "장애 조치 중 커넥션 풀 재연결과 timeout 영향을 이해해야 합니다."),
					concept("read-replica-role", "Read Replica 역할", "read-replica-read-scaling", "읽기 확장과 쓰기 장애 대응의 목적 차이를 구분해야 합니다.")
			);
			case "read-heavy-performance" -> List.of(
					concept("rds-bottleneck", "RDS 조회 병목", "rds-connection-management", "연결 수와 느린 쿼리 지표가 조회 지연을 설명하는지 봐야 합니다."),
					concept("read-replica", "Read Replica 읽기 확장", "read-replica-read-scaling", "읽기 부하 분산과 복제 지연 trade-off를 판단해야 합니다."),
					concept("redis-cache", "Redis Cache", "redis-cache", "반복 조회 캐시의 성능 개선과 무효화 위험을 함께 이해해야 합니다.")
			);
			case "redis-failure-fallback" -> List.of(
					concept("redis-cache", "Redis Cache 장애 우회", "redis-cache", "캐시 실패가 RDS fallback으로 전파되는 흐름을 이해해야 합니다."),
					concept("rds-protection", "RDS 연결 보호", "rds-connection-management", "fallback 요청 제한과 connection 보호 기준이 필요합니다.")
			);
			case "rds-connection-pool-exhaustion" -> List.of(
					concept("connection-pool", "RDS 연결 관리", "rds-connection-management", "풀 크기, timeout, 느린 쿼리를 함께 봐야 고갈 원인을 좁힐 수 있습니다."),
					concept("auto-scaling-connection", "Auto Scaling과 연결 총량", "auto-scaling-basics", "인스턴스 수 증가가 DB 연결 수를 같이 늘릴 수 있음을 알아야 합니다.")
			);
			case "alb-health-check-failure" -> List.of(
					concept("alb-health-check", "ALB Health Check", "alb-traffic-distribution", "정상 target 판정이 요청 성공 여부를 결정하는 흐름을 이해해야 합니다."),
					concept("security-group", "Security Group 최소 허용", "security-group-least-privilege", "ALB에서 EC2로 가는 포트 허용이 Health Check에 영향을 줍니다.")
			);
			case "private-subnet-nat-missing" -> List.of(
					concept("nat-gateway", "NAT Gateway 아웃바운드", "nat-gateway-outbound-communication", "Private subnet 서버가 외부 API로 나가는 경로를 알아야 합니다."),
					concept("private-subnet", "Private subnet 경계", "private-subnet-application-server", "서버 직접 노출 없이 아웃바운드만 여는 기준이 필요합니다.")
			);
			case "security-group-misconfiguration" -> List.of(
					concept("security-group", "Security Group 최소 허용", "security-group-least-privilege", "필요 포트와 출발지만 허용하는 참조 관계를 판단해야 합니다."),
					concept("alb-routing", "ALB target 요청 흐름", "alb-traffic-distribution", "ALB와 EC2 사이 차단이 사용자 요청 실패로 이어지는 흐름을 봐야 합니다.")
			);
			default -> List.of();
		};
	}

	private static ScenarioObservationPoint observationPoint(String graphKey) {
		return switch (graphKey) {
			case "single-spring-boot" -> observation("EC2 CPU, 배포 중 재시작 시간, ALB target 상태", "단일 EC2 장애 또는 배포 재시작", "Client -> EC2 -> RDS에서 Client -> ALB -> EC2 여러 대 -> RDS로 바뀌는지 봅니다.", "Client가 EC2에 직접 닿지 않고 ALB를 거치는지 확인합니다.", "RDS 쓰기 경로는 그대로이므로 데이터 정합성 변화는 작습니다.", "가용성과 성능은 좋아지지만 비용과 운영 복잡도가 늘어납니다.");
			case "private-subnet-app" -> observation("외부 노출 포트, ALB target 상태, NAT 아웃바운드 실패율", "Public subnet EC2 직접 노출 또는 Private subnet 아웃바운드 단절", "Client -> ALB -> Private EC2 -> RDS 흐름이 유지되는지 봅니다.", "EC2와 RDS가 인터넷에서 직접 접근되지 않는지 확인합니다.", "데이터 경로는 유지되므로 정합성보다는 네트워크 차단 위험이 중심입니다.", "보안은 좋아지지만 NAT/ALB 비용과 라우팅 복잡도가 늘어납니다.");
			case "traffic-spike-compute" -> observation("ALB target response time, EC2 CPU, 요청 대기 시간", "애플리케이션 서버 계층 용량 부족", "Client -> ALB -> EC2 target 분산이 피크를 따라가는지 봅니다.", "확장 인스턴스도 동일한 보안 경계를 따르는지 확인합니다.", "DB가 병목이 아니면 정합성 위험은 낮지만 세션 상태 저장 방식은 확인해야 합니다.", "처리량과 가용성은 좋아지지만 확장 지연과 비용 부담이 생깁니다.");
			case "rds-failure" -> observation("RDS failover 시간, DB connection error, 재연결 성공률", "Primary RDS 인스턴스 장애", "Client -> ALB -> EC2 -> RDS 연결이 장애 조치 후 회복되는지 봅니다.", "네트워크 경계보다 DB 엔드포인트와 애플리케이션 재연결 조건이 중심입니다.", "Multi-AZ는 동기 복제로 정합성에 유리하지만 Read Replica는 복제 지연이 있습니다.", "가용성은 좋아지지만 DB 비용과 장애 조치 운영 이해도가 필요합니다.");
			case "read-heavy-performance" -> observation("RDS CPU, slow query, cache hit rate, replica lag", "조회 부하가 RDS primary에 집중", "조회 요청이 Redis 또는 Read Replica로 분산되고 쓰기는 primary로 유지되는지 봅니다.", "데이터 계층 접근 권한이 애플리케이션에서만 열리는지 확인합니다.", "캐시 TTL, 무효화, replica lag가 최신성 위험을 만듭니다.", "성능은 좋아지지만 비용, 복잡도, 정합성 관리 부담이 늘어납니다.");
			case "redis-failure-fallback" -> observation("Redis error rate, cache hit rate, RDS CPU, DB connection 수", "Redis 장애 후 모든 조회가 RDS로 몰림", "캐시 실패 시 제한된 fallback으로 RDS에 접근하는지 봅니다.", "장애 우회 경로가 기존 데이터 접근 경계를 넘지 않는지 확인합니다.", "fallback과 TTL 정책에 따라 오래된 데이터 또는 과도한 DB 조회 위험이 있습니다.", "장애 지속 중 기능 유지와 RDS 보호 사이의 제한 정책이 필요합니다.");
			case "rds-connection-pool-exhaustion" -> observation("Hikari active/idle connection, connection wait time, slow query", "애플리케이션 커넥션 풀과 RDS 연결 한계", "Client -> ALB -> EC2 -> Connection Pool -> RDS 대기 구간을 봅니다.", "보안 경계보다 풀 설정과 DB 연결 제한이 중심입니다.", "긴 트랜잭션은 잠금과 최신성 지연을 함께 만들 수 있습니다.", "대기 시간 완화와 RDS 보호 사이에서 풀 크기/timeout/쿼리 개선을 함께 조정해야 합니다.");
			case "alb-health-check-failure" -> observation("UnHealthyHostCount, Health Check status, ALB 503", "정상 EC2가 target에서 제외됨", "Client -> ALB -> Target Group -> EC2 Health Check 흐름을 봅니다.", "ALB Security Group에서 EC2 애플리케이션 포트가 허용되는지 확인합니다.", "Health Check 경로가 DB 같은 외부 의존성을 검사하면 정합성보다 가용성 오판 위험이 큽니다.", "정확한 readiness는 가용성을 높이지만 검사 경로와 임계값 운영이 필요합니다.");
			case "private-subnet-nat-missing" -> observation("외부 API timeout, NAT Gateway bytes, route table 대상", "Private subnet EC2의 인터넷 아웃바운드 경로 누락", "Client -> ALB -> Private EC2 인바운드는 유지하고 EC2 -> NAT Gateway -> Internet 흐름을 봅니다.", "EC2를 Public subnet으로 옮기지 않고 필요한 아웃바운드만 여는지 확인합니다.", "외부 API 호출 실패가 트랜잭션 중단으로 이어질 수 있습니다.", "보안 경계는 유지되지만 NAT 비용과 라우팅 운영 복잡도가 생깁니다.");
			case "security-group-misconfiguration" -> observation("ALB 502, EC2 DB connection error, 열린 포트 범위", "필수 경로 차단과 과도한 0.0.0.0/0 개방", "Client -> ALB -> EC2 -> RDS 각 구간의 허용 출발지와 포트를 봅니다.", "Security Group 참조로 ALB, EC2, RDS 사이 최소 경계를 구성하는지 확인합니다.", "정합성보다 연결 실패와 보안 노출이 중심 위험입니다.", "빠른 개방은 복구처럼 보일 수 있지만 보안 위험을 크게 키웁니다.");
			default -> null;
		};
	}

	private static List<String> judgmentPerspectives(String graphKey) {
		return switch (graphKey) {
			case "private-subnet-app", "security-group-misconfiguration", "private-subnet-nat-missing" -> List.of("security", "availability", "cost", "complexity");
			case "rds-failure" -> List.of("availability", "consistency", "cost", "complexity");
			case "read-heavy-performance", "redis-failure-fallback", "rds-connection-pool-exhaustion" -> List.of("performance", "availability", "consistency", "cost", "complexity");
			default -> List.of("performance", "availability", "cost", "complexity");
		};
	}

	private static ScenarioPrerequisiteConcept concept(String conceptId, String displayName, String relatedDocumentId, String reason) {
		return new ScenarioPrerequisiteConcept(conceptId, displayName, relatedDocumentId, reason);
	}

	private static ScenarioObservationPoint observation(
			String bottleneckMetric,
			String failurePoint,
			String requestFlow,
			String securityBoundary,
			String consistencyRisk,
			String tradeOffSignal
	) {
		return new ScenarioObservationPoint(
				bottleneckMetric,
				failurePoint,
				requestFlow,
				securityBoundary,
				consistencyRisk,
				tradeOffSignal
		);
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
