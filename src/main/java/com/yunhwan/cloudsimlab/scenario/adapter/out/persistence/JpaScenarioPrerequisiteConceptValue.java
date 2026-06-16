package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import com.yunhwan.cloudsimlab.scenario.domain.ScenarioPrerequisiteConcept;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class JpaScenarioPrerequisiteConceptValue {

	@Column(nullable = false, length = 80)
	private String conceptId;

	@Column(nullable = false, length = 120)
	private String displayName;

	@Column(nullable = false, length = 120)
	private String relatedDocumentId;

	@Column(nullable = false, length = 500)
	private String reason;

	protected JpaScenarioPrerequisiteConceptValue() {
	}

	private JpaScenarioPrerequisiteConceptValue(String conceptId, String displayName, String relatedDocumentId, String reason) {
		this.conceptId = conceptId;
		this.displayName = displayName;
		this.relatedDocumentId = relatedDocumentId;
		this.reason = reason;
	}

	static JpaScenarioPrerequisiteConceptValue from(ScenarioPrerequisiteConcept concept) {
		return new JpaScenarioPrerequisiteConceptValue(
				concept.conceptId(),
				concept.displayName(),
				concept.relatedDocumentId(),
				concept.reason()
		);
	}

	ScenarioPrerequisiteConcept toDomain() {
		return new ScenarioPrerequisiteConcept(conceptId, displayName, relatedDocumentId, reason);
	}
}
