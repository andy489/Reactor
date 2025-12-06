package com.relax.reactor.service.gamelogic.processors;

import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.core.SlotSpinProcessor;
import com.relax.reactor.service.gamelogic.core.strategy.ClustersPaysStrategyCalculator;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;
import com.relax.reactor.service.gamelogic.dto.payout.SlotContactDto;
import com.relax.reactor.service.gamelogic.enumerated.Strategy;
import com.relax.reactor.service.gamelogic.enumerated.WildMultipliersAggregationType;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Setter
@Accessors(chain = true)
public class P02_ClusterPayoutStrategyProcessor implements SlotSpinProcessor {

    private Map<Integer, TreeMap<Integer, Double>> payouts;

    private Strategy strategy;

    private Integer minMatch;

    private Map<Integer, Double> wildMultipliers;

    private Map<Integer, WildMultipliersAggregationType> wildMultipliersAggregations;

    private List<Integer> excludeMatchSymbols;

    private List<List<Integer>> neighbors;

    @Override
    public void processSpin(SlotGameDto spinData, SlotContext slotContext, List<Integer> currentStates,
                            int reelsSetIndex, List<Integer> reelStopPositions, double totalStake,
                            SlotGameStickyData parentStickyData, int recursionLevel) {

        if (recursionLevel >= 0) {

            ClustersPaysStrategyCalculator calculator = new ClustersPaysStrategyCalculator()
                    .setPayouts(payouts)
                    .setStrategy(strategy)
                    .setMinMatch(minMatch)
                    .setWildMultipliers(wildMultipliers)
                    .setWildMultipliersAggregations(wildMultipliersAggregations)
                    .setExcludeMatchSymbols(excludeMatchSymbols)
                    .setNeighbors(neighbors);

            List<List<Integer>> screen = spinData.getGrid();
            SlotContactDto contacts = calculator.calc(screen, totalStake);

            List<BaseDto> payoutData = spinData.getPayoutData();

            if (contacts != null) {
                payoutData.add(0, contacts);
            }
        }
    }
}
