package com.yunhwan.cloudsimlab.learningdiscovery.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.learningdiscovery.domain.LearningDiscoveryItem;

public interface GetLearningDiscoveryUseCase {

	List<LearningDiscoveryItem> findAll(String category, String level, String tag, String resourceType);
}
