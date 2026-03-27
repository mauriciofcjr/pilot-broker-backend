package com.pilotbroker.web.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class GovernanceDto {
    private List<Map<String, Object>> executives;
    private List<Map<String, Object>> dividends;
    private List<Map<String, Object>> earnings;
}
