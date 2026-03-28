package com.pilotbroker.web.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderResponseDto {
    private Long   orderId;
    private String status;
    private String message;
}
