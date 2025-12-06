package com.relax.reactor.service.gamelogic.processors;

import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;

import java.util.ArrayList;
import java.util.List;

import static com.relax.reactor.service.gamelogic.core.util.UtilConstants.SCREEN_REELS_IND;
import static com.relax.reactor.service.gamelogic.core.util.UtilConstants.SCREEN_ROWS_IND;
import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.fit;

public class P01_PopulateScreenProcessor implements SlotSpinProcessor {

    @Override
    public void processSpin(SlotGameDto spinData, SlotContext slotContext, List<Integer> currentStates, int reelsSetIndex,
                            List<Integer> reelStopPositions, double totalStake, SlotGameStickyData parentStickyData,
                            int recursionLevel) {

        if (recursionLevel >= 0) {

            List<List<Integer>> grid = new ArrayList<>();

            List<List<Integer>> currentReels = slotContext.getReelSets().get(reelsSetIndex).getReelSet();

            List<Integer> gridDim = slotContext.getGridDim();

            int reelsNum = gridDim.get(SCREEN_REELS_IND);
            int rowsNum = gridDim.get(SCREEN_ROWS_IND);

            for (int i = 0; i < reelsNum; i++) {
                List<Integer> currGridReel = new ArrayList<>();
                List<Integer> currReel = currentReels.get(i);
                int currReelSize = currReel.size();
                int currReelStopPos = reelStopPositions.get(i);

                for (int j = 0; j < rowsNum; j++) {
                    currGridReel.add(currentReels.get(i).get(fit(j + currReelStopPos, currReelSize)));
                }

                grid.add(currGridReel);
            }

            spinData.setGrid(grid);
        }
    }
}
