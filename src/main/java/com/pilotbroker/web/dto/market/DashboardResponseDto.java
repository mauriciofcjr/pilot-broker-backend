package com.pilotbroker.web.dto.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardResponseDto {
    private List<IndexQuoteDto> indices;
    private List<ActivelyTradingDto> activelyTrading;
    private List<EconomicEventDto> economicCalendar;
}
