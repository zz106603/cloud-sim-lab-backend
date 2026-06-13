package com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.yunhwan.cloudsimlab.learningpath.application.port.LearningPathQueryPort;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;

@Component
public class LearningPathSeedAdapter implements LearningPathQueryPort {

	@Override
	public List<LearningPath> findAll() {
		return CurriculumSeedCatalog.paths();
	}

	@Override
	public Optional<LearningPath> findById(String pathId) {
		return CurriculumSeedCatalog.paths().stream()
				.filter(path -> path.id().equals(pathId))
				.findFirst();
	}
}
