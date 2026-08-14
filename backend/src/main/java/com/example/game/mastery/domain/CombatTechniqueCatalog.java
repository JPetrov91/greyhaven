package com.example.game.mastery.domain;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.game.item.domain.WeaponFamily;

/**
 * In-memory technique definitions. Task 6 should resolve {@link TechniqueEffectSpec#effectCode()}
 * without rewriting CombatEngine per new technique.
 */
public final class CombatTechniqueCatalog {

	private final List<CombatTechniqueDefinition> techniques;
	private final Map<String, CombatTechniqueDefinition> byCode;

	public CombatTechniqueCatalog(List<CombatTechniqueDefinition> techniques) {
		this.techniques = List.copyOf(techniques);
		this.byCode = this.techniques.stream()
				.collect(Collectors.toUnmodifiableMap(CombatTechniqueDefinition::code, Function.identity()));
	}

	public List<CombatTechniqueDefinition> all() {
		return techniques;
	}

	public CombatTechniqueDefinition require(String code) {
		CombatTechniqueDefinition definition = byCode.get(code);
		if (definition == null) {
			throw new IllegalArgumentException("Unknown technique: " + code);
		}
		return definition;
	}

	public List<CombatTechniqueDefinition> forFamily(WeaponFamily family) {
		return techniques.stream()
				.filter(technique -> technique.weaponFamily() == family)
				.toList();
	}
}
