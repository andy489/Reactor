package com.relax.reactor.service.gamelogic.slot;

import com.relax.reactor.rng.RNG;
import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.enumerated.AvalancheMode;
import com.relax.reactor.service.gamelogic.processors.*;
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

        List<Integer> initialScreenTileIds = tileIds.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .toList();

        List<Integer> reactionScreenTileIds = tileIds.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("blocker"))
                .flatMap(entry -> entry.getValue().stream())
                .toList();

        // Create spin processors
        P01_StickyApplierProcessor p01_StickyApplierProcessor = new P01_StickyApplierProcessor()
                .setAvalancheMode(avalancheMode);

        P02_PopulateScreenProcessor p02_PopulateScreenProcessor = new P02_PopulateScreenProcessor()
                .setInitialScreenTileIds(initialScreenTileIds)
                .setReactionScreenTileIds(reactionScreenTileIds)
                .setTileWeights(tileWeights)
                .setAvalancheMode(avalancheMode);

        P03_StickyApplierProcessor p03_StickyApplierProcessor = new P03_StickyApplierProcessor()
                .setAvalancheMode(avalancheMode);

        P04_ClusterPayoutStrategyProcessor p04_ClusterPayoutStrategyProcessor =
                new P04_ClusterPayoutStrategyProcessor()
                        .setPayouts(payTable)
                        .setStrategy(strategy)
                        .setMinMatch(minMatch)
                        .setWildMultipliers(wildMultipliers)
                        .setWildMultipliersAggregations(wildMultipliersAggregations)
                        .setExcludeMatchSymbols(new ArrayList<>())
                        .setNeighbors(neighbors);

        P05_AvalancheReactorProcessor p05_AvalancheReactorProcessor = new P05_AvalancheReactorProcessor()
                .setReelSetIndexes(reelSetIndexes)
                .setReelSetChances(reelSetChances)
                .setAvalancheMode(avalancheMode);

        P06_SlotSumPayoutsProcessor p06_SlotSumPayoutsProcessor = new P06_SlotSumPayoutsProcessor();

        P07_PropertiesAdjustmentsProcessor p07_PropertiesAdjustmentsProcessor =
                new P07_PropertiesAdjustmentsProcessor();
        // EO: Create spin processors

        // Arrange spin processors
        spinProcessors.add(p01_StickyApplierProcessor);
        spinProcessors.add(p02_PopulateScreenProcessor);
        spinProcessors.add(p03_StickyApplierProcessor);
        spinProcessors.add(p04_ClusterPayoutStrategyProcessor);
        spinProcessors.add(p05_AvalancheReactorProcessor);
        spinProcessors.add(p06_SlotSumPayoutsProcessor);
        spinProcessors.add(p07_PropertiesAdjustmentsProcessor);
        // EO: Arrange spin processors

        // Create post-spin processors (gambles and other choices)
        P08_GambleChoicePostSpinProcessor p08_GambleChoicePostSpinProcessor = new P08_GambleChoicePostSpinProcessor();
        // EO: Create post-spin processors

        // Arrange post-spin Processors
        postSpinProcessors.add(p08_GambleChoicePostSpinProcessor);
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
        target.setAvalancheMode(source.getAvalancheMode());

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

        if (source.getReelSetIndexes() != null) {
            target.setReelSetIndexes(source.getReelSetIndexes());
        }

        if (source.getReelSetChances() != null) {
            target.setReelSetChances(source.getReelSetChances());
        }

        if (source.getTileWeights() != null) {
            target.setTileWeights(copyTileWeights(source.getTileWeights()));
        }

        if (source.getTileIds() != null) {
            target.setTileIds(copyMap(source.getTileIds()));
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

    private static TreeMap<Integer, List<Double>> copyTileWeights(TreeMap<Integer, List<Double>> original) {
        if (original == null) return null;
        TreeMap<Integer, List<Double>> copy = new TreeMap<>();
        for (Map.Entry<Integer, List<Double>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
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
