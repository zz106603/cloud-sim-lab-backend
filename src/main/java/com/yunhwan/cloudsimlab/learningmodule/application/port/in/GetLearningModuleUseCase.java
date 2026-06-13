package com.yunhwan.cloudsimlab.learningmodule.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;

public interface GetLearningModuleUseCase {

	List<LearningModule> findAll();

	LearningModule findOne(String moduleId);
}
