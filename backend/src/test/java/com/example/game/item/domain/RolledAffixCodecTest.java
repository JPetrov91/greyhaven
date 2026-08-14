package com.example.game.item.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class RolledAffixCodecTest {

	@Test
	void roundTripsAffixes() {
		List<RolledAffix> affixes = List.of(
				new RolledAffix("SHARP", AffixKind.PREFIX, 0, 6),
				new RolledAffix("OF_STRENGTH", AffixKind.SUFFIX, 0, 2));

		assertThat(RolledAffixCodec.decode(RolledAffixCodec.encode(affixes))).isEqualTo(affixes);
		assertThat(RolledAffixCodec.decode("")).isEmpty();
	}
}
