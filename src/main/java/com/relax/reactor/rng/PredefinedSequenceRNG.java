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

    public void setSequence(List<Object> sequence) {
        this.originalSequence = new ArrayList<>(sequence);
        this.sequenceIterator = originalSequence.iterator();
        this.resultsConsumed = 0;
    }

    public Object getNext() {
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
    }

    public int getSequenceLength() {
        return originalSequence != null ? originalSequence.size() : 0;
    }
}