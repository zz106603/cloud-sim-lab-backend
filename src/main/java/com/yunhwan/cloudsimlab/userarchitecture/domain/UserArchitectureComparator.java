package com.yunhwan.cloudsimlab.userarchitecture.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureEdge;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureGraph;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureGraphs;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureNode;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult.ComparisonSummary;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult.ConnectionChange;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult.ConnectionComparison;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult.LearningImpact;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult.ResourceChange;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult.ResourceComparison;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult.ScenarioComparison;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult.TradeOffReference;

public final class UserArchitectureComparator {

	private static final Map<String, UserArchitectureResourceType> RESOURCE_TYPES_BY_SCENARIO_LABEL = Map.ofEntries(
			Map.entry("Client", UserArchitectureResourceType.CLIENT),
			Map.entry("VPC", UserArchitectureResourceType.VPC),
			Map.entry("Public subnet", UserArchitectureResourceType.SUBNET),
			Map.entry("Private subnet", UserArchitectureResourceType.SUBNET),
			Map.entry("EC2", UserArchitectureResourceType.EC2),
			Map.entry("Application server", UserArchitectureResourceType.EC2),
			Map.entry("ALB", UserArchitectureResourceType.ALB),
			Map.entry("Target Group", UserArchitectureResourceType.TARGET_GROUP),
			Map.entry("Auto Scaling", UserArchitectureResourceType.AUTO_SCALING_GROUP),
			Map.entry("RDS", UserArchitectureResourceType.RDS),
			Map.entry("RDS Standby", UserArchitectureResourceType.RDS_STANDBY),
			Map.entry("Read Replica", UserArchitectureResourceType.READ_REPLICA),
			Map.entry("Redis", UserArchitectureResourceType.REDIS),
			Map.entry("Connection Pool", UserArchitectureResourceType.CONNECTION_POOL),
			Map.entry("Health Check", UserArchitectureResourceType.HEALTH_CHECK),
			Map.entry("NAT Gateway", UserArchitectureResourceType.NAT_GATEWAY),
			Map.entry("Internet Gateway", UserArchitectureResourceType.INTERNET_GATEWAY),
			Map.entry("Security Group", UserArchitectureResourceType.SECURITY_GROUP),
			Map.entry("ALB Security Group", UserArchitectureResourceType.SECURITY_GROUP),
			Map.entry("EC2 Security Group", UserArchitectureResourceType.SECURITY_GROUP),
			Map.entry("RDS Security Group", UserArchitectureResourceType.SECURITY_GROUP)
	);

	private UserArchitectureComparator() {
	}

	public static UserArchitectureComparisonResult compare(UserArchitecture base, UserArchitecture target) {
		ComparableArchitecture baseArchitecture = ComparableArchitecture.fromUserArchitecture("USER_ARCHITECTURE", base);
		ComparableArchitecture targetArchitecture = ComparableArchitecture.fromUserArchitecture("USER_ARCHITECTURE", target);
		return new UserArchitectureComparisonResult(
				baseArchitecture.summary(),
				targetArchitecture.summary(),
				compareResourcesById(baseArchitecture.resources(), targetArchitecture.resources()),
				compareConnectionsById(baseArchitecture.connections(), targetArchitecture.connections()),
				null,
				List.of()
		);
	}

	public static UserArchitectureComparisonResult compareWithScenarioRecommendation(
			UserArchitecture architecture,
			Scenario scenario,
			List<ScenarioOption> recommendedOptions
	) {
		ComparableArchitecture recommended = ComparableArchitecture.fromScenarioRecommendation(scenario, recommendedOptions);
		ComparableArchitecture user = ComparableArchitecture.fromUserArchitecture("USER_ARCHITECTURE", architecture);
		ResourceComparison resources = compareResourcesBySignature(recommended.resources(), user.resources());
		ConnectionComparison connections = compareConnectionsBySignature(recommended.connections(), user.connections());
		ScenarioComparison scenarioComparison = scenarioComparison(scenario, resources);
		return new UserArchitectureComparisonResult(
				recommended.summary(),
				user.summary(),
				resources,
				connections,
				scenarioComparison,
				recommendedOptions.stream()
						.map(option -> new TradeOffReference(
								option.getName(),
								"시나리오 권장 구조를 만드는 핵심 선택지의 trade-off입니다.",
								option.getEffects()
						))
						.toList()
		);
	}

