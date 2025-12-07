package com.relax.reactor.service.gamelogic.dto;

import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
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

    public List<SlotGameDto> linearizeWithDepth() {
        List<SlotGameDto> result = new ArrayList<>();

        collectWithDepth(this, result, 1);

        int totalSpins = result.size();
        for (int i = 1; i <= totalSpins; i++) {
            result.get(i - 1).setTotalSpins(totalSpins);
        }

        calculateCumulativeWinsBackwards(result);

        return result;
    }

    private void collectWithDepth(SlotGameDto dto, List<SlotGameDto> result, int depth) {
        if (dto == null) {
            return;
        }

        dto.setSpinNum(depth);
        result.add(dto);

        if (dto.getPayoutData() != null) {
            for (BaseDto baseDto : dto.getPayoutData()) {
                if (baseDto instanceof SlotGameDto) {
                    collectWithDepth((SlotGameDto) baseDto, result, depth + 1);
                }
            }
        }
    }

    private void calculateCumulativeWinsBackwards(List<SlotGameDto> linearizedSlotGameDto) {

        double cumulativeWinAmount = 0.0d;
        for (SlotGameDto currDto : linearizedSlotGameDto) {
            Double currDtoWinAmount = currDto.getWinAmount();
            cumulativeWinAmount += currDtoWinAmount != null ? currDtoWinAmount : 0.0d;

            currDto.setCumulativeWinAmount(cumulativeWinAmount);
        }
    }
}
