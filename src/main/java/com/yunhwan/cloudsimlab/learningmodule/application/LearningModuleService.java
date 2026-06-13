package com.yunhwan.cloudsimlab.learningmodule.application;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yunhwan.cloudsimlab.learningmodule.application.port.LearningModuleQueryPort;
import com.yunhwan.cloudsimlab.learningmodule.application.port.in.GetLearningModuleUseCase;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;

@Service
public class LearningModuleService implements GetLearningModuleUseCase {

	private final LearningModuleQueryPort queryPort;

	public LearningModuleService(LearningModuleQueryPort queryPort) {
		this.queryPort = queryPort;
	}

	@Override
	public List<LearningModule> findAll() {
		return queryPort.findAll().stream()
				.sorted(Comparator.comparing(LearningModule::pathId).thenComparingInt(LearningModule::orderIndex))
				.toList();
	}

	@Override
	public LearningModule findOne(String moduleId) {
		return queryPort.findById(moduleId)
				.orElseThrow(() -> new LearningModuleNotFoundException(moduleId));
	}
}
