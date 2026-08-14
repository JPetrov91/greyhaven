package com.example.game.crafting.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.activity.domain.ActivityType;
import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.crafting.domain.CraftingBalance;
import com.example.game.crafting.domain.CraftingJobStatus;
import com.example.game.crafting.domain.CraftingResolver;
import com.example.game.crafting.domain.Profession;
import com.example.game.crafting.domain.ProfessionProgression;
import com.example.game.crafting.domain.RecipeValidator;
import com.example.game.crafting.domain.SalvageCalculator;
import com.example.game.crafting.infrastructure.CharacterProfessionEntity;
import com.example.game.crafting.infrastructure.CharacterProfessionRepository;
import com.example.game.crafting.infrastructure.CraftingJobEntity;
import com.example.game.crafting.infrastructure.CraftingJobRepository;
import com.example.game.crafting.infrastructure.CraftingRecipeEntity;
import com.example.game.crafting.infrastructure.CraftingRecipeInputEntity;
import com.example.game.crafting.infrastructure.CraftingRecipeInputRepository;
import com.example.game.crafting.infrastructure.CraftingRecipeRepository;
import com.example.game.crafting.infrastructure.SalvageOutputEntity;
import com.example.game.crafting.infrastructure.SalvageOutputRepository;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.application.InventoryFullException;
import com.example.game.inventory.application.SalvageSourceSnapshot;
import com.example.game.inventory.domain.InventoryBalance;
import com.example.game.item.application.AffixCatalogService;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.item.domain.GeneratedItem;
import com.example.game.item.domain.RolledAffixCodec;
import com.example.game.shared.domain.RandomProvider;
import com.example.game.world.application.LocationView;
import com.example.game.world.application.WorldApplicationService;
import com.example.game.world.domain.LocationAction;
import com.example.game.world.domain.LocationActions;

@Service
public class CraftingApplicationService {

	private static final List<CraftingJobStatus> OPEN_STATUSES = List.of(
			CraftingJobStatus.ACTIVE,
			CraftingJobStatus.COMPLETED);

	private final CharacterVitalsService characterVitalsService;
	private final CharacterLocationService characterLocationService;
	private final CharacterCombatGuard characterCombatGuard;
	private final WorldApplicationService worldApplicationService;
	private final InventoryApplicationService inventoryApplicationService;
	private final ItemCatalogService itemCatalogService;
	private final AffixCatalogService affixCatalogService;
	private final ActivityApplicationService activityApplicationService;
	private final CharacterProfessionRepository characterProfessionRepository;
	private final CraftingRecipeRepository craftingRecipeRepository;
	private final CraftingRecipeInputRepository craftingRecipeInputRepository;
	private final CraftingJobRepository craftingJobRepository;
	private final SalvageOutputRepository salvageOutputRepository;
	private final RandomProvider randomProvider;
	private final Clock clock;

