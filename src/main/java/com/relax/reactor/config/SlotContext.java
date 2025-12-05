package com.relax.reactor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relax.reactor.service.gamelogic.core.BaseSlot;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
@Getter
@Setter
@Accessors(chain = true)
public class SlotContext {

    private static final String CONFIG_PATH = "config/cluster-reactor-config.json";

    private BaseSlot baseSlot;

    @Bean
    public SlotContext baseSlot(ObjectMapper mapper) throws Exception {
        ClassPathResource resource = new ClassPathResource(CONFIG_PATH);

        if (!resource.exists()) {
            throw new Exception("Resource not found: " + CONFIG_PATH);
        }

        SlotContext slotContext = new SlotContext();

        try (InputStream inputStream = resource.getInputStream()) {
            baseSlot = mapper.readValue(inputStream, BaseSlot.class);
        } catch (Exception e) {
            throw new Exception("Failed to load slot configuration", e);
        }

        return slotContext.setBaseSlot(baseSlot);
    }
}