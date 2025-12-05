package com.relax.reactor.config;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RNGConfig {

    @Bean
    public UniformRandomProvider uniformRandomProvider() {
        return RandomSource.MT.create(System.currentTimeMillis());
    }
}