	public CraftingApplicationService(
			CharacterVitalsService characterVitalsService,
			CharacterLocationService characterLocationService,
			CharacterCombatGuard characterCombatGuard,
			WorldApplicationService worldApplicationService,
			InventoryApplicationService inventoryApplicationService,
			ItemCatalogService itemCatalogService,
			AffixCatalogService affixCatalogService,
			ActivityApplicationService activityApplicationService,
			CharacterProfessionRepository characterProfessionRepository,
			CraftingRecipeRepository craftingRecipeRepository,
			CraftingRecipeInputRepository craftingRecipeInputRepository,
			CraftingJobRepository craftingJobRepository,
			SalvageOutputRepository salvageOutputRepository,
			RandomProvider randomProvider,
			Clock clock) {
		this.characterVitalsService = characterVitalsService;
		this.characterLocationService = characterLocationService;
		this.characterCombatGuard = characterCombatGuard;
		this.worldApplicationService = worldApplicationService;
		this.inventoryApplicationService = inventoryApplicationService;
		this.itemCatalogService = itemCatalogService;
		this.affixCatalogService = affixCatalogService;
		this.activityApplicationService = activityApplicationService;
		this.characterProfessionRepository = characterProfessionRepository;
		this.craftingRecipeRepository = craftingRecipeRepository;
		this.craftingRecipeInputRepository = craftingRecipeInputRepository;
		this.craftingJobRepository = craftingJobRepository;
		this.salvageOutputRepository = salvageOutputRepository;
		this.randomProvider = randomProvider;
		this.clock = clock;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void initializeForCharacter(UUID characterId) {
		ensureProfessions(characterId);
	}

	@Transactional
	public List<ProfessionView> professions(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		ensureProfessions(vitals.characterId());
		return toProfessionViews(vitals.characterId());
	}

	@Transactional
	public List<RecipeView> recipes(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		ensureProfessions(vitals.characterId());
		Map<Profession, CharacterProfessionEntity> professions = professionsByType(vitals.characterId());
		List<CraftingRecipeEntity> recipes = craftingRecipeRepository
				.findAllByOrderByProfessionAscRequiredProfessionRankAscCodeAsc();
		Map<UUID, List<CraftingRecipeInputEntity>> inputsByRecipe = new HashMap<>();
		List<UUID> recipeIds = recipes.stream().map(CraftingRecipeEntity::getId).toList();
		for (CraftingRecipeInputEntity input : craftingRecipeInputRepository.findByRecipeIdIn(recipeIds)) {
			inputsByRecipe.computeIfAbsent(input.getRecipeId(), key -> new ArrayList<>()).add(input);
		}
		return recipes.stream()
				.map(recipe -> toRecipeView(recipe, inputsByRecipe.getOrDefault(recipe.getId(), List.of()), vitals, professions))
				.toList();
	}

	@Transactional
	public CraftingJobView start(UUID accountId, String recipeCode) {
		if (recipeCode == null || recipeCode.isBlank()) {
			throw CraftingErrors.recipeNotFound();
		}
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		assertAtWard(accountId, LocationAction.CRAFT);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());
		ensureProfessions(vitals.characterId());

		if (craftingJobRepository.existsByCharacterIdAndStatusIn(vitals.characterId(), OPEN_STATUSES)) {
			throw CraftingErrors.jobInProgress();
		}

		CraftingRecipeEntity recipe = craftingRecipeRepository.findByCode(recipeCode.trim())
				.orElseThrow(CraftingErrors::recipeNotFound);
		CharacterProfessionEntity profession = characterProfessionRepository
				.findWithLockByCharacterIdAndProfession(vitals.characterId(), recipe.getProfession())
				.orElseThrow(() -> new IllegalStateException("profession row missing"));

		List<CraftingRecipeInputEntity> inputRows = craftingRecipeInputRepository.findByRecipeId(recipe.getId());
		inputRows.sort(Comparator.comparing(CraftingRecipeInputEntity::getItemDefinitionId));
		List<RecipeValidator.RecipeInput> inputs = new ArrayList<>();
		Map<String, Integer> available = new HashMap<>();
		Map<UUID, ItemDefinitionView> inputDefinitions = itemCatalogService.findByIds(
				inputRows.stream().map(CraftingRecipeInputEntity::getItemDefinitionId).toList());
		for (CraftingRecipeInputEntity input : inputRows) {
			ItemDefinitionView definition = inputDefinitions.get(input.getItemDefinitionId());
			if (definition == null) {
				throw CraftingErrors.itemDefinitionMissing(input.getItemDefinitionId().toString());
			}
			inputs.add(new RecipeValidator.RecipeInput(definition.code(), input.getQuantity()));
			available.put(
					definition.code(),
					inventoryApplicationService.unreservedQuantityByCode(vitals.characterId(), definition.code()));
		}

		RecipeValidator.Failure failure = RecipeValidator.validate(
				new RecipeValidator.RecipeRequirement(
						recipe.getProfession(),
						recipe.getRequiredProfessionRank(),
						recipe.getRequiredCharacterLevel(),
						recipe.getGoldCost(),
						List.copyOf(inputs)),
				profession.getRank(),
				vitals.level(),
				vitals.gold(),
				available);
		if (failure == RecipeValidator.Failure.PROFESSION_RANK) {
			throw CraftingErrors.professionRankTooLow();
		}
		if (failure == RecipeValidator.Failure.CHARACTER_LEVEL) {
			throw CraftingErrors.characterLevelTooLow();
		}
		if (failure == RecipeValidator.Failure.GOLD) {
			throw CraftingErrors.insufficientGold();
		}
		if (failure == RecipeValidator.Failure.MATERIALS) {
			throw CraftingErrors.missingMaterials();
		}

		for (RecipeValidator.RecipeInput input : inputs) {
			inventoryApplicationService.consumeUnreservedByCode(vitals.characterId(), input.itemCode(), input.quantity());
		}
		if (recipe.getGoldCost() > 0) {
			characterVitalsService.spendGold(vitals.characterId(), recipe.getGoldCost());
		}

		ItemDefinitionView output = itemCatalogService.findByIds(List.of(recipe.getOutputItemDefinitionId()))
				.get(recipe.getOutputItemDefinitionId());
		if (output == null) {
			throw CraftingErrors.itemDefinitionMissing(recipe.getOutputItemDefinitionId().toString());
		}

		Instant now = Instant.now(clock);
		CraftingResolver.PlannedCraft planned = CraftingResolver.resolve(
				now,
				recipe.getDurationSeconds(),
				output.toData(),
				affixCatalogService.load(),
				randomProvider,
				recipe.getMinRarity(),
				recipe.getMaxRarity(),
				profession.getRank(),
				recipe.getProfessionXp());

		CraftingJobEntity job = new CraftingJobEntity(
				UUID.randomUUID(),
				vitals.characterId(),
				recipe.getId(),
				recipe.getProfession(),
				now,
				planned.completesAt(),
				output.id(),
				output.code(),
				recipe.getOutputQuantity(),
				planned.generated().rarity(),
				planned.generated().rolledWeaponDamage(),
				planned.generated().rolledArmorValue(),
				RolledAffixCodec.encode(planned.generated().affixes()),
				planned.professionXp(),
				now);
		try {
			craftingJobRepository.saveAndFlush(job);
		}
		catch (DataIntegrityViolationException exception) {
			throw CraftingErrors.jobInProgress();
		}

		activityApplicationService.record(
				vitals.characterId(),
				ActivityType.CRAFTING_STARTED,
				"You began crafting " + recipe.getName() + ".");
		return toJobView(job, recipe.getCode(), recipe.getName(), output.name());
	}

