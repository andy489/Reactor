package com.relax.reactor.service.gamelogic.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.relax.reactor.service.gamelogic.core.data.ReelSet;
import com.relax.reactor.service.gamelogic.enumerated.AvalancheMode;
import com.relax.reactor.service.gamelogic.enumerated.PayTableType;
import com.relax.reactor.service.gamelogic.enumerated.Strategy;
import com.relax.reactor.service.gamelogic.enumerated.WildMultipliersAggregationType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder({"gameName", "slotId", "version", "sharpRtp", "stateNum", "hasChoice", "gridDim", "tileIds",
        "tileNames", "linesNum", "lineDefinitions", "payTableType", "payTable", "strategy", "avalancheMode", "minStake",
        "minMatch", "wildMultipliers", "wildMultipliersAggregations", "reelSets",
})
public class SettingsDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected String gameName;
    protected Integer slotId;
    protected Integer version;
    protected Map<String, Double> sharpRtp;
    protected Integer stateNum;
    protected Boolean hasChoice;
    protected List<Integer> gridDim;
    protected Map<String, List<Integer>> tileIds;
    protected Map<Integer, String> tileNames;
    protected List<ReelSet> reelSets;
    protected Integer linesNum;
    protected List<List<Integer>> lineDefinitions;
    protected PayTableType payTableType;
    protected Map<Integer, TreeMap<Integer, Double>> payTable;
    protected Strategy strategy;
    protected AvalancheMode avalancheMode;
    protected Double minStake;
    protected Integer minMatch;
    protected Map<Integer, Double> wildMultipliers;
    protected Map<Integer, WildMultipliersAggregationType> wildMultipliersAggregations;
}
