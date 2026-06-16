package com.yunhwan.cloudsimlab.architecturepractice.domain;

import java.util.Objects;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;

public record ArchitecturePracticeConnection(
		String id,
		String sourceNodeId,
		String targetNodeId,
		UserArchitectureConnectionType connectionType
) {
	public ArchitecturePracticeConnection {
		id = requireText(id, "connection id");
		sourceNodeId = requireText(sourceNodeId, "connection sourceNodeId");
		targetNodeId = requireText(targetNodeId, "connection targetNodeId");
		Objects.requireNonNull(connectionType, "Architecture practice connection connectionType must not be null");
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Architecture practice " + fieldName + " must not be blank");
		}
		return value;
	}
}
