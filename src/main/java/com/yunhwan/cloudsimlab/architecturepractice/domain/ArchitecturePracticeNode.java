package com.yunhwan.cloudsimlab.architecturepractice.domain;

import java.util.Objects;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

public record ArchitecturePracticeNode(
		String id,
		UserArchitectureResourceType resourceType,
		String displayName
) {
	public ArchitecturePracticeNode {
		id = requireText(id, "node id");
		Objects.requireNonNull(resourceType, "Architecture practice node resourceType must not be null");
		displayName = requireText(displayName, "node displayName");
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Architecture practice " + fieldName + " must not be blank");
		}
		return value;
	}
}
