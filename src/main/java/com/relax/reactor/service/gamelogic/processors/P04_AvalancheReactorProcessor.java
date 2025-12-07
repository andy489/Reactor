package com.relax.reactor.service.gamelogic.processors;

import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.core.data.GridPosition;
import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;
import com.relax.reactor.service.gamelogic.dto.payout.SlotContactDto;
import com.relax.reactor.service.gamelogic.dto.payout.SlotExplodeFallDto;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Accessors(chain = true)
public class P04_AvalancheReactorProcessor implements SlotSpinProcessor {

    private List<Integer> reactionReelSetIndexes;
    private List<Double> reactionReelSetChances;

    @Override
    public void processSpin(SlotGameDto spinData, SlotContext slotContext, List<Integer> currentStates,
                            int reelsSetIndex, List<Integer> reelStopPositions, double totalStake,
                            SlotGameStickyData parentStickyData, int recursionLevel) {

        if (recursionLevel >= 0) {

            List<BaseDto> payoutData = spinData.getPayoutData();

            // check if there are match combinations (explode fall tiles)
            boolean hasExplodeFallReactions = false;
            SlotContactDto slotContactDto = null;

            for (BaseDto dto : payoutData) {
                if (dto instanceof SlotContactDto) {
                    hasExplodeFallReactions = true;
                    slotContactDto = (SlotContactDto) dto;
                    break;
                }
            }
            // EO: check if there are match combinations (explode fall tiles)

            if (hasExplodeFallReactions) {

                // extract explode positions
                Set<GridPosition> explodePositions = new HashSet<>();

                List<List<List<Integer>>> contact2DimPos = slotContactDto.getContact2DimPositions();

                for (List<List<Integer>> clusterPositions : contact2DimPos) {
                    for (int i = 0; i < clusterPositions.size(); i++) {
                        List<Integer> positions = clusterPositions.get(i);

                        for (Integer currPos : positions) {
                            explodePositions.add(new GridPosition(i, currPos));
                        }
                    }
                }
                // EO: extract explode positions

                if (!explodePositions.isEmpty()) {
                    SlotExplodeFallDto explodeFallDto = new SlotExplodeFallDto();
                    List<List<Integer>> grid = spinData.getGrid();

                    for (GridPosition gp : explodePositions) {
                        explodeFallDto.getExplodeReels().add(gp.getReelInd());
                        explodeFallDto.getExplodePositions().add(gp.getRowInd());
                        explodeFallDto.getExplodeSymbolIds().add(grid.get(gp.getReelInd()).get(gp.getRowInd()));
                    }

                    SlotGameStickyData holdStickyData = new SlotGameStickyData();

                    for (int currReel = 0; currReel < grid.size(); ++currReel) {
                        int explosionsCount = 0;

                        for (int currPos = grid.get(currReel).size() - 1; currPos >= 0; --currPos) {
                            Integer currSymbolId = grid.get(currReel).get(currPos);

                            if (explodePositions.contains(new GridPosition(currReel, currPos))) {
                                // if we have an explosion
                                ++explosionsCount;
                            } else if (explosionsCount > 0) {
                                // no explosion but there were some underneath
                                explodeFallDto.getFallReels().add(currReel);
                                explodeFallDto.getFallStarts().add(currPos);
                                explodeFallDto.getFallStops().add(currPos + explosionsCount);
                                explodeFallDto.getFallSymbolIds().add(grid.get(currReel).get(currPos));

                                int shiftedPosition = currPos + explosionsCount;
                                holdStickyData.addStickyDataEntry(currReel, shiftedPosition, currSymbolId);
                            } else {
                                // no explosions at all so far
                                explodeFallDto.getHoldReels().add(currReel);
                                explodeFallDto.getHoldPositions().add(currPos);
                                explodeFallDto.getHoldSymbolIds().add(currSymbolId);

                                holdStickyData.addStickyDataEntry(currReel, currPos, currSymbolId);
                            }
                        }
                    }

                    spinData.getPayoutData().add(explodeFallDto);

                    // Here we use the actual symbols from the current reels after the explosion.
                    List<Integer> newReelsStopPositions = new ArrayList<>(spinData.getReelStopPositions());

                    for (int j = 0; j < explodeFallDto.getExplodeReels().size(); j++) {
                        if (newReelsStopPositions.get(explodeFallDto.getExplodeReels().get(j)) - 1 < 0) {
                            newReelsStopPositions.set(explodeFallDto.getExplodeReels().get(j),
                                    slotContext.getReelSets().get(reelsSetIndex).getReelSet()
                                            .get(explodeFallDto.getExplodeReels().get(j)).size() - 1);
                        } else {
                            newReelsStopPositions.set(explodeFallDto.getExplodeReels().get(j),
                                    newReelsStopPositions.get(explodeFallDto.getExplodeReels().get(j)) - 1);
                        }
                    }
                    // EO: Here we use the actual symbols from the current reels after the explosion.


                    // construct re-spin
                    int internalRecursion = recursionLevel + 1;

                    // Here we use symbols from randomly generated new reels from one reel set
//                    // select re-spin reel set index
//                    int reSpinReelSetIndex = slotContext.getRng()
//                            .getWeightedIndex(reactionReelSetIndexes, reactionReelSetChances);
//
//                    List<Integer> reSpinReelStopPositions = slotContext.generateReelsStopPositions(reSpinReelSetIndex);
//
//                    SlotGameDto reactionReSpin = slotContext.createSlotSpin(currentStates, reSpinReelSetIndex, reSpinReelStopPositions, totalStake,
//                            holdStickyData, internalRecursion);
                    // EO: Here we use symbols from randomly generated new reels from one reel set


                    SlotGameDto reactionReSpin = slotContext.createSlotSpin(currentStates, reelsSetIndex, newReelsStopPositions, totalStake,
                            holdStickyData, internalRecursion);

                    payoutData.add(reactionReSpin);
                }
            }
        }
    }
}
