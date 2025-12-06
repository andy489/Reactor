package com.relax.reactor.service.gamelogic.dto.payout;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
public class TestProcessorsAreActivatedDto extends BaseDto {
    private String custom;

    public TestProcessorsAreActivatedDto() {}
}
