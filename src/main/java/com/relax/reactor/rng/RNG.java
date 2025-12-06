package com.relax.reactor.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.relax.reactor.config.UtilConstants.EPSILON;

@Service
public class RNG {

    private final UniformRandomProvider mersenneTwister;

    @Autowired
    public RNG(UniformRandomProvider mersenneTwister) {
        this.mersenneTwister = mersenneTwister;
    }

    public int getWeightedIndex(List<Integer> items, List<Double> chances) {
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

        // Normalize chances if needed
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
                return i; // Return the index instead of the item
            }
        }

        return -1;
    }

    public int getUniformIndex(List<Integer> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty");
        }

        // Return a random index between 0 and items.size()-1
        return mersenneTwister.nextInt(items.size());
    }

    public int getUniformIndex(int start, int end) { // end is inclusive
        if (start > end) {
            throw new IllegalArgumentException("Start must be less than or equal to end");
        }
        if (start == end) {
            return start;
        }

        int range = end - start + 1;
        return start + mersenneTwister.nextInt(range);
    }
}
