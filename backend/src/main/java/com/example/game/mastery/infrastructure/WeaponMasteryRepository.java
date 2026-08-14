package com.example.game.mastery.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.item.domain.WeaponFamily;

import jakarta.persistence.LockModeType;

public interface WeaponMasteryRepository extends JpaRepository<WeaponMasteryEntity, UUID> {

	List<WeaponMasteryEntity> findByCharacterIdOrderByWeaponFamilyAsc(UUID characterId);

	Optional<WeaponMasteryEntity> findByCharacterIdAndWeaponFamily(UUID characterId, WeaponFamily weaponFamily);

	boolean existsByCharacterIdAndWeaponFamily(UUID characterId, WeaponFamily weaponFamily);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select m from WeaponMasteryEntity m
			where m.characterId = :characterId and m.weaponFamily = :weaponFamily
			""")
	Optional<WeaponMasteryEntity> findWithLockByCharacterIdAndWeaponFamily(
			@Param("characterId") UUID characterId,
			@Param("weaponFamily") WeaponFamily weaponFamily);
}
