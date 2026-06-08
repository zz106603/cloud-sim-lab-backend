package com.yunhwan.cloudsimlab.userarchitecture.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_architecture")
class JpaUserArchitectureEntity {

	@Id
	@Column(length = 36)
	private String architectureId;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@ElementCollection
	@CollectionTable(name = "user_architecture_node", joinColumns = @JoinColumn(name = "architecture_id"))
	@OrderColumn(name = "order_index")
	private List<JpaUserArchitectureNodeValue> nodes = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "user_architecture_connection", joinColumns = @JoinColumn(name = "architecture_id"))
	@OrderColumn(name = "order_index")
	private List<JpaUserArchitectureConnectionValue> connections = new ArrayList<>();

	protected JpaUserArchitectureEntity() {
	}

	private JpaUserArchitectureEntity(
			String architectureId,
			String title,
			String description,
			Instant createdAt,
			Instant updatedAt,
			List<JpaUserArchitectureNodeValue> nodes,
			List<JpaUserArchitectureConnectionValue> connections
	) {
		this.architectureId = architectureId;
		this.title = title;
		this.description = description;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.nodes.addAll(nodes);
		this.connections.addAll(connections);
	}

	static JpaUserArchitectureEntity from(UserArchitecture architecture) {
		return new JpaUserArchitectureEntity(
				architecture.getArchitectureId(),
				architecture.getTitle(),
				architecture.getDescription(),
				architecture.getCreatedAt(),
				architecture.getUpdatedAt(),
				architecture.getNodes().stream()
						.map(JpaUserArchitectureNodeValue::from)
						.toList(),
				architecture.getConnections().stream()
						.map(JpaUserArchitectureConnectionValue::from)
						.toList()
		);
	}

	UserArchitecture toDomain() {
		return new UserArchitecture(
				architectureId,
				title,
				description,
				createdAt,
				updatedAt,
				nodes.stream()
						.map(JpaUserArchitectureNodeValue::toDomain)
						.toList(),
				connections.stream()
						.map(JpaUserArchitectureConnectionValue::toDomain)
						.toList()
		);
	}
}
