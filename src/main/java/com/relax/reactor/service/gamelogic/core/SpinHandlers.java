package com.relax.reactor.service.gamelogic.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
public class SpinHandlers {

    protected List<SlotSpinProcessor> spinProcessors;
    protected List<SlotSpinProcessor> postSpinProcessors;

    public SpinHandlers() {
        this.spinProcessors = new ArrayList<>();
        this.postSpinProcessors = new ArrayList<>();
    }
}
