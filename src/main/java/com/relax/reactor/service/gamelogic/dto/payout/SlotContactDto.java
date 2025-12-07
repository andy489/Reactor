package com.relax.reactor.service.gamelogic.dto.payout;

import com.relax.reactor.service.gamelogic.core.data.GridPosition;
import com.relax.reactor.service.gamelogic.enumerated.MatchType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class SlotContactDto extends BaseDto {

    private List<Integer> floatIds;
    private List<Integer> payoutSymbols;

    private List<Integer> contactSizes;
    private List<GridPosition> contactStarts; // most-left & most-up pos

    private MatchType matchType;

    private List<List<Integer>> contact1DimPositions; // count from 0 row by row
    private List<List<Integer>> contact1DimSymbols;

    private List<List<List<Integer>>> contact2DimPositions; // counting from 0 per every reel
    private List<List<List<Integer>>> contact2DimSymbols;

    private List<Double> localMultipliers;
    private Double globalMultiplier;

    private List<Double> payouts;
    private List<Double> multipliedPayouts;

    private Double stashedTotalWinAmount;
}
