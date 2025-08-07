package com.beartrail.marketdata.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestMarketDataResponseDto {
    @JsonProperty("status")
    private String status;
    @JsonProperty("data")
    private Map<String, DataDto> data;
}
