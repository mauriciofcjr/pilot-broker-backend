package com.pilotbroker.web.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StockDetailDto {
    private ProfileDto profile;
    private QuoteDto quote;
    private List<CandlestickDto> candlesticks;
    private List<String> peers;
}
