package com.example.game.character.application;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.account.infrastructure.AccountEntity;
import com.example.game.account.infrastructure.AccountRepository;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;

@Component
public class ActiveCharacterResolver {

	private final AccountRepository accountRepository;
	private final CharacterRepository characterRepository;

	public ActiveCharacterResolver(
			AccountRepository accountRepository,
			CharacterRepository characterRepository) {
		this.accountRepository = accountRepository;
		this.characterRepository = characterRepository;
	}

	@Transactional(readOnly = true)
	public CharacterEntity requireActive(UUID accountId) {
		return loadActive(accountId, false);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterEntity requireActiveLocked(UUID accountId) {
		return loadActive(accountId, true);
	}

	public UUID requireActiveId(UUID accountId) {
		return requireActive(accountId).getId();
	}

	private CharacterEntity loadActive(UUID accountId, boolean lock) {
		AccountEntity account = accountRepository.findById(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		UUID activeId = account.getActiveCharacterId();
		if (activeId == null) {
			throw CharacterErrors.noActiveCharacter();
		}
		CharacterEntity character = (lock
				? characterRepository.findWithLockById(activeId)
				: characterRepository.findById(activeId))
				.orElseThrow(CharacterErrors::noActiveCharacter);
		if (!accountId.equals(character.getAccountId())) {
			throw CharacterErrors.noActiveCharacter();
		}
		return character;
	}
}
