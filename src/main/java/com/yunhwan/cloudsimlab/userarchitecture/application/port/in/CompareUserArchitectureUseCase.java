package com.yunhwan.cloudsimlab.userarchitecture.application.port.in;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureComparisonResult;

public interface CompareUserArchitectureUseCase {

	UserArchitectureComparisonResult compareSaved(String baseArchitectureId, String targetArchitectureId);

	UserArchitectureComparisonResult compareWithScenarioRecommendation(String architectureId, Long scenarioId);
}
