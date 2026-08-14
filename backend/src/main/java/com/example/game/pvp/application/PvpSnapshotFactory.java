package com.example.game.pvp.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.CharacterPublicCore;
import com.example.game.character.application.CharacterApplicationService;
import com.example.game.character.application.EquippedBonusProvider;
import com.example.game.character.application.EquippedBonuses;
import com.example.game.character.domain.CharacterStatCalculator;
import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.combat.domain.CombatantStats;
import com.example.game.inventory.application.EquippedWeaponQuery;
import com.example.game.inventory.application.HealingPotionConsumption;
import com.example.game.inventory.application.HealingPotionStock;
import com.example.game.inventory.application.PublicEquipmentQuery;
import com.example.game.inventory.application.PublicEquippedItemView;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.mastery.application.CombatTechniqueCatalogService;
import com.example.game.mastery.application.TechniqueLoadoutQuery;
import com.example.game.mastery.domain.CombatTechniqueCatalog;
import com.example.game.mastery.domain.CombatTechniqueDefinition;
import com.example.game.mastery.domain.TechniqueEffectSpec;
import com.example.game.mastery.domain.TechniqueKind;
import com.example.game.pvp.domain.ArenaDefenseStrategy;
import com.example.game.pvp.domain.PvpAffixSnapshot;
import com.example.game.pvp.domain.PvpCombatantSnapshot;
import com.example.game.pvp.domain.PvpEquippedItemSnapshot;
import com.example.game.pvp.domain.PvpMatchSnapshot;
import com.example.game.pvp.domain.PvPBalance;

@Component
public class PvpSnapshotFactory {

	private final CharacterApplicationService characterApplicationService;
	private final EquippedBonusProvider equippedBonusProvider;
	private final PublicEquipmentQuery publicEquipmentQuery;
	private final HealingPotionConsumption healingPotionConsumption;
	private final TechniqueLoadoutQuery techniqueLoadoutQuery;
	private final CombatTechniqueCatalogService combatTechniqueCatalogService;
	private final EquippedWeaponQuery equippedWeaponQuery;

	public PvpSnapshotFactory(
			CharacterApplicationService characterApplicationService,
			EquippedBonusProvider equippedBonusProvider,
			PublicEquipmentQuery publicEquipmentQuery,
			HealingPotionConsumption healingPotionConsumption,
			TechniqueLoadoutQuery techniqueLoadoutQuery,
			CombatTechniqueCatalogService combatTechniqueCatalogService,
			EquippedWeaponQuery equippedWeaponQuery) {
		this.characterApplicationService = characterApplicationService;
		this.equippedBonusProvider = equippedBonusProvider;
		this.publicEquipmentQuery = publicEquipmentQuery;
		this.healingPotionConsumption = healingPotionConsumption;
		this.techniqueLoadoutQuery = techniqueLoadoutQuery;
		this.combatTechniqueCatalogService = combatTechniqueCatalogService;
		this.equippedWeaponQuery = equippedWeaponQuery;
	}

	@Transactional
	public PvpMatchSnapshot capture(UUID attackerId, UUID defenderId, ArenaDefenseStrategy defense) {
		return new PvpMatchSnapshot(
				PvPBalance.SNAPSHOT_VERSION,
				captureCombatant(attackerId),
				captureCombatant(defenderId),
				defense);
	}

	PvpCombatantSnapshot captureCombatant(UUID characterId) {
		CharacterPublicCore core = characterApplicationService.requirePublic(characterId);
		EquippedBonuses bonuses = equippedBonusProvider.bonusesFor(characterId);
		DerivedCombatStats derived = CharacterStatCalculator.calculate(
				core.strength(),
				core.agility(),
				core.perception(),
				bonuses.weaponDamage(),
				bonuses.armorValue(),
				bonuses.accuracy(),
				bonuses.dodge(),
				bonuses.criticalChance(),
				bonuses.strength(),
				bonuses.agility(),
				bonuses.endurance(),
				bonuses.perception());
		int maxHealth = core.maxHealth();
		int maxStamina = core.maxStamina();
		WeaponFamily family = equippedWeaponQuery.mainHandFamily(characterId).orElse(null);
		List<PublicEquippedItemView> equipped = publicEquipmentQuery.equippedItems(characterId);
		List<String> codes = techniqueLoadoutQuery.activeTechniqueCodes(characterId);
		CombatTechniqueCatalog catalog = combatTechniqueCatalogService.load();
		Map<String, TechniqueEffectSpec> specs = new LinkedHashMap<>();
		for (String code : codes) {
			specs.put(code, catalog.require(code).effect());
		}
		WeaponFamily loadoutFamily = codes.isEmpty() ? family : catalog.require(codes.get(0)).weaponFamily();
		if (family == null) {
			family = loadoutFamily;
		}
		TechniqueEffectSpec passive = masteryPassive(characterId, family, catalog);
		HealingPotionStock potions = healingPotionConsumption.consumeUpTo(
				characterId, PvPBalance.MAX_SNAPSHOT_POTIONS);
		int charges = potions.quantity();
		return new PvpCombatantSnapshot(
				core.name(),
				core.level(),
				core.strength(),
				core.agility(),
				core.endurance(),
				core.perception(),
				maxHealth,
				maxStamina,
				new CombatantStats(
						derived.physicalDamage(),
						derived.accuracy(),
						derived.dodge(),
						derived.criticalChance(),
						derived.armor(),
						core.agility() + bonuses.agility()),
				family,
				bonuses.staminaCostReduction(),
				codes,
				specs,
				passive,
				charges,
				potions.healAmount(),
				equipped.stream()
						.map(item -> new PvpEquippedItemSnapshot(
								item.slot(),
								item.code(),
								item.displayName(),
								item.rarity(),
								item.weaponDamage(),
								item.armorValue(),
								item.affixes().stream()
										.map(affix -> new PvpAffixSnapshot(
												affix.code(),
												affix.displayName(),
												affix.stat().name(),
												affix.magnitude()))
										.toList()))
						.toList());
	}

	private TechniqueEffectSpec masteryPassive(
			UUID characterId,
			WeaponFamily family,
			CombatTechniqueCatalog catalog) {
		if (family == null || techniqueLoadoutQuery.masteryLevel(characterId, family) < 10) {
			return null;
		}
		return catalog.forFamily(family).stream()
				.filter(definition -> definition.kind() == TechniqueKind.PASSIVE)
				.map(CombatTechniqueDefinition::effect)
				.findFirst()
				.orElse(null);
	}
}
