package com.yunhwan.cloudsimlab.learningdiscovery.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.architecturepractice.application.port.ArchitecturePracticeQueryPort;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;
import com.yunhwan.cloudsimlab.learningdiscovery.application.port.in.GetLearningDiscoveryUseCase;
import com.yunhwan.cloudsimlab.learningdiscovery.domain.LearningDiscoveryItem;
import com.yunhwan.cloudsimlab.learningdiscovery.domain.LearningDiscoveryResourceType;
import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.learningmodule.application.port.LearningModuleQueryPort;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence.CurriculumSeedCatalog;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelation;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelations;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioPrerequisiteConcept;

@Service
@Transactional(readOnly = true)
public class LearningDiscoveryService implements GetLearningDiscoveryUseCase {

	private static final int UNORDERED = Integer.MAX_VALUE;

	private final LearningDocumentQueryPort documentQueryPort;
	private final ScenarioQueryPort scenarioQueryPort;
	private final LearningModuleQueryPort moduleQueryPort;
	private final ArchitecturePracticeQueryPort architecturePracticeQueryPort;

	public LearningDiscoveryService(
			LearningDocumentQueryPort documentQueryPort,
			ScenarioQueryPort scenarioQueryPort,
			LearningModuleQueryPort moduleQueryPort,
			ArchitecturePracticeQueryPort architecturePracticeQueryPort
	) {
		this.documentQueryPort = documentQueryPort;
		this.scenarioQueryPort = scenarioQueryPort;
		this.moduleQueryPort = moduleQueryPort;
		this.architecturePracticeQueryPort = architecturePracticeQueryPort;
	}

	@Override
	public List<LearningDiscoveryItem> findAll(String category, String level, String tag, String resourceType) {
		List<LearningDocument> documents = documentQueryPort.findAll();
		List<Scenario> scenarios = scenarioQueryPort.findAll(null, null);
		List<LearningModule> modules = moduleQueryPort.findAll();
		List<ArchitecturePracticeTemplate> practices = architecturePracticeQueryPort.findAll();
		DiscoveryContext context = DiscoveryContext.from(documents, scenarios, modules, practices);

		return Stream.of(
						documents.stream().map(document -> documentItem(document, context)),
						scenarios.stream().map(scenario -> scenarioItem(scenario, context)),
						modules.stream().map(module -> moduleItem(module, context)),
						practices.stream().map(practice -> architecturePracticeItem(practice, context))
				)
				.flatMap(stream -> stream)
				.filter(matches(category, level, tag, resourceType))
				.sorted(discoveryOrder())
				.toList();
	}

	private LearningDiscoveryItem documentItem(LearningDocument document, DiscoveryContext context) {
		String documentId = documentId(document);
		String documentKey = document.getDocumentKey();
		List<String> relatedModuleIds = unique(Stream.concat(
				document.getRelatedModuleIds().stream(),
				context.moduleIdsByDocumentId.getOrDefault(documentId, List.of()).stream()
		));
		List<String> relatedScenarioIds = unique(Stream.concat(
				document.getRelatedScenarioIds().stream(),
				LearningRelations.forDocument(documentKey).stream().map(LearningRelation::scenarioKey)
		));
		List<String> relatedArchitecturePracticeIds = context.practiceIdsByDocumentId.getOrDefault(documentId, List.of());

		return new LearningDiscoveryItem(
				LearningDiscoveryResourceType.DOCUMENT,
				documentId,
				document.getTitle(),
				document.getSummary(),
				nameOf(document.getCategory()),
				nameOf(document.getLevel()),
				document.getConceptTags(),
				document.getPrerequisiteDocumentIds(),
				relatedScenarioIds,
				relatedModuleIds,
				relatedArchitecturePracticeIds,
				context.includesRecommendedModule(relatedModuleIds),
				document.getOrderIndex()
		);
	}

