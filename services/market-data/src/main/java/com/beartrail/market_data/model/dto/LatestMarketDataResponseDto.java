package com.beartrail.market_data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestMarketDataResponseDto {
    private String status;
    private List<DataDto> data;
}
