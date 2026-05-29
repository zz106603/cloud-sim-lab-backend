package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ArchitectureGraphs {

	private ArchitectureGraphs() {
	}

	public static ArchitectureGraph initialFor(Scenario scenario) {
		return graphFor(scenario, List.of());
	}

	public static ArchitectureGraph finalFor(Scenario scenario, List<ScenarioOption> selectedOptions) {
		return graphFor(scenario, selectedOptions);
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
			applyOption(nodesById, edges, option.getName());
		}
		return new ArchitectureGraph(new ArrayList<>(nodesById.values()), edges);
	}

	private static void applyOption(Map<String, ArchitectureNode> nodesById, List<ArchitectureEdge> edges, String optionName) {
		if (optionName.contains("ALB") && optionName.contains("Auto Scaling")) {
			addNode(nodesById, "ALB");
			addNode(nodesById, "Auto Scaling");
			addNode(nodesById, "EC2");
			addEdge(edges, "Client", "ALB", "HTTP 요청");
			addEdge(edges, "ALB", "Auto Scaling", "정상 target 분산");
			addEdge(edges, "Auto Scaling", "EC2", "인스턴스 확장");
			return;
		}
		if (optionName.contains("ALB") && optionName.contains("Private subnet")) {
			addNode(nodesById, "ALB");
			addNode(nodesById, "Private subnet");
			addNode(nodesById, "EC2");
			addEdge(edges, "Client", "ALB", "외부 진입점");
			addEdge(edges, "ALB", "Private subnet", "내부 전달");
			addEdge(edges, "Private subnet", "EC2", "애플리케이션 요청");
			return;
		}
		if (optionName.contains("NAT Gateway")) {
			addNode(nodesById, "Private subnet");
			addNode(nodesById, "NAT Gateway");
			addNode(nodesById, "Internet Gateway");
			addEdge(edges, "Private subnet", "NAT Gateway", "아웃바운드");
			addEdge(edges, "NAT Gateway", "Internet Gateway", "인터넷 접근");
			return;
		}
		if (optionName.contains("Auto Scaling")) {
			addNode(nodesById, "Auto Scaling");
			addNode(nodesById, "EC2");
			addEdge(edges, "ALB", "Auto Scaling", "트래픽 분산");
			addEdge(edges, "Auto Scaling", "EC2", "인스턴스 확장");
			return;
		}
		if (optionName.contains("Multi-AZ")) {
			addNode(nodesById, "RDS");
			addNode(nodesById, "RDS Standby");
			addEdge(edges, "RDS", "RDS Standby", "동기 복제");
			return;
		}
		if (optionName.contains("Read Replica")) {
			addNode(nodesById, "RDS");
			addNode(nodesById, "Read Replica");
			addEdge(edges, "EC2", "Read Replica", "읽기 요청");
			addEdge(edges, "RDS", "Read Replica", "비동기 복제");
			return;
		}
		if (optionName.contains("Redis")) {
			addNode(nodesById, "Redis");
			addEdge(edges, "EC2", "Redis", "캐시 조회");
		}
	}

	private static void addNode(Map<String, ArchitectureNode> nodesById, String label) {
		nodesById.putIfAbsent(nodeId(label), new ArchitectureNode(
				nodeId(label),
				label,
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
		return label.toLowerCase(Locale.ROOT)
				.replace(" ", "-")
				.replace("_", "-");
	}

	private static String nodeType(String label) {
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
		if (source.contains("Client")) {
			return "요청";
		}
		if (target.contains("RDS")) {
			return "DB 접근";
		}
		return "연결";
	}
}
