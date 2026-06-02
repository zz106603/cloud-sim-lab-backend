package com.yunhwan.cloudsimlab.scenario.domain;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;

public record RelatedLearningDocument(
		Long id,
		String title,
		DocumentCategory category,
		DocumentLevel level,
		String summary,
		String reviewReason
) {
}
