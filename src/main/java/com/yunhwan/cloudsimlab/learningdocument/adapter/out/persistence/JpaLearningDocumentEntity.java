package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
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

	@Column(name = "order_index", nullable = false)
	private int orderIndex;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "learning_document_prerequisite", joinColumns = @JoinColumn(name = "document_id"))
	@OrderColumn(name = "sort_order")
	@Column(name = "prerequisite_document_id", nullable = false, length = 80)
	private List<String> prerequisiteDocumentIds = new ArrayList<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "learning_document_concept_tag", joinColumns = @JoinColumn(name = "document_id"))
	@OrderColumn(name = "sort_order")
	@Column(name = "concept_tag", nullable = false, length = 80)
	private List<String> conceptTags = new ArrayList<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "learning_document_related_module", joinColumns = @JoinColumn(name = "document_id"))
	@OrderColumn(name = "sort_order")
	@Column(name = "module_id", nullable = false, length = 80)
	private List<String> relatedModuleIds = new ArrayList<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "learning_document_related_scenario", joinColumns = @JoinColumn(name = "document_id"))
	@OrderColumn(name = "sort_order")
	@Column(name = "scenario_id", nullable = false, length = 80)
	private List<String> relatedScenarioIds = new ArrayList<>();

	protected JpaLearningDocumentEntity() {
	}

	private JpaLearningDocumentEntity(
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
		this.documentKey = documentKey;
		this.title = title;
		this.category = category;
		this.level = level;
		this.summary = summary;
		this.content = content;
		this.orderIndex = orderIndex;
		this.prerequisiteDocumentIds = new ArrayList<>(prerequisiteDocumentIds);
		this.conceptTags = new ArrayList<>(conceptTags);
		this.relatedModuleIds = new ArrayList<>(relatedModuleIds);
		this.relatedScenarioIds = new ArrayList<>(relatedScenarioIds);
	}

	static JpaLearningDocumentEntity from(LearningDocument document) {
		return new JpaLearningDocumentEntity(
				document.getDocumentKey(),
				document.getTitle(),
				document.getCategory(),
				document.getLevel(),
				document.getSummary(),
				document.getContent(),
				document.getOrderIndex(),
				document.getPrerequisiteDocumentIds(),
				document.getConceptTags(),
				document.getRelatedModuleIds(),
				document.getRelatedScenarioIds()
		);
	}

	LearningDocument toDomain() {
		return new LearningDocument(
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
				relatedScenarioIds
		);
	}
}
