package com.yunhwan.cloudsimlab.learningpath.application;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yunhwan.cloudsimlab.learningmodule.application.port.LearningModuleQueryPort;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningpath.application.port.LearningPathQueryPort;
import com.yunhwan.cloudsimlab.learningpath.application.port.in.GetLearningPathUseCase;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;

@Service
public class LearningPathService implements GetLearningPathUseCase {

	private final LearningPathQueryPort queryPort;
	private final LearningModuleQueryPort moduleQueryPort;

	public LearningPathService(LearningPathQueryPort queryPort, LearningModuleQueryPort moduleQueryPort) {
		this.queryPort = queryPort;
		this.moduleQueryPort = moduleQueryPort;
	}

	@Override
	public List<LearningPath> findAll() {
		return queryPort.findAll().stream()
				.sorted(Comparator.comparingInt(LearningPath::orderIndex))
				.toList();
	}

	@Override
	public LearningPath findOne(String pathId) {
		return queryPort.findById(pathId)
				.orElseThrow(() -> new LearningPathNotFoundException(pathId));
	}

	@Override
	public List<LearningModule> findModules(LearningPath path) {
		if (path == null) {
			return List.of();
		}
		return moduleQueryPort.findAllByIdIn(path.moduleIds()).stream()
				.sorted(Comparator.comparingInt(LearningModule::orderIndex))
				.toList();
	}
}
