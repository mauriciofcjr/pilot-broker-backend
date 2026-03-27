package com.pilotbroker.web.dto.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EconomicEventDto {
    private String event;
    private String date;
    private String impact;
}
