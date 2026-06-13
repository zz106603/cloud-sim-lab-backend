package com.yunhwan.cloudsimlab.learningpath.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;

final class LearningPathDtos {

	private LearningPathDtos() {
	}

	record SummaryResponse(
			String id,
			String title,
			String description,
			String targetLevel,
			String learningGoal,
			boolean recommended,
			int orderIndex,
			List<String> moduleIds
	) {
		static SummaryResponse from(LearningPath path) {
			return new SummaryResponse(
					path.id(),
					path.title(),
					path.description(),
					path.targetLevel(),
					path.learningGoal(),
					path.recommended(),
					path.orderIndex(),
					path.moduleIds()
			);
		}
	}

	record DetailResponse(
			String id,
			String title,
			String description,
			String targetLevel,
			String learningGoal,
			boolean recommended,
			int orderIndex,
			List<LearningModuleResponse> modules
	) {
		static DetailResponse from(LearningPath path, List<LearningModule> modules) {
			return new DetailResponse(
					path.id(),
					path.title(),
					path.description(),
					path.targetLevel(),
					path.learningGoal(),
					path.recommended(),
					path.orderIndex(),
					modules.stream()
							.map(LearningModuleResponse::from)
							.toList()
			);
		}
	}

	record LearningModuleResponse(
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
		static LearningModuleResponse from(LearningModule module) {
			return new LearningModuleResponse(
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
