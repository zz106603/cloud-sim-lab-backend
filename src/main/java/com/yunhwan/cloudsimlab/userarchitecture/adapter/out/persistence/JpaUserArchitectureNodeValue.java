package com.yunhwan.cloudsimlab.userarchitecture.adapter.out.persistence;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureNode;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
class JpaUserArchitectureNodeValue {

	@Column(name = "node_id", nullable = false, length = 80)
	private String nodeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private UserArchitectureResourceType resourceType;

	@Column(nullable = false, length = 120)
	private String displayName;

	protected JpaUserArchitectureNodeValue() {
	}

	private JpaUserArchitectureNodeValue(String nodeId, UserArchitectureResourceType resourceType, String displayName) {
		this.nodeId = nodeId;
		this.resourceType = resourceType;
		this.displayName = displayName;
	}

	static JpaUserArchitectureNodeValue from(UserArchitectureNode node) {
		return new JpaUserArchitectureNodeValue(node.id(), node.resourceType(), node.displayName());
	}

	UserArchitectureNode toDomain() {
		return new UserArchitectureNode(nodeId, resourceType, displayName);
	}
}
