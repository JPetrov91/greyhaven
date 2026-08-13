package com.example.game.character.application;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;

/**
 * Character names for other modules that must not depend on character persistence types.
 */
@Service
public class CharacterIdentityService {

	private final CharacterRepository characterRepository;

	public CharacterIdentityService(CharacterRepository characterRepository) {
		this.characterRepository = characterRepository;
	}

	@Transactional(readOnly = true)
	public String requireName(UUID characterId) {
		return characterRepository.findById(characterId)
				.map(CharacterEntity::getName)
				.orElseThrow(CharacterErrors::characterNotFound);
	}

	@Transactional(readOnly = true)
	public Map<UUID, String> namesOf(Collection<UUID> characterIds) {
		Map<UUID, String> names = new HashMap<>();
		if (characterIds == null || characterIds.isEmpty()) {
			return names;
		}
		for (CharacterEntity character : characterRepository.findAllById(characterIds)) {
			names.put(character.getId(), character.getName());
		}
		return names;
	}
}
