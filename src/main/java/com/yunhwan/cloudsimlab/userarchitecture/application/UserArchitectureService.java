package com.yunhwan.cloudsimlab.userarchitecture.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.userarchitecture.application.port.UserArchitectureCommandPort;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.UserArchitectureQueryPort;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.GetUserArchitectureUseCase;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.ConnectionCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.CreateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.NodeCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.UpdateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnection;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureNode;

@Service
@Transactional(readOnly = true)
public class UserArchitectureService implements GetUserArchitectureUseCase, ManageUserArchitectureUseCase {

	private final UserArchitectureQueryPort queryPort;
	private final UserArchitectureCommandPort commandPort;
	private final Clock clock;

	@Autowired
	public UserArchitectureService(UserArchitectureQueryPort queryPort, UserArchitectureCommandPort commandPort) {
		this(queryPort, commandPort, Clock.systemUTC());
	}

	UserArchitectureService(UserArchitectureQueryPort queryPort, UserArchitectureCommandPort commandPort, Clock clock) {
		this.queryPort = queryPort;
		this.commandPort = commandPort;
		this.clock = clock;
	}

	@Override
	public List<UserArchitecture> findAll() {
		return queryPort.findAll();
	}

	@Override
	public UserArchitecture findOne(String architectureId) {
		return queryPort.findById(architectureId)
				.orElseThrow(() -> new UserArchitectureNotFoundException(architectureId));
	}

	@Override
	@Transactional
	public UserArchitecture create(CreateUserArchitectureCommand command) {
		if (command == null) {
			throw new InvalidUserArchitectureRequestException("request body must not be null");
		}
		Instant now = Instant.now(clock);
		return commandPort.save(toDomain(UUID.randomUUID().toString(), command.title(), command.description(), now, now,
				command.nodes(), command.connections()));
	}

	@Override
	@Transactional
	public UserArchitecture update(String architectureId, UpdateUserArchitectureCommand command) {
		if (command == null) {
			throw new InvalidUserArchitectureRequestException("request body must not be null");
		}
		UserArchitecture current = findOne(architectureId);
		Instant now = Instant.now(clock);
		return commandPort.save(toDomain(current.getArchitectureId(), command.title(), command.description(), current.getCreatedAt(), now,
				command.nodes(), command.connections()));
	}

	@Override
	@Transactional
	public void delete(String architectureId) {
		if (!commandPort.existsById(architectureId)) {
			throw new UserArchitectureNotFoundException(architectureId);
		}
		commandPort.deleteById(architectureId);
	}

	private UserArchitecture toDomain(
			String architectureId,
			String title,
			String description,
			Instant createdAt,
			Instant updatedAt,
			List<NodeCommand> nodeCommands,
			List<ConnectionCommand> connectionCommands
	) {
		try {
			return new UserArchitecture(
					architectureId,
					title,
					description,
					createdAt,
					updatedAt,
					toNodes(nodeCommands),
					toConnections(connectionCommands)
			);
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidUserArchitectureRequestException(ex.getMessage());
		}
	}

	private List<UserArchitectureNode> toNodes(List<NodeCommand> commands) {
		if (commands == null) {
			return List.of();
		}
		return commands.stream()
				.map(this::toNode)
				.toList();
	}

	private List<UserArchitectureConnection> toConnections(List<ConnectionCommand> commands) {
		if (commands == null) {
			return List.of();
		}
		return commands.stream()
				.map(this::toConnection)
				.toList();
	}

	private UserArchitectureNode toNode(NodeCommand command) {
		if (command == null) {
			throw new InvalidUserArchitectureRequestException("nodes must not contain null");
		}
		return new UserArchitectureNode(command.id(), command.resourceType(), command.displayName());
	}

	private UserArchitectureConnection toConnection(ConnectionCommand command) {
		if (command == null) {
			throw new InvalidUserArchitectureRequestException("connections must not contain null");
		}
		return new UserArchitectureConnection(
				command.id(),
				command.sourceNodeId(),
				command.targetNodeId(),
				command.connectionType()
		);
	}
}