	private LearningDiscoveryItem scenarioItem(Scenario scenario, DiscoveryContext context) {
		String scenarioId = scenarioId(scenario);
		String scenarioKey = scenario.getGraphKey();
		List<String> relatedDocumentIds = unique(Stream.concat(
				LearningRelations.forScenario(scenarioKey).stream().map(LearningRelation::documentKey),
				context.documentIdsByScenarioId.getOrDefault(scenarioId, List.of()).stream()
		));
		List<String> relatedModuleIds = unique(Stream.concat(
				scenario.getRelatedModuleIds().stream(),
				context.moduleIdsByScenarioId.getOrDefault(scenarioId, List.of()).stream()
		));
		List<String> relatedArchitecturePracticeIds = context.practiceIdsByScenarioId.getOrDefault(scenarioId, List.of());

		return new LearningDiscoveryItem(
				LearningDiscoveryResourceType.SCENARIO,
				scenarioId,
				scenario.getTitle(),
				scenario.getSummary(),
				nameOf(scenario.getCategory()),
				nameOf(scenario.getLevel()),
				unique(Stream.concat(
						scenario.getPrerequisiteConcepts().stream().map(ScenarioPrerequisiteConcept::conceptId),
						scenario.getJudgmentPerspectives().stream()
				)),
				relatedDocumentIds,
				List.of(),
				relatedModuleIds,
				relatedArchitecturePracticeIds,
				context.includesRecommendedModule(relatedModuleIds),
				context.minimumModuleOrder(relatedModuleIds)
		);
	}

	private LearningDiscoveryItem moduleItem(LearningModule module, DiscoveryContext context) {
		List<String> conceptTags = module.documentIds().stream()
				.flatMap(documentId -> context.conceptTagsByDocumentId.getOrDefault(documentId, List.of()).stream())
				.collect(Collectors.collectingAndThen(
						Collectors.toCollection(LinkedHashSet::new),
						List::copyOf
				));

		return new LearningDiscoveryItem(
				LearningDiscoveryResourceType.MODULE,
				module.id(),
				module.title(),
				module.description(),
				"CURRICULUM",
				context.levelByPathId.get(module.pathId()),
				conceptTags,
				module.documentIds(),
				module.relatedScenarioIds(),
				List.of(),
				module.relatedArchitecturePracticeIds(),
				context.recommendedModuleIds.contains(module.id()),
				module.orderIndex()
		);
	}

	private LearningDiscoveryItem architecturePracticeItem(ArchitecturePracticeTemplate practice, DiscoveryContext context) {
		List<String> conceptTags = unique(Stream.concat(
				practice.relatedDocumentIds().stream()
						.flatMap(documentId -> context.conceptTagsByDocumentId.getOrDefault(documentId, List.of()).stream()),
				Stream.concat(
						practice.requiredResourceTypes().stream().map(Enum::name),
						practice.requiredConnectionTypes().stream().map(Enum::name)
				)
		));

		return new LearningDiscoveryItem(
				LearningDiscoveryResourceType.ARCHITECTURE_PRACTICE,
				practice.id(),
				practice.title(),
				practice.description(),
				"ARCHITECTURE",
				nameOf(practice.level()),
				conceptTags,
				practice.relatedDocumentIds(),
				practice.relatedScenarioIds(),
				practice.relatedModuleIds(),
				List.of(),
				context.includesRecommendedModule(practice.relatedModuleIds()),
				context.minimumModuleOrder(practice.relatedModuleIds())
		);
	}

	private Predicate<LearningDiscoveryItem> matches(String category, String level, String tag, String resourceType) {
		return item -> matchesValue(item.category(), category)
				&& matchesValue(item.level(), level)
				&& matchesResourceType(item.resourceType(), resourceType)
				&& matchesTag(item.conceptTags(), tag);
	}

	private boolean matchesValue(String actual, String expected) {
		if (!hasText(expected)) {
			return true;
		}
		return actual != null && actual.equalsIgnoreCase(expected.trim());
	}

	private boolean matchesResourceType(LearningDiscoveryResourceType actual, String expected) {
		if (!hasText(expected)) {
			return true;
		}
		return actual.name().equalsIgnoreCase(expected.trim());
	}

	private boolean matchesTag(List<String> actualTags, String expected) {
		if (!hasText(expected)) {
			return true;
		}
		String target = expected.trim().toLowerCase(Locale.ROOT);
		return actualTags.stream()
				.anyMatch(tag -> tag.toLowerCase(Locale.ROOT).equals(target));
	}

	private Comparator<LearningDiscoveryItem> discoveryOrder() {
		return Comparator
				.comparing(LearningDiscoveryItem::recommendedPathIncluded).reversed()
				.thenComparingInt(item -> levelOrder(item.level()))
				.thenComparingInt(LearningDiscoveryItem::orderIndex)
				.thenComparing(item -> item.resourceType().name())
				.thenComparing(LearningDiscoveryItem::id);
	}

