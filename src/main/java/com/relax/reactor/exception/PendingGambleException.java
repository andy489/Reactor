package com.relax.reactor.exception;

import lombok.Getter;

@Getter
public class PendingGambleException extends RuntimeException {

    private final Double stashedCumulativeWinAmount;

    public PendingGambleException(String message, Double stashedCumulativeWinAmount) {
        super(message);
        this.stashedCumulativeWinAmount = stashedCumulativeWinAmount;
    }
}