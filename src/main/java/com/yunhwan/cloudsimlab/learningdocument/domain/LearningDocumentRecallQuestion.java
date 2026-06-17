package com.yunhwan.cloudsimlab.learningdocument.domain;

public record LearningDocumentRecallQuestion(
		String id,
		String question,
		String expectedAnswer,
		String relatedScenarioId
) {
}