	private static ResourceComparison compareResourcesById(List<ComparableResource> base, List<ComparableResource> target) {
		return compareResources(base, target, ComparableResource::id, true);
	}

	private static ResourceComparison compareResourcesBySignature(List<ComparableResource> base, List<ComparableResource> target) {
		return compareResources(base, target, ComparableResource::signature, false);
	}

	private static ResourceComparison compareResources(
			List<ComparableResource> base,
			List<ComparableResource> target,
			Function<ComparableResource, String> keyExtractor,
			boolean detectChanges
	) {
		Map<String, ComparableResource> baseByKey = toMap(base, keyExtractor);
		Map<String, ComparableResource> targetByKey = toMap(target, keyExtractor);
		List<ResourceChange> added = new ArrayList<>();
		List<ResourceChange> removed = new ArrayList<>();
		List<ResourceChange> changed = new ArrayList<>();
		List<ResourceChange> unchanged = new ArrayList<>();
		for (String key : orderedUnion(baseByKey, targetByKey)) {
			ComparableResource baseResource = baseByKey.get(key);
			ComparableResource targetResource = targetByKey.get(key);
			if (baseResource == null) {
				added.add(resourceChange("ADDED", key, null, targetResource, "비교 대상 아키텍처에만 있는 리소스입니다."));
			}
			else if (targetResource == null) {
				removed.add(resourceChange("REMOVED", key, baseResource, null, "기준 아키텍처에만 있는 리소스입니다."));
			}
			else if (detectChanges && !baseResource.sameMeaning(targetResource)) {
				changed.add(resourceChange("CHANGED", key, baseResource, targetResource, "같은 ID의 리소스 타입 또는 표시 이름이 변경되었습니다."));
			}
			else {
				unchanged.add(resourceChange("UNCHANGED", key, baseResource, targetResource, "두 아키텍처에서 같은 의미로 유지된 리소스입니다."));
			}
		}
		return new ResourceComparison(added, removed, changed, unchanged);
	}

	private static ConnectionComparison compareConnectionsById(List<ComparableConnection> base, List<ComparableConnection> target) {
		return compareConnections(base, target, ComparableConnection::id, true);
	}

	private static ConnectionComparison compareConnectionsBySignature(List<ComparableConnection> base, List<ComparableConnection> target) {
		return compareConnections(base, target, ComparableConnection::signature, false);
	}

	private static ConnectionComparison compareConnections(
			List<ComparableConnection> base,
			List<ComparableConnection> target,
			Function<ComparableConnection, String> keyExtractor,
			boolean detectChanges
	) {
		Map<String, ComparableConnection> baseByKey = toMap(base, keyExtractor);
		Map<String, ComparableConnection> targetByKey = toMap(target, keyExtractor);
		List<ConnectionChange> added = new ArrayList<>();
		List<ConnectionChange> removed = new ArrayList<>();
		List<ConnectionChange> changed = new ArrayList<>();
		List<ConnectionChange> unchanged = new ArrayList<>();
		for (String key : orderedUnion(baseByKey, targetByKey)) {
			ComparableConnection baseConnection = baseByKey.get(key);
			ComparableConnection targetConnection = targetByKey.get(key);
			if (baseConnection == null) {
				added.add(connectionChange("ADDED", key, null, targetConnection, "비교 대상 아키텍처에만 있는 연결입니다."));
			}
			else if (targetConnection == null) {
				removed.add(connectionChange("REMOVED", key, baseConnection, null, "기준 아키텍처에만 있는 연결입니다."));
			}
			else if (detectChanges && !baseConnection.sameMeaning(targetConnection)) {
				changed.add(connectionChange("CHANGED", key, baseConnection, targetConnection, "같은 ID의 연결 source, target 또는 타입이 변경되었습니다."));
			}
			else {
				unchanged.add(connectionChange("UNCHANGED", key, baseConnection, targetConnection, "두 아키텍처에서 같은 의미로 유지된 연결입니다."));
			}
		}
		return new ConnectionComparison(added, removed, changed, unchanged);
	}

