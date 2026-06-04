package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_document")
class JpaLearningDocumentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, length = 80)
	private String documentKey;

	@Column(nullable = false, length = 120)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private DocumentCategory category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private DocumentLevel level;

	@Column(nullable = false, length = 500)
	private String summary;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	protected JpaLearningDocumentEntity() {
	}

	private JpaLearningDocumentEntity(
			String documentKey,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String content
	) {
		this.documentKey = documentKey;
		this.title = title;
		this.category = category;
		this.level = level;
		this.summary = summary;
		this.content = content;
	}

	static JpaLearningDocumentEntity from(LearningDocument document) {
		return new JpaLearningDocumentEntity(
				document.getDocumentKey(),
				document.getTitle(),
				document.getCategory(),
				document.getLevel(),
				document.getSummary(),
				document.getContent()
		);
	}

	LearningDocument toDomain() {
		return new LearningDocument(id, documentKey, title, category, level, summary, content);
	}
}
