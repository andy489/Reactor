package com.relax.reactor.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;
import com.relax.reactor.service.gamelogic.dto.payout.SlotContactDto;
import com.relax.reactor.service.gamelogic.dto.payout.SlotExplodeFallDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class BaseDtoDeserializer extends StdDeserializer<BaseDto> {

    public BaseDtoDeserializer() {
        this(null);
    }

    public BaseDtoDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public BaseDto deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        log.debug("Deserializing BaseDto node with fields: {}", node.fieldNames());

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();

        // Check for fields that indicate specific types
        if (node.has("floatIds") && node.has("payoutSymbols")) {
            return mapper.treeToValue(node, SlotContactDto.class);
        } else if (node.has("holdReels") && node.has("explodeReels")) {
            return mapper.treeToValue(node, SlotExplodeFallDto.class);
        } else if (node.has("grid") && node.has("reelStopPositions")) {
            return mapper.treeToValue(node, SlotGameDto.class);
        }

        log.warn("Could not determine concrete type for BaseDto, using default");
        BaseDto baseDto = new BaseDto();

        if (node.has("winAmount")) {
            baseDto.setWinAmount(node.get("winAmount").asDouble());
        }

        return baseDto;
    }
}