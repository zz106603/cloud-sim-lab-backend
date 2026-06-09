package com.yunhwan.cloudsimlab.userarchitecture.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidationResult;

public interface ValidateUserArchitectureUseCase {

	UserArchitectureValidationResult validate(ValidateUserArchitectureCommand command);

	UserArchitectureValidationResult validateSaved(String architectureId);

	record ValidateUserArchitectureCommand(
			List<NodeCommand> nodes,
			List<ConnectionCommand> connections
	) {
	}

	record NodeCommand(
			String id,
			String resourceType,
			String displayName
	) {
	}

	record ConnectionCommand(
			String id,
			String sourceNodeId,
			String targetNodeId,
			String connectionType
	) {
	}
}
