package com.relax.reactor.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.relax.reactor.config.Util.EPSILON;

@Service
public class RNG {

    private final UniformRandomProvider mersenneTwister;

    public RNG(UniformRandomProvider mersenneTwister) {
        this.mersenneTwister = mersenneTwister;
    }

    public UniformRandomProvider getMersenneTwister() {
        return mersenneTwister;
    }

    public Integer getWeightedInt(List<Integer> items, List<Double> chances) {
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
        final List<Double> normalizedChances;

        if (Math.abs(total - 1.0) > EPSILON) {
            final double finalTotal = total;
            normalizedChances = chances.stream().map(chance -> chance / finalTotal).toList();
        } else {
            normalizedChances = chances;
        }

        double randomValue = mersenneTwister.nextDouble();
        double cumulativeProbability = 0.0;

        for (int i = 0; i < items.size(); i++) {
            cumulativeProbability += normalizedChances.get(i);
            if (randomValue < cumulativeProbability) {
                return items.get(i);
            }
        }

        return items.get(items.size() - 1);
    }

    public Integer getUniform(List<Integer> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty");
        }

        int randomIndex = mersenneTwister.nextInt(items.size());
        return items.get(randomIndex);
    }
}
