package com.relax.reactor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class PredefinedSequenceRequest {
    @NotNull(message = "Stake is required")
    private Double stake;
    
    @Size(min = 1, message = "Sequence must contain at least one value")
    @NotNull(message = "Sequence is required")
    private List<Object> sequence;

    private List<Integer> states;
}