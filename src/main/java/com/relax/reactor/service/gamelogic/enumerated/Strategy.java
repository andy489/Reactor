package com.relax.reactor.service.gamelogic.enumerated;

import lombok.Getter;

@Getter
public enum Strategy {

    LTR(MatchType.LTR),
    RTL(MatchType.RTL),
    BOTH_WAYS(MatchType.BOTH_WAYS),
    ALL_WAYS(MatchType.WAYS),
    MEGA_WAYS(MatchType.WAYS),
    SCATTERS_PAYS(MatchType.SCATTERS),
    CLUSTERS_PAYS(MatchType.CLUSTERS),
    SUPER_LINES(MatchType.SUPER_LINES),
    ADJACENT(MatchType.ADJACENT),
    ZIG_ZAG(MatchType.ZIG_ZAG),
    HONEY_COMB(MatchType.HONEY_COMB);

    private final MatchType matchType;

    Strategy(MatchType matchType) {
        this.matchType = matchType;
    }
}