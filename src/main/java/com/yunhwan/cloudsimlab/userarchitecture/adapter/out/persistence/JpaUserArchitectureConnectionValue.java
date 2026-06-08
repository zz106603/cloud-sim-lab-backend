package com.yunhwan.cloudsimlab.userarchitecture.adapter.out.persistence;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnection;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
class JpaUserArchitectureConnectionValue {

	@Column(name = "connection_id", nullable = false, length = 80)
	private String connectionId;

	@Column(nullable = false, length = 80)
	private String sourceNodeId;

	@Column(nullable = false, length = 80)
	private String targetNodeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private UserArchitectureConnectionType connectionType;

	protected JpaUserArchitectureConnectionValue() {
	}

	private JpaUserArchitectureConnectionValue(
			String connectionId,
			String sourceNodeId,
			String targetNodeId,
			UserArchitectureConnectionType connectionType
	) {
		this.connectionId = connectionId;
		this.sourceNodeId = sourceNodeId;
		this.targetNodeId = targetNodeId;
		this.connectionType = connectionType;
	}

	static JpaUserArchitectureConnectionValue from(UserArchitectureConnection connection) {
		return new JpaUserArchitectureConnectionValue(
				connection.id(),
				connection.sourceNodeId(),
				connection.targetNodeId(),
				connection.connectionType()
		);
	}

	UserArchitectureConnection toDomain() {
		return new UserArchitectureConnection(connectionId, sourceNodeId, targetNodeId, connectionType);
	}
}
