package com.pilotbroker.web.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CandlestickDto {
    private String date;   // formato "YYYY-MM-DD HH:mm" — padrão FMP, acordado com frontend
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
}
