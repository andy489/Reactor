package com.relax.reactor.service.gamelogic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.relax.reactor.service.gamelogic.core.data.SlotGameStickyData;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@JsonPropertyOrder({"_links",
        "stake", "userChoice",
        "cumulativeWinAmount", "stashedCumulativeWinAmountBeforeGambleChoice",
        "accumulatedWinAmount", "winAmount",
        "gambleMultiplier",
        "spinNum", "totalSpins",
        "reelsSetIndex", "reelStopPositions",
        "gridDim", "grid",
        "slotGameStickyData", "preSpinStates", "postSpinStates",
        "stakeMultiplier", "recursionLevel", "payoutData"})
@ToString(callSuper = true)
public class SlotGameDto extends BaseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Double stake;

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

    private Double accumulatedWinAmount;
    private Double cumulativeWinAmount;

    private List<BaseDto> payoutData;

    private Double stashedCumulativeWinAmountBeforeGambleChoice;
    private Integer gambleMultiplier = -1; // -1 (no choice made); 0 (gamble and lost); 1 (no gamble); 2 (gamble and won)

    @JsonProperty("_links")
    private Map<String, String> links;

    private Integer userChoice; // user's gamble choice: 1 for COLLECT, 2 for GAMBLE

    public SlotGameDto() {
    }

    public List<SlotGameDto> linearize() {
        List<SlotGameDto> result = new ArrayList<>();

        dfs(this, result);

        return result;
    }

    private void dfs(SlotGameDto dto, List<SlotGameDto> result) {
        if (dto == null) {
            return;
        }

        result.add(dto);

        if (dto.getPayoutData() != null) {
            for (BaseDto baseDto : dto.getPayoutData()) {
                if (baseDto instanceof SlotGameDto) {
                    dfs((SlotGameDto) baseDto, result);
                }
            }
        }
    }
}
