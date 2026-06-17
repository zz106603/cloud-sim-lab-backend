package com.yunhwan.cloudsimlab.learningdiscovery.domain;

import java.util.List;
import java.util.Objects;

public record LearningDiscoveryItem(
		LearningDiscoveryResourceType resourceType,
		String id,
		String title,
		String summary,
		String category,
		String level,
		List<String> conceptTags,
		List<String> relatedDocumentIds,
		List<String> relatedScenarioIds,
		List<String> relatedModuleIds,
		List<String> relatedArchitecturePracticeIds,
		boolean recommendedPathIncluded,
		int orderIndex
) {
	public LearningDiscoveryItem {
		resourceType = Objects.requireNonNull(resourceType, "Learning discovery resourceType must not be null");
		id = requireText(id, "id");
		title = requireText(title, "title");
		summary = summary == null ? "" : summary;
		conceptTags = List.copyOf(Objects.requireNonNull(conceptTags, "Learning discovery conceptTags must not be null"));
		relatedDocumentIds = List.copyOf(Objects.requireNonNull(relatedDocumentIds, "Learning discovery relatedDocumentIds must not be null"));
		relatedScenarioIds = List.copyOf(Objects.requireNonNull(relatedScenarioIds, "Learning discovery relatedScenarioIds must not be null"));
		relatedModuleIds = List.copyOf(Objects.requireNonNull(relatedModuleIds, "Learning discovery relatedModuleIds must not be null"));
		relatedArchitecturePracticeIds = List.copyOf(Objects.requireNonNull(relatedArchitecturePracticeIds, "Learning discovery relatedArchitecturePracticeIds must not be null"));
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Learning discovery " + fieldName + " must not be blank");
		}
		return value;
	}
}
