package com.yunhwan.cloudsimlab.learningdocument.domain;

public class LearningDocument {

	private final Long id;
	private final String title;
	private final DocumentCategory category;
	private final DocumentLevel level;
	private final String summary;
	private final String content;

	public LearningDocument(Long id, String title, DocumentCategory category, DocumentLevel level, String summary, String content) {
		this.id = id;
		this.title = title;
		this.category = category;
		this.level = level;
		this.summary = summary;
		this.content = content;
	}

	public static LearningDocument newDocument(String title, DocumentCategory category, DocumentLevel level, String summary, String content) {
		return new LearningDocument(null, title, category, level, summary, content);
	}

	public Long getId() {
		return id;
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
}
