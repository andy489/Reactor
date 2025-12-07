package com.relax.reactor.service.gamelogic.processors;

import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;

import java.util.List;

public class P05_SlotSumPayoutsProcessor implements SlotSpinProcessor {

    @Override
    public void processSpin(SlotGameDto spinData, SlotContext slotContext, List<Integer> currentStates,
                            int reelsSetIndex, List<Integer> reelStopPositions, double totalStake,
                            SlotGameStickyData parentStickyData, int recursionLevel) {

        List<BaseDto> payoutData = spinData.getPayoutData();

        double spinWinAmount = 0.0d;

        for(BaseDto dto : payoutData) {
            if(dto instanceof SlotGameDto) {
                continue;
            }

            spinWinAmount += dto.getWinAmount();
        }

        spinData.setWinAmount(spinWinAmount);
    }
}
