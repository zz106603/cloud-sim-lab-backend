package com.yunhwan.cloudsimlab.learningdocument.domain;

import java.util.List;
import java.util.Objects;

public class LearningDocument {

	private final Long id;
	private final String documentKey;
	private final String title;
	private final DocumentCategory category;
	private final DocumentLevel level;
	private final String summary;
	private final String content;
	private final int orderIndex;
	private final List<String> prerequisiteDocumentIds;
	private final List<String> conceptTags;
	private final List<String> relatedModuleIds;
	private final List<String> relatedScenarioIds;
	private final List<LearningDocumentCheckpoint> checkpoints;
	private final List<LearningDocumentRecallQuestion> recallQuestions;

	public LearningDocument(Long id, String title, DocumentCategory category, DocumentLevel level, String summary, String content) {
		this(id, null, title, category, level, summary, content);
	}

	public LearningDocument(
			Long id,
			String documentKey,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String content
	) {
		this(id, documentKey, title, category, level, summary, content, 0, List.of(), List.of(), List.of(), List.of());
	}

	public LearningDocument(
			Long id,
			String documentKey,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String content,
			int orderIndex,
			List<String> prerequisiteDocumentIds,
			List<String> conceptTags,
			List<String> relatedModuleIds,
			List<String> relatedScenarioIds
	) {
		this(
				id,
				documentKey,
				title,
				category,
				level,
				summary,
				content,
				orderIndex,
				prerequisiteDocumentIds,
				conceptTags,
				relatedModuleIds,
				relatedScenarioIds,
				List.of(),
				List.of()
		);
	}

	public LearningDocument(
			Long id,
			String documentKey,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String content,
			int orderIndex,
			List<String> prerequisiteDocumentIds,
			List<String> conceptTags,
			List<String> relatedModuleIds,
			List<String> relatedScenarioIds,
			List<LearningDocumentCheckpoint> checkpoints,
			List<LearningDocumentRecallQuestion> recallQuestions
	) {
		this.id = id;
		this.documentKey = documentKey;
		this.title = title;
		this.category = category;
		this.level = level;
		this.summary = summary;
		this.content = content;
		this.orderIndex = orderIndex;
		this.prerequisiteDocumentIds = copy(prerequisiteDocumentIds);
		this.conceptTags = copy(conceptTags);
		this.relatedModuleIds = copy(relatedModuleIds);
		this.relatedScenarioIds = copy(relatedScenarioIds);
		this.checkpoints = copy(checkpoints);
		this.recallQuestions = copy(recallQuestions);
	}

	public static LearningDocument newDocument(String title, DocumentCategory category, DocumentLevel level, String summary, String content) {
		return new LearningDocument(null, title, category, level, summary, content);
	}

	public static LearningDocument newDocumentWithKey(
			String documentKey,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String content
	) {
		return new LearningDocument(null, documentKey, title, category, level, summary, content);
	}

	public static LearningDocument newDocumentWithMetadata(
			String documentKey,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String content,
			int orderIndex,
			List<String> prerequisiteDocumentIds,
			List<String> conceptTags,
			List<String> relatedModuleIds,
			List<String> relatedScenarioIds
	) {
		return new LearningDocument(
				null,
				documentKey,
				title,
				category,
				level,
				summary,
				content,
				orderIndex,
				prerequisiteDocumentIds,
				conceptTags,
				relatedModuleIds,
				relatedScenarioIds
		);
	}

	public static LearningDocument newDocumentWithReinforcement(
			String documentKey,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String content,
			int orderIndex,
			List<String> prerequisiteDocumentIds,
			List<String> conceptTags,
			List<String> relatedModuleIds,
			List<String> relatedScenarioIds,
			List<LearningDocumentCheckpoint> checkpoints,
			List<LearningDocumentRecallQuestion> recallQuestions
	) {
		return new LearningDocument(
				null,
				documentKey,
				title,
				category,
				level,
				summary,
				content,
				orderIndex,
				prerequisiteDocumentIds,
				conceptTags,
				relatedModuleIds,
				relatedScenarioIds,
				checkpoints,
				recallQuestions
		);
	}

	public Long getId() {
		return id;
	}

	public String getDocumentKey() {
		return documentKey;
	}

	public String getTitle() {
		return title;
	}

	public DocumentCategory getCategory() {
		return category;
	}

	public DocumentLevel getLevel() {
		return level;
	}

	public String getSummary() {
		return summary;
	}

	public String getContent() {
		return content;
	}

	public int getOrderIndex() {
		return orderIndex;
	}

	public List<String> getPrerequisiteDocumentIds() {
		return prerequisiteDocumentIds;
	}

	public List<String> getConceptTags() {
		return conceptTags;
	}

	public List<String> getRelatedModuleIds() {
		return relatedModuleIds;
	}

	public List<String> getRelatedScenarioIds() {
		return relatedScenarioIds;
	}

	public List<LearningDocumentCheckpoint> getCheckpoints() {
		return checkpoints;
	}

	public List<LearningDocumentRecallQuestion> getRecallQuestions() {
		return recallQuestions;
	}

	private static <T> List<T> copy(List<T> values) {
		return List.copyOf(Objects.requireNonNull(values));
	}
}
