package com.relax.reactor.service.gamelogic.processors;

import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;

import java.util.ArrayList;
import java.util.List;

import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.roundToPrecision;

public class P06_PropertiesAdjustmentsProcessor implements SlotSpinProcessor {

    @Override
    public void processSpin(SlotGameDto spinData, SlotContext slotContext, List<Integer> currentStates,
                            int reelsSetIndex, List<Integer> reelStopPositions, double totalStake,
                            SlotGameStickyData parentStickyData, int recursionLevel) {

        if (recursionLevel == 0) {
            spinData.setStake(totalStake);
            setSpinPropertiesRecursively(spinData);
        }
    }

    public void setSpinPropertiesRecursively(SlotGameDto slotGameDto) {
        if (slotGameDto == null) {
            return;
        }

        List<SlotGameDto> allSpins = new ArrayList<>();
        collectAllSpinsRecursive(slotGameDto, allSpins);

        int totalSpins = allSpins.size();

        double accumulated = 0.0;

        for (int i = 0; i < totalSpins; i++) {
            SlotGameDto spin = allSpins.get(i);

            spin.setSpinNum(i + 1);

            spin.setTotalSpins(totalSpins);

            Double winAmount = spin.getWinAmount();
            double currentWin = (winAmount != null ? winAmount : 0.0);
            accumulated += currentWin;
            spin.setAccumulatedWinAmount(roundToPrecision(accumulated));
        }

        double cumulative = 0.0;
        for (int i = totalSpins - 1; i >= 0; i--) {
            SlotGameDto spin = allSpins.get(i);

            Double winAmount = spin.getWinAmount();
            double currentWin = (winAmount != null ? winAmount : 0.0);
            cumulative += currentWin;
            spin.setCumulativeWinAmount(roundToPrecision(cumulative));
        }
    }

    private void collectAllSpinsRecursive(SlotGameDto current, List<SlotGameDto> result) {
        if (current == null) {
            return;
        }

        result.add(current);

        if (current.getPayoutData() != null) {
            for (BaseDto baseDto : current.getPayoutData()) {
                if (baseDto instanceof SlotGameDto) {
                    collectAllSpinsRecursive((SlotGameDto) baseDto, result);
                }
            }
        }
    }
}
