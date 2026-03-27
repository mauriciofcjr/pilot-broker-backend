package com.pilotbroker.web.dto.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActivelyTradingDto {
    private String symbol;
    private String name;
    private Double price;
    private Double change;
    private Long volume;
}
