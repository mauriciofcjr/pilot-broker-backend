package com.pilotbroker.web.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderListItemDto {
    private Long    id;
    private String  symbol;
    private String  tipo;
    private Integer quantidade;
    private Double  preco;
    private String  status;
    private String  dataCriacao;  // ISO 8601 "YYYY-MM-DDThh:mm:ss"
}
