package com.yunhwan.cloudsimlab.learningmodule.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.yunhwan.cloudsimlab.learningmodule.application.port.LearningModuleQueryPort;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence.CurriculumSeedCatalog;

@Component
public class LearningModuleSeedAdapter implements LearningModuleQueryPort {

	@Override
	public List<LearningModule> findAll() {
		return CurriculumSeedCatalog.modules();
	}

	@Override
	public Optional<LearningModule> findById(String moduleId) {
		return CurriculumSeedCatalog.modules().stream()
				.filter(module -> module.id().equals(moduleId))
				.findFirst();
	}

	@Override
	public List<LearningModule> findAllByIdIn(List<String> moduleIds) {
		Set<String> targetIds = Set.copyOf(moduleIds == null ? List.of() : moduleIds);
		return CurriculumSeedCatalog.modules().stream()
				.filter(module -> targetIds.contains(module.id()))
				.toList();
	}
}
