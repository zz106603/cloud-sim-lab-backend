package com.yunhwan.cloudsimlab.learningpath.application.port;

import java.util.List;
import java.util.Optional;

import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;

public interface LearningPathQueryPort {

	List<LearningPath> findAll();

	Optional<LearningPath> findById(String pathId);
}
