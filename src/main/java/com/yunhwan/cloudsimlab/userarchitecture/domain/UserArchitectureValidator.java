package com.yunhwan.cloudsimlab.userarchitecture.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidationSeverity.ERROR;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidationSeverity.GUIDANCE;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidationSeverity.WARNING;

public final class UserArchitectureValidator {

	private static final Set<UserArchitectureResourceType> DATA_STORES = EnumSet.of(
			UserArchitectureResourceType.RDS,
			UserArchitectureResourceType.RDS_STANDBY,
			UserArchitectureResourceType.READ_REPLICA,
			UserArchitectureResourceType.REDIS
	);
	private static final Set<UserArchitectureResourceType> APPLICATION_RESOURCES = EnumSet.of(
			UserArchitectureResourceType.EC2,
			UserArchitectureResourceType.AUTO_SCALING_GROUP
	);
	private static final Set<UserArchitectureResourceType> ALB_TARGETS = EnumSet.of(
			UserArchitectureResourceType.EC2,
			UserArchitectureResourceType.AUTO_SCALING_GROUP,
			UserArchitectureResourceType.TARGET_GROUP
	);
	private static final Set<UserArchitectureResourceType> NETWORK_RESOURCES = EnumSet.of(
			UserArchitectureResourceType.VPC,
			UserArchitectureResourceType.SUBNET,
			UserArchitectureResourceType.NAT_GATEWAY,
			UserArchitectureResourceType.INTERNET_GATEWAY,
			UserArchitectureResourceType.EXTERNAL_SERVICE
	);
	private static final Set<UserArchitectureResourceType> REQUEST_FLOW_BOUNDARIES = EnumSet.of(
			UserArchitectureResourceType.VPC,
			UserArchitectureResourceType.SUBNET,
			UserArchitectureResourceType.NAT_GATEWAY,
			UserArchitectureResourceType.INTERNET_GATEWAY,
			UserArchitectureResourceType.SECURITY_GROUP
	);

	private UserArchitectureValidator() {
	}

	public static UserArchitectureValidationResult validate(DraftArchitecture architecture) {
		DraftArchitecture candidate = architecture == null ? new DraftArchitecture(List.of(), List.of()) : architecture;
		List<UserArchitectureValidationIssue> errors = new ArrayList<>();
		List<UserArchitectureValidationIssue> warnings = new ArrayList<>();
		List<UserArchitectureValidationIssue> guidance = new ArrayList<>();
		Map<String, ValidatedNode> nodesById = validateNodes(candidate.nodes(), errors);
		List<ValidatedConnection> connections = validateConnections(candidate.connections(), nodesById, errors);

		if (errors.isEmpty()) {
			validateConnectionSemantics(connections, errors);
		}
		if (errors.isEmpty()) {
			validateOperationalRisks(nodesById, connections, warnings, guidance);
		}
		else {
			guidance.add(issue(
					GUIDANCE,
					"FIX_STRUCTURE_FIRST",
					"ARCHITECTURE",
					null,
					"구조 오류를 먼저 수정한 뒤 운영 위험을 다시 확인하세요.",
					"존재하지 않는 노드나 지원하지 않는 타입이 있으면 요청 경로와 운영 위험을 신뢰할 수 있게 해석하기 어렵습니다."
			));
		}

		return new UserArchitectureValidationResult(errors.isEmpty(), errors, warnings, guidance);
	}

	private static Map<String, ValidatedNode> validateNodes(List<DraftNode> nodes, List<UserArchitectureValidationIssue> errors) {
		Map<String, ValidatedNode> nodesById = new LinkedHashMap<>();
		Set<String> nodeIds = new HashSet<>();
		List<DraftNode> candidateNodes = nodes == null ? List.of() : nodes;
		for (DraftNode node : candidateNodes) {
			if (node == null) {
				errors.add(issue(ERROR, "NULL_NODE", "NODE", null, "노드 항목은 null일 수 없습니다.", "검증 대상 노드는 안정적인 id와 지원 리소스 타입을 가져야 합니다."));
				continue;
			}
			if (isBlank(node.id())) {
				errors.add(issue(ERROR, "BLANK_NODE_ID", "NODE", null, "노드 ID는 비어 있을 수 없습니다.", "연결은 노드 ID를 참조하므로 빈 ID는 그래프 의미를 유지할 수 없습니다."));
				continue;
			}
			if (!nodeIds.add(node.id())) {
				errors.add(issue(ERROR, "DUPLICATE_NODE_ID", "NODE", node.id(), "노드 ID는 중복될 수 없습니다.", "같은 ID의 노드가 여러 개 있으면 연결 대상이 어느 리소스인지 결정할 수 없습니다."));
				continue;
			}
			UserArchitectureResourceType resourceType = parseResourceType(node.resourceType());
			if (resourceType == null) {
				errors.add(issue(ERROR, "UNSUPPORTED_RESOURCE_TYPE", "NODE", node.id(), "지원하지 않는 리소스 타입입니다.", "리소스 타입은 아키텍처 빌더 카탈로그의 key 중 하나여야 합니다."));
				continue;
			}
			nodesById.put(node.id(), new ValidatedNode(node.id(), resourceType, node.displayName()));
		}
		return nodesById;
	}

