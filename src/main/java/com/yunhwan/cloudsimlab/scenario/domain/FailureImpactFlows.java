package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FailureImpactFlows {

	private static final Map<String, Definition> DEFINITIONS = Map.of(
			"rds-failure", rdsFailure(),
			"redis-failure-fallback", redisFailureFallback(),
			"rds-connection-pool-exhaustion", rdsConnectionPoolExhaustion(),
			"alb-health-check-failure", albHealthCheckFailure(),
			"private-subnet-nat-missing", privateSubnetNatMissing(),
			"security-group-misconfiguration", securityGroupMisconfiguration()
	);

	private FailureImpactFlows() {
	}

	public static FailureImpact initialFor(Scenario scenario) {
		Definition definition = definitionFor(scenario);
		return definition == null ? null : definition.initialImpact();
	}

	public static FailureImpactResult resultFor(Scenario scenario, List<ScenarioOption> selectedOptions) {
		Definition definition = definitionFor(scenario);
		if (definition == null) {
			return null;
		}
		List<ScenarioOption> options = selectedOptions == null ? List.of() : selectedOptions;
		return options.stream()
				.filter(ScenarioOption::isCore)
				.map(ScenarioOption::getGraphKey)
				.filter(graphKey -> graphKey != null)
				.filter(definition.recoveries()::containsKey)
				.findFirst()
				.or(() -> options.stream()
						.map(ScenarioOption::getGraphKey)
						.filter(graphKey -> graphKey != null)
						.filter(definition.recoveries()::containsKey)
						.findFirst())
				.map(definition.recoveries()::get)
				.map(recovery -> new FailureImpactResult(
						recovery.recoveredEdges(),
						recovery.remainingImpact(),
						recovery.postActionNotes()
				))
				.orElseGet(() -> new FailureImpactResult(
						List.of(),
						definition.initialImpact(),
						List.of("선택한 대응은 장애 영향 경로를 직접 복구하지 못해 초기 영향이 그대로 남습니다.")
				));
	}

	public static boolean hasDefinition(String scenarioGraphKey) {
		return DEFINITIONS.containsKey(normalizedKey(scenarioGraphKey));
	}

	private static Definition definitionFor(Scenario scenario) {
		if (scenario == null) {
			return null;
		}
		return DEFINITIONS.get(normalizedKey(scenario.getGraphKey()));
	}

	private static Definition rdsFailure() {
		FailureImpact initialImpact = impact(
				"RDS",
				List.of("RDS", "EC2"),
				List.of(edge("EC2", "RDS", "DB 접근")),
				List.of(
						"쓰기와 조회 요청이 모두 DB 오류 또는 timeout으로 실패합니다.",
						"ALB와 EC2가 정상이어도 데이터 접근이 막혀 서비스 기능이 중단됩니다."
				),
				List.of("단일 AZ RDS 장애에서는 수동 복구 시간 동안 서비스 중단이 길어질 수 있습니다.")
		);
		return new Definition(initialImpact, Map.of(
				"enable-multi-az", recovery(
						List.of(edge("EC2", "RDS", "DB 접근")),
						remaining("RDS", List.of("장애 조치 중 기존 DB 연결은 끊길 수 있어 재연결과 재시도 정책이 필요합니다.")),
						List.of("Multi-AZ 장애 조치로 같은 엔드포인트의 DB 접근 경로를 복구합니다.")
				),
				"add-read-replica", recovery(
						List.of(),
						initialImpact,
						List.of("Read Replica는 읽기 분산에는 도움이 되지만 Primary 장애의 쓰기 실패와 자동 장애 조치를 해결하지 못합니다.")
				)
		));
	}

	private static Definition redisFailureFallback() {
		FailureImpact initialImpact = impact(
				"Redis",
				List.of("Redis", "RDS", "EC2"),
				List.of(
						edge("EC2", "Redis", "연결"),
						edge("Redis", "RDS", "DB 접근")
				),
				List.of(
						"캐시 조회 실패가 증가하고 조회 요청이 RDS로 한꺼번에 우회됩니다.",
						"RDS CPU와 connection 수가 상승하며 API 응답 시간이 길어집니다."
				),
				List.of("fallback 제한이 없으면 Redis 장애가 RDS 포화로 확산될 수 있습니다.")
		);
		return new Definition(initialImpact, Map.of(
				"add-cache-fallback-guard", recovery(
						List.of(
								edge("EC2", "Redis", "캐시 조회"),
								edge("EC2", "RDS Fallback Guard", "제한된 fallback"),
								edge("RDS Fallback Guard", "RDS", "보호된 조회")
						),
						remaining("Redis", List.of("Redis 자체 복구 전까지 캐시 hit율 저하와 일부 응답 지연은 남습니다.")),
						List.of("짧은 timeout, 제한된 fallback, TTL 분산으로 RDS 포화를 막는 경로를 복구합니다.")
				)
		));
	}

	private static Definition rdsConnectionPoolExhaustion() {
		FailureImpact initialImpact = impact(
				"Connection Pool",
				List.of("Connection Pool", "EC2", "RDS"),
				List.of(
						edge("EC2", "Connection Pool", "연결"),
						edge("Connection Pool", "RDS", "DB 접근")
				),
				List.of(
						"요청이 커넥션 획득을 기다리다 timeout으로 실패합니다.",
						"RDS 연결 수와 느린 쿼리가 함께 증가해 장애가 재발하기 쉽습니다."
				),
				List.of("풀 크기만 키우면 RDS 동시 연결 폭주가 더 커질 수 있습니다.")
		);
		return new Definition(initialImpact, Map.of(
				"tune-connection-pool-limits", recovery(
						List.of(
								edge("EC2", "Connection Pool", "제한된 연결"),
								edge("Connection Pool", "RDS", "쿼리 실행")
						),
						remaining("Connection Pool", List.of("느린 쿼리와 긴 트랜잭션은 별도 튜닝 없이는 다시 풀 대기를 만들 수 있습니다.")),
						List.of("풀 한계, timeout, 쿼리 시간을 함께 조정해 요청 대기와 RDS 연결 폭주를 줄입니다.")
				)
		));
	}

	private static Definition albHealthCheckFailure() {
		FailureImpact initialImpact = impact(
				"Target Group",
				List.of("Target Group", "ALB", "EC2"),
				List.of(
						edge("ALB", "Target Group", "연결"),
						edge("Target Group", "EC2", "연결")
				),
				List.of(
						"정상 EC2가 target에서 제외되어 사용자 요청이 503으로 실패합니다.",
						"애플리케이션 프로세스가 떠 있어도 Health Check 조건 불일치로 트래픽을 받지 못합니다."
				),
				List.of("Health Check가 외부 의존성을 과도하게 검사하면 배포 중 정상 인스턴스도 제외될 수 있습니다.")
		);
		return new Definition(initialImpact, Map.of(
				"fix-health-check-path", recovery(
						List.of(
								edge("ALB", "Health Check", "readiness 확인"),
								edge("Health Check", "EC2", "정상 target 판정")
						),
						remaining("Target Group", List.of("배포 직후 준비 시간과 Health Check 유예 시간은 계속 맞춰야 합니다.")),
						List.of("가벼운 readiness 경로와 ALB-EC2 포트 허용으로 정상 target 판정 경로를 복구합니다.")
				)
		));
	}

	private static Definition privateSubnetNatMissing() {
		FailureImpact initialImpact = impact(
				"Private subnet",
				List.of("Private subnet", "EC2"),
				List.of(edge("Private subnet", "EC2", "연결")),
				List.of(
						"Private subnet EC2의 외부 API 호출과 패키지 다운로드가 timeout으로 실패합니다.",
						"사용자 인바운드 요청은 들어오지만 결제사 같은 외부 의존 기능이 실패합니다."
				),
				List.of("아웃바운드를 열더라도 서버 직접 노출 없이 필요한 목적지만 통제해야 합니다.")
		);
		return new Definition(initialImpact, Map.of(
				"add-nat-gateway-route", recovery(
						List.of(
								edge("Private subnet", "NAT Gateway", "아웃바운드"),
								edge("NAT Gateway", "Internet Gateway", "인터넷 접근")
						),
						remaining("Private subnet", List.of("NAT Gateway 비용과 라우팅 테이블 변경 영향은 운영 중 계속 확인해야 합니다.")),
						List.of("Private 라우팅 테이블에서 NAT Gateway를 통해 외부 호출 경로를 복구합니다.")
				)
		));
	}

	private static Definition securityGroupMisconfiguration() {
		FailureImpact initialImpact = impact(
				"Security Group",
				List.of("Security Group", "EC2", "RDS"),
				List.of(
						edge("ALB", "Security Group", "연결"),
						edge("Security Group", "EC2", "연결"),
						edge("EC2", "RDS", "DB 접근")
				),
				List.of(
						"ALB 502와 EC2 DB 연결 실패가 함께 발생합니다.",
						"임시 전체 개방 포트가 남아 보안 노출 위험이 커집니다."
				),
				List.of("요청 경로 복구와 과도한 인바운드 차단을 함께 검증해야 합니다.")
		);
		return new Definition(initialImpact, Map.of(
				"fix-security-group-references", recovery(
						List.of(
								edge("ALB Security Group", "EC2 Security Group", "애플리케이션 포트 허용"),
								edge("EC2 Security Group", "RDS Security Group", "DB 포트 허용")
						),
						remaining("Security Group", List.of("보안 그룹 변경 후 ALB Health Check와 DB 연결을 함께 검증해야 합니다.")),
						List.of("Security Group 참조 관계로 필요한 포트만 허용해 요청 경로와 보안 경계를 함께 복구합니다.")
				)
		));
	}

	private static FailureImpact impact(
			String failureSource,
			List<String> affectedNodes,
			List<FailureImpactEdge> affectedEdges,
			List<String> userSymptoms,
			List<String> remainingRisks
	) {
		return new FailureImpact(
				nodeId(failureSource),
				affectedNodes.stream()
						.map(FailureImpactFlows::nodeId)
						.toList(),
				affectedEdges,
				userSymptoms,
				remainingRisks
		);
	}

	private static FailureImpact remaining(String failureSource, List<String> remainingRisks) {
		return impact(failureSource, List.of(), List.of(), List.of(), remainingRisks);
	}

	private static Recovery recovery(
			List<FailureImpactEdge> recoveredEdges,
			FailureImpact remainingImpact,
			List<String> postActionNotes
	) {
		return new Recovery(recoveredEdges, remainingImpact, postActionNotes);
	}

	private static FailureImpactEdge edge(String source, String target, String label) {
		return new FailureImpactEdge(nodeId(source), nodeId(target), label);
	}

	private static String nodeId(String label) {
		if (label == null || label.isBlank()) {
			return "unknown";
		}
		return label.trim()
				.toLowerCase(Locale.ROOT)
				.replace(" ", "-")
				.replace("_", "-");
	}

	private static String normalizedKey(String key) {
		return key == null ? "" : key.trim();
	}

	private record Definition(FailureImpact initialImpact, Map<String, Recovery> recoveries) {
	}

	private record Recovery(
			List<FailureImpactEdge> recoveredEdges,
			FailureImpact remainingImpact,
			List<String> postActionNotes
	) {
	}
}
