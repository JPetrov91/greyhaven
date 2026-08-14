package com.example.game.pvp.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.CharacterApplicationService;
import com.example.game.character.application.CharacterPublicCore;
import com.example.game.inventory.application.EquippedWeaponQuery;
import com.example.game.inventory.application.PublicEquipmentQuery;
import com.example.game.inventory.application.PublicEquippedItemView;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.mastery.application.TechniqueLoadoutQuery;
import com.example.game.pvp.application.PublicCharacterView.PublicAffixView;

@Service
public class PvpInspectApplicationService {

	private final CharacterApplicationService characterApplicationService;
	private final PublicEquipmentQuery publicEquipmentQuery;
	private final TechniqueLoadoutQuery techniqueLoadoutQuery;
	private final EquippedWeaponQuery equippedWeaponQuery;

	public PvpInspectApplicationService(
			CharacterApplicationService characterApplicationService,
			PublicEquipmentQuery publicEquipmentQuery,
			TechniqueLoadoutQuery techniqueLoadoutQuery,
			EquippedWeaponQuery equippedWeaponQuery) {
		this.characterApplicationService = characterApplicationService;
		this.publicEquipmentQuery = publicEquipmentQuery;
		this.techniqueLoadoutQuery = techniqueLoadoutQuery;
		this.equippedWeaponQuery = equippedWeaponQuery;
	}

	@Transactional(readOnly = true)
	public PublicCharacterView inspect(UUID characterId) {
		CharacterPublicCore core = characterApplicationService.requirePublic(characterId);
		List<PublicEquippedItemView> equipped = publicEquipmentQuery.equippedItems(characterId);
		WeaponFamily family = equippedWeaponQuery.mainHandFamily(characterId).orElse(null);
		Integer mastery = family == null ? null : techniqueLoadoutQuery.masteryLevel(characterId, family);
		List<PublicCharacterView.PublicEquippedItemView> equipment = new ArrayList<>();
		for (PublicEquippedItemView item : equipped) {
			equipment.add(new PublicCharacterView.PublicEquippedItemView(
					item.slot(),
					item.code(),
					item.displayName(),
					item.rarity(),
					item.weaponDamage(),
					item.armorValue(),
					item.affixes().stream()
							.map(affix -> new PublicAffixView(
									affix.code(),
									affix.displayName(),
									affix.stat().name(),
									affix.magnitude()))
							.toList()));
		}
		return new PublicCharacterView(
				core.id(),
				core.name(),
				core.level(),
				core.strength(),
				core.agility(),
				core.endurance(),
				core.perception(),
				core.arenaRating(),
				family,
				mastery,
				techniqueLoadoutQuery.activeTechniqueCodes(characterId),
				equipment);
	}
}