	private int levelOrder(String level) {
		if ("BEGINNER".equals(level)) {
			return 0;
		}
		if ("INTERMEDIATE".equals(level)) {
			return 1;
		}
		if ("ADVANCED".equals(level)) {
			return 2;
		}
		return 99;
	}

	private static List<String> unique(Stream<String> values) {
		return values
				.filter(LearningDiscoveryService::hasText)
				.collect(Collectors.collectingAndThen(
						Collectors.toCollection(LinkedHashSet::new),
						List::copyOf
				));
	}

	private static String nameOf(Enum<?> value) {
		return value == null ? null : value.name();
	}

	private static String documentId(LearningDocument document) {
		if (hasText(document.getDocumentKey())) {
			return document.getDocumentKey();
		}
		return String.valueOf(document.getId());
	}

	private static String scenarioId(Scenario scenario) {
		if (hasText(scenario.getGraphKey())) {
			return scenario.getGraphKey();
		}
		return String.valueOf(scenario.getId());
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private record DiscoveryContext(
			Map<String, List<String>> moduleIdsByDocumentId,
			Map<String, List<String>> moduleIdsByScenarioId,
			Map<String, List<String>> practiceIdsByDocumentId,
			Map<String, List<String>> practiceIdsByScenarioId,
			Map<String, List<String>> documentIdsByScenarioId,
			Map<String, List<String>> conceptTagsByDocumentId,
			Map<String, Integer> moduleOrderById,
			Map<String, String> levelByPathId,
			Set<String> recommendedModuleIds
	) {
		static DiscoveryContext from(
				List<LearningDocument> documents,
				List<Scenario> scenarios,
				List<LearningModule> modules,
				List<ArchitecturePracticeTemplate> practices
		) {
			Map<String, List<String>> moduleIdsByDocumentId = modules.stream()
					.flatMap(module -> module.documentIds().stream().map(documentId -> Map.entry(documentId, module.id())))
					.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
			Map<String, List<String>> moduleIdsByScenarioId = modules.stream()
					.flatMap(module -> module.relatedScenarioIds().stream().map(scenarioId -> Map.entry(scenarioId, module.id())))
					.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
			Map<String, List<String>> practiceIdsByDocumentId = practices.stream()
					.flatMap(practice -> practice.relatedDocumentIds().stream().map(documentId -> Map.entry(documentId, practice.id())))
					.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
			Map<String, List<String>> practiceIdsByScenarioId = practices.stream()
					.flatMap(practice -> practice.relatedScenarioIds().stream().map(scenarioId -> Map.entry(scenarioId, practice.id())))
					.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
			Map<String, List<String>> documentIdsByScenarioId = documents.stream()
					.flatMap(document -> document.getRelatedScenarioIds().stream().map(scenarioId -> Map.entry(scenarioId, documentId(document))))
					.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
			Map<String, List<String>> conceptTagsByDocumentId = documents.stream()
					.collect(Collectors.toMap(LearningDiscoveryService::documentId, LearningDocument::getConceptTags, (left, right) -> left));
			Map<String, Integer> moduleOrderById = modules.stream()
					.collect(Collectors.toMap(LearningModule::id, LearningModule::orderIndex, Math::min));
			Map<String, String> levelByPathId = CurriculumSeedCatalog.paths().stream()
					.collect(Collectors.toMap(LearningPath::id, LearningPath::targetLevel, (left, right) -> left));
			Set<String> recommendedModuleIds = CurriculumSeedCatalog.paths().stream()
					.filter(LearningPath::recommended)
					.flatMap(path -> path.moduleIds().stream())
					.collect(Collectors.toUnmodifiableSet());
			return new DiscoveryContext(
					moduleIdsByDocumentId,
					moduleIdsByScenarioId,
					practiceIdsByDocumentId,
					practiceIdsByScenarioId,
					documentIdsByScenarioId,
					conceptTagsByDocumentId,
					moduleOrderById,
					levelByPathId,
					recommendedModuleIds
			);
		}

		boolean includesRecommendedModule(List<String> moduleIds) {
			return moduleIds.stream().anyMatch(recommendedModuleIds::contains);
		}

		int minimumModuleOrder(List<String> moduleIds) {
			List<Integer> orders = new ArrayList<>();
			for (String moduleId : moduleIds) {
				Integer order = moduleOrderById.get(moduleId);
				if (order != null) {
					orders.add(order);
				}
			}
			return orders.stream()
					.mapToInt(Integer::intValue)
					.min()
					.orElse(UNORDERED);
		}
	}
}
