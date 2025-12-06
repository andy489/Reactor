package com.relax.reactor.service.gamelogic.slot;

import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.processors.P01_PopulateScreenProcessor;
import com.relax.reactor.service.gamelogic.processors.P02_ClusterPayoutStrategyProcessor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder
public class MySlotGame extends SlotContext {

    public MySlotGame() {
        super();
    }

    private List<SlotSpinProcessor> createAndArrangeProcessors() {
        List<SlotSpinProcessor> spinProcessors = new ArrayList<>();

        // Create spin processors
        P01_PopulateScreenProcessor p01_PopulateScreenProcessor = new P01_PopulateScreenProcessor();

        P02_ClusterPayoutStrategyProcessor p02_ClusterPayoutStrategyProcessor =
                new P02_ClusterPayoutStrategyProcessor()
                        .setPayouts(payTable)
                        .setStrategy(strategy)
                        .setMinMatch(minMatch)
                        .setWildMultipliers(wildMultipliers)
                        .setWildMultipliersAggregations(wildMultipliersAggregations)
                        .setExcludeMatchSymbols(new ArrayList<>())
                        .setNeighbors(neighbors);
        // EO: Create Spin Processors

        // Arrange Spin Processors
        spinProcessors.add(p01_PopulateScreenProcessor);
        spinProcessors.add(p02_ClusterPayoutStrategyProcessor);
        // EO: Arrange Spin Processors

        return spinProcessors;
    }

    public SlotGameDto createSlotSpin(List<Integer> states, int reelsSetIndex, List<Integer> reelsStopPositions,
                                      double stake, SlotGameStickyData slotGameStickyData, int recursionLevel) {

        // Ensure processors are created before use
        if (this.spinProcessors == null || this.spinProcessors.isEmpty()) {
            this.spinProcessors = createAndArrangeProcessors();
        }

        SlotGameDto slotGameDto = new SlotGameDto()
                .setReelStopPositions(reelsStopPositions)
                .setPayoutData(new ArrayList<>())
                .setReelsSetIndex(reelsSetIndex)
                .setStakeMultiplier(1.0)
                .setRecursionLevel(recursionLevel)
                .setGridDim(this.gridDim);

        slotGameDto.setWinAmount(0.0d);

        if (states != null) {
            slotGameDto.setPreSpinStates(new ArrayList<>(states));
        }

        SlotGameStickyData slotGameDtoStickyData = new SlotGameStickyData();
        if (slotGameStickyData != null) {
            slotGameDtoStickyData.setStickyPos(slotGameStickyData.getStickyPos())
                    .setStickyTileIds(slotGameStickyData.getStickyTileIds())
                    .setStickyReels(slotGameStickyData.getStickyReels());
        }

        slotGameDto.setSlotGameStickyData(slotGameDtoStickyData);

        // Here is the most important mechanical part in the architecture of a slot machine.
        // It may not look recursive, but can act like one.
        for (SlotSpinProcessor spinProcessor : this.spinProcessors) {

            spinProcessor.processSpin(slotGameDto, this, states, reelsSetIndex, reelsStopPositions, stake,
                    slotGameDtoStickyData, recursionLevel);
        }

        if (states != null) {
            slotGameDto.setPostSpinStates(new ArrayList<>(states));
        }

        return slotGameDto;
    }

    public SlotGameDto createMainSlotSpin(double stake, List<Integer> states) {

        List<Integer> actualStates = new ArrayList<>();

        if (states == null) {
            if (stateNum != null) {
                for (int i = 0; i < this.stateNum; i++) {
                    actualStates.add(i);
                }
            }
        } else {
            actualStates = new ArrayList<>(states);
        }

        int reelSetIndex = generateMainReelSetIndex();
        List<Integer> reelsStopPositions = generateReelsStopPositions(reelSetIndex);

        SlotGameStickyData slotGameStickyData = new SlotGameStickyData();
        int recursionLevel = 0;

        return this.createSlotSpin(actualStates, reelSetIndex, reelsStopPositions, stake, slotGameStickyData, recursionLevel);
    }

    public int generateMainReelSetIndex() {
        return rng.getWeightedIndex(this.mainGameReelSetIndexes, this.mainGameReelSetChances);
    }

    public List<Integer> generateReelsStopPositions(int reelsSetIndex) {

        List<Integer> result = new ArrayList<>();

        List<List<Integer>> reelSet = this.reelSets.get(reelsSetIndex).getReelSet();

        for (List<Integer> currReel : reelSet) {
            int currReelSize = currReel.size();
            result.add(rng.getUniformIndex(0, currReelSize));
        }

        return result;
    }
}
