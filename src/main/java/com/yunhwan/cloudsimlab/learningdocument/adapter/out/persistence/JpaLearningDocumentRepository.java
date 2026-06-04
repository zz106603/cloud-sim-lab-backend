package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaLearningDocumentRepository extends JpaRepository<JpaLearningDocumentEntity, Long> {

	List<JpaLearningDocumentEntity> findByDocumentKeyIn(List<String> documentKeys);
}
