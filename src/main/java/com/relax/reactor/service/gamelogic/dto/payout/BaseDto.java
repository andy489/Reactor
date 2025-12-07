package com.relax.reactor.service.gamelogic.dto.payout;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY
)
public class BaseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected Double winAmount;

    public BaseDto() {
        this.winAmount = 0.0d;
    }
}
