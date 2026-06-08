package com.yunhwan.cloudsimlab.userarchitecture.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

public interface ManageUserArchitectureUseCase {

	UserArchitecture create(CreateUserArchitectureCommand command);

	UserArchitecture update(String architectureId, UpdateUserArchitectureCommand command);

	void delete(String architectureId);

	record CreateUserArchitectureCommand(
			String title,
			String description,
			List<NodeCommand> nodes,
			List<ConnectionCommand> connections
	) {
	}

	record UpdateUserArchitectureCommand(
			String title,
			String description,
			List<NodeCommand> nodes,
			List<ConnectionCommand> connections
	) {
	}

	record NodeCommand(
			String id,
			UserArchitectureResourceType resourceType,
			String displayName
	) {
	}

	record ConnectionCommand(
			String id,
			String sourceNodeId,
			String targetNodeId,
			UserArchitectureConnectionType connectionType
	) {
	}
}
