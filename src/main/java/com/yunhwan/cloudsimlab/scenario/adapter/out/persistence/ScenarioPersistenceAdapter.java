package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioSeedPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

@Component
class ScenarioPersistenceAdapter implements ScenarioQueryPort, ScenarioSeedPort {

	private final JpaScenarioRepository repository;

	ScenarioPersistenceAdapter(JpaScenarioRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<Scenario> findAll(ScenarioCategory category, ScenarioLevel level) {
		return findEntities(category, level)
				.stream()
				.map(JpaScenarioEntity::toDomain)
				.toList();
	}

	@Override
	public Optional<Scenario> findById(Long scenarioId) {
		return repository.findById(scenarioId)
				.map(JpaScenarioEntity::toDomain);
	}

	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public Scenario save(Scenario scenario) {
		return repository.save(JpaScenarioEntity.from(scenario))
				.toDomain();
	}

	private List<JpaScenarioEntity> findEntities(ScenarioCategory category, ScenarioLevel level) {
		if (category != null && level != null) {
			return repository.findByCategoryAndLevel(category, level);
		}
		if (category != null) {
			return repository.findByCategory(category);
		}
		if (level != null) {
			return repository.findByLevel(level);
		}
		return repository.findAll();
	}
}
