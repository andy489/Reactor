package com.relax.reactor.service.gamelogic.dto;

import com.relax.reactor.service.gamelogic.core.SlotGameStickyData;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class SlotGameDto extends GameDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer reelsSetIndex;
    private List<Integer> stopPositions;

    private List<List<Integer>> screen;
    private List<Integer> screenSize;

    private Integer spinNum;
    private Integer totalSpins;

    private SlotGameStickyData slotGameStickyData;

    private List<Integer> preSpinStates;
    private List<Integer> postSpinStates;

    private Double stakeMultiplier = 1.0;
    private Integer afterDecimalPrecision = 2;

    private Integer recursionLevel;
    private Double cumulativeWinAmount;

    private List<BaseDto> payoutData;
}
