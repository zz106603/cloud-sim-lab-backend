package com.yunhwan.cloudsimlab.userarchitecture.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.scenario.application.ScenarioNotFoundException;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.UserArchitectureCommandPort;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.UserArchitectureQueryPort;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.CompareUserArchitectureUseCase;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.GetUserArchitectureUseCase;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ValidateUserArchitectureUseCase;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ValidateUserArchitectureUseCase.ValidateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparator;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnection;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureNode;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidationResult;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidator;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidator.DraftArchitecture;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidator.DraftConnection;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidator.DraftNode;

@Service
@Transactional(readOnly = true)
public class UserArchitectureService implements GetUserArchitectureUseCase, ManageUserArchitectureUseCase, ValidateUserArchitectureUseCase, CompareUserArchitectureUseCase {

	private final UserArchitectureQueryPort queryPort;
	private final UserArchitectureCommandPort commandPort;
	private final ScenarioQueryPort scenarioQueryPort;
	private final Clock clock;

	@Autowired
	public UserArchitectureService(
			UserArchitectureQueryPort queryPort,
			UserArchitectureCommandPort commandPort,
			ScenarioQueryPort scenarioQueryPort
	) {
		this(queryPort, commandPort, scenarioQueryPort, Clock.systemUTC());
	}

	UserArchitectureService(UserArchitectureQueryPort queryPort, UserArchitectureCommandPort commandPort, Clock clock) {
		this(queryPort, commandPort, null, clock);
	}

	UserArchitectureService(
			UserArchitectureQueryPort queryPort,
			UserArchitectureCommandPort commandPort,
			ScenarioQueryPort scenarioQueryPort,
			Clock clock
	) {
		this.queryPort = queryPort;
		this.commandPort = commandPort;
		this.scenarioQueryPort = scenarioQueryPort;
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
	public UserArchitectureValidationResult validate(ValidateUserArchitectureCommand command) {
		if (command == null) {
			throw new InvalidUserArchitectureRequestException("request body must not be null");
		}
		return UserArchitectureValidator.validate(new DraftArchitecture(
				toDraftNodes(command.nodes()),
				toDraftConnections(command.connections())
		));
	}

	@Override
	public UserArchitectureValidationResult validateSaved(String architectureId) {
		UserArchitecture architecture = findOne(architectureId);
		return UserArchitectureValidator.validate(new DraftArchitecture(
				architecture.getNodes().stream()
						.map(node -> new DraftNode(node.id(), node.resourceType().name(), node.displayName()))
						.toList(),
				architecture.getConnections().stream()
						.map(connection -> new DraftConnection(
								connection.id(),
								connection.sourceNodeId(),
								connection.targetNodeId(),
								connection.connectionType().name()
						))
						.toList()
		));
	}

	@Override
	public UserArchitectureComparisonResult compareSaved(String baseArchitectureId, String targetArchitectureId) {
		if (baseArchitectureId == null || baseArchitectureId.isBlank()) {
			throw new InvalidUserArchitectureRequestException("baseArchitectureId must not be blank");
		}
		if (targetArchitectureId == null || targetArchitectureId.isBlank()) {
			throw new InvalidUserArchitectureRequestException("targetArchitectureId must not be blank");
		}
		return UserArchitectureComparator.compare(findOne(baseArchitectureId), findOne(targetArchitectureId));
	}

	@Override
	public UserArchitectureComparisonResult compareWithScenarioRecommendation(String architectureId, Long scenarioId) {
		if (scenarioId == null) {
			throw new InvalidUserArchitectureRequestException("scenarioId must not be null");
		}
		if (scenarioQueryPort == null) {
			throw new InvalidUserArchitectureRequestException("scenario comparison is not available");
		}
		UserArchitecture architecture = findOne(architectureId);
		Scenario scenario = scenarioQueryPort.findById(scenarioId)
				.orElseThrow(() -> new ScenarioNotFoundException(scenarioId));
		List<ScenarioOption> recommendedOptions = recommendedOptionsFor(scenario);
		if (recommendedOptions.isEmpty()) {
			throw new InvalidUserArchitectureRequestException("scenario has no comparable recommended options: " + scenarioId);
		}
		return UserArchitectureComparator.compareWithScenarioRecommendation(architecture, scenario, recommendedOptions);
	}

	@Override
	@Transactional
	public UserArchitecture create(ManageUserArchitectureUseCase.CreateUserArchitectureCommand command) {
		if (command == null) {
			throw new InvalidUserArchitectureRequestException("request body must not be null");
		}
		Instant now = Instant.now(clock);
		return commandPort.save(toDomain(UUID.randomUUID().toString(), command.title(), command.description(), now, now,
				command.nodes(), command.connections()));
	}

	@Override
	@Transactional
	public UserArchitecture update(String architectureId, ManageUserArchitectureUseCase.UpdateUserArchitectureCommand command) {
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
			List<ManageUserArchitectureUseCase.NodeCommand> nodeCommands,
			List<ManageUserArchitectureUseCase.ConnectionCommand> connectionCommands
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

	private List<UserArchitectureNode> toNodes(List<ManageUserArchitectureUseCase.NodeCommand> commands) {
		if (commands == null) {
			return List.of();
		}
		return commands.stream()
				.map(this::toNode)
				.toList();
	}

	private List<UserArchitectureConnection> toConnections(List<ManageUserArchitectureUseCase.ConnectionCommand> commands) {
		if (commands == null) {
			return List.of();
		}
		return commands.stream()
				.map(this::toConnection)
				.toList();
	}

	private UserArchitectureNode toNode(ManageUserArchitectureUseCase.NodeCommand command) {
		if (command == null) {
			throw new InvalidUserArchitectureRequestException("nodes must not contain null");
		}
		return new UserArchitectureNode(command.id(), command.resourceType(), command.displayName());
	}

	private UserArchitectureConnection toConnection(ManageUserArchitectureUseCase.ConnectionCommand command) {
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

	private List<DraftNode> toDraftNodes(List<ValidateUserArchitectureUseCase.NodeCommand> commands) {
		if (commands == null) {
			return List.of();
		}
		return commands.stream()
				.map(command -> command == null ? null : new DraftNode(command.id(), command.resourceType(), command.displayName()))
				.toList();
	}

	private List<DraftConnection> toDraftConnections(List<ValidateUserArchitectureUseCase.ConnectionCommand> commands) {
		if (commands == null) {
			return List.of();
		}
		return commands.stream()
				.map(command -> command == null ? null : new DraftConnection(
						command.id(),
						command.sourceNodeId(),
						command.targetNodeId(),
						command.connectionType()
				))
				.toList();
	}

	private List<ScenarioOption> recommendedOptionsFor(Scenario scenario) {
		List<ScenarioOption> coreOptions = scenario.getOptions().stream()
				.filter(ScenarioOption::isCore)
				.toList();
		if (!coreOptions.isEmpty()) {
			return coreOptions;
		}
		return scenario.getOptions().stream()
				.filter(option -> option.getScore() > 0)
				.toList();
	}
}
