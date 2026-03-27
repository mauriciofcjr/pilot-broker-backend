package com.pilotbroker.web.controller;

import com.pilotbroker.service.StockService;
import com.pilotbroker.web.dto.stock.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Stocks", description = "Busca, screener e detalhes de ativos")
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StockController {

    private final StockService stockService;

    @Operation(summary = "Busca ativos por símbolo ou nome")
    @GetMapping("/search")
    public ResponseEntity<List<SearchResultDto>> search(
            @RequestParam String query) {
        return ResponseEntity.ok(stockService.searchStocks(query));
    }

    @Operation(summary = "Screener com filtros avançados (sector, marketCap, price, etc.)")
    @GetMapping("/screener")
    public ResponseEntity<List<ScreenerResponseDto>> screener(
            @RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(stockService.getScreener(params != null ? params : Map.of()));
    }

    @Operation(summary = "Detalhes do ativo: profile, quote, candlesticks e peers (paralelo)")
    @GetMapping("/{symbol}")
    public ResponseEntity<StockDetailDto> getStockDetail(
            @PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockDetail(symbol.toUpperCase()));
    }

    @Operation(summary = "Análise fundamentalista: DRE, Balanço, Fluxo de Caixa, Indicadores, Scores")
    @GetMapping("/{symbol}/fundamentals")
    public ResponseEntity<FundamentalsDto> getFundamentals(
            @PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getFundamentals(symbol.toUpperCase()));
    }

    @Operation(summary = "Governança e proventos: Diretoria, Dividendos, Earnings")
    @GetMapping("/{symbol}/governance")
    public ResponseEntity<GovernanceDto> getGovernance(
            @PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getGovernance(symbol.toUpperCase()));
    }
}
