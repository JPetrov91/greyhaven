package com.example.game.world.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterLocationView;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.quest.application.QuestApplicationService;
import com.example.game.quest.application.QuestErrors;
import com.example.game.quest.application.QuestProgressSink;
import com.example.game.quest.application.QuestView;
import com.example.game.quest.domain.TalkFact;
import com.example.game.quest.infrastructure.QuestDefinitionEntity;
import com.example.game.quest.infrastructure.QuestDefinitionRepository;
import com.example.game.world.domain.NpcInteraction;
import com.example.game.world.infrastructure.LocationEntity;
import com.example.game.world.infrastructure.LocationRepository;
import com.example.game.world.infrastructure.NpcDefinitionEntity;
import com.example.game.world.infrastructure.NpcDefinitionRepository;

@Service
public class NpcApplicationService {

	private final NpcDefinitionRepository npcDefinitionRepository;
	private final LocationRepository locationRepository;
	private final CharacterLocationService characterLocationService;
	private final CharacterVitalsService characterVitalsService;
	private final QuestApplicationService questApplicationService;
	private final QuestDefinitionRepository questDefinitionRepository;
	private final QuestProgressSink questProgressSink;

	public NpcApplicationService(
			NpcDefinitionRepository npcDefinitionRepository,
			LocationRepository locationRepository,
			CharacterLocationService characterLocationService,
			CharacterVitalsService characterVitalsService,
			QuestApplicationService questApplicationService,
			QuestDefinitionRepository questDefinitionRepository,
			QuestProgressSink questProgressSink) {
		this.npcDefinitionRepository = npcDefinitionRepository;
		this.locationRepository = locationRepository;
		this.characterLocationService = characterLocationService;
		this.characterVitalsService = characterVitalsService;
		this.questApplicationService = questApplicationService;
		this.questDefinitionRepository = questDefinitionRepository;
		this.questProgressSink = questProgressSink;
	}

	@Transactional(readOnly = true)
	public List<NpcView> atCurrentLocation(UUID accountId) {
		CharacterLocationView location = characterLocationService.locationOf(accountId);
		String locationCode = locationRepository.findById(location.currentLocationId())
				.map(LocationEntity::getCode)
				.orElseThrow(QuestErrors::wrongLocation);
		List<QuestView> quests = questApplicationService.list(accountId);
		return npcDefinitionRepository.findByLocationCodeOrderBySortOrderAsc(locationCode).stream()
				.map(npc -> toView(npc, quests))
				.toList();
	}

	@Transactional(readOnly = true)
	public NpcView get(UUID accountId, String code) {
		return atCurrentLocation(accountId).stream()
				.filter(npc -> npc.code().equals(code))
				.findFirst()
				.orElseThrow(QuestErrors::npcNotAtLocation);
	}

	@Transactional
	public NpcTalkView talk(UUID accountId, String code, String questCode) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		CharacterLocationView location = characterLocationService.lockLocationOf(accountId);
		NpcDefinitionEntity npc = npcDefinitionRepository.findByCode(code).orElseThrow(QuestErrors::npcNotFound);
		String locationCode = locationRepository.findById(location.currentLocationId())
				.map(LocationEntity::getCode)
				.orElseThrow(QuestErrors::wrongLocation);
		if (!npc.getLocationCode().equals(locationCode)) {
			throw QuestErrors.npcNotAtLocation();
		}
		questProgressSink.notify(vitals.characterId(), new TalkFact(npc.getCode()));
		List<QuestView> quests = questApplicationService.list(accountId);
		QuestView focused = resolveFocusedQuest(npc, quests, questCode);
		String text = dialogueText(npc, focused);
		List<NpcTalkActionView> actions = new ArrayList<>();
		if (focused != null && "AVAILABLE".equals(focused.status()) && npc.getCode().equals(focused.startNpcCode())) {
			actions.add(new NpcTalkActionView("ACCEPT", focused.code(), null, "Accept quest"));
		}
		if (focused != null && "READY_TO_TURN_IN".equals(focused.status())
				&& npc.getCode().equals(focused.turnInNpcCode())) {
			actions.add(new NpcTalkActionView("TURN_IN", focused.code(), null, "Turn in"));
		}
		if (npc.getMerchantCode() != null) {
			actions.add(new NpcTalkActionView("SHOP", null, npc.getMerchantCode(), "Browse wares"));
		}
		actions.add(new NpcTalkActionView("CLOSE", null, null, "Leave"));
		return new NpcTalkView(
				npc.getCode(),
				npc.getName(),
				npc.getTitle(),
				npc.getPortraitCode(),
				text,
				npc.getMerchantCode(),
				actions);
	}

	private static NpcView toView(NpcDefinitionEntity npc, List<QuestView> quests) {
		List<String> badges = new ArrayList<>();
		for (QuestView quest : quests) {
			if ("AVAILABLE".equals(quest.status()) && npc.getCode().equals(quest.startNpcCode())) {
				badges.add("AVAILABLE_QUEST");
			}
			if ("READY_TO_TURN_IN".equals(quest.status()) && npc.getCode().equals(quest.turnInNpcCode())) {
				badges.add("TURN_IN");
			}
			if ("ACTIVE".equals(quest.status())
					&& (npc.getCode().equals(quest.startNpcCode()) || npc.getCode().equals(quest.turnInNpcCode()))) {
				badges.add("ACTIVE");
			}
		}
		return new NpcView(
				npc.getCode(),
				npc.getName(),
				npc.getTitle(),
				npc.getDescription(),
				npc.getGreeting(),
				npc.getPortraitCode(),
				npc.getLocationCode(),
				npc.getMerchantCode(),
				parseInteractions(npc.getInteractions()),
				badges.stream().distinct().toList());
	}

	private QuestView resolveFocusedQuest(NpcDefinitionEntity npc, List<QuestView> quests, String questCode) {
		if (questCode != null && !questCode.isBlank()) {
			return quests.stream().filter(quest -> quest.code().equals(questCode)).findFirst().orElse(null);
		}
		return quests.stream()
				.filter(quest -> "READY_TO_TURN_IN".equals(quest.status()) && npc.getCode().equals(quest.turnInNpcCode()))
				.findFirst()
				.or(() -> quests.stream()
						.filter(quest -> "AVAILABLE".equals(quest.status()) && npc.getCode().equals(quest.startNpcCode()))
						.findFirst())
				.or(() -> quests.stream()
						.filter(quest -> "ACTIVE".equals(quest.status())
								&& (npc.getCode().equals(quest.startNpcCode()) || npc.getCode().equals(quest.turnInNpcCode())))
						.findFirst())
				.orElse(null);
	}

	private String dialogueText(NpcDefinitionEntity npc, QuestView focused) {
		if (focused == null) {
			return npc.getGreeting();
		}
		QuestDefinitionEntity definition = questDefinitionRepository.findByCode(focused.code()).orElse(null);
		if (definition == null) {
			return npc.getGreeting();
		}
		return switch (focused.status()) {
			case "AVAILABLE" -> definition.getOfferText();
			case "READY_TO_TURN_IN" -> definition.getCompleteText();
			case "COMPLETED" -> definition.getCompleteText();
			default -> definition.getProgressText();
		};
	}

	private static List<String> parseInteractions(String raw) {
		return Arrays.stream(raw.split(","))
				.map(String::trim)
				.filter(value -> !value.isBlank())
				.filter(value -> {
					try {
						NpcInteraction.valueOf(value);
						return true;
					}
					catch (IllegalArgumentException exception) {
						return false;
					}
				})
				.toList();
	}
}
