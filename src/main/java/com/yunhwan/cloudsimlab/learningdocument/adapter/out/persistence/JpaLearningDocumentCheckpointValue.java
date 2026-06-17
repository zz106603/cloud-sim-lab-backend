package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocumentCheckpoint;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class JpaLearningDocumentCheckpointValue {

	@Column(name = "checkpoint_id", nullable = false, length = 80)
	private String id;

	@Column(nullable = false, length = 500)
	private String keySentence;

	@Column(nullable = false, length = 200)
	private String judgmentPerspectives;

	protected JpaLearningDocumentCheckpointValue() {
	}

	private JpaLearningDocumentCheckpointValue(String id, String keySentence, List<String> judgmentPerspectives) {
		this.id = id;
		this.keySentence = keySentence;
		this.judgmentPerspectives = String.join(",", judgmentPerspectives);
	}

	static JpaLearningDocumentCheckpointValue from(LearningDocumentCheckpoint checkpoint) {
		return new JpaLearningDocumentCheckpointValue(
				checkpoint.id(),
				checkpoint.keySentence(),
				checkpoint.judgmentPerspectives()
		);
	}

	LearningDocumentCheckpoint toDomain() {
		return new LearningDocumentCheckpoint(id, keySentence, split(judgmentPerspectives));
	}

	private static List<String> split(String values) {
		if (values == null || values.isBlank()) {
			return List.of();
		}
		return Arrays.stream(values.split(","))
				.map(String::trim)
				.filter(value -> !value.isBlank())
				.collect(Collectors.toUnmodifiableList());
	}
}
