package com.example.game.sparring.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.domain.ProgressionBalance;
import com.example.game.combat.domain.EnemyAiArchetype;
import com.example.game.shared.domain.MutableRandomProvider;

class SparringBotGeneratorTest {

	@Test
	void namesAndArchetypesFollowLevel() {
		assertThat(SparringBots.nameForLevel(1)).isEqualTo("Green Recruit");
		assertThat(SparringBots.nameForLevel(10)).isEqualTo("Watch Provost");
		assertThat(SparringBots.codeForLevel(3)).isEqualTo("SPARRING_BOT_L03");
		assertThat(SparringBotGenerator.archetypeFor(1)).isEqualTo(EnemyAiArchetype.AGGRESSIVE);
		assertThat(SparringBotGenerator.archetypeFor(5)).isEqualTo(EnemyAiArchetype.DEFENSIVE);
		assertThat(SparringBotGenerator.archetypeFor(10)).isEqualTo(EnemyAiArchetype.ARMORED);
	}

	@Test
	void generationIsDeterministicAndScalesWithLevel() {
		MutableRandomProvider first = new MutableRandomProvider();
		first.queue(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		MutableRandomProvider second = new MutableRandomProvider();
		second.queue(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		SparringBotProfile a = SparringBotGenerator.generate(5, first);
		SparringBotProfile b = SparringBotGenerator.generate(5, second);
		assertThat(a).isEqualTo(b);
		assertThat(a.name()).isEqualTo("Militia Drillman");
		assertThat(a.level()).isEqualTo(5);
		int expectedStrength = CharacterBalance.STARTING_STRENGTH
				+ (4 * ProgressionBalance.ATTRIBUTE_POINTS_PER_LEVEL);
		assertThat(a.strength()).isEqualTo(expectedStrength);
		assertThat(a.maxHealth()).isGreaterThan(SparringBotGenerator.generate(1, new MutableRandomProvider()).maxHealth());
		assertThat(a.damageMax()).isGreaterThan(SparringBotGenerator.generate(1, new MutableRandomProvider()).damageMax());
	}

	@Test
	void rejectsOutOfRangeLevel() {
		assertThatThrownBy(() -> SparringBotGenerator.generate(11, new MutableRandomProvider()))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