	@Transactional
	public CraftingJobView current(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		List<CraftingJobEntity> open = craftingJobRepository.findWithLockByCharacterIdAndStatusIn(
				vitals.characterId(),
				OPEN_STATUSES);
		if (open.isEmpty()) {
			return null;
		}
		CraftingJobEntity job = open.get(0);
		completeIfDue(job, Instant.now(clock));
		return toJobView(job);
	}

	@Transactional
	public CraftingJobView claim(UUID accountId, UUID jobId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		assertAtWard(accountId, LocationAction.CLAIM_CRAFT);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());

		CraftingJobEntity job = craftingJobRepository.findWithLockById(jobId)
				.orElseThrow(CraftingErrors::jobNotFound);
		if (!job.getCharacterId().equals(vitals.characterId())) {
			throw CraftingErrors.jobNotFound();
		}
		completeIfDue(job, Instant.now(clock));
		if (job.getStatus() == CraftingJobStatus.CLAIMED) {
			throw CraftingErrors.jobAlreadyClaimed();
		}
		if (job.getStatus() != CraftingJobStatus.COMPLETED) {
			throw CraftingErrors.jobNotReady();
		}

		GeneratedItem generated = new GeneratedItem(
				job.getRarity(),
				job.getRolledWeaponDamage(),
				job.getRolledArmorValue(),
				RolledAffixCodec.decode(job.getRolledAffixes()));
		try {
			inventoryApplicationService.grantRolled(
					vitals.characterId(),
					job.getOutputItemCode(),
					job.getOutputQuantity(),
					generated);
		}
		catch (InventoryFullException exception) {
			throw CraftingErrors.rewardsNeedInventorySpace();
		}

