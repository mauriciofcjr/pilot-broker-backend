package com.pilotbroker.web.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuoteDto {
    private Double price;
    private Double change;
    private Double changePercent;
    private Long volume;
}
