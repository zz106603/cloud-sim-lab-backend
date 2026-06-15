package com.yunhwan.cloudsimlab.learningdocument.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
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
			List<String> relatedModuleIds
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
					relatedModuleIds
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
					relatedScenarios.stream()
							.map(RelatedScenarioResponse::from)
							.toList()
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
