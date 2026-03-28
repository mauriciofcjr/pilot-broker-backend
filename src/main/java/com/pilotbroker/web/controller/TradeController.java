package com.pilotbroker.web.controller;

import com.pilotbroker.service.OrderService;
import com.pilotbroker.web.dto.trade.OrderListItemDto;
import com.pilotbroker.web.dto.trade.OrderRequestDto;
import com.pilotbroker.web.dto.trade.OrderResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Trade", description = "Submissão e histórico de ordens")
@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TradeController {

    private final OrderService orderService;

    @Operation(summary = "Submete uma ordem de compra ou venda")
    @PostMapping("/order")
    public ResponseEntity<OrderResponseDto> criarOrdem(
            @RequestBody @Valid OrderRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(orderService.criarOrdem(userDetails.getUsername(), dto));
    }

    @Operation(summary = "Lista ordens do usuário autenticado")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderListItemDto>> listarOrdens(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.listarOrdens(userDetails.getUsername()));
    }
}
