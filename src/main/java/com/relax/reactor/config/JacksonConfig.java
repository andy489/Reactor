package com.relax.reactor.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.indentOutput(true);
            builder.failOnUnknownProperties(false);
            builder.serializationInclusion(JsonInclude.Include.NON_ABSENT);
        };
    }

    @Bean
    public ObjectMapper objectMapper(BaseDtoDeserializer baseDtoDeserializer) {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule("BaseDtoModule");
        module.addDeserializer(BaseDto.class, baseDtoDeserializer);
        mapper.registerModule(module);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        mapper.findAndRegisterModules();

        return mapper;
    }
}