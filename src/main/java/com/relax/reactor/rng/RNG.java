package com.relax.reactor.rng;

import lombok.Getter;
import org.apache.commons.rng.UniformRandomProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.relax.reactor.service.gamelogic.core.util.UtilConstants.EPSILON;

@Service
@Getter
public class RNG {

    private final UniformRandomProvider mersenneTwister;
    private final PredefinedSequenceRNG predefinedSequenceRNG;
    private boolean usePredefinedSequence = false;

    private int totalCallsMade = 0;
    private int randomCallsMade = 0;

    @Autowired
    public RNG(UniformRandomProvider mersenneTwister, PredefinedSequenceRNG predefinedSequenceRNG) {
        this.mersenneTwister = mersenneTwister;
        this.predefinedSequenceRNG = predefinedSequenceRNG;
    }

    public void enablePredefinedSequence(List<Object> sequence) {
        predefinedSequenceRNG.setSequence(sequence);
        usePredefinedSequence = true;
        totalCallsMade = 0;
        randomCallsMade = 0;
    }

    public void disablePredefinedSequence() {
        usePredefinedSequence = false;
        totalCallsMade = 0;
        randomCallsMade = 0;
        predefinedSequenceRNG.clearSequence();
    }

    public boolean isPredefinedSequenceEnabled() {
        return usePredefinedSequence;
    }

    public boolean hasMoreSequenceValues() {
        return usePredefinedSequence && predefinedSequenceRNG.hasMoreValues();
    }

    public int getWeightedIndex(List<Integer> items, List<Double> chances) {
        totalCallsMade++;

        if (usePredefinedSequence && predefinedSequenceRNG.hasMoreValues()) {
            Object result = predefinedSequenceRNG.getNext();

            if (!(result instanceof Integer)) {
                throw new IllegalStateException("Expected Integer result for getWeightedIndex, got: " +
                        result.getClass().getSimpleName() + " at position #" + predefinedSequenceRNG.getResultsConsumed());
            }

            int index = (Integer) result;

            if (index < 0 || index >= items.size()) {
                throw new IllegalStateException("Result index " + index + " out of range [0, " +
                        (items.size() - 1) + "] at position #" + predefinedSequenceRNG.getResultsConsumed());
            }

            return index;
        } else {
            randomCallsMade++;
            return getRandomWeightedIndex(items, chances);
        }
    }

    private int getRandomWeightedIndex(List<Integer> items, List<Double> chances) {
        if (items == null || chances == null) {
            throw new IllegalArgumentException("Both items and chances lists must not be null");
        }

        if (items.isEmpty() || chances.isEmpty()) {
            throw new IllegalArgumentException("Both items and chances lists must not be empty");
        }

        if (items.size() != chances.size()) {
            throw new IllegalArgumentException(String.format("Items size (%d) must match chances size (%d)",
                    items.size(), chances.size()));
        }

        // normalize chances if needed
        double total = chances.stream().mapToDouble(Double::doubleValue).sum();

        if (Math.abs(total - 1.0) > EPSILON) {
            final double finalTotal = total;
            chances = chances.stream().map(chance -> chance / finalTotal).toList();
        }

        double randomValue = mersenneTwister.nextDouble();
        double cumulativeProbability = 0.0;

        for (int i = 0; i < items.size(); i++) {
            cumulativeProbability += chances.get(i);
            if (randomValue < cumulativeProbability) {
                return i;
            }
        }

        return -1;
    }

    public int getUniformIndex(List<Integer> items) {
        totalCallsMade++;

        if (usePredefinedSequence && predefinedSequenceRNG.hasMoreValues()) {
            Object result = predefinedSequenceRNG.getNext();

            if (!(result instanceof Integer)) {
                throw new IllegalStateException("Expected Integer result for getUniformIndex, got: " +
                        result.getClass().getSimpleName() + " at position #" + predefinedSequenceRNG.getResultsConsumed());
            }

            int index = (Integer) result;

            if (index < 0 || index >= items.size()) {
                throw new IllegalStateException("Result index " + index + " out of range [0, " +
                        (items.size() - 1) + "] at position #" + predefinedSequenceRNG.getResultsConsumed());
            }

            return index;
        } else {
            randomCallsMade++;
            return getRandomUniformIndex(items);
        }
    }

    private int getRandomUniformIndex(List<Integer> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty");
        }

        return mersenneTwister.nextInt(items.size());
    }

    public int getUniformIndex(int start, int end) {
        totalCallsMade++;

        if (usePredefinedSequence && predefinedSequenceRNG.hasMoreValues()) {
            Object result = predefinedSequenceRNG.getNext();

            if (!(result instanceof Integer)) {
                throw new IllegalStateException("Expected Integer result for getUniformIndex(range), got: " +
                        result.getClass().getSimpleName() + " at position #" + predefinedSequenceRNG.getResultsConsumed());
            }

            int index = (Integer) result;

            if (index < start || index > end) {
                throw new IllegalStateException("Result index " + index + " out of range [" +
                        start + ", " + end + "] at position #" + predefinedSequenceRNG.getResultsConsumed());
            }

            return index;
        } else {
            randomCallsMade++;
            return getRandomUniformIndex(start, end);
        }
    }

    private int getRandomUniformIndex(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("Start must be less than or equal to end");
        }
        if (start == end) {
            return start;
        }

        int range = end - start + 1;
        return start + mersenneTwister.nextInt(range);
    }

    public double nextDouble() {
        totalCallsMade++;

        if (usePredefinedSequence && predefinedSequenceRNG.hasMoreValues()) {
            Object result = predefinedSequenceRNG.getNext();

            if (!(result instanceof Double || result instanceof Integer)) {
                throw new IllegalStateException("Expected Double result for nextDouble, got: " +
                        result.getClass().getSimpleName() + " at position #" + predefinedSequenceRNG.getResultsConsumed());
            }

            if (result instanceof Integer) {
                return ((Integer) result).doubleValue();
            }

            double value = (Double) result;

            if (value < 0.0 || value >= 1.0) {
                throw new IllegalStateException("Result value " + value + " out of range [0.0, 1.0) at position #" +
                        predefinedSequenceRNG.getResultsConsumed());
            }

            return value;
        } else {
            randomCallsMade++;
            return getRandomDouble();
        }
    }

    private double getRandomDouble() {
        return mersenneTwister.nextDouble();
    }

    public int getSequenceResultsConsumed() {
        return predefinedSequenceRNG.getResultsConsumed();
    }

    public boolean isSequenceExhausted() {
        return usePredefinedSequence && !predefinedSequenceRNG.hasMoreValues();
    }

    public int getSequenceCallsMade() {
        return totalCallsMade - randomCallsMade;
    }
}