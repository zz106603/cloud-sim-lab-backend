package com.yunhwan.cloudsimlab.userarchitecture.domain;

public record UserArchitectureValidationIssue(
		UserArchitectureValidationSeverity severity,
		String code,
		String targetType,
		String targetId,
		String message,
		String reason
) {
	public UserArchitectureValidationIssue {
		if (severity == null) {
			throw new IllegalArgumentException("validation severity must not be null");
		}
		requireText(code, "validation code");
		requireText(targetType, "validation targetType");
		requireText(message, "validation message");
		requireText(reason, "validation reason");
	}

	private static void requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
	}
}
