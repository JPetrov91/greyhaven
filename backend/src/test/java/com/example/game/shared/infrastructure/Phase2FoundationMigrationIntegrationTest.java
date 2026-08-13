package com.example.game.shared.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.inventory.infrastructure.EquipmentEntity;

class Phase2FoundationMigrationIntegrationTest {

	private static final UUID CITY_SQUARE = UUID.fromString("a0000000-0000-4000-8000-000000000001");
	private static final UUID RUSTY_SWORD = UUID.fromString("c0000000-0000-4000-8000-000000000001");
	private static final UUID WORN_LEATHER = UUID.fromString("c0000000-0000-4000-8000-000000000002");
	private static final UUID IRON_SWORD = UUID.fromString("c0000000-0000-4000-8000-000000000003");
	private static final UUID LEATHER_ARMOR = UUID.fromString("c0000000-0000-4000-8000-000000000004");
	private static final UUID OLD_DAGGER = UUID.fromString("c0000000-0000-4000-8000-000000000005");
	private static final UUID HEALING_POTION = UUID.fromString("c0000000-0000-4000-8000-000000000006");
	private static final UUID WOLF_PELT = UUID.fromString("c0000000-0000-4000-8000-000000000007");
	private static final UUID STREET_THUG = UUID.fromString("d0000000-0000-4000-8000-000000000001");