	private static List<ValidatedConnection> validateConnections(
			List<DraftConnection> connections,
			Map<String, ValidatedNode> nodesById,
			List<UserArchitectureValidationIssue> errors
	) {
		List<ValidatedConnection> validatedConnections = new ArrayList<>();
		Set<String> connectionIds = new HashSet<>();
		Set<String> connectionKeys = new HashSet<>();
		List<DraftConnection> candidateConnections = connections == null ? List.of() : connections;
		for (DraftConnection connection : candidateConnections) {
			if (connection == null) {
				errors.add(issue(ERROR, "NULL_CONNECTION", "CONNECTION", null, "연결 항목은 null일 수 없습니다.", "검증 대상 연결은 안정적인 id와 source, target, 연결 타입을 가져야 합니다."));
				continue;
			}
			if (isBlank(connection.id())) {
				errors.add(issue(ERROR, "BLANK_CONNECTION_ID", "CONNECTION", null, "연결 ID는 비어 있을 수 없습니다.", "연결별 검증 결과를 안정적으로 표시하려면 연결 ID가 필요합니다."));
				continue;
			}
			if (!connectionIds.add(connection.id())) {
				errors.add(issue(ERROR, "DUPLICATE_CONNECTION_ID", "CONNECTION", connection.id(), "연결 ID는 중복될 수 없습니다.", "같은 ID의 연결이 여러 개 있으면 어떤 연결이 문제인지 표시할 수 없습니다."));
				continue;
			}
			UserArchitectureConnectionType connectionType = parseConnectionType(connection.connectionType());
			if (connectionType == null) {
				errors.add(issue(ERROR, "UNSUPPORTED_CONNECTION_TYPE", "CONNECTION", connection.id(), "지원하지 않는 연결 타입입니다.", "연결 타입은 아키텍처 빌더 카탈로그의 key 중 하나여야 합니다."));
				continue;
			}
			if (isBlank(connection.sourceNodeId()) || !nodesById.containsKey(connection.sourceNodeId())) {
				errors.add(issue(ERROR, "MISSING_CONNECTION_SOURCE", "CONNECTION", connection.id(), "연결 source가 존재하는 노드를 참조해야 합니다.", "연결의 sourceNodeId는 같은 아키텍처 안의 기존 노드 ID여야 합니다."));
				continue;
			}
			if (isBlank(connection.targetNodeId()) || !nodesById.containsKey(connection.targetNodeId())) {
				errors.add(issue(ERROR, "MISSING_CONNECTION_TARGET", "CONNECTION", connection.id(), "연결 target이 존재하는 노드를 참조해야 합니다.", "연결의 targetNodeId는 같은 아키텍처 안의 기존 노드 ID여야 합니다."));
				continue;
			}
			if (connection.sourceNodeId().equals(connection.targetNodeId())) {
				errors.add(issue(ERROR, "SELF_LOOP_CONNECTION", "CONNECTION", connection.id(), "연결 source와 target은 같을 수 없습니다.", "self-loop는 현재 학습용 아키텍처에서 의미 있는 리소스 간 관계를 표현하지 못합니다."));
				continue;
			}
			String connectionKey = connection.sourceNodeId() + "->" + connection.targetNodeId() + "::" + connectionType.name();
			if (!connectionKeys.add(connectionKey)) {
				errors.add(issue(ERROR, "DUPLICATE_CONNECTION", "CONNECTION", connection.id(), "동일한 source, target, 타입의 연결은 중복될 수 없습니다.", "중복 연결은 같은 구조를 반복해서 표시해 검증 결과와 시각화를 혼동하게 합니다."));
				continue;
			}
			validatedConnections.add(new ValidatedConnection(
					connection.id(),
					nodesById.get(connection.sourceNodeId()),
					nodesById.get(connection.targetNodeId()),
					connectionType
			));
		}
		return validatedConnections;
	}

