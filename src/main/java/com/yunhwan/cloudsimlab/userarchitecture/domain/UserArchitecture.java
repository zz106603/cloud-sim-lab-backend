package com.yunhwan.cloudsimlab.userarchitecture.domain;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class UserArchitecture {

	private final String architectureId;
	private final String title;
	private final String description;
	private final Instant createdAt;
	private final Instant updatedAt;
	private final List<UserArchitectureNode> nodes;
	private final List<UserArchitectureConnection> connections;

	public UserArchitecture(
			String architectureId,
			String title,
			String description,
			Instant createdAt,
			Instant updatedAt,
			List<UserArchitectureNode> nodes,
			List<UserArchitectureConnection> connections
	) {
		requireText(architectureId, "architectureId");
		requireText(title, "title");
		if (createdAt == null) {
			throw new IllegalArgumentException("createdAt must not be null");
		}
		if (updatedAt == null) {
			throw new IllegalArgumentException("updatedAt must not be null");
		}
		if (updatedAt.isBefore(createdAt)) {
			throw new IllegalArgumentException("updatedAt must not be before createdAt");
		}

		List<UserArchitectureNode> copiedNodes = nodes == null ? List.of() : List.copyOf(nodes);
		List<UserArchitectureConnection> copiedConnections = connections == null ? List.of() : List.copyOf(connections);
		validateNodes(copiedNodes);
		validateConnections(copiedConnections, copiedNodes);

		this.architectureId = architectureId;
		this.title = title;
		this.description = description == null ? "" : description;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.nodes = copiedNodes.stream()
				.sorted(Comparator.comparing(UserArchitectureNode::id))
				.toList();
		this.connections = copiedConnections.stream()
				.sorted(Comparator.comparing(UserArchitectureConnection::id))
				.toList();
	}

	private static void validateNodes(List<UserArchitectureNode> nodes) {
		Set<String> nodeIds = new HashSet<>();
		for (UserArchitectureNode node : nodes) {
			if (node == null) {
				throw new IllegalArgumentException("nodes must not contain null");
			}
			if (!nodeIds.add(node.id())) {
				throw new IllegalArgumentException("node id must be unique: " + node.id());
			}
		}
	}

	private static void validateConnections(List<UserArchitectureConnection> connections, List<UserArchitectureNode> nodes) {
		Set<String> nodeIds = new HashSet<>();
		for (UserArchitectureNode node : nodes) {
			nodeIds.add(node.id());
		}

		Set<String> connectionIds = new HashSet<>();
		for (UserArchitectureConnection connection : connections) {
			if (connection == null) {
				throw new IllegalArgumentException("connections must not contain null");
			}
			if (!connectionIds.add(connection.id())) {
				throw new IllegalArgumentException("connection id must be unique: " + connection.id());
			}
			if (!nodeIds.contains(connection.sourceNodeId())) {
				throw new IllegalArgumentException("connection sourceNodeId must reference an existing node: " + connection.sourceNodeId());
			}
			if (!nodeIds.contains(connection.targetNodeId())) {
				throw new IllegalArgumentException("connection targetNodeId must reference an existing node: " + connection.targetNodeId());
			}
		}
	}

	private static void requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
	}

	public String getArchitectureId() {
		return architectureId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public List<UserArchitectureNode> getNodes() {
		return nodes;
	}

	public List<UserArchitectureConnection> getConnections() {
		return connections;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UserArchitecture that)) {
			return false;
		}
		return Objects.equals(architectureId, that.architectureId)
				&& Objects.equals(title, that.title)
				&& Objects.equals(description, that.description)
				&& Objects.equals(createdAt, that.createdAt)
				&& Objects.equals(updatedAt, that.updatedAt)
				&& Objects.equals(nodes, that.nodes)
				&& Objects.equals(connections, that.connections);
	}

	@Override
	public int hashCode() {
		return Objects.hash(architectureId, title, description, createdAt, updatedAt, nodes, connections);
	}
}
