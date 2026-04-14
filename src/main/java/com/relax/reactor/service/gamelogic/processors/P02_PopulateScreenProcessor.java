package com.relax.reactor.service.gamelogic.processors;

import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.enumerated.AvalancheMode;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.fit;

@Setter
@Accessors(chain = true)
public class P02_PopulateScreenProcessor implements SlotSpinProcessor {

    private AvalancheMode avalancheMode;

    private List<Integer> initialScreenTileIds;
    private List<Integer> reactionScreenTileIds;

    private TreeMap<Integer, List<Double>> tileWeights;

    @Override
    public void processSpin(SlotGameDto spinData, SlotContext slotContext, List<Integer> currentStates, int reelsSetIndex,
                            List<Integer> reelStopPositions, double totalStake, SlotGameStickyData parentStickyData,
                            int recursionLevel) {

        if (recursionLevel >= 0) {
            List<Integer> currScreenTilesIds = reactionScreenTileIds;
            if (recursionLevel == 0) {
                currScreenTilesIds = initialScreenTileIds;
            }

            List<List<Integer>> grid = spinData.getGrid();
            int reelsNum = grid.size();
            int rowsNum = grid.get(0).size();

            switch (avalancheMode) {
                case REGENERATE -> {
                    for (List<Integer> currReel : grid) {
                        for (int j = 0; j < rowsNum; j++) {
                            if (currReel.get(j) < 0) {
                                // RNG: select tile to populate
                                int selectedTileIdIndex = slotContext.getRng().getWeightedIndex(currScreenTilesIds,
                                        tileWeights.floorEntry(recursionLevel).getValue());
                                int selectedTileId = currScreenTilesIds.get(selectedTileIdIndex);
                                currReel.set(j, selectedTileId);
                            }
                        }
                    }
                }
                case CASCADE, REROLL -> {
                    List<List<Integer>> currentReels = slotContext
                            .getReelSets()
                            .get(reelsSetIndex)
                            .getReelSet();

                    for (int i = 0; i < reelsNum; i++) {
                        int currReelStopPos = reelStopPositions.get(i);
                        List<Integer> currReel = grid.get(i);
                        int currReelSize = currentReels.get(i).size();

                        for (int j = 0; j < rowsNum; j++) {
                            currReel.set(j, currentReels.get(i).get(fit(j + currReelStopPos, currReelSize)));
                        }

                        grid.set(i, currReel);
                    }
                }
            }

            spinData.setGrid(grid);
        }
    }
}