	private static ScenarioComparison scenarioComparison(Scenario scenario, ResourceComparison resources) {
		List<LearningImpact> impacts = new ArrayList<>();
		for (ResourceChange missing : resources.removed()) {
			impacts.add(new LearningImpact(
					"RECOMMENDED_RESOURCE_MISSING",
					missing.resourceKey(),
					"권장 구조의 핵심 컴포넌트가 사용자 아키텍처에 없습니다.",
					"시나리오 학습 목표인 '" + scenario.getSummary() + "'에 영향을 줄 수 있는 차이입니다."
			));
		}
		for (ResourceChange extra : resources.added()) {
			impacts.add(new LearningImpact(
					"EXTRA_RESOURCE_PRESENT",
					extra.resourceKey(),
					"권장 구조에는 없는 컴포넌트가 사용자 아키텍처에 있습니다.",
					"불필요하다고 판정하지는 않지만 현재 시나리오 권장 구조와 다른 설계 의도가 있는지 확인해야 합니다."
			));
		}
		return new ScenarioComparison(
				scenario.getId(),
				scenario.getTitle(),
				scenario.getSummary(),
				resources.removed(),
				resources.added(),
				impacts
		);
	}

	private static ResourceChange resourceChange(String changeType, String key, ComparableResource base, ComparableResource target, String reason) {
		return new ResourceChange(
				changeType,
				key,
				base == null ? target.id() : base.id(),
				base == null ? null : base.resourceType(),
				base == null ? null : base.displayName(),
				target == null ? null : target.resourceType(),
				target == null ? null : target.displayName(),
				reason
		);
	}

	private static ConnectionChange connectionChange(String changeType, String key, ComparableConnection base, ComparableConnection target, String reason) {
		return new ConnectionChange(
				changeType,
				key,
				base == null ? target.id() : base.id(),
				base == null ? null : base.sourceId(),
				base == null ? null : base.targetId(),
				base == null ? null : base.connectionType(),
				target == null ? null : target.sourceId(),
				target == null ? null : target.targetId(),
				target == null ? null : target.connectionType(),
				reason
		);
	}

	private static <T> Map<String, T> toMap(List<T> values, Function<T, String> keyExtractor) {
		return values.stream()
				.collect(Collectors.toMap(keyExtractor, Function.identity(), (first, ignored) -> first, LinkedHashMap::new));
	}

	private static List<String> orderedUnion(Map<String, ?> base, Map<String, ?> target) {
		return java.util.stream.Stream.concat(base.keySet().stream(), target.keySet().stream())
				.distinct()
				.sorted()
				.toList();
	}

