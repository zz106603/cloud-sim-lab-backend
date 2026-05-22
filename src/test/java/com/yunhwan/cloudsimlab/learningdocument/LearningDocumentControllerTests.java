package com.yunhwan.cloudsimlab.learningdocument;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentSeedPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

@SpringBootTest
@AutoConfigureMockMvc
class LearningDocumentControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private LearningDocumentSeedPort seedPort;

	private LearningDocument document;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("delete from learning_document");
		document = seedPort.save(LearningDocument.newDocument(
				"Virtual machines and compute capacity",
				DocumentCategory.COMPUTE,
				DocumentLevel.BEGINNER,
				"Understand compute capacity.",
				"Virtual machines run application workloads on configurable CPU and memory resources."
		));
	}

	@Test
	void findAllReturnsDocumentSummaries() throws Exception {
		mockMvc.perform(get("/api/docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(document.getId()))
				.andExpect(jsonPath("$[0].title").value("Virtual machines and compute capacity"))
				.andExpect(jsonPath("$[0].category").value("COMPUTE"))
				.andExpect(jsonPath("$[0].level").value("BEGINNER"))
				.andExpect(jsonPath("$[0].summary").value("Understand compute capacity."))
				.andExpect(jsonPath("$[0].content").doesNotExist());
	}

	@Test
	void findOneReturnsDocumentDetail() throws Exception {
		mockMvc.perform(get("/api/docs/{documentId}", document.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(document.getId()))
				.andExpect(jsonPath("$.title").value("Virtual machines and compute capacity"))
				.andExpect(jsonPath("$.content").value("Virtual machines run application workloads on configurable CPU and memory resources."));
	}

	@Test
	void findOneReturnsNotFoundForMissingDocument() throws Exception {
		mockMvc.perform(get("/api/docs/{documentId}", 999L))
				.andExpect(status().isNotFound());
	}
}
