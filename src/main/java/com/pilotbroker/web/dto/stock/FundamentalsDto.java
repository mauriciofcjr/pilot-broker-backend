package com.pilotbroker.web.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class FundamentalsDto {
    // Map<String, Object>: estrutura variável da FMP, passada diretamente ao frontend sem mapeamento tipado
    private List<Map<String, Object>> incomeStatement;
    private List<Map<String, Object>> balanceSheet;
    private List<Map<String, Object>> cashFlow;
    private Map<String, Object> ratios;
    private Map<String, Object> scores;
}
