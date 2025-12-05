package com.relax.reactor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relax.reactor.config.SlotContext;
import com.relax.reactor.service.gamelogic.core.BaseSlot;
import com.relax.reactor.service.gamelogic.dto.SettingsDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;


@Service
public class SlotService {

    private final ObjectMapper mapper;

    private final SlotContext slotContext;

    public SlotService(ObjectMapper mapper, SlotContext slotContext) {
        this.mapper = mapper;
        this.slotContext = slotContext;
    }

    public Optional<SettingsDto> settings() throws IOException {

        BaseSlot baseSlot = slotContext.getBaseSlot();

        SettingsDto settingsDto = mapper.convertValue(baseSlot, SettingsDto.class);

        return Optional.of(settingsDto);
    }
}
