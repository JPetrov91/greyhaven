package com.example.game.shared.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.game.shared.domain.RandomProvider;
import com.example.game.shared.domain.ThreadLocalRandomProvider;

@Configuration
public class RandomConfig {

	@Bean
	@ConditionalOnMissingBean(RandomProvider.class)
	RandomProvider randomProvider() {
		return new ThreadLocalRandomProvider();
	}
}
