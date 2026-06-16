package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.Objects;

public record ScenarioPrerequisiteConcept(
		String conceptId,
		String displayName,
		String relatedDocumentId,
		String reason
) {

	public ScenarioPrerequisiteConcept {
		conceptId = requireText(conceptId, "conceptId");
		displayName = requireText(displayName, "displayName");
		relatedDocumentId = requireText(relatedDocumentId, "relatedDocumentId");
		reason = requireText(reason, "reason");
	}

	private static String requireText(String value, String fieldName) {
		if (!Objects.requireNonNull(value, fieldName + " must not be null").isBlank()) {
			return value;
		}
		throw new IllegalArgumentException(fieldName + " must not be blank");
	}
}
