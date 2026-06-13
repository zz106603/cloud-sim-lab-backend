package com.yunhwan.cloudsimlab.learningmodule.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;

final class LearningModuleDtos {

	private LearningModuleDtos() {
	}

	record Response(
			String id,
			String pathId,
			String title,
			String description,
			List<String> learningGoals,
			List<String> prerequisites,
			int orderIndex,
			List<String> documentIds,
			List<String> relatedScenarioIds,
			List<String> relatedArchitecturePracticeIds
	) {
		static Response from(LearningModule module) {
			return new Response(
					module.id(),
					module.pathId(),
					module.title(),
					module.description(),
					module.learningGoals(),
					module.prerequisites(),
					module.orderIndex(),
					module.documentIds(),
					module.relatedScenarioIds(),
					module.relatedArchitecturePracticeIds()
			);
		}
	}
}
