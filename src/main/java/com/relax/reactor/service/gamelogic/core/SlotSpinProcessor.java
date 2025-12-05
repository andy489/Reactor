package com.relax.reactor.service.gamelogic.core;


import com.relax.reactor.service.gamelogic.dto.SlotGameDto;

import java.io.Serializable;
import java.util.List;

public interface SlotSpinProcessor extends Serializable {

    void processSpin(SlotGameDto spinData,
                     BaseSlot myGame,
                     List<Integer> myGameStates,
                     int reelsSetIndex,
                     List<Integer> stopPositions,
                     double totalStake,
                     SlotGameStickyData parentStickyData,
                     int recursionLevel
    );
}