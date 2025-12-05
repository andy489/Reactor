package com.relax.reactor.service.gamelogic.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReelSet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String setName;

    private List<Double> recursionChances;

    private List<List<Integer>> reelSet;

    public ReelSet(ReelSet other) {
        if (other == null) {
            throw new IllegalArgumentException("Source ReelSet cannot be null");
        }

        this.setName = other.setName;

        // Deep copy recursionChances list
        if (other.recursionChances != null) {
            this.recursionChances = new ArrayList<>(other.recursionChances);
        } else {
            this.recursionChances = null;
        }

        // Deep copy nested reelSet (List<List<Integer>>)
        if (other.reelSet != null) {
            this.reelSet = new ArrayList<>();
            for (List<Integer> innerList : other.reelSet) {
                if (innerList != null) {
                    this.reelSet.add(new ArrayList<>(innerList));
                } else {
                    this.reelSet.add(null);
                }
            }
        } else {
            this.reelSet = null;
        }
    }
}
