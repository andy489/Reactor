package com.relax.reactor.service.gamelogic.slot;

import com.relax.reactor.rng.RNG;
import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.processors.P01_PopulateScreenProcessor;
import com.relax.reactor.service.gamelogic.processors.P02_StickyApplierProcessor;
import com.relax.reactor.service.gamelogic.processors.P03_ClusterPayoutStrategyProcessor;
import com.relax.reactor.service.gamelogic.processors.P04_AvalancheReactorProcessor;
import com.relax.reactor.service.gamelogic.processors.P05_SlotSumPayoutsProcessor;
import com.relax.reactor.service.gamelogic.processors.P06_PropertiesAdjustmentsProcessor;
import com.relax.reactor.service.gamelogic.processors.P07_GambleChoicePostSpinProcessor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@Accessors(chain = true)
public class MySlotGame extends SlotContext {

    private boolean processorsNeedInitialization = true;

    public MySlotGame() {
        super();
    }

    public void initializeProcessorsIfNeeded() {
        if (processorsNeedInitialization && this.payTable != null) {
            initializeAndArrangeProcessors();
            processorsNeedInitialization = false;
        }
    }

    public void initializeAndArrangeProcessors() {
        // Create spin processors
        P01_PopulateScreenProcessor p01_PopulateScreenProcessor = new P01_PopulateScreenProcessor();

        P02_StickyApplierProcessor p02_StickyApplierProcessor = new P02_StickyApplierProcessor();

        P03_ClusterPayoutStrategyProcessor p03_ClusterPayoutStrategyProcessor =
                new P03_ClusterPayoutStrategyProcessor()
                        .setPayouts(payTable)
                        .setStrategy(strategy)
                        .setMinMatch(minMatch)
                        .setWildMultipliers(wildMultipliers)
                        .setWildMultipliersAggregations(wildMultipliersAggregations)
                        .setExcludeMatchSymbols(new ArrayList<>())
                        .setNeighbors(neighbors);

        P04_AvalancheReactorProcessor p04_AvalancheReactorProcessor = new P04_AvalancheReactorProcessor()
                .setReactionReelSetIndexes(reactionGameReelSetIndexes)
                .setReactionReelSetChances(reactionGameReelSetChances);

        P05_SlotSumPayoutsProcessor p05_SlotSumPayoutsProcessor = new P05_SlotSumPayoutsProcessor();

        P06_PropertiesAdjustmentsProcessor p06_PropertiesAdjustmentsProcessor =
                new P06_PropertiesAdjustmentsProcessor();
        // EO: Create spin processors

        // Arrange spin processors
        spinProcessors.add(p01_PopulateScreenProcessor);
        spinProcessors.add(p02_StickyApplierProcessor);
        spinProcessors.add(p03_ClusterPayoutStrategyProcessor);
        spinProcessors.add(p04_AvalancheReactorProcessor);
        spinProcessors.add(p05_SlotSumPayoutsProcessor);
        spinProcessors.add(p06_PropertiesAdjustmentsProcessor);
        // EO: Arrange spin processors

        // Create post-spin processors (gambles and other choices)
        P07_GambleChoicePostSpinProcessor p07_GambleChoicePostSpinProcessor = new P07_GambleChoicePostSpinProcessor();
        // EO: Create post-spin processors

        // Arrange post-spin Processors
        postSpinProcessors.add(p07_GambleChoicePostSpinProcessor);
        // EO: Arrange post-spin Processors
    }

    public static MySlotGame createWithRNG(RNG rng, SlotContext baseConfig) {
        MySlotGame game = new MySlotGame();

        // 1. Set the RNG (fixes your NullPointerException)
        game.setRng(rng);

        // 2. Copy ALL configuration from base SlotContext
        copyAllProperties(baseConfig, game);

        // 3. Initialize processors
        game.initializeProcessorsIfNeeded();

        return game;
    }

