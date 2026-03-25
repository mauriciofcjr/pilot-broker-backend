package com.pilotbroker.web.controller;

import com.pilotbroker.service.MarketService;
import com.pilotbroker.web.dto.market.DashboardResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Market", description = "Dados de mercado para o dashboard")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MarketController {

    private final MarketService marketService;

    @Operation(summary = "Dados do dashboard: índices, ativos e calendário econômico")
    @GetMapping
    public ResponseEntity<DashboardResponseDto> getDashboard() {
        return ResponseEntity.ok(marketService.getDashboard());
    }
}
