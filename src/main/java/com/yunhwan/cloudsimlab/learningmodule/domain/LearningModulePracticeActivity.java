package com.yunhwan.cloudsimlab.learningmodule.domain;

import java.util.Objects;

public record LearningModulePracticeActivity(
		String id,
		LearningModulePracticeActivityType type,
		String title,
		String description,
		String targetResourceId,
		int recommendedOrder
) {
	public LearningModulePracticeActivity {
		id = requireText(id, "id");
		type = Objects.requireNonNull(type, "Learning module practice activity type must not be null");
		title = requireText(title, "title");
		description = requireText(description, "description");
		targetResourceId = requireText(targetResourceId, "targetResourceId");
		if (recommendedOrder < 1) {
			throw new IllegalArgumentException("Learning module practice activity recommendedOrder must be greater than 0");
		}
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Learning module practice activity " + fieldName + " must not be blank");
		}
		return value;
	}
}
