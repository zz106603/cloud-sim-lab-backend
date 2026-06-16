package com.yunhwan.cloudsimlab.architecturepractice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

class ArchitecturePracticeTemplateTests {

	@Test
	void 템플릿_필수_문자열은_null이나_blank일_수_없다() {
		assertThatThrownBy(() -> template(null, "제목", "설명", "목표"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Architecture practice id must not be blank");

		assertThatThrownBy(() -> template("practice", " ", "설명", "목표"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Architecture practice title must not be blank");

		assertThatThrownBy(() -> template("practice", "제목", "", "목표"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Architecture practice description must not be blank");

		assertThatThrownBy(() -> template("practice", "제목", "설명", null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Architecture practice learningGoal must not be blank");
	}

	@Test
	void 템플릿_필수_객체와_컬렉션은_null일_수_없다() {
		assertThatThrownBy(() -> new ArchitecturePracticeTemplate(
				"practice",
				"제목",
				"설명",
				null,
				"목표",
				List.of("지시"),
				List.of(),
				List.of(),
				List.of(UserArchitectureResourceType.EC2),
				List.of(UserArchitectureConnectionType.REQUEST_FLOW),
				List.of("ec2-compute-capacity"),
				List.of("single-spring-boot"),
				List.of("single-server-deployment")
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Architecture practice level must not be null");

		assertThatThrownBy(() -> new ArchitecturePracticeTemplate(
				"practice",
				"제목",
				"설명",
				ArchitecturePracticeLevel.BEGINNER,
				"목표",
				null,
				List.of(),
				List.of(),
				List.of(UserArchitectureResourceType.EC2),
				List.of(UserArchitectureConnectionType.REQUEST_FLOW),
				List.of("ec2-compute-capacity"),
				List.of("single-spring-boot"),
				List.of("single-server-deployment")
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Architecture practice instructions must not be null");
	}

	@Test
	void 템플릿_컬렉션은_null_원소를_허용하지_않고_방어적으로_복사된다() {
		List<String> instructions = new ArrayList<>();
		instructions.add("지시");
		ArchitecturePracticeTemplate template = new ArchitecturePracticeTemplate(
				"practice",
				"제목",
				"설명",
				ArchitecturePracticeLevel.BEGINNER,
				"목표",
				instructions,
				List.of(),
				List.of(),
				List.of(UserArchitectureResourceType.EC2),
				List.of(UserArchitectureConnectionType.REQUEST_FLOW),
				List.of("ec2-compute-capacity"),
				List.of("single-spring-boot"),
				List.of("single-server-deployment")
		);

		instructions.add("추가 지시");

		assertThat(template.instructions()).containsExactly("지시");
		assertThatThrownBy(() -> template.instructions().add("변경"))
				.isInstanceOf(UnsupportedOperationException.class);

		List<String> instructionsWithNull = new ArrayList<>();
		instructionsWithNull.add(null);
		assertThatThrownBy(() -> new ArchitecturePracticeTemplate(
				"practice",
				"제목",
				"설명",
				ArchitecturePracticeLevel.BEGINNER,
				"목표",
				instructionsWithNull,
				List.of(),
				List.of(),
				List.of(UserArchitectureResourceType.EC2),
				List.of(UserArchitectureConnectionType.REQUEST_FLOW),
				List.of("ec2-compute-capacity"),
				List.of("single-spring-boot"),
				List.of("single-server-deployment")
		))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void starter_node와_connection은_필수값을_검증한다() {
		assertThatThrownBy(() -> new ArchitecturePracticeNode("", UserArchitectureResourceType.EC2, "API 서버"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Architecture practice node id must not be blank");

		assertThatThrownBy(() -> new ArchitecturePracticeNode("ec2", null, "API 서버"))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Architecture practice node resourceType must not be null");

		assertThatThrownBy(() -> new ArchitecturePracticeConnection("conn", "source", " ", UserArchitectureConnectionType.REQUEST_FLOW))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Architecture practice connection targetNodeId must not be blank");

		assertThatThrownBy(() -> new ArchitecturePracticeConnection("conn", "source", "target", null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Architecture practice connection connectionType must not be null");
	}

	private ArchitecturePracticeTemplate template(String id, String title, String description, String learningGoal) {
		return new ArchitecturePracticeTemplate(
				id,
				title,
				description,
				ArchitecturePracticeLevel.BEGINNER,
				learningGoal,
				List.of("지시"),
				List.of(),
				List.of(),
				List.of(UserArchitectureResourceType.EC2),
				List.of(UserArchitectureConnectionType.REQUEST_FLOW),
				List.of("ec2-compute-capacity"),
				List.of("single-spring-boot"),
				List.of("single-server-deployment")
		);
	}
}
