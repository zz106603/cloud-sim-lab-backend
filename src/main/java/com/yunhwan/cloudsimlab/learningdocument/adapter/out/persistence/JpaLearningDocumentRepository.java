package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;

interface JpaLearningDocumentRepository extends JpaRepository<JpaLearningDocumentEntity, Long> {

	List<JpaLearningDocumentEntity> findByCategory(DocumentCategory category);
}
