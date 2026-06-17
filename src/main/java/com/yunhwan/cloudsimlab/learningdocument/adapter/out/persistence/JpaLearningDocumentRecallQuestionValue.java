package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocumentRecallQuestion;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class JpaLearningDocumentRecallQuestionValue {

	@Column(name = "question_id", nullable = false, length = 80)
	private String id;

	@Column(nullable = false, length = 500)
	private String question;

	@Column(nullable = false, length = 700)
	private String expectedAnswer;

	@Column(nullable = false, length = 80)
	private String relatedScenarioId;

	protected JpaLearningDocumentRecallQuestionValue() {
	}

	private JpaLearningDocumentRecallQuestionValue(
			String id,
			String question,
			String expectedAnswer,
			String relatedScenarioId
	) {
		this.id = id;
		this.question = question;
		this.expectedAnswer = expectedAnswer;
		this.relatedScenarioId = relatedScenarioId;
	}

	static JpaLearningDocumentRecallQuestionValue from(LearningDocumentRecallQuestion recallQuestion) {
		return new JpaLearningDocumentRecallQuestionValue(
				recallQuestion.id(),
				recallQuestion.question(),
				recallQuestion.expectedAnswer(),
				recallQuestion.relatedScenarioId()
		);
	}

	LearningDocumentRecallQuestion toDomain() {
		return new LearningDocumentRecallQuestion(id, question, expectedAnswer, relatedScenarioId);
	}
}
