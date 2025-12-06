package com.relax.reactor.service.gamelogic.dto;

import com.relax.reactor.service.gamelogic.core.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class SlotGameDto extends BaseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer reelsSetIndex;
    private List<Integer> reelStopPositions;

    private List<List<Integer>> grid;
    private List<Integer> gridDim;

    private Integer spinNum;
    private Integer totalSpins;

    private SlotGameStickyData slotGameStickyData;

    private List<Integer> preSpinStates;
    private List<Integer> postSpinStates;

    private Double stakeMultiplier = 1.0;

    private Integer recursionLevel;
    private Double cumulativeWinAmount;

    private List<BaseDto> payoutData;

    public SlotGameDto() {
    }
}
