package com.yunhwan.cloudsimlab.learningpath.domain;

import java.util.List;
import java.util.Objects;

public record LearningPath(
		String id,
		String title,
		String description,
		String targetLevel,
		String learningGoal,
		boolean recommended,
		int orderIndex,
		List<String> moduleIds
) {
	public LearningPath {
		id = requireText(id, "id");
		title = requireText(title, "title");
		description = requireText(description, "description");
		targetLevel = requireText(targetLevel, "targetLevel");
		learningGoal = requireText(learningGoal, "learningGoal");
		moduleIds = List.copyOf(Objects.requireNonNull(moduleIds, "Learning path moduleIds must not be null"));
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Learning path " + fieldName + " must not be blank");
		}
		return value;
	}
}