	private static void validateConnectionSemantics(List<ValidatedConnection> connections, List<UserArchitectureValidationIssue> errors) {
		for (ValidatedConnection connection : connections) {
			UserArchitectureResourceType sourceType = connection.source().resourceType();
			UserArchitectureResourceType targetType = connection.target().resourceType();
			switch (connection.connectionType()) {
				case REPLICATION -> {
					if (!DATA_STORES.contains(sourceType) || !DATA_STORES.contains(targetType)) {
						errors.add(issue(ERROR, "INVALID_REPLICATION_CONNECTION", "CONNECTION", connection.id(), "복제 연결은 데이터 저장소 사이에서만 사용할 수 있습니다.", "현재 카탈로그에서 REPLICATION은 RDS, standby, replica 같은 데이터 복제 관계를 표현합니다."));
					}
				}
				case SECURITY_RULE -> {
					if (sourceType != UserArchitectureResourceType.SECURITY_GROUP && targetType != UserArchitectureResourceType.SECURITY_GROUP) {
						errors.add(issue(ERROR, "INVALID_SECURITY_RULE_CONNECTION", "CONNECTION", connection.id(), "보안 규칙 연결은 Security Group을 포함해야 합니다.", "SECURITY_RULE은 접근 허용 경계를 표현하므로 최소 한쪽 끝은 SECURITY_GROUP이어야 합니다."));
					}
				}
				case NETWORK_ROUTE -> {
					if (!NETWORK_RESOURCES.contains(sourceType) || !NETWORK_RESOURCES.contains(targetType)) {
						errors.add(issue(ERROR, "INVALID_NETWORK_ROUTE_CONNECTION", "CONNECTION", connection.id(), "네트워크 라우팅 연결은 네트워크 경계 리소스 사이에서만 사용합니다.", "NETWORK_ROUTE은 VPC, subnet, gateway, 외부 서비스 사이의 라우팅 경로를 표현합니다."));
					}
				}
				case REQUEST_FLOW -> {
					if (REQUEST_FLOW_BOUNDARIES.contains(sourceType) || REQUEST_FLOW_BOUNDARIES.contains(targetType)) {
						errors.add(issue(ERROR, "INVALID_REQUEST_FLOW_CONNECTION", "CONNECTION", connection.id(), "요청 흐름 연결은 네트워크/보안 경계를 직접 통과하는 타입으로 사용하지 않습니다.", "VPC, subnet, gateway, Security Group 관계는 NETWORK_ROUTE 또는 SECURITY_RULE로 분리해야 학습 의미가 명확합니다."));
					}
				}
				case DEPENDS_ON -> {
				}
			}
		}
	}

	private static void validateOperationalRisks(
			Map<String, ValidatedNode> nodesById,
			List<ValidatedConnection> connections,
			List<UserArchitectureValidationIssue> warnings,
			List<UserArchitectureValidationIssue> guidance
	) {
		warnClientToRds(connections, warnings);
		warnPrivateSubnetRisks(nodesById, connections, warnings);
		warnAlbWithoutTarget(nodesById, connections, warnings);
		warnApplicationWithoutDataPath(nodesById, connections, warnings);
		addLearningGuidance(nodesById, guidance);
	}

	private static void warnClientToRds(List<ValidatedConnection> connections, List<UserArchitectureValidationIssue> warnings) {
		for (ValidatedConnection connection : connections) {
			if (connection.connectionType() == UserArchitectureConnectionType.REQUEST_FLOW
					&& connection.source().resourceType() == UserArchitectureResourceType.CLIENT
					&& connection.target().resourceType() == UserArchitectureResourceType.RDS) {
				warnings.add(issue(WARNING, "CLIENT_DIRECT_RDS_ACCESS", "CONNECTION", connection.id(), "외부 Client가 RDS에 직접 연결되어 있습니다.", "RDS는 일반적으로 애플리케이션 계층과 보안 경계를 통해 접근하도록 분리해야 합니다."));
			}
		}
	}

