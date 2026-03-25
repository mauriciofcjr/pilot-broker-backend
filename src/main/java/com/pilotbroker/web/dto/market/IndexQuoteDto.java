package com.pilotbroker.web.dto.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IndexQuoteDto {
    private String symbol;
    private String name;
    private Double price;
    private Double change;
}
