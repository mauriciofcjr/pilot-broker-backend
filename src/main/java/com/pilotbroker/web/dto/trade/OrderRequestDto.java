package com.pilotbroker.web.dto.trade;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    @NotBlank
    @Size(max = 10)
    private String symbol;

    @NotBlank
    private String tipo;

    @NotNull
    @Min(1)
    private Integer quantidade;

    @NotNull
    @Positive
    private Double preco;
}
