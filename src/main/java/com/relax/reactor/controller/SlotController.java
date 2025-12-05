package com.relax.reactor.controller;

import com.relax.reactor.service.SlotService;
import com.relax.reactor.service.gamelogic.dto.SettingsDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@AllArgsConstructor
public class SlotController {

    private SlotService slotService;

    @GetMapping("/settings")
    public ResponseEntity<SettingsDto> getSpinResult() throws IOException {
        return ResponseEntity.of(slotService.settings());
    }
}
