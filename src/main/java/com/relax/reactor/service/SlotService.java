package com.relax.reactor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relax.reactor.rng.RNG;
import com.relax.reactor.service.gamelogic.core.SlotContext;
import com.relax.reactor.service.gamelogic.dto.SettingsDto;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.slot.MySlotGame;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SlotService {

    private final ObjectMapper mapper;
    private final SlotContext slotContext;
    private final RNG rng;

    public SlotService(ObjectMapper mapper, SlotContext slotContext, RNG rng) {
        this.mapper = mapper;
        this.slotContext = slotContext;
        this.rng = rng;
    }

    public Optional<SettingsDto> settings() {
        SettingsDto settingsDto = mapper.convertValue(slotContext, SettingsDto.class);
        return Optional.of(settingsDto);
    }

    public Optional<SlotGameDto> spin(double stake, List<Integer> states) {

        MySlotGame mySlotGame = new MySlotGame();

        org.springframework.beans.BeanUtils.copyProperties(slotContext, mySlotGame,
                "spinProcessors", "postSpinProcessors");

        SlotGameDto slotGameDto = mapper.convertValue(mySlotGame.createMainSlotSpin(stake, states), SlotGameDto.class);

        return Optional.of(slotGameDto);
    }
}