		CharacterProfessionEntity profession = characterProfessionRepository
				.findWithLockByCharacterIdAndProfession(vitals.characterId(), job.getProfession())
				.orElseThrow(() -> new IllegalStateException("profession row missing"));
		int previousRank = profession.getRank();
		ProfessionProgression.ProgressionResult progress = ProfessionProgression.applyExperience(
				profession.getRank(),
				profession.getXp(),
				job.getProfessionXpPlanned());
		profession.apply(progress.rank(), progress.experience(), Instant.now(clock));
		characterProfessionRepository.saveAndFlush(profession);

		job.markClaimed(Instant.now(clock));
		craftingJobRepository.saveAndFlush(job);

		ItemDefinitionView output = requireOutput(job.getOutputItemDefinitionId());
		CraftingRecipeEntity recipe = craftingRecipeRepository.findById(job.getRecipeId()).orElse(null);
		String recipeName = recipe == null ? output.name() : recipe.getName();
		activityApplicationService.record(
				vitals.characterId(),
				ActivityType.CRAFTING_CLAIMED,
				"You finished " + recipeName + ".");
		if (progress.rank() > previousRank) {
			activityApplicationService.record(
					vitals.characterId(),
					ActivityType.PROFESSION_RANK_UP,
					job.getProfession().name() + " reached rank " + progress.rank() + ".");
		}
		return toJobView(job, recipe == null ? output.code() : recipe.getCode(), recipeName, output.name());
	}

	@Transactional
	public SalvageView salvage(UUID accountId, UUID itemInstanceId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		assertAtWard(accountId, LocationAction.SALVAGE);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());

		SalvageSourceSnapshot source = inventoryApplicationService.requireSalvageSource(
				vitals.characterId(),
				itemInstanceId);
		if (source.equipped()) {
			throw CraftingErrors.cannotSalvageEquipped();
		}
		if (source.listedQuantity() > 0) {
			throw CraftingErrors.cannotSalvageListed();
		}
		if (!SalvageCalculator.isSalvageable(source.type())) {
			throw CraftingErrors.cannotSalvageItem();
		}

		List<SalvageOutputEntity> catalogRows = salvageOutputRepository.findBySourceItemDefinitionId(
				source.itemDefinitionId());
		if (catalogRows.isEmpty()) {
			throw CraftingErrors.cannotSalvageItem();
		}
		Map<UUID, ItemDefinitionView> results = itemCatalogService.findByIds(
				catalogRows.stream().map(SalvageOutputEntity::getResultItemDefinitionId).toList());
		List<SalvageCalculator.CatalogLine> catalog = new ArrayList<>();
		for (SalvageOutputEntity row : catalogRows) {
			ItemDefinitionView result = results.get(row.getResultItemDefinitionId());
			if (result == null) {
				throw CraftingErrors.itemDefinitionMissing(row.getResultItemDefinitionId().toString());
			}
			catalog.add(new SalvageCalculator.CatalogLine(result.code(), row.getBaseQuantity()));
		}
		List<SalvageCalculator.SalvageOutput> outputs = SalvageCalculator.calculate(
				source.type(),
				source.rarity(),
				catalog);

		int extraSlots = 0;
		for (SalvageCalculator.SalvageOutput output : outputs) {
			if (inventoryApplicationService.unreservedQuantityByCode(vitals.characterId(), output.itemCode()) < 1) {
				extraSlots++;
			}
		}
		int used = inventoryApplicationService.usedCapacity(vitals.characterId());
		if (!InventoryBalance.hasRoom(used - 1, extraSlots)) {
			throw CraftingErrors.salvageNeedsInventorySpace();
		}

		inventoryApplicationService.destroyInstance(vitals.characterId(), itemInstanceId);
		try {
			for (SalvageCalculator.SalvageOutput output : outputs) {
				inventoryApplicationService.grantItems(vitals.characterId(), output.itemCode(), output.quantity());
			}
		}
		catch (InventoryFullException exception) {
			throw CraftingErrors.salvageNeedsInventorySpace();
		}

		ItemDefinitionView sourceDefinition = itemCatalogService.findByCode(source.itemCode())
				.orElseThrow(() -> CraftingErrors.itemDefinitionMissing(source.itemCode()));
		activityApplicationService.record(
				vitals.characterId(),
				ActivityType.ITEM_SALVAGED,
				"You salvaged " + sourceDefinition.name() + ".");
		List<SalvageResultView> resultViews = new ArrayList<>();
		for (SalvageCalculator.SalvageOutput output : outputs) {
			ItemDefinitionView result = itemCatalogService.findByCode(output.itemCode())
					.orElseThrow(() -> CraftingErrors.itemDefinitionMissing(output.itemCode()));
			resultViews.add(new SalvageResultView(result.code(), result.name(), output.quantity()));
		}
		return new SalvageView(sourceDefinition.code(), sourceDefinition.name(), List.copyOf(resultViews));
	}

	private void completeIfDue(CraftingJobEntity job, Instant now) {
		if (job.isDue(now)) {
			job.markCompleted(now);
			craftingJobRepository.saveAndFlush(job);
		}
	}

	private void ensureProfessions(UUID characterId) {
		List<CharacterProfessionEntity> existing = characterProfessionRepository
				.findByCharacterIdOrderByProfessionAsc(characterId);
		if (existing.size() == Profession.values().length) {
			return;
		}
		Instant now = Instant.now(clock);
		for (Profession profession : Profession.values()) {
			boolean present = existing.stream().anyMatch(row -> row.getProfession() == profession);
			if (!present) {
				characterProfessionRepository.save(new CharacterProfessionEntity(
						UUID.randomUUID(),
						characterId,
						profession,
						0,
						CraftingBalance.STARTING_RANK,
						now));
			}
		}
		characterProfessionRepository.flush();
	}

	private Map<Profession, CharacterProfessionEntity> professionsByType(UUID characterId) {
		Map<Profession, CharacterProfessionEntity> byType = new EnumMap<>(Profession.class);
		for (CharacterProfessionEntity row : characterProfessionRepository.findByCharacterIdOrderByProfessionAsc(characterId)) {
			byType.put(row.getProfession(), row);
		}
		return byType;
	}

	private List<ProfessionView> toProfessionViews(UUID characterId) {
		List<ProfessionView> views = new ArrayList<>();
		for (Profession profession : Profession.values()) {
			CharacterProfessionEntity row = professionsByType(characterId).get(profession);
			if (row == null) {
				continue;
			}
			boolean maxRank = row.getRank() >= CraftingBalance.MAX_RANK;
			int xpToNext = 0;
			if (!maxRank) {
				xpToNext = Math.max(0, CraftingBalance.cumulativeXpForRank(row.getRank() + 1) - row.getXp());
			}
			views.add(new ProfessionView(row.getProfession(), row.getRank(), row.getXp(), xpToNext, maxRank));
		}
		return views;
	}

	private RecipeView toRecipeView(
			CraftingRecipeEntity recipe,
			List<CraftingRecipeInputEntity> inputRows,
			CharacterVitalsView vitals,
			Map<Profession, CharacterProfessionEntity> professions) {
		ItemDefinitionView output = requireOutput(recipe.getOutputItemDefinitionId());
		Map<UUID, ItemDefinitionView> inputDefinitions = itemCatalogService.findByIds(
				inputRows.stream().map(CraftingRecipeInputEntity::getItemDefinitionId).toList());
		List<RecipeInputView> inputs = new ArrayList<>();
		List<RecipeValidator.RecipeInput> validatorInputs = new ArrayList<>();
		Map<String, Integer> availableByCode = new HashMap<>();
		for (CraftingRecipeInputEntity input : inputRows) {
			ItemDefinitionView definition = inputDefinitions.get(input.getItemDefinitionId());
			if (definition == null) {
				throw CraftingErrors.itemDefinitionMissing(input.getItemDefinitionId().toString());
			}
			int available = inventoryApplicationService.unreservedQuantityByCode(vitals.characterId(), definition.code());
			inputs.add(new RecipeInputView(definition.code(), definition.name(), input.getQuantity(), available));
			validatorInputs.add(new RecipeValidator.RecipeInput(definition.code(), input.getQuantity()));
			availableByCode.put(definition.code(), available);
		}
		CharacterProfessionEntity profession = professions.get(recipe.getProfession());
		int rank = profession == null ? 1 : profession.getRank();
		RecipeValidator.Failure failure = RecipeValidator.validate(
				new RecipeValidator.RecipeRequirement(
						recipe.getProfession(),
						recipe.getRequiredProfessionRank(),
						recipe.getRequiredCharacterLevel(),
						recipe.getGoldCost(),
						List.copyOf(validatorInputs)),
				rank,
				vitals.level(),
				vitals.gold(),
				availableByCode);
		String reason = switch (failure == null ? RecipeValidator.Failure.GOLD : failure) {
			case PROFESSION_RANK -> "profession rank";
			case CHARACTER_LEVEL -> "character level";
			case GOLD -> failure == null ? null : "gold";
			case MATERIALS -> "materials";
		};
		if (failure == null) {
			reason = null;
		}
		return new RecipeView(
				recipe.getCode(),
				recipe.getName(),
				recipe.getProfession(),
				recipe.getRequiredProfessionRank(),
				recipe.getRequiredCharacterLevel(),
				recipe.getGoldCost(),
				recipe.getDurationSeconds(),
				output.code(),
				output.name(),
				recipe.getOutputQuantity(),
				recipe.getMinRarity(),
				recipe.getMaxRarity(),
				recipe.getProfessionXp(),
				failure == null,
				reason,
				List.copyOf(inputs));
	}

	private CraftingJobView toJobView(CraftingJobEntity job) {
		CraftingRecipeEntity recipe = craftingRecipeRepository.findById(job.getRecipeId()).orElse(null);
		ItemDefinitionView output = requireOutput(job.getOutputItemDefinitionId());
		return toJobView(
				job,
				recipe == null ? output.code() : recipe.getCode(),
				recipe == null ? output.name() : recipe.getName(),
				output.name());
	}

	private static CraftingJobView toJobView(
			CraftingJobEntity job,
			String recipeCode,
			String recipeName,
			String outputName) {
		return new CraftingJobView(
				job.getId(),
				job.getProfession(),
				recipeCode,
				recipeName,
				job.getStatus(),
				job.getStartedAt(),
				job.getCompletesAt(),
				job.getClaimedAt(),
				job.getStatus() != CraftingJobStatus.ACTIVE,
				job.getOutputItemCode(),
				outputName,
				job.getOutputQuantity(),
				job.getRarity(),
				job.getProfessionXpPlanned());
	}

	private ItemDefinitionView requireOutput(UUID definitionId) {
		ItemDefinitionView output = itemCatalogService.findByIds(List.of(definitionId)).get(definitionId);
		if (output == null) {
			throw CraftingErrors.itemDefinitionMissing(definitionId.toString());
		}
		return output;
	}

	private void assertAtWard(UUID accountId, LocationAction action) {
		LocationView location = worldApplicationService.currentLocation(accountId);
		if (!LocationActions.forCode(location.code()).contains(action)) {
			throw CraftingErrors.locationCannotCraft();
		}
	}
}
