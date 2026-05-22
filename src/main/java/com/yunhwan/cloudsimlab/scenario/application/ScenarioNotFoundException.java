package com.yunhwan.cloudsimlab.scenario.application;

public class ScenarioNotFoundException extends RuntimeException {

	public ScenarioNotFoundException(Long scenarioId) {
		super("Scenario not found: " + scenarioId);
	}
}
