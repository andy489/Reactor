package com.relax.reactor.dto;

import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PredefinedSequenceResponse {
    private List<SlotGameDto> spinResults;
    private int totalRNGCallsMade;
    private int predefinedRNGConsumed;
    private int predefinedRNGRemaining;
    private boolean sequenceCompleted;
    private boolean sequenceExhausted;
    private int randomRNGUsed;
    private String error;

    public static PredefinedSequenceResponse error(String message) {
        PredefinedSequenceResponse response = new PredefinedSequenceResponse();
        response.setError(message);
        return response;
    }
}