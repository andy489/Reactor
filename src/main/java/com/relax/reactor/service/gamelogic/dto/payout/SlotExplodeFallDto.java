package com.relax.reactor.service.gamelogic.dto.payout;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
public class SlotExplodeFallDto extends BaseDto {

    private List<Integer> holdReels;
    private List<Integer> holdPositions;
    private List<Integer> holdSymbolIds;

    private List<Integer> explodeReels;
    private List<Integer> explodePositions;
    private List<Integer> explodeSymbolIds;

    private List<Integer> fallReels;
    private List<Integer> fallStarts;
    private List<Integer> fallStops;
    private List<Integer> fallSymbolIds;

    public SlotExplodeFallDto() {
        holdReels = new ArrayList<>();
        holdPositions = new ArrayList<>();
        holdSymbolIds = new ArrayList<>();

        explodeReels = new ArrayList<>();
        explodePositions = new ArrayList<>();
        explodeSymbolIds = new ArrayList<>();

        fallReels = new ArrayList<>();
        fallStarts = new ArrayList<>();
        fallStops = new ArrayList<>();
        fallSymbolIds = new ArrayList<>();

        this.setWinAmount(0.0d);
    }
}
