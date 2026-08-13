package com.example.game.account.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Email lookups are expressed with {@code lower(...)} so they can use the
 * {@code uq_accounts_email_lower} index. Spring Data's {@code IgnoreCase} keyword would
 * generate {@code upper(...)} instead and force a sequential scan.
 */
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

	@Query("select count(a) > 0 from AccountEntity a where lower(a.email) = lower(:email)")
	boolean existsByEmailIgnoreCase(@Param("email") String email);

	@Query("select a from AccountEntity a where lower(a.email) = lower(:email)")
	Optional<AccountEntity> findByEmailIgnoreCase(@Param("email") String email);
}
