package com.relax.reactor.controller;

import com.relax.reactor.dto.PredefinedSequenceResponse;
import com.relax.reactor.service.ParameterParsingService;
import com.relax.reactor.service.SlotService;
import com.relax.reactor.service.gamelogic.dto.SettingsDto;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.SlotStatsDto;
import com.relax.reactor.service.gamelogic.enumerated.AvalancheMode;
import com.relax.reactor.validation.MaxBetAmount;
import com.relax.reactor.validation.MultipleOfMinStake;
import com.relax.reactor.validation.ValidGambleChoice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
@Tag(name = "Slot Game", description = "Slot machine game endpoints for spinning, gambling, and simulations")
public class SlotController {

    private final SlotService slotService;
    private final ParameterParsingService parameterParsingService;

    @Autowired
    public SlotController(ParameterParsingService parameterParsingService, SlotService slotService) {
        this.parameterParsingService = parameterParsingService;
        this.slotService = slotService;
    }

    @Operation(
            summary = "Get game settings",
            description = "Retrieves the configuration and settings for the slot game including min/max bets, symbols, paylines, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Game settings retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SettingsDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game settings not found"
            )
    })
    @GetMapping("/settings")
    public ResponseEntity<SettingsDto> settings() {
        return ResponseEntity.of(slotService.settings());
    }

    @Operation(
            summary = "Spin the slot reels",
            description = "Performs a spin with the specified stake. Optionally accepts predefined game states for testing."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Spin completed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SlotGameDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid stake amount or validation failed"
            )
    })
    @GetMapping("/spin")
    public ResponseEntity<List<SlotGameDto>> spin(
            @Parameter(
                    description = "Bet amount per spin. Must be positive, in multiples of $0.10, and not exceed $100.00",
                    required = true,
                    example = "1.00"
            )
            @NotNull(message = "Stake is required")
            @Positive(message = "Stake must be positive")
            @MultipleOfMinStake(minStake = 0.10, message = "Stake must be in multiples of $0.10")
            @MaxBetAmount(maxAmount = 100.00)
            Double stake,

            @Parameter(
                    description = "Optional predefined game states for deterministic testing",
                    example = "[1, 2, 3]"
            )
            @RequestParam(value = "states", required = false)
            List<Integer> states
    ) {
        return ResponseEntity.of(slotService.spin(stake, states));
    }

    @Operation(
            summary = "Gamble winnings",
            description = "Attempt to double winnings through a gamble feature (e.g., guess the card color)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Gamble completed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SlotGameDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid gamble choice or no active gamble available"
            )
    })
    @GetMapping("/gamble")
    public ResponseEntity<List<SlotGameDto>> gamble(
            @Parameter(
                    description = "Gamble choice (e.g., 0=cash out, 1=red, 2=black, etc.)",
                    required = true,
                    example = "1"
            )
            @ValidGambleChoice Integer choice) {
        return ResponseEntity.of(slotService.gamble(choice));
    }

    @Operation(
            summary = "Run statistical simulation",
            description = "Simulates a large number of spins to calculate RTP (Return to Player), volatility, and other statistics."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Simulation completed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SlotStatsDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameters (spins count out of range or invalid stake)"
            )
    })
    @GetMapping("/stats")
    public ResponseEntity<SlotStatsDto> stats(
            @Parameter(
                    description = "Number of spins to simulate (1 to 3,000,000)",
                    required = true,
                    example = "10000"
            )
            @RequestParam(defaultValue = "10000")
            @Min(value = 1, message = "Spins must be at least 1")
            @Max(value = 3000000, message = "Spins cannot exceed 3,000,000")
            Integer spins,

            @Parameter(
                    description = "Bet amount per spin. Must be positive, in multiples of $0.10, and not exceed $100.00",
                    required = true,
                    example = "0.10"
            )
            @RequestParam(defaultValue = "0.10")
            @NotNull(message = "Stake is required")
            @Positive(message = "Stake must be positive")
            @MultipleOfMinStake(minStake = 0.10, message = "Stake must be in multiples of $0.10")
            @MaxBetAmount(maxAmount = 100.00)
            Double stake) {

        return ResponseEntity.of(slotService.runSimulation(spins, stake));
    }

    @Operation(
            summary = "Execute predefined sequence",
            description = "Run a specific sequence of outcomes for testing and debugging purposes. Useful for QA and automated testing."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sequence executed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PredefinedSequenceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid sequence format, stake, or states parameter"
            )
    })
    @GetMapping("/spin/sequence")
    public ResponseEntity<PredefinedSequenceResponse> spinWithSequence(
            @Parameter(
                    description = "Bet amount per spin. Must be positive, in multiples of $0.10, and not exceed $100.00",
                    required = true,
                    example = "1.00"
            )
            @NotNull(message = "Stake is required")
            @Positive(message = "Stake must be positive")
            @MultipleOfMinStake(minStake = 0.10, message = "Stake must be in multiples of $0.10")
            @MaxBetAmount(maxAmount = 100.00)
            @RequestParam Double stake,

            @Parameter(
                    description = "Comma-separated sequence of predefined outcomes. Supported values: win, loss, bonus, free_spins, jackpot, etc.",
                    required = true,
                    example = "win,loss,bonus,free_spins"
            )
            @NotNull(message = "Sequence is required")
            @RequestParam String sequence,

            @Parameter(
                    description = "Optional comma-separated game states corresponding to the sequence",
                    example = "1,2,3,4"
            )
            @RequestParam(value = "states", required = false)
            String states) {

        try {
            List<Object> parsedSequence = parameterParsingService.parseSequence(sequence);
            List<Integer> parsedStates = parameterParsingService.parseStates(states);

            PredefinedSequenceResponse response = slotService.spinWithPredefinedSequence(
                    stake,
                    parsedSequence,
                    parsedStates
            );

            if (response.getError() != null) {
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    PredefinedSequenceResponse.error("Invalid parameter: " + e.getMessage())
            );
        }
    }
}