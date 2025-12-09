package com.relax.reactor.service.gamelogic.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder({
        "rtp", "maxStakeMultiplier", "hitRate", "winRate", "stake", "median", "variance", "standardDeviation",
        "winRate", "hitRate", "randomVariable", "timeElapsed", "totalSpinsCount"
})
public class SlotStatsDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String rtp; // equivalent to RTP

    private Double maxStakeMultiplier;

    private Double hitRate; // win > 0
    private Double winRate; // win > stake

    private Double stake;

    private Double median;

    private Double variance;
    private Double standardDeviation;

    private String randomVariable = "STAKE_MULTIPLIER";
    private String timeElapsed;
    private String totalSpinsCount;
}
