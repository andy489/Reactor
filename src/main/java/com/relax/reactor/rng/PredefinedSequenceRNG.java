package com.relax.reactor.rng;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Getter
public class PredefinedSequenceRNG {

    private Iterator<Object> sequenceIterator;
    private List<Object> originalSequence;
    private int resultsConsumed = 0;
    private String expectedMethod = null; // Track which method expects the next value

    public void setSequence(List<Object> sequence) {
        this.originalSequence = new ArrayList<>(sequence);
        this.sequenceIterator = originalSequence.iterator();
        this.resultsConsumed = 0;
        this.expectedMethod = null;
    }

    public Object getNext(String methodName) {
        this.expectedMethod = methodName;
        if (sequenceIterator != null && sequenceIterator.hasNext()) {
            resultsConsumed++;
            return sequenceIterator.next();
        }
        return null;
    }

    public boolean hasMoreValues() {
        return sequenceIterator != null && sequenceIterator.hasNext();
    }

    public void clearSequence() {
        this.sequenceIterator = null;
        this.originalSequence = null;
        this.resultsConsumed = 0;
        this.expectedMethod = null;
    }

    public int getSequenceLength() {
        return originalSequence != null ? originalSequence.size() : 0;
    }

    public String getExpectedMethod() {
        return expectedMethod;
    }
}