package com.yunhwan.cloudsimlab.learningdiscovery.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.learningdiscovery.domain.LearningDiscoveryItem;
import com.yunhwan.cloudsimlab.learningdiscovery.domain.LearningDiscoveryResourceType;

final class LearningDiscoveryDtos {

	private LearningDiscoveryDtos() {
	}

	record Response(
			LearningDiscoveryResourceType resourceType,
			String id,
			String title,
			String summary,
			String category,
			String level,
			List<String> conceptTags,
			List<String> relatedDocumentIds,
			List<String> relatedScenarioIds,
			List<String> relatedModuleIds,
			List<String> relatedArchitecturePracticeIds,
			boolean recommendedPathIncluded,
			int orderIndex
	) {
		static Response from(LearningDiscoveryItem item) {
			return new Response(
					item.resourceType(),
					item.id(),
					item.title(),
					item.summary(),
					item.category(),
					item.level(),
					item.conceptTags(),
					item.relatedDocumentIds(),
					item.relatedScenarioIds(),
					item.relatedModuleIds(),
					item.relatedArchitecturePracticeIds(),
					item.recommendedPathIncluded(),
					item.orderIndex()
			);
		}
	}
}
