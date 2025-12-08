package com.relax.reactor.controller;

import com.relax.reactor.service.SlotService;
import com.relax.reactor.service.gamelogic.dto.SettingsDto;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.SlotStatsDto;
import com.relax.reactor.validation.MaxBetAmount;
import com.relax.reactor.validation.MultipleOfMinStake;
import com.relax.reactor.validation.ValidGambleChoice;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/slot")
@Validated
@AllArgsConstructor
public class SlotController {

    private SlotService slotService;

    @GetMapping("/settings")
    public ResponseEntity<SettingsDto> settings() {
        return ResponseEntity.of(slotService.settings());
    }

    @GetMapping("/spin")
    public ResponseEntity<List<SlotGameDto>> spin(
            @NotNull(message = "Stake is required")
            @Positive(message = "Stake must be positive")
            @MultipleOfMinStake(minStake = 0.10, message = "Stake must be in multiples of $0.10")
            @MaxBetAmount(maxAmount = 100.00)
            Double stake,

            @RequestParam(value = "states", required = false)
            List<Integer> states
    ) {
        return ResponseEntity.of(slotService.spin(stake, states));
    }

    @GetMapping("/gamble")
    public ResponseEntity<List<SlotGameDto>> gamble(@ValidGambleChoice Integer choice) {
        return ResponseEntity.of(slotService.gamble(choice));
    }

    @GetMapping("/stats")
    public ResponseEntity<SlotStatsDto> stats(@RequestParam(defaultValue = "10000")
                                              @Min(value = 1, message = "Spins must be at least 1")
                                              @Max(value = 3000000, message = "Spins cannot exceed 3,000,000")
                                              Integer spins,

                                              @RequestParam(defaultValue = "0.10")
                                              @NotNull(message = "Stake is required")
                                              @Positive(message = "Stake must be positive")
                                              @MultipleOfMinStake(minStake = 0.10, message = "Stake must be in multiples of $0.10")
                                              @MaxBetAmount(maxAmount = 100.00)
                                              Double stake) {

        return ResponseEntity.of(slotService.runSimulation(spins, stake));
    }
}