	private static void warnPrivateSubnetRisks(
			Map<String, ValidatedNode> nodesById,
			List<ValidatedConnection> connections,
			List<UserArchitectureValidationIssue> warnings
	) {
		List<ValidatedNode> privateSubnets = nodesById.values().stream()
				.filter(node -> node.resourceType() == UserArchitectureResourceType.SUBNET)
				.filter(UserArchitectureValidator::isPrivateSubnet)
				.toList();
		for (ValidatedNode privateSubnet : privateSubnets) {
			boolean hasOutboundRoute = connections.stream()
					.anyMatch(connection -> connection.connectionType() == UserArchitectureConnectionType.NETWORK_ROUTE
							&& connection.source().id().equals(privateSubnet.id())
							&& connection.target().resourceType() == UserArchitectureResourceType.NAT_GATEWAY);
			if (!hasOutboundRoute) {
				warnings.add(issue(WARNING, "PRIVATE_SUBNET_OUTBOUND_MISSING", "NODE", privateSubnet.id(), "Private subnet의 아웃바운드 경로가 보이지 않습니다.", "외부 API 호출이나 패키지 업데이트가 필요하다면 NAT Gateway 같은 제한된 아웃바운드 경로를 검토해야 합니다."));
			}
			boolean containsApplication = connections.stream()
					.anyMatch(connection -> touches(connection, privateSubnet)
							&& APPLICATION_RESOURCES.contains(other(connection, privateSubnet).resourceType()));
			boolean hasAlbEntry = containsApplication && connections.stream()
					.anyMatch(connection -> connection.connectionType() == UserArchitectureConnectionType.REQUEST_FLOW
							&& connection.source().resourceType() == UserArchitectureResourceType.ALB
							&& (APPLICATION_RESOURCES.contains(connection.target().resourceType())
									|| connection.target().resourceType() == UserArchitectureResourceType.TARGET_GROUP
									|| connection.target().id().equals(privateSubnet.id())));
			if (containsApplication && !hasAlbEntry) {
				warnings.add(issue(WARNING, "PRIVATE_SUBNET_ENTRY_MISSING", "NODE", privateSubnet.id(), "Private subnet 애플리케이션의 진입 경로가 보이지 않습니다.", "외부 요청을 받아야 하는 서버라면 ALB 같은 공개 진입점과 내부 전달 경로를 분리해야 합니다."));
			}
		}
	}

	private static void warnAlbWithoutTarget(
			Map<String, ValidatedNode> nodesById,
			List<ValidatedConnection> connections,
			List<UserArchitectureValidationIssue> warnings
	) {
		nodesById.values().stream()
				.filter(node -> node.resourceType() == UserArchitectureResourceType.ALB)
				.forEach(alb -> {
					boolean hasTarget = connections.stream()
							.anyMatch(connection -> connection.connectionType() == UserArchitectureConnectionType.REQUEST_FLOW
									&& connection.source().id().equals(alb.id())
									&& ALB_TARGETS.contains(connection.target().resourceType()));
					if (!hasTarget) {
						warnings.add(issue(WARNING, "ALB_TARGET_MISSING", "NODE", alb.id(), "ALB 뒤에 요청 처리 대상이 보이지 않습니다.", "ALB는 EC2, Auto Scaling Group, Target Group 같은 target으로 요청을 전달해야 의미 있는 진입점이 됩니다."));
					}
				});
	}

	private static void warnApplicationWithoutDataPath(
			Map<String, ValidatedNode> nodesById,
			List<ValidatedConnection> connections,
			List<UserArchitectureValidationIssue> warnings
	) {
		List<ValidatedNode> applications = nodesById.values().stream()
				.filter(node -> APPLICATION_RESOURCES.contains(node.resourceType()))
				.toList();
		boolean hasDataStore = nodesById.values().stream()
				.anyMatch(node -> DATA_STORES.contains(node.resourceType()));
		if (applications.isEmpty() || !hasDataStore) {
			return;
		}
		Map<String, List<String>> graph = directedGraph(connections);
		for (ValidatedNode application : applications) {
			if (!canReachDataStore(application, nodesById, graph)) {
				warnings.add(issue(WARNING, "APPLICATION_DATA_PATH_MISSING", "NODE", application.id(), "애플리케이션에서 데이터 저장소로 가는 경로가 보이지 않습니다.", "상태를 저장하거나 조회하는 서버라면 RDS, Redis, Read Replica 같은 데이터 계층으로 이어지는 명시적인 경로가 필요합니다."));
			}
		}
	}

