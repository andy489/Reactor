package com.relax.reactor.service.gamelogic.enumerated;

import lombok.Getter;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Getter
public enum MatchType {

    LTR(singletonList(Strategy.LTR)),
    RTL(singletonList(Strategy.RTL)),
    BOTH_WAYS(singletonList(Strategy.BOTH_WAYS)),
    WAYS(asList(Strategy.ALL_WAYS, Strategy.MEGA_WAYS)),
    SCATTERS(singletonList(Strategy.SCATTERS_PAYS)),
    CLUSTERS(singletonList(Strategy.CLUSTERS_PAYS)),
    SUPER_LINES(singletonList(Strategy.SUPER_LINES)),
    ADJACENT(singletonList(Strategy.ADJACENT)),
    ZIG_ZAG(singletonList(Strategy.ZIG_ZAG)),
    HONEY_COMB(singletonList(Strategy.HONEY_COMB));

    private final List<Strategy> strategy;

    MatchType(List<Strategy> strategy) {
        this.strategy = strategy;
    }
}