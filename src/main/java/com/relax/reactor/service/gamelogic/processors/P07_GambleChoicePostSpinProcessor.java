package com.relax.reactor.service.gamelogic.processors;

import com.relax.reactor.rng.RNG;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;

import java.util.List;

import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.roundToPrecision;

public class P07_GambleChoicePostSpinProcessor implements SlotSpinProcessor {

    @Override
    public void processSpin(SlotGameDto spinData, SlotContext slotContext, List<Integer> currentStates,
                            int reelsSetIndex, List<Integer> reelStopPositions, double totalStake,
                            SlotGameStickyData parentStickyData, int recursionLevel) {

        Integer userChoice = spinData.getUserChoice();

        switch (userChoice) {
            case 1: {
                // COLLECT
                spinData.setGambleMultiplier(1);

                Double stashedCumulativeWinAmountBeforeGambleChoice =
                        spinData.getStashedCumulativeWinAmountBeforeGambleChoice();

                spinData.setCumulativeWinAmount(stashedCumulativeWinAmountBeforeGambleChoice);
                spinData.setStashedCumulativeWinAmountBeforeGambleChoice(null);

                break;
            }
            case 2: {
                // GAMBLE
                RNG rng = slotContext.getRng();

                int gambleOutcome = rng.getUniformIndex(1, 2);

                if (gambleOutcome == 1) {
                    // LOST
                    spinData.setGambleMultiplier(0);

                    spinData.setCumulativeWinAmount(0.0d);
                    spinData.setStashedCumulativeWinAmountBeforeGambleChoice(null);
                } else {
                    // WIN
                    spinData.setGambleMultiplier(2);

                    Double stashedCumulativeWinAmountBeforeGambleChoice =
                            spinData.getStashedCumulativeWinAmountBeforeGambleChoice();

                    spinData.setCumulativeWinAmount(
                            roundToPrecision(stashedCumulativeWinAmountBeforeGambleChoice * 2.0d));

                    spinData.setStashedCumulativeWinAmountBeforeGambleChoice(null);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid gamble choice");
            }
        }
    }
}
