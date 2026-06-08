package com.yunhwan.cloudsimlab.userarchitecture.adapter.in.web;

import java.time.Instant;
import java.util.List;

import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.ConnectionCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.CreateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.NodeCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.UpdateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnection;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureNode;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

final class UserArchitectureDtos {

	private UserArchitectureDtos() {
	}

	record SummaryResponse(
			String architectureId,
			String title,
			String description,
			Instant createdAt,
			Instant updatedAt,
			int nodeCount,
			int connectionCount
	) {
		static SummaryResponse from(UserArchitecture architecture) {
			return new SummaryResponse(
					architecture.getArchitectureId(),
					architecture.getTitle(),
					architecture.getDescription(),
					architecture.getCreatedAt(),
					architecture.getUpdatedAt(),
					architecture.getNodes().size(),
					architecture.getConnections().size()
			);
		}
	}

	record DetailResponse(
			String architectureId,
			String title,
			String description,
			Instant createdAt,
			Instant updatedAt,
			List<NodeResponse> nodes,
			List<ConnectionResponse> connections
	) {
		static DetailResponse from(UserArchitecture architecture) {
			return new DetailResponse(
					architecture.getArchitectureId(),
					architecture.getTitle(),
					architecture.getDescription(),
					architecture.getCreatedAt(),
					architecture.getUpdatedAt(),
					architecture.getNodes().stream()
							.map(NodeResponse::from)
							.toList(),
					architecture.getConnections().stream()
							.map(ConnectionResponse::from)
							.toList()
			);
		}
	}

	record NodeResponse(
			String id,
			UserArchitectureResourceType resourceType,
			String displayName
	) {
		static NodeResponse from(UserArchitectureNode node) {
			return new NodeResponse(node.id(), node.resourceType(), node.displayName());
		}
	}

	record ConnectionResponse(
			String id,
			String sourceNodeId,
			String targetNodeId,
			UserArchitectureConnectionType connectionType
	) {
		static ConnectionResponse from(UserArchitectureConnection connection) {
			return new ConnectionResponse(
					connection.id(),
					connection.sourceNodeId(),
					connection.targetNodeId(),
					connection.connectionType()
			);
		}
	}

	record SaveRequest(
			String title,
			String description,
			List<NodeRequest> nodes,
			List<ConnectionRequest> connections
	) {
		CreateUserArchitectureCommand toCreateCommand() {
			return new CreateUserArchitectureCommand(title, description, toNodeCommands(nodes), toConnectionCommands(connections));
		}

		UpdateUserArchitectureCommand toUpdateCommand() {
			return new UpdateUserArchitectureCommand(title, description, toNodeCommands(nodes), toConnectionCommands(connections));
		}
	}

	record NodeRequest(
			String id,
			UserArchitectureResourceType resourceType,
			String displayName
	) {
		NodeCommand toCommand() {
			return new NodeCommand(id, resourceType, displayName);
		}
	}

	record ConnectionRequest(
			String id,
			String sourceNodeId,
			String targetNodeId,
			UserArchitectureConnectionType connectionType
	) {
		ConnectionCommand toCommand() {
			return new ConnectionCommand(id, sourceNodeId, targetNodeId, connectionType);
		}
	}

	private static List<NodeCommand> toNodeCommands(List<NodeRequest> requests) {
		if (requests == null) {
			return List.of();
		}
		return requests.stream()
				.map(request -> request == null ? new NodeCommand(null, null, null) : request.toCommand())
				.toList();
	}

	private static List<ConnectionCommand> toConnectionCommands(List<ConnectionRequest> requests) {
		if (requests == null) {
			return List.of();
		}
		return requests.stream()
				.map(request -> request == null ? new ConnectionCommand(null, null, null, null) : request.toCommand())
				.toList();
	}
}
