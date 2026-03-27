package com.pilotbroker.web.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScreenerResponseDto {
    private String symbol;
    private String companyName;
    private Double price;
    private Double changePercent;   // obrigatório — PriceBadge usa percentual
    private String sector;
}
