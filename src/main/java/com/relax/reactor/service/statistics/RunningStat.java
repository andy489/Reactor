package com.relax.reactor.service.statistics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.PriorityQueue;

import static java.lang.Math.sqrt;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class RunningStat {

    private static final Double Z_SCORE = 0.96;

    private Integer n;

    private Double maxStakeMultiplier;

    private Integer hitsCount;
    private Integer winsCount;

    private Double mOldMean;
    private Double mNewMean;

    private Double mOldSd;
    private Double mNewSd;

    private PriorityQueue<Double> minHeap;
    private PriorityQueue<Double> maxHeap;

    private Boolean median;

    public RunningStat(boolean median) {
        hitsCount = 0;
        winsCount = 0;

        maxStakeMultiplier = 0.0;

        n = 0;
        this.median = median;

        if (median) {
            minHeap = new PriorityQueue<>(Double::compareTo);
            maxHeap = new PriorityQueue<>((a, b) -> -a.compareTo(b));
        }
    }

    public void clear() {
        n = 0;
        if (median) {
            minHeap = new PriorityQueue<>(Double::compareTo);
            maxHeap = new PriorityQueue<>((a, b) -> -a.compareTo(b));
        }
    }

    public void push(double x) {
        n++;

        if (x > maxStakeMultiplier) {
            maxStakeMultiplier = x;
        }

        if (median) {
            maxHeap.offer(x);

            if (maxHeap.size() > minHeap.size() + 1) {
                Double movedValue = maxHeap.poll();
                if (movedValue != null) {
                    minHeap.offer(movedValue);
                }
            }

            if (!minHeap.isEmpty() && !maxHeap.isEmpty() && maxHeap.peek() > minHeap.peek()) {
                Double maxRoot = maxHeap.poll();
                Double minRoot = minHeap.poll();

                if (maxRoot != null && minRoot != null) {
                    maxHeap.offer(minRoot);
                    minHeap.offer(maxRoot);
                } else {
                    if (maxRoot != null) maxHeap.offer(maxRoot);
                    if (minRoot != null) minHeap.offer(minRoot);
                }
            }
        }

        if (n == 1) {
            mOldMean = x;
            mNewMean = x;
            mOldSd = 0.0d;
        } else {
            mNewMean = mOldMean + (x - mOldMean) / n;
            mNewSd = mOldSd + (x - mOldMean) * (x - mNewMean);

            mOldMean = mNewMean;
            mOldSd = mNewSd;
        }

        if (x > 1.0) {
            winsCount++;
        }

        if (x > 0.0) {
            hitsCount++;
        }
    }

    public double median() {

        if (maxHeap.size() > minHeap.size()) {
            return maxHeap.peek();
        } else {
            if (!maxHeap.isEmpty()) {
                return (maxHeap.peek() + minHeap.peek()) / 2.0d;
            } else {
                return 0.0d;
            }
        }
    }

    public double mean() {
        return (n > 0) ? mNewMean : 0.0d;
    }

    public double variance() {
        return ((n > 1) ? mNewSd / (n - 1) : 0.0d);
    }

    public double standardDeviation() {
        return sqrt(variance());
    }

    public double hitRate() {
        return (double) hitsCount / (double) n;
    }

    public double winRate() {
        return (double) winsCount / (double) n;
    }
}
