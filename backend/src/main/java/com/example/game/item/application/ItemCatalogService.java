package com.example.game.item.application;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.item.domain.ItemType;
import com.example.game.item.infrastructure.ItemDefinitionEntity;
import com.example.game.item.infrastructure.ItemDefinitionRepository;

/**
 * Read access to item game data for other modules, so they do not depend on item persistence.
 */
@Service
public class ItemCatalogService {

	private final ItemDefinitionRepository itemDefinitionRepository;

	public ItemCatalogService(ItemDefinitionRepository itemDefinitionRepository) {
		this.itemDefinitionRepository = itemDefinitionRepository;
	}

	/**
	 * Resolves definitions in one query. Ids without a definition are absent from the result.
	 */
	@Transactional(readOnly = true)
	public Map<UUID, ItemDefinitionView> findByIds(Collection<UUID> itemDefinitionIds) {
		Map<UUID, ItemDefinitionView> definitions = new HashMap<>();
		for (ItemDefinitionEntity definition : itemDefinitionRepository.findAllById(itemDefinitionIds)) {
			definitions.put(
					definition.getId(),
					toView(definition));
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
		return itemDefinitionRepository.findByCode(itemCode).map(ItemCatalogService::toView);
	}

	/**
	 * Resolves definitions by stable codes in one pass. Unknown codes are absent from the result.
	 */
	@Transactional(readOnly = true)
	public Map<String, ItemDefinitionView> findByCodes(Collection<String> itemCodes) {
		Map<String, ItemDefinitionView> definitions = new HashMap<>();
		for (String code : itemCodes) {
			itemDefinitionRepository.findByCode(code).ifPresent(definition -> definitions.put(
					definition.getCode(),
					toView(definition)));
		}
		return definitions;
	}

	private static ItemDefinitionView toView(ItemDefinitionEntity definition) {
		return new ItemDefinitionView(
				definition.getId(),
				definition.getCode(),
				definition.getName(),
				definition.getType(),
				definition.getRarity());
	}
}

