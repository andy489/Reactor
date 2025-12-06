package com.relax.reactor.service.gamelogic.slot;

import com.relax.reactor.service.gamelogic.core.SlotContext;
import com.relax.reactor.service.gamelogic.core.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;
import com.relax.reactor.service.gamelogic.dto.payout.TestProcessorsAreActivatedDto;

import java.util.List;

public class P01_SpinTheReelsProcessor implements SlotSpinProcessor {

    @Override
    public void processSpin(SlotGameDto spinData, SlotContext myGame, List<Integer> myGameStates, int reelsSetIndex,
                            List<Integer> stopPositions, double totalStake, SlotGameStickyData parentStickyData,
                            int recursionLevel) {

        System.out.println("[DEBUG] Recursion level: " + recursionLevel + " Spinning the reels");

        List<BaseDto> payoutData = spinData.getPayoutData();

        payoutData.add(new TestProcessorsAreActivatedDto().setCustom("Test processors work"));
    }
}