    private static void copyAllProperties(SlotContext source, MySlotGame target) {
        // Copy primitive and simple fields
        target.setGameName(source.getGameName());
        target.setSlotId(source.getSlotId());
        target.setVersion(source.getVersion());
        target.setSharpRtp(source.getSharpRtp());
        target.setStateNum(source.getStateNum());
        target.setHasChoice(source.getHasChoice());
        target.setLinesNum(source.getLinesNum());
        target.setPayTableType(source.getPayTableType());
        target.setStrategy(source.getStrategy());
        target.setMinStake(source.getMinStake());
        target.setMinMatch(source.getMinMatch());
        target.setAfterDecimalPrecision(source.getAfterDecimalPrecision());
        target.setBonusNum(source.getBonusNum());

        // Copy collections with new instances to avoid shared references
        if (source.getGridDim() != null) {
            target.setGridDim(new ArrayList<>(source.getGridDim()));
        }

        if (source.getStateDescriptor() != null) {
            target.setStateDescriptor(copyMap(source.getStateDescriptor()));
        }

        if (source.getRecursionLevelsDescriptor() != null) {
            target.setRecursionLevelsDescriptor(copyMap(source.getRecursionLevelsDescriptor()));
        }

        if (source.getTileIds() != null) {
            target.setTileIds(copyStringListMap(source.getTileIds()));
        }

        if (source.getTileNames() != null) {
            target.setTileNames(copyMap(source.getTileNames()));
        }

        if (source.getReelSets() != null) {
            target.setReelSets(new ArrayList<>(source.getReelSets()));
        }

        if (source.getMainGameReelSetIndexes() != null) {
            target.setMainGameReelSetIndexes(new ArrayList<>(source.getMainGameReelSetIndexes()));
        }

        if (source.getMainGameReelSetChances() != null) {
            target.setMainGameReelSetChances(new ArrayList<>(source.getMainGameReelSetChances()));
        }

        if (source.getReactionGameReelSetIndexes() != null) {
            target.setReactionGameReelSetIndexes(new ArrayList<>(source.getReactionGameReelSetIndexes()));
        }

        if (source.getReactionGameReelSetChances() != null) {
            target.setReactionGameReelSetChances(new ArrayList<>(source.getReactionGameReelSetChances()));
        }

        if (source.getLineDefinitions() != null) {
            List<List<Integer>> copiedLineDefinitions = new ArrayList<>();
            for (List<Integer> line : source.getLineDefinitions()) {
                copiedLineDefinitions.add(new ArrayList<>(line));
            }
            target.setLineDefinitions(copiedLineDefinitions);
        }

        if (source.getPayTable() != null) {
            Map<Integer, TreeMap<Integer, Double>> copiedPayTable = new TreeMap<>();
            for (Map.Entry<Integer, TreeMap<Integer, Double>> entry : source.getPayTable().entrySet()) {
                copiedPayTable.put(entry.getKey(), new TreeMap<>(entry.getValue()));
            }
            target.setPayTable(copiedPayTable);
        }

        if (source.getNeighbors() != null) {
            List<List<Integer>> copiedNeighbors = new ArrayList<>();
            for (List<Integer> neighborList : source.getNeighbors()) {
                copiedNeighbors.add(new ArrayList<>(neighborList));
            }
            target.setNeighbors(copiedNeighbors);
        }

        if (source.getWildMultipliers() != null) {
            target.setWildMultipliers(copyMap(source.getWildMultipliers()));
        }

        if (source.getWildMultipliersAggregations() != null) {
            target.setWildMultipliersAggregations(copyMap(source.getWildMultipliersAggregations()));
        }

        if (source.getBonusNames() != null) {
            target.setBonusNames(copyMap(source.getBonusNames()));
        }

        // Copy processor lists (important!)
        if (source.getSpinProcessors() != null) {
            target.getSpinProcessors().clear();
            target.getSpinProcessors().addAll(source.getSpinProcessors());
        }

        if (source.getPostSpinProcessors() != null) {
            target.getPostSpinProcessors().clear();
            target.getPostSpinProcessors().addAll(source.getPostSpinProcessors());
        }
    }

    // Helper methods for copying collections
    private static <K, V> Map<K, V> copyMap(Map<K, V> original) {
        if (original == null) return null;
        return new TreeMap<>(original);
    }

    private static Map<String, List<Integer>> copyStringListMap(Map<String, List<Integer>> original) {
        if (original == null) return null;
        Map<String, List<Integer>> copy = new TreeMap<>();
        for (Map.Entry<String, List<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}
