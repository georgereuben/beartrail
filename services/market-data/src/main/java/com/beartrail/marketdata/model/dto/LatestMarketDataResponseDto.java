package com.beartrail.marketdata.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestMarketDataResponseDto {
    private String status;
    private Map<String, DataDto> data;
}
