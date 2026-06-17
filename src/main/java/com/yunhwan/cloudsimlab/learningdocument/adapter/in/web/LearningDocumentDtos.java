package com.yunhwan.cloudsimlab.learningdocument.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocumentCheckpoint;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocumentRecallQuestion;
import com.yunhwan.cloudsimlab.learningdocument.domain.RelatedScenario;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

final class LearningDocumentDtos {

	private LearningDocumentDtos() {
	}

	record SummaryResponse(
			Long id,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			int orderIndex,
			List<String> prerequisiteDocumentIds,
			List<String> conceptTags,
			List<String> relatedScenarioIds,
			List<String> relatedModuleIds,
			int checkpointCount,
			int recallQuestionCount
	) {
		static SummaryResponse from(LearningDocument document, List<String> relatedModuleIds, List<String> relatedScenarioIds) {
			return new SummaryResponse(
					document.getId(),
					document.getTitle(),
					document.getCategory(),
					document.getLevel(),
					document.getSummary(),
					document.getOrderIndex(),
					document.getPrerequisiteDocumentIds(),
					document.getConceptTags(),
					relatedScenarioIds,
					relatedModuleIds,
					document.getCheckpoints().size(),
					document.getRecallQuestions().size()
			);
		}
	}

	record DetailResponse(
			Long id,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String content,
			int orderIndex,
			List<String> prerequisiteDocumentIds,
			List<String> conceptTags,
			List<String> relatedModuleIds,
			List<String> relatedScenarioIds,
			List<CheckpointResponse> checkpoints,
			List<RecallQuestionResponse> recallQuestions,
			List<RelatedScenarioResponse> relatedScenarios
	) {
		static DetailResponse from(
				LearningDocument document,
				List<String> relatedModuleIds,
				List<String> relatedScenarioIds,
				List<RelatedScenario> relatedScenarios
		) {
			return new DetailResponse(
					document.getId(),
					document.getTitle(),
					document.getCategory(),
					document.getLevel(),
					document.getSummary(),
					document.getContent(),
					document.getOrderIndex(),
					document.getPrerequisiteDocumentIds(),
					document.getConceptTags(),
					relatedModuleIds,
					relatedScenarioIds,
					document.getCheckpoints().stream()
							.map(CheckpointResponse::from)
							.toList(),
					document.getRecallQuestions().stream()
							.map(RecallQuestionResponse::from)
							.toList(),
					relatedScenarios.stream()
							.map(RelatedScenarioResponse::from)
							.toList()
			);
		}
	}

	record CheckpointResponse(
			String id,
			String keySentence,
			List<String> judgmentPerspectives
	) {
		static CheckpointResponse from(LearningDocumentCheckpoint checkpoint) {
			return new CheckpointResponse(
					checkpoint.id(),
					checkpoint.keySentence(),
					checkpoint.judgmentPerspectives()
			);
		}
	}

	record RecallQuestionResponse(
			String id,
			String question,
			String expectedAnswer,
			String relatedScenarioId
	) {
		static RecallQuestionResponse from(LearningDocumentRecallQuestion recallQuestion) {
			return new RecallQuestionResponse(
					recallQuestion.id(),
					recallQuestion.question(),
					recallQuestion.expectedAnswer(),
					recallQuestion.relatedScenarioId()
			);
		}
	}

	record RelatedScenarioResponse(
			Long id,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String reason
	) {
		static RelatedScenarioResponse from(RelatedScenario relatedScenario) {
			Scenario scenario = relatedScenario.scenario();
			return new RelatedScenarioResponse(
					scenario.getId(),
					scenario.getTitle(),
					scenario.getCategory(),
					scenario.getLevel(),
					scenario.getSummary(),
					relatedScenario.reason()
			);
		}
	}
}
