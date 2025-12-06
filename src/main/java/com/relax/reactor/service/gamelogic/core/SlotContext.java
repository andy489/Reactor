package com.relax.reactor.service.gamelogic.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.relax.reactor.rng.RNG;
import com.relax.reactor.service.gamelogic.enumerated.PayTableType;
import com.relax.reactor.service.gamelogic.enumerated.Strategy;
import com.relax.reactor.service.gamelogic.enumerated.WildMultipliersAggregationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class SlotContext extends SpinHandlers implements Serializable {

    protected transient RNG rng;

    protected String gameName;
    protected Integer slotId;
    protected Integer version;
    protected Double sharpRtp;
    protected Integer stateNum;

    protected Map<Integer, String> stateDescriptor;
    protected Map<Integer, String> recursionLevelsDescriptor;

    protected Boolean hasChoice;

    protected List<Integer> gridDim;

    protected Integer bonusNum;
    protected Map<Integer, String> bonusNames;

    protected Map<String, List<Integer>> tileIds;
    protected Map<Integer, String> tileNames;

    protected List<ReelSet> reelSets;
    protected List<Integer> mainGameReelSetIndexes;
    protected List<Double> mainGameReelSetChances;

    protected Integer linesNum;
    protected List<List<Integer>> lineDefinitions;

    protected PayTableType payTableType;
    protected Map<Integer, TreeMap<Integer, Double>> payTable;

    protected Strategy strategy;
    protected Double minStake;
    protected Integer minMatch;

    protected Map<Integer, Double> wildMultipliers;
    protected Map<Integer, WildMultipliersAggregationType> wildMultipliersAggregations;

    protected Integer afterDecimalPrecision = 2;
}
