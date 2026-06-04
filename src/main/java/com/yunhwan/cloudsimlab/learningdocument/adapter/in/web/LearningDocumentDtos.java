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

	record SummaryResponse(Long id, String title, DocumentCategory category, DocumentLevel level, String summary) {
		static SummaryResponse from(LearningDocument document) {
			return new SummaryResponse(
					document.getId(),
					document.getTitle(),
					document.getCategory(),
					document.getLevel(),
					document.getSummary()
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
			List<RelatedScenarioResponse> relatedScenarios
	) {
		static DetailResponse from(LearningDocument document, List<RelatedScenario> relatedScenarios) {
			return new DetailResponse(
					document.getId(),
					document.getTitle(),
					document.getCategory(),
					document.getLevel(),
					document.getSummary(),
					document.getContent(),
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
