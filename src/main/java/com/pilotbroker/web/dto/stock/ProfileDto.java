package com.pilotbroker.web.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileDto {
    private String symbol;
    private String companyName;
    private Double price;
    private String image;
}
