package com.relax.reactor.service.gamelogic.dto.payout;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
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
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = false,
        defaultImpl = SlotContactDto.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SlotContactDto.class, name = "contact"),
        @JsonSubTypes.Type(value = SlotExplodeFallDto.class, name = "explode_fall"),
        @JsonSubTypes.Type(value = SlotGameDto.class, name = "slot_game")
})
public class BaseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected Double winAmount;

    public BaseDto() {
        this.winAmount = 0.0d;
    }
}