	private static void addLearningGuidance(Map<String, ValidatedNode> nodesById, List<UserArchitectureValidationIssue> guidance) {
		boolean hasApplicationOrData = nodesById.values().stream()
				.anyMatch(node -> APPLICATION_RESOURCES.contains(node.resourceType()) || DATA_STORES.contains(node.resourceType()));
		boolean hasSecurityGroup = nodesById.values().stream()
				.anyMatch(node -> node.resourceType() == UserArchitectureResourceType.SECURITY_GROUP);
		if (hasApplicationOrData && !hasSecurityGroup) {
			guidance.add(issue(GUIDANCE, "SECURITY_BOUNDARY_REVIEW", "ARCHITECTURE", null, "Security Group 경계를 함께 검토하세요.", "현재 검증은 전체 보안 평가가 아니지만 ALB, EC2, RDS 사이 최소 허용 관계를 표시하면 학습 피드백이 명확해집니다."));
		}
	}

	private static Map<String, List<String>> directedGraph(List<ValidatedConnection> connections) {
		Map<String, List<String>> graph = new HashMap<>();
		for (ValidatedConnection connection : connections) {
			if (connection.connectionType() != UserArchitectureConnectionType.REQUEST_FLOW) {
				continue;
			}
			graph.computeIfAbsent(connection.source().id(), ignored -> new ArrayList<>())
					.add(connection.target().id());
		}
		return graph;
	}

	private static boolean canReachDataStore(
			ValidatedNode start,
			Map<String, ValidatedNode> nodesById,
			Map<String, List<String>> graph
	) {
		Set<String> visited = new HashSet<>();
		Queue<String> queue = new ArrayDeque<>();
		queue.add(start.id());
		visited.add(start.id());
		while (!queue.isEmpty()) {
			String current = queue.poll();
			ValidatedNode node = nodesById.get(current);
			if (node != null && !node.id().equals(start.id()) && DATA_STORES.contains(node.resourceType())) {
				return true;
			}
			for (String next : graph.getOrDefault(current, List.of())) {
				if (visited.add(next)) {
					queue.add(next);
				}
			}
		}
		return false;
	}

	private static boolean touches(ValidatedConnection connection, ValidatedNode node) {
		return connection.source().id().equals(node.id()) || connection.target().id().equals(node.id());
	}

	private static ValidatedNode other(ValidatedConnection connection, ValidatedNode node) {
		return connection.source().id().equals(node.id()) ? connection.target() : connection.source();
	}

	private static boolean isPrivateSubnet(ValidatedNode node) {
		String label = (node.displayName() == null ? "" : node.displayName()).toLowerCase(Locale.ROOT);
		return label.contains("private") || label.contains("프라이빗");
	}

	private static UserArchitectureResourceType parseResourceType(String value) {
		if (isBlank(value)) {
			return null;
		}
		try {
			return UserArchitectureResourceType.valueOf(value.trim());
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static UserArchitectureConnectionType parseConnectionType(String value) {
		if (isBlank(value)) {
			return null;
		}
		try {
			return UserArchitectureConnectionType.valueOf(value.trim());
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static UserArchitectureValidationIssue issue(
			UserArchitectureValidationSeverity severity,
			String code,
			String targetType,
			String targetId,
			String message,
			String reason
	) {
		return new UserArchitectureValidationIssue(severity, code, targetType, targetId, message, reason);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	public record DraftArchitecture(
			List<DraftNode> nodes,
			List<DraftConnection> connections
	) {
	}

	public record DraftNode(
			String id,
			String resourceType,
			String displayName
	) {
	}

	public record DraftConnection(
			String id,
			String sourceNodeId,
			String targetNodeId,
			String connectionType
	) {
	}

	private record ValidatedNode(
			String id,
			UserArchitectureResourceType resourceType,
			String displayName
	) {
	}

	private record ValidatedConnection(
			String id,
			ValidatedNode source,
			ValidatedNode target,
			UserArchitectureConnectionType connectionType
	) {
	}
}
