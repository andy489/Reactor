package com.relax.reactor.service.gamelogic.slot;

import com.relax.reactor.service.gamelogic.core.data.SlotContext;
import com.relax.reactor.service.gamelogic.processors.P01_PopulateScreenProcessor;
import com.relax.reactor.service.gamelogic.processors.P02_StickyApplierProcessor;
import com.relax.reactor.service.gamelogic.processors.P03_ClusterPayoutStrategyProcessor;
import com.relax.reactor.service.gamelogic.processors.P04_AvalancheReactorProcessor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;

@Getter
@Setter
@Accessors(chain = true)
public class MySlotGame extends SlotContext {

    private boolean processorsNeedInitialization = true;

    public MySlotGame() {
        super();
    }

    public void initializeProcessorsIfNeeded() {
        if (processorsNeedInitialization && this.payTable != null) {
            initializeAndArrangeProcessors();
            processorsNeedInitialization = false;
        }
    }

    private void initializeAndArrangeProcessors() {
        // Create spin processors
        P01_PopulateScreenProcessor p01_PopulateScreenProcessor = new P01_PopulateScreenProcessor();

        P02_StickyApplierProcessor p02_StickyApplierProcessor = new P02_StickyApplierProcessor();

        P03_ClusterPayoutStrategyProcessor p03_ClusterPayoutStrategyProcessor =
                new P03_ClusterPayoutStrategyProcessor()
                        .setPayouts(payTable)
                        .setStrategy(strategy)
                        .setMinMatch(minMatch)
                        .setWildMultipliers(wildMultipliers)
                        .setWildMultipliersAggregations(wildMultipliersAggregations)
                        .setExcludeMatchSymbols(new ArrayList<>())
                        .setNeighbors(neighbors);

        P04_AvalancheReactorProcessor p04_AvalancheReactorProcessor = new P04_AvalancheReactorProcessor()
                .setReactionReelSetIndexes(reactionGameReelSetIndexes)
                .setReactionReelSetChances(reactionGameReelSetChances);
        // EO: Create Spin Processors

        // Arrange Spin Processors
        spinProcessors.add(p01_PopulateScreenProcessor);
        spinProcessors.add(p02_StickyApplierProcessor);
        spinProcessors.add(p03_ClusterPayoutStrategyProcessor);
        spinProcessors.add(p04_AvalancheReactorProcessor);
        // EO: Arrange Spin Processors
    }
}
