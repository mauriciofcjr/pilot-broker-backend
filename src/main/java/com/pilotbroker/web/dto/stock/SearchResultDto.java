package com.pilotbroker.web.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchResultDto {
    private String symbol;
    private String name;
    private String currency;
    private String stockExchange;
}
