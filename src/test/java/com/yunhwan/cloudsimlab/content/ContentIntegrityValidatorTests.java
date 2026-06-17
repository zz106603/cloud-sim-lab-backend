package com.yunhwan.cloudsimlab.content;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.yunhwan.cloudsimlab.architecturepractice.adapter.out.persistence.ArchitecturePracticeSeedCatalog;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeConnection;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeLevel;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeNode;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;
import com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence.LearningDocumentSeedCatalog.SeedDocument;
import com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence.LearningDocumentSeedCatalog;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModulePracticeActivity;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModulePracticeActivityType;
import com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence.CurriculumSeedCatalog;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelation;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelations;
import com.yunhwan.cloudsimlab.scenario.adapter.out.persistence.ScenarioSeedCatalog;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioObservationPoint;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioPrerequisiteConcept;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

class ContentIntegrityValidatorTests {

	private final ContentIntegrityValidator validator = new ContentIntegrityValidator();

	@Test
	void 정상_seed_콘텐츠는_무결성_검증을_통과한다() {
		validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documents(),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules(),
				ArchitecturePracticeSeedCatalog.practices()
		);
	}

	@Test
	void 모듈_검증을_요청하지_않으면_시나리오_relatedModuleIds_참조_검증을_건너뛴다() {
		assertThatCode(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all()
		)).doesNotThrowAnyException();
	}

	@Test
	void 모듈_검증을_명시적으로_요청했는데_모듈_목록이_비어_있으면_relatedModuleIds_참조를_진단한다() {
		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("scenario[single-spring-boot|단일 Spring Boot 배포] references unknown relatedModuleId: single-server-deployment");
	}

	@Test
	void 아키텍처_연습_검증을_요청하지_않으면_모듈의_relatedArchitecturePracticeIds_참조_검증을_건너뛴다() {
		assertThatCode(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules()
		)).doesNotThrowAnyException();
	}

	@Test
	void 문서_seed_검증에서도_아키텍처_연습_검증을_요청하지_않으면_관련_참조_검증을_건너뛴다() {
		assertThatCode(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documents(),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules()
		)).doesNotThrowAnyException();
	}

	@Test
	void 아키텍처_연습_검증을_빈_목록으로_요청하면_모듈의_관련_템플릿_참조를_진단한다() {
		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules(),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningModule[single-server-deployment|단일 서버 배포] references unknown relatedArchitecturePracticeId: architecture-builder-basic")
				.hasMessageContaining("learningModule[alb-private-subnet|ALB와 private subnet 분리] references unknown relatedArchitecturePracticeId: alb-private-subnet-application")
				.hasMessageContaining("learningModule[data-tier-scaling|RDS, Read Replica, Redis 데이터 계층] references unknown relatedArchitecturePracticeId: read-heavy-scaling-practice");
	}

	@Test
	void 문서_seed_검증에서도_아키텍처_연습_검증을_빈_목록으로_요청하면_관련_템플릿_참조를_진단한다() {
		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documents(),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules(),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningModule[single-server-deployment|단일 서버 배포] references unknown relatedArchitecturePracticeId: architecture-builder-basic");
	}

	@Test
	void 학습_문서_메타데이터_참조가_깨지면_진단한다() {
		SeedDocument brokenDocument = new SeedDocument(
				"broken-document",
				"깨진 문서",
				DocumentCategory.COMPUTE,
				DocumentLevel.BEGINNER,
				"요약",
				"broken.md",
				1,
				List.of("missing-document"),
				List.of("EC2"),
				List.of("missing-module"),
				List.of("missing-scenario")
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				List.of(brokenDocument),
				List.of(),
				List.of(),
				List.of()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningDocument[broken-document|깨진 문서] category must follow design document categories: COMPUTE")
				.hasMessageContaining("references unknown prerequisiteDocumentId: missing-document")
				.hasMessageContaining("references unknown relatedModuleId: missing-module")
				.hasMessageContaining("references unknown relatedScenarioId: missing-scenario")
				.hasMessageContaining("relatedScenarioIds must match explicit learning relations");
	}

	@Test
	void 학습_문서_documentKey가_null이어도_관계_검증_NPE가_아닌_문서_검증_오류로_진단한다() {
		SeedDocument brokenDocument = new SeedDocument(
				null,
				"키 누락 문서",
				DocumentCategory.EC2,
				DocumentLevel.BEGINNER,
				"요약",
				"broken.md",
				1,
				List.of(),
				List.of("EC2"),
				List.of("single-server-deployment"),
				List.of("single-spring-boot")
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				List.of(brokenDocument),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules(),
				ArchitecturePracticeSeedCatalog.practices()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningDocument[null|키 누락 문서] documentKey must not be blank");
	}

	@Test
	void 학습_문서_documentKey가_공백이어도_관계_검증_NPE가_아닌_문서_검증_오류로_진단한다() {
		SeedDocument brokenDocument = new SeedDocument(
				" ",
				"공백 키 문서",
				DocumentCategory.EC2,
				DocumentLevel.BEGINNER,
				"요약",
				"broken.md",
				1,
				List.of(),
				List.of("EC2"),
				List.of("single-server-deployment"),
				List.of("single-spring-boot")
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				List.of(brokenDocument),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules(),
				ArchitecturePracticeSeedCatalog.practices()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningDocument[ |공백 키 문서] documentKey must not be blank")
				.hasMessageContaining("learningDocument[ |공백 키 문서] documentKey must not have surrounding whitespace");
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
	void 모듈의_아키텍처_연습_참조가_깨지면_진단한다() {
		LearningPath path = new LearningPath(
				"path",
				"경로",
				"설명",
				"BEGINNER",
				"목표",
				true,
				1,
				List.of("module")
		);
		LearningModule module = new LearningModule(
				"module",
				"path",
				"모듈",
				"설명",
				List.of("목표"),
				List.of(),
				1,
				List.of("ec2-compute-capacity"),
				List.of("single-spring-boot"),
				List.of("missing-practice")
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(path),
				List.of(module),
				ArchitecturePracticeSeedCatalog.practices()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningModule[module|모듈] references unknown relatedArchitecturePracticeId: missing-practice");
	}

	@Test
	void 모듈_실습_활동_대상_ID가_깨지면_진단한다() {
		LearningPath path = new LearningPath(
				"path",
				"경로",
				"설명",
				"BEGINNER",
				"목표",
				true,
				1,
				List.of("module")
		);
		LearningModule module = new LearningModule(
				"module",
				"path",
				"모듈",
				"설명",
				List.of("목표"),
				List.of(),
				1,
				List.of("ec2-compute-capacity"),
				List.of("single-spring-boot"),
				List.of(),
				List.of(
						new LearningModulePracticeActivity(
								"read-missing",
								LearningModulePracticeActivityType.READ_DOCUMENT,
								"누락 문서 읽기",
								"존재하지 않는 문서 대상입니다.",
								"missing-document",
								1
						),
						new LearningModulePracticeActivity(
								"run-mismatch",
								LearningModulePracticeActivityType.RUN_SCENARIO,
								"다른 시나리오 실행",
								"모듈에 포함되지 않은 시나리오 대상입니다.",
								"rds-failure",
								2
						)
				)
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(path),
				List.of(module),
				ArchitecturePracticeSeedCatalog.practices()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("practiceActivity[read-missing] references unknown document targetResourceId: missing-document")
				.hasMessageContaining("practiceActivity[read-missing] targetResourceId must be included in module documentIds: missing-document")
				.hasMessageContaining("practiceActivity[run-mismatch] targetResourceId must be included in module relatedScenarioIds: rds-failure");
	}

	@Test
	void 아키텍처_연습_검증을_요청하지_않아도_모듈_실습_활동의_관련_연습_포함_여부를_진단한다() {
		LearningPath path = new LearningPath(
				"path",
				"경로",
				"설명",
				"BEGINNER",
				"목표",
				true,
				1,
				List.of("module")
		);
		LearningModule module = new LearningModule(
				"module",
				"path",
				"모듈",
				"설명",
				List.of("목표"),
				List.of(),
				1,
				List.of("ec2-compute-capacity"),
				List.of(),
				List.of(),
				List.of(new LearningModulePracticeActivity(
						"build-missing",
						LearningModulePracticeActivityType.BUILD_ARCHITECTURE,
						"누락 연습 작성",
						"모듈에 연결되지 않은 아키텍처 연습 대상입니다.",
						"missing-practice",
						1
				))
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(path),
				List.of(module)
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("practiceActivity[build-missing] targetResourceId must be included in module relatedArchitecturePracticeIds: missing-practice");
	}

	@Test
	void 모듈_실습_활동_목록이_null이면_NPE_없이_진단한다() {
		LearningPath path = new LearningPath(
				"path",
				"경로",
				"설명",
				"BEGINNER",
				"목표",
				true,
				1,
				List.of("module")
		);
		LearningModule module = Mockito.mock(LearningModule.class);
		Mockito.when(module.id()).thenReturn("module");
		Mockito.when(module.pathId()).thenReturn("path");
		Mockito.when(module.title()).thenReturn("모듈");
		Mockito.when(module.description()).thenReturn("설명");
		Mockito.when(module.learningGoals()).thenReturn(List.of("목표"));
		Mockito.when(module.prerequisites()).thenReturn(List.of());
		Mockito.when(module.orderIndex()).thenReturn(1);
		Mockito.when(module.documentIds()).thenReturn(List.of("ec2-compute-capacity"));
		Mockito.when(module.relatedScenarioIds()).thenReturn(List.of());
		Mockito.when(module.relatedArchitecturePracticeIds()).thenReturn(List.of());
		Mockito.when(module.practiceActivities()).thenReturn(null);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(path),
				List.of(module)
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningModule[module|모듈] practiceActivities must not be null");
	}

	@Test
	void 모듈_실습_활동에_적용_활동이_없으면_진단한다() {
		LearningPath path = new LearningPath(
				"path",
				"경로",
				"설명",
				"BEGINNER",
				"목표",
				true,
				1,
				List.of("module")
		);
		LearningModule module = new LearningModule(
				"module",
				"path",
				"모듈",
				"설명",
				List.of("목표"),
				List.of(),
				1,
				List.of("ec2-compute-capacity"),
				List.of("single-spring-boot"),
				List.of(),
				List.of(new LearningModulePracticeActivity(
						"read-ec2",
						LearningModulePracticeActivityType.READ_DOCUMENT,
						"EC2 문서 읽기",
						"EC2 실행 단위를 확인합니다.",
						"ec2-compute-capacity",
						1
				))
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				List.of(path),
				List.of(module)
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("learningModule[module|모듈] practiceActivities must include at least one RUN_SCENARIO or BUILD_ARCHITECTURE activity");
	}

	@Test
	void 아키텍처_연습_템플릿_참조와_starter_연결이_깨지면_진단한다() {
		ArchitecturePracticeTemplate brokenPractice = new ArchitecturePracticeTemplate(
				"broken-practice",
				"깨진 실습",
				"설명",
				ArchitecturePracticeLevel.BEGINNER,
				"목표",
				List.of("지시", " "),
				List.of(new ArchitecturePracticeNode("ec2", UserArchitectureResourceType.EC2, "API 서버")),
				List.of(new ArchitecturePracticeConnection("conn", "ec2", "missing", UserArchitectureConnectionType.REQUEST_FLOW)),
				List.of(UserArchitectureResourceType.EC2, UserArchitectureResourceType.EC2),
				List.of(UserArchitectureConnectionType.REQUEST_FLOW, UserArchitectureConnectionType.REQUEST_FLOW),
				List.of("missing-document"),
				List.of("missing-scenario"),
				List.of("missing-module")
		);

		assertThatThrownBy(() -> validator.validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules(),
				List.of(brokenPractice)
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("architecturePractice[broken-practice|깨진 실습] instructions must not be blank")
				.hasMessageContaining("starterConnection[conn] targetNodeId references unknown starterNode: missing")
				.hasMessageContaining("requiredResourceTypes has duplicated resourceType: EC2")
				.hasMessageContaining("requiredConnectionTypes has duplicated connectionType: REQUEST_FLOW")
				.hasMessageContaining("architecturePractice[broken-practice|깨진 실습] references unknown relatedDocumentId: missing-document")
				.hasMessageContaining("architecturePractice[broken-practice|깨진 실습] references unknown relatedScenarioId: missing-scenario")
				.hasMessageContaining("architecturePractice[broken-practice|깨진 실습] references unknown relatedModuleId: missing-module")
				.hasMessageContaining("learningModule[single-server-deployment|단일 서버 배포] references unknown relatedArchitecturePracticeId: architecture-builder-basic");
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
	void 시나리오_학습_맥락_참조가_깨지면_진단한다() {
		Scenario brokenScenario = Scenario.newScenarioWithLearningContext(
				"single-spring-boot",
				"깨진 학습 맥락 시나리오",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"학습 목표",
				"설명",
				List.of("Client", "EC2", "RDS"),
				List.of("missing-module"),
				List.of(new ScenarioPrerequisiteConcept(
						"missing-concept",
						"누락 개념",
						"missing-document",
						"존재하지 않는 문서를 참조합니다."
				)),
				new ScenarioObservationPoint(
						"병목 지표",
						"장애 지점",
						"요청 흐름",
						"보안 경계",
						"정합성 위험",
						"trade-off 신호"
				),
				List.of("performance", "unknown-perspective"),
				List.of(
						ScenarioOption.newOptionWithGraphKey("add-alb-auto-scaling", "ALB 추가", "설명", 1, true, 0)
				)
		);

		assertThatThrownBy(() -> validator.validate(
				List.of(brokenScenario),
				LearningDocumentSeedCatalog.documentKeys(),
				List.of(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules()
		))
				.isInstanceOf(ContentIntegrityException.class)
				.hasMessageContaining("judgmentPerspectives has unknown perspective: unknown-perspective")
				.hasMessageContaining("references unknown relatedModuleId: missing-module")
				.hasMessageContaining("prerequisiteConcept[missing-concept] references unknown relatedDocumentId: missing-document");
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
