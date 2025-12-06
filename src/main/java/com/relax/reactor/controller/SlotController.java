package com.relax.reactor.controller;

import com.relax.reactor.service.SlotService;
import com.relax.reactor.service.gamelogic.dto.SettingsDto;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.validation.MultipleOfMinStake;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/slot")
@Validated
@AllArgsConstructor
public class SlotController {

    private SlotService slotService;

    @GetMapping("/settings")
    public ResponseEntity<SettingsDto> settings(){
        return ResponseEntity.of(slotService.settings());
    }

    @GetMapping("/spin")
    public ResponseEntity<SlotGameDto> spin(
            @NotNull(message = "Stake is required")
            @Positive(message = "Stake must be positive")
            @MultipleOfMinStake(minStake = 0.10, message = "Stake must be in multiples of $0.10")
            Double stake,

            @RequestParam(value = "states", required = false)
            List<Integer> states
    ) {
        return ResponseEntity.of(slotService.spin(stake, states));
    }
}
