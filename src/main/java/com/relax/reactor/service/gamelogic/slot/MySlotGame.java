package com.relax.reactor.service.gamelogic.slot;

import com.relax.reactor.service.gamelogic.core.SlotContext;
import com.relax.reactor.service.gamelogic.core.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
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

        P01_SpinTheReelsProcessor p01_SpinTheReelsProcessor = new P01_SpinTheReelsProcessor();

        super.getSpinProcessors().add(p01_SpinTheReelsProcessor);
    }

    public SlotGameDto createSlotSpin(List<Integer> states, int reelsSetIndex, List<Integer> reelsStopPositions,
                                      double stake, SlotGameStickyData slotGameStickyData, int recursionLevel) {

        SlotGameDto slotGameDto = new SlotGameDto()
                .setReelStopPositions(reelsStopPositions)
                .setPayoutData(new ArrayList<>())
                .setReelsSetIndex(reelsSetIndex)
                .setStakeMultiplier(1.0)
                .setRecursionLevel(recursionLevel);

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
