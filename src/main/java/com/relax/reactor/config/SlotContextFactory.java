package com.relax.reactor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relax.reactor.rng.RNG;
import com.relax.reactor.service.gamelogic.core.SlotContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
public class SlotContextFactory {

    private static final String CONFIG_PATH = "config/cluster-reactor-config.json";

    private final RNG rng;
    private final ObjectMapper mapper;

    @Autowired
    public SlotContextFactory(RNG rng, ObjectMapper mapper) {
        this.rng = rng;
        this.mapper = mapper;
    }

    @Bean
    public SlotContext slotContext() throws Exception {
        return createSlotContextFromConfig();
    }

    public SlotContext createSlotContextFromConfig() throws Exception {
        return createSlotContext(mapper).setRng(rng);
    }

    public SlotContext createSlotContext(ObjectMapper mapper) throws Exception {
        ClassPathResource resource = new ClassPathResource(CONFIG_PATH);

        if (!resource.exists()) {
            throw new Exception("Resource not found: " + CONFIG_PATH);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            SlotContext slotContext = mapper.readValue(inputStream, SlotContext.class);

            // Additional initializations
            slotContext.setAfterDecimalPrecision(2);

            return slotContext;
        } catch (Exception e) {
            throw new Exception("Failed to load slot configuration", e);
        }
    }
}
