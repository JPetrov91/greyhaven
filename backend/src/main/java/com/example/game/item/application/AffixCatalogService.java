package com.example.game.item.application;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.item.domain.AffixCatalog;
import com.example.game.item.domain.AffixDefinition;
import com.example.game.item.infrastructure.AffixDefinitionEntity;
import com.example.game.item.infrastructure.AffixDefinitionRepository;

@Service
public class AffixCatalogService {

	private final AffixDefinitionRepository affixDefinitionRepository;

	public AffixCatalogService(AffixDefinitionRepository affixDefinitionRepository) {
		this.affixDefinitionRepository = affixDefinitionRepository;
	}

	@Transactional(readOnly = true)
	public AffixCatalog load() {
		List<AffixDefinition> affixes = affixDefinitionRepository.findAll(Sort.by("code")).stream()
				.map(AffixCatalogService::toDomain)
				.toList();
		return new AffixCatalog(affixes);
	}

	private static AffixDefinition toDomain(AffixDefinitionEntity entity) {
		return new AffixDefinition(
				entity.getCode(),
				entity.getKind(),
				entity.getDisplayName(),
				entity.getStat(),
				entity.getMagnitudeMin(),
				entity.getMagnitudeMax(),
				AffixDefinition.parseItemTypes(entity.getAllowedItemTypes()),
				AffixDefinition.parseSlots(entity.getAllowedEquipmentSlots()),
				AffixDefinition.parseFamilies(entity.getAllowedWeaponFamilies()),
				AffixDefinition.parseCategories(entity.getAllowedArmorCategories()));
	}
}
