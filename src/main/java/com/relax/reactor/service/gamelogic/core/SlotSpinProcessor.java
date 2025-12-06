package com.relax.reactor.service.gamelogic.core;


import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;

import java.io.Serializable;
import java.util.List;

public interface SlotSpinProcessor extends Serializable {

    void processSpin(SlotGameDto spinData,
                     SlotContext slotContext,
                     List<Integer> currentStates,
                     int reelsSetIndex,
                     List<Integer> reelStopPositions,
                     double totalStake,
                     SlotGameStickyData parentStickyData,
                     int recursionLevel
    );
}