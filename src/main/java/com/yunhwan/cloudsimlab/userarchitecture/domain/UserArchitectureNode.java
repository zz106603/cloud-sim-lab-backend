package com.yunhwan.cloudsimlab.userarchitecture.domain;

public record UserArchitectureNode(
		String id,
		UserArchitectureResourceType resourceType,
		String displayName
) {
	public UserArchitectureNode {
		requireText(id, "node id");
		if (resourceType == null) {
			throw new IllegalArgumentException("node resourceType must not be null");
		}
		requireText(displayName, "node displayName");
	}

	private static void requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
	}
}
