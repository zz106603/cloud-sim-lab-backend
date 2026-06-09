package com.yunhwan.cloudsimlab.userarchitecture.adapter.in.web;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.ConnectionCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.CreateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.NodeCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.UpdateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ValidateUserArchitectureUseCase;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ValidateUserArchitectureUseCase.ValidateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnection;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureNode;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidationIssue;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidationResult;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidationSeverity;

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

	record CatalogResponse(
			List<ResourceTypeResponse> resourceTypes,
			List<ConnectionTypeResponse> connectionTypes
	) {
		private static final CatalogResponse SUPPORTED_TYPES = new CatalogResponse(
					Arrays.stream(UserArchitectureResourceType.values())
							.map(ResourceTypeResponse::from)
							.toList(),
					Arrays.stream(UserArchitectureConnectionType.values())
							.map(ConnectionTypeResponse::from)
							.toList()
			);

		static CatalogResponse supportedTypes() {
			return SUPPORTED_TYPES;
		}
	}

	record ResourceTypeResponse(
			String key,
			String displayName,
			String description,
			String visualizationCategory,
			String learningPurpose
	) {
		static ResourceTypeResponse from(UserArchitectureResourceType resourceType) {
			return new ResourceTypeResponse(
					resourceType.getKey(),
					resourceType.getDisplayName(),
					resourceType.getDescription(),
					resourceType.getVisualizationCategory(),
					resourceType.getLearningPurpose()
			);
		}
	}

	record ConnectionTypeResponse(
			String key,
			String displayName,
			String meaning
	) {
		static ConnectionTypeResponse from(UserArchitectureConnectionType connectionType) {
			return new ConnectionTypeResponse(
					connectionType.getKey(),
					connectionType.getDisplayName(),
					connectionType.getMeaning()
			);
		}
	}

	record ValidationResponse(
			boolean valid,
			List<ValidationIssueResponse> errors,
			List<ValidationIssueResponse> warnings,
			List<ValidationIssueResponse> guidance
	) {
		static ValidationResponse from(UserArchitectureValidationResult result) {
			return new ValidationResponse(
					result.valid(),
					result.errors().stream()
							.map(ValidationIssueResponse::from)
							.toList(),
					result.warnings().stream()
							.map(ValidationIssueResponse::from)
							.toList(),
					result.guidance().stream()
							.map(ValidationIssueResponse::from)
							.toList()
			);
		}
	}

	record ValidationIssueResponse(
			UserArchitectureValidationSeverity severity,
			String code,
			String targetType,
			String targetId,
			String message,
			String reason
	) {
		static ValidationIssueResponse from(UserArchitectureValidationIssue issue) {
			return new ValidationIssueResponse(
					issue.severity(),
					issue.code(),
					issue.targetType(),
					issue.targetId(),
					issue.message(),
					issue.reason()
			);
		}
	}

	record ValidationRequest(
			List<ValidationNodeRequest> nodes,
			List<ValidationConnectionRequest> connections
	) {
		ValidateUserArchitectureCommand toCommand() {
			return new ValidateUserArchitectureCommand(
					toValidationNodeCommands(nodes),
					toValidationConnectionCommands(connections)
			);
		}
	}

	record ValidationNodeRequest(
			String id,
			String resourceType,
			String displayName
	) {
		ValidateUserArchitectureUseCase.NodeCommand toCommand() {
			return new ValidateUserArchitectureUseCase.NodeCommand(
					id,
					resourceType,
					displayName
			);
		}
	}

	record ValidationConnectionRequest(
			String id,
			String sourceNodeId,
			String targetNodeId,
			String connectionType
	) {
		ValidateUserArchitectureUseCase.ConnectionCommand toCommand() {
			return new ValidateUserArchitectureUseCase.ConnectionCommand(
					id,
					sourceNodeId,
					targetNodeId,
					connectionType
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
				.map(request -> request == null ? null : request.toCommand())
				.toList();
	}

	private static List<ConnectionCommand> toConnectionCommands(List<ConnectionRequest> requests) {
		if (requests == null) {
			return List.of();
		}
		return requests.stream()
				.map(request -> request == null ? null : request.toCommand())
				.toList();
	}

	private static List<ValidateUserArchitectureUseCase.NodeCommand> toValidationNodeCommands(
			List<ValidationNodeRequest> requests
	) {
		if (requests == null) {
			return List.of();
		}
		return requests.stream()
				.map(request -> request == null ? null : request.toCommand())
				.toList();
	}

	private static List<ValidateUserArchitectureUseCase.ConnectionCommand> toValidationConnectionCommands(
			List<ValidationConnectionRequest> requests
	) {
		if (requests == null) {
			return List.of();
		}
		return requests.stream()
				.map(request -> request == null ? null : request.toCommand())
				.toList();
	}
}
