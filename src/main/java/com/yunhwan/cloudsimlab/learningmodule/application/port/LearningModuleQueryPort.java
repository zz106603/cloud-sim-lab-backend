package com.yunhwan.cloudsimlab.learningmodule.application.port;

import java.util.List;
import java.util.Optional;

import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;

public interface LearningModuleQueryPort {

	List<LearningModule> findAll();

	Optional<LearningModule> findById(String moduleId);

	List<LearningModule> findAllByIdIn(List<String> moduleIds);
}
