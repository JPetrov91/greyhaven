package com.example.game.shared.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
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

import com.example.game.inventory.infrastructure.EquipmentEntity;

class Phase3BaselineSchemaIntegrationTest {

	private static final UUID RUSTY_SWORD = UUID.fromString("c0000000-0000-4000-8000-000000000001");
	private static final UUID WORN_LEATHER = UUID.fromString("c0000000-0000-4000-8000-000000000002");

	@Test
	void phase3BaselineCreatesFinalSchemaAndSeeds() {
		try (PostgreSQLContainer postgres = postgres()) {
			postgres.start();
			DataSource dataSource = dataSource(postgres);
			JdbcTemplate jdbc = new JdbcTemplate(dataSource);

			var result = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
			assertThat(result.migrationsExecuted).isEqualTo(3);

			assertThat(jdbc.queryForObject(
					"select count(*) from flyway_schema_history where success = true",
					Integer.class)).isEqualTo(3);
			assertThat(jdbc.queryForList(
					"select version from flyway_schema_history where success = true order by installed_rank",
					String.class)).containsExactly("1", "2", "3");
			assertThat(jdbc.queryForObject(
					"select value from schema_meta where key = 'bootstrap_version'",
					String.class)).isEqualTo("phase3");

			assertThat(jdbc.queryForObject(
					"""
							select count(*) from information_schema.tables
							where table_schema = 'public' and table_type = 'BASE TABLE'
							  and table_name <> 'flyway_schema_history'
							""",
					Integer.class)).isEqualTo(59);

			assertThat(jdbc.queryForObject("select count(*) from locations", Integer.class)).isEqualTo(13);
			assertThat(jdbc.queryForObject("select count(*) from location_connections", Integer.class)).isEqualTo(26);
			assertThat(jdbc.queryForObject("select count(*) from item_definitions", Integer.class)).isEqualTo(38);
			assertThat(jdbc.queryForObject("select count(*) from item_definitions where legacy = true", Integer.class))
					.isEqualTo(7);
			assertThat(jdbc.queryForObject("select count(*) from affix_definitions", Integer.class)).isEqualTo(16);
			assertThat(jdbc.queryForObject("select count(*) from item_definition_modifiers", Integer.class)).isEqualTo(29);
			assertThat(jdbc.queryForObject("select count(*) from monster_definitions", Integer.class)).isEqualTo(18);
			assertThat(jdbc.queryForObject("select count(*) from combat_technique_definitions", Integer.class)).isEqualTo(25);
			assertThat(jdbc.queryForObject("select count(*) from merchant_definitions", Integer.class)).isEqualTo(4);
			assertThat(jdbc.queryForObject("select count(*) from merchant_stock", Integer.class)).isEqualTo(19);
			assertThat(jdbc.queryForObject("select count(*) from crafting_recipes", Integer.class)).isEqualTo(12);
			assertThat(jdbc.queryForObject("select count(*) from salvage_outputs", Integer.class)).isEqualTo(50);
			assertThat(jdbc.queryForObject("select count(*) from dungeon_definitions", Integer.class)).isEqualTo(1);
			assertThat(jdbc.queryForObject("select count(*) from dungeon_rooms", Integer.class)).isEqualTo(8);
			assertThat(jdbc.queryForObject("select count(*) from npc_definitions", Integer.class)).isEqualTo(7);
			assertThat(jdbc.queryForObject("select count(*) from quest_definition", Integer.class)).isEqualTo(2);
			assertThat(jdbc.queryForObject("select count(*) from quest_objective_definition", Integer.class)).isEqualTo(4);
			assertThat(jdbc.queryForObject("select count(*) from quest_reward_definition", Integer.class)).isEqualTo(3);
			assertThat(jdbc.queryForObject(
					"""
							select count(*) from information_schema.table_constraints
							where table_schema = 'public' and table_name = 'quest_definition'
							  and constraint_name in ('fk_quest_definition_prerequisite', 'fk_quest_definition_next')
							""",
					Integer.class)).isEqualTo(2);

			assertThat(jdbc.queryForObject(
					"""
							select count(*) from location_connections c
							join locations a on a.id = c.from_location_id
							join locations b on b.id = c.to_location_id
							where a.code = 'HARBOUR' and b.code = 'ANCIENT_RUINS'
							""",
					Integer.class)).isZero();
			assertThat(jdbc.queryForObject(
					"""
							select count(*) from location_connections c
							join locations a on a.id = c.from_location_id
							join locations b on b.id = c.to_location_id
							where a.code = 'BANDIT_CAMP' and b.code = 'ANCIENT_RUINS'
							""",
					Integer.class)).isEqualTo(1);

			assertThat(jdbc.queryForObject(
					"""
							select count(*) from monster_loot_entries e
							join monster_definitions m on m.id = e.monster_definition_id
							join item_definitions i on i.id = e.item_definition_id
							where m.code = 'BANDIT' and i.code = 'IRON_AXE'
							""",
					Integer.class)).isEqualTo(1);
			assertThat(jdbc.queryForObject(
					"select gold_cost from crafting_recipes where code = 'FORGE_IRON_SWORD'",
					Integer.class)).isEqualTo(16);
			assertThat(jdbc.queryForObject(
					"select gold_cost from crafting_recipes where code = 'BREW_HEALING_POTION'",
					Integer.class)).isEqualTo(8);

			assertThat(jdbc.queryForObject(
					"select equipment_slot from item_definitions where id = ?",
					String.class,
					RUSTY_SWORD)).isEqualTo("MAIN_HAND");
			assertThat(jdbc.queryForObject(
					"select equipment_slot from item_definitions where id = ?",
					String.class,
					WORN_LEATHER)).isEqualTo("CHEST");

			assertHibernateCanReadEquipment(dataSource);
		}
	}

	private static void assertHibernateCanReadEquipment(DataSource dataSource) {
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
					.createQuery("from EquipmentEntity", EquipmentEntity.class)
					.getResultList();
			assertThat(rows).isEmpty();
		}
	}

	private static PostgreSQLContainer postgres() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:18"));
	}

	private static DataSource dataSource(PostgreSQLContainer postgres) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(postgres.getJdbcUrl());
		dataSource.setUsername(postgres.getUsername());
		dataSource.setPassword(postgres.getPassword());
		return dataSource;
	}
}
