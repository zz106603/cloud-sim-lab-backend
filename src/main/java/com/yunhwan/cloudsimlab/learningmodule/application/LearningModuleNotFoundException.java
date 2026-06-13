package com.yunhwan.cloudsimlab.learningmodule.application;

public class LearningModuleNotFoundException extends RuntimeException {

	public LearningModuleNotFoundException(String moduleId) {
		super("Learning module not found: " + moduleId);
	}
}
