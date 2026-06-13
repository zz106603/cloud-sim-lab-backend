package com.yunhwan.cloudsimlab.learningmodule.domain;

import java.util.List;
import java.util.Objects;

public record LearningModule(
		String id,
		String pathId,
		String title,
		String description,
		List<String> learningGoals,
		List<String> prerequisites,
		int orderIndex,
		List<String> documentIds,
		List<String> relatedScenarioIds,
		List<String> relatedArchitecturePracticeIds
) {
	public LearningModule {
		id = requireText(id, "id");
		pathId = requireText(pathId, "pathId");
		title = requireText(title, "title");
		description = requireText(description, "description");
		learningGoals = List.copyOf(Objects.requireNonNull(learningGoals, "Learning module learningGoals must not be null"));
		prerequisites = List.copyOf(Objects.requireNonNull(prerequisites, "Learning module prerequisites must not be null"));
		documentIds = List.copyOf(Objects.requireNonNull(documentIds, "Learning module documentIds must not be null"));
		relatedScenarioIds = List.copyOf(Objects.requireNonNull(relatedScenarioIds, "Learning module relatedScenarioIds must not be null"));
		relatedArchitecturePracticeIds = List.copyOf(Objects.requireNonNull(relatedArchitecturePracticeIds, "Learning module relatedArchitecturePracticeIds must not be null"));
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Learning module " + fieldName + " must not be blank");
		}
		return value;
	}
}