	private record ComparableArchitecture(
			ComparisonSummary summary,
			List<ComparableResource> resources,
			List<ComparableConnection> connections
	) {
		static ComparableArchitecture fromUserArchitecture(String comparisonType, UserArchitecture architecture) {
			List<ComparableResource> resources = architecture.getNodes().stream()
					.map(node -> new ComparableResource(node.id(), node.resourceType(), node.displayName()))
					.toList();
			List<ComparableConnection> connections = architecture.getConnections().stream()
					.map(connection -> new ComparableConnection(
							connection.id(),
							connection.sourceNodeId(),
							connection.targetNodeId(),
							connection.connectionType(),
							signatureOf(resources, connection.sourceNodeId()),
							signatureOf(resources, connection.targetNodeId())
					))
					.toList();
			return new ComparableArchitecture(
					new ComparisonSummary(comparisonType, architecture.getArchitectureId(), architecture.getTitle(), resources.size(), connections.size()),
					resources,
					connections
			);
		}

		static ComparableArchitecture fromScenarioRecommendation(Scenario scenario, List<ScenarioOption> recommendedOptions) {
			ArchitectureGraph graph = ArchitectureGraphs.finalFor(scenario, recommendedOptions);
			List<ComparableResource> resources = graph.nodes().stream()
					.map(node -> new ComparableResource(node.id(), resourceTypeOf(node), node.label()))
					.toList();
			List<ComparableConnection> connections = graph.edges().stream()
					.map(edge -> new ComparableConnection(
							edge.source() + "->" + edge.target() + "::" + edge.label(),
							edge.source(),
							edge.target(),
							connectionTypeOf(edge),
							signatureOf(resources, edge.source()),
							signatureOf(resources, edge.target())
					))
					.toList();
			return new ComparableArchitecture(
					new ComparisonSummary("SCENARIO_RECOMMENDATION", String.valueOf(scenario.getId()), scenario.getTitle(), resources.size(), connections.size()),
					resources,
					connections
			);
		}

		private static String signatureOf(List<ComparableResource> resources, String id) {
			return resources.stream()
					.filter(resource -> resource.id().equals(id))
					.findFirst()
					.map(ComparableResource::signature)
					.orElse(id);
		}

		private static UserArchitectureResourceType resourceTypeOf(ArchitectureNode node) {
			String label = node.label() == null ? "" : node.label().trim();
			UserArchitectureResourceType mapped = RESOURCE_TYPES_BY_SCENARIO_LABEL.get(label);
			if (mapped != null) {
				return mapped;
			}
			String nodeType = node.type() == null ? "" : node.type();
			return switch (nodeType) {
				case "CLIENT" -> UserArchitectureResourceType.CLIENT;
				case "ALB" -> UserArchitectureResourceType.ALB;
				case "AUTO_SCALING" -> UserArchitectureResourceType.AUTO_SCALING_GROUP;
				case "EC2" -> UserArchitectureResourceType.EC2;
				case "RDS" -> UserArchitectureResourceType.RDS;
				case "READ_REPLICA" -> UserArchitectureResourceType.READ_REPLICA;
				case "REDIS" -> UserArchitectureResourceType.REDIS;
				case "SUBNET" -> UserArchitectureResourceType.SUBNET;
				case "SECURITY_GROUP" -> UserArchitectureResourceType.SECURITY_GROUP;
				default -> UserArchitectureResourceType.EXTERNAL_SERVICE;
			};
		}

		private static UserArchitectureConnectionType connectionTypeOf(ArchitectureEdge edge) {
			String label = edge.label() == null ? "" : edge.label();
			if (label.contains("복제")) {
				return UserArchitectureConnectionType.REPLICATION;
			}
			if (label.contains("아웃바운드") || label.contains("인터넷")) {
				return UserArchitectureConnectionType.NETWORK_ROUTE;
			}
			if (label.contains("허용") || label.contains("포트")) {
				return UserArchitectureConnectionType.SECURITY_RULE;
			}
			return UserArchitectureConnectionType.REQUEST_FLOW;
		}
	}

	private record ComparableResource(
			String id,
			UserArchitectureResourceType resourceType,
			String displayName
	) {
		String signature() {
			return resourceType.name() + "::" + normalized(displayName);
		}

		boolean sameMeaning(ComparableResource other) {
			return resourceType == other.resourceType && Objects.equals(normalized(displayName), normalized(other.displayName));
		}
	}

	private record ComparableConnection(
			String id,
			String sourceId,
			String targetId,
			UserArchitectureConnectionType connectionType,
			String sourceSignature,
			String targetSignature
	) {
		String signature() {
			return sourceSignature + "->" + targetSignature + "::" + connectionType.name();
		}

		boolean sameMeaning(ComparableConnection other) {
			return Objects.equals(sourceId, other.sourceId)
					&& Objects.equals(targetId, other.targetId)
					&& connectionType == other.connectionType;
		}
	}

	private static String normalized(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}
}
