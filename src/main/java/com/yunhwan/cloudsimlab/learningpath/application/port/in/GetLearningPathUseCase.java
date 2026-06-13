package com.yunhwan.cloudsimlab.learningpath.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;

public interface GetLearningPathUseCase {

	List<LearningPath> findAll();

	LearningPath findOne(String pathId);

	List<LearningModule> findModules(LearningPath path);
}
