package com.yunhwan.cloudsimlab.userarchitecture.domain;

import java.util.List;

import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

public record UserArchitectureComparisonResult(
		ComparisonSummary base,
		ComparisonSummary target,
		ResourceComparison resources,
		ConnectionComparison connections,
		ScenarioComparison scenarioComparison,
		List<TradeOffReference> tradeOffReferences
) {
	public UserArchitectureComparisonResult {
		resources = resources == null ? ResourceComparison.empty() : resources;
		connections = connections == null ? ConnectionComparison.empty() : connections;
		tradeOffReferences = tradeOffReferences == null ? List.of() : List.copyOf(tradeOffReferences);
	}

	public record ComparisonSummary(
			String comparisonType,
			String id,
			String title,
			int resourceCount,
			int connectionCount
	) {
	}

	public record ResourceComparison(
			List<ResourceChange> added,
			List<ResourceChange> removed,
			List<ResourceChange> changed,
			List<ResourceChange> unchanged
	) {
		public ResourceComparison {
			added = added == null ? List.of() : List.copyOf(added);
			removed = removed == null ? List.of() : List.copyOf(removed);
			changed = changed == null ? List.of() : List.copyOf(changed);
			unchanged = unchanged == null ? List.of() : List.copyOf(unchanged);
		}

		static ResourceComparison empty() {
			return new ResourceComparison(List.of(), List.of(), List.of(), List.of());
		}
	}

	public record ResourceChange(
			String changeType,
			String resourceKey,
			String resourceId,
			UserArchitectureResourceType baseResourceType,
			String baseDisplayName,
			UserArchitectureResourceType targetResourceType,
			String targetDisplayName,
			String reason
	) {
	}

	public record ConnectionComparison(
			List<ConnectionChange> added,
			List<ConnectionChange> removed,
			List<ConnectionChange> changed,
			List<ConnectionChange> unchanged
	) {
		public ConnectionComparison {
			added = added == null ? List.of() : List.copyOf(added);
			removed = removed == null ? List.of() : List.copyOf(removed);
			changed = changed == null ? List.of() : List.copyOf(changed);
			unchanged = unchanged == null ? List.of() : List.copyOf(unchanged);
		}

		static ConnectionComparison empty() {
			return new ConnectionComparison(List.of(), List.of(), List.of(), List.of());
		}
	}

	public record ConnectionChange(
			String changeType,
			String connectionKey,
			String connectionId,
			String baseSourceNodeId,
			String baseTargetNodeId,
			UserArchitectureConnectionType baseConnectionType,
			String targetSourceNodeId,
			String targetTargetNodeId,
			UserArchitectureConnectionType targetConnectionType,
			String reason
	) {
	}

	public record ScenarioComparison(
			Long scenarioId,
			String scenarioTitle,
			String learningGoal,
			List<ResourceChange> missingRecommendedResources,
			List<ResourceChange> extraResources,
			List<LearningImpact> learningImpacts
	) {
		public ScenarioComparison {
			missingRecommendedResources = missingRecommendedResources == null ? List.of() : List.copyOf(missingRecommendedResources);
			extraResources = extraResources == null ? List.of() : List.copyOf(extraResources);
			learningImpacts = learningImpacts == null ? List.of() : List.copyOf(learningImpacts);
		}
	}

	public record LearningImpact(
			String code,
			String targetKey,
			String message,
			String reason
	) {
	}

	public record TradeOffReference(
			String optionName,
			String reason,
			TradeOffEffects effects
	) {
	}
}