	@Test
	void upgradesPhase1SchemaWithoutLosingProgressionOrItems() {
		try (PostgreSQLContainer postgres = postgres()) {
			postgres.start();
			DataSource dataSource = dataSource(postgres);
			JdbcTemplate jdbc = new JdbcTemplate(dataSource);

			flyway(dataSource).target(MigrationVersion.fromVersion("17")).load().migrate();

			UUID accountId = UUID.fromString("11111111-1111-4111-8111-111111111111");
			UUID characterId = UUID.fromString("22222222-2222-4222-8222-222222222222");
			UUID swordInstanceId = UUID.fromString("33333333-3333-4333-8333-333333333333");
			UUID armorInstanceId = UUID.fromString("44444444-4444-4444-8444-444444444444");
			UUID peltInstanceId = UUID.fromString("55555555-5555-4555-8555-555555555555");
			UUID listingId = UUID.fromString("66666666-6666-4666-8666-666666666666");
			UUID encounterId = UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee");
			UUID combatId = UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff");

			jdbc.update(
					"""
							insert into accounts (id, email, password_hash, created_at, updated_at)
							values (?, 'legacy@greyhaven.test', 'hash', now(), now())
							""",
					accountId);
			jdbc.update(
					"""
							insert into characters (
								id, account_id, name, level, experience, strength, agility, endurance, perception,
								current_health, max_health, current_stamina, max_stamina, gold,
								unspent_attribute_points, current_location_id, created_at, updated_at)
							values (?, ?, 'LegacyHero', 5, 1600, 7, 5, 6, 5, 160, 172, 80, 84, 40, 2, ?, now(), now())
							""",
					characterId, accountId, CITY_SQUARE);
			jdbc.update(
					"""
							insert into item_instances (id, item_definition_id, owner_character_id, quantity, stackable, created_at)
							values (?, ?, ?, 1, false, now()), (?, ?, ?, 1, false, now()), (?, ?, ?, 3, true, now())
							""",
					swordInstanceId, RUSTY_SWORD, characterId,
					armorInstanceId, WORN_LEATHER, characterId,
					peltInstanceId, WOLF_PELT, characterId);
			jdbc.update(
					"""
							insert into equipment (id, character_id, slot, item_instance_id) values
							(?, ?, 'WEAPON', ?),
							(?, ?, 'ARMOR', ?)
							""",
					UUID.fromString("77777777-7777-4777-8777-777777777777"), characterId, swordInstanceId,
					UUID.fromString("88888888-8888-4888-8888-888888888888"), characterId, armorInstanceId);
			jdbc.update(
					"""
							insert into market_listings (
								id, seller_character_id, buyer_character_id, item_instance_id, item_definition_id,
								quantity, price, status, created_at, sold_at, cancelled_at, version)
							values (?, ?, null, ?, ?, 3, 18, 'ACTIVE', now(), null, null, 0)
							""",
					listingId, characterId, peltInstanceId, WOLF_PELT);
			UUID expeditionId = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");
			jdbc.update(
					"""
							insert into expeditions (
								id, character_id, expedition_type, strategy, status,
								started_at, completes_at, claimed_at, result_generated,
								planned_xp, planned_gold, planned_injury,
								xp_awarded, gold_awarded, injury_applied,
								version, created_at, updated_at)
							values (
								?, ?, 'FOREST_PATROL', 'BALANCED', 'ACTIVE',
								now(), now() + interval '1 hour', null, false,
								null, null, null, null, null, null,
								0, now(), now())
							""",
					expeditionId, characterId);
			jdbc.update(
					"""
							insert into encounters (
								id, character_id, location_id, monster_definition_id, status, created_at, updated_at)
							values (?, ?, ?, ?, 'COMBAT_STARTED', now(), now())
							""",
					encounterId, characterId, CITY_SQUARE, STREET_THUG);
			jdbc.update(
					"""
							insert into combat_sessions (
								id, encounter_id, character_id, monster_definition_id, status, round_number,
								player_health, player_stamina, enemy_health, rewards_applied, version,
								created_at, updated_at)
							values (?, ?, ?, ?, 'ACTIVE', 1, 160, 80, 70, false, 0, now(), now())
							""",
					combatId, encounterId, characterId, STREET_THUG);

			flyway(dataSource).load().migrate();

			assertThat(jdbc.queryForObject(
					"select count(*) from flyway_schema_history where version = '18' and success = true",
					Integer.class)).isEqualTo(1);

			List<String> slots = jdbc.queryForList(
					"select slot from equipment where character_id = ? order by slot",
					String.class,
					characterId);
			assertThat(slots).containsExactly("CHEST", "MAIN_HAND");

			assertThat(jdbc.queryForObject(
					"select count(*) from equipment where slot in ('WEAPON', 'ARMOR')",
					Integer.class)).isZero();

			assertCatalogSlots(jdbc);
			assertThat(jdbc.queryForObject(
					"select count(*) from item_definitions where legacy = true",
					Integer.class)).isEqualTo(7);

			Map<String, Object> character = jdbc.queryForMap(
					"""
							select level, experience, gold, strength, agility, endurance, perception,
								unspent_attribute_points
							from characters where id = ?
							""",
					characterId);
			assertThat(character.get("level")).isEqualTo(5);
			assertThat(character.get("experience")).isEqualTo(1600);
			assertThat(character.get("gold")).isEqualTo(40);
			assertThat(character.get("strength")).isEqualTo(7);
			assertThat(character.get("agility")).isEqualTo(5);
			assertThat(character.get("endurance")).isEqualTo(6);
			assertThat(character.get("perception")).isEqualTo(5);
			assertThat(character.get("unspent_attribute_points")).isEqualTo(2);

			assertThat(jdbc.queryForObject(
					"select id from item_instances where item_definition_id = ?",
					UUID.class,
					RUSTY_SWORD)).isEqualTo(swordInstanceId);
			assertThat(jdbc.queryForObject(
					"select legacy from item_instances where id = ?",
					Boolean.class,
					swordInstanceId)).isTrue();
			assertThat(jdbc.queryForObject(
					"select item_instance_id from market_listings where id = ?",
					UUID.class,
					listingId)).isEqualTo(peltInstanceId);
			assertThat(jdbc.queryForObject(
					"select status from expeditions where id = ?",
					String.class,
					expeditionId)).isEqualTo("ACTIVE");
			assertThat(jdbc.queryForObject(
					"select status from combat_sessions where id = ?",
					String.class,
					combatId)).isEqualTo("ACTIVE");

			assertEquipmentReadableViaJpa(dataSource, characterId);

			assertThatThrownBy(() -> jdbc.update(
					"update equipment set slot = 'WEAPON' where character_id = ?",
					characterId))
					.hasMessageContaining("chk_equipment_slot");

			UUID postMigrationInstanceId = UUID.fromString("abababab-abab-4bab-8bab-abababababab");
			jdbc.update(
					"""
							insert into item_instances (id, item_definition_id, owner_character_id, quantity, stackable, created_at)
							values (?, ?, ?, 1, false, now())
							""",
					postMigrationInstanceId, OLD_DAGGER, characterId);
			assertThat(jdbc.queryForObject(
					"select legacy from item_instances where id = ?",
					Boolean.class,
					postMigrationInstanceId)).isFalse();

			jdbc.update(
					"""
							insert into accounts (id, email, password_hash, created_at, updated_at)
							values (?, 'cap30@greyhaven.test', 'hash', now(), now())
							""",
					UUID.fromString("99999999-9999-4999-8999-999999999999"));
			jdbc.update(
					"""
							insert into characters (
								id, account_id, name, level, experience, strength, agility, endurance, perception,
								current_health, max_health, current_stamina, max_stamina, gold,
								unspent_attribute_points, current_location_id, created_at, updated_at)
							values (?, ?, 'CapThirty', 30, 184830, 5, 5, 5, 5, 160, 160, 80, 80, 100, 0, ?, now(), now())
							""",
					UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa"),
					UUID.fromString("99999999-9999-4999-8999-999999999999"),
					CITY_SQUARE);
			assertThat(jdbc.queryForObject(
					"select level from characters where name = 'CapThirty'",
					Integer.class)).isEqualTo(30);
		}
	}

