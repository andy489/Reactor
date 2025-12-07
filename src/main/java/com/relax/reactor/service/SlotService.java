package com.relax.reactor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relax.reactor.exception.GambleNotAvailableException;
import com.relax.reactor.exception.PendingGambleException;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.core.data.SlotContext;
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
    private final SpinResultLogger logger;

    public SlotService(ObjectMapper mapper,
                       SlotContext slotContext,
                       SpinResultLogger logger) {
        this.mapper = mapper;
        this.slotContext = slotContext;
        this.logger = logger;
    }

    public Optional<SettingsDto> settings() {
        SettingsDto settingsDto = mapper.convertValue(slotContext, SettingsDto.class);
        return Optional.of(settingsDto);
    }

    public Optional<List<SlotGameDto>> spin(double stake, List<Integer> states) {
        SlotGameDto lastSpin = logger.loadOriginalSpin();

        if (lastSpin != null) {
            Integer gambleMultiplier = lastSpin.getGambleMultiplier();
            boolean hasPendingGamble = (gambleMultiplier != null && gambleMultiplier == -1);

            if (hasPendingGamble) {
                Double toGamble = lastSpin.getStashedCumulativeWinAmountBeforeGambleChoice();
                String message = String.format(
                        "Pending gamble decision required. Choose to COLLECT (win $%.2f) or GAMBLE for $%.2f x 2",
                        toGamble != null ? toGamble : 0.0, toGamble != null ? toGamble : 0.0
                );
                throw new PendingGambleException(message, toGamble);
            }
        }

        MySlotGame mySlotGame = new MySlotGame();

        org.springframework.beans.BeanUtils.copyProperties(slotContext, mySlotGame,
                "spinProcessors", "postSpinProcessors");

        if (mySlotGame.isProcessorsNeedInitialization()) {
            mySlotGame.initializeProcessorsIfNeeded();
            mySlotGame.setProcessorsNeedInitialization(false);
        }

        SlotGameDto slotSpin = mySlotGame.createMainSlotSpin(stake, states);

        slotSpin.setStashedCumulativeWinAmountBeforeGambleChoice(slotSpin.getCumulativeWinAmount());
        slotSpin.setCumulativeWinAmount(0.0);

        logger.logOriginalSpin(slotSpin);
        List<SlotGameDto> linearizedResponse = slotSpin.linearize();

        addGambleLinks(linearizedResponse);

        return Optional.of(linearizedResponse);
    }

    public Optional<List<SlotGameDto>> gamble(Integer choice) {
        SlotGameDto spin = logger.loadOriginalSpin();

        if (spin == null) {
            throw new GambleNotAvailableException("No spin found. Please make a spin first.");
        }

        Integer gambleMultiplier = spin.getGambleMultiplier();

        if (gambleMultiplier == null || gambleMultiplier != -1) {
            throw new GambleNotAvailableException(
                    "No pending gamble decision available. Gamble has already been resolved or no win to gamble."
            );
        }

        spin.setUserChoice(choice);

        MySlotGame mySlotGame = new MySlotGame();
        org.springframework.beans.BeanUtils.copyProperties(slotContext, mySlotGame,
                "spinProcessors", "postSpinProcessors");

        if (mySlotGame.isProcessorsNeedInitialization()) {
            mySlotGame.initializeProcessorsIfNeeded();
            mySlotGame.setProcessorsNeedInitialization(false);
        }

        processGambleChoice(mySlotGame, spin, spin.getStake(), spin.getPreSpinStates());
        logger.logOriginalSpin(spin);

        List<SlotGameDto> linearizedResult = spin.linearize();
        addSpinAndSettingsLinks(linearizedResult);

        return Optional.of(linearizedResult);
    }

    private void processGambleChoice(MySlotGame slotGame, SlotGameDto spin, Double stake, List<Integer> states) {
        List<SlotSpinProcessor> postSpinProcessors = slotGame.getPostSpinProcessors();

        if (postSpinProcessors != null && !postSpinProcessors.isEmpty()) {
            for (SlotSpinProcessor processor : postSpinProcessors) {
                processor.processSpin(
                        spin, // Pass the original non-linearized spin
                        slotGame,
                        states != null ? states : spin.getPostSpinStates(),
                        spin.getReelsSetIndex() != null ? spin.getReelsSetIndex() : 0,
                        spin.getReelStopPositions() != null ? spin.getReelStopPositions() : List.of(),
                        stake != null ? stake : 0.0,
                        spin.getSlotGameStickyData(),
                        spin.getRecursionLevel() != null ? spin.getRecursionLevel() : 0
                );
            }
        }
    }

    private void addGambleLinks(List<SlotGameDto> spins) {
        if (spins == null || spins.isEmpty()) {
            return;
        }

        SlotGameDto firstSpin = spins.get(0);

        if (firstSpin.getGambleMultiplier() == -1) {
            if (firstSpin.getLinks() == null) {
                firstSpin.setLinks(new java.util.HashMap<>());
            }

            firstSpin.getLinks().put("COLLECT", "/slot/gamble?choice=1");
            firstSpin.getLinks().put("GAMBLE", "/slot/gamble?choice=2");
        }
    }

    private void addSpinAndSettingsLinks(List<SlotGameDto> spins) {
        if (spins == null || spins.isEmpty()) {
            return;
        }

        SlotGameDto firstSpin = spins.get(0);

        if (firstSpin.getGambleMultiplier() != -1) {
            if (firstSpin.getLinks() == null) {
                firstSpin.setLinks(new java.util.HashMap<>());
            }

            firstSpin.getLinks().put("SPIN", "/slot/spin?stake=0.10");
            firstSpin.getLinks().put("SETTINGS", "/slot/settings");
        }
    }
}