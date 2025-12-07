package com.relax.reactor.exception;

public class PendingGambleException extends RuntimeException {

    private final Double stashedCumulativeWinAmount;

    public PendingGambleException(String message, Double stashedCumulativeWinAmount) {
        super(message);
        this.stashedCumulativeWinAmount = stashedCumulativeWinAmount;
    }

    public Double getStashedCumulativeWinAmount() {
        return stashedCumulativeWinAmount;
    }
}