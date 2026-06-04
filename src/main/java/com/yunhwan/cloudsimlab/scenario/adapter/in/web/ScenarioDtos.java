package com.yunhwan.cloudsimlab.scenario.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureEdge;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureGraph;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureGraphs;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureNode;
import com.yunhwan.cloudsimlab.scenario.domain.RelatedLearningDocument;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.SimulationReview;
import com.yunhwan.cloudsimlab.scenario.domain.SimulationResult;
import com.yunhwan.cloudsimlab.scenario.domain.SimulationResultType;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffSummary;

final class ScenarioDtos {

	private ScenarioDtos() {
	}

	record SummaryResponse(Long id, String title, ScenarioCategory category, ScenarioLevel level, String summary) {
		static SummaryResponse from(Scenario scenario) {
			return new SummaryResponse(
					scenario.getId(),
					scenario.getTitle(),
					scenario.getCategory(),
					scenario.getLevel(),
					scenario.getSummary()
			);
		}
	}

	record DetailResponse(
			Long id,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			String problem,
			List<String> initialArchitecture,
			ArchitectureGraphResponse initialArchitectureGraph,
			List<OptionResponse> options,
			List<RelatedLearningDocumentResponse> relatedLearningDocuments
	) {
		static DetailResponse from(Scenario scenario, List<RelatedLearningDocument> relatedLearningDocuments) {
			String problem = scenario.getDescription();
			List<RelatedLearningDocument> documents = relatedLearningDocuments == null ? List.of() : relatedLearningDocuments;
			return new DetailResponse(
					scenario.getId(),
					scenario.getTitle(),
					scenario.getCategory(),
					scenario.getLevel(),
					scenario.getSummary(),
					problem,
					problem,
					scenario.getInitialArchitecture(),
					ArchitectureGraphResponse.from(ArchitectureGraphs.initialFor(scenario)),
					scenario.getOptions().stream()
							.map(OptionResponse::from)
							.toList(),
					documents.stream()
							.map(RelatedLearningDocumentResponse::from)
							.toList()
			);
		}
	}

	record OptionResponse(Long id, String name, String description, TradeOffEffectsResponse effects) {
		static OptionResponse from(ScenarioOption option) {
			return new OptionResponse(
					option.getId(),
					option.getName(),
					option.getDescription(),
					TradeOffEffectsResponse.from(option.getEffects())
			);
		}
	}

	record SimulateRequest(List<Long> selectedOptionIds) {
	}

	record SimulationResponse(
			Long scenarioId,
			SimulationResultType resultType,
			int score,
			int riskScore,
			String summary,
			String detail,
			ReviewResponse review,
			List<OptionResponse> selectedOptions,
			TradeOffSummaryResponse tradeOffSummary,
			List<String> finalArchitecture,
			ArchitectureGraphResponse finalArchitectureGraph,
			List<RelatedLearningDocumentResponse> relatedLearningDocuments
	) {
		static SimulationResponse from(SimulationResult result) {
			return new SimulationResponse(
					result.getScenarioId(),
					result.getResultType(),
					result.getScore(),
					result.getRiskScore(),
					result.getSummary(),
					result.getDetail(),
					ReviewResponse.from(result.getReview()),
					result.getSelectedOptions().stream()
							.map(OptionResponse::from)
							.toList(),
					TradeOffSummaryResponse.from(result.getTradeOffSummary()),
					result.getFinalArchitecture(),
					ArchitectureGraphResponse.from(result.getFinalArchitectureGraph()),
					result.getRelatedLearningDocuments().stream()
							.map(RelatedLearningDocumentResponse::from)
							.toList()
			);
		}
	}

	record TradeOffEffectsResponse(
			int performance,
			int availability,
			int cost,
			int complexity,
			int consistency,
			int security
	) {
		static TradeOffEffectsResponse from(TradeOffEffects effects) {
			return new TradeOffEffectsResponse(
					effects.performance(),
					effects.availability(),
					effects.cost(),
					effects.complexity(),
					effects.consistency(),
					effects.security()
			);
		}
	}

	record TradeOffSummaryResponse(
			int performance,
			int availability,
			int cost,
			int complexity,
			int consistency,
			int security
	) {
		static TradeOffSummaryResponse from(TradeOffSummary summary) {
			return new TradeOffSummaryResponse(
					summary.performance(),
					summary.availability(),
					summary.cost(),
					summary.complexity(),
					summary.consistency(),
					summary.security()
			);
		}
	}

	record ReviewResponse(
			String reason,
			List<String> strengths,
			List<String> limitations,
			List<String> missedTradeOffs,
			String nextStep
	) {
		static ReviewResponse from(SimulationReview review) {
			return new ReviewResponse(
					review.reason(),
					review.strengths(),
					review.limitations(),
					review.missedTradeOffs(),
					review.nextStep()
			);
		}
	}

	record ArchitectureGraphResponse(List<ArchitectureNodeResponse> nodes, List<ArchitectureEdgeResponse> edges) {
		static ArchitectureGraphResponse from(ArchitectureGraph graph) {
			return new ArchitectureGraphResponse(
					graph.nodes().stream()
							.map(ArchitectureNodeResponse::from)
							.toList(),
					graph.edges().stream()
							.map(ArchitectureEdgeResponse::from)
							.toList()
			);
		}
	}

	record ArchitectureNodeResponse(String id, String label, String type, String description) {
		static ArchitectureNodeResponse from(ArchitectureNode node) {
			return new ArchitectureNodeResponse(node.id(), node.label(), node.type(), node.description());
		}
	}

	record ArchitectureEdgeResponse(String source, String target, String label) {
		static ArchitectureEdgeResponse from(ArchitectureEdge edge) {
			return new ArchitectureEdgeResponse(edge.source(), edge.target(), edge.label());
		}
	}

	record RelatedLearningDocumentResponse(
			Long id,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String reviewReason
	) {
		static RelatedLearningDocumentResponse from(RelatedLearningDocument document) {
			return new RelatedLearningDocumentResponse(
					document.id(),
					document.title(),
					document.category(),
					document.level(),
					document.summary(),
					document.reviewReason()
			);
		}
	}
}
