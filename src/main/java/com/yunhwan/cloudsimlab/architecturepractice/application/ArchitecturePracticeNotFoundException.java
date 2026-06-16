package com.yunhwan.cloudsimlab.architecturepractice.application;

public class ArchitecturePracticeNotFoundException extends RuntimeException {

	public ArchitecturePracticeNotFoundException(String practiceId) {
		super("Architecture practice not found: " + practiceId);
	}
}
