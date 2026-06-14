package com.yunhwan.cloudsimlab.content;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence.LearningDocumentSeedCatalog;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence.CurriculumSeedCatalog;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelation;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelations;
import com.yunhwan.cloudsimlab.scenario.adapter.out.persistence.ScenarioSeedCatalog;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

class ContentIntegrityValidatorTests {

	private final ContentIntegrityValidator validator = new ContentIntegrityValidator();

	@Test
	void 정상_seed_콘텐츠는_무결성_검증을_통과한다() {
		validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules()
		);
	}

	@Test
	void 학습_경로가_다른_경로의_모듈을_참조하면_진단한다() {
		LearningPath path = new LearningPath(
				"path-a",
				"경로 A",
				"설명",
				"BEGINNER",
				"목표",
				true,
				1,
				List.of("module-a")
		);
		LearningPath otherPath = new LearningPath(
				"path-b",
				"경로 B",
				"설명",
				"BEGINNER",
				"목표",
				false,
				2,
				List.of("module-b")
		);
		LearningModule module = new LearningModule(
				"module-a",
				"path-b",
				"다른 경로 모듈",
				"설명",
				List.of("목표"),
				List.of(),
				1,
				List.of("ec2-compute-capacity"),
				List.of(),
				List.of()
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(path, otherPath),
				List.of(module)
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningPath[path-a|경로 A] references moduleId with mismatched pathId: module-a");
	}

	@Test
	void 학습_경로의_moduleIds에_null_원소가_있어도_NPE가_아닌_검증_오류로_진단한다() {
		List<String> moduleIds = new ArrayList<>();
		moduleIds.add(null);
		LearningPath path = new LearningPath(
				"path",
				"경로",
				"설명",
				"BEGINNER",
				"목표",
				true,
				1,
				moduleIds
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(path),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningPath[path|경로] moduleIds must not be blank");
	}

	@Test
	void 학습_경로와_모듈_참조가_깨지면_콘텐츠_ID를_포함해_진단한다() {
		LearningPath brokenPath = new LearningPath(
				"broken-path",
				"깨진 경로",
				"설명",
				"BEGINNER",
				"목표",
				false,
				1,
				List.of("missing-module", "missing-module")
		);
		LearningModule brokenModule = new LearningModule(
				"broken-module",
				"unknown-path",
				"깨진 모듈",
				"설명",
				List.of("목표"),
				List.of(),
				1,
				List.of("unknown-document"),
				List.of("unknown-scenario"),
				List.of()
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(brokenPath),
				List.of(brokenModule)
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningPaths must include at least one recommended path")
				.hasMessageContaining("learningPath[broken-path|깨진 경로] references unknown moduleId: missing-module")
				.hasMessageContaining("learningPath[broken-path|깨진 경로] moduleIds has duplicated moduleId: missing-module")
				.hasMessageContaining("learningModule[broken-module|깨진 모듈] references unknown pathId: unknown-path")
				.hasMessageContaining("learningModule[broken-module|깨진 모듈] references unknown documentId: unknown-document")
				.hasMessageContaining("learningModule[broken-module|깨진 모듈] references unknown relatedScenarioId: unknown-scenario");
	}

	@Test
	void 학습_모듈은_학습목표와_문서_또는_시나리오_참조가_필요하다() {
		LearningPath path = new LearningPath(
				"path",
				"경로",
				"설명",
				"BEGINNER",
				"목표",
				true,
				1,
				List.of("empty-module")
		);
		LearningModule module = new LearningModule(
				"empty-module",
				"path",
				"빈 모듈",
				"설명",
				List.of(),
				List.of(),
				1,
				List.of(),
				List.of(),
				List.of()
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(path),
				List.of(module)
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningModule[empty-module|빈 모듈] learningGoals must not be empty")
				.hasMessageContaining("learningModule[empty-module|빈 모듈] must reference at least one document or scenario");
	}

	@Test
	void graphKey_공백_중복_누락_매핑을_진단한다() {
		Scenario brokenScenario = Scenario.newScenarioWithGraphKey(
				"single-spring-boot ",
				"깨진 시나리오",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"학습 목표",
				"설명",
				List.of("Client", "EC2"),
				List.of(
						ScenarioOption.newOptionWithGraphKey("add-alb-auto-scaling ", "공백 선택지", "설명", 1, true, 0),
						ScenarioOption.newOptionWithGraphKey("missing-mapping", "누락 선택지", "설명", 1, false, 0),
						ScenarioOption.newOptionWithGraphKey("missing-mapping", "중복 선택지", "설명", 1, false, 0)
				)
		);

		assertThatThrownBy(() -> validator.validate(
				List.of(brokenScenario),
				LearningDocumentSeedCatalog.documentKeys(),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("scenario[single-spring-boot |깨진 시나리오] graphKey must not have surrounding whitespace")
				.hasMessageContaining("option[add-alb-auto-scaling ] graphKey must not have surrounding whitespace")
				.hasMessageContaining("graph mapping is missing: single-spring-boot ::missing-mapping")
				.hasMessageContaining("option[missing-mapping] graphKey is duplicated in scenario");
	}

	@Test
	void graphKey에_대문자가_있으면_진단한다() {
		Scenario brokenScenario = Scenario.newScenarioWithGraphKey(
				"RDS-Failure",
				"대문자 graphKey 시나리오",
				ScenarioCategory.STORAGE,
				ScenarioLevel.INTERMEDIATE,
				"학습 목표",
				"설명",
				List.of("Client", "ALB", "EC2", "RDS"),
				List.of(
						ScenarioOption.newOptionWithGraphKey("Enable-Multi-AZ", "Multi-AZ 활성화", "설명", 1, true, 0)
				)
		);

		assertThatThrownBy(() -> validator.validate(
				List.of(brokenScenario),
				LearningDocumentSeedCatalog.documentKeys(),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("scenario[RDS-Failure|대문자 graphKey 시나리오] graphKey must be lowercase: 'RDS-Failure'")
				.hasMessageContaining("option[Enable-Multi-AZ] graphKey must be lowercase: 'Enable-Multi-AZ'");
	}

	@Test
	void 존재하지_않는_문서와_시나리오_관계를_진단한다() {
		LearningRelation brokenRelation = new LearningRelation(
				"unknown-scenario",
				"unknown-document",
				"이유",
				"검토 포인트"
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				Set.of("ec2-compute-capacity"),
				List.of(brokenRelation)
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("references unknown scenarioKey: unknown-scenario")
				.hasMessageContaining("references unknown documentKey: unknown-document");
	}

	@Test
	void 장애_영향이_초기_그래프에_없는_node를_참조하면_진단한다() {
		Scenario brokenScenario = Scenario.newScenarioWithGraphKey(
				"rds-failure",
				"깨진 RDS 장애 시나리오",
				ScenarioCategory.STORAGE,
				ScenarioLevel.INTERMEDIATE,
				"장애 영향을 검증합니다.",
				"RDS 노드가 빠진 장애 시나리오입니다.",
				List.of("Client", "ALB", "EC2"),
				List.of(
						ScenarioOption.newOptionWithGraphKey("enable-multi-az", "Multi-AZ 활성화", "설명", 1, true, 0)
				)
		);

		assertThatThrownBy(() -> validator.validate(
				List.of(brokenScenario),
				LearningDocumentSeedCatalog.documentKeys(),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("initialFailureImpact failureSourceNodeId references unknown graph node: rds")
				.hasMessageContaining("initialFailureImpact affectedEdges edge does not exist in graph: ec2->rds|DB 접근");
	}

	@Test
	void 필수_시나리오_데이터와_effects_누락을_진단한다() {
		Scenario brokenScenario = Scenario.newScenarioWithGraphKey(
				"broken-scenario",
				"",
				null,
				ScenarioLevel.BEGINNER,
				"",
				"",
				List.of(),
				List.of(optionWithoutEffects())
		);

		assertThatThrownBy(() -> validator.validate(
				List.of(brokenScenario),
				LearningDocumentSeedCatalog.documentKeys(),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("scenario[broken-scenario|] title must not be blank")
				.hasMessageContaining("summary must not be blank")
				.hasMessageContaining("description must not be blank")
				.hasMessageContaining("category must not be null")
				.hasMessageContaining("initialArchitecture must not be empty")
				.hasMessageContaining("effects must not be null")
				.hasMessageContaining("options must include a core option or a positive score option");
	}

	@Test
	void 초기_아키텍처와_선택지_목록이_null이어도_진단_메시지로_실패한다() {
		Scenario brokenScenario = new Scenario(
				null,
				"single-spring-boot",
				"null 컬렉션 시나리오",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"학습 목표",
				"설명",
				List.of("Client", "EC2"),
				List.of(ScenarioOption.newOptionWithGraphKey("add-alb-auto-scaling", "선택지", "설명", 1, true, 0))
		) {
			@Override
			public List<String> getInitialArchitecture() {
				return null;
			}

			@Override
			public List<ScenarioOption> getOptions() {
				return null;
			}
		};

		assertThatThrownBy(() -> validator.validate(
				List.of(brokenScenario),
				LearningDocumentSeedCatalog.documentKeys(),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("initialArchitecture must not be null")
				.hasMessageContaining("options must not be null");
	}

	private ScenarioOption optionWithoutEffects() {
		return new ScenarioOption(null, null, "검증 선택지", "설명", 0, false, 0, TradeOffEffects.none()) {
			@Override
			public TradeOffEffects getEffects() {
				return null;
			}
		};
	}
}
