package com.yunhwan.cloudsimlab.content;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence.LearningDocumentSeedCatalog;
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
				LearningRelations.all()
		);
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

	private ScenarioOption optionWithoutEffects() {
		return new ScenarioOption(null, null, "검증 선택지", "설명", 0, false, 0, TradeOffEffects.none()) {
			@Override
			public TradeOffEffects getEffects() {
				return null;
			}
		};
	}
}