	@Test
	void cleanDatabaseAppliesFoundationAndKeepsCatalog() {
		try (PostgreSQLContainer postgres = postgres()) {
			postgres.start();
			DataSource dataSource = dataSource(postgres);
			JdbcTemplate jdbc = new JdbcTemplate(dataSource);

			flyway(dataSource).load().migrate();

			assertThat(jdbc.queryForObject("select count(*) from item_definitions", Integer.class)).isEqualTo(7);
			assertThat(jdbc.queryForObject(
					"select count(*) from flyway_schema_history where version = '18' and success = true",
					Integer.class)).isEqualTo(1);
			assertCatalogSlots(jdbc);
			assertThat(jdbc.queryForObject(
					"select count(*) from item_definitions where legacy = true",
					Integer.class)).isEqualTo(7);

			UUID accountId = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
			UUID characterId = UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
			jdbc.update(
					"""
							insert into accounts (id, email, password_hash, created_at, updated_at)
							values (?, 'fresh@greyhaven.test', 'hash', now(), now())
							""",
					accountId);
			jdbc.update(
					"""
							insert into characters (
								id, account_id, name, level, experience, strength, agility, endurance, perception,
								current_health, max_health, current_stamina, max_stamina, gold,
								unspent_attribute_points, current_location_id, created_at, updated_at)
							values (?, ?, 'FreshHero', 1, 0, 5, 5, 5, 5, 160, 160, 80, 80, 100, 0, ?, now(), now())
							""",
					characterId, accountId, CITY_SQUARE);
			assertThat(jdbc.queryForObject(
					"select experience from characters where id = ?",
					Integer.class,
					characterId)).isZero();
		}
	}

	private static void assertCatalogSlots(JdbcTemplate jdbc) {
		Map<UUID, String> slots = new HashMap<>();
		jdbc.query("select id, equipment_slot from item_definitions", rs -> {
			slots.put(rs.getObject("id", UUID.class), rs.getString("equipment_slot"));
		});
		assertThat(slots)
				.containsEntry(RUSTY_SWORD, "MAIN_HAND")
				.containsEntry(IRON_SWORD, "MAIN_HAND")
				.containsEntry(OLD_DAGGER, "MAIN_HAND")
				.containsEntry(WORN_LEATHER, "CHEST")
				.containsEntry(LEATHER_ARMOR, "CHEST")
				.containsEntry(HEALING_POTION, null)
				.containsEntry(WOLF_PELT, null);
	}

	private static void assertEquipmentReadableViaJpa(DataSource dataSource, UUID characterId) {
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.applySetting("hibernate.connection.datasource", dataSource)
				.applySetting("hibernate.hbm2ddl.auto", "none")
				.build();
		try (SessionFactory sessionFactory = new MetadataSources(registry)
				.addAnnotatedClass(EquipmentEntity.class)
				.buildMetadata()
				.buildSessionFactory();
				Session session = sessionFactory.openSession()) {
			List<EquipmentEntity> rows = session
					.createQuery("from EquipmentEntity e where e.characterId = :id", EquipmentEntity.class)
					.setParameter("id", characterId)
					.getResultList();
			assertThat(rows)
					.extracting(EquipmentEntity::getSlot)
					.containsExactlyInAnyOrder(EquipmentSlot.MAIN_HAND, EquipmentSlot.CHEST);
		}
	}

	private static PostgreSQLContainer postgres() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:18"));
	}

	private static FluentConfiguration flyway(DataSource dataSource) {
		return Flyway.configure().dataSource(dataSource).locations("classpath:db/migration");
	}

	private static DataSource dataSource(PostgreSQLContainer postgres) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(postgres.getJdbcUrl());
		dataSource.setUsername(postgres.getUsername());
		dataSource.setPassword(postgres.getPassword());
		return dataSource;
	}
}
