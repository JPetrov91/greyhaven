package com.example.game.item.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.item.domain.ItemType;
import com.example.game.item.infrastructure.ItemDefinitionEntity;
import com.example.game.item.infrastructure.ItemDefinitionModifierEntity;
import com.example.game.item.infrastructure.ItemDefinitionModifierRepository;
import com.example.game.item.infrastructure.ItemDefinitionRepository;

/**
 * Read access to item game data for other modules, so they do not depend on item persistence.
 */
@Service
public class ItemCatalogService {

	private final ItemDefinitionRepository itemDefinitionRepository;
	private final ItemDefinitionModifierRepository itemDefinitionModifierRepository;

	public ItemCatalogService(
			ItemDefinitionRepository itemDefinitionRepository,
			ItemDefinitionModifierRepository itemDefinitionModifierRepository) {
		this.itemDefinitionRepository = itemDefinitionRepository;
		this.itemDefinitionModifierRepository = itemDefinitionModifierRepository;
	}

	/**
	 * Resolves definitions in one query. Ids without a definition are absent from the result.
	 */
	@Transactional(readOnly = true)
	public Map<UUID, ItemDefinitionView> findByIds(Collection<UUID> itemDefinitionIds) {
		List<ItemDefinitionEntity> entities = itemDefinitionRepository.findAllById(itemDefinitionIds);
		Map<UUID, List<ItemModifierView>> modifiers = loadModifiers(
				entities.stream().map(ItemDefinitionEntity::getId).toList());
		Map<UUID, ItemDefinitionView> definitions = new HashMap<>();
		for (ItemDefinitionEntity definition : entities) {
			definitions.put(definition.getId(), toView(definition, modifiers));
		}
		return definitions;
	}

	/**
	 * Definition ids of one item type, so other modules can filter their own rows by type without
	 * joining item persistence.
	 */
	@Transactional(readOnly = true)
	public Set<UUID> idsOfType(ItemType itemType) {
		return new HashSet<>(itemDefinitionRepository.findIdsByType(itemType));
	}

	@Transactional(readOnly = true)
	public Optional<ItemDefinitionView> findByCode(String itemCode) {
		return itemDefinitionRepository.findByCode(itemCode).map(definition -> toView(
				definition,
				loadModifiers(List.of(definition.getId()))));
	}

	/**
	 * Resolves definitions by stable codes in one pass. Unknown codes are absent from the result.
	 */
	@Transactional(readOnly = true)
	public Map<String, ItemDefinitionView> findByCodes(Collection<String> itemCodes) {
		Map<String, ItemDefinitionView> definitions = new HashMap<>();
		if (itemCodes == null || itemCodes.isEmpty()) {
			return definitions;
		}
		List<ItemDefinitionEntity> entities = itemDefinitionRepository.findByCodeIn(itemCodes);
		Map<UUID, List<ItemModifierView>> modifiers = loadModifiers(
				entities.stream().map(ItemDefinitionEntity::getId).toList());
		for (ItemDefinitionEntity definition : entities) {
			definitions.put(definition.getCode(), toView(definition, modifiers));
		}
		return definitions;
	}

	private Map<UUID, List<ItemModifierView>> loadModifiers(Collection<UUID> definitionIds) {
		Map<UUID, List<ItemModifierView>> modifiers = new HashMap<>();
		if (definitionIds.isEmpty()) {
			return modifiers;
		}
		for (ItemDefinitionModifierEntity row : itemDefinitionModifierRepository.findByItemDefinitionIdIn(definitionIds)) {
			modifiers.computeIfAbsent(row.getItemDefinitionId(), key -> new ArrayList<>())
					.add(new ItemModifierView(row.getStat(), row.getMagnitude()));
		}
		return modifiers;
	}

	private static ItemDefinitionView toView(
			ItemDefinitionEntity definition,
			Map<UUID, List<ItemModifierView>> modifiers) {
		return new ItemDefinitionView(
				definition.getId(),
				definition.getCode(),
				definition.getName(),
				definition.getDescription(),
				definition.getType(),
				definition.getRarity(),
				definition.getBaseValue(),
				definition.getRequiredLevel(),
				definition.getWeaponDamage(),
				definition.getArmorValue(),
				definition.getHealAmount(),
				definition.isTwoHanded(),
				definition.getEquipmentSlot(),
				definition.getWeaponFamily(),
				definition.getArmorCategory(),
				definition.getRequiredStrength(),
				definition.getRequiredAgility(),
				definition.getRequiredEndurance(),
				definition.getRequiredPerception(),
				List.copyOf(modifiers.getOrDefault(definition.getId(), List.of())));
	}
}
