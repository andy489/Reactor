package com.relax.reactor.service.gamelogic.processors;

import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.enumerated.AvalancheMode;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Accessors(chain = true)
public class P01_StickyApplierProcessor implements SlotSpinProcessor {

    private AvalancheMode avalancheMode;

    @Override
    public void processSpin(SlotGameDto spinData, SlotContext slotContext, List<Integer> currentStates,
                            int reelsSetIndex, List<Integer> reelStopPositions, double totalStake,
                            SlotGameStickyData parentStickyData, int recursionLevel) {

        if (recursionLevel > 0 && avalancheMode == AvalancheMode.REGENERATE) {

            int SIZE = parentStickyData.getStickyReels().size();

            List<List<Integer>> grid = spinData.getGrid();

            List<Integer> stickyReels = parentStickyData.getStickyReels();
            List<Integer> stickyPos = parentStickyData.getStickyPos();
            List<Integer> stickySymbolIds = parentStickyData.getStickyTileIds();

            for (int i = 0; i < SIZE; i++) {
                grid.get(stickyReels.get(i)).set(stickyPos.get(i), stickySymbolIds.get(i));
            }
        }
    }
}
