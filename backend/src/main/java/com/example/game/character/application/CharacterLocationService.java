package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;

/**
 * Character location state for other gameplay modules. Character persistence stays private to this
 * module: callers describe intent ({@code relocate}) instead of mutating the entity themselves.
 */
@Service
public class CharacterLocationService {

	private final CharacterRepository characterRepository;
	private final Clock clock;

	public CharacterLocationService(CharacterRepository characterRepository, Clock clock) {
		this.characterRepository = characterRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public CharacterLocationView locationOf(UUID accountId) {
		return toView(characterRepository.findByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound));
	}

	/**
	 * Reads the location under a row lock so the caller can validate a move and persist the result
	 * without a concurrent request interleaving. {@code MANDATORY} propagation guarantees the lock
	 * is held for the caller's whole unit of work rather than released when this method returns.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterLocationView lockLocationOf(UUID accountId) {
		return toView(characterRepository.findWithLockByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound));
	}

	/**
	 * Applies a movement decision already validated by the caller. Requires the caller's
	 * transaction, which is where the row lock from {@link #lockLocationOf(UUID)} lives.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void relocate(UUID accountId, UUID destinationLocationId) {
		CharacterEntity character = characterRepository.findByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		character.moveTo(destinationLocationId, Instant.now(clock));
		characterRepository.saveAndFlush(character);
	}

	@Transactional(readOnly = true)
	public List<CharacterAtLocationView> othersAt(UUID locationId, UUID excludedCharacterId, int maxResults) {
		return characterRepository
				.findByCurrentLocationIdAndIdNotOrderByNameAsc(locationId, excludedCharacterId, Limit.of(maxResults))
				.stream()
				.map(character -> new CharacterAtLocationView(
						character.getId(),
						character.getName(),
						character.getLevel()))
				.toList();
	}

	private static CharacterLocationView toView(CharacterEntity character) {
		return new CharacterLocationView(character.getId(), character.getCurrentLocationId());
	}
}
