package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ArchitectureGraphs {

	private static final Set<String> MAPPED_OPTION_KEYS = Set.of(
			"single-spring-boot::add-alb-auto-scaling",
			"traffic-spike-compute::add-auto-scaling",
			"private-subnet-app::add-alb-private-ec2",
			"private-subnet-app::add-private-ec2-nat",
			"rds-failure::enable-multi-az",
			"rds-failure::add-read-replica",
			"read-heavy-performance::add-read-replica",
			"read-heavy-performance::add-redis-cache",
			"redis-failure-fallback::add-cache-fallback-guard",
			"rds-connection-pool-exhaustion::tune-connection-pool-limits",
			"alb-health-check-failure::fix-health-check-path",
			"private-subnet-nat-missing::add-nat-gateway-route",
			"security-group-misconfiguration::fix-security-group-references"
	);

	private ArchitectureGraphs() {
	}

	public static ArchitectureGraph initialFor(Scenario scenario) {
		return graphFor(scenario, List.of());
	}

	public static ArchitectureGraph finalFor(Scenario scenario, List<ScenarioOption> selectedOptions) {
		return graphFor(scenario, selectedOptions);
	}

	public static boolean hasOptionMapping(String scenarioGraphKey, String optionGraphKey) {
		return MAPPED_OPTION_KEYS.contains(graphMappingKey(scenarioGraphKey, optionGraphKey));
	}

	private static ArchitectureGraph graphFor(Scenario scenario, List<ScenarioOption> selectedOptions) {
		Map<String, ArchitectureNode> nodesById = new LinkedHashMap<>();
		List<ArchitectureEdge> edges = new ArrayList<>();
		List<String> components = scenario.getInitialArchitecture();

		for (String component : components) {
			addNode(nodesById, component);
		}
		for (int i = 0; i < components.size() - 1; i++) {
			addEdge(edges, components.get(i), components.get(i + 1), defaultEdgeLabel(components.get(i), components.get(i + 1)));
		}

		for (ScenarioOption option : selectedOptions) {
			applyOption(nodesById, edges, scenario.getGraphKey(), option.getGraphKey());
		}
		return new ArchitectureGraph(new ArrayList<>(nodesById.values()), edges);
	}

	private static void applyOption(
			Map<String, ArchitectureNode> nodesById,
			List<ArchitectureEdge> edges,
			String scenarioGraphKey,
			String optionGraphKey
	) {
		switch (graphMappingKey(scenarioGraphKey, optionGraphKey)) {
			case "single-spring-boot::add-alb-auto-scaling",
					"traffic-spike-compute::add-auto-scaling" -> addAutoScalingPath(nodesById, edges);
			case "private-subnet-app::add-alb-private-ec2" -> addPrivateAlbPath(nodesById, edges);
			case "private-subnet-app::add-private-ec2-nat" -> addNatGatewayPath(nodesById, edges);
			case "rds-failure::enable-multi-az" -> addMultiAzPath(nodesById, edges);
			case "rds-failure::add-read-replica",
					"read-heavy-performance::add-read-replica" -> addReadReplicaPath(nodesById, edges);
			case "read-heavy-performance::add-redis-cache" -> addRedisPath(nodesById, edges);
			case "redis-failure-fallback::add-cache-fallback-guard" -> addCacheFallbackGuardPath(nodesById, edges);
			case "rds-connection-pool-exhaustion::tune-connection-pool-limits" -> addConnectionPoolGuardPath(nodesById, edges);
			case "alb-health-check-failure::fix-health-check-path" -> addHealthCheckPath(nodesById, edges);
			case "private-subnet-nat-missing::add-nat-gateway-route" -> addNatGatewayPath(nodesById, edges);
			case "security-group-misconfiguration::fix-security-group-references" -> addSecurityGroupPath(nodesById, edges);
			default -> {
			}
		}
	}

	private static String graphMappingKey(String scenarioGraphKey, String optionGraphKey) {
		return normalizedKey(scenarioGraphKey) + "::" + normalizedKey(optionGraphKey);
	}

	private static String normalizedKey(String key) {
		return key == null ? "" : key.trim();
	}

	private static void addAutoScalingPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "ALB");
		addNode(nodesById, "Auto Scaling");
		addNode(nodesById, "EC2");
		addEdge(edges, "Client", "ALB", "HTTP 요청");
		addEdge(edges, "ALB", "Auto Scaling", "정상 target 분산");
		addEdge(edges, "Auto Scaling", "EC2", "인스턴스 확장");
	}

	private static void addPrivateAlbPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "ALB");
		addNode(nodesById, "Private subnet");
		addNode(nodesById, "EC2");
		addEdge(edges, "Client", "ALB", "외부 진입점");
		addEdge(edges, "ALB", "Private subnet", "내부 전달");
		addEdge(edges, "Private subnet", "EC2", "애플리케이션 요청");
	}

	private static void addNatGatewayPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "Private subnet");
		addNode(nodesById, "NAT Gateway");
		addNode(nodesById, "Internet Gateway");
		addEdge(edges, "Private subnet", "NAT Gateway", "아웃바운드");
		addEdge(edges, "NAT Gateway", "Internet Gateway", "인터넷 접근");
	}

	private static void addMultiAzPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "RDS");
		addNode(nodesById, "RDS Standby");
		addEdge(edges, "RDS", "RDS Standby", "동기 복제");
	}

	private static void addReadReplicaPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "RDS");
		addNode(nodesById, "Read Replica");
		addEdge(edges, "EC2", "Read Replica", "읽기 요청");
		addEdge(edges, "RDS", "Read Replica", "비동기 복제");
	}

	private static void addRedisPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "Redis");
		addEdge(edges, "EC2", "Redis", "캐시 조회");
	}

	private static void addCacheFallbackGuardPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "Redis");
		addNode(nodesById, "RDS Fallback Guard");
		addNode(nodesById, "RDS");
		addEdge(edges, "EC2", "Redis", "캐시 조회");
		addEdge(edges, "EC2", "RDS Fallback Guard", "제한된 fallback");
		addEdge(edges, "RDS Fallback Guard", "RDS", "보호된 조회");
	}

	private static void addConnectionPoolGuardPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "Connection Pool");
		addNode(nodesById, "RDS");
		addEdge(edges, "EC2", "Connection Pool", "제한된 연결");
		addEdge(edges, "Connection Pool", "RDS", "쿼리 실행");
	}

	private static void addHealthCheckPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "ALB");
		addNode(nodesById, "Health Check");
		addNode(nodesById, "EC2");
		addEdge(edges, "ALB", "Health Check", "readiness 확인");
		addEdge(edges, "Health Check", "EC2", "정상 target 판정");
	}

	private static void addSecurityGroupPath(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges) {
		addNode(nodesById, "ALB Security Group");
		addNode(nodesById, "EC2 Security Group");
		addNode(nodesById, "RDS Security Group");
		addEdge(edges, "ALB Security Group", "EC2 Security Group", "애플리케이션 포트 허용");
		addEdge(edges, "EC2 Security Group", "RDS Security Group", "DB 포트 허용");
	}

	private static void addNode(Map<String, ArchitectureNode> nodesById, String label) {
		nodesById.computeIfAbsent(nodeId(label), nodeId -> new ArchitectureNode(
				nodeId,
				nodeLabel(label),
				nodeType(label),
				nodeDescription(label)
		));
	}

	private static void addEdge(List<ArchitectureEdge> edges, String sourceLabel, String targetLabel, String label) {
		String source = nodeId(sourceLabel);
		String target = nodeId(targetLabel);
		boolean exists = edges.stream()
				.anyMatch(edge -> edge.source().equals(source) && edge.target().equals(target) && edge.label().equals(label));
		if (!exists) {
			edges.add(new ArchitectureEdge(source, target, label));
		}
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

	private static String nodeLabel(String label) {
		return label == null || label.isBlank() ? "Unknown" : label;
	}

	private static String nodeType(String label) {
		if (label == null || label.isBlank()) {
			return "RESOURCE";
		}
		if (label.contains("Client")) {
			return "CLIENT";
		}
		if (label.contains("ALB") || label.contains("Load Balancer")) {
			return "ALB";
		}
		if (label.contains("Auto Scaling")) {
			return "AUTO_SCALING";
		}
		if (label.contains("EC2") || label.contains("Application server")) {
			return "EC2";
		}
		if (label.contains("Read Replica")) {
			return "READ_REPLICA";
		}
		if (label.contains("RDS")) {
			return "RDS";
		}
		if (label.contains("Redis")) {
			return "REDIS";
		}
		if (label.contains("subnet")) {
			return "SUBNET";
		}
		if (label.contains("Gateway")) {
			return "GATEWAY";
		}
		if (label.contains("Security Group")) {
			return "SECURITY_GROUP";
		}
		return "RESOURCE";
	}

	private static String nodeDescription(String label) {
		return switch (nodeType(label)) {
			case "CLIENT" -> "사용자 요청이 시작되는 외부 클라이언트입니다.";
			case "ALB" -> "요청을 정상 target으로 분산하고 장애 인스턴스를 우회하는 진입점입니다.";
			case "AUTO_SCALING" -> "부하나 장애 상황에 맞춰 애플리케이션 인스턴스 수를 조정합니다.";
			case "EC2" -> "애플리케이션 요청을 처리하는 컴퓨팅 리소스입니다.";
			case "RDS" -> "애플리케이션의 주요 영속 데이터를 저장하는 데이터베이스입니다.";
			case "READ_REPLICA" -> "읽기 요청을 분산하지만 복제 지연을 고려해야 하는 복제본입니다.";
			case "REDIS" -> "반복 조회를 빠르게 처리하고 DB 부하를 줄이는 캐시 계층입니다.";
			case "SUBNET" -> "네트워크 노출 범위와 라우팅 경계를 나누는 영역입니다.";
			case "GATEWAY" -> "VPC와 외부 네트워크 사이의 통신 경로를 제공합니다.";
			case "SECURITY_GROUP" -> "리소스 인바운드/아웃바운드 접근을 제한하는 보안 경계입니다.";
			default -> "아키텍처를 구성하는 학습용 리소스입니다.";
		};
	}

	private static String defaultEdgeLabel(String source, String target) {
		if (source != null && source.contains("Client")) {
			return "요청";
		}
		if (target != null && target.contains("RDS")) {
			return "DB 접근";
		}
		return "연결";
	}
}
