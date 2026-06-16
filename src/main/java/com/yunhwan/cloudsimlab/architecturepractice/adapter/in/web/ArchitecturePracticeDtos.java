package com.yunhwan.cloudsimlab.architecturepractice.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeConnection;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeLevel;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeNode;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

final class ArchitecturePracticeDtos {

	private ArchitecturePracticeDtos() {
	}

	record SummaryResponse(
			String id,
			String title,
			String description,
			ArchitecturePracticeLevel level,
			String learningGoal,
			List<UserArchitectureResourceType> requiredResourceTypes,
			List<UserArchitectureConnectionType> requiredConnectionTypes,
			List<String> relatedDocumentIds,
			List<String> relatedScenarioIds,
			List<String> relatedModuleIds
	) {
		static SummaryResponse from(ArchitecturePracticeTemplate practice) {
			return new SummaryResponse(
					practice.id(),
					practice.title(),
					practice.description(),
					practice.level(),
					practice.learningGoal(),
					practice.requiredResourceTypes(),
					practice.requiredConnectionTypes(),
					practice.relatedDocumentIds(),
					practice.relatedScenarioIds(),
					practice.relatedModuleIds()
			);
		}
	}

	record DetailResponse(
			String id,
			String title,
			String description,
			ArchitecturePracticeLevel level,
			String learningGoal,
			List<String> instructions,
			List<NodeResponse> starterNodes,
			List<ConnectionResponse> starterConnections,
			List<UserArchitectureResourceType> requiredResourceTypes,
			List<UserArchitectureConnectionType> requiredConnectionTypes,
			List<String> relatedDocumentIds,
			List<String> relatedScenarioIds,
			List<String> relatedModuleIds
	) {
		static DetailResponse from(ArchitecturePracticeTemplate practice) {
			return new DetailResponse(
					practice.id(),
					practice.title(),
					practice.description(),
					practice.level(),
					practice.learningGoal(),
					practice.instructions(),
					practice.starterNodes().stream()
							.map(NodeResponse::from)
							.toList(),
					practice.starterConnections().stream()
							.map(ConnectionResponse::from)
							.toList(),
					practice.requiredResourceTypes(),
					practice.requiredConnectionTypes(),
					practice.relatedDocumentIds(),
					practice.relatedScenarioIds(),
					practice.relatedModuleIds()
			);
		}
	}

	record NodeResponse(
			String id,
			UserArchitectureResourceType resourceType,
			String displayName
	) {
		static NodeResponse from(ArchitecturePracticeNode node) {
			return new NodeResponse(node.id(), node.resourceType(), node.displayName());
		}
	}

	record ConnectionResponse(
			String id,
			String sourceNodeId,
			String targetNodeId,
			UserArchitectureConnectionType connectionType
	) {
		static ConnectionResponse from(ArchitecturePracticeConnection connection) {
			return new ConnectionResponse(
					connection.id(),
					connection.sourceNodeId(),
					connection.targetNodeId(),
					connection.connectionType()
			);
		}
	}
}
