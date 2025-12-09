package com.relax.reactor.service.gamelogic.core.data;

import com.relax.reactor.rng.RNG;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.core.SpinHandlers;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.enumerated.PayTableType;
import com.relax.reactor.service.gamelogic.enumerated.Strategy;
import com.relax.reactor.service.gamelogic.enumerated.WildMultipliersAggregationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class SlotContext extends SpinHandlers implements Serializable {

    protected transient RNG rng;

    protected String gameName;
    protected Integer slotId;
    protected Integer version;
    protected Double sharpRtp;
    protected Integer stateNum;

    protected Map<Integer, String> stateDescriptor;
    protected Map<Integer, String> recursionLevelsDescriptor;

    protected Boolean hasChoice;

    protected List<Integer> gridDim;

    protected Integer bonusNum;
    protected Map<Integer, String> bonusNames;

    protected Map<String, List<Integer>> tileIds;
    protected Map<Integer, String> tileNames;

    protected List<ReelSet> reelSets;

    protected List<Integer> mainGameReelSetIndexes;
    protected List<Double> mainGameReelSetChances;

    protected List<Integer> reactionGameReelSetIndexes;
    protected List<Double> reactionGameReelSetChances;

    protected Integer linesNum;
    protected List<List<Integer>> lineDefinitions;

    protected PayTableType payTableType;
    protected Map<Integer, TreeMap<Integer, Double>> payTable;
    protected List<List<Integer>> neighbors;

    protected Strategy strategy;
    protected Double minStake;
    protected Integer minMatch;

    protected Map<Integer, Double> wildMultipliers;
    protected Map<Integer, WildMultipliersAggregationType> wildMultipliersAggregations;

    protected Integer afterDecimalPrecision = 2;

    public SlotGameDto createSlotSpin(List<Integer> states, int reelsSetIndex, List<Integer> reelsStopPositions,
                                      double stake, SlotGameStickyData slotGameStickyData, int recursionLevel) {

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
