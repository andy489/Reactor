package com.relax.reactor.service.gamelogic.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public abstract class BaseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 489L;

    protected String className;
    protected Double winAmount;

    public BaseDto() {
        this.className = getClass().getName();
    }
}
