package com.relax.reactor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relax.reactor.config.SlotContextFactory;
import com.relax.reactor.dto.PredefinedSequenceResponse;
import com.relax.reactor.exception.GambleNotAvailableException;
import com.relax.reactor.exception.PendingGambleException;
import com.relax.reactor.rng.RNG;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.dto.SettingsDto;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.SlotStatsDto;
import com.relax.reactor.service.gamelogic.slot.MySlotGame;
import com.relax.reactor.service.statistics.RunningStat;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.roundToPrecision;

@Service
public class SlotService {

    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();
    private static final DecimalFormat df = new DecimalFormat("#,###.##");
    {
        PERCENT_FORMAT.setMaximumFractionDigits(4);
    }

    private final ObjectMapper mapper;
    private final SlotContext slotContext;
    private final SpinResultLogger logger;
    private final RNG rng;

    public SlotService(ObjectMapper mapper,
                       SlotContext slotContext,
                       SpinResultLogger logger,
                       RNG rng, SlotContextFactory slotContextFactory) {
        this.mapper = mapper;
        this.slotContext = slotContext;
        this.logger = logger;
        this.rng = rng;
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
                        spin, // pass the original non-linearized spin
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

    public Optional<SlotStatsDto> runSimulation(Integer spins, Double stake) {

        RunningStat runningStat = new RunningStat(true);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < spins; i++) {
            MySlotGame mySlotGame = MySlotGame.createWithRNG(rng, slotContext);
            SlotGameDto mainSlotSpin = mySlotGame.createMainSlotSpin(stake, null);
            Double cumulativeWinAmount = mainSlotSpin.getCumulativeWinAmount();

            // System.out.println(mainSlotSpin.getCumulativeWinAmount());

            runningStat.push(roundToPrecision(cumulativeWinAmount / stake));
        }
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        SlotStatsDto slotStatsDto = new SlotStatsDto()
                .setRtp(formatAsPercentage(runningStat.mean()))
                .setMaxStakeMultiplier(runningStat.getMaxStakeMultiplier())
                .setHitRate(roundToPrecision(runningStat.hitRate(), 4))
                .setWinRate(roundToPrecision(runningStat.winRate(), 4))
                .setStake(stake)
                .setTotalSpinsCount(df.format(spins))
                .setMedian(roundToPrecision(runningStat.median(), 4))
                .setStandardDeviation(roundToPrecision(runningStat.standardDeviation(), 4))
                .setVariance(roundToPrecision(runningStat.variance(), 4))
                .setTimeElapsed(formatDuration(totalTime));

        return Optional.of(slotStatsDto);
    }

    public PredefinedSequenceResponse spinWithPredefinedSequence(double stake, List<Object> sequence, List<Integer> states) {
        rng.enablePredefinedSequence(sequence);

        try {
            int sequenceLength = sequence.size();

            Optional<List<SlotGameDto>> spinResult = spin(stake, states);

            int sequenceCallsMade = rng.getSequenceResultsConsumed();
            int totalCallsMade = rng.getTotalCallsMade();
            int randomCallsMade = rng.getRandomCallsMade();

            int resultsRemaining = Math.max(0, sequenceLength - sequenceCallsMade);

            boolean sequenceExhausted = !rng.hasMoreSequenceValues();

            boolean sequenceCompleted = sequenceCallsMade == sequenceLength && randomCallsMade == 0;

            PredefinedSequenceResponse response = new PredefinedSequenceResponse()
                    .setTotalRNGCallsMade(totalCallsMade)
                    .setPredefinedRNGConsumed(sequenceCallsMade)
                    .setPredefinedRNGRemaining(resultsRemaining)
                    .setSequenceCompleted(sequenceCompleted)
                    .setSequenceExhausted(sequenceExhausted)
                    .setRandomRNGUsed(randomCallsMade);

            spinResult.ifPresent(response::setSpinResults);

            return response;

        } catch (Exception e) {
            PredefinedSequenceResponse response = new PredefinedSequenceResponse();
            response.setError("Error at sequence position #" + rng.getSequenceResultsConsumed() + ": " + e.getMessage());
            return response;

        } finally {
            rng.disablePredefinedSequence();
        }
    }

    public static String formatDuration(long totalTimeMillis) {
        Duration duration = Duration.ofMillis(totalTimeMillis);

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millis = duration.toMillisPart();

        if (hours > 0) {
            return String.format("%d h %d min %d sec", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d min %d sec", minutes, seconds);
        } else if (seconds > 0) {
            return String.format("%d.%03d seconds", seconds, millis);
        } else {
            return String.format("%d ms", millis);
        }
    }

    private String formatAsPercentage(double value) {
        return PERCENT_FORMAT.format(value);
    }
}