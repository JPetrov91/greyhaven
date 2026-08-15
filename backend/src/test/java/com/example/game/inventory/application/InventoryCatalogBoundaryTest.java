package com.example.game.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.infrastructure.ItemDefinitionRepository;

/**
 * Guards IMP-4: inventory reads item definitions through the catalog, not item persistence.
 */
class InventoryCatalogBoundaryTest {

	@Test
	void inventoryApplicationDependsOnItemCatalogNotDefinitionRepository() {
		assertThat(constructorTakes(InventoryApplicationService.class, ItemCatalogService.class)).isTrue();
		assertThat(constructorTakes(InventoryApplicationService.class, ItemDefinitionRepository.class)).isFalse();
		assertThat(constructorTakes(GreyhavenStarterLoadoutGranter.class, ItemCatalogService.class)).isTrue();
		assertThat(constructorTakes(InventoryEquippedWeaponQuery.class, ItemCatalogService.class)).isTrue();
	}

	private static boolean constructorTakes(Class<?> type, Class<?> dependency) {
		return Arrays.stream(type.getConstructors())
				.flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
				.anyMatch(parameter -> parameter == dependency);
	}
}
