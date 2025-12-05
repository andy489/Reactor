package com.relax.reactor.controller;

import com.relax.reactor.service.SlotService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SlotController {

    private SlotService slotService;

    @GetMapping("/spin")
    public String slot(){
        return "Hello World";
    }
}
