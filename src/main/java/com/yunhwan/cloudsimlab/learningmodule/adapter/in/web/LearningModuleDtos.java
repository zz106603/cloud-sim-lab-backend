package com.yunhwan.cloudsimlab.learningmodule.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModulePracticeActivity;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModulePracticeActivityType;

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
			List<String> relatedArchitecturePracticeIds,
			List<PracticeActivityResponse> practiceActivities
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
					module.relatedArchitecturePracticeIds(),
					module.practiceActivities().stream()
							.map(PracticeActivityResponse::from)
							.toList()
			);
		}
	}

	record PracticeActivityResponse(
			String id,
			LearningModulePracticeActivityType type,
			String title,
			String description,
			String targetResourceId,
			int recommendedOrder
	) {
		static PracticeActivityResponse from(LearningModulePracticeActivity activity) {
			return new PracticeActivityResponse(
					activity.id(),
					activity.type(),
					activity.title(),
					activity.description(),
					activity.targetResourceId(),
					activity.recommendedOrder()
			);
		}
	}
}
