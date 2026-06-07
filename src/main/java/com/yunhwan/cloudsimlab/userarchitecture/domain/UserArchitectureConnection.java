package com.yunhwan.cloudsimlab.userarchitecture.domain;

public record UserArchitectureConnection(
		String id,
		String sourceNodeId,
		String targetNodeId,
		UserArchitectureConnectionType connectionType
) {
	public UserArchitectureConnection {
		requireText(id, "connection id");
		requireText(sourceNodeId, "connection sourceNodeId");
		requireText(targetNodeId, "connection targetNodeId");
		if (connectionType == null) {
			throw new IllegalArgumentException("connection connectionType must not be null");
		}
	}

	private static void requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
	}
}
