package com.yunhwan.cloudsimlab.architecturepractice.domain;

import java.util.List;
import java.util.Objects;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

public record ArchitecturePracticeTemplate(
		String id,
		String title,
		String description,
		ArchitecturePracticeLevel level,
		String learningGoal,
		List<String> instructions,
		List<ArchitecturePracticeNode> starterNodes,
		List<ArchitecturePracticeConnection> starterConnections,
		List<UserArchitectureResourceType> requiredResourceTypes,
		List<UserArchitectureConnectionType> requiredConnectionTypes,
		List<String> relatedDocumentIds,
		List<String> relatedScenarioIds,
		List<String> relatedModuleIds
) {
	public ArchitecturePracticeTemplate {
		id = requireText(id, "id");
		title = requireText(title, "title");
		description = requireText(description, "description");
		Objects.requireNonNull(level, "Architecture practice level must not be null");
		learningGoal = requireText(learningGoal, "learningGoal");
		instructions = List.copyOf(Objects.requireNonNull(instructions, "Architecture practice instructions must not be null"));
		starterNodes = List.copyOf(Objects.requireNonNull(starterNodes, "Architecture practice starterNodes must not be null"));
		starterConnections = List.copyOf(Objects.requireNonNull(starterConnections, "Architecture practice starterConnections must not be null"));
		requiredResourceTypes = List.copyOf(Objects.requireNonNull(requiredResourceTypes, "Architecture practice requiredResourceTypes must not be null"));
		requiredConnectionTypes = List.copyOf(Objects.requireNonNull(requiredConnectionTypes, "Architecture practice requiredConnectionTypes must not be null"));
		relatedDocumentIds = List.copyOf(Objects.requireNonNull(relatedDocumentIds, "Architecture practice relatedDocumentIds must not be null"));
		relatedScenarioIds = List.copyOf(Objects.requireNonNull(relatedScenarioIds, "Architecture practice relatedScenarioIds must not be null"));
		relatedModuleIds = List.copyOf(Objects.requireNonNull(relatedModuleIds, "Architecture practice relatedModuleIds must not be null"));
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Architecture practice " + fieldName + " must not be blank");
		}
		return value;
	}
}
