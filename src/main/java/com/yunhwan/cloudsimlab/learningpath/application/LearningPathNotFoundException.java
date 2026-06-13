package com.yunhwan.cloudsimlab.learningpath.application;

public class LearningPathNotFoundException extends RuntimeException {

	public LearningPathNotFoundException(String pathId) {
		super("Learning path not found: " + pathId);
	}
}
