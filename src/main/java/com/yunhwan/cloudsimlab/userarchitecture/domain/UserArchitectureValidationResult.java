package com.yunhwan.cloudsimlab.userarchitecture.domain;

import java.util.List;

public record UserArchitectureValidationResult(
		boolean valid,
		List<UserArchitectureValidationIssue> errors,
		List<UserArchitectureValidationIssue> warnings,
		List<UserArchitectureValidationIssue> guidance
) {
	public UserArchitectureValidationResult {
		errors = immutableIssues(errors);
		warnings = immutableIssues(warnings);
		guidance = immutableIssues(guidance);
		valid = errors.isEmpty();
	}

	private static List<UserArchitectureValidationIssue> immutableIssues(List<UserArchitectureValidationIssue> issues) {
		if (issues == null) {
			return List.of();
		}
		return issues.stream()
				.map(issue -> {
					if (issue == null) {
						throw new IllegalArgumentException("validation issues must not contain null");
					}
					return issue;
				})
				.toList();
	}
}
