package com.yunhwan.cloudsimlab.learningdocument.domain;

import java.util.List;
import java.util.Objects;

public record LearningDocumentCheckpoint(
		String id,
		String keySentence,
		List<String> judgmentPerspectives
) {

	public LearningDocumentCheckpoint {
		judgmentPerspectives = List.copyOf(Objects.requireNonNull(judgmentPerspectives));
	}
}
